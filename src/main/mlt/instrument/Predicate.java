package mlt.instrument;

import java.io.Serializable;
import java.util.HashSet;

public class Predicate implements Serializable {

	private static final long serialVersionUID = -6775548673062385131L;
	
	private String className;
	private String methodName;
	private int lineNumber;
	private String expression;
	private String type; // four types: if, do, while, and for
	private HashSet<Integer> depInputs; // indexes of the inputs that the predicate depends on
	
	public Predicate(String className, String methodName, int lineNumber, String expression, String type) {
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public HashSet<Integer> getDepInputs() {
		return depInputs;
	}

	public void setDepInputs(HashSet<Integer> depInputs) {
		this.depInputs = depInputs;
	}

	@Override
	public String toString() {
		return "Predicate [ className = " + className + ", methodName = "
				+ methodName + ", lineNumber = " + lineNumber + ", expression = "
				+ expression + ", type = " + type + ", depInputs = " + depInputs + " ]";
	}

}
