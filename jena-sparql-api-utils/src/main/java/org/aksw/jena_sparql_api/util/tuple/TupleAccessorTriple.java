/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.aksw.jena_sparql_api.util.tuple;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 */
public class TupleAccessorTriple
    implements TupleAccessor<Triple, Node>
{
    public static final TupleAccessorTriple INSTANCE = new TupleAccessorTriple();

    @Override
    public int getDimension() {
        return 3;
    }

    @Override
    public Node get(Triple triple, int idx) {
        return getComponent(triple, idx);
    }

    /**
     * Surely there is some common util method somewhere?
     *
     *
     * @return
     */
    public static Node getComponent(Triple triple, int idx) {
        switch (idx) {
        case 0: return triple.getSubject();
        case 1: return triple.getPredicate();
        case 2: return triple.getObject();
        default: throw new IndexOutOfBoundsException("Cannot access index " + idx + " of a triple");
        }
    }

    @Override
    public <T> Triple restore(T obj, TupleAccessorCore<? super T, ? extends Node> accessor) {
//        validateRestoreArg(accessor);

        return new Triple(
                accessor.get(obj, 0),
                accessor.get(obj, 1),
                accessor.get(obj, 2));
    }

}
