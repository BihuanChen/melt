package mlt.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestRunner {

	private String className;
	private String methodName;
	@SuppressWarnings("rawtypes")
	private Class[] cls;
	
	public TestRunner(String className, String methodName, @SuppressWarnings("rawtypes") Class[] cls) {
		this.className = className;
		this.methodName = methodName;
		this.cls = cls;
	}

	public void run(Object[] obj) {
		try {
			Class<?> c = Class.forName(className);
			Object o = c.newInstance();
			Method m = c.getMethod(methodName, cls);
			m.invoke(o, obj);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		TestRunner runner = new TestRunner("mlt.dependency.test1.TestInputBranchDependencyInter1", "entryPointMain", new Class[]{int.class, int.class, int.class, boolean.class});
		runner.run(new Object[]{1, 2, 3, true});
	}

}
