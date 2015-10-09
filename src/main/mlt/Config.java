package mlt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

	public enum Mode {RANDOM, SYSTEMATIC} 
	public enum Model {J48, NAIVEBAYES, LIBSVM, RANDOMFOREST}

	// default configuration parameters of ml-testing
	public static Mode MODE = Mode.SYSTEMATIC; 		// find an unexplored branch either randomly or systematically
	public static int MAX_ATTEMPTS = 3; 			// the maximum number of attempts to cover an unexplored branch
	public static Model MODEL = Model.RANDOMFOREST; // the applied classification model
	public static int TESTS_SIZE = 10; 				// the number of test cases that need to be generated at a time

	// configuration information of the target project
	public static String SOURCEPATH = null;
	public static String CLASSPATH = null;
	public static String MAINCLASS = null;
	public static String ENTRYMETHOD = null;
	
	// reflection information for running tests
	public static String METHOD = null;
	@SuppressWarnings("rawtypes")
	public static Class[] CLS = null;
	
	public static void loadProperties(String file) throws IOException {
		// load configuration
		Properties prop = new Properties();
		FileInputStream fis = new FileInputStream(file);
		prop.load(fis);

		SOURCEPATH = prop.getProperty("sourcepath");
		if (SOURCEPATH == null) {
			System.err.println("[ml-testing] configuration error: source path not set");
			System.exit(0);
		}
		System.out.println("[ml-testing] sourcepath = " + SOURCEPATH);
		
		CLASSPATH = prop.getProperty("classpath");
		if (CLASSPATH == null) {
			System.err.println("[ml-testing] configuration error: class path not set");
			System.exit(0);
		}
		System.out.println("[ml-testing] classepath = " + CLASSPATH);
		
		MAINCLASS = prop.getProperty("mainclass");
		if (MAINCLASS == null) {
			System.err.println("[ml-testing] configuration error: main class not set");
			System.exit(0);
		}
		System.out.println("[ml-testing] mainclass = " + MAINCLASS);
		
		ENTRYMETHOD = prop.getProperty("entrymethod");
		if (ENTRYMETHOD == null) {
			System.err.println("[ml-testing] configuration error: entry method not set");
			System.exit(0);
		}
		System.out.println("[ml-testing] entrymethod = " + ENTRYMETHOD);
		
		String p = prop.getProperty("exploration.mode");
		if (p != null) {
			if (p.equals("systematic")) {
				MODE = Mode.SYSTEMATIC;
			} else if (p.equals("random")) {
				MODE = Mode.RANDOM;
			} else {
				System.err.println("[ml-testing] configuration error: incorrect exploration mode (systematic or random)");
				System.exit(0);
			}
		}
		System.out.println("[ml-testing] exploration.mode = " + MODE.toString().toLowerCase());
				
		p = prop.getProperty("classification.model");
		if (p != null) {
			if (p.equals("j48")) {
				MODEL = Model.J48;
			} else if (p.equals("naivebayes")) {
				MODEL = Model.NAIVEBAYES;
			} else if (p.equals("libsvm")) {
				MODEL = Model.LIBSVM;
			} else if (p.equals("randomforest")) {
				MODEL = Model.RANDOMFOREST;
			} else {
				System.err.println("[ml-testing] configuration error: incorrect classification model (j48, naivebayes, libsvm, or randomforest)");
				System.exit(0);
			}
		}
		System.out.println("[ml-testing] classification.model = " + MODEL.toString().toLowerCase());

		p = prop.getProperty("max.attempts");
		if (p != null) {
			MAX_ATTEMPTS = Integer.valueOf(p);
		}
		System.out.println("[ml-testing] max.attempts = " + MAX_ATTEMPTS);
		
		p = prop.getProperty("tests.size");
		if (p != null) {
			TESTS_SIZE = Integer.valueOf(p);
		}
		System.out.println("[ml-testing] tests.size = " + TESTS_SIZE + "\n");
		
		fis.close();
		
		// derive the reflection information to run test cases
		METHOD = Config.ENTRYMETHOD.substring(Config.ENTRYMETHOD.indexOf(" ") + 1, Config.ENTRYMETHOD.indexOf("("));
		String[] clsStr = Config.ENTRYMETHOD.substring(Config.ENTRYMETHOD.indexOf("(") + 1, Config.ENTRYMETHOD.indexOf(")")).split(",");
		int size = clsStr.length;
		CLS = new Class[size];
		for (int i = 0; i < size; i++) {
			if (clsStr[i].equals("byte")) {
				CLS[i] = byte.class;
			} else if (clsStr[i].equals("short")) {
				CLS[i] = short.class;
			} else if (clsStr[i].equals("int")) {
				CLS[i] = int.class;
			} else if (clsStr[i].equals("long")) {
				CLS[i] = long.class;
			} else if (clsStr[i].equals("boolean")) {
				CLS[i] = boolean.class;
			} else if (clsStr[i].equals("float")) {
				CLS[i] = float.class;
			} else if (clsStr[i].equals("double")) {
				CLS[i] = double.class;
			} else {
				System.err.println("[ml-testing] unsupported input type " + clsStr[i]);
			}
		}
	}
	
}
