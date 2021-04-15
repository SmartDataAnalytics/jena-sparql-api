package org.aksw.jena_sparql_api.sparql.ext.fs;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class E_RdfLang
    extends FunctionBase1
{
    @Override
    public NodeValue exec(NodeValue nv) {
    	NodeValue result = determineLang(nv);
    	return result;
    }
    
    public static NodeValue determineLang(NodeValue nv) {
    	NodeValue result = null;//NodeValue.nvNothing;// Expr.NONE.getConstant();
    	if(nv.isIRI()) {
    		String iri = nv.asNode().getURI();
            Lang lang = RDFDataMgr.determineLang(iri, null, null);
            if(lang != null) {
            	result = NodeValue.makeString(lang.getContentType().getContentTypeStr());
            }
    	}
    	
    	if(result == null) {
    		throw new ExprEvalException("no result for determineLang with arg " + nv);
    	}
    	
        return result;    	
    }
}
