package org.aksw.difs.engine;

import java.util.Iterator;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterRepeatApply;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/** Match a single quad pattern */
public class QueryIterQuadPattern extends QueryIterRepeatApply
{
    private final Quad pattern ;

    public QueryIterQuadPattern( QueryIterator input,
                                   Quad pattern ,
                                   ExecutionContext cxt)
    {
        super(input, cxt) ;
        this.pattern = pattern ;
    }

    @Override
    protected QueryIterator nextStage(Binding binding)
    {
        return new QuadMapper(binding, pattern, getExecContext()) ;
    }

    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt) {
        out.print("QueryIterTriplePattern: " + pattern);
    }

    static int countMapper = 0 ;
    static class QuadMapper extends QueryIter
    {
    	private Node g;
        private Node s ;
        private Node p ;
        private Node o ;
        private Binding binding ;
        private ClosableIterator<Quad> graphIter ;
        private Binding slot = null ;
        private boolean finished = false ;
        private volatile boolean cancelled = false ;

        QuadMapper(Binding binding, Quad pattern, ExecutionContext cxt)
        {
            super(cxt) ;
            this.g = substitute(pattern.getGraph(), binding) ;
            this.s = substitute(pattern.getSubject(), binding) ;
            this.p = substitute(pattern.getPredicate(), binding) ;
            this.o = substitute(pattern.getObject(), binding) ;
            this.binding = binding ;
            Node g2 = tripleNode(g) ;
            Node s2 = tripleNode(s) ;
            Node p2 = tripleNode(p) ;
            Node o2 = tripleNode(o) ;
            DatasetGraph dg = cxt.getDataset();
            this.graphIter = makeClosable( dg.find(g2, s2, p2, o2) ) ;
        }
        
        private static <T> ClosableIterator<T> makeClosable(Iterator<T> it) {
        	ClosableIterator<T> result;
        	if (it instanceof ClosableIterator) {
        		result = (ClosableIterator<T>) it;
        	} else {
        		// Output a warning because usually we expect closable iterators?
        		result = WrappedIterator.create(it);
        	} 
        	return result;
        }

        private static Node tripleNode(Node node)
        {
            if ( node.isVariable() )
                return Node.ANY ;
            return node ;
        }

        private static Node substitute(Node node, Binding binding)
        {
            if ( Var.isVar(node) )
            {
                Node x = binding.get(Var.alloc(node)) ;
                if ( x != null )
                    return x ;
            }
            return node ;
        }

        private Binding mapper(Quad r)
        {
            BindingMap results = BindingFactory.create(binding) ;

            if ( ! insert(g, r.getGraph(), results) )
                return null ;
            if ( ! insert(s, r.getSubject(), results) )
                return null ;
            if ( ! insert(p, r.getPredicate(), results) )
                return null ;
            if ( ! insert(o, r.getObject(), results) )
                return null ;
            return results ;
        }

        private static boolean insert(Node inputNode, Node outputNode, BindingMap results)
        {
            if ( ! Var.isVar(inputNode) )
                return true ;

            Var v = Var.alloc(inputNode) ;
            Node x = results.get(v) ;
            if ( x != null )
                return outputNode.equals(x) ;

            results.add(v, outputNode) ;
            return true ;
        }

        @Override
        protected boolean hasNextBinding()
        {
            if ( finished ) return false ;
            if ( slot != null ) return true ;
            if ( cancelled )
            {
                graphIter.close() ;
                finished = true ;
                return false ;
            }

            while(graphIter.hasNext() && slot == null )
            {
                Quad t = graphIter.next() ;
                slot = mapper(t) ;
            }
            if ( slot == null )
                finished = true ;
            return slot != null ;
        }

        @Override
        protected Binding moveToNextBinding()
        {
            if ( ! hasNextBinding() )
                throw new ARQInternalErrorException() ;
            Binding r = slot ;
            slot = null ;
            return r ;
        }

        @Override
        protected void closeIterator()
        {
            if ( graphIter != null )
                NiceIterator.close(graphIter) ;
            graphIter = null ;
        }

        @Override
        protected void requestCancel()
        {
            // The QueryIteratorBase machinery will do the real work.
            cancelled = true ;
        }
    }
}