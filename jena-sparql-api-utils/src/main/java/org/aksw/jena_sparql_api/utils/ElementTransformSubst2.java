package org.aksw.jena_sparql_api.utils;

import java.util.List;

import org.apache.jena.atlas.iterator.Iter;

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


import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Node_Variable ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.TriplePath ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.graph.NodeTransform ;
import org.apache.jena.sparql.syntax.Element ;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementPathBlock ;
import org.apache.jena.sparql.syntax.ElementTriplesBlock ;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;

import com.google.common.collect.Streams;


// THERE ARE TWO DIFFERENCES TO Jena's ElementTransformSubst:
// This class accepts a NodeTransformer for the ctor-arg.
// It transforms within ElementData elements

/** An {@link ElementTransform} which replaces occurences of a variable with a Node value.
 * Because a {@link Var} is a subclass of {@link Node_Variable} which is a {@link Node},
 * this includes variable renaming.
 * <p>
 * This is a transformation on the syntax - all occurences of a variable are replaced, even if
 * inside sub-select's and not project (which means it is effectively a different variable).
 */
public class ElementTransformSubst2 extends ElementTransformCopyBase {
    private final NodeTransform nodeTransform;

    public ElementTransformSubst2(NodeTransform nodeTransform) {
        this.nodeTransform = nodeTransform;
    }


    @Override
    public Element transform(ElementTriplesBlock el) {
        ElementTriplesBlock etb = new ElementTriplesBlock() ;
        boolean changed = false ;
        for (Triple t : el.getPattern()) {
            Triple t2 = transform(t) ;
            changed = changed || t != t2 ;
            etb.addTriple(t2) ;
        }
        if ( changed )
            return etb ;
        return el ;
    }


    @Override
    public Element transform(ElementPathBlock el) {
        ElementPathBlock epb = new ElementPathBlock() ;
        boolean changed = false ;
        for (TriplePath p : el.getPattern()) {
            TriplePath p2 = transform(p) ;
            changed = changed || p != p2 ;
            epb.addTriplePath(p2) ;
        }
        if ( changed )
            return epb ;
        return el ;
    }

    private TriplePath transform(TriplePath path) {
        Node s = path.getSubject() ;
        Node s1 = transform(s) ;
        Node o = path.getObject() ;
        Node o1 = transform(o) ;

        if ( path.isTriple() ) {
            Node p = path.getPredicate() ;
            Node p1 = transform(p) ;
            if ( s == s1 && p == p1 && o == o1 )
                return path ;
            return new TriplePath(Triple.create(s1, p1, o1)) ;
        }
        if ( s == s1 && o == o1 )
            return path ;
        return new TriplePath(s1, path.getPath(), o1) ;
    }

    public Triple transform(Triple triple) {
        Node s = triple.getSubject() ;
        Node s1 = transform(s) ;
        Node p = triple.getPredicate() ;
        Node p1 = transform(p) ;
        Node o = triple.getObject() ;
        Node o1 = transform(o) ;

        if ( s == s1 && p == p1 && o == o1 )
            return triple ;
        return Triple.create(s1, p1, o1) ;
    }

    protected Node transform(Node n) {
        return nodeTransform.apply(n) ;
    }


    public static ElementData transform(ElementData el, NodeTransform nodeTransform) {
        Table inTable = el.getTable();
        // Does not transform data in tables - only the result vars (jena 3.11.0)
        //Table outTable = NodeTransformLib.transform(inTable, nodeTransform);

        ElementData result = new ElementData();

        inTable.getVars().stream().map(nodeTransform).map(v -> (Var)v).forEach(result::add);
        Streams.stream(inTable.rows()).map(b -> transform(b, nodeTransform)).forEach(result::add);

        return result;
    }

    public static Binding transform(Binding b, NodeTransform transform) {
        BindingBuilder b2 = BindingBuilder.create();
        List<Var> vars = Iter.toList(b.vars()) ;
        for ( Var v : vars ) {
            Node n = b.get(v);
            Var v2 = (Var)transform.apply(v) ;
            Node n2 = transform.apply(n);
            b2.add(v2, n2);
        }
        return b2.build();
    }

    @Override
    public Element transform(ElementData el) {
        Element result = transform(el, nodeTransform);
        return result;
        //    	List<Var> vars = el.getVars().stream()
//    			.map(v -> Optional.ofNullable((Var)nodeTransform.apply(v)).orElse(v))
//    			.collect(Collectors.toList());
//
//
//    	TableData table = new TableData(vars, bindings);
    }
}
