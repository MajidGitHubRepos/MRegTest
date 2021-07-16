package ca.queensu.cs.antler4AC;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;


/* 
 * java -jar antlr-4.7.2-complete.jar -visitor AC.g4
 * 
 */


public class EvalVisitor extends ACBaseVisitor<Value> {

	// used to compare floating point numbers
	public static final double SMALL_VALUE = 0.00000000001;

	// store variables (there's only one global scope!)
	private Map<String, Value> HeapMem = new HashMap<String, Value>();
	private List<SendMessage> listPortMsg;
	public boolean eof;
	public String srvName;

	public EvalVisitor(Map<String, Value> mapLocalHeap, String serverName) {
		this.listPortMsg = new ArrayList<SendMessage>();
		eof = false;
		this.srvName = serverName;
		if (mapLocalHeap != null)
			for (Entry<String, Value> entry : mapLocalHeap.entrySet()) {
				HeapMem.put(entry.getKey(), entry.getValue());
			}
	}
	public EvalVisitor() {
		this(null, "");
	}
	

	public String getOpertionType(Value left,Value right) {

		String type = "String";
		if (left.type.contentEquals("Integer") && right.type.contentEquals("Integer")) {
			type = "Integer";
		}else if (left.type.contentEquals("Double") && !right.type.contentEquals("String")) {
			type = "Double";
		}else if (!left.type.contentEquals("String") && right.type.contentEquals("Double")) {
			type = "Double";
		}else if (!left.type.contentEquals("String") && right.type.contentEquals("Double")) {
			type = "Double";
		}
		return type;
	}

	public List<SendMessage> getListPortMsg() {
		eof = true;
		return listPortMsg;
	}

	public Map<String, Value> getHeapMem() {
		return HeapMem;
	}
	
	@Override
	public Value visitProcessingDone(ACParser.ProcessingDoneContext ctx) {
		eof = true;
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>> EOF <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		return new Value (0, "");
	}
	
	@Override
	public Value visitStrcmpFuncExpr(ACParser.StrcmpFuncExprContext ctx){
		 String str1Id = "";
		 String str2Id = "";
		 
		 Value str1 = null;
		 Value str2 = null;
		
		if (ctx.ID(0) != null) {
			str1Id = ctx.ID(0).getText();
			str1 = HeapMem.get(str1Id);
		}else {
			str1 = HeapMem.get(ctx.op1.getText());
			if (str1 == null)
				str1 = new Value(ctx.op1.getText(), "String");
		}
		
		if (ctx.ID(1) != null) {
			str2Id = ctx.ID(1).getText();
			str2 = HeapMem.get(str2Id);

		}else {
			str2 = HeapMem.get(ctx.op2.getText());
			if (str2 == null)
				str2 = new Value(ctx.op2.getText(), "String");
		}
		//System.out.println("str1ID: " + str1Id + ", str2ID: "+str2Id);
		//System.out.println("str1: " + str1.asString().replaceAll("^\"|\"$", "") + ", str2: "+str2.asString().replaceAll("^\"|\"$", ""));
		
		boolean eval = false;
		
		if (str1Id.isEmpty())
			eval = str1.asString().replaceAll("^\"|\"$", "").contentEquals(str2.toString().replaceAll("^\"|\"$", ""));
			//return new Value(str1.asString().contentEquals(str2.toString()),"");
		else 
			eval = str2.asString().replaceAll("^\"|\"$", "").contentEquals(str1.toString().replaceAll("^\"|\"$", ""));
			//return new Value(str2.asString().contentEquals(str2.toString()),"");
		
		if (eval)
			return new Value (0, "Integer");
		return new Value (-1, "Integer");
	}
	
	@Override
	public Value visitTimer_stat(ACParser.Timer_statContext ctx) {
		//Do nothing for timer stat
		return new Value(0,"");
		
	}
	
	@Override
	public Value visitGetTimerAssignment(ACParser.GetTimerAssignmentContext ctx) {
		//Do nothing for timer stat
		return new Value(0,"");
	}
	
	@Override
	public Value visitIgnoreExpr(ACParser.IgnoreExprContext ctx) {
		//Do nothing for timer stat
		return new Value(false,"");
	}
	
