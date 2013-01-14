parser grammar Templating2Parser;

options {
  tokenVocab = Templating2Lexer;
}

@parser::header {
package org.deegree.featureinfo.templating;

import org.deegree.featureinfo.templating.lang.*;
import java.util.HashMap;
}

definitions returns [HashMap<String, Definition> definitions]
@init {
  $definitions = new HashMap<String, Definition>();
}:
  definition[$definitions]+ EOF
  ;

definition[HashMap<String, Definition> defs]:
  template[$defs] ExplicitTemplateEnd?
  | map[$defs] ExplicitTemplateEnd?
  ;

template[HashMap<String, Definition> defs]
@init {
  TemplateDefinition templdef = new TemplateDefinition();
}:
  TemplateDefinitionStart n = ID TagClose templatebody[templdef]+ { $defs.put($n.text, templdef); templdef.name = $n.text; };

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
  t = Rest { $text = $t.text; }
  | t = ID { $text = $t.text; }
  | t = WS { $text = $t.text; }
  | t = TagClose { $text = $t.text; }
  | t = TagOpen { $text = $t.text; }
  | t = Star { $text = $t.text; }
  | t = Colon { $text = $t.text; }
  | t = KvpLeft { $text = $t.text; }
  | t = KvpRight { $text = $t.text; }
  | t = Equals { $text = $t.text; }
  | t = BracketLeft { $text = $t.text; }
  | t = BracketRight { $text = $t.text; }
  ;

map[HashMap<String, Definition> defs]
@init {
  MapDefinition mapdef = new MapDefinition();
}:
  MapDefinitionStart n = ID TagClose kvp[mapdef.map]+ { $defs.put($n.text, mapdef); mapdef.name = $n.text; }
  ;

kvp[HashMap<String, String> map]:
  l = KvpLeft+ Equals r = KvpRight+ NewLine { $map.put($l.text.trim(), $r.text.trim()); };

featurecall:
  FeatureCallStart templateselector Colon ID TagClose;

propertycall:
  PropertyCallStart templateselector Colon ID TagClose;

templateselector:
  Not WS* BracketLeft templateselector BracketRight
  | Star (WS* ID WS*)?
  | WS* ID WS* (Comma templateselector)?
  ;

name returns [Object name]:
  NameStart TagClose { $name = new Name(); }
  | NameStart Colon MapSpace n = ID TagClose { $name = new MapCall($n.text, MapCall.Type.Name); };

value returns [Object value]:
  ValueStart TagClose { $value = new Value(); }
  | ValueStart Colon MapSpace n = ID TagClose { $value = new MapCall($n.text, MapCall.Type.Value); };

odd returns [OddEven even]:
  OddStart Colon n = ID TagClose { $even = new OddEven($n.text, true); };

even returns [OddEven even]:
  EvenStart Colon n = ID TagClose { $even = new OddEven($n.text, false); };

link returns [Link link]:
  LinkStart Colon pref = url text = (Colon ~(Colon | TagClose)+)? TagClose
  {
    if($text == null) $link = new Link(pref);
    else $link = new Link(pref, $text.text);
  }
  ;

url returns [String url]:
  val = Url { $url = $val.text; };

index returns [Index index]:
  Index { $index = new Index(); };

gmlid returns [GMLId gmlId]:
  GmlId { $gmlId = new GMLId(); };
