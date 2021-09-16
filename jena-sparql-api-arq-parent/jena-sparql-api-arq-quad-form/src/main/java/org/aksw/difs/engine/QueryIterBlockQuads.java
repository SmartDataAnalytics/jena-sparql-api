package org.aksw.difs.engine;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter1;
import org.apache.jena.sparql.serializer.SerializationContext;

public class QueryIterBlockQuads extends QueryIter1
{
    public static QueryIterator create(QueryIterator input, QuadPattern pattern,
                                       ExecutionContext execContext) {
        return new QueryIterBlockQuads(input, pattern, execContext);
    }

    private QuadPattern pattern;
    private QueryIterator output;

    private QueryIterBlockQuads(QueryIterator input, QuadPattern pattern ,
                                  ExecutionContext execContext) {
        super(input, execContext);
        this.pattern = pattern;
        QueryIterator chain = getInput();
        for (Quad quad : pattern)
            chain = new QueryIterQuadPattern(chain, quad, execContext);
        output = chain;
    }

    @Override
    protected boolean hasNextBinding() {
        return output.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
        return output.nextBinding();
    }

    @Override
    protected void closeSubIterator() {
        if ( output != null )
            output.close();
        output = null;
    }

    @Override
    protected void requestSubCancel() {
        if ( output != null )
            output.cancel();
    }

    @Override
    protected void details(IndentedWriter out, SerializationContext sCxt) {
        out.print(Lib.className(this));
        out.println();
        out.incIndent();

        // FIXME Tidy up
        // FmtUtils.formatPattern(out, pattern, sCxt);
        out.println(pattern);
        out.decIndent();
    }
}