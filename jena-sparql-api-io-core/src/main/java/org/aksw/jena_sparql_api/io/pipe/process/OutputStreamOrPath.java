package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Object that holds either an output stream or a path.
 * Used to set up process redirection.
 *
 * @author raven
 *
 */
public class OutputStreamOrPath {
    protected Path path;
    protected OutputStream OutputStream;

    public OutputStreamOrPath(Path path) {
        super();
        this.path = Objects.requireNonNull(path);
    }

    public OutputStreamOrPath(OutputStream OutputStream) {
        super();
        this.OutputStream = Objects.requireNonNull(OutputStream);
    }

    public boolean isPath() {
        return path != null;
    }

    public Path getPath() {
        return path;
    }

    public boolean isOutputStream() {
        return OutputStream != null;
    }

    public OutputStream getOutputStream() {
        return OutputStream;
    }
}
