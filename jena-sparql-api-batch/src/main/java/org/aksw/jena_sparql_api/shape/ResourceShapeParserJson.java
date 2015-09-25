package org.aksw.jena_sparql_api.shape;

import java.util.Map.Entry;

import org.aksw.jena_sparql_api.batch.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.beans.json.JsonVisitor;
import org.aksw.jena_sparql_api.beans.json.JsonWalker;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.utils.Vars;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.util.ExprUtils;


/**
 * shape: true
 *
 * shape: 'rdfs:label'
 *
 *
 * shape: ['rdf:type', 'rdfs:label']
 *
 * shape: {
 *   'rdf:type': true # Fetch rdf:type triples together with all outgoing triples of rdf:type
 * }
 *
 * shape: {
 *   'rdf:type': false # Fetch rdf:type triples, but no further reachable triples
 * }
 *
 * shape: {
 *   '-rdf:type': ... # Prefix with '-' to navigate in inverse direction (should replace '&gt' which we used so far)
 * }
 *
 * shape: {
 *   '~?p = rdf:type && langMatches(lang(?o), "en")' // Prefix with ~ to use a sparql expression
 * }
 *
 * Special attributes start with '$':
 * $filter: Set a concept for filtering the set of reached resources
 *
 * note:
 * ['rdf:type'] is equivalent to { 'rdf:type': false }
 *
 * shape: {
 *   'rdf:type': {
 *     $filter: '?s | ?s a owl:Class' // Only fetch types that are owl:Classes (i.e. exclude e.g. SKOS concepts),
 *     $predicates: ['rdfs:label']
 *   }
 * }
 *
 * Macro symbols:
 * shape: '{@literal @}spatial'
 *
 * At {@literal @}spatial will extended with its definition.
 *
 *
 * @author raven
 *
 */
public class ResourceShapeParserJson
	implements JsonVisitor<Void>
{
	protected ResourceShapeBuilder builder;

	public ResourceShapeBuilder getBuilder() {
		return builder;
	}

	public ResourceShapeParserJson() {
		this(new PrefixMappingImpl());
	}

    public ResourceShapeParserJson(PrefixMapping prefixMapping) {
    	this(new ResourceShapeBuilder(prefixMapping));
    }

    public ResourceShapeParserJson(ResourceShapeBuilder builder) {
    	this.builder = builder;
    	//this.builder = new ResourceShapeBuilder(prefixMapping);
    }


    /**
     * String must be of format
     * [-] [~] str
     *
     * -: If present, assume inverse direction
     * ~: If present, str is assumed to be a SPARQL expression. Otherwise, a property URI is assumed
     *
     *
     * @param str
     * @return
     */
    public static StepRelation parseStep(String str, PrefixMapping prefixMapping) {
        str = str.trim();

        // Check the first character
        char c = str.charAt(0);
        boolean isInverse = c == '-';

        if(isInverse) {
            str = str.substring(1);
        }

        c = str.charAt(0);
        boolean isExpr = c == '~';

        if(isExpr) {
            str = str.substring(1);
        }

        Expr expr;
        if(isExpr) {
            expr = ExprUtils.parse(str, prefixMapping);
        } else {
            String p = prefixMapping.expandPrefix(str);
            Node np = NodeFactory.createURI(p);
            expr = new E_Equals(new ExprVar(Vars.p), NodeValue.makeNode(np));
        }

        Relation relation = new Relation(new ElementFilter(expr), Vars.p, Vars.o);

        StepRelation result = new StepRelation(relation, isInverse);
        return result;
    }

    public static ResourceShape parse(JsonElement json) {
    	ResourceShapeParserJson parser = new ResourceShapeParserJson();
    	JsonWalker.visit(json, parser);

        ResourceShape result = parser.getBuilder().getResourceShape();
        return result;
    }

    public static ResourceShape parse(JsonElement json, ResourceShapeBuilder builder) {
    	ResourceShapeParserJson parser = new ResourceShapeParserJson(builder);

    	JsonWalker.visit(json, parser);
        ResourceShape result = builder.getResourceShape();
        return result;
    }


	@Override
	public Void visit(JsonNull json) {
		return null;
	}


	@Override
	public Void visit(JsonObject json) {
		PrefixMapping pm = builder.getPrefixMapping();

        for(Entry<String, JsonElement> entry : json.entrySet()) {
            String str = entry.getKey();
            JsonElement e = entry.getValue();
            StepRelation step = parseStep(str, pm);
            ResourceShapeBuilder subBilder = builder.nav(step);
            JsonWalker.visit(e, this);
            parse(e, subBilder);
        }

        return null;
	}


	@Override
	public Void visit(JsonArray json) {
        for(JsonElement item : json) {
            JsonWalker.walk(item, this);
        }
        return null;
	}


	@Override
	public Void visit(JsonPrimitive json) {
		PrefixMapping pm = builder.getPrefixMapping();

		if(json.isBoolean()) {
            Boolean tf = json.getAsBoolean();
            if(tf == true) {
                builder.nav(NodeValue.TRUE, true);
            }
        }
        else if (json.isString()) { // fetch a single property
            String str = json.getAsString();
            StepRelation step = parseStep(str, pm);

            builder.nav(step);
        }

		return null;
	}
}
