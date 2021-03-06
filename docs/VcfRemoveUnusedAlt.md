# VcfRemoveUnusedAlt

Remove unused ALT allele if there is no genotype with this alt, or there is no sample but AC=0


## Usage

```
Usage: vcfremoveunusedalt [options] Files
  Options:
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -neverspan, --neverspan
      Remove ALL spanning deletions '*'. VCF must have no genotype.
      Default: false
    -onespan, --onespan
      Don't print the variant if the only remaining allele is  '*'
      Default: false
    -o, --out
      Output file. Optional . Default: stdout
    --version
      print version and exit

```


## Keywords

 * vcf
 * genotype


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
$ make vcfremoveunusedalt
```

The *.jar libraries are not included in the main jar file, [so you shouldn't move them](https://github.com/lindenb/jvarkit/issues/15#issuecomment-140099011 ).
The required libraries will be downloaded and installed in the `dist` directory.

Experimental: you can also create a [fat jar](https://stackoverflow.com/questions/19150811/) which contains classes from all the libraries, on which your project depends (it's bigger). Those fat-jar are generated by adding `standalone=yes` to the gnu make command, for example ` make vcfremoveunusedalt standalone=yes`.

### edit 'local.mk' (optional)

The a file **local.mk** can be created edited to override/add some definitions.

For example it can be used to set the HTTP proxy:

```
http.proxy.host=your.host.com
http.proxy.port=124567
```
## Source code 

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VcfRemoveUnusedAlt.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/misc/VcfRemoveUnusedAlt.java)


<details>
<summary>Git History</summary>

```
Wed Dec 20 15:17:02 2017 +0100 ; prettysam with VCF file ; https://github.com/lindenb/jvarkit/commit/b87d8f2413a2b5765b1560da800dbf3fe30c8701
Wed Dec 20 09:20:51 2017 +0100 ; fix vcfremoveunusedalt ; https://github.com/lindenb/jvarkit/commit/02b15e77bdd681fafa9da32a5ee602f9a0345975
Tue Dec 19 19:36:40 2017 +0100 ; VcfRemoveUnusedAlt ; https://github.com/lindenb/jvarkit/commit/ce5bb48bf7ee51d8d70a0f779f08556ee07c82f3
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfremoveunusedalt** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)


## Motivation

when using gatk SelectVariants with sample names (-sn) some alleles specific of the samples than have been removed, remain in the vcf.

## SNPEFF / VEP

this tool removes unused annotations from SNPEFF(ANN=) and VEP.

## Example

```bash
$ cat in.vcf
(...)
chr1	7358	.	ACTT	*,A	1313.61	PASS	AC=0,10;AF=0,0.005828;AN=1716

$ java -jar dist/vcfremoveunusedalt.jar  in.vcf | grep -w 17358 -m1
chr1	7358	.	ACTT	A	1313.61	PASS	AC=10;AF=0.005828;AN=1716
```

