package melt.instrument;

import java.io.Serializable;

public class Predicate implements Serializable {

	private static final long serialVersionUID = -6775548673062385131L;
	
	private String className;
	private String methodName;
	private String signature;
	private int lineNumber;
	
	private String expression;
	
	// four types: if, do, while, and for
	public enum TYPE {IF, DO, WHILE, FOR, FOREACH}
	private TYPE type;
	
	public Predicate(String className, String methodName, String signature, int lineNumber, String expression, TYPE type) {
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
		this.signature = signature;
		this.expression = expression;
		this.type = type;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Predicate [ className = " + className + ", methodName = "
				+ methodName + ", signature = " + signature + ", lineNumber = " + lineNumber + ", expression = "
				+ expression + ", type = " + type + " ]";
	}

}
