package org.aksw.jena_sparql_api.batch.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.springframework.batch.item.ItemWriter;


public class ItemWriterQuadFile
    implements ItemWriter<Quad>
{
    private File file;

    public ItemWriterQuadFile() {
    }

    public ItemWriterQuadFile(File file) {
        this.file = file;
    }

    public File getTarget() {
        return file;
    }

    public void setTarget(File file) {
        this.file = file;
    }


    @Override
    public void write(List<? extends Quad> quads) throws Exception {
        DatasetGraph dg = DatasetGraphFactory.create();
        for(Quad quad : quads) {
            dg.add(quad);
        }
                
        try(OutputStream out = new FileOutputStream(file)) {
            RDFDataMgr.write(out, dg, RDFFormat.NTRIPLES);
        }
    }
}
