package mlt.concolic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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

public class ConcolicExecution {

	private final String CONFIG_KEY_CONCOLIC_EXPLORER = "jdart.concolic_explorer_instance";
	private final ConcolicConfig cc;

	private Config jpfConf;
	private ConcolicExplorer ce;
	
	private JPFLogger logger;

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

	public void run() {
		if (ce.hasCurrentAnalysis()) {
			// FIXME ce.completedAnalyses have previous results
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
	
	public ArrayList<Valuation> getValuations(String srcLoc) {
		return ce.getCurrentAnalysis().getInternalConstraintsTree().findValuations(srcLoc);
	}
	
	public HashMap<String, HashSet<Expression<Boolean>>> getBranchConstraints() {
		return ce.getCurrentAnalysis().getInternalConstraintsTree().getBranchConstraints();
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

	public static void main(String[] args) {
		ConcolicExecution jdart = new ConcolicExecution("C:/Users/bhchen/workspace/testing/jdart/src/examples/features/nested/test_bar.jpf");
		jdart.run();
		ArrayList<Valuation> vals = jdart.getValuations("features.nested.Input.foo(Input.java:23)");
		System.out.println(vals);
		System.out.println(jdart.getBranchConstraints());
		jdart.statistics();
				
		jdart.run();
		vals = jdart.getValuations("features.nested.Input.foo(Input.java:25)");
		System.out.println(vals);
		System.out.println(jdart.getBranchConstraints());
		jdart.statistics();
		
		mlt.Config.CLS = new Class[]{double.class};
		mlt.Config.PARAMETERS = new String[]{"d"};
		Object[] test1 = new Object[1];
		test1[0] = 2.5;
		System.out.println(mlt.test.Util.toValuation(test1));
		
		//Object[] test2 = mlt.test.Util.toTest(vals.get(0));
		//for (int i = 0; i < test2.length; i++) {
		//	System.out.println(test2[i].getClass() + " " + test2[i]);
		//}
	}

}
