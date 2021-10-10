package org.aksw.jena_sparql_api.rx.io.resultset;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

import org.apache.jena.atlas.io.IO;

import com.google.gson.JsonElement;

/**
 * Writes a json array of indefinite length on the given output stream.
 * Writes '[' on start, ']' on close and ', ' before every item except the first
 *
 * @author raven
 *
 */
public class SinkStreamingJsonArray
   extends SinkStreamingBase<JsonElement>
{
    protected OutputStream out;
    protected PrintStream pout;

    protected boolean writeArray = true;
    protected boolean isFirstItem = true;

    public SinkStreamingJsonArray(OutputStream out) {
        this(out, true);
    }

    public SinkStreamingJsonArray(OutputStream out, boolean writeArray) {
        super();
        this.writeArray = writeArray;
        this.out = out;
        this.pout = new PrintStream(out);
    }

    @Override
    public void flush() {
        IO.flush(out);
    }

    @Override
    public void close() {
        IO.flush(out);
    }

    @Override
    protected void startActual() {
        if (writeArray) {
            pout.print("[");
        }
    }

    @Override
    protected void sendActual(JsonElement item) {
        if (isFirstItem) {
            isFirstItem = false;
        } else {
            if (writeArray) {
                pout.println(",");
            }
        }

        pout.print(Objects.toString(item));
    }

    @Override
    protected void finishActual() {
        if (writeArray) {
            pout.println("]");
        }
    }
}