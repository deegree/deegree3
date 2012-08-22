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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.deegree.ogcwebservices.wpvs.capabilities.WPVSCapabilities"%>
<%@ page import="org.deegree.portal.common.WPVSClientConfig"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
        //import="org.deegree.ogcwebservices.wpvs.capabilities.WPVSCapabilitiesDocument"
        //import="org.deegree.ogcwebservices.wpvs.capabilities.Dataset"
        //import="java.net.URL"
        //import="org.deegree.owscommon.com110.Operation110"
        //import="org.deegree.owscommon.com110.HTTP110"
        //import="org.deegree.ogcwebservices.wpvs.capabilities.WPVSOperationsMetadata"
        //import="org.deegree.enterprise.WebUtils"
        WPVSClientConfig wpvsConfiguration = WPVSClientConfig.getInstance();
      
        int viewHeight = wpvsConfiguration.getViewHeight();
        int viewWidth = wpvsConfiguration.getViewWidth();
        
        int overViewHeight = wpvsConfiguration.getOverViewHeight();
        int overViewWidth = wpvsConfiguration.getOverViewWidth();
        
        String wmsGetMapFragment = wpvsConfiguration.getWmsGetMapFragment();
              
        WPVSCapabilities caps = wpvsConfiguration.getWpvsCapabilities();
        String title = wpvsConfiguration.getServiceIdentification();
        
        String elevModel = wpvsConfiguration.getElevationModel();
        String[] datasets = wpvsConfiguration.getAvailableDatasets();
         
        String wpvsBaseURL = wpvsConfiguration.getWpvsBaseURL();
        String crs = wpvsConfiguration.getDefaultCRS();
        int distanceAboveSeaLevel = wpvsConfiguration.getDistanceAboveSeaLevel();
        int initialHeight = wpvsConfiguration.getInitialHeight();
        double poi_x = wpvsConfiguration.getPOIX();
        double poi_y = wpvsConfiguration.getPOIY();
        int initialDistance = wpvsConfiguration.getInitialDistance();
        int initialPitch = wpvsConfiguration.getInitialPitch();
        int initialYaw = wpvsConfiguration.getInitialYaw();
        int initialRoll = wpvsConfiguration.getInitialRoll();
            
        //try to get a parameter from the request.
        String tmp = request.getParameter( "bbox" );
        String bboxAsString = wpvsConfiguration.getInitialBBoxAsString();
        String[] boxCoords = wpvsConfiguration.getInitialBBox();
        if( tmp != null ){
           boxCoords = tmp.split( "," );
        }
        
        String[] buttons = { "zoomin", "zoomout", "move" };
