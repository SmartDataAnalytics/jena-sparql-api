package org.aksw.jena_sparql_api.conjure.entity.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import org.aksw.jena_sparql_api.conjure.entity.algebra.HashUtils;
import org.aksw.jena_sparql_api.conjure.entity.algebra.HasherBase;
import org.aksw.jena_sparql_api.conjure.entity.algebra.Op;
import org.aksw.jena_sparql_api.conjure.entity.algebra.OpCode;
import org.aksw.jena_sparql_api.conjure.entity.algebra.OpConvert;
import org.aksw.jena_sparql_api.conjure.entity.algebra.OpPath;
import org.aksw.jena_sparql_api.conjure.entity.algebra.OpUtils;
import org.aksw.jena_sparql_api.conjure.entity.algebra.OpValue;
import org.aksw.jena_sparql_api.conjure.entity.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.entity.utils.PathCoder;
import org.aksw.jena_sparql_api.conjure.entity.utils.PathCoderRegistry;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpResourceFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.apache.http.entity.ContentType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Single;

public class OpExecutor
    implements OpVisitor<Path>
{
    private static final Logger logger = LoggerFactory.getLogger(OpExecutor.class);

    // The repo reference here is only used for reading
    protected HttpResourceRepositoryFromFileSystem repository;

    // The repo may also have the hashStore configured
    protected ResourceStore hashStore;

    protected OpVisitor<String> hasher;

    public static String hashForOpPath(OpPath op, HttpResourceRepositoryFromFileSystem repository) {
        String str = op.getName();
        Path path = Paths.get(str);
        RdfHttpEntityFile entity = repository.getEntityForPath(path);

        if(entity == null) {
            throw new RuntimeException("Null entity; Should not happen; path=" + path);
        }

        String pathHash = ResourceStore.readHash(entity, "sha256");

        String result = HashUtils.computeHash("path", pathHash);

        return result;
    }

    public OpExecutor(HttpResourceRepositoryFromFileSystem repository, ResourceStore hashStore) {
        super();
        this.repository = repository;
        this.hashStore = hashStore;
        this.hasher = HasherBase.create(op -> OpExecutor.hashForOpPath(op, repository));
    }

    public static Path execute(Op op, HttpResourceRepositoryFromFileSystem repository, ResourceStore hashStore) {
        OpExecutor executor = new OpExecutor(repository, hashStore);
        Path result = op.accept(executor);
        return result;
    }

    public Op optimizeInPlace(Op op) {
        Op result = OpUtils.optimize(op, hasher, hashStore);
        return result;
    }
//
//	public Path cacheLookup(Op op, Function<Op, > ) {
//		String hash = op.accept(hasher);
//		Path path = hashSpace.get(hash);
//
//		if(path != null) {
//
//		}
//
//	}

    @Override
    public Path visit(OpCode op) {
        String coderName = op.getCoderName();
        PathCoder coder = PathCoderRegistry.get().getCoder(coderName);

        Objects.requireNonNull(coder, "No coder for " + coderName);
        Op subOp = op.getSubOp();
        Path srcPath = subOp.accept(this);

        Path tgtPath = getTargetPath(op);

        try {
            Files.createDirectories(tgtPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean isDecode = Boolean.TRUE.equals(op.isDecode());
        logger.info("Writing " + tgtPath + " via " + coderName + " " + (isDecode ? "decoding" : "encoding") + " of " + srcPath);

        Single<Integer> task = isDecode
            ? coder.decode(srcPath, tgtPath)
            : coder.encode(srcPath, tgtPath);

        task.blockingGet();

        return tgtPath;
    }

    public Path getTargetPath(Op op) {
        String hash = op.accept(hasher);

        RdfHttpResourceFile res = hashStore.getResource(hash);
        RdfHttpEntityFile entity = res.allocate(ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class)
                .setContentType(ContentType.APPLICATION_OCTET_STREAM.toString()));


        Path result = entity.getAbsolutePath();
        //Path result = hashStore.getResource()

        return result;
    }

    @Override
    public Path visit(OpConvert op) {
        Op subOp = op.getSubOp();

        Path srcPath = subOp.accept(this);
        Path tgtPath = getTargetPath(op);

        String srcContentType = op.getSourceContentType();
        String tgtContentType = op.getTargetContentType();

        logger.info("Writing " + tgtPath + " ("+ tgtContentType + ") from " + srcPath + "(" + srcContentType + ")");

        convert(srcPath, tgtPath, srcContentType, tgtContentType);

        return tgtPath;
    }

    @Override
    public Path visit(OpPath op) {
        String str = op.getName();
        Path path = Paths.get(str);
        //RdfHttpEntityFile entity = repository.getEntityForPath(path);
        //String hash = ResourceStore.readHash(entity, hashName);

        //String hash = op.getName();
        //hashSpace.get(hash)

        return path;
    }

    @Override
    public Path visit(OpValue op) {
        String str = Objects.toString(op.getValue());
        Path result = Paths.get(str);
        return result;
    }


    public static Single<Integer> convert(Path srcPath, Path tgtPath, String srcContentType, String tgtContentType) {

        try {
            Files.createDirectories(tgtPath.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        Model m = ModelFactory.createDefaultModel();


        Lang srcLang = RDFLanguages.nameToLang(srcContentType);
        Lang tgtLang = RDFLanguages.nameToLang(tgtContentType);

        try {
            try(InputStream in = Files.newInputStream(srcPath, StandardOpenOption.READ)) {
                RDFDataMgr.read(m, in, srcLang);
            }
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        try(OutputStream out = Files.newOutputStream(tgtPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            RDFDataMgr.write(out, m, tgtLang);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Single.just(0);

    }

//	public CompletableFuture<FileEntityEx> execPlan(FileEntityEx source, List<TransformStep> plan) throws Exception {
//
//		//List<TransformStep> plan = createPlan(source, rawTgtBasePath, tgtContentType, tgtEncodings);
//		Path src = source.getRelativePath();
//		Path dest = src;
//
//		// Execute the plan
//		for(TransformStep step : plan) {
//			dest = step.destPath;
//
//			if(!Files.exists(dest)) {
//
//				Path tmp = allocateTmpFile(dest);
//				logger.info("Creating " + dest + " from " + src + " via tmp " + tmp);
//				step.method.apply(src, tmp)
//					.blockingGet();
//
//				Files.move(tmp, dest, StandardCopyOption.ATOMIC_MOVE);
//			}
//
//
//			src = dest;
//		}
//
//		System.out.println("Generated: " + dest);
//
//		return null;
//	}

}