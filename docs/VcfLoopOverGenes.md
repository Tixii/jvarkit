# VcfLoopOverGenes

Generates a BED file of the Genes in an annotated VCF, loop over those genes and generate a VCF for each gene, then execute an optional command.


## Usage

```
Usage: vcfloopovergenes [options] Files
  Options:
    -compress, --compress
      generate VCF.gz files
      Default: false
    --contigWinLength
      [20171018] window size when splitting per contig
      Default: 1000
    --contigWinShift
      [20171018] window shift when splitting per contig
      Default: 500
    -delete, --delete
      if a command if executed with '-exec', delete the file after the 
      completion of the command.
      Default: false
    -e, -exec, --exec
      When saving the VCF to a directory. Execute the following command line. 
      The words __PREFIX__  __CONTIG__ (or __CHROM__ ) __ID__ __NAME__ 
      __SOURCE__ __VCF__ __START__ __END__ will be replaced by their values.
      Default: <empty string>
    -g, --gene, -gene, --genes
      Loop over the gene file. If not defined VCF will be scanned for SnpEff 
      annotations and output will be a BED file with the gene names and 
      provenance.If defined, I will create a VCF for each Gene.
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -j, --jobs
      When -exec is specified, use <n> jobs. A value lower than 1 means use 
      all procs available.
      Default: 1
    --maxRecordsInRam
      When writing  files that need to be sorted, this will specify the number 
      of records stored in RAM before spilling to disk. Increasing this number 
      reduces the number of file  handles needed to sort a file, and increases 
      the amount of RAM needed
      Default: 50000
    -o, --output
      For gene.bed: a File or stdout. When creating a VCF this should be a zip 
      file or an existing directory.
    -p, -prefix, --prefix
      File prefix when saving the individual VCF files.
      Default: <empty string>
    -r, --region
      An interval as the following syntax : "chrom:start-end" or 
      "chrom:middle+extend"  or "chrom:start-end+extend".A program might use a 
      Reference sequence to fix the chromosome name (e.g: 1->chr1)
      Default: <empty string>
    --snpEffNoIntergenic
      [20170711] when using SNPEFF annotations ignore intergenic variants. 
      Makes things faster if you're only working with protein-things.
      Default: false
    --splitMethod
      [20170711] How to split primary vcf
      Default: Annotations
      Possible Values: [Annotations, VariantSlidingWindow, ContigSlidingWindow]
    --tmpDir
      tmp working directory. Default: java.io.tmpDir
      Default: []
    --variantsWinCount
      [20170711] when split per count of variants, put at most this number of 
      variants in the chunk.
      Default: 1000
    --variantsWinShift
      [20170711] when split per count of variants, shift the window by this 
      number of variants.
      Default: 500
    --version
      print version and exit

```


## Keywords

 * vcf
 * gene
 * burden


## Compilation

### Requirements / Dependencies

