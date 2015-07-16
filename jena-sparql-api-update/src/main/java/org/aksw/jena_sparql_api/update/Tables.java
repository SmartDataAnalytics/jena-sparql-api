package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.utils.Vars;

import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.TableFactory;

public class Tables {
    private Table defaultGraphTable;
    private Table namedGraphTable;

    public Tables() {
        this(TableFactory.create(Vars.spo), TableFactory.create(Vars.gspo));
    }

    public Tables(Table defaultGraphTable, Table namedGraphTable) {
        super();
        this.defaultGraphTable = defaultGraphTable;
        this.namedGraphTable = namedGraphTable;
    }

    public Table getDefaultGraphTable() {
        return defaultGraphTable;
    }

    public Table getNamedGraphTable() {
        return namedGraphTable;
    }
}