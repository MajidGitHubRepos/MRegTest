package ca.queensu.cs.antler4AC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import ca.queensu.cs.controller.CapsuleTracker;
/*
public class RunnableVisitor implements Runnable { 
	  
	public String actionCode = "";
	public static EvalVisitor visitor;
	
	public RunnableVisitor() {
		this.visitor= new EvalVisitor(CapsuleTracker.maplocalHeap);
	}
    public void run() 
    { 
        System.out.println(Thread.currentThread().getName() 
                         + ", executing run() method!"); 
    
        ACLexer lexer = new ACLexer(new ANTLRInputStream(CapsuleTracker.currentActionCode));
		ACParser parser = new ACParser(new CommonTokenStream(lexer));
		visitor.visit(parser.parse());
    }

}
*/