%>
        <title><%=title%></title>
        <link rel="stylesheet" type="text/css" href="./css/deegree.css" />
        <script language="JavaScript1.2" type="text/javascript" src="js/mapnav_functions.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/envelope.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/geotransform.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/wpvsrequest.js"></script>
        <script language="JavaScript1.2" type="text/javascript" src="js/page_helper.js"></script>
        <script type="text/javascript">
        <!--
        var overviewFrame = null;
        var getviewFrame = null;
        var gtrans = null;
        var oviewWidth = <%=overViewWidth%>;
        var oviewHeight = <%=overViewHeight%>;
        
        //defined in mapnav_functions.js
        var centroid = findCentroid( <%=boxCoords[0]%>, <%=boxCoords[1]%>, <%=boxCoords[2]%>, <%=boxCoords[3]%> );
      
        var buttons = new Array(<%=buttons.length%>);
        <%
        for(int i=0;i< buttons.length;i++){
            out.println( "buttons[" + i + "] = '"+ buttons[i] +"';" );
        }
        %>
  
        //defined in wpvsrequest.js
        //function WPVSRequest( url, datasets, elevModel, bbox, poi, yaw, pitch, distance, crs, width, height, splitter, background )
        var wpvsRequest = new WPVSRequest(  '<%= wpvsBaseURL %>', 
                                            '<%= datasets[0] %>', 
                                            '<%= elevModel %>',  
                                            '<%= bboxAsString %>',
                                            '<%= ""+poi_x + "," + poi_y +","+initialHeight%>', 
                                            <%=initialYaw%>, 
                                            <%=initialPitch%>, 
                                            <%=initialDistance%>, 
                                            '<%= crs %>', 
                                            <%=viewWidth%>, 
                                            <%=viewHeight%>,
                                            'BBOX',
                                            'cirrus' );
  
        var wmsRequestFragment = '<%= wmsGetMapFragment %>';
  
        function init(){
            var oviewFrame = document.getElementById( 'overviewFrame' );
            if( oviewFrame != null ){
            oviewFrame.width = <%=overViewWidth%>;
            oviewFrame.height = <%=overViewHeight%>;
            }
            var pitchID = '80dg';
            var pitchHeight = <%=initialPitch%>;
            if( pitchHeight == 5 ){
                pitchID = '5dg';
            } else if( pitchHeight == 10 ){
            	pitchID = '10dg';
            } else if( pitchHeight == 15 ){
                pitchID = '15dg';
            } else if( pitchHeight == 20 ){
                pitchID = '20dg';
            } else if( pitchHeight == 25 ){
                pitchID = '25dg';
            } else if( pitchHeight == 30 ){
                pitchID = '30dg';
            } else if( pitchHeight == 40 ){
                pitchID = '40dg';
            } else if( pitchHeight == 60 ){
                pitchID = '60dg';
            }
            handleSetPitch( pitchID, pitchHeight );
      
            pitchID = 'd5km';
            var configuredDistance = <%=initialDistance%>;
            if( configuredDistance == 25 ){
                pitchID = 'd25m';
            } else if( configuredDistance == 50 ){
                pitchID = 'd50m';
            } else if( configuredDistance == 100 ){
                pitchID = 'd100m';
            } else if( configuredDistance == 200 ){
                pitchID = 'd200m';
            } else if( configuredDistance == 500 ){
                pitchID = 'd500m';
            } else if( configuredDistance == 1000 ){
                pitchID = 'd1km';
            } else if( configuredDistance == 2500 ){
                pitchID = 'd2.5km';
            }
            handleSetDistance( pitchID, configuredDistance ); 

            //defined in page_helper.js
            redraw(true);
        }
        
        function redraw( updateOverview ){ 
            //defined in page_helper.js
            initGeoTransform();
          
            var datasets = "";
            var ds = document.getElementsByName( 'ds' );
            var isDatasetOk = false;
            for(var i=0;i< ds.length;i++){
                if( ds[i].checked ){
                    var prefix = "";
                    if( datasets != "" ){
                        prefix = ",";
                    }
                    datasets = datasets + prefix + ds[i].value;
                    isDatasetOk = true;
                }        
            }
  
            if( !isDatasetOk ){    
                alert( "At least one dataset must be selected!" );
                ds[0].checked = 'checked';
                return;
            }
            wpvsRequest.setDatasets( datasets );
      
            var em = document.getElementById( 'elevationModel' );
            if( em.checked ){
                wpvsRequest.setElevationModel( '<%=elevModel%>' );
            } else {
                wpvsRequest.setElevationModel( null );
            }

            if( updateOverview && overviewFrame != null ){
                overviewFrame.setWMSRequest( wmsRequestFragment 
                	                        + "&BBOX=" + wpvsRequest.getBbox() 
                                        + "&SRS=" + wpvsRequest.getCRS()
                                        + "&WIDTH=" + <%=overViewWidth%> 
                                        + "&HEIGHT=" + <%=overViewHeight%> );
            }
            requestWPVSUpdate();                    
            //set the yaw between the rotation buttons
            //defined in page_helper.js
            setElementText( wpvsRequest.getYaw(), 'yawTxtArea' );
            //set the initial pitch.
        }
        
        function requestWPVSUpdate(){
            getviewFrame.setWPVSRequest( wpvsRequest.createRequest(), <%=viewWidth%>, <%=viewHeight%> );
            var viewFrame = document.getElementById( 'viewFrame' );
        }
        //-->
        </script>
    </head>
    <body onload="init();" marginheight="5" marginwidth="5">
    <%
    if( caps == null ){
        out.print("<p style='color:#ff9999;'>Client is not properly configured. Are the WPVS capabilities available?</p>" );
    } else {
    %>
        <table width="100%" height="600px" cellpadding="0" cellspacing="0" class="main">
            <tr>
                <td class="menu" colspan="2" height="20px"><jsp:include flush="true" page="jsp/header.jsp"></jsp:include></td>
            </tr>
            <tr>
                <!-- left panel -->
                <td class="main" width="200px" valign="top">
                    <table height="100%">
                      <!-- toolbar -->
                      <tr>
                        <td>
                          <%for ( int i = 0; i < buttons.length; i++ ) {%> 
                            <img onclick="setModeForClient('<%=buttons [i]%>');"
                                 id="<%=buttons [i]%>" 
                                 src="../images/<%= i == 0 ? buttons [i] + "_a" : buttons [i]%>.gif" />&nbsp; 
                          <%}%>
                        </td>
                      </tr>
                      <tr>
                        <!-- overview  -->
                        <td>
                          <iframe id="overviewFrame" 
                                  src="overviewframe.jsp" 
                                  height="<%=overViewHeight%>"
                                  width="<%=overViewWidth%>" 
                                  scrolling="no"></iframe>
                        </td>
                      </tr>
                      <tr>
                        <!-- data checkboxes -->
                        <td>
                          <table>
                            <tr>
                              <td><b class="mainGUI"><%=title%></b> 
                                <br />
                                <br />
                                Datasets:<br />
                                <%for ( int i = 0; i < datasets.length; i++ ) {%> 
                                  <input type="checkbox" 
                                         name="ds" 
                                         value="<%= datasets[i] %>"
                                         onclick="redraw(false);" 
                                         checked="checked"> <%=datasets[i]%><br />
                                <%}%>
                              </td>
                            </tr>
                            <tr>
                              <td><br />
                              Elevationmodel:<br/>
                                <input type="checkbox" 
                                       name="elevationModel" 
                                       id="elevationModel" 
                                       checked="checked" 
                                       onclick="redraw(false);">Use elevation model (dem)
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                      <tr>
                        <!-- for creating an emtpy space -->
                        <td height="*">&nbsp;</td>
                      </tr>
                      <tr>
                        <!-- spitter -->
                        <td>
                          <input type="radio" 
                                 name="splitter" 
                                 checked="checked" 
                                 onclick="setSplitter('BBOX');">BBox mode(fast)<br />
                          <input type="radio" 
                                 name="splitter" 
                                 onclick="setSplitter('QUAD');">Horizon mode(slow)<br />
                        </td>
                      </tr>
                      <tr>
                        <!-- nav arrows -->
                        <td colspan="3" align="center" valign="bottom"><br />
                          <jsp:include flush="true" page="jsp/nav_poi.jsp?directionlabels=front,right,back,left"></jsp:include>
                          <br />
                        </td>
                      </tr>
                    </table>
                </td>
                <!-- right panel -->
                <td valign="top">
                    <table class="main" width="100%" height="100%">
                      <tr valign="top" height="1">
                        <!-- begin dummy cells for layout -->
                        <td width="800" colspan="4"></td>
                        <td width="*" rowspan="3"></td>
                      </tr>
                      <!-- end dummy cells for layout -->
                      <tr valign="top" height="50">
                        <td>
                          <jsp:include flush="true" page="jsp/nav_yaw.jsp?title=Rotate view"></jsp:include>
                        </td>
                        <td>
                          <jsp:include flush="true" page="jsp/nav_distance.jsp?title=Distance"></jsp:include>
                        </td>
                        <td>&nbsp;</td>
                        <td>
                          <jsp:include flush="true" page="jsp/nav_pitch.jsp?title=Viewangle"></jsp:include>
                        </td>
                      </tr>
                      <tr>
                        <td valign="top" colspan="4"><!--  <img align="top" src="" id="viewFrame" ></img> --> 
                          <iframe id="getviewFrame" 
                                  name="getViewFrame"
                                  src="getviewframe.jsp" 
                                  height="<%=viewHeight%>" 
                                  width="<%=viewWidth%>" 
                                  scrolling="no"></iframe>
                        </td>
                      </tr>
                    </table>
                </td>
            </tr>
            <tr height="20px">
                <td class="menu"><span id="footerTxtArea"></span></td>
                <td align="right" class="menu"><a href="http://www.deegree.org">powered by deegree</a>&nbsp;&nbsp;&nbsp;&nbsp;</td>
            </tr>
        </table>
    <%  
    }
    %>
    </body>
</html>
