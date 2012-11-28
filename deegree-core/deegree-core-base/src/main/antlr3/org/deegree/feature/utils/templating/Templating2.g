grammar Templating2;

@parser::header {
package org.deegree.feature.utils.templating;

import org.deegree.feature.utils.templating.lang.*;
import java.util.Map;
import java.util.HashMap;
}

@lexer::header {
package org.deegree.feature.utils.templating;
}

definitions returns [Map<String, Definition> definitions]
@init {
  $definitions = new HashMap<String, Definition>();
}:
  definition[$definitions]+ EOF
  ;

definition[Map<String, Definition> defs]:
  template[$defs] ExplicitTemplateEnd?
  | map[$defs] ExplicitTemplateEnd?
  ;

template[Map<String, Definition> defs]
@init {
  TemplateDefinition templdef = new TemplateDefinition();
}:
  SpecialConstructStart 'template ' n = Name '>' templatebody[templdef]+ { $defs.put($n.text, templdef); templdef.name = $n.text; };

templatebody[TemplateDefinition def]:
  templatebodytext { $def.body.add($templatebodytext.text); }
  | featurecall
  | propertycall
  | name { $def.body.add($name.name); }
  | value { $def.body.add($value.value); }
  | odd { $def.body.add($odd.even); }
  | even { $def.body.add($even.even); }
  | link { $def.body.add($link.link); }
  | index { $def.body.add($index.index); }
  | gmlid { $def.body.add($gmlid.gmlId); }
  ;

templatebodytext returns [String text]:
  t = ValidChars { $text = $t.text; }
  | t = Name { $text = $t.text; }
  | t = WS { $text = $t.text; }
  ; 

map[Map<String, Definition> defs]
@init {
  MapDefinition mapdef = new MapDefinition();
}:
  SpecialConstructStart 'map ' n = Name '>' kvp[mapdef.map]+ { $defs.put($n.text, mapdef); mapdef.name = $n.text; }
  ;

kvp[Map<String, String> map]:
  l = KvpLeft+ '=' r = KvpRight+ NewLine { $map.put($l.text.trim(), $r.text.trim()); };

featurecall:
  SpecialConstructStart 'feature ' templateselector ':' Name '>';

propertycall:
  SpecialConstructStart 'property ' templateselector ':' Name '>';

templateselector:
  'not' WS* '(' templateselector ')'
  | '*' WS* Name+ WS*
  | WS* Name+ WS* ',' templateselector
  ;

name returns [Object name]:
  SpecialConstructStart 'name>' { $name = new Name(); }
  | SpecialConstructStart 'name:map' n = Name '>' { $name = new MapCall($n.text, MapCall.Type.Name); };

value returns [Object value]:
  SpecialConstructStart 'value>' { $value = new Value(); }
  | SpecialConstructStart 'value:map ' n = Name '>' { $value = new MapCall($n.text, MapCall.Type.Value); };

odd returns [OddEven even]:
  SpecialConstructStart 'odd:' n = Name '>' { $even = new OddEven($n.text, true); };

even returns [OddEven even]:
  SpecialConstructStart 'even:' n = Name '>' { $even = new OddEven($n.text, false); };

link returns [Link link]:
  SpecialConstructStart 'link:' pref = url text = (':' ~(':' | '>')+)? '>'
  {
    if($text == null) $link = new Link(pref);
    else $link = new Link(pref, $text.text);
  }
  ;

url returns [String url]:
  val = (Letter '://' (Name | '/' | '.')+) { $url = $val.text; };

index returns [Index index]:
  SpecialConstructStart 'index>' { $index = new Index(); };

gmlid returns [GMLId gmlId]:
  SpecialConstructStart 'gmlid>' { $gmlId = new GMLId(); };

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
WS: (' ' | '\t' | '\r' | '\n' | '\f' | '\b');
ValidChars: '<' ~'?' | ~'<'; 
