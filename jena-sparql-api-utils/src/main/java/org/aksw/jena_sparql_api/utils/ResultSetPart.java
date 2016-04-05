package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

public class ResultSetPart {
    private List<String> varNames;
    private List<Binding> rows;


    public ResultSetPart() {
        this(Collections.emptyList(), new ArrayList<Binding>());
    }

    public ResultSetPart(List<String> varNames) {
        this(varNames, Collections.emptyList());
    }

    public ResultSetPart(List<String> varNames, List<Binding> rows) {
        super();
        this.varNames = varNames;
        this.rows = rows;
    }

    public List<Binding> getBindings() {
        return rows;
    }

    public List<String> getVarNames() {
        return varNames;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rows == null) ? 0 : rows.hashCode());
        result = prime * result
                + ((varNames == null) ? 0 : varNames.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResultSetPart other = (ResultSetPart) obj;
        if (rows == null) {
            if (other.rows != null)
                return false;
        } else if (!rows.equals(other.rows))
            return false;
        if (varNames == null) {
            if (other.varNames != null)
                return false;
        } else if (!varNames.equals(other.varNames))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ResultSetPart [varNames=" + varNames + ", rows=" + rows + "]";
    }

    public static ResultSet toResultSet(ResultSetPart rsp) {
        Iterator<Binding> it = rsp.getBindings().iterator();
        QueryIter queryIter = new QueryIterPlainWrapper(it);
        ResultSet result = ResultSetFactory.create(queryIter, rsp.getVarNames());
        return result;
        //ResultSetCloseable result = new ResultSetCloseable(baseRs, closeable);

    }
}
