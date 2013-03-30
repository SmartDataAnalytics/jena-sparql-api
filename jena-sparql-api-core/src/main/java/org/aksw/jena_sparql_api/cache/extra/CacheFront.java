package org.aksw.jena_sparql_api.cache.extra;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 4:23 PM
 * /
public class CacheFront {
    ClosingResultSet(ResultSetFactory.fromXML(in),
}
*/

/*

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
      case Some(in) => return new ClosingResultSet(ResultSetFactory.fromXML(in), in);
      case None => {
        val rs = decoratee.executeSelect(query)

        cacheResultSetXml(rs, file);

        val in = new FileInputStream(file);
        return new ClosingResultSet(ResultSetFactory.fromXML(in), in);
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

  /*
  def cacheWrite(file: File, out: InputStream) : Unit = {
    println("Creating cache file for: " + file)
    file.getParentFile.mkdirs
    StreamUtils.copyThenClose(in, new FileOutputStream(file))
  }
  */
  /*
	def cacheWrite(file : File, obj : Object) : Unit = {
		println("Creating cache file for: " + file)
		file.getParentFile.mkdirs

		SerializationUtils.serializeXml(obj, file)
	}* /
}
  */