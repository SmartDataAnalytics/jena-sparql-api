package org.aksw.jena_sparql_api.decision_tree.impl.jena;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.aksw.jena_sparql_api.decision_tree.api.DtVisitor;
import org.aksw.jena_sparql_api.decision_tree.api.InnerNode;
import org.aksw.jena_sparql_api.decision_tree.api.LeafNode;
import org.apache.jena.atlas.io.IndentedWriter;

public class DtVisitorToString<C, V, T>
	implements DtVisitor<C, V, T>
{
	protected IndentedWriter writer;
	protected ByteArrayOutputStream out;

	public DtVisitorToString() {
		super();
		this.out = new ByteArrayOutputStream();
		this.writer = new IndentedWriter(out);
	}

	public DtVisitorToString(IndentedWriter writer) {
		super();
		this.writer = writer;
	}

	public String getResult() {
		writer.flush();
		return out.toString(); //StandardCharsets.UTF_8.toString());
	}
	
	@Override
	public <X> X visit(InnerNode<C, V, T> node) {
		
		writer.println("SWITCH (" + (node.getClassifier()) + ") {");
		
		for (InnerNode<C, V, T> innerNode : node.getInnerNodes()) {
			Object value = innerNode.getReachingValue();
			
			writer.println((value == null ? "DEFAULT: " : "CASE " + value + ": ") + "{");
			writer.incIndent();
			innerNode.accept(this);
			writer.decIndent();
			writer.println("}");
		}

		for (LeafNode<C, V, T> leafNode : node.getLeafNodes()) {
			Object value = leafNode.getReachingValue();
			
			writer.print((value == null ? "DEFAULT: " : "CASE " + value + ": ") + "{ ");
			leafNode.accept(this);
			writer.println(" }");
		}

		writer.println("}");
	
		return null;
	}

	@Override
	public <X> X visit(LeafNode<C, V, T> leafNode) {
		writer.print(leafNode.toString());
		return null;
	}
	
	
}
