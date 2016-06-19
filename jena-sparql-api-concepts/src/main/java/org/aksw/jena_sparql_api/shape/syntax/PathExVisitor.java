package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.path.PathVisitor;

public interface PathExVisitor
    extends PathVisitor
{
    void visit(P_Relation path);
    void visit(P_Service path);
    void visit(P_Var path);
    void visit(P_Element path);
    //void visit(P_ForAll pathMo);
}
