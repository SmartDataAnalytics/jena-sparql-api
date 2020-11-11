package org.aksw.jena_sparql_api.conjure.fluent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUtils;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.lang.arq.ParseException;

public class JobUtils {

    public static Job fromSparqlFile(String path) throws FileNotFoundException, IOException, ParseException {
        // TODO Add API for Query objects to fluent
        List<SparqlStmt> stmts = Streams.stream(SparqlStmtUtils.processFile(DefaultPrefixes.prefixes, path))
                .collect(Collectors.toList());

        List<String> stmtStrs = stmts.stream()
                .map(Object::toString)
                .collect(Collectors.toList());


        //RDFDataMgrRx
        //SparqlStmtUtils.


//
//		List<String> queries = RDFDataMgrEx.loadQueries(path, DefaultPrefixes.prefixes).stream()
//				.map(Object::toString)
//				.collect(Collectors.toList());
        ConjureBuilder cj = new ConjureBuilderImpl();

        String opVarName = "ARG";
        Op op = cj.fromVar(opVarName).stmts(stmtStrs).getOp();

//		Set<String> vars = OpUtils.mentionedVarNames(op);
//		for(SparqlStmt stmt : stmts) {
//			System.out.println("Env vars: " + SparqlStmtUtils.mentionedEnvVars(stmt));
//		}

        Map<String, Boolean> combinedMap = stmts.stream()
            .map(SparqlStmtUtils::mentionedEnvVars)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Set<String> envVars = combinedMap.keySet();
//		System.out.println("All env vars: " + combinedMap);


//		System.out.println("MentionedVars: " + vars);

        Job result = Job.create(cj.getContext().getModel())
                .setOp(op)
                .setDeclaredVars(envVars)
                .setOpVars(Collections.singleton(opVarName));


        return result;
    }

    public static JobInstance createJobInstanceWithCopy(
            Job job,
            Map<String, ? extends Node> env,
            Map<String, ? extends Op> map) {
        Model model = ModelFactory.createDefaultModel();
        Job j = JenaPluginUtils.copyClosureInto(job, Job.class, model);

        JobInstance result = model.createResource().as(JobInstance.class)
                .setJob(j);

        result.getEnvMap().putAll(env);

        for(Entry<String, ? extends Op> e : map.entrySet()) {
            String k = e.getKey();
            Op v = e.getValue();

            Op vv = JenaPluginUtils.copyClosureInto(v, Op.class, model);
            result.getOpVarMap().put(k, vv);
        }

        return result;
    }

    /**
     * Create a job instance in the same model as the job
     *
     * @param job
     * @param env
     * @param map
     * @return
     */
    public static JobInstance createJobInstance(
            Job job,
            Map<String, ? extends Node> env,
            Map<String, ? extends Op> map) {
        Model model = job.getModel();
        JobInstance result = model.createResource().as(JobInstance.class)
                .setJob(job);

        result.getEnvMap().putAll(env);

        for(Entry<String, ? extends Op> e : map.entrySet()) {
            String k = e.getKey();
            Op v = e.getValue();

            Op vv = JenaPluginUtils.copyClosureInto(v, Op.class, model);
            result.getOpVarMap().put(k, vv);
        }

        return result;
    }

    /**
     * Return the associated op with all all variables (literals and resources) substituted
     *
     * @param jobInstance
     * @return
     */
    public static Op materializeJobInstance(JobInstance jobInstance) {
        Map<String, Node> envMap = jobInstance.getEnvMap();
        Map<String, Op> opMap = jobInstance.getOpVarMap();

        Job job = jobInstance.getJob();
        Op tmp = job.getOp();
        Op op = JenaPluginUtils.reachableClosure(tmp, Op.class);

        NodeTransform nodeTransform = x -> NodeUtils.substWithLookup2(x, envMap::get);
        //NodeTransform nodeTransform = new NodeTransformRenameMap(envMap);
        OpUtils.applyNodeTransform(op, nodeTransform, stmt -> SparqlStmtUtils.optimizePrefixes(SparqlStmtParserImpl.create(DefaultPrefixes.prefixes).apply(stmt)));

        // OpUtils.applyNodeTransform();


        //ResourceUtils.reachableClosure(root)

        Op inst = OpUtils.substituteVars(op, opMap::get);

        return inst;
    }
}
