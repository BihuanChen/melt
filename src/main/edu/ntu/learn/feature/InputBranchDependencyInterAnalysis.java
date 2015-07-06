package edu.ntu.learn.feature;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import soot.NullType;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;
import soot.toolkits.scalar.Pair;

public class InputBranchDependencyInterAnalysis extends DefaultJimpleIFDSTabulationProblem<Pair<Value, Set<Value>>, InterproceduralCFG<Unit,SootMethod>> {

	public InputBranchDependencyInterAnalysis(InterproceduralCFG<Unit, SootMethod> icfg) {
		super(icfg);
	}

	@Override
	protected FlowFunctions<Unit, Pair<Value, Set<Value>>, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Pair<Value, Set<Value>>, SootMethod>() {

			@Override
			public FlowFunction<Pair<Value, Set<Value>>> getNormalFlowFunction(Unit curr, Unit succ) {
				if (curr instanceof IdentityStmt && interproceduralCFG().getMethodOf(curr) == Scene.v().getMainMethod()) {
					IdentityStmt stmt = (IdentityStmt) curr;
					Value lo = stmt.getLeftOp();
					Value ro = stmt.getRightOp();
					if (ro instanceof ParameterRef) {
						LinkedHashSet<Value> set = new LinkedHashSet<Value>();
						set.add(ro);
						return new Gen<Pair<Value,Set<Value>>>(new Pair<Value, Set<Value>>(lo, set), zeroValue());
					}
				}
				/*if (curr instanceof DefinitionStmt) {
					final DefinitionStmt stmt = (DefinitionStmt) curr;
					return new FlowFunction<Pair<Value,Set<Value>>>() {
						@Override
						public Set<Pair<Value, Set<Value>>> computeTargets(Pair<Value, Set<Value>> source) {
							if (source != zeroValue()) {
								if (source.getO1().equivTo(stmt.getLeftOp())) {
									return Collections.emptySet();
								}
								return Collections.singleton(source);
							} else {
								LinkedHashSet<Pair<Value, Set<Value>>> res = new LinkedHashSet<Pair<Value, Set<Value>>>();
								res.add(new Pair<Value, Set<Value>>(stmt.getLeftOp(), Collections.<Value> singleton(stmt.getRightOp())));
								return res;
							}
						}
					};
				}*/
				return Identity.v();
			}
			
			@Override
			public FlowFunction<Pair<Value, Set<Value>>> getCallFlowFunction(Unit arg0, SootMethod arg1) {
				return Identity.v();
			}

			@Override
			public FlowFunction<Pair<Value, Set<Value>>> getReturnFlowFunction(Unit arg0, SootMethod arg1, Unit arg2, Unit arg3) {
				return Identity.v();
			}
			
			@Override
			public FlowFunction<Pair<Value, Set<Value>>> getCallToReturnFlowFunction(Unit arg0, Unit arg1) {
				return Identity.v();
			}
			
		};
	}

	@Override
	public Map<Unit, Set<Pair<Value, Set<Value>>>> initialSeeds() {
		return DefaultSeeds.make(Collections.singleton(Scene.v().getMainMethod().getActiveBody().getUnits().getFirst()), zeroValue());
	}
	
	@Override
	protected Pair<Value, Set<Value>> createZeroValue() {
		return new Pair<Value, Set<Value>>(new JimpleLocal("<<zero>>", NullType.v()), Collections.<Value>emptySet());
	}

}
