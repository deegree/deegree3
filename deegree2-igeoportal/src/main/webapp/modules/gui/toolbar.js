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

function Toolbar(moduleName, buttonGroups, width, height, bgcolor) {
	
	// variable declaration
	this.moduleName = moduleName;
	this.buttonGroups = buttonGroups;
	this.height = height;
	this.width = width;
	this.bgcolor = bgcolor;
	
	// method declaration
	this.getModuleName = getModuleName;	
	this.getButtonGroups = getButtonGroups;
	this.getButtonGroup = getButtonGroup;
	this.getButtonGroupById = getButtonGroupById;
	this.addButtonGroup = addButtonGroup;
	this.removeButtonGroup = removeButtonGroup;
	this.paint = paint;
	this.repaint = repaint;
	this.createTable = createTable;
	this.createRow = createRow;
	this.createColumn = createColumn;
	this.reset = reset;

	// implementation
	function getModuleName() {
		return this.moduleName;
	}

	function getButtonGroups() {
		return this.buttonGroups;
	}

	function getButtonGroup(index) {
		return this.buttonGroups[index];
	}

	function getButtonGroupById(id) {
		for (var i = 0; i < this.buttonGroups.length; i++) {
			if ( this.buttonGroups[i].getId == id ) {
				return this.buttonGroups[i];
			}
		}
	}

	function addButtonGroup(buttonGroup) {
		this.buttonGroups.push( buttonGroup );
	}

	function removeButtonGroup(buttonGroup) {
		var bgs = new Array();
		for (var i = 0; i < this.buttonGroups.length; i++) {
			if ( this.buttonGroups[i].getId != id ) {
				bgs.push( this.buttonGroups[i] );
			}
		}
		this.buttonGroups = bgs;
	}

	function paint(targetDocument, parentElement) {
		var node = targetDocument.getElementById(moduleName);
		var table = this.createTable(targetDocument, parentElement);
		var row = this.createRow( targetDocument );
		table.firstChild.appendChild( row );		
		for (var i = 0; i < this.buttonGroups.length; i++) {
			var col = this.createColumn( targetDocument );
			this.buttonGroups[i].paint( targetDocument, col ); 
			row.appendChild( col );
		}
		if ( node != null ) {
			var parentElement = node.parentNode;
			parentElement.replaceChild( node, table );
		} else {
			parentElement.appendChild( table );
		}
	}
	
	function repaint() {}

	function createTable(targetDocument) {
		var table = targetDocument.createElement("TABLE");
		var oTBody0 = targetDocument.createElement("TBODY");
		table.setAttribute("border", 0);
        //JM: refactored (css)
        //if param bgcolor is set in WMC, this overrides settings in deegree.css
        if ( this.bgcolor != null && this.bgcolor != "undefined" && this.bgcolor != "null" && this.bgcolor != "" && this.bgcolor != "-" ) {
            table.style.backgroundColor = this.bgcolor;
        }
		table.setAttribute("id", "TOOLBAR" + moduleName);
		table.setAttribute("cellpadding", 0);
		table.setAttribute("cellspacing", 0);
		table.setAttribute("height", height);
        // IE problem: width cannot be set, if buttons shall be aligned in center 
        //table.setAttribute("width", width);
        table.setAttribute("width", "100%");
        table.setAttribute("align", "left");
		table.appendChild( oTBody0 );
		return table;
	}

	function createRow( targetDocument ) {
		var tr = targetDocument.createElement("tr");
        tr.setAttribute("height", height);
        //JM: refactored (css)
        //if param bgcolor is set in WMC, this overrides settings in deegree.css
        if ( this.bgcolor != null && this.bgcolor != "undefined" && this.bgcolor != "null" && this.bgcolor != "" && this.bgcolor != "-" ) {
            tr.style.backgroundColor = this.bgcolor;
        }
        return tr;
	}

	function createColumn( targetDocument ) {
		var td = targetDocument.createElement("td");
    	td.setAttribute("align", 'left');
        //JM: refactored (css)
        //if param bgcolor is set in WMC, this overrides settings in deegree.css
		if ( this.bgcolor != null && this.bgcolor != "undefined" && this.bgcolor != "null" && this.bgcolor != "" && this.bgcolor != "-" ) {
            td.style.backgroundColor = this.bgcolor;
        }
    	td.setAttribute("width", height);
        
        //td.setAttribute("valign", 'top'); // doesn't work in IE !!!
        var attrNode = targetDocument.createAttribute( "valign" );
        attrNode.nodeValue = "top";
        td.setAttributeNode( attrNode );
        
        return td;
	}
	
	function reset() {
		for (var i = 0; i < this.buttonGroups.length; i++) {
			this.buttonGroups[i].reset();
		}
	}

}