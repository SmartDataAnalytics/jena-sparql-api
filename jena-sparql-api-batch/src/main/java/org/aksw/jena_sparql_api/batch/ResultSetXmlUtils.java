package org.aksw.jena_sparql_api.batch;

import java.util.Collection;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class ResultSetXmlUtils {
    public static String toXmlStringBinding(Binding binding, Collection<String> varNames) {
        String result = "<result>";
        for(String varName : varNames) {
            Node node = binding.get(Var.alloc(varName));
            result += toXmlStringBindingItem(varName, node);
        }
        result += "</result>";

        return result;
    }

    
    public static String toXmlStringBindingItem(String varName, Node node) {
        String nodeStr = toXmlStringNode(node);
        
        String result = nodeStr == null ? "" : "<binding name=\"" + varName + "\">" + nodeStr + "</binding>";

        return result;
    }
    
    public static String toXmlStringNode(Node node) {
        String result;
        if(node == null) {
            result = null;
        }
        else if(node.isBlank()) {
            result = toXmlStringBlank(node);
        }
        else if (node.isURI()) {
            result = toXmlStringUri(node);
        }
        else if(node.isLiteral()) {
            result = toXmlStringLiteral(node);
        }
        else {
            throw new RuntimeException("Unknow node type: " + node);
        }

        return result;
    }

//    public static String toStringXmlBinding(String varName, Node node)
//    {
//        if(node == null) {
//            return null;
//        }
//        
//        String nodeStr = toXmlStringNode(node);
//        
//        String result;
//        if(nodeStr == null) {
//            result = "";
//        }
//        else {
//            result = "<binding name=\"" + varName + "\" />" + nodeStr + "</binding>";
//        }
// 
//        return result;
//    }
    
    public static String toXmlStringLiteral(Node node)
    {
        String datatype = node.getLiteralDatatypeURI();
        String lang = node.getLiteralLanguage();
        
        String result = "<literal";

        if(!StringUtils.isEmpty(lang))
        {
            result += " xml:lang=\"" + lang + "\"";
        }
            
        if(!StringUtils.isEmpty(datatype))
        {
            result += " datatype=\"" + datatype + "\"";
        }

        result += ">" + StringEscapeUtils.escapeXml(node.getLiteralLexicalForm()) + "</literal>";
        
        return result;
    }
    
    
    public static String toXmlStringBlank(Node node) {
        String label = node.getBlankNodeId().getLabelString();

        String result = "<bnode>" + label + "</bnode>";
        return result;
    }
    
    public static String toXmlStringUri(Node node)
    {
        String result = "<uri>" + StringEscapeUtils.escapeXml(node.getURI()) + "</uri>"; 
        return result;
    }

}
