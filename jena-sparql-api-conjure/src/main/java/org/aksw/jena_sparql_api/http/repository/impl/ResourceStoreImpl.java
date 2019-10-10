package org.aksw.jena_sparql_api.http.repository.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.conjure.utils.ContentTypeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.PathAnnotatorRdf;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpResourceFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;


interface ResourceSourceFile {
	Resource getResource(Path path);
}


class ResourceSourceFileImpl
	implements ResourceSourceFile 
{
	@Override
	public Resource getResource(Path path) {
		String fileName = path.getFileName().toString();
		RdfEntityInfo result = ContentTypeUtils.deriveHeadersFromFileExtension(fileName);
		return result;
	}
}

class ResourceUtils {
	/**
	 * Copies all direct outgoing properties of src to tgt
	 * 
	 * @param tgt
	 * @param src
	 * @return
	 */
	public static Resource copyDirectProperties(Resource tgt, Resource src) {
		StmtIterator it = src.listProperties();
		while(it.hasNext()) {
			Statement stmt = it.next();

			//RDFNode s = stmt.getSubject();
			Property p = stmt.getPredicate();
			RDFNode o = stmt.getObject();

			tgt.addProperty(p, o);
		}
		
		return tgt;
	}

	
	public static Resource merge(Collection<Resource> resources) {
		Model m = ModelFactory.createDefaultModel();
		Resource result = m.createResource();
		
		for(Resource r : resources) {
			Model n = r.getModel();
			StmtIterator it = n.listStatements();
			while(it.hasNext()) {
				Statement stmt = it.next();

				RDFNode s = stmt.getSubject();
				Property p = stmt.getPredicate();
				RDFNode o = stmt.getObject();
				
				if(s.equals(r)) { s = result; }
				if(o.equals(r)) { o = result; }
				
				// Should never be not a resource here
//				if(s.isResource()) {
				m.add(s.asResource(), p, o);
//				}
			}
		}
		
		return result;
	}
}


/**
 * ResourceManager for a single folder; does not manage multiple repositories
 * 
 * @author raven
 *
 */
