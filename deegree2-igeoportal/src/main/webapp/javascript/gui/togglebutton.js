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

function ToggleButton(mode, label, icon, pressedIcon) {

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
    this.getMode = getMode;
    this.setListener = setListener;
    this.isPressed = isPressed;
    this.setPressed = setPressed;
    this.paint = paint;
    this.createIMG = createIMG;
    this.setButtonGroup = setButtonGroup;
    this.getButtonGroup = getButtonGroup;
    this.repaint = repaint;
    this.notify = notify;

    // implementation

    function getType() {
        return 'ToggleButton';
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
            var event = new Event( this, this.mode + "Pressed", this.mode );
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
        if ( this.buttonGroup != null ) {
            this.buttonGroup.repaint();
        } else {
            this.repaint();
        }
    }

    function paint(targetDocument, parentElement) {
        this.targetDocument = targetDocument;
        var img = this.createIMG(targetDocument);
        var node = targetDocument.getElementById(mode);
        if ( node != null ) {
            var parentElement = node.parentNode;
            parentElement.replaceChild( node, img );
        } else {
            parentElement.appendChild( img );
        }
    }

    function repaint() {
        var node = this.targetDocument.getElementById(mode);
        var parent = node.parentNode;
        if ( parent == null ) {
            parent = node.parentElement;    
        }
        if ( parent != null ) {
            if ( this.pressed ) {
                //IE
                if ( typeof ActiveXObject != 'undefined' ) {
                    parent.setAttribute("className", "buttonIconPressed");    
                } else {
                //Firefox
                    parent.setAttribute("class", "buttonIconPressed");
                }
            } else {
                //IE
                if ( typeof ActiveXObject != 'undefined' ) {
                    parent.setAttribute("className", "buttonIcon");
                } else {
                //Firefox
                    parent.setAttribute("class", "buttonIcon");
                }
            }
        }
        node.src = this.currentIcon;
    }

    function createIMG(targetDocument) {
      
      var img = targetDocument.createElement("img");
      img.setAttribute("src", this.currentIcon);
      img.setAttribute("id", this.mode);
      img.setAttribute("alt", this.label);
      img.setAttribute("title", this.label);
      /* The size of button icons is set to a fixed value. 
      If you want to change these values, or even disable limiting image sizes,
      then please make sure that these changes are working on all IE versions */
      img.setAttribute("width", 16 );
      img.setAttribute("height", 16 );
      img["onclick"]= new Function( 'swapToggleButtonPressed(this)' );
      addObjectToRepository( this.mode, this );
      var div = targetDocument.createElement("div");
      //IE
        if ( typeof ActiveXObject != 'undefined' ) {
            div.setAttribute("className", "buttonIcon");    
        } else {
        //Firefox
            div.setAttribute("class", "buttonIcon");
        }
      div.appendChild(img);
      return div;
    }
}

function swapToggleButtonPressed(image) {
    var o = getObjectFromRepository(image.id);
    o.getButtonGroup().reset();
    o.setPressed( true );
    o.notify();
}