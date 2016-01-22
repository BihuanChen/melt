package mlt;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class Config {

	public enum Mode {RANDOM, SYSTEMATIC} 
	public enum Model {J48, NAIVEBAYES, LIBSVM, RANDOMFOREST}

	// default configuration parameters of ml-testing
	public static Mode MODE = Mode.SYSTEMATIC; 		// find an unexplored branch either randomly or systematically
	public static int MAX_ATTEMPTS = 3; 			// the maximum number of attempts to cover an unexplored branch
	public static Model MODEL = Model.RANDOMFOREST; // the applied classification model
	public static int TESTS_SIZE = 10; 				// the number of test cases that need to be generated at a time

	// configuration information of the target project for instrumentation
	public static String SOURCEPATH = null;
	
	// configuration information for taint analysis
	public static String TAINT = "dynamic";
	
	// configuration information of the target project for static taint analysis
	public static String CLASSPATH = null;
	public static String MAINCLASS = null;
	public static String ENTRYMETHOD = null;
	
	// filter package name for taint analysis
	public static String FILTER = null;
	
	// configuration file for concolic execution
	public static String JPFCONFIG = null;
	
	// reflection information for running tests
	public static String METHOD = null;
	@SuppressWarnings("rawtypes")
	public static Class[] CLS = null;
	public static String[] PARAMETERS = null;
	
	// lower and upper bounds on integer and real variables
	public static byte MIN_BYTE = -100;
	public static byte MAX_BYTE = 100;
	public static short MIN_SHORT = -100;
	public static short MAX_SHORT = 100;
	public static int MIN_INT = -100;
	public static int MAX_INT = 100;
	public static long MIN_LONG = -100;
	public static long MAX_LONG = 100;
	public static float MIN_FLOAT = -8;
	public static float MAX_FLOAT = 7;
	public static double MIN_DOUBLE = -8;
	public static double MAX_DOUBLE = 7;
	
	public static SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static void loadProperties(String file) throws IOException {
		System.out.println("[ml-testing] " + FORMAT.format(System.currentTimeMillis()));
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
		
		FILTER = MAINCLASS.substring(0, MAINCLASS.indexOf("."));
		System.out.println("[ml-tesitng] filter = " + FILTER);
		
		ENTRYMETHOD = prop.getProperty("entrymethod");
		if (ENTRYMETHOD == null) {
			System.err.println("[ml-testing] configuration error: entry method not set");
			System.exit(0);
		}
		System.out.println("[ml-testing] entrymethod = " + ENTRYMETHOD);
		
		JPFCONFIG = prop.getProperty("jpfconfig");
		if (JPFCONFIG == null) {
			System.err.println("[ml-testing] configuration error: jpf configuration file not set");
			System.exit(0);
		}
		System.out.println("[ml-testing] jpfconfig = " + JPFCONFIG);
		
		/*String p = prop.getProperty("exploration.mode");
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
		System.out.println("[ml-testing] exploration.mode = " + MODE.toString().toLowerCase());*/
				
		String p = prop.getProperty("classification.model");
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
		System.out.println("[ml-testing] tests.size = " + TESTS_SIZE);
		
		p = prop.getProperty("min.byte");
		if (p != null) {
			MIN_BYTE = Byte.valueOf(p);
		}
		p = prop.getProperty("max.byte");
		if (p != null) {
			MAX_BYTE = Byte.valueOf(p);
		}
		if (MAX_BYTE < MIN_BYTE) {
			System.err.println("[ml-testing] illegal lower and upper bounds on byte variables");
			System.exit(0);
		}
		System.out.println("[ml-testing] min.byte = " + MIN_BYTE);
		System.out.println("[ml-testing] max.byte = " + MAX_BYTE);
		
		p = prop.getProperty("min.short");
		if (p != null) {
			MIN_SHORT = Short.valueOf(p);
		}
		p = prop.getProperty("max.short");
		if (p != null) {
			MAX_SHORT = Short.valueOf(p);
		}
		if (MAX_SHORT < MIN_SHORT) {
			System.err.println("[ml-testing] illegal lower and upper bounds on short variables");
			System.exit(0);
		}
		System.out.println("[ml-testing] min.short = " + MIN_SHORT);
		System.out.println("[ml-testing] max.short = " + MAX_SHORT);
		
		p = prop.getProperty("min.int");
		if (p != null) {
			MIN_INT = Integer.valueOf(p);
		}
		p = prop.getProperty("max.int");
		if (p != null) {
			MAX_INT = Integer.valueOf(p);
		}
		if (MAX_INT < MIN_INT) {
			System.err.println("[ml-testing] illegal lower and upper bounds on integer variables");
			System.exit(0);
		}
		System.out.println("[ml-testing] min.int = " + MIN_INT);
		System.out.println("[ml-testing] max.int = " + MAX_INT);
		
		p = prop.getProperty("min.long");
		if (p != null) {
			MIN_LONG = Long.valueOf(p);
		}
		p = prop.getProperty("max.long");
		if (p != null) {
			MAX_LONG = Long.valueOf(p);
		}
		if (MAX_LONG < MIN_LONG) {
			System.err.println("[ml-testing] illegal lower and upper bounds on long variables");
			System.exit(0);
		}
		System.out.println("[ml-testing] min.long = " + MIN_LONG);
		System.out.println("[ml-testing] max.long = " + MAX_LONG);
		
		p = prop.getProperty("min.float");
		if (p != null) {
			MIN_FLOAT = Float.valueOf(p);
		}
		p = prop.getProperty("max.float");
		if (p != null) {
			MAX_FLOAT = Float.valueOf(p);
		}
		if (MAX_FLOAT < MIN_FLOAT) {
			System.err.println("[ml-testing] illegal lower and upper bounds on float variables");
			System.exit(0);
		}
		System.out.println("[ml-testing] min.float = " + MIN_FLOAT);
		System.out.println("[ml-testing] max.float = " + MAX_FLOAT);
		
		p = prop.getProperty("min.double");
		if (p != null) {
			MIN_DOUBLE = Double.valueOf(p);
		}
		p = prop.getProperty("max.double");
		if (p != null) {
			MAX_DOUBLE = Double.valueOf(p);
		}
		if (MAX_DOUBLE < MIN_DOUBLE) {
			System.err.println("[ml-testing] illegal lower and upper bounds on double variables");
			System.exit(0);
		}
		System.out.println("[ml-testing] min.double = " + MIN_DOUBLE);
		System.out.println("[ml-testing] max.double = " + MAX_DOUBLE + "\n");

		fis.close();
		
		// derive the reflection information to run test cases
		String returnType = Config.ENTRYMETHOD.substring(0, Config.ENTRYMETHOD.indexOf(" "));
		METHOD = Config.ENTRYMETHOD.substring(Config.ENTRYMETHOD.indexOf(" ") + 1, Config.ENTRYMETHOD.indexOf("("));
		String[] clsStr = Config.ENTRYMETHOD.substring(Config.ENTRYMETHOD.indexOf("(") + 1, Config.ENTRYMETHOD.indexOf(")")).split(",");
		int size = clsStr.length;
		CLS = new Class[size];
		PARAMETERS = new String[size];
		ENTRYMETHOD = returnType + " " + METHOD + "(";
		for (int i = 0; i < size; i++) {
			String[] pair = clsStr[i].split(":");
			if (pair[1].equals("byte")) {
				CLS[i] = byte.class;
			} else if (pair[1].equals("short")) {
				CLS[i] = short.class;
			} else if (pair[1].equals("int")) {
				CLS[i] = int.class;
			} else if (pair[1].equals("long")) {
				CLS[i] = long.class;
			} else if (pair[1].equals("boolean")) {
				CLS[i] = boolean.class;
			} else if (pair[1].equals("float")) {
				CLS[i] = float.class;
			} else if (pair[1].equals("double")) {
				CLS[i] = double.class;
			} else {
				System.err.println("[ml-testing] unsupported input type " + clsStr[i]);
			}
			PARAMETERS[i] = pair[0];
			ENTRYMETHOD += pair[1] + ",";
		}
		ENTRYMETHOD = ENTRYMETHOD.substring(0, ENTRYMETHOD.length() - 1) + ")";
	}
	
}
