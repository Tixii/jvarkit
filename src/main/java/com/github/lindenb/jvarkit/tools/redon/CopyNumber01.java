/*
The MIT License (MIT)

Copyright (c) 2014 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


History:
* 2014 creation

*/
package com.github.lindenb.jvarkit.tools.redon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import htsjdk.tribble.readers.LineIterator;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.Cigar;
import htsjdk.samtools.CigarElement;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMReadGroupRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.filter.SamRecordFilter;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.StringUtil;

import com.beust.jcommander.Parameter;
import com.github.lindenb.jvarkit.io.ArchiveFactory;
import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.lang.JvarkitException;
import com.github.lindenb.jvarkit.util.bio.fasta.ContigNameConverter;
import com.github.lindenb.jvarkit.util.bio.samfilter.SamFilterParser;
import com.github.lindenb.jvarkit.util.jcommander.Launcher;
import com.github.lindenb.jvarkit.util.jcommander.Program;
import com.github.lindenb.jvarkit.util.log.Logger;
import com.github.lindenb.jvarkit.util.picard.GenomicSequence;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;


/**
 * 
 * CopyNumber01
 *
 */
@SuppressWarnings("unused")
@Program(name="copynumber01",
	description="experimental CNV detection",
	keywords= {"cnv","bam","sam"}
	)
