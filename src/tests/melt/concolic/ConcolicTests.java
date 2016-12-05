package melt.concolic;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.api.Valuation;
import gov.nasa.jpf.vm.Instruction;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import melt.test.generation.concolic.ConcolicExecution;

public class ConcolicTests {

	public static void test1() throws NotFoundException, CannotCompileException, IOException, BadBytecode {
		ConcolicExecution jdart = ConcolicExecution.getInstance("/home/bhchen/workspace/testing/jdart/src/examples/features/nested/test_bar.jpf");
		Object[] obj = new Object[]{1.733};
		jdart.run(obj);
		HashMap<Instruction, Expression<Boolean>> cons = new HashMap<Instruction, Expression<Boolean>>();
		HashSet<Valuation> vals = jdart.getValuations("features.nested.Input.foo(Input.java:33)", melt.Config.TESTS_SIZE, cons);
		System.out.println(vals);
		System.out.println(cons);
		jdart.statistics();
	}
	
	public static void test2() throws NotFoundException, CannotCompileException, IOException, BadBytecode {
		ConcolicExecution jdart = ConcolicExecution.getInstance("/home/bhchen/workspace/testing/benchmark1-art/src/dt/original/Bessj.jpf");
		Object[] obj = new Object[]{7975, -5814.517874260192};
		jdart.run(obj);
		HashMap<Instruction, Expression<Boolean>> cons = new HashMap<Instruction, Expression<Boolean>>();
		HashSet<Valuation> vals = jdart.getValuations("dt.original.Bessj.bessj(Bessj.java:25)", melt.Config.TESTS_SIZE, cons);
		System.out.println(vals);
		System.out.println(cons);
		jdart.statistics();
	}
	
	public static void test3() throws NotFoundException, CannotCompileException, IOException, BadBytecode {
		melt.Config.loadProperties("/home/bhchen/workspace/testing/benchmark2-jpf/src/tsafe/TSAFE.melt");
		ConcolicExecution jdart = ConcolicExecution.getInstance("/home/bhchen/workspace/testing/benchmark2-jpf/src/tsafe/TSAFE.jpf");
		Object[] obj = new Object[]{87.8166690171517, -53.703386742825224, 67.90922066561362, 53.856785468275916};
		jdart.run(obj);
		HashMap<Instruction, Expression<Boolean>> cons = new HashMap<Instruction, Expression<Boolean>>();
		//HashSet<Valuation> vals = jdart.getValuations("tsafe.Calculator.angleXY(Calculator.java:109)", melt.Config.TESTS_SIZE, cons);
		//System.out.println(vals);
		System.out.println(cons);
		jdart.statistics();
	}
	
	public static void test4() throws NotFoundException, CannotCompileException, IOException, BadBytecode {
		melt.Config.loadProperties("/home/bhchen/workspace/testing/benchmark4-siemens/src/schedule/Schedule.melt");
		ConcolicExecution jdart = ConcolicExecution.getInstance("/home/bhchen/workspace/testing/benchmark4-siemens/src/schedule/Schedule.jpf");
		Object[] obj = new Object[]{26996336, -99741853, 93093590, 20404571};
		jdart.run(obj);
		HashMap<Instruction, Expression<Boolean>> cons = new HashMap<Instruction, Expression<Boolean>>();
		HashSet<Valuation> vals = jdart.getValuations("schedule.Schedule.mainMethod(Schedule.java:76)", 1, cons);
		System.out.println(vals);
		System.out.println(cons);
		jdart.statistics();
	}
	
	public static void main(String[] args) throws NotFoundException, CannotCompileException, IOException, BadBytecode {
		ConcolicTests.test4();
	}

}
