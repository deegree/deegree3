lexer grammar Templating2Lexer;

options {
  filter = true;
}

@lexer::header {
package org.deegree.featureinfo.templating;
}

TemplateDefinitionStart: '<?template ';
MapDefinitionStart: '<?map ';
FeatureCallStart: '<?feature ';
PropertyCallStart: '<?property ';
NameStart: '<?name';
ValueStart: '<?value';
OddStart: '<?odd';
EvenStart: '<?even';
LinkStart: '<?link';
Index: '<?index>';
GmlId: '<?gmlid>';
ExplicitTemplateEnd: '</?>';

fragment Letter: ('a'..'z' | 'A'..'Z');
fragment Digit: ('0'..'9');
fragment WS: ' ' | '\n' | '\t' | '\r' | '\f';

Url: (Letter+ '://' (ID | '/' | '.')+);

Equals: '=';
Star: '*';
Comma: ',';
Colon: ':';
TagOpen: '<';
TagClose: '>';
Map: 'map ';
Not: 'not';
BracketLeft: '(';
BracketRight: ')';

ID: (Letter | Digit | '_')+;
KvpLeft: (~('=' | '<' | '>' | '?' | '/' | '\n'))+;
KvpRight: (~('\n'))+;

Rest: ~('<')+;
