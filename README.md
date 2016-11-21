MELT: Systematic Testing via Learning and Guiding
======

MELT is a tool for systematic software testing through machine learning and concolic execution.


Running
------
* Instrument the software under test (SUT) at the source code level for taint tracking and branch execution recording.
* Use [Phosphor](https://github.com/Programming-Systems-Lab/phosphor) to instrument jre, melt.jar and SUT at the bytecode level for taint tracking, and the instrumented SUT will be run with the instrumented jre and melt.jar.
* Use the original jre and melt.jar for running concolic execution and our tool...
