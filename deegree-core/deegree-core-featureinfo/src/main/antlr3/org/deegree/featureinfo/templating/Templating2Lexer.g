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

fragment Letter: { Character.isLetter( input.LA(1) ) }? .;
fragment Digit: { Character.isDigit( input.LA(1) ) }? .;
fragment WS: { Character.isWhitespace( input.LA(1) ) }? .;
fragment Point: '.';
fragment Slash: '/';

//fragment UrlText: ':' ~(Colon | TagClose | '\n')+;
//UrlWithPort: Url Colon Digit+ ~(Colon | TagClose | '\n')+ UrlText?;
Url: (ID Colon Slash Slash ~(TagClose)+);

Equals: '=';
Star: '*';
Comma: ',';
Colon: ':';
TagOpen: '<';
TagClose: '>';
MapSpace: 'map ';
Not: 'not';
BracketLeft: '(';
BracketRight: ')';

Kvp: '\n'* (~('=' | '<' | '>' | '?' | '/' | '\n'))+ '=' (~('\n'))+ '\n'*;
ID: (Letter | Digit | '_' | '-')+;

Rest: ~('<')+;
