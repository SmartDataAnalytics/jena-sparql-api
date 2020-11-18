package org.aksw.jena_sparql_api.conjure.plugin;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefDcat;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefEmpty;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefGit;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefOp;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobBinding;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jena_sparql_api.conjure.job.api.Macro;
import org.aksw.jena_sparql_api.conjure.job.api.MacroParam;
import org.aksw.jena_sparql_api.conjure.resourcespec.ResourceSpec;
import org.aksw.jena_sparql_api.conjure.resourcespec.ResourceSpecInline;
import org.aksw.jena_sparql_api.conjure.resourcespec.ResourceSpecUrl;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpPropertyPath;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal0;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal1;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal2;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversalSelf;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfoDefault;
import org.aksw.jena_sparql_api.io.hdt.JenaPluginHdt;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.utils.turtle.TurtleWriterNoBase;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginConjure
    implements JenaSubsystemLifecycle {

    public void start() {
        init();
    }

    @Override
    public void stop() {
    }


    public static void init() {
        TurtleWriterNoBase.register();
        JenaPluginHdt.init();

        JenaPluginUtils.registerResourceClasses(
                Job.class, JobBinding.class, JobInstance.class, Macro.class,
                MacroParam.class);

        JenaPluginUtils.registerResourceClasses(
                OpTraversal.class, OpTraversal0.class, OpTraversal1.class,
                OpTraversal2.class, OpTraversalSelf.class, OpPropertyPath.class);

        JenaPluginUtils.registerResourceClasses(RdfEntityInfoDefault.class);

        JenaPluginUtils.registerResourceClasses(Checksum.class);

        JenaPluginUtils.registerResourceClasses(
                DataRef.class, DataRefCatalog.class, DataRefDcat.class, DataRefEmpty.class, DataRefExt.class,
                DataRefGit.class, DataRefOp.class, DataRefSparqlEndpoint.class, DataRefUrl.class);

        JenaPluginUtils.registerResourceClasses(
                org.aksw.jena_sparql_api.conjure.entity.algebra.Op.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.Op0.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.Op1.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.OpCode.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.OpConvert.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.OpPath.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.OpValue.class
                );

        JenaPluginUtils.registerResourceClasses(
                org.aksw.jena_sparql_api.conjure.dataset.algebra.Op.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.Op1.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.Op2.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpCoalesce.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpError.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpHdtHeader.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpJobInstance.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpMacroCall.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpN.class,
//        		org.aksw.jena_sparql_api.conjure.dataset.algebra.OpNothing.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpQueryOverViews.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSequence.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSet.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpStmtList.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar.class,
                org.aksw.jena_sparql_api.conjure.dataset.algebra.OpWhen.class
                );

        JenaPluginUtils.registerResourceClasses(
                ResourceSpec.class, ResourceSpecInline.class, ResourceSpecUrl.class);
    }
}