* java [compiler SDK 1.8](http://www.oracle.com/technetwork/java/index.html) (**NOT the old java 1.7 or 1.6**) and avoid OpenJdk, use the java from Oracle. Please check that this java is in the `${PATH}`. Setting JAVA_HOME is not enough : (e.g: https://github.com/lindenb/jvarkit/issues/23 )
* GNU Make >= 3.81
* curl/wget
* git
* xsltproc http://xmlsoft.org/XSLT/xsltproc2.html (tested with "libxml 20706, libxslt 10126 and libexslt 815")


### Download and Compile

```bash
$ git clone "https://github.com/lindenb/jvarkit.git"
$ cd jvarkit
$ make vcfloopovergenes
```

The *.jar libraries are not included in the main jar file, [so you shouldn't move them](https://github.com/lindenb/jvarkit/issues/15#issuecomment-140099011 ).
The required libraries will be downloaded and installed in the `dist` directory.

Experimental: you can also create a [fat jar](https://stackoverflow.com/questions/19150811/) which contains classes from all the libraries, on which your project depends (it's bigger). Those fat-jar are generated by adding `standalone=yes` to the gnu make command, for example ` make vcfloopovergenes standalone=yes`.

### edit 'local.mk' (optional)

The a file **local.mk** can be created edited to override/add some definitions.

For example it can be used to set the HTTP proxy:

```
http.proxy.host=your.host.com
http.proxy.port=124567
```
## Source code 

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/burden/VcfLoopOverGenes.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/burden/VcfLoopOverGenes.java)


<details>
<summary>Git History</summary>

```
Wed Oct 18 17:17:31 2017 +0200 ; add skat, add splitter by contig ; https://github.com/lindenb/jvarkit/commit/fd4408e3d1bbd312db8b3329d59ceb12a9d0dc29
Fri Aug 4 16:40:02 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/57f08e720a97f952bab81961431d83accdefeae3
Thu Jul 20 16:08:20 2017 +0200 ; changes to vcfloopovergenes + cnv01 ; https://github.com/lindenb/jvarkit/commit/1ef43b445fd0849725e0148d0431587aea43040b
Tue Jul 11 17:57:33 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/1f248bc7f1fd8a0824bb65a4c67eb052d5a6e381
Thu Jun 29 17:31:10 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/1aac040bed918f89b1ce68b2c8f7a0c6d5cfddd0
Tue Jun 27 17:36:29 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/278970358111f7e3eca02e77d9a238321668a2dd
Mon Jun 26 17:29:03 2017 +0200 ; burden ; https://github.com/lindenb/jvarkit/commit/a3b7abf21d07f0366e81816ebbb2cce26b2341e7
Sun Jun 25 16:43:47 2017 +0200 ; loop over gene in region ; https://github.com/lindenb/jvarkit/commit/a491397b51bb7149fcdccad8c5dab9bdf6fd83fa
Fri Jun 23 15:26:55 2017 +0200 ; updated vcf2multiallele ; https://github.com/lindenb/jvarkit/commit/775e8ddcc38a3e283cf49d9287b06510d7634e31
Thu Jun 22 15:28:11 2017 +0200 ; cont ; https://github.com/lindenb/jvarkit/commit/cb19128044b14265ee78325199515e2121904871
Thu Jun 22 13:16:05 2017 +0200 ; vcfloopovergenes ; https://github.com/lindenb/jvarkit/commit/aa4a6f29c853efddcee5678f9441d9994a2deee6
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfloopovergenes** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Example

Generate the bed file from a VCF annotated with SnpEff

```
$ java -jar dist/vcfloopovergenes.jar -p KARAKA input.vcf.gz > genes.bed 
$ head jeter.bed
13	62807	62808	KARAKA.000000002	2V3477:ENST000002607	ANN_FeatureID	1
13	11689	20735	KARAKA.000000004	AC07.1	ANN_GeneName	30
13	75803	90595	KARAKA.000000006	ENSG000000781	ANN_GeneID	284
13	44306	68961	KARAKA.000000008	ENSG00044491	ANN_GeneID	1545
(...)
```

Generate the VCFs:


```
 $ java -jar dist/vcfloopovergenes.jar -p KARAKA -g genes.bed -o tmp input.vcf.gz
 

 $ head tmp/KARAKA.manifest.txt 
KARAKA.3_KARAKA.000000001.vcf
KARAKA.3_KARAKA.000000002.vcf
KARAKA.3_KARAKA.000000003.vcf
KARAKA.3_KARAKA.000000004.vcf
(..)
 ```

 ```
$ ls tmp/*.vcf | head
tmp/KARAKA.3_KARAKA.000000001.vcf
tmp/KARAKA.3_KARAKA.000000002.vcf
tmp/KARAKA.3_KARAKA.000000003.vcf
tmp/KARAKA.3_KARAKA.000000004.vcf
(...)
```

### Example 'Exec'

```
$ java -jar dist/vcfloopovergenes.jar -p KARAKA -g genes.bed -exec "echo __ID__ __PREFIX__ __VCF__ __CONTIG__" -o tmp input.vcf.gz 
KARAKA.000000001 KARAKA. tmp/KARAKA.000000001.vcf 3
KARAKA.000000002 KARAKA. tmp/KARAKA.000000002.vcf 3
KARAKA.000000003 KARAKA. tmp/KARAKA.000000003.vcf 3
KARAKA.000000004 KARAKA. tmp/KARAKA.000000004.vcf 3
KARAKA.000000005 KARAKA. tmp/KARAKA.000000005.vcf 3
KARAKA.000000006 KARAKA. tmp/KARAKA.000000006.vcf 3
KARAKA.000000007 KARAKA. tmp/KARAKA.000000007.vcf 3
KARAKA.000000008 KARAKA. tmp/KARAKA.000000008.vcf 3
KARAKA.000000009 KARAKA. tmp/KARAKA.000000009.vcf 3
KARAKA.000000010 KARAKA. tmp/KARAKA.000000010.vcf 3
KARAKA.000000011 KARAKA. tmp/KARAKA.000000011.vcf 3
KARAKA.000000012 KARAKA. tmp/KARAKA.000000012.vcf 3
```


### Example 'Exec'

execute the following Makefile 'count.mk' for each gene:

```make
all:${MYVCF}
	echo -n "Number of variants in $< : " && grep -vE '^#' $< | wc -l	
```

```
java -jar dist/vcfloopovergenes.jar \
	-p MATILD -gene genes.bed input.vcf.gz \
	-exec 'make -f count.mk MYVCF=__VCF__' -o tmp
```




