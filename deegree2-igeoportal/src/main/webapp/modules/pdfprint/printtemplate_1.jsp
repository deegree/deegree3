<%-- $HeadURL$ --%>
<%-- $Id$ --%>
<%-- 
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
        <title>t1</title>
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
        <script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>
        <script type="text/javascript" charset="UTF-8">
        <!--

            function init() {
                parent.txtDoc = document;
            }

	        /**
	         * initializing ExtJS GUI elements. At the moment just Ext.Button elements will be used
	         * In future version this dialog will be completly reimplemented as an ExtJS to avoid
	         * opening an additional browser window
	         */
	         function initGUI() {
	              Ext.QuickTips.init();   
	              new Ext.Button({
	                  tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_PRINTPDF" ) %>',
	                  text: '<%=Messages.get( loc, "IGEO_STD_BTN_PRINTPDF" ) %>',
	                  renderTo: 'buttonArea2',
	                  width: 150,
	                  height: 25,
	                  handler: function(toggled){                               
	                	  printMap(true);
	                  }
	              });
	              
	              new Ext.Button({
	                  tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_DWNLDIMAGE" ) %>',
	                  text: '<%=Messages.get( loc, "IGEO_STD_BTN_DWNLDIMAGE" ) %>',
	                  renderTo: 'buttonArea1',
	                  width: 150,
	                  height: 25,
	                  handler: function(toggled){                               
	                	  printMap(false);
	                  }
	              });

	              new Ext.Button({
                      tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_CLOSE" ) %>',
                      text: '<%=Messages.get( loc, "IGEO_STD_BTN_CLOSE" ) %>',
                      renderTo: 'buttonArea0',
                      width: 150,
                      height: 25,
                      handler: function(toggled){                               
                    	  parent.close();
                      }
                  });
	              
	         }

            function printMap( asPDF ) {
                var el = document.getElementsByTagName( 'textarea' );
                var a = new Array();
                var k = 0;
                for ( var i = 0; i < el.length; i++) {
                    a[k] = new Array(2);
                    a[k][0] = el[i].name;
                    a[k++][1] = el[i].value;
                }
                parent.printMap( a , asPDF );
            }

        // -->
        </script>
    </head>
    <body onload="init(); initGUI();" class="pPrintDialog">
        <table width="200">
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td width="10">&nbsp;</td>
                <td>
                    <%=Messages.get( loc, "IGEO_STD_PDF_TMPLT_TITLE" ) %><br>
                    <textarea id="T1" name="TITLE"><%=Messages.get( loc, "IGEO_STD_PDF_TMPLT_TITLE_INP" ) %></textarea>
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td width="10">&nbsp;</td>
                <td>
                    <%=Messages.get( loc, "IGEO_STD_PDF_TMPLT_AUTHOR" ) %><br>
                    <textarea id="T2" name="AUTHOR"><%=Messages.get( loc, "IGEO_STD_PDF_TMPLT_AUTHOR_INP" ) %></textarea>
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td width="10">&nbsp;</td>
                <td>
                    <%=Messages.get( loc, "IGEO_STD_PDF_TMPLT_DESCRIPTION" ) %><br>
                    <textarea id="T3" name="DESCRIPTION"><%=Messages.get( loc, "IGEO_STD_PDF_TMPLT_DESCRIPTION_INP" ) %></textarea>
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
            </tr>
            <tr>
                <td width="10">&nbsp;</td>
                <td>
                    <div id="buttonArea2"></div>
                    <br>
                    <div id="buttonArea1"></div>
                    <br>
                    <div id="buttonArea0"></div>
                </td>
            </tr>
        </table>
    </body>
</html>
