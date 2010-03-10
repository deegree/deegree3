package org.deegree.feature.utils.templating;

import java_cup.runtime.*;

/**
 * <code>TemplatingLexer</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 *
 * @version $Revision: 18644 $, $Date: 2009-07-23 13:50:17 +0200 (Thu, 23 Jul 2009) $
 */
@SuppressWarnings("all")
%%

%class TemplatingLexer
%unicode
%cupsym TemplatingSymbols
%cup
%line
%column
%public
%states TEMPLATE MAP_KEY MAP_VALUE CALL TEMPLATE_NAME LINK

%{
%}

WhiteSpace           = [ \b\f\n\r]
LetterOrDigit        = ([:letter:] | [0-9])
LetterOrDigitOrSpace = ({LetterOrDigit} | [ ])
LetterDigitSym       = {LetterOrDigit} | [-_]

%%

<YYINITIAL> {
  [<][?]                                                       { yybegin(TEMPLATE); }
  .                                                            |
  {WhiteSpace}*{LetterOrDigitOrSpace}+{WhiteSpace}*            |
  {WhiteSpace}*[<]{LetterOrDigitOrSpace}+[>]{WhiteSpace}*      |
  {WhiteSpace}*[<][/]{LetterOrDigitOrSpace}+[>]{WhiteSpace}*   |
  {WhiteSpace}+                                                { return new Symbol(TemplatingSymbols.TEXT_TOKEN, yyline, yycolumn, yytext()); }
  {WhiteSpace}*[<][/][?][>]{WhiteSpace}*                       { return new Symbol(TemplatingSymbols.END_DEFINITION_TOKEN, yyline, yycolumn); }
}

<TEMPLATE> {
  {WhiteSpace}+
              {}

  property[ ]
              { yybegin(CALL);
                return new Symbol(TemplatingSymbols.PROPERTY_CALL_TOKEN, yyline, yycolumn); }

  feature[ ]
              { yybegin(CALL);
                return new Symbol(TemplatingSymbols.FEATURE_CALL_TOKEN, yyline, yycolumn); }

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

  link
              { yybegin(LINK);
                return new Symbol(TemplatingSymbols.LINK_CALL_TOKEN, yyline, yycolumn); }

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

<LINK> {
  [:]         {}
  [:letter:]+[:][/][/]{LetterDigitSym}+([.]{LetterDigitSym}+)*([:][0-9]+)?([^:>]*)
              { return new Symbol(TemplatingSymbols.LINK_PREFIX_TOKEN, yyline, yycolumn, yytext()); }
  [^:>]+      { return new Symbol(TemplatingSymbols.LINK_TEXT_TOKEN, yyline, yycolumn, yytext()); }
  [>]         { yybegin(YYINITIAL); }
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

<TEMPLATE_NAME> {
  [^>]+                 { yybegin(TEMPLATE);
                          String s = yytext().trim();
                          if(s.startsWith("template ")) s = s.substring(9);
                          return new Symbol(TemplatingSymbols.TEMPLATE_NAME_TOKEN, yyline, yycolumn, s); }
}

<CALL> {
  not                    { return new Symbol(TemplatingSymbols.NOT_TOKEN, yyline, yycolumn); }
  [*]                    { return new Symbol(TemplatingSymbols.STAR_TOKEN, yyline, yycolumn); }
  [^ *:,()]+[*]?         { return new Symbol(TemplatingSymbols.SELECTION_TOKEN, yyline, yycolumn, yytext().trim()); }
  [*][^ *:,()]+          { return new Symbol(TemplatingSymbols.SELECTION_TOKEN, yyline, yycolumn, yytext().trim()); }
  [,]                    |
  {WhiteSpace}+          {}
  [(]                    { return new Symbol(TemplatingSymbols.LPAREN_TOKEN, yyline, yycolumn); }
  [)]                    { return new Symbol(TemplatingSymbols.RPAREN_TOKEN, yyline, yycolumn); }
  [:]                    { yybegin(TEMPLATE_NAME); }
}
