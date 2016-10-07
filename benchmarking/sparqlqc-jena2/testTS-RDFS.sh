/bin/rm results.tsv

echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs1
echo 'Test : benchmark/rdfs/Q9a < benchmark/rdfs/Q9c =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs1 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs2
echo 'Test : benchmark/rdfs/Q9c < benchmark/rdfs/Q9a =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs2 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs3
echo 'Test : benchmark/rdfs/Q9a < benchmark/rdfs/Q9b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs3 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs4
echo 'Test : benchmark/rdfs/Q9b < benchmark/rdfs/Q9a =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs4 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs5
echo 'Test : benchmark/rdfs/Q9b < benchmark/rdfs/Q9c =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs5 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs6
echo 'Test : benchmark/rdfs/Q9c < benchmark/rdfs/Q9b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs6 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs7
echo 'Test : benchmark/rdfs/Q9d < benchmark/rdfs/Q9e =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs7 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs8
echo 'Test : benchmark/rdfs/Q9e < benchmark/rdfs/Q9d =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs8 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs9
echo 'Test : benchmark/rdfs/Q10b < benchmark/rdfs/Q10d =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs9 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs10
echo 'Test : benchmark/rdfs/Q10d < benchmark/rdfs/Q10b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs10 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs11
echo 'Test : benchmark/rdfs/Q10e < benchmark/rdfs/Q10b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs11 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs12
echo 'Test : benchmark/rdfs/Q10b < benchmark/rdfs/Q10e =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs12 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs13
echo 'Test : benchmark/rdfs/Q11b < benchmark/rdfs/Q11c =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs13 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs14
echo 'Test : benchmark/rdfs/Q11c < benchmark/rdfs/Q11b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs14 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs15
echo 'Test : benchmark/rdfs/Q11b < benchmark/rdfs/Q11d =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs15 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs16
echo 'Test : benchmark/rdfs/Q11d < benchmark/rdfs/Q11b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs16 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs17
echo 'Test : benchmark/rdfs/Q11c < benchmark/rdfs/Q11d =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs17 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs18
echo 'Test : benchmark/rdfs/Q11d < benchmark/rdfs/Q11c =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs18 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs19
echo 'Test : benchmark/rdfs/Q11b < benchmark/rdfs/Q11a =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs19 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs20
echo 'Test : benchmark/rdfs/Q11a < benchmark/rdfs/Q11b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs20 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs21
echo 'Test : benchmark/rdfs/Q11e < benchmark/rdfs/Q11a =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs21 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs22
echo 'Test : benchmark/rdfs/Q11a < benchmark/rdfs/Q11e =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs22 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs23
echo 'Test : benchmark/rdfs/Q13a < benchmark/rdfs/Q13b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs23 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs24
echo 'Test : benchmark/rdfs/Q13b < benchmark/rdfs/Q13a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs24 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs25
echo 'Test : benchmark/rdfs/Q13a < benchmark/rdfs/Q13c =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs25 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs26
echo 'Test : benchmark/rdfs/Q13c < benchmark/rdfs/Q13a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs26 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs27
echo 'Test : benchmark/rdfs/Q13b < benchmark/rdfs/Q13c =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs27 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQrdfs#rdfs28
echo 'Test : benchmark/rdfs/Q13c < benchmark/rdfs/Q13b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/ucqrdfs.rdf -n rdfs28 -f asc -o results.tsv
echo 'Results are in results.tsv'

mv results.tsv TS-RDFS.tsv
