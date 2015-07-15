package edu.ntu.feature.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import heros.DefaultSeeds;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;
import heros.flowfunc.KillAll;
import soot.EquivalentValue;
import soot.Local;
import soot.NullType;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;
import soot.toolkits.scalar.Pair;

/**
 * identify the set of input parameters that each variable (and branch) depends on
 * @author huan
 *
 */
public class InputBranchDependencyInterAnalysis extends DefaultJimpleIFDSTabulationProblem<Pair<Value, Value>, InterproceduralCFG<Unit,SootMethod>> {

	private String entryPoint;
	
	private LinkedHashMap<String, HashSet<Integer>> branchesToInputsDependency;
	
	public InputBranchDependencyInterAnalysis(InterproceduralCFG<Unit, SootMethod> icfg, String entryPoint) {
		super(icfg);
		this.entryPoint = entryPoint;
		this.branchesToInputsDependency = new LinkedHashMap<String, HashSet<Integer>>();
	}

	@Override
	protected FlowFunctions<Unit, Pair<Value,Value>, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Pair<Value, Value>, SootMethod>() {

			@Override
			public FlowFunction<Pair<Value, Value>> getNormalFlowFunction(final Unit curr, Unit succ) {
				if ((!(curr instanceof DefinitionStmt) && !(curr instanceof IfStmt)) || interproceduralCFG().getMethodOf(curr) == Scene.v().getMainMethod()) {
					return Identity.v();
				}
				if (curr instanceof IfStmt) {
					final IfStmt ifStmt = (IfStmt) curr;
					final String id = interproceduralCFG().getMethodOf(ifStmt).getDeclaringClass().toString() + " " + ifStmt.getJavaSourceStartLineNumber();
					return new FlowFunction<Pair<Value,Value>>() {
						@Override
						public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
							Iterator<ValueBox> i = ifStmt.getCondition().getUseBoxes().iterator();
							while (i.hasNext()) {
								Value v = i.next().getValue();
								if (source.getO1().equivTo(v)) {
									if (branchesToInputsDependency.get(id) == null) {
										branchesToInputsDependency.put(id, new HashSet<Integer>());
									}
									branchesToInputsDependency.get(id).add(((ParameterRef)source.getO2()).getIndex());
								}
							}
							return Collections.singleton(source);
						}
					};
				}
				
				final DefinitionStmt stmt = (DefinitionStmt) curr;
				return new FlowFunction<Pair<Value, Value>>() {
					@Override
					public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
						Set<Pair<Value, Value>> results = new LinkedHashSet<Pair<Value, Value>>();
						// initialize facts with respect to parameters
						if (stmt instanceof IdentityStmt && interproceduralCFG().getMethodOf(stmt) == Scene.v().getMethod(entryPoint)) {
							IdentityStmt idStmt = (IdentityStmt) stmt;
							Value lo = idStmt.getLeftOp();
							Value ro = idStmt.getRightOp();
							if (ro instanceof ParameterRef) {
								results.add(new Pair<Value, Value>(lo, ro));
								if (source != zeroValue()) {
									results.add(source);
								}
								return results;
							}
						}
						// flow facts
						if (source != zeroValue()) {
							// compute a new fact if exists
							Value lo = stmt.getLeftOp();
							Iterator<ValueBox> i = stmt.getUseBoxes().iterator();
							while (i.hasNext()) {
								Value ro = i.next().getValue();
								if (source.getO1().equivTo(ro)) {
									results.add(new Pair<Value, Value>(lo, source.getO2()));
									break;
								}
							}
							// deal with the source fact
							if (!source.getO1().equivTo(lo) && !(stmt instanceof IdentityStmt && source.getO1().equivTo(stmt.getRightOp()))) {
								results.add(source);
							}
						}
						return results;
					}
				};
			}
			
			@Override
			public FlowFunction<Pair<Value, Value>> getCallFlowFunction(Unit callStmt, final SootMethod destinationMethod) {
				List<Value> args = ((Stmt) callStmt).getInvokeExpr().getArgs();
				final List<Local> localArgs = new ArrayList<Local>(args.size());
				for (Value value : args) {
					if (value instanceof Local) {
						localArgs.add((Local) value);
					} else {
						localArgs.add(null);
					}
				}
				return new FlowFunction<Pair<Value, Value>>() {
					@Override
					public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
						if (!destinationMethod.getName().equals("<clinit>") && !destinationMethod.getSubSignature().equals("void run()")) {
							if(localArgs.contains(source.getO1())) {
								int paramIndex = localArgs.indexOf(source.getO1());
								Pair<Value, Value> pair = new Pair<Value, Value>(new EquivalentValue(Jimple.v().newParameterRef(destinationMethod.getParameterType(paramIndex), paramIndex)), source.getO2());
								return Collections.singleton(pair);
							}
						}
						return Collections.emptySet();
					}
				};				
			}

			@Override
			public FlowFunction<Pair<Value, Value>> getReturnFlowFunction(final Unit callSite, SootMethod calleeMethod, final Unit exitStmt, Unit returnSite) {
				if (!(callSite instanceof DefinitionStmt) || exitStmt instanceof ReturnVoidStmt) {
					return KillAll.v();
				}
				
				return new FlowFunction<Pair<Value, Value>>() {
					@Override
					public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
						if(exitStmt instanceof ReturnStmt) {
							ReturnStmt returnStmt = (ReturnStmt) exitStmt;
							if (returnStmt.getOp().equivTo(source.getO1())) {
								DefinitionStmt definitionStmt = (DefinitionStmt) callSite;
								Pair<Value, Value> pair = new Pair<Value, Value>(definitionStmt.getLeftOp(), source.getO2());
								return Collections.singleton(pair);
							}
						}
						return Collections.emptySet();
					}
				};
			}
			
			@Override
			public FlowFunction<Pair<Value, Value>> getCallToReturnFlowFunction(Unit callSite, Unit returnSite) {
				if (!(callSite instanceof DefinitionStmt)) {
					return Identity.v();
				}
				
				final DefinitionStmt definitionStmt = (DefinitionStmt) callSite;
				return new FlowFunction<Pair<Value, Value>>() {
					@Override
					public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
						if(source.getO1().equivTo(definitionStmt.getLeftOp())) {
							return Collections.emptySet();
						} else {
							return Collections.singleton(source);
						}
					}
				};
			}
			
		};
	}

	@Override
	public Map<Unit, Set<Pair<Value, Value>>> initialSeeds() {
		return DefaultSeeds.make(Collections.singleton(Scene.v().getMainMethod().getActiveBody().getUnits().getFirst()), zeroValue());
	}
	
	@Override
	protected Pair<Value, Value> createZeroValue() {
		return new Pair<Value, Value>(new JimpleLocal("<<zero>>", NullType.v()), new JimpleLocal("<<zero>>", NullType.v()));
	}

	public String getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	public LinkedHashMap<String, HashSet<Integer>> getBranchesToInputsDependency() {
		return branchesToInputsDependency;
	}

}
