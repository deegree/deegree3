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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
    Locale loc = request.getLocale();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title>Insert title here</title>
		<link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
		<!--
		<link rel="stylesheet" type="text/css" title="blue"  href="../../javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 
        <link rel="stylesheet" type="text/css" title="gray"  href="../../javascript/ext-3.3.1/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" title="black" href="../../javascript/ext-3.3.1/resources/css/xtheme-access.css" />

        -->
        <link rel="stylesheet" href="../../css/deegree.css" type="text/css" />
       <style type="text/css">
       
			.headerModule {
				 position: absolute;
				 left: 10px;
				 top: 10px;
			}			
			
			.headerUser {
				 position: absolute;
				 left: 0px;
				 top: 260px;
			}
		
		    .folder .x-tree-node-icon{
		        background:transparent url(http://dev.sencha.com/deploy/ext-3.4.0/resources/images/default/tree/folder.gif);
		    }
		    .x-tree-node-expanded .x-tree-node-icon{
		        background:transparent url(http://dev.sencha.com/deploy/ext-3.4.0/resources/images/default/tree/folder-open.gif);
		    }
					
	    </style>
		<!--[if lt IE 7.]>
		<script defer type="text/javascript" src="../../javascript/igeoportal/pngfix.js"></script>
		<![endif]-->
		<script type="text/javascript" src="initGUI.js"></script>
		<script type="text/javascript" src="../../javascript/request_handler.js"></script>
		<script type="text/javascript" src="../../javascript/utils.js"></script>
		<script type="text/javascript" src="../../javascript/json2.js"></SCRIPT>
		<script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
		<script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>
		
		<script LANGUAGE="JavaScript1.2" TYPE="text/javascript">

		   
		   var url;		   

		   function Node(id) {
			   this.id = id;
			   this.children = new Array(); 
		   }

			function register() {
				if ( parent.controller == null ) {
					parent.controller = new parent.Controller();
					parent.controller.init();
				}
			}

			function initAdminConsole() {	
                 
                var tmp = window.location.pathname.split( '/' );
                var s = '';
                for ( var i = 0; i < tmp.length-3; i++) {
                    s += (tmp[i] + '/' ); 
                } 
                url = window.location.protocol + '//' + window.location.host + s + 'ajaxcontrol';
                parent.controller.initAdminConsole( document );       
                parent.controller.vAdminConsole.setURL( url );               
            }            

            /**
             * perform creating a new context for selected users
             */
            function saveContext() {
                // check if user has defined a name for the new context
            	var wmcName = Ext.get( 'contextNameAreaField' ).getValue();
                if ( wmcName.length == 0 ) {
                     Ext.MessageBox.show({
                         title: '<%=Messages.get( loc, "IGEO_STD_ADMIN_ERROR_HEADER" ) %>',
                         msg: '<%=Messages.get( loc, "IGEO_STD_ADMIN_ERROR_1_MSG" ) %>',
                         buttons: Ext.MessageBox.OK
                     });
                    return;
                }

                // traverse the selected modules tree and collect all selected modules
                var node = selectedModulesTree.getRootNode();
                var result = new Node( node.id );
                collect( node, result );

                // find selected users
                var selectionModel = selectedUsers.getSelectionModel();
                selectionModel.selectAll();
                var selection = selectionModel.getSelections();
                var users = new Array();
                for (var i = 0; i < selection.length; i++) {
                    users.push( selection[i].get( 'name' ) ); 
                }
                // if no user has been selected add default user
                if ( users.length == 0 ) {
                	users.push( 'default' );
                }

                // create a bean containing all required informations for creating a new context
                // for selected users
                var bean = JSON.stringify( new function() {
                    this.action = 'createWMC';
                    this.modules = result;
                    this.wmc = wmcName;
                    this.users = users;
                }, null, false );

                // send request/bean to the server part of the module
                try {
                     submitPostRequest( url, function(result) { 
                         Ext.MessageBox.show({
                             title: '<%=Messages.get( loc, "IGEO_STD_ADMIN_SUCCESS_HEADER" ) %>',
                             msg: '<%=Messages.get( loc, "IGEO_STD_ADMIN_SUCCESS_MSG" ) %>',
                             buttons: Ext.MessageBox.OK                                  
                         });

                         } , bean );                    
                } catch(e) {
                    alert( 22+ JSON.stringify( e ) );
                }
            }

            /**
            * traverse a (sub-)tree
            */
            function collect(node, result) {
            	for ( var i = 0; i < node.childNodes.length; i++ ) {                             
                    if ( node.childNodes[i].isLeaf() ) {
                        result.children.push( new Node( node.childNodes[i].id ) );
                    } else {
                    	var tmp = new Node( node.childNodes[i].id )
                    	result.children.push( tmp );
                    	collect( node.childNodes[i], tmp );
                    }
                } 
            }
        
		</script>
	</head>
	<body onload="register(); initAdminConsole(); initExtJs()">	
      <!-- Modulauswahl -->
      <div class="headerModule">
		  <h2><%=Messages.get( loc, "IGEO_STD_ADMIN_MODULE_SELECT" ) %></h2>
      <div>
      <div>
		  <table border="0" cellspacing="5" cellpadding="10">
			<tbody>
				<tr>
					<th><%=Messages.get( loc, "IGEO_STD_ADMIN_SELECT_MODULES" ) %></th>
					<th><%=Messages.get( loc, "IGEO_STD_ADMIN_AVAIL_MODULES" ) %></th>
				</tr>
				<tr height="10">
				    <td colspan="2"></td>
				</tr>
				<tr>
					<td>
						<div id="SelectedModules"></div>
					</td>
					<td>
						<div id="AvailableModules"></div>
					</td>
				</tr>
			</tbody>
		</table>
      </div>
      
      <!-- Nutzerauswahl -->
	 <div class="headerUser">
		  <h2><%=Messages.get( loc, "IGEO_STD_ADMIN_USER_SELECT" ) %></h2>
      <div>
      <table border="0" cellspacing="5" cellpadding="10">
			<tbody>
				<tr>
					<th><%=Messages.get( loc, "IGEO_STD_ADMIN_SELECTED_USERS" ) %></th>
					<th width="30">&nbsp;&nbsp;</th>
					<th><%=Messages.get( loc, "IGEO_STD_ADMIN_AVAIL_USERS" ) %></th>
				</tr>
				<tr height="10">
                    <td colspan="2"></td>
                </tr>
				<tr>
					<td valign="top">
						<div id="SelectedUsers"></div>						
					</td>
					<td valign="middle" align="center"  width="30">						
					</td>
					<td>
						<a>Nutzergruppe:</a>
						<br/>
						<div id="AvailableUserGroups"></div>
						<br/>
						<div id="AvailableUsers"></div>
					</td>
				</tr>
			</tbody>
		</table>
		<br></br>
		<div id="contextNameArea"></div>
		<br></br>
		<div id="buttonArea"></div>		
	</body>
</html>