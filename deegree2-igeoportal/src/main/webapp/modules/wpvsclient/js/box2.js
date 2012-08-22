//$HeadURL$
//$Id$
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

function DragBox (layWidth, layHeight, hspc, vspc, zoomBoxColor) {

	this.iWidth = layWidth;
	this.iHeight = layHeight;
	this.hspc = hspc;
	this.vspc = vspc;
	this.zoomBoxColor = zoomBoxColor;
	this.listener = null;
	this.jg = null;
	this.mouseX=0;
    this.mouseY=0;
	this.x = 0;
	this.y = 0;
	this.zoom = false;
	this.isNav = false;
    this.is5up = false;

	// method declaration
	this.init = init;
	this.kill = kill;
	this.clear = clear;
	this.setListener = setListener;
	this.getMouse = getMouse;
	this.chkMouseUp = chkMouseUp;
	this.mapTool = mapTool;
	this.getZoomBox = getZoomBox;
	this.createLayer = createLayer;
	this.detectBrowser = detectBrowser;
    this.createImage = createImage;

	// method implementation
	function init() {
		this.kill();
		this.detectBrowser();
		this.createLayer( "drawLayer", hspc, vspc, 50, null, true);
		this.jg = new jsGraphics( "drawLayer" );
		this.jg.setPrintable(true);
		this.jg.setColor(zoomBoxColor);
		this.jg.setStroke(2);
		this.createLayer( "topLayer", hspc, vspc, 100, null,false);
    }
	
	function createLayer(name, offsetx, offsety, zIdx, bgColor, visible) {
		newLayer = document.createElement("div");
		newLayer.setAttribute("id", name);
		newLayer.setAttribute("name", name);
		newLayer.style.position = "absolute";
		newLayer.style.top = offsety + 'px';
		newLayer.style.left = offsetx + 'px';
		newLayer.style.zIndex = zIdx;
		newLayer.style.width = this.iWidth;
		newLayer.style.height = this.iHeight;
		newLayer.style.overflow ='visible';
		newLayer.style.visibility = visible ? 'visible' : 'hidden';
		if ( bgColor != null ) {
			newLayer.style.backgroundColor = bgColor;
		}
        if ( name == "topLayer" && !this.isNav ) {
			this.createImage(newLayer);
		}
		document.getElementsByTagName('body')[0].appendChild(newLayer);
	}
    
    function createImage(newLayer) {
		var newImage = document.createElement("img");
		newImage.setAttribute("id", "overviewt");
		newImage.setAttribute("src", "../images/pixel.gif");
		newImage.setAttribute("border", 0);	
		newImage.setAttribute("height", this.iHeight);
		newImage.setAttribute("width", this.iWidth);
		newLayer.appendChild(newImage);
	}
	
	function clear() {
		this.jg.clear();
	}
	
	function kill() {
		var layer = document.getElementById("drawLayer");
		if ( layer != null ) {
			layer.parentNode.removeChild( layer );
			layer = document.getElementById("topLayer");
			layer.parentNode.removeChild( layer );
		}
	}
	
	function detectBrowser() {	
		this.isNav = (navigator.appName.indexOf("Netscape")>=0);
		if ( !this.isNav) {
			if (navigator.appVersion.indexOf("MSIE 5")>0 ||
				navigator.appVersion.indexOf("MSIE 6")>0 ) {
				this.is5up = true;
			}
		}
	}
	
	function setListener(listener) {
		this.listener = listener;
	}

    function getMouse(xx,yy) {		
		if ( (xx <= this.iWidth) && (yy <= this.iHeight) && 
			  xx > hspc && yy > vspc && this.mouseX >= 0 && 
             this.mouseY >= 0 && xx > 5 && yy > 5) {
			this.x = xx;
			this.y = yy;
			if ( this.zoom ) {		
				var rx = this.mouseX;
				var ry = this.mouseY;
				var w = xx-this.mouseX;
				var h = yy-this.mouseY;
				if ( w < 0 ) {
					rx = xx;
					w = -1*w;
				}
				if ( h < 0 ) {
					ry = yy;
					h = -1*h;
				}
				this.jg.clear();
				this.jg.drawRect( rx, ry, w, h);
				this.jg.paint();
			}
		}
	}

    function chkMouseUp(e) {
		this.zoom = false;
		if ( //this.listener != null && 
				this.x > 0 && 
		     this.x < (this.iWidth) && this.y > 0 &&
			 this.y < (this.iHeight )) {
		}
    }

    function mapTool(x,y) {
        if ( !this.zoom ) {
            this.mouseX= x;
            this.mouseY= y;
            this.x = this.mouseX;
            this.y = this.mouseY;
        
        }
        this.zoom = true;	
    }

    function getZoomBox() {
		if ( this.x < this.mouseX ) {
			var tmp = this.x;
			this.x = this.mouseX;
			this.mouseX = tmp;
		}
		if ( this.y < this.mouseY ) {
			var tmp = this.y;
			this.y = this.mouseY;
			this.mouseY = tmp;
		}
		return new Envelope( this.mouseX, this.mouseY, this.x, this.y );
    }

}
