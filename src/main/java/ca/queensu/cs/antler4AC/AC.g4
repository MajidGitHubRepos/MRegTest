grammar AC;

parse
 : block EOF
 ;

block
 : stat*
 ;

stat
 : assignment
 | if_stat
 | while_stat
 | loop_stat
 | log
 | send_stat
 | sendat_stat
 | showContent_stat
 | timer_stat
 | return_stat
 | ignore_stat
// | unknowns
 ;

done: EOF #processingDone
; 
assignment
 : ID ASSIGN (INTVAR | DOUBLEVAR | STRINGVAR | BOOLVAR)? expr SCOL     											 #normalAssignment
 | op=(INTVAR | DOUBLEVAR | STRINGVAR | BOOLVAR) ID ASSIGN? (INTVAR | DOUBLEVAR | STRINGVAR | BOOLVAR)? expr? SCOL	     #basicAssignment
 | ID MINUSMINUS SCOL       										 #minusminusAssignment
 | ID PLUSPLUS SCOL													 #plusplusAssignment
 | STRINGVAR? ID ASSIGN GETNAME SCOL 								 #getNameAssignment
 | ID ASSIGN timer_stat												 #getTimerAssignment
 ;

ignore_stat
 : IGNORELINE                                                   #ignoreLine
 ;

if_stat
 : IF condition_block (ELSE IF condition_block)* (ELSE stat_block)?
 ;
 
condition_block
 : expr stat_block
 ;

stat_block
 : OBRACE block CBRACE
 | stat
 ;

while_stat
 : WHILE OPAR expr CPAR stat_block
 ;
 
loop_stat
   : WHILE '(' expr ')' stat_block                     			#whileLoop
   | DO stat_block WHILE '(' expr ')' ';'			   			#doWhileLoop
   | FOR '(' op=(INTVAR | DOUBLEVAR)? ID ASSIGN expr ';' expr ';' expr ')' stat_block    #forLoop
   ;
 
sendat_stat
   : ID '.' ID '(' (INTVAR | DOUBLEVAR | STRINGVAR | BOOLVAR)? expr ')' '.' SENDAT '(' (INTVAR | DOUBLEVAR | STRINGVAR | BOOLVAR)? (expr | BACKMSG) ')' SCOL
   | ID '.' ID '()' '.' SENDAT '(' (expr | BACKMSG) ')' SCOL
   ;

send_stat
   : ID '.' ID '(' (INTVAR | DOUBLEVAR | STRINGVAR | BOOLVAR)? expr ')' '.' SEND '()' SCOL
   | ID '.' ID '(' (INTVAR | DOUBLEVAR | STRINGVAR | BOOLVAR)? GETNAME? ')' '.' SEND '()' SCOL
   | ID '.' ID '()' '.' SEND '()' SCOL
   ;

timer_stat
 : ID '.' op=('informIn' | 'cancelTimer' | 'informEvery') .*? SCOL 
 ;

showContent_stat
   : SHOWHEAP SCOL     #showHeapMem
   | SHOWLISTSEND SCOL #showListSendMsg
   ;

log
 : LOG expr SCOL
 ;

return_stat
 : RETURN expr? SCOL
 ;

expr
 : expr POW<assoc=right> expr           						#powExpr
 | expr MINUSMINUS					    						#minusminusExpr
 | expr PLUSPLUS					    						#plusplusExpr
 | MINUS expr                          							#unaryMinusExpr
 | NOT expr                             						#notExpr
 | expr op=(MULT | DIV | MOD | MINUSMINUS | PLUSPLUS) expr      #multiplicationExpr
 | expr op=(PLUS | MINUS) expr          						#additiveExpr
 | expr op=(LTEQ | GTEQ | LT | GT) expr 						#relationalExpr
 | expr op=(EQ | NEQ) expr               						#equalityExpr
 | expr AND expr                        						#andExpr
 | expr OR expr                         						#orExpr
 | GETNAME														#getNameExpr
 | BACKMSG														#backMsgExpr
 | atom                                 						#atomExpr
 | RANDFUNC														#randFuncExpr
 | ID DOT 'find' OPAR (ID | STRING) CPAR 											#findFuncExpr
 | (ID | STRING) DOT 'substr' OPAR expr ',' expr CPAR 						#substrFuncExpr
 | (ID | STRING) DOT 'length()' 											#lengthFuncExpr
 | 'strcmp' '(' STRINGVAR? op1=(ID | STRING) ',' STRINGVAR? op2=(ID | STRING) ')' 								    #strcmpFuncExpr
 | IGNOREEXPR                                                   #ignoreExpr
 ;

