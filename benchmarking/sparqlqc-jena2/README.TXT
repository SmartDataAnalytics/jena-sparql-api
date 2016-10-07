# JE: 17/07/2013

# ====================================================================================================
# Compiling

$ ant compileall    -> compiles everything
$ ant jar           -> incremental compilation

# ====================================================================================================
# Running

Everything depends on the content of lib/
- if it contains javabdd...jar => this can run AFMU, not TreeSolver
- if it contains libbuddy.X (X depending on the architecture) => the BDD code will be native

$ sh runtests.sh   -> should do everything

Files are here with the correct timeout and memory size, do not regenerate without 
knowing what you are doing.

# This run specific tests individually

$ java -Xms1512m -Xmx2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -x benchmark/cqnoproj.rdf -n nop6 -f asc
$ java -Xms1512m -Xmx2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -n nop6 -f asc
$ java -Xms1512m -Xmx2024m -Djava.library.path=lib -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -x benchmark/cqnoproj.rdf -n nop6 -f asc

# To generate these types of scripts

$ java -jar lib/containmenttester.jar fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -x benchmark/cqnoproj.rdf -f shell -o scripts/testSA-NOP.sh
                    -> generate test files (change arguments).

# ====================================================================================================
# Generating web site

$ java -cp lib/containmenttester.jar fr.inrialpes.tyrexmo.testqc.SuiteTool benchmark/cqnoproj.rdf -o html/CQNoProj.html
$ java -cp lib/containmenttester.jar fr.inrialpes.tyrexmo.testqc.SuiteTool benchmark/ucqproj.rdf -o html/UCQProj.html
$ java -cp lib/containmenttester.jar fr.inrialpes.tyrexmo.testqc.SuiteTool benchmark/ucqrdfs.rdf -o html/UCQrdfs.html

# ====================================================================================================
# Analysing queries

$ java -cp lib/containmenttester.jar fr.inrialpes.tyrexmo.queryanalysis.Analysis querylog

with querylog the name of a directory containing one sparql query per file.

# ====================================================================================================
# Content:

benchmark     description of the benchmark data sets

lib           containmenttester.jar -> the framework with all wrappers
              lmusolver.jar -> the Lmu-calculus solver, aka TreeSolver
              subsum.jar -> the SPARQL Algebra solver stuff that works
              afmusolver.jar -> the Japanese Mu-calculus solver (hopefully non touched)

src the source for QC testing including wrappers
  fr.inrialpes.tyrexmo.queryanalysis -> common tools for analysing and manipulating queries
  				     + includes a splitlog.pl perl scripts for extracting queries from logs
  fr.inrialpes.tyrexmo.testqc.TestContain -> the main class for testing containment (called by the jat above).
  fr.inrialpes.tyrexmo.testqc.ContainmentSolver -> the minimal interface wrapper have to implement
  fr.inrialpes.tyrexmo.qcwrapper.afmu.AFMUContainmentWrapper -> our wrapper around AFMU solver
  fr.inrialpes.tyrexmo.qcwrapper.lmu.TreeSolverWrapper -> same for L_mu solver
  fr.inrialpes.tyrexmo.qcwrapper.sparqlalg.SPARQLAlgebraWrapper -> same for the SPARQL-Algebra solver

