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

