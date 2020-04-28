package org.aksw.jena_sparql_api.io.filter.sys;

import java.nio.file.Path;

/**
 * Generator for system commands that realize filters
 *
 * @author raven
 *
 */
public interface SysCallFn {
    String[] buildCheckCmd();

    /**
     *
     * @param input The input file. If null, the request is for command should be able to deal with input from stream.
     * @param output The output file. If null, the request is for the command's output to be suitable for use with an output stream.
     * @return The command strings or null if invocation with the provided arguments is not possible
     */
    default String[] buildCmdForFileToStream(Path input) { return null; }
    default String[] buildCmdForStreamToFile(Path input) { return null; }
    default String[] buildCmdForStreamToStream() { return null; }
    default String[] buildCmdForFileToFile(Path input, Path output) { return null; }
}
