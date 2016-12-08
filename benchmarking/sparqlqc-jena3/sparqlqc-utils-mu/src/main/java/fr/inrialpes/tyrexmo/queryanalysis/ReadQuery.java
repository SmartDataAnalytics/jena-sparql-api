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

/**
 * JE: This is useless... QueryFactory.read( f.toURI().toString() ) does it
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadQuery {
    private String firstQuery, secondQuery;
	
    public ReadQuery() {}
	
    /**
     * Read two queries from a file
     * @param f1 : filename of the first query
     *
     * @param f2: filename of sencod query
     * @throws IOException 
     */
    public ReadQuery (String f1, String f2) throws IOException {
	setFirstQuery( read(f1) );
	setSecondQuery( read(f2) );
    }
    /**
     *  Read a query from a file
     * @param fname a file containing a SPARQL query
     * @return returns the query as a string
     * @throws IOException
     */
    public String read(String fname) throws IOException {
	String qry = "";
	try {
	    BufferedReader in = new BufferedReader(new FileReader(fname));
	    String str;
	    while ((str = in.readLine()) != null) {
		qry += str;
	    }
	    in.close();
	} catch (IOException e) {}
	return qry;
    }
    protected void setFirstQuery(String q) {
	firstQuery = q;
    }
    protected void setSecondQuery(String q) {
	secondQuery = q;
    }
    public String getFirstQuery() {
	return firstQuery;
    }
    public String getSecondQuery() {
	return secondQuery;
    }
}	
