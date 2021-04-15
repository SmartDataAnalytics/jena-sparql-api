package org.aksw.jena_sparql_api.http.repository.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import com.google.common.collect.Iterables;

public class HttpObjectSerializerModel
	implements HttpObjectSerializer<Model>
{
//	protected HttpResourceRepositoryFromFileSystem repo;
//	protected ResourceStore store;
	protected RDFFormat preferredOutFormat;
	
	public HttpObjectSerializerModel() {
		this(RDFFormat.TURTLE_PRETTY);
	}

	public HttpObjectSerializerModel(RDFFormat preferredOutFormat) {
		super();
		this.preferredOutFormat = preferredOutFormat;
	}
	
	@Override
	public HttpUriRequest createHttpRequest(String uri) {
		HttpUriRequest result =
				RequestBuilder.get(uri)
				.setHeader(HttpHeaders.ACCEPT, "application/n-triples")
				.setHeader(HttpHeaders.ACCEPT_ENCODING, "identity,bzip2,gzip")
				.build();
		return result;
	}

	@Override
	public RdfHttpEntityFile serialize(String uri, ResourceStore store, Model model) throws IOException {
		//RDFLanguages.fi
		RDFFormat effectiveOutFormat;
		String fileExt = Iterables.getFirst(preferredOutFormat.getLang().getFileExtensions(), null);
		effectiveOutFormat = fileExt == null
				? RDFFormat.TURTLE_PRETTY
				: preferredOutFormat;
		
		fileExt = Iterables.getFirst(effectiveOutFormat.getLang().getFileExtensions(), null);
		
		Objects.requireNonNull(fileExt, "Should not happen");
		
		java.nio.file.Path tmpFile = Files.createTempFile("data-", fileExt);
		try(OutputStream out = Files.newOutputStream(tmpFile, StandardOpenOption.WRITE)) {
			RDFDataMgr.write(out, model, effectiveOutFormat);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		RdfEntityInfo entityInfo = ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class)
				.setContentType(effectiveOutFormat.getLang().getContentType().getContentTypeStr());
		RdfHttpEntityFile result = store.putWithMove(uri, entityInfo, tmpFile);
		HttpResourceRepositoryFromFileSystemImpl.computeHashForEntity(result, null);
		
		return result;
	}

	@Override
	public Model deserialize(RdfHttpEntityFile entity) {
		String absPath = entity.getAbsolutePath().toString();
		Model result = RDFDataMgr.loadModel(absPath);
		return result;
	}
}