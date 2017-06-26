package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.MapUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.PatternVars;
//import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
//import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
//import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;

//import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
//import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
//import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;


public class ElementUtils {

//    public static Generator<Var> freshVars(Element element){
//        Collection<Var> blacklistVars = PatternVars.vars(element);
//
//        Generator<Var> gen = VarGeneratorImpl2.create("v");
//        Generator<Var> result = new VarGeneratorBlacklist(gen, blacklistVars);
//
//        return result;
//    }


    public static ElementTriplesBlock createElement(Triple triple) {
        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);
        ElementTriplesBlock result = new ElementTriplesBlock(bgp);
        return result;
    }

    public static ElementPathBlock createElement(TriplePath triplePath) {
        ElementPathBlock result = new ElementPathBlock();
        result.addTriplePath(triplePath);
        return result;
    }


    public static List<Triple> extractTriples(Element e) {
        List<Triple> result = new ArrayList<Triple>();
        extractTriples(e, result);
        return result;
    }


    //public static Element join()

    public static Triple extractTriple(Element e) {
        //Node result = null;
        Triple result = null;

        if(e instanceof ElementFilter) {
            ElementFilter x = (ElementFilter)e;
            Expr expr = x.getExpr();
            Set<Set<Expr>> cnf = CnfUtils.toSetCnf(expr);
            Map<Var, Node> map = CnfUtils.getConstants(cnf);

            //Node g = MapUtils.getOrElse(map, Vars.g, Node.ANY);
            Node s = MapUtils.getOrElse(map, Vars.s, Node.ANY);
            Node p = MapUtils.getOrElse(map, Vars.p, Node.ANY);
            Node o = MapUtils.getOrElse(map, Vars.o, Node.ANY);
            result = new Triple(s, p, o);
        } else {
            List<Triple> triples = extractTriples(e);
            if(triples.size() == 1) {
                result = triples.get(0);
                //Triple t = triples.get(0);
                //result = t.getPredicate();
            }
        }
        return result;
    }

    public static void extractTriples(Element e, List<Triple> result) {

        if(e instanceof ElementGroup) {
            ElementGroup g = (ElementGroup)e;
            for(Element item : g.getElements()) {
                extractTriples(item, result);
            }
        } else if(e instanceof ElementTriplesBlock) {
            ElementTriplesBlock b = (ElementTriplesBlock)e;
            List<Triple> triples = b.getPattern().getList();
            result.addAll(triples);
        }
    }

    public static Map<Node, Var> createMapFixVarNames(Element element) {
        Collection<Var> vars = PatternVars.vars(element);
        Map<Node, Var> result = createMapFixVarNames(vars);

        return result;
    }


    public static Map<Node, Var> createMapFixVarNames(Collection<Var> vars) {
        //Set<Var> vars = NodeUtils.getVarsMentioned(nodes);
        //Set<Node> bnodes = NodeUtils.getBnodesMentioned(vars);
        Generator<Var> gen = VarGeneratorBlacklist.create("v", vars);

        Map<Node, Var> result = new HashMap<Node, Var>();
//        for(Node node : bnodes) {
//            result.put(node, gen.next());
//        }
        for(Var var : vars) {
            if(var.getName().startsWith("?") || var.getName().startsWith("/")) {
                result.put(var, gen.next());
            }
            //System.out.println(var);
        }

        return result;
    }


    public static Element fixVarNames(Element element) {
        Map<Node, Var> nodeMap = createMapFixVarNames(element);

        NodeTransform nodeTransform = new NodeTransformRenameMap(nodeMap);
        Element result = ElementUtils.applyNodeTransform(element, nodeTransform);

        return result;
    }

    public static Element toElement(Collection<Element> elements) {
        Element result;
        if(elements.size() == 1) {
            result = elements.iterator().next();
        } else {
            ElementGroup e = new ElementGroup();
            for(Element element : elements) {
                e.addElement(element);
            }
            result = e;
        }

        return result;
    }

    public static Element union(Collection<Element> elements) {
        Element result;
        if(elements.size() == 1) {
            result = elements.iterator().next();
        } else {
            ElementUnion e = new ElementUnion();
            for(Element element : elements) {
                e.addElement(element);
            }
            result = e;
        }

        return result;
    }

    public static Element groupIfNeeded(Collection<Element> members) {
        Element result = members.size() == 1
                ? members.iterator().next()
                : createElementGroup(members)
                ;

        return result;

    }

    public static Element groupIfNeeded(Element ... members) {
        Element result = groupIfNeeded(Arrays.asList(members));
        return result;
    }

    public static ElementGroup createElementGroup(Iterable<Element> members) {
        ElementGroup result = new ElementGroup();
        for(Element member : members) {
            result.addElement(member);
        }
        return result;
    }

    public static ElementGroup createElementGroup(Element ... members) {
        ElementGroup result = new ElementGroup();
        for(Element member : members) {
            result.addElement(member);
        }
        return result;
    }

    public static Element createRenamedElement(Element element, Map<? extends Node, ? extends Node> nodeMap) {
        NodeTransform nodeTransform = new NodeTransformRenameMap(nodeMap);
        Element result = applyNodeTransform(element, nodeTransform);
        return result;
    }

    @Deprecated // Use TransformElementLib.transform instead
    public static Element applyNodeTransform(Element element, NodeTransform nodeTransform) {
        org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform elementTransform = new ElementTransformSubst2(nodeTransform);//new ElementTransformSubst2(nodeTransform);
        ExprTransform exprTransform = new org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement(nodeTransform, elementTransform);
        //Element result = ElementTransformer.transform(element, elementTransform, exprTransform);
        Element result = org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer.transform(element, elementTransform, exprTransform);
        return result;
    }

    public static void copyElements(ElementGroup target, Element source) {
        if(source instanceof ElementGroup) {
            ElementGroup es = (ElementGroup)source;

            for(Element e : es.getElements()) {
                target.addElement(e);
            }
        } else {
            target.addElement(source);
        }
    }

    public static void mergeElements(ElementGroup target, ElementTriplesBlock etb, Element source) {
        if(source instanceof ElementTriplesBlock) {
            ElementTriplesBlock e = (ElementTriplesBlock)source;
            for(Triple t : e.getPattern()) {
                etb.addTriple(t);
            }
        } else if(source instanceof ElementGroup) {
            ElementGroup es = (ElementGroup)source;

            for(Element e : es.getElements()) {
                mergeElements(target, etb, e);
                //target.addElement(e);
            }
        } else {
            target.addElement(source);
        }
    }

    /**
     * Creates a new ElementGroup that contains the elements of the given arguments.
     * Argument ElementGroups are flattened. ElementTriplesBlocks however are not combined.
     *
     * @param first
     * @param second
     * @return
     */
    public static Element mergeElements(Element first, Element second) {
        ElementGroup tmp = new ElementGroup();
        ElementTriplesBlock etb = new ElementTriplesBlock();
        tmp.addElement(etb);


        mergeElements(tmp, etb, first);
        mergeElements(tmp, etb, second);

        // Remove empty element triple blocks
        //ElementGroup result = new ElementGroup();
        List<Element> els = new ArrayList<>();
        for(Element e : tmp.getElements()) {
            if((e instanceof ElementTriplesBlock) && ((ElementTriplesBlock)e).isEmpty()) {
                // Skip
            } else {
                els.add(e);
            }
        }

        Element result = groupIfNeeded(els);

        return result;
    }

    public static Element unionElements(Element first, Element second) {
        ElementUnion result = new ElementUnion();

        addUnionElements(result, first);
        addUnionElements(result, second);

        return result;
    }

    public static void addUnionElements(ElementUnion out, Element e) {
        if(e instanceof ElementUnion) {
            ElementUnion u = (ElementUnion)e;
            for(Element m : u.getElements()) {
                out.addElement(m);
            }
        }
        else if(e instanceof ElementGroup && ((ElementGroup)e).isEmpty()) {
            // nothing todo
        } else {
            out.addElement(e);
        }
    }

    public static List<Element> toElementList(Element element) {
        List<Element> result;

        if(element instanceof ElementGroup) {
            result = ((ElementGroup)element).getElements();
        } else {
            result = Arrays.asList(element);
        }

        // This method always returns a copy of the elements
        result = new ArrayList<Element>(result);

        return result;
    }

    /**
     * TODO This method should flatten elements recursively
     *
     * @param e
     * @return
     */
    public static Element flatten(Element e) {
        Element result;
        if(e instanceof ElementGroup) {
            ElementGroup tmp = (ElementGroup)e;
            List<Element> els = tmp.getElements();

            result = els.size() == 1 ? els.get(0) : tmp;
        } else {
            result = e;
        }

        return result;
    }

}