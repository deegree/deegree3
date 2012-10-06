package org.deegree.protocol.wms.dims;

import java_cup.runtime.*;

/**
 * <code>DimensionLexer</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 *
 * @version $Revision: 18644 $, $Date: 2009-07-23 13:50:17 +0200 (Thu, 23 Jul 2009) $
 */
%%

%class DimensionLexer
%unicode
%cup
%line
%column
%public

%{
%}

InputCharacters = [^\r\n\t\f ,/]+
WhiteSpace     = [ \t\f\r\n]+

%state STRING

%%

<YYINITIAL> {
  {InputCharacters}            { return new Symbol(sym.TOKEN, yyline, yycolumn, yytext()); }
  {WhiteSpace}                 { }
  ","                          { return new Symbol(sym.COMMA, yyline, yycolumn); }
  "/"                          { return new Symbol(sym.SLASH, yyline, yycolumn); }
}
