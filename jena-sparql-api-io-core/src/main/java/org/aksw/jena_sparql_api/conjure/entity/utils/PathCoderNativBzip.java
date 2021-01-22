package org.aksw.jena_sparql_api.conjure.entity.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import io.reactivex.rxjava3.core.Single;

public class PathCoderNativBzip
    implements PathCoder
{
    @Override
    public boolean cmdExists() {
        return true;
    }

    @Override
    public Single<Integer> decode(Path input, Path output) {
        try(InputStream in = new BZip2CompressorInputStream(Files.newInputStream(input), true)) {
            Files.copy(in, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Single.just(0);
    }

    @Override
    public Single<Integer> encode(Path input, Path output) {
        throw new RuntimeException("not implemented yet");
//		try(InputStream in = new BZip2(Files.newInputStream(input))) {
//			Files.copy(in, output);
//		}
        //return Single.just(0);
    }
}
