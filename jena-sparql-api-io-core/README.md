## Jena Sparql Api: IO Core

Special purpose IO functionality

* Binary search on byte-wise sorted files with Graph wrapper for NTriple files
  Well optimized implementation using Java's memory-mapped IO
  Performance of scanning a file about 50% slower than Files.lines() - on 3GB file with 10M triples, with 3M distinct subjects:
  Files.lines(): ~6 sec, BinarySearchOverFile: ~9 sec
  In contrast, time required to actually parsing lines to triples using Jena's triple reader: ~100 sec
  Lookups with all 3M subjects sorted: 752 sec
  Lookups with all 3M sujects shuffled: 1193 sec

* RDFNode (de-)serializers which allow for serializing RDF with a dedicated root node
  Useful e.g. to pass RDF resources to Spark workers as done in the conjure module



## System Process Abstraction
Transparently switch between native and system-call data processors.

## Motivation
For many data processing tasks, such as conversion and encoding, although there may exist Java implementations, there often also exist highly optimized system tools that perform significantly faster. For example, counting lines with `wc -l` is at least twice as fast as a multi-threaded memory mapped io Java implementation. Other typical tools are `bzip2`, `grep`, etc. So why not transparently use the faster tooling if it is available and otherwise fall back to native tooling?


### Example 1


### Example 2
This example shows how to create a flow that upon subscription yields an appropriate input stream:
The initial file is first `cat`, then `sort`ed, then `grep`ped and an input stream to the result is supplied back to java.
If the flow does not complete within the timeout the involved system processes will be terminated.

Note, that that `sort.mapStreamToPath` writes out a file, and multiple subscriptions on the composite flowable would result in conflicting writes
of the file. This means, that you need to take care on how to design flows - e.g. whether new filenames should be picked if they already exist,
or whether new flows should wait for the first one to complete.



```java
Path path = Paths.get("lines.txt");
PipeTransformRx cat = PipeTransformRx.fromSysCallStreamToStream("/bin/cat");
PipeTransformRx sort = PipeTransformRx.fromSysCallStreamToStream("/usr/bin/sort");
PipeTransformRx filter = PipeTransformRx.fromSysCallStreamToStream("/bin/grep", "size");

InputStream in =
  Single.just(path)
    .compose(cat.mapPathToStream())
    .compose(sort.mapStreamToPath(Paths.get("/tmp/foo.bar")))
    .compose(filter.mapPathToStream())
    // The timeout refers to when the InputStream becomes available i.e. the time when foo.bar has been written
    // See below to set a timeout on data becoming ready on the input stream
    .timeout(10, TimeUnit.SECONDS)
    .blockingGet();
```


Set a timeout for data (lines) to become available:
```
  Flowable<String> lines = Flowable.generate(
    () -> new BufferedReader(new InputStreamReader(in)),
    (it, e) -> {
      String line = it.readLine();
      if(line == null) {
        e.onComplete();
      } else {
        e.onNext(line);
      }
    },
    AutoCloseable::close);

    List<String> strs = lines
      // This will raise an exception if it takes longer than 5 seconds to obtain the next line from the input stream
      .timeout(5, TimeUnit.SECONDS)
      .toList()
      .blockingGet();
```


## Sorted Blocked Turtle
This is just a fun little idea and I consider it more of a gimmick - its not implemented (yet)

It's turtle suitable for binary search

Features

* Often a lot smaller than n-triples
* Usually better readable than n-triples
* Makes for nice git diffs
* In contrast to HDT it is a text format
* One can do do binary search on files
* This might be useful for (federated) git-based (data) catalogs, which are small enough so that it makes sense to keep them in text,
but numerous so that lookups for whether a specific dataset is listed might still take annoyingly long if catalogs have to be fully read first and there is only a single catalog that is large enough to notably delay the process.


Format 1: Lines with non-abbreviated subjects

* Suject IRIs must never be abbreviated
* Subjects must / Only sujects may / start after a newline or the beginning of the file. So if a line starts with < or _ its either a IRI or a blank node

* Pro: No redundancy
* Contra: If object IRIs are abbreviated but subjects are not, then regex search sucks.

Example:
```turtle
<http:foobar>
       a
                 ns2:bar 
.

<http:foocar> 
```


Format 2: Turtle comment with block marker
```
_:bode # _ already functions as block marker

# }{|[ <http:foobar>
eg:foobar


```

