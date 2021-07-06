package org.aksw.jena_sparql_api.utils.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.jena_sparql_api.utils.PrefixUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.model.PrefixMapAdapter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Quad;

public class StreamRDFDeferred
    implements StreamRDF
{
    protected StreamRDF delegate;
    protected List<Quad> deferredData;

    protected Node mostRecentTripleSubject = null;
    protected Node mostRecentQuadGraph = null;

    // allowExtendBasePrefixes: If true then every value passed to prefix(...) is added to the base prefixes
    protected boolean allowExtendBasePrefixes = true;
    protected PrefixMapping basePrefixes;

    protected PrefixMapping usedPrefixes = new PrefixMappingImpl();

    // Adapter to bridge between riot and rdf
    protected PrefixMap usedPrefixAdapter = new PrefixMapAdapter(usedPrefixes);

    // Wait for this number of events before used-prefix
    // analysis and prefix optimize-prefixes is performed and output starts
    protected long remainingQuadDeferrals;
    protected long remainingBatchDeferrals;

    // The base url; only the first non-value will be passed to the underlying writer
    protected String base = null;


    public StreamRDFDeferred(
            StreamRDF delegate,
            boolean allowExtendBasePrefixes,
            PrefixMapping basePrefixes,
            long remainingBatchDeferrals,
            long remainingQuadDeferrals,
            String base) {
        super();
        this.delegate = delegate;
        this.allowExtendBasePrefixes = allowExtendBasePrefixes;
        this.basePrefixes = basePrefixes;
        this.remainingQuadDeferrals = remainingQuadDeferrals;
        this.remainingBatchDeferrals = remainingBatchDeferrals;
        this.base = base;


        // If either value is 0 (or less) then the other must be 0 as well
        if (remainingBatchDeferrals <= 0 || remainingQuadDeferrals <= 0) {
            this.remainingBatchDeferrals = 0;
            this.remainingQuadDeferrals = 0;
        }

        this.deferredData = remainingQuadDeferrals > 1 ? new ArrayList<>() : null;
    }

    @Override
    public void triple(Triple triple) {
        if (remainingQuadDeferrals > 0) {
            quad(Quad.create(Quad.defaultGraphIRI, triple));
        } else {
            delegate.triple(triple);
        }
    }

    @Override
    public void quad(Quad quad) {
        if (remainingQuadDeferrals > 0) {

            QuadUtils.streamNodes(quad)
                .forEach(node -> PrefixUtils.usedPrefixes(node, basePrefixes, usedPrefixes));

            --remainingQuadDeferrals;

            if (remainingQuadDeferrals == 0) {

                sendDeferredData();
                delegate.quad(quad);

            } else {
                Node quadGraph = quad.getGraph();
                Node tripleSubject = quad.getSubject();

                boolean isConsecutiveGraph = Objects.equals(mostRecentQuadGraph, quadGraph);

                boolean isConsecutiveTuple = (isConsecutiveGraph && !Quad.isDefaultGraph(quadGraph)) ||
                        Objects.equals(mostRecentTripleSubject, tripleSubject);

                mostRecentQuadGraph = quadGraph;
                mostRecentTripleSubject = tripleSubject;

                if (!isConsecutiveTuple) {
                    --remainingBatchDeferrals;

                    if (remainingBatchDeferrals == 0) {
                        sendDeferredData();
                        delegateTripleOrQuad(quad);
                    } else {
                        deferredData.add(quad);
                    }
                } else {
                    deferredData.add(quad);
                }
            }


        } else {
            // Write out the current item
            delegate.quad(quad);
        }
    }

    public void delegateTripleOrQuad(Quad quad) {
        if (Quad.isDefaultGraph(quad.getGraph())) {
            delegate.triple(quad.asTriple());
        } else {
            delegate.quad(quad);
        }
    }

    public void sendDeferredData() {
        // Skip once the deferred data has been sent
        if (deferredData == null) {
            return;
        }

        if (base != null) {
            delegate.base(base);
        }

        StreamRDFOps.sendPrefixesToStream(usedPrefixes, delegate);

        for (Quad d : deferredData) {
            delegateTripleOrQuad(d);
        }

        remainingBatchDeferrals = 0;
        remainingQuadDeferrals = 0;

        // Unset deferredData to Allow for garbage collection
        deferredData = null;
    }

    // Only the first base is delegated
    @Override
    public void base(String base) {
        if (this.base == null) {
            this.base = base;
        }
    }

    @Override
    public void prefix(String prefix, String iri) {
        if (remainingQuadDeferrals > 0 && allowExtendBasePrefixes) {
            basePrefixes.setNsPrefix(prefix, iri);
        }
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void finish() {
        // At latest we nee to send out the deferred data now
        sendDeferredData();
        delegate.finish();
    }

}
