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

package fr.inrialpes.tyrexmo.testqc;

import com.hp.hpl.jena.query.Query;
// or ontology.OntModel
import com.hp.hpl.jena.rdf.model.Model;

public interface ContainmentSolver {

    public void warmup() throws ContainmentTestException;

    public boolean entailed( Query q1, Query q2 ) throws ContainmentTestException;
	
    public boolean entailedUnderSchema( Model schema, Query q1, Query q2 ) throws ContainmentTestException;

    // Provisory before schemas are really parsed from Jena
    public boolean entailedUnderSchema( String schema, Query q1, Query q2 ) throws ContainmentTestException;

    public void cleanup() throws ContainmentTestException;

}
