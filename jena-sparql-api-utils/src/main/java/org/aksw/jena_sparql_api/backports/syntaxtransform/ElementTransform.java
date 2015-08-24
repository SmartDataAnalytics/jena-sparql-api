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

/** Transformation function on an Element
 *  @see ElementTransformer
 */
public interface ElementTransform
{
    public Element transform(ElementTriplesBlock el) ;
    public Element transform(ElementPathBlock el) ;
    public Element transform(ElementFilter el, Expr expr2) ;
    public Element transform(ElementAssign el, Var v, Expr expr2) ;
    public Element transform(ElementBind el, Var v, Expr expr2) ;
    public Element transform(ElementData el) ;
    public Element transform(ElementDataset el, Element subElt) ;
    public Element transform(ElementUnion el, List<Element> elements) ;
    public Element transform(ElementOptional el, Element opElt) ;
    public Element transform(ElementGroup el, List<Element> members) ;
    public Element transform(ElementNamedGraph el, Node gn, Element subElt) ;
    public Element transform(ElementExists el, Element subElt) ;
    public Element transform(ElementNotExists el, Element subElt) ;
    public Element transform(ElementMinus el, Element eltRHS) ;
    public Element transform(ElementService el, Node service, Element subElt) ;
    public Element transform(ElementSubQuery el, Query query) ;
}

