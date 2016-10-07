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

package fr.inrialpes.tyrexmo.qcwrapper.sparqlalg;

import fr.inrialpes.tyrexmo.queryanalysis.CommonWrapper;
import fr.inrialpes.tyrexmo.queryanalysis.TransformAlgebra;

import fr.inrialpes.tyrexmo.testqc.ContainmentSolver;
import fr.inrialpes.tyrexmo.testqc.ContainmentTestException;

import amod.PropertyTester;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.rdf.model.Model;

public class SPARQLAlgebraWrapper extends CommonWrapper implements ContainmentSolver {

    public void warmup() {};

    /**
     * Developed based on the model of amod.PropertyTester
     */
    public boolean entailed( Query q1, Query q2 ) throws ContainmentTestException {
	if ( supportedTest( q1, q2 ) ) {
	    PropertyTester solver = new PropertyTester();
	    return solver.isContained( Algebra.compile(q1), Algebra.compile(q2) );
	} else {
	    throw new ContainmentTestException( "Cannot deal with such a test" );
	}
    }

    public boolean entailedUnderSchema( Model schema, Query q1, Query q2 ) throws ContainmentTestException {
	throw new ContainmentTestException( "Cannot deal with schema" );
    };

    public boolean entailedUnderSchema( String schema, Query q1, Query q2 ) throws ContainmentTestException {
	throw new ContainmentTestException( "Cannot deal with schema" );
    };

    public void cleanup() {};

    private boolean supportedTest( Query q1, Query q2 ) {
	TransformAlgebra ta1 = new TransformAlgebra( q1 );
	TransformAlgebra ta2 = new TransformAlgebra( q2 );
	if ( containsOptional( ta1, ta2 ) || isValidQueryType( q1, q2 ) || isCyclic( ta1, ta2 ) )
	    return false;
	else 
	    return true;
    }
}
