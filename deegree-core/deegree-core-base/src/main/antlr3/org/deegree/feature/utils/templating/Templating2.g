grammar Templating2;

@parser::header {
package org.deegree.feature.utils.templating;
}

@lexer::header {
package org.deegree.feature.utils.templating;
}

definitions: definition+;

definition:
  template ExplicitTemplateEnd?
  | map ExplicitTemplateEnd?
  ;

template: SpecialConstructStart 'template ' Name '>' templatebody+;

templatebody:
   templatebodytext
  | featurecall
  | propertycall
  | name
  | value
  | odd
  | even
  | link
  | index
  | gmlid
  ;

templatebodytext: (Letter | Digit | Sym); 

map: 
  SpecialConstructStart 'map ' Name '>' kvp+;

kvp: KvpLeft+ '=' KvpRight+ NewLine;

featurecall: SpecialConstructStart 'feature ' templateselector ':' Name '>';

propertycall: SpecialConstructStart 'property ' templateselector ':' Name '>';

templateselector:
  'not' WS* '(' templateselector ')'
  | '*' WS* Name+ WS*
  | WS* Name+ WS* ',' templateselector
  ;

name:
  SpecialConstructStart 'name>' 
  | SpecialConstructStart 'name:map' Name '>';

value:
  SpecialConstructStart 'value>' 
  | SpecialConstructStart 'value:map ' Name '>';

odd: SpecialConstructStart 'odd:' Name '>';

even: SpecialConstructStart 'even:' Name '>';

link:
  SpecialConstructStart 'link:' url (':' ~(':' | '>')+)? '>';

url: Letter '://' (Name | '/' | '.')+;

index: SpecialConstructStart 'index>';

gmlid: SpecialConstructStart 'gmlid>';

// Lexer rules
fragment Letter: ('a'..'z' | 'A'..'Z');
fragment Digit: ('0'..'9');
fragment NewLine: ('\n');
fragment TemplateEnd: SpecialConstructStart | ExplicitTemplateEnd;
fragment KvpLeft: (~('=' | '<' | '>' | '?' | '/'));
fragment KvpRight: (~('\n'));

SpecialConstructStart: '<?';
ExplicitTemplateEnd: '</?>';
Name: (Letter | Digit | '_')+;
Colon: ':';
Sym: '<' | '>' | Colon | '?';
WS: (' ' | '\t' | '\r' | '\n');
