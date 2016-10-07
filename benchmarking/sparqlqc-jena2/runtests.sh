#!/bin/sh

DIR=RES`date +%Y%m%d%Hh%M`
mkdir $DIR

mv lib/jena-arq-2.10.0.jar .
mv lib/jena-core-2.10.0.jar .
mv lib/jena-iri-0.9.5.jar .
sh testSA-NOP.sh
mv SA-NOP.tsv $DIR/SA-NOP$BDD.tsv
mv jena-arq-2.10.0.jar lib/
mv jena-core-2.10.0.jar lib/
mv jena-iri-0.9.5.jar lib/

sh testAFMU-NOP.sh
mv AFMU-NOP.tsv $DIR/AFMU-NOP$BDD.tsv

mv lib/javabdd-1.0b2.jar .
sh testTS-NOP.sh
mv TS-NOP.tsv $DIR/TS-NOP$BDD.tsv
mv javabdd-1.0b2.jar lib/

sh testAFMU-RDFS.sh
mv AFMU-RDFS.tsv $DIR/AFMU-RDFS$BDD.tsv

sh testAFMU-UCQ.sh
mv AFMU-UCQ.tsv $DIR/AFMU-UCQ$BDD.tsv

mv lib/javabdd-1.0b2.jar .
sh testTS-RDFS.sh
mv TS-RDFS.tsv $DIR/TS-RDFS$BDD.tsv
mv javabdd-1.0b2.jar lib/

mv lib/javabdd-1.0b2.jar .
sh testTS-UCQ.sh
mv TS-UCQ.tsv $DIR/TS-UCQ$BDD.tsv
mv javabdd-1.0b2.jar lib/

