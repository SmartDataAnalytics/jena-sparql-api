package org.aksw.jena_sparql_api.concepts;

import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;

import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

/**
 * A relation that in addition has a variable for the property
 *
 * @author raven
 *
 */
public class PropertyRelation
    extends Relation
{
    protected Var propertyVar;

    public PropertyRelation(Element element, Var sourceVar, Var propertyVar, Var targetVar) {
        super(element, sourceVar, targetVar);
        this.propertyVar = propertyVar;
    }

    public Var getPropertyVar() {
        return propertyVar;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((propertyVar == null) ? 0 : propertyVar.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PropertyRelation other = (PropertyRelation) obj;
        if (propertyVar == null) {
            if (other.propertyVar != null)
                return false;
        } else if (!propertyVar.equals(other.propertyVar))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PropertyRelation [propertyVar=" + propertyVar + "]";
    }


    public static PropertyRelation create(String elementStr, String sourceVarName, String propertyVarName, String targetVarName) {
        Var sourceVar = Var.alloc(sourceVarName);
        Var propertyVar = Var.alloc(propertyVarName);
        Var targetVar = Var.alloc(targetVarName);
        SparqlElementParser parser = SparqlElementParserImpl.create(Syntax.syntaxARQ, new Prologue());
        Element element = parser.apply(elementStr);

        PropertyRelation result = new PropertyRelation(element, sourceVar, propertyVar, targetVar);
        return result;
    }
}