	@Override
	public Value visitFindFuncExpr(ACParser.FindFuncExprContext ctx) {
		
		String str = "";
		String delimiter = "";
		
		String id0 = ctx.ID(0).getText();
		Value value0 = HeapMem.get(id0);
		str = value0.asString();
		
		
		
		if (ctx.ID(1) != null) {
			String id1 = ctx.ID(1).getText();
			Value value1 = HeapMem.get(id1);
			delimiter = value1.asString();
		
		}else { 
			delimiter = ctx.STRING().toString().replaceAll("^\"|\"$", "");
		}
		//System.out.println("str: " + str + ", delimiter: "+delimiter);

		return new Value(str.indexOf(delimiter),"Integer");
	}
	
	@Override
	public Value visitSubstrFuncExpr(ACParser.SubstrFuncExprContext ctx) {
		String str = "";
		int pos0 =  0;
		int pos1 = 0;
		
		if (ctx.ID() != null) {
			String id = ctx.ID().getText();
			Value value = HeapMem.get(id);
			str = value.asString();
		
		}else { 
			str = ctx.STRING().toString().replaceAll("^\"|\"$", "");
		}
		
		Value value0 = this.visit(ctx.expr(0));
		Value value1 = this.visit(ctx.expr(1));
		
		pos0 = value0.asInteger();
		pos1 = value1.asInteger();
		
		
		//System.out.println("str: " + str + ", pos0: "+pos0 +", pos1: "+ pos1);
		if (pos1 != -1)
			return new Value(str.substring(pos0, pos1),"String");
		else 
			return new Value("","String");
	}
	
	@Override
	public Value visitLengthFuncExpr(ACParser.LengthFuncExprContext ctx) {
		String str = "";
		
		if (ctx.ID() != null) {
			String id = ctx.ID().getText();
			Value value = HeapMem.get(id);
			str = value.asString();
		
		}else { 
			str = ctx.STRING().toString().replaceAll("^\"|\"$", "");
		}
		
		return new Value(str.length(),"Integer");
	}

	// assignment/id overrides
	@Override
	public Value visitNormalAssignment(ACParser.NormalAssignmentContext ctx) {
		String id = ctx.ID().getText();
		Value value = HeapMem.get(id);
		//System.out.println("in visitNormalAssignment: "+value);
		if(value == null) {
			throw new RuntimeException("no such variable: " + id);
		}else if (value.toString().contains("getName()") && !srvName.isEmpty())
			return HeapMem.put(id, new Value (srvName, "String"));
		else if (value.toString().contains("getName()") && srvName.isEmpty())
			return HeapMem.put(id, new Value ("__CapInstanceName__", "String"));
		else 
			value = this.visit(ctx.expr());
		return HeapMem.put(id, value);
	}

	@Override
	public Value visitBasicAssignment(ACParser.BasicAssignmentContext ctx) {

		String id = ctx.ID().getText();
		if (ctx.expr().getText().contains("msg->sapIndex0_")) {
			return HeapMem.put(id, new Value ("msg->sapIndex0_", "String"));
		}else if (ctx.expr().getText().contains("getName()") && !srvName.isEmpty())
			return HeapMem.put(id, new Value (srvName, "String"));
		else if (ctx.expr().getText().contains("getName()")) {
			return HeapMem.put(id, new Value ("__CapInstanceName__", "String"));
		}
		if (ctx.expr() == null)
			return HeapMem.put(id, new Value (0, ""));
		else {
			switch (ctx.op.getType()) {
			case ACParser.INTVAR:
				return HeapMem.put(id, new Value (this.visit(ctx.expr()).asDouble().intValue(),"Integer"));
			case ACParser.DOUBLEVAR:
				return HeapMem.put(id, new Value (this.visit(ctx.expr()),"Double"));
			case ACParser.STRINGVAR:
				return HeapMem.put(id, new Value (this.visit(ctx.expr()),"String"));
			default:
				return HeapMem.put(id, new Value (this.visit(ctx.expr()),""));
			}
		}


	}

	@Override
	public Value visitMinusminusAssignment(ACParser.MinusminusAssignmentContext ctx) {
		String id = ctx.ID().getText();
		Value value = HeapMem.get(id);
		if(value == null) {
			throw new RuntimeException("no such variable: " + id);
		}
		switch (value.type) {
		case "Double":
			return HeapMem.put(id, new Value(value.asDouble()-1, "Double"));
		case "Integer":
			return HeapMem.put(id, new Value(value.asInteger()-1, "Integer"));
		case "Real":
			return HeapMem.put(id, new Value(value.asDouble()-1, "Double"));
		default:
			throw new RuntimeException("no such type: " + id);
		}
	}

