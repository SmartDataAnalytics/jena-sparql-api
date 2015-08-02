package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.QuadContainmentChecker;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.UpdateContext;

/**
 * @author raven
 *
 */
public class UpdateStrategyEventSource
     implements UpdateStrategy<UpdateExecutionFactoryEventSource>
{
    public static final int defaultBatchSize = 128;
    public static final QuadContainmentChecker defaultQuadContainmentChecker = new QuadContainmentCheckerSimple();

    private Integer batchSize;
    private QuadContainmentChecker containmentChecker;

    public UpdateStrategyEventSource() {
        this(defaultBatchSize, defaultQuadContainmentChecker);
    }

    public UpdateStrategyEventSource(Integer batchSize) {
        this(batchSize, defaultQuadContainmentChecker);
    }

    public UpdateStrategyEventSource(QuadContainmentCheckerSimple quadContainmentChecker) {
        this(defaultBatchSize, defaultQuadContainmentChecker);
    }

    public UpdateStrategyEventSource(Integer batchSize, QuadContainmentChecker containmentChecker) {
        super();
        this.batchSize = batchSize;
        this.containmentChecker = containmentChecker;
    }

    @Override
    public UpdateExecutionFactoryEventSource apply(SparqlService sparqlService) {
        UpdateContext updateContext = new UpdateContext(sparqlService, batchSize, containmentChecker);

        UpdateExecutionFactoryEventSource result = new UpdateExecutionFactoryEventSource(updateContext);
        return result;
    }
}
