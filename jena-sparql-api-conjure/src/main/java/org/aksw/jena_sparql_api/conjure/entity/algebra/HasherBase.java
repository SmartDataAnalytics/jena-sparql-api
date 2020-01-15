package org.aksw.jena_sparql_api.conjure.entity.algebra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.hash.HashCode;
import org.apache.jena.ext.com.google.common.hash.Hashing;
import org.apache.jena.ext.com.google.common.io.ByteSource;


public abstract class HasherBase
	implements OpVisitor<String>
{
	//protected Function<String, String> varToHash;
//	protected Map<String, String> varToHash;
//
//	public Hasher(Map<String, String> varToHash) {
//		super();
//		this.varToHash = varToHash;
//	}
//	
//	public static Hasher create(Map<String, String> varToHash) {
//		return new Hasher(varToHash);
//	}

//	public Hasher(Function<String, String> varToHash) {
//		super();
//		this.varToHash = varToHash;
//	}

//	public static Hasher create(Function<String, String> varToHash) {
//		return new Hasher(varToHash);
//	}

	@Override
	public String visit(OpCode op) {
		String result = HashUtils.computeHash(
				(op.isDecode() ? "decode" : "encode"),
				op.getSubOp().accept(this));
		return result;
	}

	@Override
	public String visit(OpConvert op) {
		String result = HashUtils.computeHash(
				op.getTargetContentType(),
				op.getSourceContentType(),
				op.getSubOp().accept(this));
		return result;		
	}

//	@Override
//	public String visit(OpPath op) {
//		String varName = op.getName();
//		String result = Objects.requireNonNull(varToHash.apply(varName));
//		return result;
//	}

	@Override
	public String visit(OpValue op) {
		Object value = op.getValue();
		String result = HashUtils.computeHash("value", Objects.toString(value));
		return result;
	}
	
	
	public static OpVisitor<String> create(Function<OpPath, String> opPathToHash) {
		return new HasherBase() {
			@Override
			public String visit(OpPath op) {
				String result = opPathToHash.apply(op);
				return result;
			}
		};
	}
}