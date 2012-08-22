<%-- $HeadURL$ --%>
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
<%@ page import="org.deegree.portal.standard.wms.control.ScaleSwitcherListener" %>
<%@ page import="org.deegree.portal.PortalException" %>
<%
    //task represents the task just executed by the listener
    String task = (String)request.getAttribute(ScaleSwitcherListener.TASK_FROM_LISTENER);
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <title>deegree iGeoPortal - scaleswitcherproxy</title>
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <script language="JavaScript1.2" type="text/javascript">

            /**
            * The method sets this document has the proxy of the HTML
            * part of the ScaleSwitcher Module
            */
            function registerProxy() {
                parent.setProxy(document);
<%
                if (task != null) {
                    out.println("updateScaleSwitcher();");
                }
%>
            }

            /**
            * The method will either update the map of igeoportal or update the
            * scaleValue depending on whether the listener calculated or new BBox or not
            */
            function updateScaleSwitcher() {
<%
                if (ScaleSwitcherListener.FULL_EXTENT.equals(task)) {////listener requested callZoomToFullExtent
                    out.println("parent.setScale(-1);");
                    //out.println("parent.callPaintNow();");
                    out.println("parent.callZoomToFullExtent();");
                } else {
                    //actual scaleValue
                    Integer scaleInteger = (Integer)request.getAttribute( ScaleSwitcherListener.SCALE_VALUE );

                    if ((scaleInteger != null) && (scaleInteger.intValue() > 0 )) {
                        out.println("var value ="+ scaleInteger.intValue()+";");
                        //out.println("alert('value='+value);");
                        out.println("parent.setScale(value);");
                    } else {
                        out.println("parent.setScale(-1);");
                    }

                    if ( ScaleSwitcherListener.NEW_BBOX.equals(task) ) { //listener calculated newBBox
                        double[] bbox = (double[])request.getAttribute( ScaleSwitcherListener.BBOX );
                        if ( bbox != null ) {
                            out.println("var minx ="+ bbox[0]+";");
                            out.println("var miny ="+ bbox[1]+";");
                            out.println("var maxx ="+ bbox[2]+";");
                            out.println("var maxy ="+ bbox[3]+";");
                            out.println("parent.callUpdateMap(minx, miny, maxx, maxy);");
                        } else {
                            //System.out.println("Unexpected: servlet bbox is null");
                            throw new PortalException("Unexpected: servlet bbox is null");
                        }
                    } else if (ScaleSwitcherListener.NEW_SCALE_VALUE.equals(task)) { //listener calculated newScaleValue
                        out.println("parent.callPaintNow();");
                    }
                }
%>
            }
        </script>
    </head>
    <body onload="registerProxy()">
        <form action="" id="form" method="post"></form>
    </body>
</html>