	@Override
	public Value visitPlusplusAssignment(ACParser.PlusplusAssignmentContext ctx) {
		String id = ctx.ID().getText();
		Value value = HeapMem.get(id);

		if(value == null) {
			throw new RuntimeException("no such variable: " + id);
		}

		switch (value.type) {
		case "Double":
			return HeapMem.put(id, new Value(value.asDouble()+1, "Double"));
		case "Integer":
			return HeapMem.put(id, new Value(value.asInteger()+1, "Integer"));
		case "Real":
			return HeapMem.put(id, new Value(value.asDouble()+1, "Double"));
		default:
			throw new RuntimeException("no such type: " + id);

		}
	}

	@Override
	public Value visitPlusplusExpr(ACParser.PlusplusExprContext ctx) {
		Value value = this.visit(ctx .expr());
		return new Value(value.asDouble()+1, "Double");
	}

	@Override
	public Value visitMinusminusExpr(ACParser.MinusminusExprContext ctx) {
		Value value = this.visit(ctx .expr());
		return new Value(value.asDouble()-1, "Double");
	}
	
	@Override
	public Value visitGetNameAssignment(ACParser.GetNameAssignmentContext ctx) {
		String id = ctx.ID().getText();
		if (!srvName.isEmpty())
			return HeapMem.put(id, new Value (srvName, "String"));
		else
			return HeapMem.put(id, new Value("__CapInstanceName__", "String"));
	}

	@Override
	public Value visitIdAtom(ACParser.IdAtomContext ctx) {
		String id = ctx.getText();


		Value value = HeapMem.get(id);
		if(value == null) {
			// throw new RuntimeException("no such variable: " + id); //TODO: all varibale should be given properly later like: this->hostConfig=config;
		}
		return value;
	}

	// atom overrides
	@Override
	public Value visitStringAtom(ACParser.StringAtomContext ctx) {
		String str = ctx.getText();
		// strip quotes
		str = str.substring(1, str.length() - 1).replace("\"\"", "\"");
		return new Value(str, "String");
	}

	@Override
	public Value visitNumberAtom(ACParser.NumberAtomContext ctx) {
		if (ctx.getText().contains("."))
			return new Value(Double.valueOf(ctx.getText()), "Double");
		else
			return new Value(Integer.valueOf(ctx.getText()), "Integer");
	}

	@Override
	public Value visitBooleanAtom(ACParser.BooleanAtomContext ctx) {
		return new Value(Boolean.valueOf(ctx.getText()), "Boolean");
	}

	@Override
	public Value visitNilAtom(ACParser.NilAtomContext ctx) {
		return new Value(null, "");
	}

	// expr overrides
	@Override
	public Value visitParExpr(ACParser.ParExprContext ctx) {
		return this.visit(ctx.expr());
	}

	@Override
	public Value visitPowExpr(ACParser.PowExprContext ctx) {
		Value left = this.visit(ctx.expr(0));
		Value right = this.visit(ctx.expr(1));
		
		
		switch (left.type) {
		case "Double":
			return new Value(Math.pow(left.asDouble(), right.asDouble()), "Double");
		case "Integer":
			return new Value(Math.pow(left.asInteger(), right.asInteger()), "Integer");
		case "Real":
			return new Value(Math.pow(left.asDouble(), right.asDouble()), "Double");
		default:
			throw new RuntimeException("no such type! ");
		}
	}

	@Override
	public Value visitUnaryMinusExpr(ACParser.UnaryMinusExprContext ctx) {
		Value value = this.visit(ctx.expr());
		
		switch (value.type) {
		case "Double":
			return new Value(-value.asDouble(), "Double");
		case "Integer":
			return new Value(-value.asInteger(), "Integer");
		case "Real":
			return new Value(value.asDouble(), "Double");
		default:
			throw new RuntimeException("no such type! ");
		}
	}

	@Override
	public Value visitNotExpr(ACParser.NotExprContext ctx) {
		Value value = this.visit(ctx.expr());
		return new Value(!value.asBoolean(), "Boolean");
	}

