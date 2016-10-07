/*
 * Copyright (C) INRIA, 2012-2013
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.tyrexmo.queryanalysis;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.query.Query;

public class CommonWrapper {
	
    protected boolean containsOptional( TransformAlgebra left, TransformAlgebra right ) {
	return (left.containsOpt() || right.containsOpt());
    }

	//TODO: add same number and type of variables
	/**
	 *  same left-hand and right-hand side query encoding
	 */
    protected boolean useSameEncoding( Query leftQuery, Query rightQuery ) {
	return leftQuery.equals(rightQuery);
    }
	/**
	 * restrict query types to SELCET and ASK
	 */
    protected boolean isValidQueryType( Query leftQuery, Query rightQuery ) {
	return (leftQuery.isConstructType() || rightQuery.isConstructType() || 
		leftQuery.isDescribeType() || rightQuery.isDescribeType());
    }

    /**
     *  check if the left and right-hand side queries
     *  have the same number and type of distinguished
     *  variables.
     * 
     * JE: It seems to check if variables have the same NAME!
     * which is wrong, their arity should be checked (what about "*"?).
     */
    protected boolean haveSameDistVar( Query leftQuery, Query rightQuery ) {
	List <String> rightQueryDistVars = rightQuery.getResultVars(); 
	Collections.sort( rightQueryDistVars );
	List <String> leftQueryDistVars = leftQuery.getResultVars();
	Collections.sort( leftQueryDistVars );
	return !rightQueryDistVars.equals( leftQueryDistVars );	
	//		return !leftQuery.getResultVars().equals(rightQuery.getResultVars());
    }

    /**
     * check if there is a cycle in the queries among the non-distinguished 
     * variables
     * 
     * @return
     */
    protected boolean isCyclic( TransformAlgebra left, TransformAlgebra right ) {
	CycleAnalysis l = new CycleAnalysis( left.getTriples() );
	CycleAnalysis r = new CycleAnalysis( right.getTriples() );
	if (l.isCyclic() || r.isCyclic())
	    return true;
	else {
	    return false;
	}
    }

}

