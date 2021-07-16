package ca.queensu.cs.antler4AC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import ca.queensu.cs.antler4AC.ACLexer;
import ca.queensu.cs.antler4AC.ACParser;
import ca.queensu.cs.antler4AC.EvalVisitor;

public class Main {

	public static void main(String[] args) throws Exception {
		/*String expression = "int n= 50; "
				+ "for (n=5;  n  <  10 ;n++) {\n" + 
				"\n" + 
				" // expressions can be surrounded by parenthesis, of course\n" + 
 
				"  log n;\n" + 
 
				"\n" + 
				"}";
		*/
		//String expression = "int count = 1;";
		//String expression = "int count = 4; for (count = 0 ; count < 10 ; count++) { log \"count: \" + count; if (count % 2 == 0) {log \" EVEN\"; } else { log \" ODD\"; }  } showHeap; showListSendMsg;";
		
		/*String expression = "int a=1; int b=0; \n" + 
				"    	if (1==1){\n" + 
				"    	log  \"a is equal to b\"; } "
				+ "		else if (1==1){log a;} "
				+ "		else if (b==1) {log b;}"
				+ "		showHeap; showListSendMsg;";
		*/
		//String expression = "int random = rand()%2; String capInsName = (char *)   this->getName(); ConfigComm.IAMMaster(capInsName).send(); showHeap; showListSendMsg;";
		
		/*String expression = "int j=1;\n" + 
				"    for (int i=0;i<10;i++)\n" + 
				"	j=j+1;  showHeap; showListSendMsg;";
		*/
		
		//String expression =  "char* name1 = \"Majid\"; char* name2 = \"Majid\"; int a = 0; int b = 2; if (name1 == name2) log \" They are the same !\";";
		
		/*String expression1 = "char* name1 = \"Majid\"; char* name2 = \"Majid\"; if (strcmp((char *   )   name1, \"Majid\")==0) log \" They are the same !\"; else log \" They are different!\";";
		String expression2 = "char* name1 = \"Majid\"; char* name2 = \"Majid\"; if (strcmp(name1, name2)==0) log \" They are the same !\"; else log \" They are different!\";";
		String expression3 = "char* name1 = \"Majid\"; char* name2 = \"Majid\"; if (strcmp(\"Majid\", \"Majid\")==0) log \" They are the same !\"; else log \" They are different!\";";
		String expression4 = "char* name1 = \"Majid\"; char* name2 = \"Majid\"; if (strcmp(\"Majid\", name2)==0) log \" They are the same !\"; else log \" They are different!\";";
		*/
		
		/*String expression1 = "AnnouncementServer1Id=AnnouncmentServer1.informEvery(UMLRTTimespec(AnnouncmentServerTimeout,0),4);";
		String expression2 = "AnnouncmentServer1.cancelTimer(AnnouncementServer1Id);";
		String expression3 = "configTimer.informIn(UMLRTTimespec(3,0));";
		String expression4 = "char * MasterName = (char *)this->getName();  if (KeepAliveTimerId.isValid()) KeepAliveTimer.cancelTimer(KeepAliveTimerId); if (logfile.is_open()) log \"Do not print that!\"; else log \" Print that!\"; showHeap; showListSendMsg; ";
		*/
		String expression3 = "int count = 4; for (count = 0 ; count < 10 ; count++) { log \"count: \" + count; if (count % 2 == 0) {log \" EVEN\"; } else { log \" ODD\"; }  } showHeap; showListSendMsg;";

		
		/*String expression1 = "char* str = \"MajidBabaei\"; int pos = str.find(\"B\"); log \"pos: \" + pos; showHeap; showListSendMsg;";
		String expression2 = "std::string str = \"MajidBabaei\"; int pos = str.find(\"B\"); log \"pos: \" + pos; showHeap; showListSendMsg;";
		String expression3 = "std::string str = \"MajidBabaei\"; std::string det = \"B\"; int pos = str.find(det); log \"pos: \" + pos; showHeap; showListSendMsg;";
		String expression4 = "(char*) str = \"MajidBabaei\"; int pos = str.find ( \"B\" ); log \"pos: \" + pos; showHeap; showListSendMsg;";
		*/
		
		String expression1 = " int r=rand()%30+5; char* str = \"MajidBabaei\"; int pos = str.find(\"B\"); std::string subStr = str.substr(pos,pos+2);";
		String expression2 = "std::string str = \"MajidBabaei\"; int pos = str.find(\"B\"); ";
		String expr = "  ConfigComm.QueryConfig().send();  showHeap; showListSendMsg;";
		
		//String expression4 = " ts.getclock(ts); std::this_thread::sleep_for(std::chrono::milliseconds(700)); if (logfile.is_open()) log \" DO not print\"; string this->hostConfig = \"1:server1\"; int pos = this->hostConfig.find(\":\"); string systemConfigRunningMode = this->hostConfig.substr(0, pos); string serverName = this->hostConfig.substr(pos+1, this->hostConfig.length()); if (strcmp(systemConfigRunningMode,\"1\")==0) Master.IAmAlive((char *)this->getName()).send(); showHeap; showListSendMsg;";
		//String expression4 = " int var = 1; ConfigComm.QueryConfig().send(); port.msg(\"test\").send(); port.msg(this->getName()).send(); port.msg(var).send(); showHeap; showListSendMsg;";
		
		List<String> listExprs = new ArrayList<String>();
		
		//listExprs.add(expression1);listExprs.add(expression2);listExprs.add(expression3);listExprs.add(expression4);
		
		Map<String, Value> maplocalHeap = new HashMap<String, Value>();
		maplocalHeap.put("pingCount", new Value(1,"Integer"));
		
		/*ACLexer lexer = new ACLexer(new ANTLRInputStream(expression));
		ACParser parser = new ACParser(new CommonTokenStream(lexer));
		new EvalVisitor(maplocalHeap).visit(parser.parse());
		//ParseTree tree = parser.parse();
		EvalVisitor visitor = new EvalVisitor(maplocalHeap);
		*/
		//for (String expr : listExprs) {
		ACLexer lexer = new ACLexer(new ANTLRInputStream(expr));
		//System.out.println("lexer.nextToken().getType(): "+lexer.nextToken().getType());
		ACParser parser = new ACParser(new CommonTokenStream(lexer));
		EvalVisitor visitor = new EvalVisitor(maplocalHeap,"server1");
		visitor.visit(parser.parse());
		//}
		//listPortMsg = visitor.getListPortMsg();
	}
	

}
