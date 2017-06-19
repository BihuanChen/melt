package melt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

public class Config {

	public enum Model {J48, NAIVEBAYES, LIBSVM, RANDOMFOREST}

	// enable concolic execution or not
	public static boolean CE_ENABLED = true;
	public static boolean DT_ENABLED = true;
	
	// default configuration parameters of melt
	public static int MAX_ATTEMPTS = 3; 			// the maximum number of attempts to cover an unexplored branch
	public static Model MODEL = Model.RANDOMFOREST; // the applied classification model
	public static int LEARN_THRESHOLD = 10;			// the threshold for re-learn the model
	public static int TESTS_SIZE = 10; 				// the number of test cases that need to be generated at a time

	// configuration information of the target project for instrumentation
	public static String SOURCEPATH = null;
	
	// configuration information of the target project for static taint analysis
	public static String[] CLASSPATH = null;
	public static String MAINCLASS = null;
	public static String ENTRYMETHOD = null;
	
	// configuration file for concolic executionrue
	public static String JPFCONFIG = null;
	// skip branches that may have extremely long symbolic constraints
	public static HashSet<String> SKIPPED_BRANCH = null;
	
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
	public static char MIN_CHAR = 0;
	public static char MAX_CHAR = 65535;
	public static int MIN_INT = -100;
	public static int MAX_INT = 100;
	public static long MIN_LONG = -100;
	public static long MAX_LONG = 100;
	public static float MIN_FLOAT = -8;
	public static float MAX_FLOAT = 7;
	public static double MIN_DOUBLE = -8;
	public static double MAX_DOUBLE = 7;
	
	//TODO support other primitive types for variables' minimum and maximum
	public static Map<String, Integer> varMinIntMap;
	public static Map<String, Integer> varMaxIntMap;
	
