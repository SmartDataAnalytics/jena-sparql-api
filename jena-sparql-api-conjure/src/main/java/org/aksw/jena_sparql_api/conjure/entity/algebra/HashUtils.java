package org.aksw.jena_sparql_api.conjure.entity.algebra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.hash.HashCode;
import org.apache.jena.ext.com.google.common.hash.HashFunction;
import org.apache.jena.ext.com.google.common.hash.Hashing;
import org.apache.jena.ext.com.google.common.io.ByteSource;

public class HashUtils {
	protected static HashFunction hashing = Hashing.sha256();
	
	public static String computeHash(List<?> args) {
		String str = "[" +
				Arrays.asList(args).stream().map(Object::toString)
				.collect(Collectors.joining(", ")) + "]";
		
		ByteSource bs = ByteSource.wrap(str.getBytes(StandardCharsets.UTF_8));
		HashCode hashCode;
		try {
			hashCode = bs.hash(hashing);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String result = hashCode.toString();
		return result;
	}

	public static String computeHash(String opName, String ...args) {
		String str = opName + "(" +
				Arrays.asList(args).stream().collect(Collectors.joining(", ")) + ")";
		
		ByteSource bs = ByteSource.wrap(str.getBytes(StandardCharsets.UTF_8));
		HashCode hashCode;
		try {
			hashCode = bs.hash(hashing);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String result = hashCode.toString();
		return result;
	}

}