	@Override
	public Value visitMultiplicationExpr(@NotNull ACParser.MultiplicationExprContext ctx) {
		Value left = this.visit(ctx.expr(0));
		Value right = this.visit(ctx.expr(1));

		String type = getOpertionType(left,right);

		switch (ctx.op.getType()) {
		case ACParser.MULT:
			switch (type) {
			case "Double":
				return new Value(left.asDouble() * right.asDouble(), type);
			case "Integer":
				return new Value(left.asInteger() * right.asInteger(), type);
			case "Real":
				return new Value(left.asDouble() * right.asDouble(), "Double");
			default:
				throw new RuntimeException("no such type! ");
			}
		case ACParser.DIV:
			switch (type) {
			case "Double":
				return new Value(left.asDouble() / right.asDouble(), type);
			case "Integer":
				return new Value(left.asInteger() / right.asInteger(), type);
			case "Real":
				return new Value(left.asDouble() / right.asDouble(), "Double");
			default:
				throw new RuntimeException("no such type! ");
			}
		case ACParser.MOD:
			switch (type) {
			case "Double":
				return new Value(left.asDouble()  % right.asDouble(), type);
			case "Integer":
				return new Value(left.asInteger() % right.asInteger(), type);
			case "Real":
				return new Value(left.asDouble()  % right.asDouble(), "Double");
			default:
				throw new RuntimeException("no such type! ");
			}
		default:
			throw new RuntimeException("unknown operator: " + ACParser.tokenNames[ctx.op.getType()]);
		}
	}
	@Override
	public Value visitAdditiveExpr(@NotNull ACParser.AdditiveExprContext ctx) {

		Value left = this.visit(ctx.expr(0));
		Value right = this.visit(ctx.expr(1));

		String type = getOpertionType(left,right);
		//System.out.println(ctx.op.getType() + " int visitAdditiveExpr [type]: " + left.type + ", " + right.type + ", " + ACParser.PLUS + ", " + type);

		switch (ctx.op.getType()) {
		case ACParser.PLUS:

			switch (type) {
			case "Double":
				return new Value(left.asDouble() + right.asDouble(), type);
			case "Integer":
				return new Value(left.asInteger() + right.asInteger(), type);
			case "Real":
				return new Value(left.asDouble() + right.asDouble(), "Double");
			default:
				return new Value(left.asString() + right.asString(), "String");
			}

		case ACParser.PLUSPLUS:
			switch (type) {
			case "Double":
				return new Value(left.asDouble() + 1, type);
			case "Integer":
				return new Value(left.asInteger() + 1, type);
			case "Real":
				return new Value(left.asDouble() + 1, "Double");
			}

		case ACParser.MINUS:
			switch (type) {
			case "Double":
				return new Value(left.asDouble() - right.asDouble(), type);
			case "Integer":
				return new Value(left.asInteger() - right.asInteger(), type);
			case "Real":
				return new Value(left.asDouble() - right.asDouble(), "Double");
			}
		case ACParser.MINUSMINUS:
			switch (type) {
			case "Double":
				return new Value(left.asDouble() - 1, type);
			case "Integer":
				return new Value(left.asInteger() - 1, type);
			case "Real":
				return new Value(left.asDouble() - 1, "Double");
			
			}
		default:
			throw new RuntimeException("unknown operator: " + ACParser.tokenNames[ctx.op.getType()]);
		}
	}

	@Override
	public Value visitRelationalExpr(@NotNull ACParser.RelationalExprContext ctx) {

		Value left = this.visit(ctx.expr(0));
		Value right = this.visit(ctx.expr(1));

		switch (ctx.op.getType()) {
		case ACParser.LT:
			return new Value(left.asDouble() < right.asDouble(), "");
		case ACParser.LTEQ:
			return new Value(left.asDouble() <= right.asDouble(),"");
		case ACParser.GT:
			return new Value(left.asDouble() > right.asDouble(),"");
		case ACParser.GTEQ:
			return new Value(left.asDouble() >= right.asDouble(),"");
		default:
			throw new RuntimeException("unknown operator: " + ACParser.tokenNames[ctx.op.getType()]);
		}
	}
	
