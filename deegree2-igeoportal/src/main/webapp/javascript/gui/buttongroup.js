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

function ButtonGroup(id) {

    // variable declaration
    this.id = id;
    this.buttons = new Array();

    // method declaration
    this.getId = getId;
    this.getMode = getMode;
    this.setMode = setMode;
    this.getButtons = getButtons;
    this.getButton = getButton;
    this.getButtonByMode = getButtonByMode;
    this.addButton = addButton;
    this.removeButton = removeButton;
    this.reset = reset;
    this.paint = paint;
    this.createTable = createTable;
    this.createColumn = createColumn;
    this.repaint = repaint;

    // implementation
    function getId() {
        return id;
    }

    function getMode() {
        for (var i = 0; i < this.buttons.length; i++) {
            if ( this.buttons[i].isPressed() ) {
                return this.buttons[i].getMode();
            }
        }
        return null;
    }

    function setMode(mode) {
        this.reset();
        var bt = this.getButtonByMode( mode );
        if ( bt != null ) {
            this.buttons[i].setPressed(true);
        }
    }

    function getButtons() {
        return this.buttons;
    }

    function getButton(index) {
        return this.buttons[index];
    }

    function getButtonByMode(mode) {
        for (var i = 0; i < this.buttons.length; i++) {
            if ( this.buttons[i].getMode() == mode ) {
                return this.buttons[i];
            }
        }
        return null;
    }

    function addButton(button) {
        this.buttons.push(button);
        button.setButtonGroup(this);
    }

    function removeButton(button) {
        bts = new Array();
        for (var i = 0; i < this.buttons.length; i++) {
            if ( this.buttons.getMode() != button.getMode() ) {
                bts.push( this.buttons[i] );
            }
        }
        this.buttons = bts;
    }

    function reset() {
        for (var i = 0; i < this.buttons.length; i++) {
            this.buttons[i].setPressed( false );
        }
    }

    function paint(targetDocument, parentElement) {
        var node = targetDocument.getElementById(id);
        var table = this.createTable(targetDocument, parentElement);
        var row = targetDocument.createElement("tr");
        table.firstChild.appendChild(row);
        for (var i = 0; i < this.buttons.length; i++) {
            var col = this.createColumn( targetDocument );
            this.buttons[i].paint( targetDocument, col );
            row.appendChild( col );
            if ( i < this.buttons.length-1) {
                col = this.createColumn( targetDocument );
                var space = targetDocument.createElement("img");
                space.setAttribute("src", "../../images/space.gif");
                space.setAttribute("border", "0");
                space.setAttribute("alt", " ");
                space.setAttribute("width", "7");
                space.setAttribute("height", "20");
                col.appendChild( space );
                row.appendChild( col );
            }
        }
        if ( node != null ) {
            var parentElement = node.parentNode;
            parentElement.replaceChild( node, table );
        } else {
            parentElement.appendChild( table );
        }
    }

    function repaint() {
        for (var i = 0; i < this.buttons.length; i++) {
            this.buttons[i].repaint();
        }
    }

    function createTable(targetDocument) {
        var table = targetDocument.createElement("TABLE");
        var oTBody0 = targetDocument.createElement("TBODY");
        table.setAttribute("border", 0);
        table.setAttribute("id", "BTGROUP" + id);
        table.setAttribute("cellPadding", 0);
        table.setAttribute("cellSpacing", 0);
        table.setAttribute( "align", "left" );
        table.appendChild( oTBody0 );
        return table;
    }

    function createColumn( targetDocument ) {
         var td = targetDocument.createElement("td");
         td.setAttribute("valign", 'middle');
         td.setAttribute("align", 'center');
         return td;
    }
}
