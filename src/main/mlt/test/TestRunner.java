package mlt.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mlt.Config;

public class TestRunner {

	public void run(Object[] test) {
		try {
			Class<?> c = Class.forName(Config.CLASS_NAME);
			Object o = c.newInstance();
			Method m = c.getMethod(Config.METHOD_NAME, Config.CLS);
			m.invoke(o, test);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}