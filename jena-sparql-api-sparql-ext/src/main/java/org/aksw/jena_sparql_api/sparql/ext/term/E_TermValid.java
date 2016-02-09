package org.aksw.jena_sparql_api.sparql.ext.term;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

/**
 *
 * NOTE: The older version of org.apache.commons.validator.routines.UrlValidator is located in package
 * org.apache.commons.validator.UrlValidator
 *
 * @author raven
 *
 */
public class E_TermValid
    extends FunctionBase1
{
//    protected UrlValidator urlValidator;

    public E_TermValid() {
        super();
//        this(new UrlValidator());
    }

//    public E_TermValid(UrlValidator urlValidator) {
//        super();
//        this.urlValidator = urlValidator;
//    }



    @Override
    public NodeValue exec(NodeValue nv) {
        Node node = nv.asNode();

        NodeValue result;

        if(node.isURI()) {
            String iri = node.getURI();
            //boolean verdict = urlValidator.isValid(iri);
            boolean isValid = !IRIResolver.checkIRI(iri);

            result = isValid ? NodeValue.TRUE : NodeValue.FALSE;
        } else {
            result = NodeValue.TRUE;
        }

        return result;
    }
}