	@Override
	public Value visitEqualityExpr(@NotNull ACParser.EqualityExprContext ctx) {
		//System.out.println("in visitEqualityExpr");

		Value left = this.visit(ctx.expr(0));
		if (left == null) {
			String leftID = ctx.expr(0).getText();
			left = HeapMem.get(leftID);
		}
		Value right = this.visit(ctx.expr(1));
		if (left == null) {
			String rightID = ctx.expr(1).getText();
			right = HeapMem.get(rightID);
		}

		String type = getOpertionType(left,right);

		switch (ctx.op.getType()) {
		case ACParser.EQ:
			switch (type) {
			case "Double":
				return left.isDouble() && right.isDouble() ?
						new Value(Math.abs(left.asDouble() - right.asDouble()) < SMALL_VALUE, "") :
							new Value(left.equals(right),"");
			case "Integer":
				return left.isInteger() && right.isInteger() ?
						new Value(Math.abs(left.asInteger() - right.asInteger()) < SMALL_VALUE, "") :
							new Value(left.equals(right),"");	
			}
		case ACParser.NEQ:
			
			switch (type) {
			case "Double":
				return left.isDouble() && right.isDouble() ?
						new Value(Math.abs(left.asDouble() - right.asDouble()) >= SMALL_VALUE, "") :
							new Value(!left.equals(right),"");
			case "Integer":
				return left.isInteger() && right.isInteger() ?
						new Value(Math.abs(left.asInteger() - right.asInteger()) >= SMALL_VALUE, "") :
							new Value(!left.equals(right),"");
			}
		default:
			throw new RuntimeException("unknown operator: " + ACParser.tokenNames[ctx.op.getType()]);
		}
	}

	@Override
	public Value visitAndExpr(ACParser.AndExprContext ctx) {
		Value left = this.visit(ctx.expr(0));
		Value right = this.visit(ctx.expr(1));
		return new Value(left.asBoolean() && right.asBoolean(), "Boolean");
	}

	@Override
	public Value visitOrExpr(ACParser.OrExprContext ctx) {
		Value left = this.visit(ctx.expr(0));
		Value right = this.visit(ctx.expr(1));
		return new Value(left.asBoolean() || right.asBoolean(), "Boolean");
	}
	
	@Override
	public Value visitRandFuncExpr(ACParser.RandFuncExprContext ctx){
		//Generate random number within [1,100]
		Random random = new Random();
		return new Value(random.nextInt((100 - 1) + 1) + 1, "Integer");
	}

	// log override
	@Override
	public Value visitLog(ACParser.LogContext ctx) {
		Value value = this.visit(ctx.expr());
		System.out.println(value);
		return value;
	}

	// if override
	@Override
	public Value visitIf_stat(ACParser.If_statContext ctx) {

		List<ACParser.Condition_blockContext> conditions =  ctx.condition_block();

		boolean evaluatedBlock = false;

		for(ACParser.Condition_blockContext condition : conditions) {

			Value evaluated = this.visit(condition.expr());

			if(evaluated.asBoolean()) {
				evaluatedBlock = true;
				// evaluate this block whose expr==true
				this.visit(condition.stat_block());
				break;
			}
		}

		if(!evaluatedBlock && ctx.stat_block() != null) {
			// evaluate the else-stat_block (if present == not null)
			this.visit(ctx.stat_block());
		}
		return Value.VOID;
	}
	
	// while override
	@Override
	public Value visitWhile_stat(ACParser.While_statContext ctx) {

		Value value = this.visit(ctx.expr());

		while(value.asBoolean()) {

			// evaluate the code block
			this.visit(ctx.stat_block());

			// evaluate the expression
			value = this.visit(ctx.expr());
		}

		return Value.VOID;
	}


	@Override
	public Value visitDoWhileLoop(ACParser.DoWhileLoopContext ctx) {
		Value value = this.visit(ctx.expr());

		do{
			// evaluate the code block
			this.visit(ctx.stat_block());

			// evaluate the expression
			value = this.visit(ctx.expr());
		} while(value.asBoolean());

		return Value.VOID;
	}


	@Override
	public Value visitForLoop(ACParser.ForLoopContext ctx) {
		
		
		String id = ctx.ID().getText();

		if (ctx.op != null) {
			switch (ctx.op.getType()) {
			case ACParser.INTVAR:
				HeapMem.put(id, new Value (this.visit(ctx.expr(0)).asDouble().intValue(),"Integer"));
			case ACParser.DOUBLEVAR:
				HeapMem.put(id, new Value (this.visit(ctx.expr(0)).asDouble(),"Double"));
			default:
				HeapMem.put(id, new Value (this.visit(ctx.expr(0)).asDouble().intValue(),"Integer"));
			}
		}else {
		Value valueFirst = HeapMem.get(id);
		HeapMem.put(id, new Value (this.visit(ctx.expr(0)),valueFirst.type));
		}
		
		
		Value evaluated = this.visit(ctx.expr(1));
		
		while (evaluated.asBoolean()) {
			// evaluate the code block
			this.visit(ctx.stat_block());            
			HeapMem.put(id, new Value (this.visit(ctx.expr(2)).asDouble().intValue(),"Integer"));
			evaluated = this.visit(ctx.expr(1));
		}
		return Value.VOID;
	}

