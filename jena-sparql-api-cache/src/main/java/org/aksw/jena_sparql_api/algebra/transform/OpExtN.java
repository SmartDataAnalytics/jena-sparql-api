package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public abstract class OpExtN
	extends OpExt
{
    protected List<Op> elements = new ArrayList<>() ;

    protected OpExtN(String name)         { super(name); elements = new ArrayList<>() ; }
    protected OpExtN(String name, List<Op> x)   { super(name); elements = x ; }

    /** Accumulate an op in the OpN.
     *  This exists to help building OpN in teh first place.
     *  Once built, an OpN, like any Op should be treated as immutable
     *  with no calls change the sub ops contents.
     *  No calls to .add.
     */
    public void add(Op op) { elements.add(op) ; }
    public Op get(int idx) { return elements.get(idx) ; }

    public abstract Op apply(Transform transform, List<Op> elts) ;
    public abstract OpExtN copy(List<Op> elts) ;

    // Tests the sub-elements for equalTo.
    protected boolean equalsSubOps(OpExtN op, NodeIsomorphismMap labelMap)
    {
        if (elements.size() != op.elements.size() )
            return false ;

        Iterator<Op> iter1 = elements.listIterator() ;
        Iterator<Op> iter2 = op.elements.listIterator() ;

        for ( ; iter1.hasNext() ; )
        {
            Op op1 = iter1.next();
            Op op2 = iter2.next();
            if ( ! op1.equalTo(op2, labelMap) )
                return false ;
        }
        return true ;
    }

    public int size()                   { return elements.size() ; }


    @Override
    public int hashCode()               { return elements.hashCode() ; }

    public List<Op> getElements()           { return elements ; }

    public Iterator<Op> iterator()          { return elements.iterator() ; }

	@Override
	public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
		for(Op elt : elements) {
			elt.output(out, sCxt);
		}
	}

}