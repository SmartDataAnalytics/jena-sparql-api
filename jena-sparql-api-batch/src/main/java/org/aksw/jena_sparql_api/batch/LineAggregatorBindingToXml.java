package org.aksw.jena_sparql_api.batch;

import java.util.Collection;

import org.springframework.batch.item.file.transform.LineAggregator;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class LineAggregatorBindingToXml
    implements LineAggregator<Binding> {

    public Collection<String> varNames;

    public LineAggregatorBindingToXml(Collection<String> varNames) {
        this.varNames = varNames;
    }

    @Override
    public String aggregate(Binding binding) {
        String result = "  " + ResultSetXmlUtils.toXmlStringBinding(binding, varNames);
        return result;
    }
}