public class ResourceStoreImpl
	implements ResourceStore
{
	protected Path basePath;
	
	protected String CONTENT = "_content";
	
	protected ResourceSourceFile resourceSource;
	protected PathAnnotatorRdf pathAnnotator;
	protected Function<String, Path> uriToRelPath;
		
	public ResourceStoreImpl(Path basePath) {
		this(basePath, UriToPathUtils::resolvePath);
	}

	public ResourceStoreImpl(Path basePath, Function<String, Path> uriToRelPath) {
		super();
		this.basePath = basePath;
		this.uriToRelPath = uriToRelPath;

		this.pathAnnotator = new PathAnnotatorRdfImpl();
		this.resourceSource = new ResourceSourceFileImpl();
	}

	
	@Override
	public Path getAbsolutePath() {
		return basePath;
	}
	
	public Resource getInfo(Path absPath) {
		Resource r1 = resourceSource.getResource(absPath);		
		Resource r2 = pathAnnotator.getRecord(absPath);
		
		Resource result = ResourceUtils.merge(Arrays.asList(r1, r2));
		
		return result;
	}
	
	public Path fullPath(String uri) {
		Path relPath = uriToRelPath.apply(uri);		
		Path result = basePath.resolve(relPath);

		return result;
	}
	
	public RdfHttpResourceFile get(String uri) {
		Path fullPath = fullPath(uri);
		RdfHttpResourceFileImpl result = new RdfHttpResourceFileImpl(this, fullPath);
		return result;
	}
	
	/**
	 * Put a given file into an appropriate place in the repository
	 * using a move operation 
	 * 
	 * @param uri
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public RdfHttpEntityFile putWithMove(String uri, RdfEntityInfo metadata, Path file) throws IOException {
		RdfHttpEntityFile result = allocateEntity(uri, metadata);
		result.updateInfo(record -> ResourceUtils.copyDirectProperties(record, metadata));
		Path tgtFile = result.getAbsolutePath();
		Files.move(file, tgtFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		
		return result;
	}
	
	
	
	public RdfHttpEntityFile getEntityForPath(Path absEntityPath) {
		RdfHttpEntityFile result;

		if(absEntityPath.startsWith(basePath)) {
			
			String fileName = absEntityPath.getFileName().toString();
			
			Path parent = absEntityPath.getParent();
			String parentFileName = parent.getFileName().toString();
			
			if(parentFileName.equals(CONTENT)) {
				Path resRelFolder = basePath.relativize(parent);
				
				Path entityRelFolder = parent.relativize(absEntityPath);
				//Path resFolder = parent.getParent();
				RdfHttpResourceFileImpl res = new RdfHttpResourceFileImpl(this, resRelFolder);
				result = new RdfHttpEntityFileImpl(res, entityRelFolder);	
			} else {
				result = null;
			}
		} else {
			result = null;
		}
		//Resource cachedInfo = pathAnnotator.getRecord(path);
		
//		RdfEntityInfo info = ContentTypeUtils.deriveHeadersFromFileExtension(fileName);

		return result;	
	}
	
	
//	EntitySpace getEntitySpace(Path basePath) {
//		
//	}

	public RdfHttpResourceFile getResource(String uri) {
		Path path = uriToRelPath.apply(uri);
		
		path = path.resolve(CONTENT);
		
		RdfHttpResourceFile result = new RdfHttpResourceFileImpl(this, path);
		return result;
	}
	
	public Collection<RdfHttpEntityFile> listEntities(Path relContentFolder) {
		//Path contentFolder = basePath.resolve(CONTENT);
		
		Path contentFolder = basePath.resolve(relContentFolder);
		
		List<RdfHttpEntityFile> result;
		
		try {
			result = (!Files.exists(contentFolder)
					? Collections.<Path>emptyList().stream()
					: Files.list(contentFolder))
						.filter(file -> pathAnnotator.isAnnotationFor(file).isEmpty())
						.map(this::getEntityForPath)
						.collect(Collectors.toList());			
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	@Override
	public boolean contains(Path path) {
		path = path.toAbsolutePath();
		
		boolean result = path.startsWith(basePath);
		return result;
	}

	@Override
	public Resource getInfo(Path path, String layer) {
		Resource result = contains(path)
			? pathAnnotator.getRecord(path)
			: null;

		return result;
	}

	@Override
	public RdfHttpEntityFile allocateEntity(String uri, Resource description) {
//		Path relPath = uriToRelPath.apply(uri);
//		path = path.resolve(CONTENT);

		RdfHttpResourceFile res = getResource(uri);
		Path resPath = res.getAbsolutePath();
		
		RdfHttpEntityFile result = allocateEntity(resPath, description);
		return result;
	}

	public RdfHttpResourceFile pathToResource(Path baseRelPath) {
		// TODO Validate the path
		RdfHttpResourceFile result = new RdfHttpResourceFileImpl(this, baseRelPath);

		return result;
	}
	
	public RdfHttpEntityFile allocateEntity(Path baseRelPath, Resource _info) {
		
		RdfEntityInfo info = _info.as(RdfEntityInfo.class);

		String suffix = ContentTypeUtils.toFileExtension(info);
		//pathToResource(baseRelPath);
		Path finalRelPath = Paths.get("data" + suffix); 

		
		RdfHttpResourceFile res = pathToResource(baseRelPath);
		
		
		RdfHttpEntityFile result = new RdfHttpEntityFileImpl(res, finalRelPath);
		
		return result;
	}


	@Override
	public void updateInfo(Path path, Consumer<? super Resource> callback) {
		Resource r = pathAnnotator.getRecord(path);
		callback.accept(r);
		pathAnnotator.setRecord(path, r);
	}

}
