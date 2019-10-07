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

