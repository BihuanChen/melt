package melt.test.generation.random;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import jmetal.util.PseudoRandom;
import melt.Config;
import melt.learn.PathLearner;
import melt.test.Profiles;
import melt.test.TestCase;
import melt.test.Util;
import melt.test.generation.TestGenerator;
import melt.test.generation.concolic.ConcolicTestGenerator;

public class AdaptiveRandomTestGenerator extends TestGenerator {
	
	private static String algorithm = "EAR";
	
	public AdaptiveRandomTestGenerator(PathLearner pathLearner) {
		super(pathLearner);
	}

	@Override
	public HashSet<TestCase> generate() throws Exception {
		if (pathLearner != null && pathLearner.getTarget().getAttempts() == Config.MAX_ATTEMPTS) {
			return new ConcolicTestGenerator(pathLearner).generate();
		} else {
			if (algorithm.equals("FSCS")) {
				return genAdaptiveRandomTestsFSCS();
			} else /*if (algorithm.equals("EAR"))*/ {
				return genAdaptiveRandomTestsEAR();
			}
		}
	}

	// might stuck when the constraints are too narrow
	private HashSet<TestCase> genAdaptiveRandomTestsFSCS() throws Exception {
		// the size of the candidate set
		int k = 10;
		HashSet<TestCase> testCases = new HashSet<TestCase>(Config.TESTS_SIZE);
		while (true) {
			// generate the k valid candidate tests
			ArrayList<TestCase> candidates = new ArrayList<TestCase>(k);
			double[] minDist = new double[k];
			for (int i = 0; i < k; ) {
				TestCase testCase = new TestCase(Util.randomTest());
				if (pathLearner == null || pathLearner.isValidTest(testCase)) { 
					candidates.add(testCase);
					minDist[i] = Double.MAX_VALUE;
					i++;
				}
			}
			// compute the minimum distances
			int size = Profiles.tests.size();
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < k; j++) {
					double dist = this.distance(candidates.get(j).getTest(), Profiles.tests.get(i).getTest());
					if (dist < minDist[j]) {
						minDist[j] = dist;
					}
				}
			}
			Iterator<TestCase> iterator = testCases.iterator();
			while (iterator.hasNext()) {
				TestCase t = iterator.next();
				for (int j = 0; j < k; j++) {
					double dist = this.distance(candidates.get(j).getTest(), t.getTest());
					if (dist < minDist[j]) {
						minDist[j] = dist;
					}
				}
			}
			// find the candidate with the maximum distance
			int index = 0;
			for (int i = 1; i < k; i++) {
				if (minDist[i] > minDist[index]) {
					index = i;
				}
			}
			// add to the set of tests
			testCases.add(candidates.get(index));
			if (testCases.size() == Config.TESTS_SIZE) {
				return testCases;
			}			
		}
	}	
	
	private HashSet<TestCase> genAdaptiveRandomTestsEAR() throws Exception {
		HashSet<TestCase> testCases = new HashSet<TestCase>(Config.TESTS_SIZE);	
		while (true) {
			int p = 20;	 // population size
			int g = 100; // number of generations
			double pMut = 0.1;	 // probability of mutation
			double pCross = 0.6; // probability of crossover

			// populations
			Individual[] pop = new Individual[p];
			for(int i = 0; i < p; i++) {
	            pop[i] = new Individual();
			}
			Individual[] newPop = new Individual[p];
			for(int i = 0; i < p; i++) {
				newPop[i] = new Individual();
			}
			
			// initialize and evaluate population
			for(int i = 0; i < p; i++) {
				TestCase tc = new TestCase(Util.randomTest());
				pop[i].test = tc.getTest();
				pop[i].changed = false;
				if (pathLearner == null || pathLearner.isValidTest(tc)) {
					double dist;
					double min = Double.MAX_VALUE;
					int size = Profiles.tests.size();
					for (int j = 0; j < size; j++) {
						dist = this.distance(pop[i].test, Profiles.tests.get(j).getTest());
						if (dist < min) {
							min = dist;
						}
					}
					Iterator<TestCase> iterator = testCases.iterator();
					while (iterator.hasNext()) {
						TestCase t = iterator.next();
						dist = this.distance(pop[i].test, t.getTest());
						if (dist < min) {
							min = dist;
						}
					}
					pop[i].fitness = min;
				} else {
					pop[i].fitness = -1;
				}
			}
			
			// loop until 100 generations
			for(int i = 0; i < g; i++) {
				// generate new individuals
				for (int j = 0; j < p; j++) {
					// find two random parent in population
					int parent1, parent2;
					int a,b;

					a = (int)(p * PseudoRandom.randDouble());
					if(a >= p) {a = p - 1;}
					b = (int)(p * PseudoRandom.randDouble());
					if(b >= p) {b = p - 1;}
					parent1 = pop[a].fitness > pop[b].fitness ? a : b;

					a = (int)(p * PseudoRandom.randDouble());
					if(a >= p) {a = p - 1;}
					b = (int)(p * PseudoRandom.randDouble());
					if(b >= p) {b = p - 1;}
					parent2 = pop[a].fitness > pop[b].fitness ? a : b;

					// crossover
					if(PseudoRandom.randDouble() < pCross) {
						int crossoverPoint = PseudoRandom.randInt(0, Config.CLS.length - 1);
						for (int k = 0; k < crossoverPoint; k++) {
							newPop[j].test[k] = pop[parent1].test[k];
						}
						for (int k = crossoverPoint; k < Config.CLS.length; k++) {
							newPop[j].test[k] = pop[parent2].test[k];
						}
						newPop[j].changed = true;
					} else {
						System.arraycopy(pop[parent1].test, 0, newPop[j].test, 0, Config.CLS.length);
						newPop[j].fitness = pop[parent1].fitness;
						newPop[j].changed = pop[parent1].changed;
					}
					
					// mutate
					for(int k = 0; k < Config.CLS.length; k++) {
						if(PseudoRandom.randDouble() < pMut) {
							Class<?> cls = newPop[j].test[k].getClass();
							if (cls == Byte.class) {
								newPop[j].test[k] = (byte)PseudoRandom.randInt(Config.MIN_BYTE, Config.MAX_BYTE);
							} else if (cls == Short.class) {
								newPop[j].test[k] = (short)PseudoRandom.randInt(Config.MIN_SHORT, Config.MAX_SHORT);
							} else if (cls == Integer.class) {
								Integer varMin = Config.varMinIntMap.get(Config.PARAMETERS[k]);
								Integer varMax = Config.varMaxIntMap.get(Config.PARAMETERS[k]);
								if (varMin != null && varMax != null) {
									newPop[j].test[k] = PseudoRandom.randInt(varMin, varMax);
								} else {
									newPop[j].test[k] = PseudoRandom.randInt(Config.MIN_INT, Config.MAX_INT);
								}
							} else if (cls == Long.class) {
								newPop[j].test[k] = (long)PseudoRandom.randDouble(Config.MIN_LONG, Config.MAX_LONG);
							} else if (cls == Float.class) {
								newPop[j].test[k] = (float)PseudoRandom.randDouble(Config.MIN_FLOAT, Config.MAX_FLOAT);
							} else if (cls == Double.class) {
								newPop[j].test[k] = PseudoRandom.randDouble(Config.MIN_DOUBLE, Config.MAX_DOUBLE);
							} else if (cls == Boolean.class) {
								newPop[j].test[k] = PseudoRandom.randInt(0, 1) == 0 ? false : true;
							}
							newPop[j].changed = true;
	                    }
	                }//end for
	                
	                // Evaluate individuals
	                if (newPop[j].changed) {
	                	if (pathLearner == null || pathLearner.isValidTest(new TestCase(newPop[j].test))) {
		                	double dist;
		                	double min = Double.MAX_VALUE;
		                	int size = Profiles.tests.size();
		    				for (int k = 0; k < size; k++) {
		    					dist = this.distance(newPop[j].test, Profiles.tests.get(k).getTest());
		    					if (dist < min) {
		    						min = dist;
		    					}
		    				}
		                	Iterator<TestCase> iterator = testCases.iterator();
		    				while (iterator.hasNext()) {
		    					TestCase t = iterator.next();
		    					dist = this.distance(newPop[j].test, t.getTest());
		    					if (dist < min) {
		    						min=dist;
		    					}
		    				}
		    				newPop[j].fitness = min;
	                	} else {
	                		newPop[j].fitness = -1;
	                	}
		    			newPop[j].changed = false;
	    			}
				}// end for j,p

	            // switch populations
				Individual[] ptr;
				ptr = pop;
				pop = newPop;
				newPop = ptr;
			}// end for i,g

	        // output best individual
	        int maxIndex = 0;
	        double max = 0;
	        for (int i = 0; i < p; i++) {
	            if (pop[i].fitness > max) {
	                max = pop[i].fitness;
	                maxIndex = i;
	            }
	        }
	        if (max >= 0) {
	        	testCases.add(new TestCase(pop[maxIndex].test));
	        	if (testCases.size() == Config.TESTS_SIZE) {
	        		return testCases;
	        	}
	        }
		}
	}
	
	// compute the distance of two tests
	private double distance(Object[] t1, Object[] t2) {
		int size = Config.CLS.length;
		double dist = 0;
		for (int i = 0; i < size; i++) {
			if (Config.CLS[i] == byte.class) {
				dist += ((byte)t1[i] - (byte)t2[i]) * ((byte)t1[i] - (byte)t2[i]);
			} else if (Config.CLS[i] == short.class) {
				dist += ((short)t1[i] - (short)t2[i]) * ((short)t1[i] - (short)t2[i]);
			} else if (Config.CLS[i] == int.class) {
				dist += ((int)t1[i] - (int)t2[i]) * ((int)t1[i] - (int)t2[i]);				
			} else if (Config.CLS[i] == long.class) {
				dist += ((long)t1[i] - (long)t2[i]) * ((long)t1[i] - (long)t2[i]);				
			} else if (Config.CLS[i] == float.class) {
				dist += ((float)t1[i] - (float)t2[i]) * ((float)t1[i] - (float)t2[i]);
			} else if (Config.CLS[i] == double.class) {
				dist += ((double)t1[i] - (double)t2[i]) * ((double)t1[i] - (double)t2[i]);				
			} else if (Config.CLS[i] == boolean.class) {
				if ((boolean)t1[i] ^ (boolean)t2[i]) {
					dist += 1.0;
				} else {
					dist += 0.0;
				}
			}
		}
		return Math.sqrt(dist);
	}

}

class Individual {
	
	Object[] test;
	double fitness;
	boolean changed;
	
    public Individual() {
    	test = new Object[Config.CLS.length];
    }
    
}