atom
 : OPAR expr CPAR #parExpr
 | (INT | FLOAT)  #numberAtom
 | (TRUE | FALSE) #booleanAtom
 | ID             #idAtom
 | STRING         #stringAtom
 | NIL            #nilAtom
 ;

DOT : '.' ;
INSTREAM : '<<';
OUTSTREAM : '>>';
OR : '||';
AND : '&&';
EQ : '==';
NEQ : '!=';
GT : '>';
LT : '<';
GTEQ : '>=';
LTEQ : '<=';
PLUS : '+';
PLUSPLUS : '++';
MINUS : '-';
MINUSMINUS : '--';
MULT : '*';
DIV : '/';
MOD : '%';
POW : '^';
NOT : '!';

COMMA : ',';
COLON : ':' ;
SCOL : ';';
ASSIGN : '=';
OPAR : '(';
CPAR : ')';
OBRACE : '{';
CBRACE : '}';
LBRACKET : '[' ;
RBRACKET : ']' ;

TRUE : 'true';
FALSE : 'false';
NIL : 'nil';
IF : 'if';
ELSE : 'else';
WHILE : 'while';
LOG : 'log';
FOR : 'for';
DO  : 'do' ;
RETURN: 'return';

SEND : 'send';
SENDAT : 'sendAt';
BACKMSG: 'msg->sapIndex0_';

GETNAME:  STRINGVAR? SPACE? 'this->getName()';

RANDFUNC: 'rand()';

SHOWHEAP : 'showHeap';
SHOWLISTSEND : 'showListSendMsg';

BOOLVAR : 'bool' ;
INTVAR : 'int';
DOUBLEVAR : 'double';
CHARVAR : 'char';
STRINGVAR 
 : 'string'
 | '('? SPACE? CHARVAR SPACE? MULT SPACE? ')'?
 ;


ID
 : [a-zA-Z_] [a-zA-Z_0-9]*
 ;

INT
 : [0-9]+
 ;

FLOAT
 : [0-9]+ '.' [0-9]* 
 | '.' [0-9]+
 ;

STRING
 : '"' (~["\r\n] | '""')* '"'
 ;

COMMENT
 : '//' ~[\r\n]* -> skip
 ;
BLOCKCOMMENT
 : '/*' .*? '*/' -> skip
 ;

SPACE
 :  (' ' | '\t' | '\n')+ -> skip
 ;

NEWLINE
 : ('\r' '\n'? | '\n') -> skip
 ;

unknowns : .   #unknownsExpr
 ;

Space
 :  (' ' | '\t' | '\n')+ {skip();}
 ; 

IGNORELINE : 'std::'? ( 'log.' | 'logfile.flush()' |'logfile<<' | 'logfile>>' | 'cout<<' | 'this_thread' | 'ts.getclock') .*? ';'  
 ;

IGNOREWORD
 : ('this->' | 'std::' | '.c_str()') -> skip
 ;

IGNOREEXPR
 : ('logfile' | 'ResponseTimerId' | 'KeepAliveTimerId' | 'KeepAliveTimerId' | 'AnnouncementTimerId' | 'TimerId' | 'AnnouncementServer1Id' | 'AnnouncementServer2Id') DOT .*? '(' .*? ')'
 ;
  
//IGNORE
// : ('log.' | 'logfile.flush()' | 'this->hostConfig' | 'RequestTimer.informIn' |  'ts.getclock' | 'std::' |IF .*? 'systemConfig.RunningMode'| IF? .*? 'logfile' | 'TimerId=' | 'KeepAliveTimerId=' | 'AnnouncementTimerId=' | 'ConfigComm.StartUp(this->systemConfig)') .*? ';'  -> skip
// ;
//IFIGNORE
// : (IF .*?  'hostConfig.')
// ; 
//BLOCKIGNORE
// : (ELSE? .*? IF *.? STRCMP) .*? '}'  -> skip
// ;
