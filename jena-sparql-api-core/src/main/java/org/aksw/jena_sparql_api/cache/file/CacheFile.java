package org.aksw.jena_sparql_api.cache.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.aksw.commons.collections.IClosable;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.ResultSetClosable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;


class ClosableInputStream
    implements IClosable
{
    private InputStream stream;

    public ClosableInputStream(InputStream stream) {
        this.stream = stream;
    }

    public InputStream getStream() {
        return stream;
    }

    @Override
    public void close() {
        if(stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 10:25 PM
 */
public class CacheFile
{
    private static final Logger logger = LoggerFactory.getLogger(CacheFile.class);

    private String basePath;

    private QueryExecutionFactory decoratee;

    //@Override
    public ResultSet executeSelectXml(String queryString) throws IOException {
        logger.trace("Query is: " + queryString);

        File file = getCacheFile(decoratee.getId(), decoratee.getState(), queryString);
        logger.trace("Cache file: " + file.getAbsolutePath());

        if(file == null) {
            //ResultSet rs = decoratee.createSelect(queryString).execute();
            //cacheResultSetXml(rs, file);
        }
        InputStream in = new FileInputStream(file);
        IClosable closable = new ClosableInputStream(in);
        return new ResultSetClosable(ResultSetFactory.fromXML(in), closable);
    }

    private File getCacheFile(String id, String state, String queryString) {
      // Create the directory
      String dirPart = basePath + "/" + StringUtils.urlEncode(id) + "/";

      String dir = dirPart + state + "/";

      File result = new File("" + dir + StringUtils.md5Hash(queryString));

      return result;
    }

    void cacheResultSetXml(ResultSet rs, File file) throws IOException {
       File directory = file.getParentFile();
       directory.mkdirs();

       File tmpFile = File.createTempFile("sparqlResultSet_", ".tmp", directory);

       OutputStream out = new FileOutputStream(tmpFile);
       try {
         ResultSetFormatter.outputAsXML(out, rs);
       } finally {
         out.flush();
         out.close();
       }
       tmpFile.renameTo(file);
     }

}


/*
{
  private def logger = LoggerFactory.getLogger(classOf[CachingSparqlEndpoint])

  def md5SumString(bytes : Array[Byte]) : String = {
    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(bytes)

    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }

  def main(args : Array[String]) : Unit = {
    val coreEndpoint = new HttpSparqlEndpoint("localhost:8890/sparql", Set("http://dbpedia.org"))
    val endpoint = new CachingSparqlEndpoint(coreEndpoint, "/tmp/sparqlCache")

    val qc = new QueryCollection(coreEndpoint, "Select ?s ?p ?o From <http://dbpedia.org> { ?s ?p ?p . } Limit 10000")

    qc.zipWithIndex foreach {case (qs, i) => {
      println(i)
      val res = qs.getResource("?s")

      val rs = endpoint.executeSelect("Select * From <http://dbpedia.org> { <" + res + "> a ?o . }")
    }}
  }


  def defaultGraphNames() = decoratee.defaultGraphNames

  def executeConstruct(query: String) = decoratee.executeConstruct(query)
  def executeConstruct(query: String, model: Model) = decoratee.executeConstruct(query, model)

  def executeAsk(query: String) = decoratee.executeAsk(query)

  /*
  def convertJsonToResultSet(json : String) : ResultSet = {
    val bais = new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8")));
    return ResultSetFactory.fromJSON(bais);
  }

  def convertResultSetToJSon(rs : ResultSet) : String = {
    val baos = new ByteArrayOutputStream();
    ResultSetFormatter.outputAsJSON(baos, rs)
    return baos.toString() //Charset.forName("UTF-8"))
  }
  */

  /**
   * Note: If there is no cache hit, we first serialize the result set to a file
   * and then re-read the result set from file rather than making a rewindable
   * in-memory copy.
   * This is in order to deal with large result sets.
   *
   * Maybe a more sophisticated solution would first try to load e.g. a few
   * 1000 rows into memory, and if it turns out to be too much, only then read from
   * the cache file
   * /
  def executeSelectXml(query: String) : ResultSet = {
    logger.trace("Query is: " + query)

    val file = cacheFile(decoratee.id, decoratee.defaultGraphNames, query)
    logger.trace("Cache file: " + file.getAbsolutePath)

    cacheLookup(file) match {
      case Some(in) => return new ResultSetClose(ResultSetFactory.fromXML(in), in);
      case None => {
        val rs = decoratee.executeSelect(query)

        cacheResultSetXml(rs, file);

        val in = new FileInputStream(file);
        return new ResultSetClose(ResultSetFactory.fromXML(in), in);
      }
    }
  }

  def executeSelectInMemory(query: String) : ResultSet = {
    logger.trace("Query is: " + query)

    val file = cacheFile(decoratee.id, decoratee.defaultGraphNames, query)
    logger.trace("Cache file: " + file.getAbsolutePath)

    cacheLookup(file) match {
      case Some(in) => {
        val result = ResultSetFactory.makeRewindable(ResultSetFactory.fromXML(in))
        in.close
        return result
      }
      case None => {
        val tmpRs = decoratee.executeSelect(query)
        val rs = ResultSetFactory.makeRewindable(tmpRs)

        cacheResultSetXml(rs, file)

        rs.reset
        return rs
      }
    }
  }


  def cacheResultSetXml(rs : ResultSet, file : File) : Unit = {
    val directory = file.getParentFile
    directory.mkdirs

    val tmpFile = File.createTempFile("sparqlResultSet_", ".tmp", directory)

    val out = new FileOutputStream(tmpFile)
    try {
      ResultSetFormatter.outputAsXML(out, rs)
    } finally {
      out.flush
      out.close
    }
    tmpFile.renameTo(file)
  }

  def executeSelect(query: String) = executeSelectXml(query)
  //def executeSelect(query: String) = executeSelectInMemory(query)


  /*
  def executeSelect(query: String) : ResultSet = {
    println("Query is: " + query)

    val file = cacheFile(decoratee.serviceName, decoratee.defaultGraphNames, query)

    cacheLookup(file, classOf[String]) match {
      case Some(obj) => return convertJsonToResultSet(obj)
      case None => {
        val rs = decoratee.executeSelect(query)

        val result = new ResultSetMem(rs)

        val cacheEntry = convertResultSetToJSon(result)
        result.reset

        cacheWrite(file, cacheEntry)


        return result
      }
    }
  }
  * /

  def cacheFile(id : String, graphNames : Set[String], query : String) : File = {
    // Create the directory
    var dirPart = basePath + "/" + URLEncoder.encode(id, "UTF-8") + "/"

    val dir = dirPart + (if(graphNames.isEmpty) "default/" else (URLEncoder.encode(graphNames.mkString("_"), "UTF-8") + "/"))

    /*
    graphName match {
      case Some(name) => dir += URLEncoder.encode(name, "UTF-8") + "/"
      case None => dir += "default/"
    }* /

    val file = new File("" + dir + md5SumString(query.getBytes))

    return file
  }

  /*
	def cacheLookup[T](file : File, resultType : Class[T]) : Option[T] = {
    if(file.exists) {
      try {
        println("Cache hit for: " + file);
        val tmp = SerializationUtils.deserializeXML(file)
        tmp match {
          case v : T => return Some(v)
          case _ => return None
        }
      }
      catch {
        case e => {
          println("Corrupted cache - deleting file")
          file.delete
        }
      }
    }

    return None
  }* /

  def cacheLookup(file: File) : Option[InputStream] = {
    if(file.exists) {
      try {
        logger.debug("Cache hit for: " + file);
        return Some(new FileInputStream(file));
      }
      catch {
        case e => {
          logger.debug("Corrupted cache - deleting file")
          file.delete
        }
      }
    }

    return None
  }


  override def id() = "cached_" + decoratee.id
*/