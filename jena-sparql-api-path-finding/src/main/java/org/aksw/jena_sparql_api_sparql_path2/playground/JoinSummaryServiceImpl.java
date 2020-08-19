package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.Map;

import org.aksw.jena_sparql_api.lookup.LookupService;
import org.apache.jena.graph.Node;

public class JoinSummaryServiceImpl
    implements JoinSummaryService
{
    protected LookupService<Node, Map<Node, Number>> fwdLookup;
    protected LookupService<Node, Map<Node, Number>> bwdLookup;

    public JoinSummaryServiceImpl(LookupService<Node, Map<Node, Number>> fwdLookup,
            LookupService<Node, Map<Node, Number>> bwdLookup) {
        super();
        this.fwdLookup = fwdLookup;
        this.bwdLookup = bwdLookup;
    }

    @Override
    public Map<Node, Map<Node, Number>> fetch(Iterable<Node> predicates, boolean reverse) {

        Map<Node, Map<Node, Number>> result = !reverse
                ? fwdLookup.fetchMap(predicates)
                : bwdLookup.fetchMap(predicates)
                ;

        return result;
    }


}
