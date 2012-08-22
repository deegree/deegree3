<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.deegree.i18n.Messages" %>
<%@ page import="java.util.Locale" %>
<%
Locale loc = request.getLocale();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta name="author" content="lat/lon GmbH" />
        <meta name="KeyWords" content="deegree iGeoPortal lat/lon" />
        <link href="../../css/deegree.css" rel="stylesheet" type="text/css" />
        <link rel="stylesheet" type="text/css" href="../../javascript/ext-3.3.1/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" title="blue"  href="../../javascript/ext-3.3.1/resources/css/xtheme-blue.css" /> 
        <link rel="stylesheet" type="text/css" title="gray"  href="../../javascript/ext-3.3.1/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" title="black" href="../../javascript/ext-3.3.1/resources/css/xtheme-access.css" />
        <link rel="stylesheet" href="../../javascript/openlayers/theme/default/style.css" type="text/css" />
        <!-- script src='http://openlayers.org/dev/OpenLayers.js'></script--> 
        <script src='../../javascript/openlayers/lib/OpenLayers.js'></script> 
        
        <script type="text/javascript" src="../../javascript/ext-3.3.1/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="../../javascript/ext-3.3.1/ext-all.js"></script>
        <script type="text/javascript" src="../../javascript/request_handler.js"></script>
        <script type="text/javascript" src="digitizer.js"></script>
        <script type="text/javascript" src="initGUI.js"></script>
        <script type="text/javascript" src="editFeaturePanel.js"></script>
        <script type="text/javascript" src="actionHandler.js"></script>
        <script type="text/javascript">

            var url;
            var dd;
            var values;
            var currentFeatureType;
            var currentFeature;  
          
            function register() {
                if ( parent.controller == null ) {
                    parent.controller = new parent.Controller();
                    parent.controller.init();
                }
                parent.controller.initDigitizer( document );
                parent.setActiveStyleSheet();
            }
            
        </script>
	</head>
	<body onload="register(); initGUI();" onunload="processReset('yes')" style="width: 100%; height: 100%;">
	</body>
</html>