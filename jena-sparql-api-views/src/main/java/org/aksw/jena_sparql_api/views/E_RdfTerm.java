package org.aksw.jena_sparql_api.views;

import java.util.List;
import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.E_BNode;
import org.apache.jena.sparql.expr.E_StrDatatype;
import org.apache.jena.sparql.expr.E_StrLang;
import org.apache.jena.sparql.expr.E_URI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.FunctionLabel;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.Tags;
import org.apache.jena.vocabulary.XSD;


public class E_RdfTerm
    extends ExprFunctionN
{
	public static final String tagRdfTerm = "http://aksw.org/sparqlify/rdfTerm";
	
    public static final NodeValue typeVar = NodeValue.makeInteger(-1);
    public static final NodeValue typeBlank = NodeValue.makeInteger(0);
    public static final NodeValue typeUri = NodeValue.makeInteger(1);
    public static final NodeValue typePlainLiteral = NodeValue.makeInteger(2);
    public static final NodeValue typeTypedLiteral = NodeValue.makeInteger(3);


    public static final E_RdfTerm TRUE = E_RdfTerm.createTypedLiteral(NodeValue.TRUE, XSD.xboolean);
    public static final E_RdfTerm FALSE = E_RdfTerm.createTypedLiteral(NodeValue.FALSE, XSD.xboolean);
    public static final E_RdfTerm TYPE_ERROR = SqlTranslationUtils.expandConstant(SparqlifyConstants.nvTypeError);


    public static E_RdfTerm createVar(ExprVar expr) {
        return new E_RdfTerm(typeVar, expr, NodeValue.nvEmptyString, NodeValue.nvEmptyString);
    }

    public static E_RdfTerm createBlankNode(Expr expr) {
        return new E_RdfTerm(typeBlank, expr, NodeValue.nvEmptyString, NodeValue.nvEmptyString);
    }

    public static E_RdfTerm createUri(Expr expr) {
        return new E_RdfTerm(typeUri, expr, NodeValue.nvEmptyString, NodeValue.nvEmptyString);
    }

    public static E_RdfTerm createPlainLiteral(Expr expr) {
        return new E_RdfTerm(typePlainLiteral, expr, NodeValue.nvEmptyString, NodeValue.nvEmptyString);
    }

    public static E_RdfTerm createPlainLiteral(Expr expr, Expr langTag) {
        return new E_RdfTerm(typePlainLiteral, expr, langTag, NodeValue.nvEmptyString);
    }

    public static E_RdfTerm createTypedLiteral(Expr expr, Expr datatype) {

        //DatatypeSystemDefault.
        return new E_RdfTerm(typeTypedLiteral, expr, NodeValue.nvEmptyString, datatype);
    }

    public static E_RdfTerm createTypedLiteral(Expr expr, Resource datatype) {

        return createTypedLiteral(expr, datatype.asNode());
    }

    public static E_RdfTerm createTypedLiteral(Expr expr, Node datatype) {

        String datatypeUri = datatype.getURI();
        Expr datatypeExpr = NodeValue.makeString(datatypeUri);
        //DatatypeSystemDefault.
        return new E_RdfTerm(typeTypedLiteral, expr, NodeValue.nvEmptyString, datatypeExpr);
    }

    public String getFunctionIRI() {
        return SparqlifyConstants.rdfTermLabel;
    }

    public E_RdfTerm(List<Expr> exprs) {
        this(exprs.get(0), exprs.get(1), exprs.get(2), exprs.get(3));


        /*
        if(exprs.size() != 4) {
            throw new IllegalArgumentException("ExprRdfTerm requires exactly four arguments");
        }*/
    }

    public E_RdfTerm(Expr type, Expr lexicalValue, Expr languageTag, Expr datatype) {
        super(SparqlifyConstants.rdfTermLabel, type, lexicalValue, languageTag, datatype);
    }

    public Expr getType()
    {
        return super.getArgs().get(0);
    }

    public Expr getLexicalValue()
    {
        return super.getArgs().get(1);
    }

    public Expr getLanguageTag()
    {
        return super.getArgs().get(2);
    }

    public Expr getDatatype()
    {
        return super.getArgs().get(3);
    }


    @Override
    public boolean isConstant()
    {
        return false;

        /*
        for(Expr expr : super.getArgs()) {
            if(!expr.isConstant()) {
                return false;
            }
        }

        return true;
        */
    }

    public String getFunctionPrintName(SerializationContext cxt) {
        String functionIri = getFunctionIRI();
        String result = functionIri == null ? super.getFunctionPrintName(cxt) : "<" + functionIri + ">";

        return result;
    }


    @Override
    public NodeValue getConstant() {
        NodeValue result = RdfTerm.eval(
                this.getArgs().get(0).getConstant(),
                this.getArgs().get(1).getConstant(),
                this.getArgs().get(2).getConstant(),
                this.getArgs().get(3).getConstant()
            );

        //System.err.println(result);

        return result;
    }


    @Override
    public NodeValue eval(List<NodeValue> args) {
        return RdfTerm.eval(args.get(0), args.get(1), args.get(2), args.get(3));
        //RdfTerm
        //throw new RuntimeException("Should not happen");
        // TODO Auto-generated method stub
        //return null;
    }

    @Override
    public Expr copy(ExprList args) {
        return new E_RdfTerm(args.get(0), args.get(1), args.get(2), args.get(3));
    }

    
    public static E_RdfTerm expand(Expr expr) {
    	E_RdfTerm result;
    	if(expr.isFunction()) {
    		ExprFunction fn = expr.getFunction();
    		result = expand(fn);
    	} else {
    		result = null;
    	}
    	
    	return result;
    }

    public static E_RdfTerm expand(ExprFunction fn) {    	
    	E_RdfTerm result;

		String symbol = Optional.ofNullable(fn.getFunctionSymbol())
				.map(FunctionLabel::getSymbol)
				.orElse(null);
		
        if(Tags.tagUri.equals(symbol) ||
           Tags.tagIri.equals(symbol)) {
            result = createUri(fn.getArg(1));
        } else if (Tags.tagStrLang.equals(symbol)) {
        	result = createPlainLiteral(fn.getArg(1), fn.getArg(2));
        } else if (Tags.tagStrDatatype.equals(symbol)) { 
        	result = createTypedLiteral(fn.getArg(1), fn.getArg(2));
        } else if (Tags.tagBNode.equals(symbol)) {
        	result = createBlankNode(fn.getArg(1));
        } else if(tagRdfTerm.equals(symbol)) {
            result = new E_RdfTerm(
                    fn.getArg(1), fn.getArg(2), fn.getArg(3), fn.getArg(4));
    	} else {
    		result = null;
    	}

        return result;
    }
    
    
	public static ExprFunction normalize(E_RdfTerm rdfTerm) {
		int termTypeId = rdfTerm.getType().getConstant().getDecimal().intValue();

		ExprFunction result;
		switch(termTypeId) {
		case 0: // blank node
			result = new E_BNode(rdfTerm.getLexicalValue());
			break;
		case 1: // uri
			result = new E_URI(rdfTerm.getLexicalValue());
			break;
		case 2: // plain literal
			result = new E_StrLang(rdfTerm.getLexicalValue(), rdfTerm.getLanguageTag());
			break;
		case 3: // typed literal
			result = new E_StrDatatype(rdfTerm.getLexicalValue(), rdfTerm.getDatatype());
			break;
		default:
			throw new RuntimeException("Unsupported term type: " + rdfTerm);
		}
	
		return result;
	}
}