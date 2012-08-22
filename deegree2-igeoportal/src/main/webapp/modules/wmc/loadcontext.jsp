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
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <title>deegree iGeoPortal</title>
        <link href="./css/deegree.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="./javascript/ext-3.3.1/resources/css/ext-all.css" />
        <script type="text/javascript" src="./javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="./javascript/ext-3.3.1/ext-all.js"></script>
        <script language="JavaScript1.2" type="text/javascript">
        <!--
<%
            List contextList = (List)request.getAttribute( "CONTEXT_LIST" );
            String user = (String)request.getAttribute( "USER" );
            String startContext = (String)request.getAttribute( "STARTCONTEXT" );
%>

			/**
			 * initializing ExtJS GUI elements. At the moment just Ext.Button elements will be used
			 * In future version this dialog will be completly reimplemented as an ExtJS to avoid
			 * opening an additional browser window
			 */
			 function initGUI() {
			      Ext.QuickTips.init();   
			      new Ext.Button({
			          tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_LOAD_TT" ) %>',
			          text: '<%=Messages.get( loc, "IGEO_STD_BTN_LOAD" ) %>',
			          renderTo: 'buttonArea0',
			          width: 100,
			          height: 25,
			          handler: function(toggled){                               
			        	  switchContext();
			          }
			      });
			      
			      new Ext.Button({
			          tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_DELETE_CNTXT_TT" ) %>',
			          text: '<%=Messages.get( loc, "IGEO_STD_BTN_DELETE_CNTXT" ) %>',
			          renderTo: 'buttonArea1',
			          width: 100,
			          height: 25,
			          handler: function(toggled){                               
			        	  deleteContext();
			          }
			      });

			      new Ext.Button({
                      tooltip: '<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL_TT" ) %>',
                      text: '<%=Messages.get( loc, "IGEO_STD_BTN_CANCEL" ) %>',
                      renderTo: 'buttonArea2',
                      width: 100,
                      height: 25,
                      handler: function(toggled){                               
                    	  window.close();
                      }
                  });   			      
			 }

            function switchContext() {
                var sel = document.getElementById('selid');
                if ( sel.selectedIndex != 0 ) {
                    var req = "control?rpc=<?xml version='1.0' encoding='UTF-8'?><methodCall>" +
                              "<methodName>mapClient:contextSwitch</methodName><params><param><value><struct>" +
                              "<member><name>mapContext</name><value><string>" + encodeURIComponent(sel.value) + "</string></value></member>" +
                              "</struct></value></param></params></methodCall>";
                    opener.parent.window.location.replace(req);
                    window.close();
                }
            }

            function deleteContext() {
                var sel = document.getElementById('selid');
                var startpos_contextname = sel.value.lastIndexOf('/') + 1;
                var contextname = sel.value.substring(startpos_contextname, sel.value.length)
                
                // only registered and logged in users are alowed to delet their own contexts
                if ( opener.parent.controller.vSessionKeeper == null || 
                     ( opener.parent.controller.vSessionKeeper != null && opener.parent.controller.vSessionKeeper.id == null ) ) {
                    alert( '<%=Messages.get( loc, "IGEO_STD_CNTXT_LOAD_MSG_LOGIN" ) %>' );
                    return;
                }
                
                if ( sel.selectedIndex != 0 ) {
                    var userStartContext = "<%=startContext %>";
                    var sessionID = opener.parent.controller.vSessionKeeper.id;
                    if ( contextname == userStartContext ) {
                        alert( '<%=Messages.get( loc, "IGEO_STD_CNTXT_LOAD_MSG_NO_DEL1") %> ' +   contextname + ' <%=Messages.get( loc, "IGEO_STD_CNTXT_LOAD_MSG_NO_DEL2") %>' );
                        return;
                    }
                    if ( !window.confirm( '<%=Messages.get( loc, "IGEO_STD_CNTXT_LOAD_MGS_CNFRM1" ) %> ' + contextname + '<%=Messages.get( loc, "IGEO_STD_CNTXT_LOAD_MGS_CNFRM2" ) %> '  ) ) {
                        return;
                    }
                    var req= "control?rpc=<?xml version='1.0' encoding='ISO-8859-1'?><methodCall>" +
                        "<methodName>mapClient:deleteContext</methodName><params><param><value><struct>"+
                        "<member><name>mapContext</name><value><string>" + sel.value + "</string></value></member>" +
                        "<member><name>sessionID</name><value><string>" + sessionID + "</string></value></member>" +
                        "</struct></value></param></params></methodCall>";
                    window.location.replace( req );
                } else {
                    alert( '<%=Messages.get( loc, "IGEO_STD_CNTXT_LOAD_MSG_SELECTWMC" ) %>' );
                }
            }
            // -->
        </script>
    </head>
    <body class="pLoadContext" onload="initGUI()">
        <!-- HEADER AREA -->
        <table>
            <tbody>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td class="header"><%=Messages.get( loc, "IGEO_STD_CNTXT_LOAD_HEADER" ) %></td>
                </tr>
            </tbody>
        </table>
        <form action="javascript:switchContext();">
            <!-- TEXT AREA -->
            <table>
                <tbody>
                    <tr>
                        <td colspan="3">&nbsp;</td>
                    </tr>
                    <tr>
                        <td width="30">&nbsp;</td>
                        <td class="text"><%=Messages.get( loc, "IGEO_STD_CNTXT_LOAD_TXT" ) %></td>
                        <td width="30">&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="3">&nbsp;</td>
                    </tr>
                    <tr>
                        <td width="30">&nbsp;</td>
                        <td>
                            <select id="selid">
                                <option value="dummy"><%=Messages.get( loc, "IGEO_STD_CNTXT_LOAD_SELECT" ) %></option>
<%
                                String path2Dir = "users/" + user + "/";
                                for ( Iterator iter = contextList.iterator(); iter.hasNext(); ) {
                                    String s = (String) iter.next();
                                    String name = s;
                                    if ( s.lastIndexOf( "." ) > 0 ) {
                                        name = s.substring( 0,  s.lastIndexOf( "." ) );
                                    }
                                    out.println("<option value=\""+ path2Dir + s +"\">"+ name +"</option>" );
                                }
%>
                            </select>
                        </td>
                        <td width="30">&nbsp;</td>
                    </tr>
                </tbody>
            </table>
            <!-- BUTTON AREA -->
            <br /><br />
            <table>
                <tr>
                    <td width="30">&nbsp;</td>
                    <td>
                        <div id="buttonArea0"></div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div id="buttonArea1"></div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div id="buttonArea2"></div>
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html>
