grammar MyGrammar;

@header {
import java.util.*;
import helpers.*;
}

proofs returns[List<Proof> listOfProofs, boolean flag] @init {
	List<Proof> listOfProofs = new LinkedList<>();
	$listOfProofs = listOfProofs;
	$flag = false;
} : (line { $listOfProofs.add($line.proof); $flag |= $line.flag; })+ EOF;

line returns[Proof proof, boolean flag]
 locals
  [ int indent = 0; ]
 : (INDENT { $indent++; })* context DELIMITER expr COLON type ruleNumber { $proof = new Proof($indent, $context.mapOfContext, $expr.info, $type.info, $ruleNumber.number); $flag = $context.flag; };

context returns[Map<String, Type> mapOfContext, boolean flag] @init {
	Map<String, Type> mapOfContext = new HashMap<>();
	$mapOfContext = mapOfContext;
	$flag = false;
} :
	| v1=VAR COLON t1=type { $mapOfContext.put($v1.text, $t1.info); } (COMMA v2=VAR COLON t2=type { if ($mapOfContext.putIfAbsent($v2.text, $t2.info) != null) { $flag = true; } })*;

ruleNumber returns[int number] : RULE_OPEN NUM RULE_CLOSE { $number = Integer.parseInt($NUM.text); };

type returns[Type info]
 locals [ LinkedHashSet<String> vars = new LinkedHashSet<>();]
 :
	LBRACKET type RBRACKET { $info = $type.info; }
	| (FORALL VAR DOT { $vars.add($VAR.text); } )* monotype { $info = new Type($vars, $monotype.info); };

monotype returns[Monotype info] :
	LBRACKET f=monotype RBRACKET { $info = $f.info; } (ARROW s=monotype { $info = new Monotype($info, $s.info); })?
	| VAR { $info = new Monotype($VAR.text); } (ARROW monotype { $info = new Monotype($VAR.text, $monotype.info);})?;

expr returns[Expression info]:
 	LET VAR IS f=expr IN s=expr { $info = new Expression($VAR.text, $f.info, $s.info); }
	| (application { $info = $application.info; })? (lambda { $info = $info == null ? $lambda.info : new Expression($info, $lambda.info); })?;

lambda returns[Expression info] : SLASH VAR DOT expr { $info = new Expression($VAR.text, $expr.info); };

application returns[Expression info] : a1=atom { $info = $a1.info; } (a2=atom { $info = new Expression($info, $a2.info); })*;

atom returns[Expression info] :
	LBRACKET expr RBRACKET { $info = $expr.info; }
	| VAR { $info = new Expression($VAR.text); };

COMMA : ',';

RULE_OPEN : '[rule #';

RULE_CLOSE : ']';

SLASH : '\\';

IS : '=';

DOT : '.';

COLON : ':';

LET : 'let';

IN : 'in';

FORALL : 'forall';

INDENT : '*';

LBRACKET : '(';

RBRACKET : ')';

ARROW : '->';

DELIMITER : '|-';

VAR : [a-z][a-z0-9']*;

NUM : [1-6];

WS : [ \t\r\n]+ -> skip;