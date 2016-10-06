/bin/rm results.tsv

echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p1
echo 'Test : benchmark/projection/Q1a < benchmark/projection/Q1b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p1 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p2
echo 'Test : benchmark/projection/Q1b < benchmark/projection/Q1a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p2 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p3
echo 'Test : benchmark/projection/Q2a < benchmark/projection/Q2b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p3 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p4
echo 'Test : benchmark/projection/Q2b < benchmark/projection/Q2a =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p4 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p5
echo 'Test : benchmark/projection/Q3a < benchmark/projection/Q3b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p5 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p6
echo 'Test : benchmark/projection/Q3b < benchmark/projection/Q3a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p6 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p7
echo 'Test : benchmark/projection/Q4c < benchmark/projection/Q4b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p7 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p8
echo 'Test : benchmark/projection/Q4b < benchmark/projection/Q4c =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p8 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p9
echo 'Test : benchmark/projection/Q5a < benchmark/projection/Q5b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p9 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p10
echo 'Test : benchmark/projection/Q5b < benchmark/projection/Q5a =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p10 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p11
echo 'Test : benchmark/projection/Q6a < benchmark/projection/Q6b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p11 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p12
echo 'Test : benchmark/projection/Q6b < benchmark/projection/Q6a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p12 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p13
echo 'Test : benchmark/projection/Q6a < benchmark/projection/Q6c =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p13 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p14
echo 'Test : benchmark/projection/Q6c < benchmark/projection/Q6a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p14 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p15
echo 'Test : benchmark/projection/Q7a < benchmark/projection/Q7b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p15 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p16
echo 'Test : benchmark/projection/Q7b < benchmark/projection/Q7a =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p16 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p17
echo 'Test : benchmark/projection/Q8a < benchmark/projection/Q8b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p17 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p18
echo 'Test : benchmark/projection/Q8b < benchmark/projection/Q8a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p18 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p19
echo 'Test : benchmark/projection/Q9a < benchmark/projection/Q9b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p19 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p20
echo 'Test : benchmark/projection/Q9b < benchmark/projection/Q9a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p20 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p21
echo 'Test : benchmark/projection/Q9c < benchmark/projection/Q9b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p21 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p22
echo 'Test : benchmark/projection/Q9b < benchmark/projection/Q9c =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p22 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p23
echo 'Test : benchmark/projection/Q10a < benchmark/projection/Q10b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p23 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p24
echo 'Test : benchmark/projection/Q10b < benchmark/projection/Q10a =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p24 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p25
echo 'Test : benchmark/projection/Q11a < benchmark/projection/Q11b =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p25 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p26
echo 'Test : benchmark/projection/Q11b < benchmark/projection/Q11a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p26 -f asc -o results.tsv -t 20000
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p27
echo 'Test : benchmark/projection/Q12a < benchmark/projection/Q12b =======> true'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p27 -f asc -o results.tsv
echo '--------------------------------------------'
echo http://sparql-qc-bench.inrialpes.fr/UCQProj#p28
echo 'Test : benchmark/projection/Q12b < benchmark/projection/Q12a =======> false'
java -Xms2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/ucqproj.rdf -n p28 -f asc -o results.tsv
echo 'Results are in results.tsv'

mv results.tsv AFMU-UCQ.tsv
