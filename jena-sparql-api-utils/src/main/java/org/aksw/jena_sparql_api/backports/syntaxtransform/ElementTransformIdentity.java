package org.aksw.jena_sparql_api.backports.syntaxtransform;

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

import java.util.List ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query ;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementAssign;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementDataset;
import org.apache.jena.sparql.syntax.ElementExists;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementNotExists;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;

/** Transformation function on an Element that does nothing.
 * Do not extend - it assumes that no change below happens.
 * Use {@link ElementTransformCopyBase} instead.
 *  @see ElementTransformer
 *  @see ElementTransformCopyBase
 */
public final class ElementTransformIdentity implements ElementTransform
{
    public ElementTransformIdentity() {}
    private static ElementTransformIdentity singleton = new ElementTransformIdentity() ;
    public static ElementTransform get() { return singleton ; }

    @Override
    public Element transform(ElementTriplesBlock el) { return el ; }
    @Override
    public Element transform(ElementPathBlock el) { return el ; }
    @Override
    public Element transform(ElementFilter el, Expr expr2) { return el ; }
    @Override
    public Element transform(ElementAssign el, Var v, Expr expr2) { return el ; }
    @Override
    public Element transform(ElementBind el, Var v, Expr expr2) { return el ; }
    @Override
    public Element transform(ElementData el) { return el ; }
    @Override
    public Element transform(ElementUnion el, List<Element> elements) { return el ; }
    @Override
    public Element transform(ElementOptional el, Element opElt) { return el ; }
    @Override
    public Element transform(ElementGroup el, List<Element> members) { return el ; }
    @Override
    public Element transform(ElementDataset el, Element subElt) { return el ; }
    @Override
    public Element transform(ElementNamedGraph el, Node gn, Element subElt) { return el ; }
    @Override
    public Element transform(ElementExists el, Element subElt) { return el ; }
    @Override
    public Element transform(ElementNotExists el, Element subElt) { return el ; }
    @Override
    public Element transform(ElementMinus el, Element eltRHS) { return el ; }
    @Override
    public Element transform(ElementService el, Node service, Element subElt) { return el ; }
    @Override
    public Element transform(ElementSubQuery el, Query query) { return el ; }

//    @Override
//    public Element transform(ElementFind el, Var v, Triple triple2) {
//        return el;
//    }

    @Override
    public Triple transform(Triple triple) {
        return triple;
    }

    @Override
    public Quad transform(Quad quad) {
        return quad;
    }
}

