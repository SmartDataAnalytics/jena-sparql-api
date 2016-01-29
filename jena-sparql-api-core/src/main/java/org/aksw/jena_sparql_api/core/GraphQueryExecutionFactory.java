package org.aksw.jena_sparql_api.core;

import java.util.Iterator;

import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

public class GraphQueryExecutionFactory
    extends GraphBase
{
    private QueryExecutionFactory qef;

    // Whether to delegate a call to close to the underlying qef
    // True by default
    boolean delegateClose;

    public GraphQueryExecutionFactory(QueryExecutionFactory qef) {
        this(qef, true);
    }

    public GraphQueryExecutionFactory(QueryExecutionFactory qef, boolean delegateClose) {
        this.qef = qef;
        this.delegateClose = delegateClose;
    }


//    @Override
//    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
//
//    }

    @Override
    public void close() {
        if(delegateClose) {
            this.qef.close();
        }
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple m) {
        Query query = new Query();
        query.setQueryConstructType();

        /*
        Node s = m.getMatchSubject();
        Node p = m.getMatchPredicate();
        Node o = m.getMatchObject();
        */
        Node s = m.getSubject();
        Node p = m.getPredicate();
        Node o = m.getObject();

        s = s == null || s.equals(Node.ANY) ? Vars.s : s;
        p = p == null || p.equals(Node.ANY) ? Vars.p : p;
        o = o == null || o.equals(Node.ANY) ? Vars.o : o;

        Triple triple = new Triple(s, p, o);

        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);

        Template template = new Template(bgp);
        Element element = new ElementTriplesBlock(bgp);

        query.setConstructTemplate(template);
        query.setQueryPattern(element);

        QueryExecution qe = qef.createQueryExecution(query);
        Iterator<Triple> it = qe.execConstructTriples();

        WrappedIterator<Triple> result = WrappedIterator.<Triple>createNoRemove(it);

        return result;
    }

}