	@Override
	public Value visitSend_stat(ACParser.Send_statContext ctx) {
		String port = ctx.ID(0).getText();
		String msg = ctx.ID(1).getText();
		String dataName = "";
		Value data = new Value("","String");
		if (ctx.expr() == null) { //port.msg().send();
			SendMessage sendMsg = new SendMessage(port,msg, null, null, null);
			listPortMsg.add(sendMsg);
			return data;
		}else if (ctx.expr().getText().contains("\"")) {  //port.msg("test").send();
			dataName = msg+":VAR";
			data = new Value(ctx.expr().getText().replaceAll("^\"|\"$", ""),"String");
		}else if (ctx.getText().contains("getName") && !srvName.isEmpty()){ //port.msg(this->getName()).send();
			dataName = "__getName__";
			data = new Value(srvName,"String"); 
		}else if (ctx.getText().contains("getName") && srvName.isEmpty()){ //port.msg(this->getName()).send();
			dataName = "__getName__";
			data = new Value("__CapInstanceName__","String"); 
		}else { //port.msg(var).send();
			dataName = ctx.expr().getText();
			data = this.visit(ctx.expr());
		}
		
		SendMessage sendMsg = new SendMessage(port,msg, dataName, data, null);
		listPortMsg.add(sendMsg);

		return data;
	}
	
	@Override
	public Value visitSendat_stat(ACParser.Sendat_statContext ctx) {
		String port = ctx.ID(0).getText();
		String msg = ctx.ID(1).getText();
		String dataName = ctx.expr(0).getText();
		Value data = this.visit(ctx.expr(0));
		Value dest = new Value(0,"String");
		Value dataTmp = HeapMem.get(dataName);

		if (ctx.getText().contains("msg->sapIndex0_")){
			dest = new Value("msg->sapIndex0_","String");
		}else	
			dest = this.visit(ctx.expr(1));

		if (ctx.getText().contains("getName") && srvName.isEmpty()){
			dataName = "__getName__";
			data = new Value("__CapInstanceName__","String");
		}else if (ctx.getText().contains("getName") && !srvName.isEmpty()){ //port.msg(this->getName()).send();
			dataName = "__getName__";
			data = new Value(srvName,"String"); 
		}else if (ctx.getText().contains("\"")|| (dataTmp == null)){
			dataName = msg+":VAR";
			data = this.visit(ctx.expr(0));
		}
		//System.out.println("in visitSendat_stat"+ data.asString());

		SendMessage sendMsg = new SendMessage(port,msg, dataName, data, dest);
		listPortMsg.add(sendMsg);

		return data;
	}
	
	@Override
	public Value visitReturn_stat(ACParser.Return_statContext ctx) {
		//TODO: we only support boolean value as a return value in this version
		if ((ctx.expr() != null) && (!HeapMem.containsKey("__GUARD__"))) {
			HeapMem.put("__GUARD__", new Value (this.visit(ctx.expr()).asBoolean(),"Boolean"));
			return new Value(this.visit(ctx.expr()), "Boolean");
		}
		return new Value(0, ""); 
	}

	@Override
	public Value visitUnknownsExpr(ACParser.UnknownsExprContext ctx) {
		//System.out.println("in [visitUnknowns] : " + ctx.getText());

		//Do nothing
		return new Value(0, "");
	}
	public Value visitIgnoreLine(ACParser.IgnoreLineContext ctx) {
		//System.out.println("in [visitUnknowns] : " + ctx.getText());

		//Do nothing
		return new Value(0, "");
	}

	@Override
	public Value visitShowHeapMem(ACParser.ShowHeapMemContext ctx){


		System.out.println("=====================[HeapMem]=================");
		for (Entry<String, Value> entry : HeapMem.entrySet()) {
			System.out.println("key: " + entry.getKey() + ", value: "+ entry.getValue().toString());
		}
		return new Value(0,"");

	}

	@Override
	public Value visitShowListSendMsg(ACParser.ShowListSendMsgContext ctx){
		System.out.println("=====================[listPortMsg]=================");
		for (SendMessage portMsg : listPortMsg) {
			System.out.println(portMsg.allDataToString());
		}
		return new Value(0,"");

	}



}
