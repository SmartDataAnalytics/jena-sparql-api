#!/bin/bash

java -cp "sparqlqc-dataset-analysis/target/sparqlqc-dataset-analysis-1.0.0-SNAPSHOT-jar-with-dependencies.jar" "org.aksw.sparqlqc.analysis.dataset.MainSparqlQcBenchmark" "$@"

