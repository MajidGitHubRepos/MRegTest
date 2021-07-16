package ca.queensu.cs.testing;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.queensu.cs.umlrtParser.ParserEngine;
import ca.queensu.cs.umlrtParser.UmlrtParser;
import ca.queensu.cs.umlrtParser.UmlrtParser.RunnableImpl;


public class UmlrtParserTest {

	@Test
	public void test() {
		UmlrtParser umlrtParser = new UmlrtParser();
		Thread t1 = new Thread(umlrtParser.new RunnableImpl()); 
        t1.start(); 
		ParserEngine parserEngine = umlrtParser.getParserEngine();
		while(!umlrtParser.getUmlrtParsingDone()) {}
	    //System.out.println("--------------> parserEngine.mapStateData: "+ parserEngine.mapStateData.size() );

		//if(umlrtParser.getUmlrtParsingDone())
	}

}
