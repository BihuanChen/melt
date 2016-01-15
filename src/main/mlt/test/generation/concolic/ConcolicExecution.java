package mlt.test.generation.concolic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.jdart.CompletedAnalysis;
import gov.nasa.jpf.jdart.ConcolicExplorer;
import gov.nasa.jpf.jdart.ConcolicInstructionFactory;
import gov.nasa.jpf.jdart.ConcolicListener;
import gov.nasa.jpf.jdart.ConcolicPerturbator;
import gov.nasa.jpf.jdart.config.ConcolicConfig;
import gov.nasa.jpf.jdart.config.ConcolicMethodConfig;
import gov.nasa.jpf.jdart.constraints.Path;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.LogManager;
import gov.nasa.jpf.util.SimpleProfiler;
import gov.nasa.jpf.vm.Instruction;

public class ConcolicExecution {

	private final String CONFIG_KEY_CONCOLIC_EXPLORER = "jdart.concolic_explorer_instance";
	private final ConcolicConfig cc;

	private Config jpfConf;
	private ConcolicExplorer ce;
	
	private JPFLogger logger;

	private static ConcolicExecution instance;
	
	private ConcolicExecution(String prop) {
		Config conf = JPF.createConfig(new String[]{prop});
		conf.initClassLoader(ConcolicExecution.class.getClassLoader());
	    // due to some bug the log manager has to be initialized first.
		LogManager.init(conf);
		this.cc = new ConcolicConfig(conf);
	    this.logger = JPF.getLogger("jdart");
	    // configure JPF
 		jpfConf = cc.generateJPFConfig(conf);
 	    jpfConf.remove("shell");
 	    jpfConf.setProperty("jvm.insn_factory.class", ConcolicInstructionFactory.class.getName());
 	    jpfConf.prepend("peer_packages", "gov.nasa.jpf.jdart.peers", ";");
 	    String listener = ConcolicListener.class.getName();
 	    if(jpfConf.hasValue("listener"))
 	    	listener += ";" + jpfConf.getString("listener");
 	    jpfConf.setProperty("listener", listener);
 	    jpfConf.setProperty("perturb.class", ConcolicPerturbator.class.getName());
 	    jpfConf.setProperty("search.multiple_errors", "true");
 	    // configure jdart
 	    jpfConf.setProperty(CONFIG_KEY_CONCOLIC_EXPLORER, ConcolicExplorer.class.getName() + "@jdart-explorer");
 	    ce = jpfConf.getEssentialInstance(CONFIG_KEY_CONCOLIC_EXPLORER, ConcolicExplorer.class);
 	    ce.configure(cc);
	}
	
	public static ConcolicExecution getInstance(String prop) {
		if (instance == null) {
			instance = new ConcolicExecution(prop);
		}
		return instance;
	}
	
	public void run(Object[] test) throws NotFoundException, CannotCompileException, IOException {
		this.prepare(test);
		if (ce.hasCurrentAnalysis()) {
			// TODO possibility to remove previous stored results in ce.completedAnalyses to save space?
	    	ce.completeAnalysis();
	    }
	    // run jpf
	    JPF jpf = new JPF(jpfConf);
	    SimpleProfiler.start("JDART-run");
	    SimpleProfiler.start("JPF-boot"); // is stopped upon searchStarted in ConcolicListener
	    jpf.run();
	    SimpleProfiler.stop("JDART-run");
	    // post process 
	    logger.info("Profiling:\n" + SimpleProfiler.getResults());
	}
	
	private void prepare(final Object[] test) throws NotFoundException, CannotCompileException, IOException {
		String mainClass = jpfConf.getString("target");
		final String methodName = jpfConf.getString("concolic.method");
		ClassPool cp = ClassPool.getDefault();
		cp.insertClassPath(jpfConf.getString("classpath").split(";")[0]);
		CtClass cc = cp.get(mainClass);
		if (cc.isFrozen()) {
			cc.defrost();
		}
		CtMethod cm = cc.getDeclaredMethod("main");
		cm.instrument(new ExprEditor(){

			@Override
			public void edit(MethodCall m) throws CannotCompileException {
				if (m.getMethodName().equals(methodName)) {
					String args = test[0].toString();
					for (int i = 1; i < test.length; i++) {
						args += ", " + test[i].toString();
					}
					m.replace("$_ = $0." + methodName + "(" + args + ");");
				}
			}
			
		});
		cc.writeFile(jpfConf.getString("classpath").split(";")[0]);
	}
	
