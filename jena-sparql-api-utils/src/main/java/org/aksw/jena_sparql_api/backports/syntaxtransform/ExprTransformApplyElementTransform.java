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

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.expr.E_Exists;
import com.hp.hpl.jena.sparql.expr.E_NotExists;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy;
import com.hp.hpl.jena.sparql.syntax.Element;

/**
 * A copying transform that applies an ElementTransform syntax pattern of
 * E_Exist and E_NoExists
 * */
public class ExprTransformApplyElementTransform extends ExprTransformCopy
{
    private final ElementTransform transform ;
    
    public ExprTransformApplyElementTransform(ElementTransform transform) {
        this.transform = transform ; 
    }
    
    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg)
    {
        Element el2 = ElementTransformer.transform(funcOp.getElement(), transform) ;
        
        if ( el2 == funcOp.getElement() )
            return super.transform(funcOp, args, opArg) ;
        if ( funcOp instanceof E_Exists )
            return new E_Exists(el2) ;
        if ( funcOp instanceof E_NotExists )
            return new E_NotExists(el2) ;
        throw new ARQInternalErrorException("Unrecognized ExprFunctionOp: \n"+funcOp) ;
    }
}

