package org.aksw.jena_sparql_api.conjure.dataset.algebra;

public class OpMapper
	implements OpVisitor<Op>
{
	@Override
	public Op visit(OpDataRefResource op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpConstruct op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpUpdateRequest op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpUnion op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpPersist op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpVar op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpData op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpCoalesce op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpHdtHeader op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpSequence op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpSet op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpWhen op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpError op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpMacroCall op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpQueryOverViews op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpStmtList op) {
		System.out.println("Op: " + op);
		return null;
	}

	@Override
	public Op visit(OpJobInstance op) {
		System.out.println("Op: " + op);
		return null;
	}

//	@Override
//	public Op visit(OpNothing op) {
//		System.out.println("Op: " + op);
//		return null;
//	}
}
