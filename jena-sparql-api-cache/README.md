
### Outline of the Approach

* Parse the query (right now we only consider quad patterns and filters)
* For each quad in the quad pattern, create a constraint summary
 * This is the subset of the filter's CNF which only affects the given quad
* Normalize the constraint summary (replace variables by the components (g, s, p, o)
* Map the constraint summary
* For each variable in the pattern, create its join summary:
  This is the set of occurrences in the constraint summaries's component



# Unsorted thoughts below



#### Join Summary
Given a set of constraint summaries, a join summary for a given variable is
a set of pairs of constraint summary and component

Example: { c1.g, c1.s, c2.g, c3.p}





Given a BGPF a and a set of BGPFs X, find all minimal sets of overlapping
candidates { W | W subset X and Union(w in W) is isomorph to a}


* For each quad pattern in the query, find the most selective one (the one having fewest candidates)


### Preliminaries / Terminology

Top-Level QP: Quad patterns that are not subsumed by other quad patterns


### Formalization
Sub Goal: Given a quad pattern Q and a set of quad patterns \P, find:
{ (P, m) | P in \P and there exists a mapping m of variables such that m(P) subset of Q)

Goal: Given a quad pattern Q and a set of candidate pairs C = {(P, m)},
find all sets such that the query is uniquely covered with candidates:
{ X | with X subset Pow(C) and ... }


### Access Paths
Finding candidate BGPs can be done in the following two ways:
- bottom-up: starting form the triple patterns, we lookup corresponding BGPS
- top-down: we first check which corresponding BGP could cover the query by the number of quads
  then we check whether there is at least some overlap with the query BGP.
  if there is an overlap, but some quads are not part of the query, we can navigate down the subsumption hierachy



### Considerations for Corner Cases
* SPARQL Queries may contain over 100 triple patterns
  We could map constraint-quads to a treemap where queries are ordered by number of triples
  This way it would be easy to exclude queries having more patterns than a given threshold
  Map<ConstraintQuad, TreeMap<Integer, Set<Patterns>>

* Degenerated Quads:
  { ?s ?s ?s ?s . Filter(regex(str(?s), 'dbpedia')) }
 [ ?s ?s ?s ?s] ->
  If we assume we can deal with normalize for symmetry, we still expand to this:
  { (g = s), (g = p), (g = o), (s = p), (s = o), (p = o) } union
  { regex(str(?g), 'dbpedia')), ... regex(str(?o), 'dbpedia')) }

  Could be done, but looks super ugly.

  _Essentially we need to normalize the constraint expressions_

  ?var -> C where C subset { g, s, p, o} i.e. the components where the variable appears in

* Equality of variables
  If we encounter (?s = ?o), we can replace one variable in all other expressions.



  Question: Can we replace ?o with ?s in general - e.g. if there are optionals?
  ?s a A
  Optional {
     ?s a ?o
  }
  Filter(?s = ?o)


* Overlapping patterns

   Given the caches for ?s with:
   { ?s a Person . ?s hasPet ?p . ?p a Cat }
   { ?s a Person . ?s hasPet ?p . ?p a Dog }

   Question: Is answering the following query the intersection of the cache patterns?
   { ?s a Person . ?s hasPet ?a . ?a a Cat . ?s hasPet ?b . ?b a Dog}
   What is the formal difference if ?b was renamed to ?a ?


* Multiple overlaps of the same pattern
    Cache: { ?s ?p ?o . ?o ?x ?y } -> [ ?s -> [ ... ] ]


    Query: { ?a ?b ?c . ?c ?d ?e . ?e ?f ?g . ?g ?h ?i . ?i ?j ?k}

    Step1: { ?a ?b ?c . ?c ?d ?e . ?e ?f ?g . Filter(?g In (...)) }
    Step2: { ?a ?b ?c . Filter(?c In (...)) ... (breaks)
        / Can't replace pattern again, because a non-cache variable (in this case ?g) is referenced


   Implications
   -> For all variables in the query pattern, check whether they have occurrences outside of the
      candidate quads
   -> Update var-occurrences as necessary







### Concept

c1: ?s in {
    ?s a Airport .
}

c2: ?s in {
    ?s a Airport .
    ?s locatedIn ?c .
}

c3: ?s in {
    ?s a Airport .
    ?s locatedIn ?c .
    ?c memberOf ?o .
    Filter(?o = EuropeanUnion)
}



Repository R:
r1: {
    ?s locatedIn ?c .
    ?c memberOf ?o .
    Filter(?o = EuropeanUnion)
} -> [ s1, ..., sn ]


### Decompose into triple pattern sources: constraints + immediate filters

s1: [null, rdf:type, Airport]
s2: [null, locatedIn, null]
s3: [null, memberOf, EuropeanUnion]

    x: [null, memberOf, null] with Filter(regex(?o, 'foobar'))

### A BGP is a set of aliased references to these sources
Structure = (A, S, m, E, c) with
  A = the set of alias labels
  S = the set of source labels
  m: A->S = mapping of source labels to aliases
  E = Pow(A) sets of aliases labels participating in a join
  c: E->expr Mapping of edge to concrete join condition in terms of aliases

c3: {
  aliases: {
    s1: a1
    s2: a2
    s3: a3
  },
  conditions: {
    e1: {a1, a2} on (a1.s = a2.s)
    e2: {a2, a3} on (a2.o = a3.s)
        x: {a1, a2, a3} on f(a1.s, a2.p, a3.o) = 1
  }
}

Issue: Join conditions are transitive, so a1 would also join with a2
We should create JoinSets for each variable:
This is the set of quads which are joined by a variable.
Map<Var, Set<Alias>> ; e.g.
{
  ?s: { a1, a2, a3 }
}

VarOccurrenceSets is a different view on joinSets: It is the set of constraint? quads
in which the variable occurrs; hence its the set of quads that participate in a join.




### Lookup of candidates
- Given a constraint-tuple, we check the alias-graph whether a corresponding node already exists and allocate a new one as needed.
- From there we can navigate to all triple pattern aliases and in turn to all queries which contain this triple pattern
- TODO I think: We can do a breath first search on the edges (join-conditions)






### Create hyper-edges for representing filters and join-conditions
    Conditions work on aliases

    _This enables getting all available joins for a quad pattern_
e1: {a1}





### Aggregate edges to BGPs:
c2: {e1}
c3: {e1, e2}




### Sub-pattern relations
a1: ?s a ?o . Filter(?o In V)
a2: ?s a ?o . Filter(?o In V') with V' subset V then a1 subset of a2




### Future Work: ###
?s in {
    ?s a Aiport .
    ?s locatedIn ?c .
    ?c memberOf ?o .
    Filter(?o = EuropeanUnion || ?o = America)
}

### Other uses of the system

* Context-sensitive constraints for query federation

Declare Dataset DBpedia
  ?s prefix 'http://dbpedia.org/resource'
when
{
  ?s a Person .
  ?s origin Wikipedia.
}


### Resolving Ambiguity

Given the following candidate and query pattern, each candidate variable may map
to 2 query variables.
{
  ?a ?b ?c .
  ?d ?e ?f .
}

{
  ?u ?v ?w
  ?x ?y ?z
}

?a -> { ?u, ?x }

So if we picked ?a -> ?u then this implies ?b -> ?v and ?c -> ?w
But: How can we access this information?


Given the VarOcc, we could check



