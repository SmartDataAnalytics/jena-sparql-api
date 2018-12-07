/**
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

import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;

/** Unwrap groups of one where they do not matter.
 * <p>
 *  They do matter for <code>OPTIONAL { { ?s ?p ?o FILTER(?foo) } }</code>.
 */
public class ElementTransformCleanGroupsOfOne extends ElementTransformCopyBase {
    // Improvements: scan group elements to work out for non-reducable adjacents.
    // These ones may clash with an adjeact one in the group above.
    // ElementTransformCleanGroupsOfOne -> ElementTransformCleanGroups
    
    public ElementTransformCleanGroupsOfOne() {}
    
    @Override
    public Element transform(ElementGroup eltGroup, List<Element> elts) {
        if ( elts.size() != 1 )
            return super.transform(eltGroup, elts) ;
        Element elt = elts.get(0) ;
        if ( ( elt instanceof ElementTriplesBlock ) ||
             ( elt instanceof ElementPathBlock ) ||
             ( elt instanceof ElementFilter ) )
            return super.transform(eltGroup, elts) ;    // No transformation.
        return elt ;
    }

    // Special case: If Optional, and the original had a {{}} protected filter, keep {{}}
    // transform/ElementGroup has already run so undo if necessary.
    @Override
    public Element transform(ElementOptional eltOptional, Element transformedElt) {
        // RHS of optional is always an ElementGroup in a normal syntax tree.
        if ( ! ( transformedElt instanceof ElementGroup ) ) {
            // DRY
            ElementGroup protectedElt = new ElementGroup() ;
            protectedElt.addElement(transformedElt);
            transformedElt = protectedElt ;
        }
        
        // Step 1 : does the original eltOptional has a {{}} RHS? 
        Element x = eltOptional.getOptionalElement() ;
        
        if ( ! ( x instanceof ElementGroup ) )
            // No. But it is not possible in written query syntax to have a nongroup as the RHS. 
            return super.transform(eltOptional, transformedElt) ;
        // So far - {}-RHS.
        ElementGroup eGroup = (ElementGroup)x ;
        
        // Is it {{}}?
        //ElementGroup inner = getGroupInGroup(x) ;
        if ( eGroup.getElements().size() != 1 )
            return super.transform(eltOptional, transformedElt) ;
        Element inner = eGroup.getElements().get(0) ;
        if ( ! ( inner instanceof ElementGroup ) )
            return super.transform(eltOptional, transformedElt) ;
        // Yes - {{}}
        ElementGroup innerGroup = (ElementGroup)inner ;
        // Unbundle multiple levels.
        innerGroup = unwrap(innerGroup) ;
        boolean mustProtect = containsFilter(innerGroup) ;
        
        if ( mustProtect ) {
            // No need to check for {{}} in elt1 as the transform(ElementGroup) will have processed it.
            ElementGroup protectedElt = new ElementGroup() ;
            protectedElt.addElement(transformedElt);
            return new ElementOptional(protectedElt) ;
        } 
        // No need to protect - process as usual.
        return super.transform(eltOptional, transformedElt) ;
    }
    
    private boolean containsFilter(ElementGroup eltGroup) {
        //return eltGroup.getElements().stream().anyMatch(el2 ->( el2 instanceof ElementFilter ) ) ;
        boolean result = false;
        for(Element e : eltGroup.getElements()) {
            if(e instanceof ElementFilter) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    // Removed layers of groups of one.  Return inner most group.
    private ElementGroup unwrap(ElementGroup eltGroup) {
        if ( eltGroup.getElements().size() != 1 )
            return eltGroup ;
        Element el = eltGroup.getElements().get(0) ;
        if ( ! ( el instanceof ElementGroup ) )
            return eltGroup ;
        ElementGroup eltGroup2 = (ElementGroup)el ;
        return unwrap(eltGroup2) ; 
    }

}