	public static SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static void loadProperties(String file) throws IOException {
		System.out.println("[melt] " + FORMAT.format(System.currentTimeMillis()));
		// load configuration
		Properties prop = new Properties();
		FileInputStream fis = new FileInputStream(file);
		prop.load(fis);

		SOURCEPATH = prop.getProperty("sourcepath");
		if (SOURCEPATH == null) {
			System.err.println("[melt] configuration error: source path not set");
			System.exit(0);
		}
		System.out.println("[melt] sourcepath = " + SOURCEPATH);
		
		String cps = prop.getProperty("classpath");
		if (cps == null) {
			System.err.println("[melt] configuration error: class path not set");
			System.exit(0);
		}
		CLASSPATH = cps.split(";");
		System.out.print("[melt] classepath = ");
		for (int i = 0; i < CLASSPATH.length; i++) {
			System.out.print(CLASSPATH[i] + " ");
		}
		System.out.println();
		
		MAINCLASS = prop.getProperty("mainclass");
		if (MAINCLASS == null) {
			System.err.println("[melt] configuration error: main class not set");
			System.exit(0);
		}
		System.out.println("[melt] mainclass = " + MAINCLASS);
		
		ENTRYMETHOD = prop.getProperty("entrymethod");
		if (ENTRYMETHOD == null) {
			System.err.println("[melt] configuration error: entry method not set");
			System.exit(0);
		}
		System.out.println("[melt] entrymethod = " + ENTRYMETHOD);
		
		JPFCONFIG = prop.getProperty("jpfconfig");
		if (JPFCONFIG == null) {
			System.err.println("[melt] configuration error: jpf configuration file not set");
			System.exit(0);
		}
		System.out.println("[melt] jpfconfig = " + JPFCONFIG);
		
		String sb = prop.getProperty("branch.skip");
		if (sb != null) {
			SKIPPED_BRANCH = new HashSet<String>();
			String[] ss = sb.split("/");
			for (int i = 0; i < ss.length; i++) {
				SKIPPED_BRANCH.add(ss[i]);
			}
		}
				
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
				System.err.println("[melt] configuration error: incorrect classification model (j48, naivebayes, libsvm, or randomforest)");
				System.exit(0);
			}
		}
		System.out.println("[melt] classification.model = " + MODEL.toString().toLowerCase());

		p = prop.getProperty("max.attempts");
		if (p != null) {
			MAX_ATTEMPTS = Integer.valueOf(p);
		}
		System.out.println("[melt] max.attempts = " + MAX_ATTEMPTS);
		
		p = prop.getProperty("learn.threshold");
		if (p != null) {
			LEARN_THRESHOLD = Integer.valueOf(p);
		}
		System.out.println("[melt] learning.threshold = " + LEARN_THRESHOLD);
		
		p = prop.getProperty("tests.size");
		if (p != null) {
			TESTS_SIZE = Integer.valueOf(p);
		}
		System.out.println("[melt] tests.size = " + TESTS_SIZE);
		
		p = prop.getProperty("min.byte");
		if (p != null) {
			MIN_BYTE = Byte.valueOf(p);
		}
		p = prop.getProperty("max.byte");
		if (p != null) {
			MAX_BYTE = Byte.valueOf(p);
		}
		if (MAX_BYTE < MIN_BYTE) {
			System.err.println("[melt] illegal lower and upper bounds on byte variables");
			System.exit(0);
		}
		System.out.println("[melt] min.byte = " + MIN_BYTE);
		System.out.println("[melt] max.byte = " + MAX_BYTE);
		
		p = prop.getProperty("min.short");
		if (p != null) {
			MIN_SHORT = Short.valueOf(p);
		}
		p = prop.getProperty("max.short");
		if (p != null) {
			MAX_SHORT = Short.valueOf(p);
		}
		if (MAX_SHORT < MIN_SHORT) {
			System.err.println("[melt] illegal lower and upper bounds on short variables");
			System.exit(0);
		}
		System.out.println("[melt] min.short = " + MIN_SHORT);
		System.out.println("[melt] max.short = " + MAX_SHORT);
		
		p = prop.getProperty("min.int");
		if (p != null) {
			MIN_INT = Integer.valueOf(p);
		}
		p = prop.getProperty("max.int");
		if (p != null) {
			MAX_INT = Integer.valueOf(p);
		}
		if (MAX_INT < MIN_INT) {
			System.err.println("[melt] illegal lower and upper bounds on integer variables");
			System.exit(0);
		}
		System.out.println("[melt] min.int = " + MIN_INT);
		System.out.println("[melt] max.int = " + MAX_INT);
		
		varMinIntMap = new HashMap<String, Integer>();
		varMaxIntMap = new HashMap<String, Integer>();
		
		for (Enumeration<?> e = prop.keys(); e.hasMoreElements(); ) {
			String k = e.nextElement().toString();
			if (k.startsWith("min.int_")) {
				String name = k.substring(8);
				varMinIntMap.put(name, Integer.valueOf(prop.getProperty(k)));
			}
			if (k.startsWith("max.int_")) {
				String name = k.substring(8);
				varMaxIntMap.put(name, Integer.valueOf(prop.getProperty(k)));
			}
		}
		System.out.println("[melt] min.int_ = " + varMinIntMap);
		System.out.println("[melt] max.int_ = " + varMaxIntMap);
		
		p = prop.getProperty("min.long");
		if (p != null) {
			MIN_LONG = Long.valueOf(p);
		}
		p = prop.getProperty("max.long");
		if (p != null) {
			MAX_LONG = Long.valueOf(p);
		}
		if (MAX_LONG < MIN_LONG) {
			System.err.println("[melt] illegal lower and upper bounds on long variables");
			System.exit(0);
		}
		System.out.println("[melt] min.long = " + MIN_LONG);
		System.out.println("[melt] max.long = " + MAX_LONG);
		
		p = prop.getProperty("min.float");
		if (p != null) {
			MIN_FLOAT = Float.valueOf(p);
		}
		p = prop.getProperty("max.float");
		if (p != null) {
			MAX_FLOAT = Float.valueOf(p);
		}
		if (MAX_FLOAT < MIN_FLOAT) {
			System.err.println("[melt] illegal lower and upper bounds on float variables");
			System.exit(0);
		}
		System.out.println("[melt] min.float = " + MIN_FLOAT);
		System.out.println("[melt] max.float = " + MAX_FLOAT);
		
		p = prop.getProperty("min.double");
		if (p != null) {
			MIN_DOUBLE = Double.valueOf(p);
		}
		p = prop.getProperty("max.double");
		if (p != null) {
			MAX_DOUBLE = Double.valueOf(p);
		}
		if (MAX_DOUBLE < MIN_DOUBLE) {
			System.err.println("[melt] illegal lower and upper bounds on double variables");
			System.exit(0);
		}
		System.out.println("[melt] min.double = " + MIN_DOUBLE);
		System.out.println("[melt] max.double = " + MAX_DOUBLE + "\n");

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
			} else if (pair[1].equals("char")) {
				CLS[i] = char.class;
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
				System.err.println("[melt] unsupported input type " + clsStr[i]);
			}
			PARAMETERS[i] = pair[0];
			ENTRYMETHOD += pair[1] + ",";
		}
		ENTRYMETHOD = ENTRYMETHOD.substring(0, ENTRYMETHOD.length() - 1) + ")";
		
		// initialize the taint server with the information about the target program
		initTaintServer();
	}
	
	public static void initTaintServer() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(System.getProperty("java.io.tmpdir") + "/taint-test")));
		writer.write(Config.MAINCLASS + "\n");
		writer.write(Config.METHOD + "\n");
		for (int i = 0; i < Config.CLS.length; i++) {
			writer.write(Config.CLS[i].toString() + " ");
		}
		writer.write("\n");
		writer.flush();
		writer.close();
		File file = new File(System.getProperty("java.io.tmpdir") + "/taint-test-indicator");
		file.createNewFile();
	}
	
}
