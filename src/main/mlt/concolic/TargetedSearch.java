/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package mlt.concolic;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;

public class TargetedSearch extends Search {

	public TargetedSearch (Config config, VM vm) {
		super(config,vm);
	}

	@Override
	public void search () {
		boolean depthLimitReached = false;
		depth = 0;
		notifySearchStarted();

		while (!done) {
			if (checkAndResetBacktrackRequest() || !isNewState() || isEndState() || isIgnoredState() || depthLimitReached ) {
				backtrack();
				depthLimitReached = false;
				depth--;
				notifyStateBacktracked();
				// get the new test inputs
				//InternalConstraintsTree tree = JDart.getConcolicExplorer(config).getCurrentAnalysis().getInternalConstraintsTree();
				//System.out.println(tree.findValuations("features.nested.Input.foo(Input.java:23)"));
				//System.out.println(tree.getBranchConstraints());
				break;
			}
			if (forward()) {
				depth++;
				notifyStateAdvanced();
				if (currentError != null){
					notifyPropertyViolated();
					if (hasPropertyTermination()) {
						break;
					}
				}
				if (depth >= depthLimit) {
					depthLimitReached = true;
					notifySearchConstraintHit("depth limit reached: " + depthLimit);
					continue;
				}
				if (!checkStateSpaceLimit()) {
					notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
					break;
				}
			} else {
				notifyStateProcessed();
			}
		}
		notifySearchFinished();
	}

  	@Override
	public boolean requestBacktrack () {
		doBacktrack = true;
		return true;
	}
  	
  	@Override
  	public boolean supportsBacktrack () {
  		return true;
  	}
}
