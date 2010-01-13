package org.deegree.commons.utils.templating;

import java_cup.runtime.*;

/**
 * <code>TemplatingLexer</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 *
 * @version $Revision: 18644 $, $Date: 2009-07-23 13:50:17 +0200 (Thu, 23 Jul 2009) $
 */
%%

%class TemplatingLexer
%unicode
%cupsym TemplatingSymbols
%cup
%line
%column
%public
%states TEMPLATE MAP_KEY MAP_VALUE

%{
%}

WhiteSpace           = [ \b\f\n\r]
LetterOrDigit        = ([:letter:] | [0-9])
LetterOrDigitOrSpace = ({LetterOrDigit} | [ ])

%%

<YYINITIAL> {
  [<][?]                                    { yybegin(TEMPLATE); }
  .                                         |
  {LetterOrDigitOrSpace}+                   |
  [<][:letter:]+[>]                         |
  [<][/][:letter:]+[>]                      |
  {WhiteSpace}+                             { return new Symbol(TemplatingSymbols.TEXT_TOKEN, yyline, yycolumn, yytext()); }
  {WhiteSpace}*[<][/][?][>]{WhiteSpace}*    { return new Symbol(TemplatingSymbols.END_DEFINITION_TOKEN, yyline, yycolumn); }
}

<TEMPLATE> {
  {WhiteSpace}+
              {}

  property[ ]!([^]* ([:]?{LetterOrDigit}+[>]) [^]*) [:]{LetterOrDigit}+
              { String s = yytext().trim().substring(9);
                return new Symbol(TemplatingSymbols.PROPERTY_TEMPLATE_CALL_TOKEN, yyline, yycolumn, s.split(":", 2)); }

  feature[ ]!([^]* ([:]?{LetterOrDigit}+[>]) [^]*) [:](template[ ])?{LetterOrDigit}+
              { String s = yytext().trim().substring(8);
                String[] ss = s.split(":", 2);
                if(ss[1].startsWith("template ")) ss[1] = ss[1].substring(9);
                return new Symbol(TemplatingSymbols.FEATURE_TEMPLATE_CALL_TOKEN, yyline, yycolumn, ss); }

  template[ ]{LetterOrDigit}+
              { String s = yytext().trim().substring(9);
                return new Symbol(TemplatingSymbols.TEMPLATE_DEFINITION_TOKEN, yyline, yycolumn, s); }

  (name | value) [:]map[ ]{LetterOrDigit}+
              { String s = yytext().trim();
                String[] vals = s.split(":", 2);
                vals[1] = vals[1].substring(4);
                return new Symbol(TemplatingSymbols.MAP_CALL_TOKEN, yyline, yycolumn, vals); }

  odd [:]{LetterOrDigit}+
              { String s = yytext().trim().substring(4);
                return new Symbol(TemplatingSymbols.ODD_CALL_TOKEN, yyline, yycolumn, s); }

  even [:]{LetterOrDigit}+
              { String s = yytext().trim().substring(5);
                return new Symbol(TemplatingSymbols.EVEN_CALL_TOKEN, yyline, yycolumn, s); }

  value[:]link
              { return new Symbol(TemplatingSymbols.LINK_CALL_TOKEN, yyline, yycolumn); }

  index
              { return new Symbol(TemplatingSymbols.INDEX_CALL_TOKEN, yyline, yycolumn); }

  map[ ]{LetterOrDigit}+
              { yybegin(MAP_KEY);
                String s = yytext().trim().substring(4);
                return new Symbol(TemplatingSymbols.MAP_DEFINITION_TOKEN, yyline, yycolumn, s); }

  name
              { return new Symbol(TemplatingSymbols.NAME_TOKEN, yyline, yycolumn); }

  value
              { return new Symbol(TemplatingSymbols.VALUE_TOKEN, yyline, yycolumn); }

  gmlid
              { return new Symbol(TemplatingSymbols.GMLID_TOKEN, yyline, yycolumn); }

  [>]
              { yybegin(YYINITIAL); }
}

<MAP_KEY> {
  [>]                                     {}
  {WhiteSpace}+                           {}
  [<][?]                                  { yybegin(TEMPLATE); }
  [^<>=\n][^=]*                           { return new Symbol(TemplatingSymbols.MAP_KEY_TOKEN, yyline, yycolumn, yytext().trim()); }
  [=]                                     { yybegin(MAP_VALUE); }
  {WhiteSpace}*[<][/][?][>]{WhiteSpace}*  { yybegin(YYINITIAL); return new Symbol(TemplatingSymbols.END_DEFINITION_TOKEN, yyline, yycolumn); }
}

<MAP_VALUE> {
  {WhiteSpace}+         {}
  .*                    { yybegin(MAP_KEY); return new Symbol(TemplatingSymbols.MAP_VALUE_TOKEN, yyline, yycolumn, yytext().trim()); }
}
