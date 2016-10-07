/bin/rm results.tsv

echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop1
echo 'Test : benchmark/noprojection/Q1a < benchmark/noprojection/Q1b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop1 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop2
echo 'Test : benchmark/noprojection/Q1b < benchmark/noprojection/Q1a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop2 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop3
echo 'Test : benchmark/noprojection/Q2a < benchmark/noprojection/Q2b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop3 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop4
echo 'Test : benchmark/noprojection/Q2b < benchmark/noprojection/Q2a =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop4 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop5
echo 'Test : benchmark/noprojection/Q3a < benchmark/noprojection/Q3b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop5 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop6
echo 'Test : benchmark/noprojection/Q3b < benchmark/noprojection/Q3a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop6 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop7
echo 'Test : benchmark/noprojection/Q4c < benchmark/noprojection/Q4b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop7 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop8
echo 'Test : benchmark/noprojection/Q4b < benchmark/noprojection/Q4c =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop8 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop9
echo 'Test : benchmark/noprojection/Q6a < benchmark/noprojection/Q6b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop9 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop10
echo 'Test : benchmark/noprojection/Q6b < benchmark/noprojection/Q6a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop10 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop11
echo 'Test : benchmark/noprojection/Q6a < benchmark/noprojection/Q6c =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop11 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop12
echo 'Test : benchmark/noprojection/Q6c < benchmark/noprojection/Q6a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop12 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop13
echo 'Test : benchmark/noprojection/Q6b < benchmark/noprojection/Q6c =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop13 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop14
echo 'Test : benchmark/noprojection/Q6c < benchmark/noprojection/Q6b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop14 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop15
echo 'Test : benchmark/noprojection/Q7a < benchmark/noprojection/Q7b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop15 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop16
echo 'Test : benchmark/noprojection/Q7b < benchmark/noprojection/Q7a =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop16 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop17
echo 'Test : benchmark/noprojection/Q8a < benchmark/noprojection/Q8b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop17 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop18
echo 'Test : benchmark/noprojection/Q8b < benchmark/noprojection/Q8a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop18 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop19
echo 'Test : benchmark/noprojection/Q9a < benchmark/noprojection/Q9b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop19 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop20
echo 'Test : benchmark/noprojection/Q9b < benchmark/noprojection/Q9a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop20 -f asc -o results.tsv
echo 'Results are in results.tsv'

mv results.tsv SA-NOP.tsv
