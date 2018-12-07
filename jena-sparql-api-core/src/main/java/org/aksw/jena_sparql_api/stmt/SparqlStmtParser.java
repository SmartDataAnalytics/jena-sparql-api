package org.aksw.jena_sparql_api.stmt;

import java.util.function.Function;

import org.apache.jena.shared.PrefixMapping;

public interface SparqlStmtParser
    extends Function<String, SparqlStmt>
{

	public static SparqlStmtParser wrapWithNamespaceTracking(PrefixMapping pm, Function<String, SparqlStmt> raw) {
		return s -> {
			SparqlStmt r = raw.apply(s);
			if(r.isParsed()) {
				PrefixMapping pm2 = null;
				if(r.isQuery()) {
					pm2 = r.getAsQueryStmt().getQuery().getPrefixMapping();
				} else if(r.isUpdateRequest()) {
					pm2 = r.getAsUpdateStmt().getUpdateRequest().getPrefixMapping();
				}
				
				if(pm2 != null) {
					pm.setNsPrefixes(pm2);
				}
			}
			return r;
		};
	};

}
