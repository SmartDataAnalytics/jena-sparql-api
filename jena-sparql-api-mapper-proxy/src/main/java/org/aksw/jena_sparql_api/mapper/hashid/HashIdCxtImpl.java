package org.aksw.jena_sparql_api.mapper.hashid;

import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import org.apache.jena.ext.com.google.common.hash.HashCode;
import org.apache.jena.ext.com.google.common.hash.HashFunction;
import org.apache.jena.rdf.model.RDFNode;

public class HashIdCxtImpl
    implements HashIdCxt
{
//  protected Set<RDFNode> seen;
//  protected Map<RDFNode, HashCode> results;
    protected RDFNode rdfNode;
    protected boolean useInnerIris;
    protected BiPredicate<? super RDFNode, ? super Integer> filterKeep;
    protected int depth;
    protected Set<RDFNode> seen;
    protected HashFunction hashFn;
    protected Map<RDFNode, HashCode> priorHash;

    @Override
    public boolean declareVisit(RDFNode node) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public boolean putHash(RDFNode node, HashCode hashCode) {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public HashCode getHash(RDFNode node) {
        // TODO Auto-generated method stub
        return null;
    }

}
