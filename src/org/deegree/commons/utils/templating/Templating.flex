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
%states TEMPLATE

%{
%}

WhiteSpace           = [ \b\f\n\r]
LetterOrDigit        = ([:letter:] | [0-9])
LetterOrDigitOrSpace = ({LetterOrDigit} | [ ])

%%

<YYINITIAL> {
  [<][?]                       { yybegin(TEMPLATE); }
  .                            |
  {LetterOrDigitOrSpace}+      |
  [<][:letter:]+[>]            |
  [<][/][:letter:]+[>]         |
  {WhiteSpace}+                { return new Symbol(TemplatingSymbols.TEXT_TOKEN, yyline, yycolumn, yytext()); }
}

<TEMPLATE> {
  {WhiteSpace}+                                                                      {}
  [^>] !([^]* ([:]?{LetterOrDigit}+[>]) [^]*) ([:]{LetterOrDigit}+[>])               { yybegin(YYINITIAL); String s = yytext().trim(); s = s.substring(0, s.length() - 1); return new Symbol(TemplatingSymbols.TEMPLATE_CALL_TOKEN, yyline, yycolumn, s); }
  {LetterOrDigit}+                                                                   { return new Symbol(TemplatingSymbols.TEMPLATE_DEFINITION_TOKEN, yyline, yycolumn, yytext().trim()); }
  [^>] !([^]* ([:]?{LetterOrDigitOrSpace}+[>]) [^]*) ([:]{LetterOrDigitOrSpace}+[>]) { yybegin(YYINITIAL); String s = yytext().trim(); s = s.substring(0, s.length() - 1); return new Symbol(TemplatingSymbols.MAP_CALL_TOKEN, yyline, yycolumn, s); }
  {LetterOrDigitOrSpace}+                                                            { return new Symbol(TemplatingSymbols.MAP_DEFINITION_TOKEN, yyline, yycolumn, yytext().trim()); }
  [>]                                                                                { yybegin(YYINITIAL); }
}
