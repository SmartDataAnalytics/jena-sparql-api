/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aksw.jena_sparql_api.backports.syntaxtransform;

import java.util.List ;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Var;
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

/** Create a copy if the Element(s) below has chanaged.
 * This is a common base class for writing recursive {@link ElementTransform}
 * in conjunction with being applied by {@link ElementTransformer}.
 */
public class ElementTransformCopyBase implements ElementTransform {
    // Note the use of == as object pointer equality.

    @Override
    public Element transform(ElementTriplesBlock el) {
        return el ;
    }

    @Override
    public Element transform(ElementPathBlock el) {
        return el ;
    }

    @Override
    public Element transform(ElementFilter el, Expr expr2) {
        if ( el.getExpr() == expr2 )
            return el ;
        return new ElementFilter(expr2) ;
    }

    @Override
    public Element transform(ElementAssign el, Var v, Expr expr2) {
        if ( el.getVar() == v && el.getExpr() == expr2 )
            return el ;
        return new ElementAssign(v, expr2) ;
    }

    @Override
    public Element transform(ElementBind el, Var v, Expr expr2) {
        if ( el.getVar() == v && el.getExpr() == expr2 )
            return el ;
        return new ElementAssign(v, expr2) ;
    }

    @Override
    public Element transform(ElementData el) {
        return el ;
    }

    @Override
    public Element transform(ElementUnion el, List<Element> elts) {
        if ( el.getElements() == elts )
            return el ;
        ElementUnion el2 = new ElementUnion() ;
        el2.getElements().addAll(elts) ;
        return el2 ;
    }

    @Override
    public Element transform(ElementOptional el, Element elt1) {
        if ( el.getOptionalElement() == elt1 )
            return el ;
        return new ElementOptional(elt1) ;
    }

    @Override
    public Element transform(ElementGroup el, List<Element> elts) {
        if ( el.getElements() == elts )
            return el ;
        ElementGroup el2 = new ElementGroup() ;
        el2.getElements().addAll(elts) ;
        return el2 ;
    }

    @Override
    public Element transform(ElementDataset el, Element elt1) {
        if ( el.getPatternElement() == elt1 )
            return el ;
        return new ElementDataset(el.getDataset(), elt1) ;
    }

    @Override
    public Element transform(ElementNamedGraph el, Node gn, Element elt1) {
        if ( el.getGraphNameNode() == gn && el.getElement() == elt1 )
            return el ;
        return new ElementNamedGraph(gn, elt1) ;
    }

    @Override
    public Element transform(ElementExists el, Element elt1) {
        if ( el.getElement() == elt1 )
            return el ;
        return new ElementExists(elt1) ;
    }

    @Override
    public Element transform(ElementNotExists el, Element elt1) {
        if ( el.getElement() == elt1 )
            return el ;
        return new ElementNotExists(elt1) ;
    }

    @Override
    public Element transform(ElementMinus el, Element elt1) {
        if ( el.getMinusElement() == elt1 )
            return el ;
        return new ElementMinus(elt1) ;
    }

    @Override
    public Element transform(ElementService el, Node service, Element elt1) {
        if ( el.getServiceNode() == service && el.getElement() == elt1 )
            return el ;
        return new ElementService(service, elt1, el.getSilent()) ;
    }

    @Override
    public Element transform(ElementSubQuery el, Query query) {
        if ( el.getQuery() == query )
            return el ;
        return new ElementSubQuery(query) ;
    }
}
