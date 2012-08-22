<%-- $HeadURL$
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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UFT-8">
		<title>Timeselect</title>
		<link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
		<link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" title="blue"  href="../../javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 
        <link rel="stylesheet" type="text/css" title="gray"  href="../../javascript/ext-3.3.1/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" title="black" href="../../javascript/ext-3.3.1/resources/css/xtheme-access.css" />

        <script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>
		<script LANGUAGE="JavaScript1.2" TYPE="text/javascript">
		
		    function register() {
                if ( parent.controller == null ) {
                    parent.controller = new parent.Controller();
                    parent.controller.init();
                }
            }

            function initTimeselect() {
                parent.controller.initTimeselect(document);
                initGUI();
            }

            /**
             * initializing ExtJS GUI elements. At the moment just Ext.Button elements will be used
             * In future version this dialog will be completly reimplemented as an ExtJS to avoid
             * opening an additional browser window
             */
             function initGUI() {
                  Ext.QuickTips.init();   
                  new Ext.Button({
                      tooltip: 'open time selection form',
                      text: 'select time',
                      renderTo: 'buttonArea0',
                      icon: '../../images/timeselect.gif',
                      width: 100,
                      height: 25,
                      handler: function(toggled){                               
                    	  openTimeselect();
                      }
                  });           
               }

            function openTimeselect() {
                window.open( "select_timestamp.jsp", "sqlselect", "width=300,height=255,left=150,top=200,scrollbars=no,resizable=no" );
            }
		</script>
	</head>
	<body class="pTimeSelect" onload="register(); initTimeselect();">
	   <div id="buttonArea0"></div>
	</body>
</html>