public class CopyNumber01 extends Launcher
	{
	private static final Logger LOG = Logger.build(CopyNumber01.class).make();
	
	/** sample Name */
	private String sampleName="SAMPLE";
	/** reference */
	private IndexedFastaSequenceFile indexedFastaSequenceFile=null;	
	/** global sam dict */
	private SAMSequenceDictionary samDictionary=null;
	/** map interval to depths and GC */
	private List<GCAndDepth> interval2row=new ArrayList<GCAndDepth>(1000);
	
	@Parameter(names={"-R","--reference"},description=INDEXED_FASTA_REFERENCE_DESCRIPTION,required=true)
	private File refFile=null;
	@Parameter(names={"-b"},description="BED capture file (optional)")
	private File bedFile=null;
	@Parameter(names={"-o","--out"},description="output base name",required=true)
	private File archiveFile=null;
	/** size of a window */
	@Parameter(names={"-w"},description="BED capture file (optional)")
	private int windowSize=150;	
	@Parameter(names={"-filter","--filter"},description=SamFilterParser.FILTER_DESCRIPTION,converter=SamFilterParser.StringConverter.class)
	private SamRecordFilter filter  = SamFilterParser.buildDefault();
	

	private ContigNameConverter contigNameConverter=null;

	/* fact: Y=depth X=GC% */
	private class GCAndDepth
		{
		int tid;
		int start;
		int end;
		double mean_depth=0;
		double gc=0;
		
		/** GC % */
		double getX()
			{
			return gc;
			}
		/** DEPTH */
		double getY()
			{
			return mean_depth;
			}
		
		public long getGenomicIndex()
			{
			long n=this.start;
			for(int i=0;i< this.tid;++i) n+=  samDictionary.getSequence(i).getSequenceLength();
			return n;
			}
		
		public String getContig()
			{
			return samDictionary.getSequence(this.tid).getSequenceName();
			}
		
		@Override
		public String toString() {
			return getContig()+":"+start+"-"+end+" GC%="+gc+" depth:"+mean_depth;
			}
		}

	private static final Comparator<GCAndDepth> sortOnXY = (a,b)->
		{
		if(a.getX() < b.getX()) return -1;
		if(a.getX() > b.getX()) return  1;

		if(a.getY() < b.getY()) return -1;
		if(a.getY() > b.getY()) return  1;
		return 0;
		};
	
	private static final Comparator<GCAndDepth> sortOnPosition = (a,b)->
		{
		int i= a.tid - b.tid;
		if(i!=0) return i;
		return a.start - b.start;
		};

			
			
	
	
	/** constructor */
	private CopyNumber01()
		{
		}
		
	
	private boolean ignoreChromosomeName(String chrom)
		{
		return !chrom.matches("(chr)?([0-9]+|X|Y)");
		}
	
	private void prefillGCPercent(
			GenomicSequence genomic,
			final int chromStart,
			final int chromEnd) throws Exception
			{
			int pos = chromStart;

			while( pos< genomic.length())
				{
				char c=genomic.charAt(pos);
				if(c=='n' || c=='N')
					{
					++pos;
					continue;
					}
				
				int pos_end = Math.min(pos + this.windowSize,chromEnd);
				
				if( (pos_end - pos) < this.windowSize*0.8)
					{
					break;
					}
				
				int total_gc=0;
				int total_bases=0;
				int n=0;
				boolean foundN=false;
				for(n=0;pos + n < pos_end && 
						pos + n < genomic.length() &&
						!foundN;
						++n)
					{
					switch(genomic.charAt(pos+n))
						{
						case 'c':case 'C':
						case 'g':case 'G':		
						case 's':case 'S':
							{
								total_gc++;
							break;
							}
						case 'n':case 'N':foundN=true;break;
						default:break;
						}
					++total_bases;
					}
				if(foundN)
					{
					pos++;
					continue;
					}
				GCAndDepth dataRow=new GCAndDepth();
				dataRow.tid=genomic.getSAMSequenceRecord().getSequenceIndex();
				dataRow.start = pos+1;
				dataRow.end = pos_end;
				
				dataRow.gc=total_gc/(double)total_bases;
				
				this.interval2row.add(dataRow);
				
				pos=pos_end;
				}
			}
	
	/** get a GC% */
	private void prefillGCPercentWithCapture(File bedFile) throws Exception
		{
		long start=System.currentTimeMillis();
		Pattern tab=Pattern.compile("[\t]");
		BufferedReader in= IOUtils.openFileForBufferedReading(bedFile);
		String line;
		Set<String> not_found=new HashSet<>(this.samDictionary.size());
		while((line=in.readLine())!=null)
			{
			if(line.trim().isEmpty() || line.startsWith("#")) continue;
			String tokens[]=tab.split(line,4);
			String chrom=tokens[0];
			if(this.samDictionary.getSequence(chrom)==null)
				{
				chrom = this.contigNameConverter.apply(chrom);
				if(chrom==null)
					{
					if(!not_found.contains(tokens[0]))
						{
						LOG.info("Cannot resolve chromosome "+tokens[0]+ " in "+line);
						not_found.add(tokens[0]);
						}
					continue;
					}
				}
			
			if(ignoreChromosomeName(chrom))
				{
				LOG.info("Ignoring "+chrom);
				continue;
				}
			String chrom_for_seq=tokens[0];//TODO
			
			
			GenomicSequence genomic=new GenomicSequence(
				this.indexedFastaSequenceFile,
				chrom_for_seq
				);
			int bedStart=Integer.parseInt(tokens[1]);
			int bedEnd=Integer.parseInt(tokens[2]);
			prefillGCPercent(genomic, bedStart, bedEnd);
			
			long now=System.currentTimeMillis();
			if( now - start > 10*1000)
				{
				LOG.info("BED:"+line+" "+this.interval2row.size());
				start=now;
				}
			}
		}
	
	
	/** get a GC% */
	private void prefillGCPercentWithoutCapture() throws Exception
		{
		for(SAMSequenceRecord ssr:this.indexedFastaSequenceFile.getSequenceDictionary().getSequences())
			{
			String chrom=ssr.getSequenceName();
			if(this.samDictionary.getSequence(chrom)==null)
				{
				chrom = this.contigNameConverter.apply(chrom);
				if(chrom==null)
					{
					LOG.info("Cannot resolve "+chrom);
					continue;
					}
				}
			
			if(ignoreChromosomeName(chrom))
				{
				LOG.info("Ignoring "+ssr.getSequenceName());
				continue;
				}
			
			
			final GenomicSequence genomic=new GenomicSequence(
				this.indexedFastaSequenceFile,
				chrom
				);
		
			prefillGCPercent(genomic,0, ssr.getSequenceLength());
			}
		}
	
	private void scanCoverage(final SAMSequenceRecord ssr,final SamReader sr)
		throws IOException
		{
		Collections.sort(this.interval2row,CopyNumber01.sortOnPosition);
		
		final SAMSequenceDictionaryProgress progress=new SAMSequenceDictionaryProgress(this.samDictionary);
		for(final GCAndDepth row:this.interval2row)
			{
			final double sum_array[]= new double[1 + row.end - row.start];
			Arrays.fill(sum_array, 0.0);
			
			final SAMRecordIterator sri=sr.query(
					this.samDictionary.getSequence(row.tid).getSequenceName(),
					row.start,
					row.end,
					false);
			while(sri.hasNext())
				{
				final SAMRecord rec = progress.watch(sri.next());
				if(rec.getReadUnmappedFlag()) continue;
				if(filter.filterOut(rec)) continue;
				final Cigar c= rec.getCigar();
				int refStart= rec.getAlignmentStart();
				for(final CigarElement ce:c.getCigarElements())
					{
					if(!ce.getOperator().consumesReferenceBases()) continue;
					if(ce.getOperator().consumesReadBases())
						{
						for(int x=0;x< ce.getLength();++x)
							{
							final int pos = refStart+x;
							if(pos >= row.start && pos <=row.end)
								{
								sum_array[pos - row.start]++;
								}
							}
						}
					refStart+=ce.getLength();
					}		
				}
			sri.close();
			row.mean_depth = new Mean().evaluate(sum_array);//TODO check
			}
		progress.finish();
		}
	
	private boolean isSexualChrom(String chrom)
		{
		return chrom.matches("(chr?)(X|Y)");
		}
	
	private UnivariateInterpolator createInterpolator()
		{	
		UnivariateInterpolator interpolator=null;
		interpolator=new LoessInterpolator(0.5,4);
		//interpolator = new NevilleInterpolator();
		return interpolator;
		}
	
	private void normalizeCoverage()
		{
		final Median medianOp =new Median();
		final Mean meanOp =new Mean();
		
		if(medianOp.evaluate(new double[]{20,1000,19})!=20)
			{
			throw new RuntimeException("boum");
			}
		
		
		int autosome_count=0;
		Collections.sort(this.interval2row,CopyNumber01.sortOnXY);
		
		for(int j=0;j< this.interval2row.size();++j)
			{
			GCAndDepth r=this.interval2row.get(j);
			if(isSexualChrom(r.getContig())) continue;
			autosome_count++;
			}

		
		
		double x[]=new double[autosome_count];
		double y[]=new double[autosome_count];

		int i=0;
		for(int j=0;j< this.interval2row.size();++j)
			{
			GCAndDepth r=this.interval2row.get(j);
			if(isSexualChrom(r.getContig())) continue;
			x[i] = r.getX();
			y[i] = r.getY();
			++i;
			}
		
		final double min_x=x[0];
		final double max_x=x[x.length-1];
		
		/* merge adjacent x having same values */
		i=0;
		int k=0;
		while(i  < x.length)
			{
			int j=i+1;
			
			while(j< x.length && Double.compare(x[i],x[j])==0)
				{
				++j;
				}
			x[k]=x[i];
			y[k]= meanOp.evaluate(y, i, j-i);
			++k;
			i=j;
			}

		/* reduce size of x et y */
		if(k != x.length)
			{
			LOG.info("Compacting X from "+x.length+" to "+k);
			x = Arrays.copyOf(x, k);
			y  =Arrays.copyOf(y, k);
			}
		
		//min depth cal
		double min_depth=Double.MAX_VALUE;

		
		UnivariateInterpolator interpolator = createInterpolator();
		UnivariateFunction  spline =  interpolator.interpolate(x, y);
		int points_removed=0;
		i=0;
		while(i<this.interval2row.size())
			{
			GCAndDepth r= this.interval2row.get(i);
			if(r.getX()< min_x || r.getX()> max_x)
				{
				this.interval2row.remove(i);
				++points_removed;
				}
			else
				{
				double norm = spline.value(r.getX());
				if(Double.isNaN(norm) || Double.isInfinite(norm)  )
					{
					LOG.info("NAN "+r);
					this.interval2row.remove(i);
					++points_removed;
					continue;
					}
				r.mean_depth -= norm; 
				min_depth=Math.min(min_depth,r.mean_depth);
				++i;
				}
			}
		LOG.info("Removed "+points_removed+" because GC% is too small (Sexual chrom)" );
		spline=null;
		
		
		//fit to min, fill new y for median calculation
		LOG.info("min:"+min_depth);

		y= new double[this.interval2row.size()];
		for(i=0;i< this.interval2row.size();++i)
			{
			GCAndDepth gc= this.interval2row.get(i);
			gc.mean_depth -= min_depth;
			y[i] = gc.mean_depth;
			}
		
		//normalize on median
		double median_depth =  medianOp.evaluate(y, 0, y.length);
		LOG.info("median:"+median_depth);
		for(i=0;i< this.interval2row.size();++i)
			{
			GCAndDepth gc= this.interval2row.get(i);
			gc.mean_depth /= median_depth;
			}
		
		
		//restore genomic order
		Collections.sort(this.interval2row,CopyNumber01.sortOnPosition);
		
		
		
		/**  smoothing values with neighbours */
		final int SMOOTH_WINDOW=5;
		y= new double[this.interval2row.size()];
		for(i=0;i< this.interval2row.size();++i)
			{
			y[i] = this.interval2row.get(i).getY();
			}
		for(i=0;i< this.interval2row.size();++i)
			{
			GCAndDepth gc= this.interval2row.get(i);
			int left=i;
			int right=i;
			while(left>0 &&
				  i-left<SMOOTH_WINDOW && 
				  this.interval2row.get(left-1).tid==gc.tid)
				{
				left--;
				}
			while(right+1< this.interval2row.size() &&
				  right-i<SMOOTH_WINDOW && 
				  this.interval2row.get(right+1).tid==gc.tid)
				{
				right++;
				}
			gc.mean_depth= medianOp.evaluate(y, left,(right-left)+1);
			}
		}
	

	
	private void saveCoverage(PrintWriter pw)
		{
		LOG.info("Dumping coverage ");
		/* header */
		pw.println("ID\tCHROM\tSTART\tEND\tGC\t"+this.sampleName);
		
		/* get data */
		for(GCAndDepth r:this.interval2row)
			{
			pw.print(r.getGenomicIndex());
			pw.print('\t');
			pw.print( this.samDictionary.getSequence(r.tid).getSequenceName());
			pw.print('\t');
			pw.print( r.start);
			pw.print('\t');
			pw.print( r.end);
			pw.print('\t');
			pw.print(r.gc);
			pw.print('\t');
			pw.print(r.mean_depth);
			pw.println();
			}
		pw.flush();
		}
	
	
	
	@Override
	public int doWork(List<String> args) {
		
		if(refFile==null)
			{
			LOG.error("Undefined REF file");
			return -1;
			}
		
		
		if(this.archiveFile==null)
			{
			LOG.error("Undefined output file.");
			return -1;
			}
	
		

		SamReader samReader = null;
		
		try
			{
			final String input = oneAndOnlyOneFile(args);
			samReader = super.openSamReader(input);
			final SAMFileHeader header = samReader.getFileHeader();
			
			
			this.samDictionary= header.getSequenceDictionary();
			if(this.samDictionary==null || this.samDictionary.isEmpty()) {
				throw new JvarkitException.DictionaryMissing(input);
				}
			
			this.sampleName =  header.getReadGroups().
					stream().
					map(RG->RG.getSample()).
					filter(S->!StringUtil.isBlank(S)).
					findFirst().orElse("SAMPLE");
			
			/* loading REF Reference */
			this.indexedFastaSequenceFile = new IndexedFastaSequenceFile(refFile);
			SAMSequenceDictionary dict=this.indexedFastaSequenceFile.getSequenceDictionary();
			if(dict==null)
				{
				throw new JvarkitException.DictionaryMissing(refFile.getPath());
				}
			
				
			this.contigNameConverter = ContigNameConverter.fromDictionaries(dict, samDictionary);
			
			
			if(this.bedFile!=null)
				{
				prefillGCPercentWithCapture(bedFile);
				}
			else
				{
				prefillGCPercentWithoutCapture();
				}
			
			ArchiveFactory archive = ArchiveFactory.open(this.archiveFile);

			
			for(final SAMSequenceRecord ssr: this.samDictionary.getSequences()) {
				scanCoverage(ssr,samReader);
				}
			samReader.close();
			
			
			/* save raw coverage */
			PrintWriter pw= archive.openWriter("raw.tsv");
			saveCoverage(pw);
			pw.flush();pw.close();
			
			normalizeCoverage();
			
			/* save normalized coverage */
			pw= archive.openWriter("normalized.tsv");
			saveCoverage(pw);
			pw.flush();pw.close();

			archive.close();
			return 0;
			}
		catch(Exception err)
			{
			LOG.error(err);
			return -1;
			}
		finally
			{
			CloserUtil.close(this.indexedFastaSequenceFile);
			}	
		}
	
	public static void main(String[] args) {
		new CopyNumber01().instanceMainWithExit(args);
		}
	}
