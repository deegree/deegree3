<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
    //http://localhost:8080/services/services?service=WPVS&request=GetView&BOUNDINGBOX=423750,4512700,425500,4513900&DATASETS=air2007,buildings,trees&ELEVATIONMODEL=dem&PITCH=16&YAW=-117121331&ROLL=0&AOV=90&FARCLIPPINGPLANE=10000&WIDTH=800&HEIGHT=600&SCALE=1.0&STYLES=default&DATETIME=2007-03-21T12:00:00&EXCEPTIONFORMAT=INIMAGE&VERSION=1.0.0&OUTPUTFORMAT=image/png&BACKGROUND=cirrus&POI=2579800,5620000,250&CRS=null&DISTANCE=1000
  
    int viewHeight = 600;
    int viewWidth =  1200;
    
    int overViewHeight = 150;
    int overViewWidth = 150;
    
    //String wmsGetMapFragment = wpvsConfiguration.getWmsGetMapFragment();
          
    String caps = "http://stadtplan.bonn.de/Deegree2wpvs/services?&version=1.0.0&CRS=EPSG%3A31466&request=GetCapabilities&service=WPVS";
    String title = "WPVS for Bonn, Germany";
    
    String elevModel = "dem";
    String[] datasets = { "air2007", "buildings", "trees" };
     
    String wpvsBaseURL = "http://stadtplan.bonn.de/Deegree2wpvs/services?service=WPVS&";
    String crs = null; //not evaluated for d3 wpvs currently
    //int distanceAboveSeaLevel = wpvsConfiguration.getDistanceAboveSeaLevel();
    int initialHeight = 560;
    double poi_x = 2579800;
    double poi_y = 5620000;
    int initialDistance = 1000;
    int initialPitch = 15;
    int initialYaw = 290;
    int initialRoll = 0;
    double initialStep = 0.1;
        
    //try to get a parameter from the request.
    String[] boxCoords = { "423750", "4512700", "425500", "4513900" };
    String bboxAsString =  boxCoords[0] + "," + boxCoords[1] + "," + boxCoords[2] + "," + boxCoords[3];
    
    String[] buttons = { "zoomin", "zoomout", "move" };
  %>
  <title><%=title%></title>
  <link rel="stylesheet" type="text/css" href="../deegree.css" />
  <script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/mapnav_functions.js"></script>
  <script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/envelope.js"></script>
  <script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/geotransform.js"></script>
  <script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/wpvsrequest.js"></script>
  <script LANGUAGE="JavaScript1.2" TYPE="text/javascript" src="js/page_helper.js"></script>
  <script type="text/javascript">
    //<!--
    var overviewFrame = null;
    var getviewFrame = null;
    var gtrans = null;
    var oviewWidth = <%=overViewWidth%>;
    var oviewHeight = <%=overViewHeight%>;
    
    //defined in mapnav_functions.js
    //var centroid = findCentroid( <%=boxCoords[0]%>, <%=boxCoords[1]%>, <%=boxCoords[2]%>, <%=boxCoords[3]%> );
  
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
                                        <%= initialStep %>, 
                                        <%=initialYaw%>, 
                                        <%=initialPitch%>, 
                                        <%=initialDistance%>, 
                                        '<%= crs %>', 
                                        <%=viewWidth%>, 
                                        <%=viewHeight%>,
                                        'BBOX',
                                        'cirrus' );
  
    <%-- var wmsRequestFragment = /<%= wmsGetMapFragment %>'; --%>
  
    function init(){
      <%--var oviewFrame = document.getElementById( 'overviewFrame' );
      if( oviewFrame != null ){
        oviewFrame.width = <%=overViewWidth%>;
        oviewFrame.height = <%=overViewHeight%>;
      }--%>
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
      }else if( pitchHeight == 60 ){
          pitchID = '60dg';
      } 
      handleSetPitch( pitchID, pitchHeight );
      
      pitchID = 's1';
      var configuredStep = <%=initialStep%>;
      if( configuredStep == 0.2 ){
    	  pitchID = 's2';
      } else if( configuredStep == 0.3 ){
    	  pitchID = 's3';
      } else if( configuredStep == 0.4 ){
    	  pitchID = 's4';
      } else if( configuredStep == 0.5 ){
    	  pitchID = 's5';
      }
      handleSetStep( pitchID, configuredStep ); 

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

      wpvsRequest.setElevationModel( '<%=elevModel%>' );
      //var em = document.getElementById( 'elevationModel' );
      //if( em.checked ){
        //wpvsRequest.setElevationModel( '<%=elevModel%>' );
      //} else {
      //  wpvsRequest.setElevationModel( null );
      //}
      
      //if( updateOverview && overviewFrame != null ){
      //  overviewFrame.setWMSRequest( wmsRequestFragment 
      //                              + "&BBOX=" + wpvsRequest.getBbox() 
      //                              + "&SRS=" + wpvsRequest.getCRS()
      //                              + "&WIDTH=" + <%=overViewWidth%> 
      //                              + "&HEIGHT=" + <%=overViewHeight%> );
      //}
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
            </td>
          </tr>
          <tr>
            <!-- overview  -->
            <td>
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
                    <%for ( int i = 0; i < datasets.length; i++ ) {
                    	if ( datasets[i].equals( "air2007" ) ) {
                    	%> 
                      	<input type="checkbox" 
                             name="ds" 
                             value="<%= datasets[i] %>"
                             onclick="redraw(false);" 
                             checked="checked"> <%=datasets[i]%><br />
						<%} else { %>
						<input type="checkbox" 
                             name="ds" 
                             value="<%= datasets[i] %>"
                             onclick="redraw(false);"> <%=datasets[i]%><br />                             
                    <%} }%>
                  </td>
                </tr>
                <tr>
                  <td>
                  
                  <br />                 
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
            <td>
            	<jsp:include flush="true" page="jsp/nav_step.jsp?title=Movement Step"></jsp:include>
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
