grammar FML;

options {
    output=AST;
    ASTLabelType=CommonTree; // type of $stat.tree ref etc...
}

@header {
  package org.deegree.feature.persistence.mapping.antlr;
  import java.util.HashMap;
  import org.deegree.feature.persistence.mapping.ColumnName;
}

@lexer::header {
  package org.deegree.feature.persistence.mapping.antlr;
}

/* This will be the entry point of our parser. */
start
    :    eval
    ;

eval returns [ColumnName value]
    :    SimpleIdentifier {$value = new ColumnName($SimpleIdentifier.text);}
    |    QualifiedIdentifier {String[] ss = $QualifiedIdentifier.text.split("[.]"); $value = new ColumnName(ss[0], ss[1]);}
    |    SimpleIdentifier Arrow eval -> eval
    |    QualifiedIdentifier Arrow eval -> eval
    ;

/* A number: can be an integer value, or a decimal value */
Number
    :    ('0'..'9')+ ('.' ('0'..'9')+)?
    ;

SimpleIdentifier
    :    ('a'..'z' | 'A'..'Z' | '-' | '_')+
    ;
    
QualifiedIdentifier
    :    ('a'..'z' | 'A'..'Z' | '-' | '_')+ '.' ('a'..'z' | 'A'..'Z' | '-' | '_')* ('a'..'z' | 'A'..'Z' | '_')
    ;

Arrow
    :    '-' '>'
    ;

/* We're going to ignore all white space characters */
WS  
    :   (' ' | '\t' | '\r'| '\n') {$channel=HIDDEN;}
    ;
