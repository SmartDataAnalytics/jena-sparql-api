package org.aksw.jena_sparql_api.sparql_path.utils;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;

// Adoption by Claus Stadler 2013

/**
 * <p>Utility class that walks the Elements of an ARQ query tree and
 * collects information that is later used to build an SQL query.</p>
 * 
 * <p>The class provides a different view on the tree of Element
 * instances. In this view, the nodes are ElementTreeAnalyser
 * instances, and the structure of the tree differs in several
 * ways from the Element tree:</p>
 *
 * <ul>
 * <li>ElementGroups and ElementBlocks are flattened away</li>
 * <li>Only OPTIONALs and UNIONs are presented as child nodes</li>
 * <li>The difference between ElementTriplePattern and
 *   ElementBasePattern is hidden</li>
 * <li>Some obvious simplifications have been done, like dropping
 *   OPTIONALs that do not bind, or flatten single-element UNIONs</li>
 * <li>GRAPH clauses and triples are flattened into triples, quads,
 *   and graphNames, where triples are to be matched against the
 *   default graph, quads are to be matched against the named graphs,
 *   and graphNames are to be matched against the list of graph names,
 *   regardless of triples therein.</li>
 * </ul>
 *   
 * @author Richard Cyganiak (richard@cyganiak.de)
 * @version $Id$
 */
public class ElementTreeAnalyser implements ElementVisitor {
    private Node defaultGraphName;
    private boolean isEmpty = true;
    private boolean canBind = false;
    private boolean mustMatchTriple = false;
    //private List<Triple> triples = new ArrayList();     // Triples
    private List<Quad> quads = new ArrayList<Quad>();       // Quads
    private List<Node> graphNames = new ArrayList<Node>();  // Nodes
    private List<ElementTreeAnalyser> optionals = new ArrayList<ElementTreeAnalyser>();   // ElementAnalysers
    private List<ElementTreeAnalyser> unions = new ArrayList<ElementTreeAnalyser>();      // Lists of ElementAnalysers
    private List<Expr> filterExprs = new ArrayList<Expr>();     // Constraints
    
    public ElementTreeAnalyser(Element element) {
        this(element, Quad.defaultGraphNodeGenerated);
    }
    
    public ElementTreeAnalyser(Element element, Node defaultGraphName) {
        this.defaultGraphName = defaultGraphName;
        element.visit(this);
    }
    
    public boolean isEmpty() {
        return isEmpty;
    }

    public boolean canBind() {
        return canBind;
    }
    
    public boolean mustMatchTriple() {
        return mustMatchTriple;
    }
    
    public List<Quad> getQuads() {
        return quads;
    }
    
    public List<ElementTreeAnalyser> getOptionals() {
        return optionals;
    }
    
    public List<Node> getGraphNames() {
        return graphNames;
    }
    
    public List<ElementTreeAnalyser> getUnions() {
        return unions;
    }
        
//    public List graphNames() {
//        return graphNames;
//    }
    
    public List<ElementTreeAnalyser> optionals() {
        return optionals;
    }
    
    public List<ElementTreeAnalyser> unions() {
        return unions;
    }

    public List<Expr> getFilterExprs() {
        return filterExprs;
    }
    
    public void visit(ElementTriplesBlock el) {
        for(Triple t : el.getPattern().getList()) {
        
            isEmpty = false;
            if (t.getSubject().isVariable()
                    || t.getPredicate().isVariable()
                    || t.getObject().isVariable()) {
                canBind = true;
            }
            if (defaultGraphName == null) {
                Quad quad = new Quad(defaultGraphName, t);
                
                quads.add(quad);
            } else {
                quads.add(new Quad(defaultGraphName, t));
            }
            mustMatchTriple = true;
        }
    }

    public void visit(ElementFilter el) {
        isEmpty = false;
        filterExprs.add(el.getExpr());
    }

    public void visit(ElementUnion el) {
        List<Element> elements = el.getElements();
        
        if (elements.size() == 1) {
            elements.get(0).visit(this);
            return;
        }

        List<ElementTreeAnalyser> union = new ArrayList<ElementTreeAnalyser>();
        boolean allMustMatchTriple = true;
        boolean anyMustMatchTriple = false;
        
        for(Element element : el.getElements()) {
            ElementTreeAnalyser analyser = new ElementTreeAnalyser(element, defaultGraphName);
            if (analyser.isEmpty()) {
                allMustMatchTriple = false;
                continue;
            }
            isEmpty = false;
            if (analyser.canBind()) {
                canBind = true;
            }
            allMustMatchTriple = allMustMatchTriple && analyser.mustMatchTriple();
            anyMustMatchTriple = anyMustMatchTriple || analyser.mustMatchTriple();
            union.add(analyser);
        }
        if (anyMustMatchTriple && allMustMatchTriple) {
            mustMatchTriple = true;
        }
        if (!union.isEmpty()) {
            unions.addAll(union);
        }
    }

//    public void visit(ElementBlock el) {
//        recurse(Collections.singletonList(el.getPatternElement()));
//    }

    public void visit(ElementOptional el) {
        ElementTreeAnalyser optional = new ElementTreeAnalyser(el.getOptionalElement(), defaultGraphName);
        if (optional.isEmpty() || !optional.canBind()) {
            return;
        }
        isEmpty = false;
        canBind = true;
        optionals.add(optional);
    }

    public void visit(ElementGroup el) {
        recurse(el.getElements());
    }

    public void visit(ElementNamedGraph el) {
        isEmpty = false;
        ElementTreeAnalyser analyser = new ElementTreeAnalyser(el.getElement(), el.getGraphNameNode());
        
        if (!analyser.mustMatchTriple()) {
            graphNames.add(el.getGraphNameNode());
        }
        if (el.getGraphNameNode().isVariable() || analyser.canBind()) {
            canBind = true;
        }
        
        
        for(Quad quad : analyser.getQuads()) {
            quads.add(new Quad(el.getGraphNameNode(), quad.asTriple()));
        }
        quads.addAll(analyser.getQuads());
        graphNames.addAll(analyser.getGraphNames());
        optionals.addAll(analyser.getOptionals());
        unions.addAll(analyser.getUnions());
        filterExprs.addAll(analyser.getFilterExprs());
    }

    
    private void recurse(List<Element> elements) {
        for(Element element : elements) {
            element.visit(this);
        }
    }

    @Override
    public void visit(ElementPathBlock el) {
        
        // TODO Paths not handled yet
        
        List<TriplePath> triplePaths = el.getPattern().getList();
        for(TriplePath triplePath : triplePaths) {
            Triple triple = triplePath.asTriple();
            Quad quad = new Quad(defaultGraphName, triple);
            quads.add(quad);
        }
        
        
        //throw new RuntimeException("Not implemented");
    }

    @Override
    public void visit(ElementAssign el) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void visit(ElementBind el) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void visit(ElementData el) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void visit(ElementDataset el) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void visit(ElementExists el) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void visit(ElementNotExists el) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void visit(ElementMinus el) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void visit(ElementService el) {
        throw new RuntimeException("Not implemented");
    }

//    @Override
//    public void visit(ElementFetch el) {
//        throw new RuntimeException("Not implemented");
//    }

    @Override
    public void visit(ElementSubQuery el) {
        throw new RuntimeException("Not implemented");
    }
}

/*
 * (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * $Id$
 */