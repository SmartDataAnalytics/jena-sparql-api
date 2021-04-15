package org.aksw.jena_sparql_api.syntax;

import java.util.function.Function;

import org.apache.jena.sparql.modify.request.UpdateAdd;
import org.apache.jena.sparql.modify.request.UpdateClear;
import org.apache.jena.sparql.modify.request.UpdateCopy;
import org.apache.jena.sparql.modify.request.UpdateCreate;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.sparql.modify.request.UpdateLoad;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.modify.request.UpdateMove;
import org.apache.jena.sparql.modify.request.UpdateVisitor;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.update.Update;

public class UpdateTransformVisitor implements UpdateVisitor {
	protected Update result = null;
	protected Function<? super Element, ? extends Element> transform;

	public UpdateTransformVisitor(Function<? super Element, ? extends Element> transform) {
		super();
		this.transform = transform;
	}

	public Update getResult() {
		return result;
	}

	@Override
	public void visit(UpdateDrop update) {
		result = update;
	}

	@Override
	public void visit(UpdateClear update) {
		result = update;
	}

	@Override
	public void visit(UpdateCreate update) {
		result = update;
	}

	@Override
	public void visit(UpdateLoad update) {
		result = update;
	}

	@Override
	public void visit(UpdateAdd update) {
		result = update;
	}

	@Override
	public void visit(UpdateCopy update) {
		result = update;
	}

	@Override
	public void visit(UpdateMove update) {
		result = update;
	}

	@Override
	public void visit(UpdateDataInsert update) {
		result = update;
	}

	@Override
	public void visit(UpdateDataDelete update) {
		result = update;
	}

	@Override
	public void visit(UpdateDeleteWhere update) {
		// TODO This needs transformation
		result = update;
	}

	@Override
	public void visit(UpdateModify update) {
		Element before = update.getWherePattern();
		Element after = transform.apply(before);

		UpdateModify tmp = UpdateUtils.clone(update);
		tmp.setElement(after);
		result = tmp;
	}
}