package mlt.test.run;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import mlt.Config;
import mlt.test.Pair;
import mlt.test.Profiles;

public class TestRunnerUtil {

	public static void run(Object[] test) throws MalformedURLException {
		try {
			Profiles.executedPredicates = new ArrayList<Pair>();
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
