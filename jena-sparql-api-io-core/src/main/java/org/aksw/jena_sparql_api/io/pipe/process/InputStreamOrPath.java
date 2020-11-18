package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Object that holds either an input stream or a path.
 * Used to set up process redirection.
 *
 * @author raven
 *
 */
public class InputStreamOrPath {
    protected Path path;
    protected InputStream inputStream;

    public InputStreamOrPath(Path path) {
        super();
        this.path = Objects.requireNonNull(path);
    }

    public InputStreamOrPath(InputStream inputStream) {
        super();
        this.inputStream = Objects.requireNonNull(inputStream);
    }

    public static InputStreamOrPath from(Path path) {
        return new InputStreamOrPath(path);
    }

    public static InputStreamOrPath from(InputStream inputStream) {
        return new InputStreamOrPath(inputStream);
    }

    public boolean isPath() {
        return path != null;
    }

    public Path getPath() {
        return path;
    }

    public boolean isStream() {
        return inputStream != null;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
