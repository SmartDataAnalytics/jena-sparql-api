package org.aksw.commons.graph.index.jena.transform;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.jgrapht.Graph;


/**
 * Create a directed labeled graph representation of a SPARQL algebra expression.
 * Nodes correspond to the SPARQL operators and are represented as indentifiers
 * with with optional information can be associated (such as filter expressions of a filter op).
 *
 * Edges represent distinct / extend / filter information
 *
 *
 *
 *
 *
 * @author raven
 *
 */
public class OpToGraph
    extends OpVisitorBase
{
    protected Graph<Op, NaEdge> tree;

    @Override
    public void visit(OpDisjunction opDisjunction) {
        // TODO Auto-generated method stub
        super.visit(opDisjunction);
    }

    @Override
    public void visit(OpSequence opSequence) {
        // TODO Auto-generated method stub
        super.visit(opSequence);
    }

    @Override
    public void visit(OpDistinct opDistinct) {
        // TODO Auto-generated method stub
        super.visit(opDistinct);
    }

    @Override
    public void visit(OpProject opProject) {
        // TODO Auto-generated method stub
        super.visit(opProject);
    }

    @Override
    public void visit(OpFilter op) {
        Op subOp = op.getSubOp();


        // TODO Auto-generated method stub
        //super.visit(opFilter);
    }

    @Override
    public void visit(OpExtend op) {

        // TODO Auto-generated method stub
        super.visit(op);
    }
}
