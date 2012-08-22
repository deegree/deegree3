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

function Legend () {
	
    this.targetDocument = null;
    this.parentNode = null;

    // method declaration
    this.paint = paint;
    this.repaint = repaint;
    this.getRPCValues = getRPCValues;

    // method implementation
    function paint(targetDocument, parentNode) {
    	this.targetDocument = targetDocument;
    	this.parentNode = parentNode;
    }

    function repaint() {
    	 var frm = this.targetDocument.forms[0];
         frm.action = "control";
         var hidden = this.targetDocument.getElementById( "dyn_rpc" );
         if ( hidden != null ) {
	         hidden.value = this.getRPCValues();
	         frm.submit();
         }
    }
    
    function getRPCValues(){
        
        var groups = controller.mapModel.getLayerList().getLayerGroups();
        var factory = new WMSRequestFactory();
		
		var s = "<?xml version='1.0' encoding='UTF-8'?>";
		s = s + "<methodCall><methodName>mapClient:drawLegend</methodName>";
		s = s + "<params>";		
		s = s + "<param><value><struct>";
     
        for(var i = groups.length-1; i >= 0 ; i--) {
        	var ly = groups[i].getLayers();
        	var vis = false;
        	for ( var j = 0; j < ly.length; j++) {
        		if ( ly[j].isVisible() ) {
        			vis = true;
        			break;
        		}
        	}
        	if ( vis ) {
				s = s + "<member><name>wmsRequest" + i + "</name><value><string><![CDATA[";					
	            if ( controller.vSessionKeeper != null ) {
				    s = s + encodeURIComponent( factory.createGetMapRequest( groups[i], controller.mapModel, controller.vSessionKeeper.id ) );
	            } else {
	                s = s + encodeURIComponent( factory.createGetMapRequest( groups[i], controller.mapModel,  null ) );
	            }		            
				s = s + "]]></string></value></member>";
			}					
		}
		
		s = s + "<member><name>bgColor</name><value><string>"
		s = s + "<![CDATA[" + encodeURIComponent( "#FFFFFF" );
		s = s + "]]></string></value></member>"
		
		if ( controller.vSessionKeeper != null && controller.vSessionKeeper.id != null ) {
			s = s + "<member><name>sessionID</name><value><string>"
			s = s + controller.vSessionKeeper.id;
			s = s + "</string></value></member>"
		}
						
		s = s + "</struct></value></param></params></methodCall>";
		return s;
	}

}
