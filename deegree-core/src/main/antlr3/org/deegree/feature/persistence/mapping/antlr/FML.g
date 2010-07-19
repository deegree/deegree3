grammar FML;

options {
    output=AST;
    ASTLabelType=CommonTree; // type of $stat.tree ref etc...
}

@header {
  package org.deegree.feature.persistence.mapping.antlr;
  import java.util.Collections;
  import org.deegree.feature.persistence.mapping.MappingExpression;
  import org.deegree.feature.persistence.mapping.JoinChain;
  import org.deegree.feature.persistence.mapping.DBField;
  import org.deegree.feature.persistence.mapping.Function;
  import org.deegree.feature.persistence.mapping.StringConst;
}

@lexer::header {
  package org.deegree.feature.persistence.mapping.antlr;
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

expr
    :    mappingExpr;

mappingExpr returns [MappingExpression value]
    :    dbField {$value=$dbField.value;}
    |    joinChain {$value=$joinChain.value;}
    |    function {$value=$function.value;}
    |    stringConst {$value=$stringConst.value;}
    ;
catch [RecognitionException re] {
    throw re;
}

stringConst returns [StringConst value]
    :   Text  {$value=new StringConst($Text.text.substring(1,$Text.text.length()-1));}
    ;

function returns [Function value]
    :    Identifier {$value=new Function($Identifier.text);}
    '(' ((ma=mappingExpr{$value.addArg($ma.value);}) (',' ma2=mappingExpr{$value.addArg($ma2.value);})*)? ')'
    ;
catch [RecognitionException re] {
    throw re;
}

joinChain returns [JoinChain value]
    :    dbf1=dbField '->' dbf2=dbField {$value=new JoinChain($dbf1.value,$dbf2.value);}
    |    dbf1=dbField '->' jc1=joinChain {$value=new JoinChain($dbf1.value, $jc1.value);}
    ;
catch [RecognitionException re] {
    throw re;
}
    
dbField returns [DBField value]
    :    i1=Identifier {$value=new DBField ($i1.text);}
    |    i1=Identifier '.' i2=Identifier {$value=new DBField ($i1.text,$i2.text);}
    |    i1=Identifier '.' i2=Identifier '.' i3=Identifier {$value=new DBField ($i1.text,$i2.text,$i3.text);}
    ;
catch [RecognitionException re] {
    throw re;
}
   
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/   
   
Identifier
    :    ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')+
    ;

Text
    :    '\'' ~('\'')* '\''
    ;

/* We're going to ignore all white space characters */
WS  
    :   (' ' | '\t' | '\r'| '\n') {$channel=HIDDEN;}
    ;
