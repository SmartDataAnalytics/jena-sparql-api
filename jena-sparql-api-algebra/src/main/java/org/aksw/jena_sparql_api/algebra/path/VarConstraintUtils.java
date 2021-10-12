package org.aksw.jena_sparql_api.algebra.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.views.PrefixSet;
import org.aksw.jena_sparql_api.views.RdfTermType;
import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.aksw.jena_sparql_api.views.VarDefinition;
import org.aksw.jena_sparql_api.views.ViewQuad;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;

public class VarConstraintUtils {

    /**
     *
     * @param view
     */
    private void index(T view) {

        //RestrictionManagerImpl restrictions = new RestrictionManagerImpl();
        //view.setRestrictions(restrictions);

        T normalized = normalizeView(view);


        RestrictionManagerImpl varRestrictions = normalized.getVarRestrictions();

        this.views.add(normalized);


        //System.out.println("Normalized view:\n" + normalized + "\n");
        //System.out.println();

        //derivePrefixConstraints(view);

        // Index the pattern constraints
        /*
        Map<Var, PrefixSet> prefixConstraints = view.getConstraints().getVarPrefixConstraints();
        for(Entry<Var, PrefixSet> entry : prefixConstraints.entrySet()) {
            restrictions.stateUriPrefixes(entry.getKey(), entry.getValue());
        }

        Map<Var, Type> typeConstraints = deriveTypeConstraints(view);
        for(Entry<Var, Type> entry : typeConstraints.entrySet()) {
            restrictions.stateType(entry.getKey(), entry.getValue());
        }
        */

        //RestrictedExpr expr;
        //expr.getRestrictions().getType()


        for(Quad quad : normalized.getTemplate()) {

            List<Collection<?>> collections = new ArrayList<Collection<?>>();

            for(int i = 0; i < 4; ++i) {
                Node node = QuadUtils.getNode(quad, i);

                // This check is only performed prior to the object position
                if(i == 3) {
                    RdfTermType type = getType(node, varRestrictions);
                    switch(type) {
                    case URI:
                        collections.add(Collections.singleton(1));
                        break;

                    case LITERAL:
                        collections.add(Collections.singleton(2));
                        break;

                    default:
                        // Either URI or literal
                        collections.add(Arrays.asList(1, 2));
                        break;
                    }
                }

                if(node.isVariable()) {

                    Var var = (Var)node;

                    PrefixSet p = null;
                    VarDefinition varDefinition = normalized.getVarDefinition();

                    if(varDefinition != null) {
                        Collection<RestrictedExpr> restExprs = varDefinition.getDefinitions(var);

                        for(RestrictedExpr restExpr : restExprs) {
                            PrefixSet tmp = restExpr.getRestrictions().getUriPrefixes();
                            if(p == null) {
                                p = tmp;
                            } else {
                                p.addAll(tmp);
                            }
                        }
                    }

                    if(p != null) {
                        collections.add(p.getSet());
                    } else {
                        collections.add(Collections.singleton(""));
                    }

                } else if (node.isURI()) {
                    collections.add(Collections.singleton(node.getURI()));
                /* } else if(node.isLiteral()) {
                    collections.add(Collections.singleton(node.getLiteralLexicalForm()));
                    */
                } else {
                    // FIXME We ignore deriving constraints for literals here
                    //throw new RuntimeException("Should not happen");
                }
            }

            ViewQuad<T> viewQuad = new ViewQuad<T>(normalized, quad);
            CartesianProduct<Object> cartesian = new CartesianProduct<Object>(collections);
            for(List<Object> item : cartesian) {
                List<Object> row = new ArrayList<Object>(item);
                row.add(viewQuad);
                table.add(row);
            }
        }
    }


}
