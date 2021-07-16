package ca.queensu.cs.testing;


public class PrepTestBed {
	
	public TestingEngine testingEngine;
	
	public PrepTestBed() {
		testingEngine = new TestingEngine();
	}


	
	public static void main(String[] args) {
	
		PrepTestBed prepTestBed = new PrepTestBed();
        Thread t1 = new Thread(prepTestBed.new RunnableImpl()); 
        t1.start(); 		
		
	}
	
	public class RunnableImpl implements Runnable {

		public void run() {
			testingEngine.modifyFile("./mxgraph/javascript/examples/grapheditor/www/index.html", "localModel_PingerPonger.xml", "localModel_PingerPonger_RT1.xml");
		}
	}
}
