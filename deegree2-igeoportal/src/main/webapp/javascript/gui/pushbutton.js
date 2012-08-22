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

function PushButton(mode, label, icon, pressedIcon) {
	
	// variable declaration
	this.mode = mode;
	this.label = label;
	this.icon = icon;
	this.tt = icon;
	this.pressedIcon = pressedIcon; 
	this.pressed = false;
	this.currentIcon = icon;
	this.buttonGroup = null;
	this.listener = null;
	this.targetDocument = null;
	
	// method declaration
	this.getType = getType;
	this.setListener = setListener;
	this.getMode = getMode;
	this.isPressed = isPressed;
	this.setPressed = setPressed;
	this.paint = paint;
	this.repaint = repaint;
	this.setButtonGroup = setButtonGroup;
	this.getButtonGroup = getButtonGroup;
	this.createIMG = createIMG;
	this.notify = notify;
	
	// implementation
	
	function getType() {
		return 'PushButton';
	}
	
	function setButtonGroup(buttonGroup) {
		this.buttonGroup = buttonGroup;
	}
	
	function getButtonGroup() {
		return this.buttonGroup;
	}
	
	function setListener(listener) {
		this.listener = listener;
	}
	
	function notify() {
		if ( this.listener != null ) {
			var event = new Event( this, mode+"Pressed", mode );
			this.listener.actionPerformed( event );
		}
	}
	
	function getMode() {
		return this.mode;
	}
	
	function isPressed() {
		return this.pressed;
	}
	
	function setPressed(pressed) {
		this.pressed = pressed;
		if ( pressed ) {
			this.currentIcon = pressedIcon;
		} else {
			this.currentIcon = icon;
		}
		var node = this.targetDocument.getElementById(this.mode);
		node.src = this.currentIcon;
	}
	
	function paint(targetDocument, parentElement) {	
		this.targetDocument = targetDocument;
		var img = this.createIMG(targetDocument);
		var node = targetDocument.getElementById(this.mode);
		if ( node != null ) {
			var pe = node.parentNode;
			pe.replaceChild( node, img );
		} else {
			parentElement.appendChild( img );
		}
	}
    
	function repaint() {}
	
    function createIMG(targetDocument) {
        var img = targetDocument.createElement("img");
        img.setAttribute("src", this.currentIcon);
        img.setAttribute("id", this.mode);
        img.setAttribute("alt", this.label);
        img.setAttribute("title", this.label);
        /* The size of button icons is set to a fixed value. 
        If you want to change these values, or even disable limiting image sizes,
        then please make sure that these changes are working on all IE versions */
        if ( this.currentIcon.indexOf('seperator') < 0 &&
             this.currentIcon.indexOf('space') < 0 ) {
            img.setAttribute("width", 16);
            img.setAttribute("height", 16);
        } else if ( this.currentIcon.indexOf('space') >= 0 ) {
            img.setAttribute("width", 130);
            img.setAttribute("height", 16);
        }
        img["onmousedown"]= new Function( 'pushButtonPressed(this)' );
        img["onmouseup"]= new Function( 'releaseButtonPressed(this)' );
        addObjectToRepository( this.mode, this );
        var div = targetDocument.createElement("div");

        if ( typeof ActiveXObject != 'undefined' ) {
            //IE
            div.setAttribute("className", "buttonIcon");    
        } else {
        	//Firefox
            div.setAttribute("class", "buttonIcon");
        }
        div.appendChild(img);
        return div;
	}	
}

function pushButtonPressed(image) {
	var o = getObjectFromRepository(image.id);
	o.setPressed( !o.isPressed() );
}

function releaseButtonPressed(image) {
	var o = getObjectFromRepository(image.id);
	o.setPressed( !o.isPressed() );
	o.notify();
}
