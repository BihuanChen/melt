package edu.ntu.instrument;

public class Predicate {

	private String className;
	private String methodName;
	private int lineNumber;
	private String expression;
	private String type; // if, do, while, and for
	
	private int[] counters; // counters[0] for true branch, counters[1] for false branch
	
	
	public Predicate(String className, String methodName, int lineNumber, String expression, String type) {
		this.className = className;
		this.methodName = methodName;
		this.lineNumber = lineNumber;
		this.expression = expression;
		this.type = type;
		
		this.counters = new int[2];
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

	public void resetCounters() {
		if (type.equals("do")) {
			counters[0] = -1;
		} else {
			counters[0] = 0;
		}
		counters[1] = 0;
	}
	
	public void incCounter(boolean branch) {
		if (branch) {
			counters[0] += 1;
		} else {
			counters[1] += 1;
		}
	}
	
	@Override
	public String toString() {
		return "Predicate [className = " + className + ", methodName = " + methodName
				+ ", lineNumber = " + lineNumber + ", expression = " + expression
				+ ", type = " + type + "]";
	}

}
