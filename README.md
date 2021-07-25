# Welcome to MRegTest Project  
> Efficient Replay-based Regression Testing forDistributed Reactive Systems in the Context ofModel-driven Development

As software evolves, regression testing techniques are typically used to ensure the new changes are not adversely affecting the existing features. Despite recent advances, regression testing for distributed systems remains challenging and extremely costly. Existing techniques often require running a failing system several time before detecting a regression. As a result, conventional approaches that use re-execution without considering the inherent non-determinism of distributed systems, and providing no (or low) control over execution are inadequate in many ways. In this paper, we present MRegTest, a replay-based regression testing framework in the context of model-driven development to facilitate deterministic replay of traces for detecting regressions while offering sufficient control for the purpose of testing over the execution of the changed system.

MRegTest provides a regression testing approach for distributed UML-RT modles developted via [Eclipse Papyrus for Real-time](https://eclipse.org/papyrus-rt/) (Papyrus-RT). Papyrus-RT is an Eclipse-based, open-source modelling development environment for UML-RT systems. Recently, an extension of Papyrus-RT has been developed that allows the development of distributed systems with [UML-RT](https://github.com/kjahed/papyrusrt-distribution)


A detailed description of the MRegTest can be found in our [MODELS 2021](https://github.com/MajidGitHubRepos/MRegTest/blob/main/MRegTest_technicalPaper.pdf) paper.


# Usage

## Step 1 (Download Eclipse development environment):
Please download [Eclipse IDE for Java Developers](https://www.eclipse.org/downloads/packages/release/2021-06/r/eclipse-ide-java-developers)
We have tested using the Version and Build id in below: 
- Version: 2021-06 (4.20.0)
- Build id: 20210612-2011

## Step 2 (Import the project):
Please open Eclipse and follow the direction in below to import the porject in your workstation.  
File > Import > Archaive File 
Then use the Brows button to select the archive file of the project.

## Step 3 (Convert to a Maven Project):
In your Eclipse just right click on the imported Java Project and click Configure and you should see "Convert to Maven Project" option.

## Step 4 (Run MRegTest's Control Panel):
1. Open the imported project and find the directory src/main/java. 
2. Inside this directory you will see the package ca.queensu.cs.outlook. Open this package.
3. Right click on MRegressionPanel.java and go to "Run as" and run it as java application. 
4. You should see the MRgeTest Control Panel
    - The first tab ```mutants generator``` provides you with the functionality for generating mutants from a model
   ![alt text](https://github.com/MajidGitHubRepos/MRegTest/blob/main/src/main/resources/mutantsgenerator.png)
    - The second tab ```RegressionTest``` allows you to perform regression testing on either multiple modified models or signel modified model
    
     ![alt text](https://github.com/MajidGitHubRepos/MRegTest/blob/main/src/main/resources/RegressionTesting.png)


## Step 5 (Testing the functionality of our proposed method):
In the following video we show how to use the system:

[<p style="text-align:center;"><img src="https://i.ibb.co/nbM8rL6/You-Tube-icon.png" width="193" height="130"></p>](https://youtu.be/1PXjmKgadQI)

Note:
- We have tested the system using the UML-RT model: BankATM.uml and also we use the 
- For pefroming the test you will need to specify a file as "Collected Traces File". You can use already collected traces file in 

## Source code layout
    .
    ├──src
    |   ├── com.controller                # All files for Creating Abstract Interpreter and Synthesizing variable values
    |   ├── com.antler4AC                 # All files for performing Action Code analysis  
    |   ├── com.server                    # All files for receiving traces from distributed clients
    |   ├── com.umlrtParser               # All files for performing structural/behavioral static analysis and creating PES
    |   ├── com.testing                   # All files for performing regression testing for distributed UML-RT models
    ├── JAR                               # All required JAR files that should be added to the project 
    ├── Experiments                   
    │   ├── Original                      # Original Models (including: Replication.zip, ParcelRouter.zip , ...)
    │   ├── Baseline                      # Models that annotate traces with timestamps
    └── MDebugger                     
    │   ├── DebuggerModel                 # The Debugging Agent which is developed using UML-RT  
    |   ├── Model_instrumentation         # All the developed script for the model transformation 
    |   ├── RealTimeLibs                  # All lib that should be added into the RTS directory
    │   └── MetaModels                    # All required metamodels for executing the transformation
    

## Background

The following links may provide useful resources regarding the UML-RT concepts, and using PapyrusRT.

[PapyrusRT Website](https://eclipse.org/papyrus-rt/)

[PapyrusRT Forums](https://www.eclipse.org/forums/index.php/f/314/)

[Getting Started with PapyrusRT](https://wiki.eclipse.org/Papyrus-RT/User/User_Guide/Getting_Started)

[UML-RT Language Concepts](https://pdfs.semanticscholar.org/7fae/fac63155a404e431c97201f89fc8c37a7d62.pdf)

[An executable formal semantics for UML-RT](https://link.springer.com/article/10.1007/s10270-014-0399-z)

[Distribution for UML-RT](https://github.com/kjahed/papyrusrt-distribution)
