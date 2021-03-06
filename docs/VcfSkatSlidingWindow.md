# VcfSkatSlidingWindow

SkatFactory Over genome using a sliding window.


## Usage

```
Usage: vcfskatslidingwindow [options] Files
  Options:
    -C, --contig
      limit to this contig(s)
      Default: []
    --contigWinLength
      window size when splitting per contig
      Default: 1000
    --contigWinShift
      window shift when splitting per contig
      Default: 500
    -h, --help
      print help and exit
    --helpFormat
      What kind of help
      Possible Values: [usage, markdown, xml]
    -j, --jobs
      When -exec is specified, use <n> jobs. A value lower than 1 means use 
      all procs available.
      Default: 1
    -o, --output
      Output file. Optional . Default: stdout
    -ped, --pedigree
      A pedigree is a text file delimited with tabs. No header. Columns are 
      (1) Family (2) Individual-ID (3) Father Id or '0' (4) Mother Id or '0' 
      (5) Sex : 1 male/2 female / 0 unknown (6) Status : 0 unaffected, 1 
      affected,-9 unknown  If not defined, I will try to extract the pedigree 
      from  the VCFheader.
    --skat-adjusted
      SKAT adjusted
      Default: false
    --skat-optimized
      SKAT optimized (SKATO)/ davies method.
      Default: false
    --skat-random-seed
      Rstats value for `set.seed`. -1 == use random
      Default: -1
    --version
      print version and exit

```


## Keywords

 * vcf
 * pedigree
 * skat
 * burden


## Compilation

### Requirements / Dependencies

* java compiler SDK 1.8 http://www.oracle.com/technetwork/java/index.html (**NOT the old java 1.7 or 1.6**) . Please check that this java is in the `${PATH}`. Setting JAVA_HOME is not enough : (e.g: https://github.com/lindenb/jvarkit/issues/23 )
* GNU Make >= 3.81
* curl/wget
* git
* xsltproc http://xmlsoft.org/XSLT/xsltproc2.html (tested with "libxml 20706, libxslt 10126 and libexslt 815")


### Download and Compile

```bash
$ git clone "https://github.com/lindenb/jvarkit.git"
$ cd jvarkit
$ make vcfskatslidingwindow
```

The *.jar libraries are not included in the main jar file, so you shouldn't move them (https://github.com/lindenb/jvarkit/issues/15#issuecomment-140099011 ).
The required libraries will be downloaded and installed in the `dist` directory.

### edit 'local.mk' (optional)

The a file **local.mk** can be created edited to override/add some definitions.

For example it can be used to set the HTTP proxy:

```
http.proxy.host=your.host.com
http.proxy.port=124567
```
## Source code 

[https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/skat/VcfSkatSlidingWindow.java](https://github.com/lindenb/jvarkit/tree/master/src/main/java/com/github/lindenb/jvarkit/tools/skat/VcfSkatSlidingWindow.java)


<details>
<summary>Git History</summary>

```
Thu Oct 19 19:18:50 2017 +0200 ; new thread model ; https://github.com/lindenb/jvarkit/commit/978e8057afdfecf2b1b10e292810a4fdd3deeec2
Thu Oct 19 17:30:16 2017 +0200 ; continue skat tools ; https://github.com/lindenb/jvarkit/commit/c5170bb590e5638e903eeffaedf1cd6eebb315d9
Thu Oct 19 15:53:48 2017 +0200 ; skat continue ; https://github.com/lindenb/jvarkit/commit/5c71e1cbcacfd5b034a49580655db7066d83c50e
```

</details>

## Contribute

- Issue Tracker: [http://github.com/lindenb/jvarkit/issues](http://github.com/lindenb/jvarkit/issues)
- Source Code: [http://github.com/lindenb/jvarkit](http://github.com/lindenb/jvarkit)

## License

The project is licensed under the MIT license.

## Citing

Should you cite **vcfskatslidingwindow** ? [https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md](https://github.com/mr-c/shouldacite/blob/master/should-I-cite-this-software.md)

The current reference is:

[http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)

> Lindenbaum, Pierre (2015): JVarkit: java-based utilities for Bioinformatics. figshare.
> [http://dx.doi.org/10.6084/m9.figshare.1425030](http://dx.doi.org/10.6084/m9.figshare.1425030)



