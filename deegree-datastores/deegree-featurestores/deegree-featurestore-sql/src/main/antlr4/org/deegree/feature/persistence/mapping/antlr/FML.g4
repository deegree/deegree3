grammar FML;

@header {
  import java.util.Collections;
  import org.deegree.sqldialect.filter.MappingExpression;
  import org.deegree.sqldialect.filter.DBField;
  import org.deegree.feature.persistence.sql.expressions.Function;
  import org.deegree.feature.persistence.sql.expressions.StringConst;
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

expr
    :    mappingExpr EOF
    ;

mappingExpr returns [MappingExpression value]
    :    dbField {$value=$dbField.value;}
    |    function {$value=$function.value;}
    |    stringConst {$value=$stringConst.value;}
    ;

stringConst returns [StringConst value]
    :   Text  {$value=new StringConst($Text.text.substring(1,$Text.text.length()-1));}
    ;

function returns [Function value]
    :    Identifier {$value=new Function($Identifier.text);}
    '(' ((ma=mappingExpr{$value.addArg($ma.value);}) (',' ma2=mappingExpr{$value.addArg($ma2.value);})*)? ')'
    ;

dbField returns [DBField value]
    :    i1=Identifier {$value=new DBField ($i1.text);}
    |    i1=Identifier '.' i2=Identifier {$value=new DBField ($i1.text,$i2.text);}
    |    i1=Identifier '.' i2=Identifier '.' i3=Identifier {$value=new DBField ($i1.text,$i2.text,$i3.text);}
    ;
   
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/   
   
Identifier
    :    ('a'..'z' | 'A'..'Z' | '0'..'9' | '_' | '-' | '"' | '\u0080'..'\ufffe' )+
    ;

Text
    :    '\'' ~('\'')* '\''
    ;

/* We're going to ignore all white space characters */
WS  
    :   [ \t\r\n] -> skip
    ;
