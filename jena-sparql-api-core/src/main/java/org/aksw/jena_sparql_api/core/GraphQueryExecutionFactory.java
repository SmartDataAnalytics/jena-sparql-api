package org.aksw.jena_sparql_api.core;

import java.util.Iterator;

import org.aksw.jena_sparql_api.utils.Vars;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

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


    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
        Query query = new Query();
        query.setQueryConstructType();

        Node s = m.getMatchSubject();
        Node p = m.getMatchPredicate();
        Node o = m.getMatchObject();

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

    @Override
    public void close() {
        if(delegateClose) {
            this.qef.close();
        }
    }

}
