package org.aksw.jena_sparql_api.decision_tree.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.FunctionLabel;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;


/**
 * 
 *  https://stackoverflow.com/questions/32210952/scala-invalidclassexception-no-valid-constructor
 *  
 *  To allow subtypes of non-serializable classes to be serialized, the subtype may assume
 *  responsibility for saving and restoring the state of the supertype's public, protected,
 *  and (if accessible) package fields. The subtype may assume this responsibility only if the
 *  class it extends has an accessible no-arg constructor to initialize the class's state.
 *  It is an error to declare a class Serializable if this is not the case.
 *  The error will be detected at runtime.  
 * 
 */
class Hack extends ExprFunction1 {
	public Hack() {
		super(null, null);
	}
	
	public Hack(Expr expr, String fName, String opSign) {
		super(expr, fName, opSign);
		// TODO Auto-generated constructor stub
	}

	public Hack(Expr expr, String fName) {
		super(expr, fName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public NodeValue eval(NodeValue v) {
		return null;
	}

	@Override
	public Expr copy(Expr expr) {
		// TODO Auto-generated method stub
		return null;
	}
}

/** An serializable expression that returns its argument */
public class E_SerializableIdentity extends Hack implements Serializable {
	private static final long serialVersionUID = 0L;

	private static final String symbol = "serializableIdentity";
	
	public E_SerializableIdentity(Expr expr) {
		super(expr, symbol);
	}

	public E_SerializableIdentity(Expr expr, String altSymbol) {
		super(expr, altSymbol);
	}
	
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException 
    {       
        String str = in.readUTF();
        Expr expr = ExprUtils.parse(str);
        try {
            Field f = ExprFunction1.class.getDeclaredField("expr");
            f.setAccessible(true);
            f.set(this, expr);;
            f.setAccessible(false);
            
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        opSign = symbol;
        funcSymbol = new FunctionLabel(symbol); 
        // in.defaultReadObject();
    }
 
    private void writeObject(ObjectOutputStream out) throws IOException 
    {
    	String str = ExprUtils.fmtSPARQL(expr);
        out.writeUTF(str);
        // out.defaultWriteObject();
    }

	@Override
	public NodeValue eval(NodeValue v) {
		return v;
	}

	@Override
	public Expr copy(Expr expr) {
		return new E_SerializableIdentity(expr);
	}
	
	public static E_SerializableIdentity wrap(Expr expr) {
		return new E_SerializableIdentity(expr);
	}
}
