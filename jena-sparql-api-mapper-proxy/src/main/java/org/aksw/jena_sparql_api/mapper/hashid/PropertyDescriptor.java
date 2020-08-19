package org.aksw.jena_sparql_api.mapper.hashid;

import java.util.Collection;
import java.util.function.Function;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.P_Path0;

public class PropertyDescriptor {
    protected ClassDescriptor classDescriptor;
    protected P_Path0 path;

    protected Function<? super Resource, ? extends Collection<? extends RDFNode>> rawProcessor;

    protected boolean includedInHashId;

    // If true the values are represented as IRIs but for the sake of identity are treated as string literals
    // Hence, ID computation does not descend into those RDF resources
    protected boolean isIriType;

    /**
     * Only valid if includeInHashId is true:
     * Usually the rdf property (path) is combined with the value to form the id. This excludes the rdf property
     * and thus only uses the value.
     *
     */
    protected boolean rdfPropertyExcludedFromHashId;

    public PropertyDescriptor(ClassDescriptor classDescriptor, P_Path0 path) {
        super();
        this.classDescriptor = classDescriptor;
        this.path = path;
    }

    public P_Path0 getPath() {
        return path;
    }

    public PropertyDescriptor setRdfPropertyExcludedFromHashId(boolean onOrOff) {
        this.rdfPropertyExcludedFromHashId = onOrOff;
        return this;
    }

    public boolean isExcludeRdfPropertyFromHashId() {
        return rdfPropertyExcludedFromHashId;
    }

    public PropertyDescriptor setIncludedInHashId(boolean onOrOff) {
        this.includedInHashId = onOrOff;
        return this;
    }

    public boolean isIncludedInHashId() {
        return includedInHashId;
    }

    public PropertyDescriptor setIriType(boolean isIriType) {
        this.isIriType = isIriType;
        return this;
    }

    public boolean isIriType() {
        return isIriType;
    }

    public void setRawProcessor(Function<? super Resource, ? extends Collection<? extends RDFNode>> rawProcessor) {
        this.rawProcessor = rawProcessor;
    }

    public Function<? super Resource, ? extends Collection<? extends RDFNode>> getRawProcessor() {
        return rawProcessor;
    }
}
