#!/bin/bash

./run-benchmark.sh -q1 'SELECT * { ?s ?p ?o }' -q2 'SELECT * { ?s ?p ?o }' -s JSAC

