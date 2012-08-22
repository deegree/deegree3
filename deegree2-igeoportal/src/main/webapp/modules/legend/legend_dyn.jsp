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
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String legendURL = (String)request.getAttribute( "LEGENDURL" );
	String width = (String)request.getAttribute( "LEGENDWIDTH" );
	String height = (String)request.getAttribute( "LEGENDHEIGHT" );
%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="no-cache">
		<title>Insert title here</title>
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
		<script type="text/javascript" src="../../javascript/model/wmsrequestfactory.js"></script>
		<script type="text/javascript" src="../../javascript/utils.js"></script>
		<script type="text/javascript">
		<!--
			var url; 

			function register() {
				if ( parent.controller == null ) {
					parent.controller = new parent.Controller();
					parent.controller.init();
				}
			}

			function initLegend() {	
                parent.controller.initLegend( document );        
                var tmp = window.location.pathname.split( '/' );
                var s = '';
                for ( var i = 0; i < tmp.length-3; i++) {
                    s += (tmp[i] + '/' ); 
                } 
                url = window.location.protocol + '//' + window.location.host + s + 'control';                 
            }
			
			
			function paintLegend() {
<%
                if ( legendURL == null ) {
%>	    
                    var frm = getElement( 'LEGENDFRM' );
                    var hidden = document.getElementById( "dyn_rpc" );
                    hidden.value = parent.controller.vLegend.getRPCValues();
                    frm.action = url;    
                    frm.submit();
<%	    
                }
%>			
			}
                       
		// -->
		</script>
	</head>
    <body class="pLegendDyn" leftmargin="0" topmargin="0" onload="register(); initLegend(); paintLegend();">
        <form id="LEGENDFRM" name="legendform" method="post">
            <input type="hidden" id="dyn_rpc" name="rpc" />
        </form>
<%
        if ( legendURL != null ) {
%>	    
            <img src="<%=legendURL %>" border="0" width="<%=width %>" height="<%=height %>" /> 
<%	    
        }
%>	
    </body>
</html>