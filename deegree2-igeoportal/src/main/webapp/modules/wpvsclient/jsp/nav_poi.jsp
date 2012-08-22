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
<%
    String directions = request.getParameter( "directionlabels" );
    String[] dirs = directions.split( "," );
%>
<table cellpadding="0" cellspacing="0">
    <tr>
        <td colspan="5" align="center"><%=dirs[0]%><br />
            <a href="javascript:changePOI('UP')"> <img src="./images/north_direction.gif" border="0" /> </a>
        </td>
    </tr>
    <tr height="40px">
        <td width="42px" align="right" valign="middle"><%=dirs[3]%>&nbsp;</td>
        <td width="42px" align="left">
            <a href="javascript:changePOI('LEFT')"><img src="./images/west_direction.gif" align="middle" border="0" /></a>
        </td>
        <td width="42px" align="center" width="50px">&nbsp;</td>
        <td width="42px" align="right">
            <a href="javascript:changePOI('RIGHT')"><img src="./images/east_direction.gif" align="middle" border="0" /></a>
        </td>
        <td width="42px" align="left" valign="middle">&nbsp;<%=dirs[1]%></td>
    </tr>
    <tr>
        <td colspan="5" align="center">
            <a href="javascript:changePOI('DOWN')"><img src="./images/south_direction.gif" border="0" /></a>
            <br /><%=dirs[2]%>
        </td>
    </tr>
</table>
