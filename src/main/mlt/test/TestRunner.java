package mlt.test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import mlt.Config;

public class TestRunner {

	public void run(Object[] test) throws MalformedURLException {
		try {
			File f = new File(Config.CLASSPATH);
			URL[] cp = {f.toURI().toURL()};
			URLClassLoader cl = new URLClassLoader(cp);
			Class<?> c = cl.loadClass(Config.MAINCLASS);
			Object o = c.newInstance();
			Method m = c.getMethod(Config.METHOD, Config.CLS);
			m.invoke(o, test);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}
