package melt.test.generation.concolic;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import melt.test.util.Util;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
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
import gov.nasa.jpf.jdart.constraints.InternalConstraintsTree;
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
	
	private boolean firstLoad = true;
	
	public ConcolicExecution(String prop) {
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
	
	@SuppressWarnings("deprecation")
	public void run(Object[] test) throws NotFoundException, CannotCompileException, IOException, BadBytecode {
		this.prepare(test);
		if (ce.hasCurrentAnalysis()) {
	    	ce.completeAnalysis();
	    }
    	ce.getCompletedAnalyses().clear();
    	ce.getMethodExplorers().clear();
	    // run jpf
	    JPF jpf = new JPF(jpfConf);
	    SimpleProfiler.start("JDART-run");
	    SimpleProfiler.start("JPF-boot"); // is stopped upon searchStarted in ConcolicListener
	    jpf.run();
	    SimpleProfiler.stop("JDART-run");
	    // post process 
	    logger.info("Profiling:\n" + SimpleProfiler.getResults());
	}
	
	public void run() throws NotFoundException, CannotCompileException, IOException, BadBytecode {
		Object[] iniTest = Util.randomTest();
		System.out.print("[FINER] Found: SAT : " + melt.Config.PARAMETERS[0] + ":=" + iniTest[0]);
		for (int i = 1; i < melt.Config.PARAMETERS.length; i++) {
			System.out.print("," + melt.Config.PARAMETERS[i] + ":=" + iniTest[i]);
		}
		System.out.println();
		this.prepare(iniTest);
		// run jpf
	    JPF jpf = new JPF(jpfConf);
	    SimpleProfiler.start("JDART-run");
	    SimpleProfiler.start("JPF-boot"); // is stopped upon searchStarted in ConcolicListener
	    jpf.run();
	    SimpleProfiler.stop("JDART-run");
	    // post process 
	    logger.info("Profiling:\n" + SimpleProfiler.getResults());
	}
	
	private void prepare(final Object[] test) throws NotFoundException, CannotCompileException, IOException, BadBytecode {
		String mainClass = jpfConf.getString("target");
		final String methodName = jpfConf.getString("concolic.method");
		ClassPool cp = ClassPool.getDefault();
		if (firstLoad) {
			cp.insertClassPath(jpfConf.getString("classpath").split(";")[0]);
			firstLoad = false;
		}
		CtClass cc = cp.get(mainClass);
		if (cc.isFrozen()) {
			cc.defrost();
		}
		CtMethod cm = cc.getDeclaredMethod("main");
		/*cm.instrument(new ExprEditor(){

			@Override
			public void edit(MethodCall m) throws CannotCompileException {
				if (m.getMethodName().equals(methodName)) {
					String args = "";
					if (melt.Config.CLS[0] == float.class) {
						args = test[0].toString() + "f";
					} else if (melt.Config.CLS[0] == char.class) {
						args = "(char)" + (int)(test[0].toString().charAt(0));
					} else {
						args = test[0].toString();
					}
					for (int i = 1; i < test.length; i++) {
						if (melt.Config.CLS[i] == float.class) {
							args += ", " + test[i].toString() + "f";
						} else if (melt.Config.CLS[i] == char.class) {
							args += ", (char)" + (int)(test[i].toString().charAt(0));
						} else {
							args += ", " + test[i].toString();
						}
					}
					m.replace("$_ = $0." + methodName + "(" + args + ");");
				}
			}
			
		});*/
		String args = "";
		if (melt.Config.CLS[0] == float.class) {
			args = test[0].toString() + "f";
		} else if (melt.Config.CLS[0] == char.class) {
			args = "(char)" + (int)(test[0].toString().charAt(0));
		} else {
			args = test[0].toString();
		}
		for (int i = 1; i < test.length; i++) {
			if (melt.Config.CLS[i] == float.class) {
				args += ", " + test[i].toString() + "f";
			} else if (melt.Config.CLS[i] == char.class) {
				args += ", (char)" + (int)(test[i].toString().charAt(0));
			} else {
				args += ", " + test[i].toString();
			}
		}
		cm.setBody("new " + mainClass + "()." + methodName + "(" + args + ");");
		cc.writeFile(jpfConf.getString("classpath").split(";")[0]);
	}
	
	public HashSet<Valuation> getValuations(String srcLoc, int size, HashMap<Instruction, Expression<Boolean>> cons) {
		return ce.getCurrentAnalysis().getInternalConstraintsTree().findValuations(srcLoc, size, cons);
	}
	
	public HashSet<Valuation> getValuations() {
		if (ce.hasCurrentAnalysis()) {
	    	ce.completeAnalysis();
	    }
		Valuation iniVal = ce.getCompletedAnalyses().entrySet().iterator().next().getValue().get(0).getInitialValuation();
		InternalConstraintsTree.getValuations().add(iniVal);
		return InternalConstraintsTree.getValuations();
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

	public Config getJpfConf() {
		return jpfConf;
	}

}
