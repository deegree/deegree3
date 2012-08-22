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

function MapOverview( src, minx, miny, maxx, maxy, fgColor, width, height, offset ) {
	
	// varaiable declaration
	this.src = src;	
	this.mapBBox = new Envelope(minx, miny, maxx, maxy); 
	this.width = width; 
	this.height = height; 
	this.fgColor = fgColor;
	this.jg = null;
	this.visibleBBox = this.mapBBox;
	this.targetDocument = null;
	this.parentElement = null;
	this.isNav = false;
    this.is5up = false;	
	this.transform = new GeoTransform( minx, miny, maxx, maxy, 0, 0, width-1, height-1 );
	this.offset = 0;
	if ( offset != null && offset != "undefined" ) {
		this.offset = offset;
	}

    // method declaration
	this.paint = paint;
	this.repaint = repaint;
	this.createImage = createImage;
	this.mouseUp = mouseUp;

	// method implementation
	function mouseUp(x,y) {
		x = this.transform.getSourceX( x - this.offset );
		y = this.transform.getSourceY( y );
		controller.vMapController.zoomByPoint( 0, x, y );
		controller.repaint();
	}
	
	function paint(targetDocument, parentElement) {
		this.targetDocument = targetDocument;
		this.parentElement = parentElement;
		this.createImage(targetDocument, parentElement);
		this.repaint();
	}
	
	function repaint() {
		removeHTMLLayer(this.targetDocument, "id:cross");
		this.mapBBox = controller.mapModel.getBoundingBox();
		var x1 = this.transform.getDestX( this.mapBBox.minx );
		var y1 = this.transform.getDestY( this.mapBBox.maxy );
		var x2 = this.transform.getDestX( this.mapBBox.maxx );
		var y2 = this.transform.getDestY( this.mapBBox.miny );
		removeHTMLLayer(this.targetDocument, "id:left");
		removeHTMLLayer(this.targetDocument, "id:right");
		removeHTMLLayer(this.targetDocument, "id:top");
		removeHTMLLayer(this.targetDocument, "id:bottom");
		if (y2-y1 > 15 && x2-x1 > 15) {
			var newLayer = createLayer( this.targetDocument, "left", "id:left", x1 + this.offset, y1, 2, y2-y1, 3, this.fgColor, true );
			this.parentElement.appendChild(newLayer);		
			newLayer = createLayer( this.targetDocument, "top", "id:top", x1 + this.offset, y1, x2-x1+1, 2, 4, this.fgColor, true );
			this.parentElement.appendChild(newLayer);
			newLayer = createLayer( this.targetDocument, "right", "id:right", x2-1 + this.offset, y1, 2, y2-y1, 5, this.fgColor, true );
			this.parentElement.appendChild(newLayer);		
			newLayer = createLayer( this.targetDocument, "bottom", "id:bottom", x1 + this.offset, y2-1, x2-x1, 2, 6, this.fgColor, true );
			this.parentElement.appendChild(newLayer);
		} else {
			var newLayer = createLayer( this.targetDocument, "cross", "id:cross", x1+(x2-x1)/2-15 + this.offset, y1+(y2-y1)/2-15, 
					                    30, 30, 3, null, true );
			var newImage = this.targetDocument.createElement("img");
			newImage.setAttribute("id", "crossimage");
			newImage.setAttribute("src", "../../images/cross.gif");
			newImage.setAttribute("border", 0);			
			newLayer.appendChild(newImage);
			this.parentElement.appendChild(newLayer);
		}
	}
	
	function createImage() {
		if ( this.targetDocument.getElementById("layerOverview") == null ) {
			var newLayer = createLayer( this.targetDocument, "layerOverview", "id:layerOverview", 0 + this.offset, 0, this.width, 
										this.height, 2, null, true );
			this.parentElement.appendChild(newLayer);
			var newImage = this.targetDocument.createElement("img");
			newImage.setAttribute("id", "overview");
			newImage.setAttribute("src", this.src);
			newImage.setAttribute("border", 0);			
			newImage.setAttribute("height", this.height);
			newImage.setAttribute("width", this.width);
			newLayer.appendChild(newImage);
		}
	}

}