	public ArrayList<Valuation> getValuations(String srcLoc, int size, HashMap<Instruction, Expression<Boolean>> cons) {
		return ce.getCurrentAnalysis().getInternalConstraintsTree().findValuations(srcLoc, size, cons);
	}
	
	public void statistics() {
	    if (ce.hasCurrentAnalysis()) {
	    	ce.completeAnalysis();
	    }
	    for (Map.Entry<String, List<CompletedAnalysis>> e : ce.getCompletedAnalyses().entrySet()) {
	    	String id = e.getKey();
	    	ConcolicMethodConfig mc = cc.getMethodConfig(id);
	    	logger.info();
	    	logger.info("Analyses for method ", mc);
	    	for (CompletedAnalysis ca : e.getValue()) {
	    		if (ca.getConstraintsTree() == null) {
	    			logger.info("tree is null");
	    			continue;
	    		}       
	    		logger.info("Initial valuation: ", ca.getInitialValuation());
	    		logger.info(ca.getConstraintsTree().toString(false, true));      
	    		logger.info("----Constraints Tree Statistics---");
	    		logger.info("# paths (total): " + ca.getConstraintsTree().getAllPaths().size());
	    		logger.info("# OK paths: " + ca.getConstraintsTree().getCoveredPaths().size());
	    		logger.info("# ERROR paths: " + ca.getConstraintsTree().getErrorPaths().size());
	    		logger.info("# DONT_KNOW paths: " + ca.getConstraintsTree().getDontKnowPaths().size());
	    		logger.info("");
	    		logger.info("-------Valuation Statistics-------");
	    		logger.info("# of valuations (OK+ERR): " + (ca.getConstraintsTree().getCoveredPaths().size() + ca.getConstraintsTree().getErrorPaths().size()));
	    		logger.info("");
	    		for (Path p : ca.getConstraintsTree().getAllPaths()) {
	    			if (p.getValuation() == null) {
	    				// dont know cases
	    				continue;
	    			}
	    			String out = "";
	    			for (Variable<?> v : p.getValuation().getVariables()) {
	    				out += v.getResultType().getName() + ":" + v.getName() + "=" + p.getValuation().getValue(v) + ", ";
	    			}
	    			logger.info(out);
	    		}
	    	}
	    }
	}

	public static void main(String[] args) throws NotFoundException, CannotCompileException, IOException {
		/*ConcolicExecution jdart = ConcolicExecution.getInstance("C:/Users/bhchen/workspace/testing/format/src/features/nested/test_bar.jpf");
		Object[] obj = new Object[1];
		obj[0] = 1.733;
		jdart.run(obj);
		HashMap<Instruction, Expression<Boolean>> cons = new HashMap<Instruction, Expression<Boolean>>();
		ArrayList<Valuation> vals = jdart.getValuations("features.nested.Input.foo(Input.java:23)", mlt.Config.TESTS_SIZE, cons);
		System.out.println(vals);
		System.out.println(cons);
		//jdart.statistics();

		obj[0] = 3.2;
		jdart.run(obj);
		cons = new HashMap<Instruction, Expression<Boolean>>();
		vals = jdart.getValuations("features.nested.Input.bar(Input.java:48)", mlt.Config.TESTS_SIZE, cons);
		System.out.println(vals);
		System.out.println(cons);
		//jdart.statistics();*/
		
		/*ConcolicExecution jdart = ConcolicExecution.getInstance("/home/bhchen/workspace/testing/benchmark1-art-ce/src/dt/original/Bessj.jpf");
		Object[] obj = new Object[2];
		obj[0] = 7975;
		obj[1] = -5814.517874260192;
		jdart.run(obj);
		HashMap<Instruction, Expression<Boolean>> cons = new HashMap<Instruction, Expression<Boolean>>();
		ArrayList<Valuation> vals = jdart.getValuations("dt.original.Bessj.bessj(Bessj.java:27)", mlt.Config.TESTS_SIZE, cons);
		System.out.println(vals);
		System.out.println(cons);*/
		//jdart.statistics();
		
		ConcolicExecution jdart = ConcolicExecution.getInstance("/home/bhchen/workspace/testing/phosphor-test/src/phosphor/test/Test1.jpf");
		Object[] obj = new Object[2];
		obj[0] = 9098;
		obj[1] = -913;
		jdart.run(obj);
		HashMap<Instruction, Expression<Boolean>> cons = new HashMap<Instruction, Expression<Boolean>>();
		ArrayList<Valuation> vals = jdart.getValuations("phosphor.test.Test1.test(Test1.java:10)", mlt.Config.TESTS_SIZE, cons);
		System.out.println(vals);
		System.out.println(cons);
		jdart.statistics();
	}

}
