package org.aksw.jena_sparql_api.io.filter;

/**
 * In an ideal world, we could transform any stream of data to another stream
 * using a transformation function like Function<InputStream, InputStream>
 * 
 * However, in practice, we sometimes need to write intermediate files:
 * 
 * - A filter may not natively support streaming input and needs to save it as a file first
 * - A filter may not natively support streaming output and needs to serve it from a file
 * - It may be more efficient to pass data to filters as files - without the jvm in between
 * 
 * So a filter becomes more of a Function<FileOrStream, FileOrStream>
 * 
 * And finally, we want to control file creation if it has to happen
 * 
 * 
 * @author raven
 *
 */
public interface Filter {

}
