//$HeadURL$
/*----------------------------------------------------------------------------
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
----------------------------------------------------------------------------*/

function WMSRequestFactory() {

    this.createGetMapRequest = createGetMapRequest;
    this.createGetFeatureInfoRequest = createGetFeatureInfoRequest;
    this.createGetFeatureInfoRequest2 = createGetFeatureInfoRequest2;
    this.createGetLegendGraphicRequest = createGetLegendGraphicRequest;

    function createGetMapRequest(layerGroup, mapModel, sessionid) {

        var wmsrequest;
        var request;
        var serviceType = layerGroup.getServiceType();
        var tmp2 = serviceType.split(" ");
        var ser = tmp2[0];
        var tmp = ser.split(":");
        var service = tmp[1];
        var version = tmp2[1];
        if (this.version <= "1.0.0") {
            request = "map";
        }else {
            request = "GetMap";
        }
        var tmp = mapModel.getBoundingBox();
        var bbox = tmp.minx + ',' + tmp.miny + ',' + tmp.maxx + ',' + tmp.maxy;
        var url = layerGroup.getServiceURL();
        if ( url.indexOf( "?" ) < 0  ) {
            url += "?";
        } else if ( url.charAt( url.length-1 ) != "&") {
        	url += "&";
        }
        
        wmsrequest = url + "VERSION=" + version + "&REQUEST=" + request + "&SERVICE=WMS" +
                     "&FORMAT=" + layerGroup.getFormat() + "&BBOX=" + bbox +
                     "&WIDTH=" + mapModel.getWidth() + "&HEIGHT=" + mapModel.getHeight() +
                     "&BGCOLOR=" + layerGroup.getBGColor() + "&EXCEPTIONS=application/vnd.ogc.se_inimage";
        if ( layerGroup.getTransparency() ) {
                wmsrequest += "&TRANSPARENT=TRUE";
        } else {
                wmsrequest += "&TRANSPARENT=FALSE";
        }
        if ( sessionid != null ) {
            wmsrequest += "&sessionID=" + sessionid;
        }
        var serviceName = layerGroup.getServiceName();
        var layers = layerGroup.getLayers();
        var elevation, time;

        var sldRef = layers[0].getSLDRef();

        if (sldRef == null){
            var layerArray = new Array();
            var styleArray = new Array();
            var k = 0;
            for(var i = layers.length-1; i >= 0; i--){
                if ( layers[i].isVisible() ) {
                    layerArray[k] = layers[i].getName();
                    var stl = layers[i].getStyleName();
                    if ( stl == 'default' ) {
                    	stl = '';
                    }
                    styleArray[k++] = stl;
                }
            }
            var layName = layerArray.join(",");
            if ( layName == null || layName == "" || layName == ","){
                return null;
            }
            var style = styleArray.join(",");
            if ( style == null || style == "" || style == "," ) {
                style = '';
            }
            wmsrequest = wmsrequest + "&STYLES=" + style + "&LAYERS=" + layName;
        } else {
            wmsrequest = wmsrequest + "&SLD=" + sldRef;
        }
        wmsrequest = wmsrequest + "&SRS=" + mapModel.getSrs();
        /*
        var elevation = layerGroup.getElevation();
        if (elevation != null && elevation.length > 0 ){
            var s = '';
            for (var el = 0; el < elevation.length; el++){
                s = s + elevation[i] + "/";
            }
            s = s.substring(0, s.length-1);
            wmsrequest = wmsrequest + "&ELEVATION=" + s;
        }
        */
        var time = layerGroup.getTime();
        if ( ( time != null ) && ( time.length > 0 ) ) {
            wmsrequest = wmsrequest + "&TIME=" + time;
        }
        return wmsrequest;
    }

    /**
     * 
     * @param layerGroup
     * @param mapModel
     * @param format
     * @param x
     * @param y
     * @param sessionid
     * @return GetFeatureInfo request or an error message if at least one selected and visible layer
     *         is not queryable or null if no layer is selected and visible  
     */
    function createGetFeatureInfoRequest(layerGroup, mapModel, format, x, y, sessionid) {

        var fiRequest = "";
        var request = "GetFeatureInfo";
        var wmsrequest = createGetMapRequest(layerGroup, mapModel, sessionid);
        if ( wmsrequest == null ) return null;
        if (wmsrequest.search(/REQUEST=GetMap/) != -1 ){
            wmsrequest = wmsrequest.replace(/REQUEST=GetMap/, "REQUEST=GetFeatureInfo");
        }else {
            wmsrequest = wmsrequest.replace(/REQUEST=map/, "REQUEST=FeatureInfo");
        }
        var layers = layerGroup.getLayers();
        var selLayer = new Array ();
        var j = 0;
        var tmp = "";
        for (var i = 0; i < layers.length; i++){
            if ( layers[i].isSelected() && layers[i].isVisible() && layers[i].isQueryable() ) {
                selLayer[j++]= layers[i].getName();
            } else if ( layers[i].isSelected() && layers[i].isVisible()&& !layers[i].isQueryable() ) {
            	tmp += (' ' + layers[i].getName());
            }
        }
        if ( tmp.length != 0 ) {
        	return "ERROR: layers: " + tmp + " does not support GetFeatureInfo";
        }
        if ( selLayer.length == 0 ) {
        	return null;
        };
        var actLayList = selLayer.join(",");
        return wmsrequest + "&QUERY_LAYERS=" + actLayList + "&INFO_FORMAT=" + 
                    format + "&X=" + x + "&Y=" + y + "&FEATURE_COUNT=999";     
    }
    
    /**
     * 
     * @param layerGroup
     * @param mapModel
     * @param format
     * @param x
     * @param y
     * @param sessionid
     * @return GetFeatureInfo request or null if no layer is selected, visible and queryable
     */
    function createGetFeatureInfoRequest2(layerGroup, mapModel, format, x, y, sessionid) {

        var fiRequest = "";
        var request = "GetFeatureInfo";
        var wmsrequest = createGetMapRequest(layerGroup, mapModel, sessionid);
        if ( wmsrequest == null ) return null;
        if (wmsrequest.search(/REQUEST=GetMap/) != -1 ){
            wmsrequest = wmsrequest.replace(/REQUEST=GetMap/, "REQUEST=GetFeatureInfo");
        }else {
            wmsrequest = wmsrequest.replace(/REQUEST=map/, "REQUEST=FeatureInfo");
        }
        var layers = layerGroup.getLayers();
        var selLayer = new Array ();
        var j = 0;
        for (var i = 0; i < layers.length; i++){
            if ( layers[i].isSelected() && layers[i].isVisible() && layers[i].isQueryable() ) {
                selLayer[j++]= layers[i].getName();
            } 
        }
        if ( selLayer.length == 0 ) {
        	return null;
        };
        var actLayList = selLayer.join(",");
        return wmsrequest + "&QUERY_LAYERS=" + actLayList + "&INFO_FORMAT=" +
                    format + "&X=" + x + "&Y=" + y + "&FEATURE_COUNT=999";     
    }

    function createGetLegendGraphicRequest(layergroup, layer, width, height, sessionid) {
        var serviceType = layergroup.getServiceType();
        var tmp2 = serviceType.split(" ");
        var ser = tmp2[0];
        var tmp = ser.split(":");
        var service = tmp[1];
        var version = tmp2[1];
        var request = layergroup.getServiceURL();
        if ( request.indexOf("?" ) < 0  ) {
        	request += "?";
        } else if ( request.charAt( request.length-1 ) != "&" ) {
        	request += "&";
        }
        if ( service == 'WMS' ) {
            request = request + 'service=' + service + '&version=' + version + '&request=GetLegendGraphic';
            request = request + '&format=image/png' + '&layer=' + layer.getName();
            request = request + '&style='+ layer.getStyleName() + '&width=' + width;
            request = request + '&height=' + height + "&SERVICE=WMS";
            if ( layer.getSLDRef() != null ) {
                request = request + '&SLD=' + layer.getSLDRef();
            }
        }
        if ( sessionid != null ) {
            request = request + "&sessionID=" + sessionid;
        }
        return request;
    }

}