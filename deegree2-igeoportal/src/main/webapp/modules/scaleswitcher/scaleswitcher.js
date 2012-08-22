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

function ScaleSwitcher(label, listOfScales) {

    // variable declaration
    this.label = label;
    this.listOfScales = listOfScales;
    this.targetDoc = null;
    this.parentElem = null;
    this.update = null;
    
    // method declaration
    this.paint = paint;
    this.repaint = repaint;
    this.createList = createList;
    this.setUpdate = setUpdate;
    this.switchScale = switchScale;

    /**
    * From the igeoportal documentation
    * Method to paint the ScaleSwitcher HTML part to the portal
    */
    function paint(targetDocument, parentElement) {    	
    	try {
	        this.targetDoc = targetDocument;
	        this.parentElem = parentElement;
	        var temporaryScale = controller.mapModel.getScaleDenominator();
	        
        	var span = targetDocument.getElementById( "spanScaleSwitchElement");
        	removeChildren( span );

            var font = targetDocument.createElement( "font" );
            //If condition added to enable loading images
            if ( label.length > 0 ) {
            	/* this is deegree standard procedure */
	            var ltxt = targetDocument.createTextNode( label + " " );
    	        font.appendChild( ltxt );
            } else {
                /* Enables loading image instead of text */
            	var img = targetDocument.createElement( "img" );
            	img.setAttribute( "src", "./images/label_scale.gif" );
            	font.appendChild( img );
            }
            span.appendChild( font );
            this.createList( targetDocument );
	        
    	} catch(e ) {
    		alert( 1+ " " + e );
    	}
    }
    
    function setUpdate(update) {
    	this.update = update;
    }

    /**
    * From the igeoportal documentation
    * Method called each time an action as been performed to update this module
    */
    function repaint() {
    	this.update();
    	//parent.controller.vOLMap.getMap().setOptions( {numZoomLevels: 5 } );
    }

    /**
    * Method to create the HTML combobox of the ScaleSwitcher Module dynamically
    */
    function createList( targetDocument ) {
    	var sel = targetDocument.getElementById( "scaleList" );
    	sel = targetDocument.getElementsByName( 'scaleList' )[0]
    	removeChildren( sel );
        
        var list = listOfScales.split(";");
        var temporaryScale = controller.mapModel.getScaleDenominator();
        
        for ( var i = 0; i < list.length; i++ ){
            var tokens = list[i].split('|');
            var tokenScale = tokens[0]; // "1:25000"
            var tokenLabel = tokens[0]; // default: label equals scale
            if ( tokens.length == 2 ) {
                // label is set in configuration:
                tokenLabel = tokens[1]; // "someLabel"
            }
            if ( i == 0 ) {
                var opt = targetDocument.createElement( "option" );
                var tmpLabel = "1:" + temporaryScale;
                if ( temporaryScale == -1 ) {
                    opt.setAttribute( "value", 'select scale' );
                    var txt = targetDocument.createTextNode( 'select scale' );
                } else {
                    for ( var j = 0; j < list.length; j++ ){
                        var myTokens = list[j].split('|');
                        if ( myTokens.length == 2 && myTokens[0].split( ':' )[1] == this.getScale() ) {
                            tmpLabel = myTokens[1];
                        }
                    }
                    opt.setAttribute( "value", '1:'+temporaryScale );
                    var txt = targetDocument.createTextNode( tmpLabel );
                }
                opt.appendChild( txt );
                sel.appendChild( opt );
            }
            if ( ('1:'+temporaryScale) != tokenScale  ) {
                var opt = targetDocument.createElement( "option" );
                opt.setAttribute( "value", tokenScale );
                var txt = targetDocument.createTextNode( tokenLabel );
                opt.appendChild( txt );
                sel.appendChild( opt );
            }
        }
        return sel;
    }
    
    function switchScale(scale) {
    	
    	var requestedScale = parseFloat( scale.split(':')[1] );    	
    	var currentScale = controller.mapModel.getScaleDenominator();
    	var ratio = requestedScale / currentScale;
    	var currentBBOX = controller.mapModel.getBoundingBox();

        var newWidth = currentBBOX.getWidth() * ratio;
        var newHeight = currentBBOX.getHeight() * ratio;
        var midX = currentBBOX.minx + ( currentBBOX.getWidth() / 2.0 );
        var midY = currentBBOX.miny + ( currentBBOX.getHeight() / 2.0 );
        var minx = midX - newWidth ;
        var maxx = midX + newWidth ;
        var miny = midY - newHeight;
        var maxy = midY + newHeight;
        var env = new Envelope( minx, miny, maxx, maxy );
        var event = new Event( 'ScaleSwitcher', 'BBOX', env );
        controller.actionPerformed( event );
    }
}