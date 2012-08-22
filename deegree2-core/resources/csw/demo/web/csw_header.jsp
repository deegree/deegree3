<%-- $HeadURL$ --%>
<%-- $Id$ --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.deegree.framework.version.*" %>
<html>
    <head>
        <title>deegree <%=Version.getVersionNumber()%> catalogueManager</title>
        <link href="./css/metadataclient.css"  type="text/css" rel="stylesheet">
        <style>
            .header {
                position: absolute;
			    top: 10px;
			    left: 10px;
			    height: 60px;
			    width: 95%;
			    z-index: 1;
			    opacity: .90;
			    filter: alpha(opacity = 90);
			    -moz-opacity: 0.9;
			    background-color: #FFFFFF;
			    visibility: visible;
			    font-size: 10pt;
			    color: #555555;
            }   
            .header h2 {
                font-size: 16pt;
                margin-top: 20px;
            }
        </style>    
    </head>
    <body> 
        <div class="header">   
	        <table width="100%" border="0" cellpadding="0" cellspacing="0" summary="">
	            <tr>
	                <td style="width:270px"><img border="0" src="./images/logo-deegree.png" alt="logo"/></td>
	                <td><h2>catalogueManager</h2></td>
	                <td align="right" style="width:210px">
	                    <a href="http://www.osgeo.org/deegree" target="_blank"><img src="images/OSGeo_project.png" height="85" width="200" alt="The OSGeo Foundation" border="0"/></a>&nbsp;
	                </td>
	             </tr>
	        </table>
        </div>
    </body>
</html>
