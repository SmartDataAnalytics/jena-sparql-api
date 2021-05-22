package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

/**
 * {
 *    Bind("['foo', 'bar']"^^xsd:json As ?json)
 *    ?json json:array ?items.
 * }
 *
 * @author raven
 *
 */
public class PropertyFunctionFactoryXmlUnnest
    implements PropertyFunctionFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(PropertyFunctionFactoryXmlUnnest.class);
    protected XPath xPath;
    
    
    public PropertyFunctionFactoryXmlUnnest() {

    	this(XPathFactory.newInstance().newXPath());
    }

    public PropertyFunctionFactoryXmlUnnest(XPath xPath) {
        super();

        this.xPath = xPath;
    }

    @Override
    public PropertyFunction create(final String uri)
    {
    	
        return new PFuncSimpleAndList() {
        	
			@Override
			public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg object,
					ExecutionContext execCxt) {

	        	// Obtain the datatype object in order to create rdf Nodes from dom Nodes
	        	// TODO We may want to check for this datatype in the execution context
	        	// before resorting to the type mapper
				RDFDatatype xmlDatatype = TypeMapper.getInstance().getTypeByClass(org.w3c.dom.Node.class);
				Objects.requireNonNull(xmlDatatype);

                // Get the subject's value
                Node node = subject.isVariable()
                        ? binding.get((Var)subject)
                        : subject;

                if(object.getArgListSize() != 2) {
                	throw new RuntimeException("property function for xpath evaluation requires two arguments");
                }

                Node xpathNode = object.getArg(0);
                Node outputNode = object.getArg(1);
                
                if(!outputNode.isVariable()) {
                    throw new RuntimeException("Object of xml array splitting must be a variable");
                }
                Var outputVar = (Var)outputNode;

                QueryIterator result = null;

                boolean isXml = node.isLiteral() && node.getLiteralDatatype() instanceof RDFDatatypeXml;
                if(isXml) {
                    org.w3c.dom.Node xml = (org.w3c.dom.Node)node.getLiteralValue();

                    Object queryObj = xpathNode.isLiteral() ? xpathNode.getLiteralValue() : null;
                    String queryStr = queryObj instanceof String ? (String)queryObj : null;
                    
        	        if(queryStr != null && xml != null) {
                        List<Binding> bindings = new ArrayList<Binding>();                        
                        
        	            try {                            
        	            	XPathExpression expr = xPath.compile(queryStr);
        	            	Object tmp = expr.evaluate(xml, XPathConstants.NODESET);
        	            	
        	            	if(tmp instanceof NodeList) {
        	            		NodeList nodes = (NodeList)tmp;
        	            		for(int i = 0; i < nodes.getLength(); ++i) {
        	            			org.w3c.dom.Node item = nodes.item(i);
        	            			
        	            			// It seems running xpath queries on nodes returned as
        	            			// xpaths results is not supported - hence we need to serialize xml Nodes
        	            			item = (org.w3c.dom.Node)xmlDatatype.parse(xmlDatatype.unparse(item));
        	            			
        	            			//System.out.println("" + node);

        	            			Node xmlNode = NodeFactory.createLiteralByValue(item, xmlDatatype);
                                    Binding b = BindingFactory.binding(binding, outputVar, xmlNode);
                                    bindings.add(b);
        	            		}
        	            	}
                            result = QueryIterPlainWrapper.create(bindings.iterator());
        	            } catch(Exception e) {
        	                logger.warn(e.getLocalizedMessage());
        	            }
                    }
                }

                if(result == null) {
                    result = QueryIterNullIterator.create(execCxt);
                }

                return result;
            }
		};
    }
}