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
  | featurecall { $def.body.add($featurecall.call); }
  | propertycall { $def.body.add($propertycall.call); }
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
  | t = Kvp { $text = $t.text; }
  | t = Equals { $text = $t.text; }
  | t = Comma { $text = $t.text; }
  | t = Slash { $text = $t.text; }
  | t = Point { $text = $t.text; }
  | t = Url { $text = $t.text; }
  | t = BracketLeft { $text = $t.text; }
  | t = BracketRight { $text = $t.text; }
  ;

map[HashMap<String, Definition> defs]
@init {
  MapDefinition mapdef = new MapDefinition();
}:
  MapDefinitionStart n = ID TagClose kvp[mapdef.map]+ WS* { $defs.put($n.text, mapdef); mapdef.name = $n.text; }
  ;

kvp[HashMap<String, String> map]:
  k = Kvp { String[] ss = $k.text.trim().split("="); $map.put(ss[0], ss[1]); };

featurecall returns [FeatureTemplateCall call]
@init {
  List<String> patterns = new ArrayList<String>();
}:
  FeatureCallStart templateselector[patterns] Colon ID TagClose { $call = new FeatureTemplateCall($ID.text, patterns, $templateselector.negate); };

propertycall returns [PropertyTemplateCall call]
@init {
    List<String> patterns = new ArrayList<String>();
}:
  PropertyCallStart templateselector[patterns] Colon ID TagClose { $call = new PropertyTemplateCall($ID.text, patterns, $templateselector.negate); };

templateselector[List<String> patterns] returns [Boolean negate]:
  Not WS* BracketLeft templatepatterns[patterns] BracketRight { $negate = true; }
  | templatepatterns[patterns] { $negate = false; }
  ;

templatepatterns[List<String> patterns]:
  Star (WS* p = ID WS*)? (Comma templatepatterns[patterns])? { if($p != null) patterns.add("*" + $p.text); else patterns.add("*"); }
  | (WS* p = ID WS*) Star (Comma templatepatterns[patterns])? { patterns.add($p.text + "*"); }
  | WS* p = ID WS* (Comma templatepatterns[patterns])? { patterns.add($p.text); }
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
  LinkStart Colon pref = url TagClose
  {
    if($pref.text == null) $link = new Link($pref.url);
    else $link = new Link($pref.url, $pref.text);
  }
  ;

url returns [String url, String text]:
  val = Url {
            $url = $val.text;
            $text = $url.substring($url.lastIndexOf(":") + 1);
            $url = $url.substring(0, $url.lastIndexOf(":"));
            if($text.trim().isEmpty()) $text = null;
        };

index returns [Index index]:
  Index { $index = new Index(); };

gmlid returns [GMLId gmlId]:
  GmlId { $gmlId = new GMLId(); };
