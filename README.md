MLT: Systematic Testing via Learning and Guiding
======

MLT is a tool for systematic software testing via machine learning techniques.


Running
------
* Instrument the software under test (SUT) at the source code level for taint tracking and branch execution recording.
* Use [Phosphor](https://github.com/Programming-Systems-Lab/phosphor) to instrument jre, mlt.jar and SUT at the bytecode level for taint tracking, and the instrumented SUT will be run with the instrumented jre and mlt.jar.
* Use the original jre and mlt.jar for running concolic execution and our tool, and communicate with the instrumented jre with [Chronicle](https://github.com/OpenHFT/Chronicle-Queue).
