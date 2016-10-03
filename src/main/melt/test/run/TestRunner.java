package melt.test.run;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import melt.Config;

public class TestRunner {
	
	public static void run(Object[] test) throws MalformedURLException {
		try {
			URL[] cp = new URL[Config.CLASSPATH.length];
			for (int i = 0; i < Config.CLASSPATH.length; i++) {
				File f = new File(Config.CLASSPATH[i]);
				cp[i] = f.toURI().toURL();
			}
			URLClassLoader cl = new URLClassLoader(cp);
			Class<?> c = cl.loadClass(Config.MAINCLASS);
			Method m = c.getMethod(Config.METHOD, Config.CLS);
			m.invoke(c.newInstance(), test);
			cl.close();
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException | ClassNotFoundException | NoSuchMethodException | SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

}
