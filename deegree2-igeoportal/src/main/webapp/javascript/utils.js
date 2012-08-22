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

/**
 * return the value of the radio button that is checked
 * return an empty string if none are checked, or there are no radio buttons
 * 
 */
function getCheckedValue(radioObj) {
	if( !radioObj ) {
		return "";
	}
	var radioLength = radioObj.length;
	if ( radioLength == undefined ) {
		if( radioObj.checked ) {
			return radioObj.value;
		} else {
			return "";
		}
	}
	for(var i = 0; i < radioLength; i++) {
		if( radioObj[i].checked ) {
			return radioObj[i].value;
		}
	}
	return "";
}

/**
* returns the selected value of a selection element that is identified by its ID
* If doc is null document the script is running will be used
*/
function getSelectedValue( id, doc, defaultValue ) {
	if ( doc == null ) {
		doc = document;
	}
	try {
		var select = doc.getElementById( id );	
		return select.options[select.options.selectedIndex].value;
	} catch (e) {
		return defaultValue;
	}
}

/**
* returns an array of selected values of a selection element that is identified by its ID
* If doc is null document the script is running will be used
*/
function getSelectedValues( id, doc ) {
	if ( doc == null ) {
		doc = document;
	}
	var select = doc.getElementById( id );
	var ar = new Array();
	for ( var i = 0; i < select.options.length; i++) {
		if ( select.options[i].selected ){
			ar.push( select.options[i].value );
		}
	}
	return ar;
}

/**
* returns an array of all values of a selection element that is identified by its ID
* If doc is null document the script is running will be used
*/
function getAllValues( id, doc ) {
	if ( doc == null ) {
		doc = document;
	}
	var select = doc.getElementById( id );
	var ar = new Array();
	for ( var i = 0; i < select.options.length; i++) {
		ar.push( select.options[i].value );		
	}
	return ar;
}

/**
 * removes all options from a select elements
 */
function clearSelect(id, doc) {
	if ( doc == null ) {
		doc = document;
	}
	var select = doc.getElementById( id );
	var ar = new Array();
	for ( var i = 0; i < select.options.length; i++) {
		ar.push( select.options[i]);		
	}
	for ( var i = 0; i < ar.length; i++) {
		removeElement( ar[i] );		
	}
}

/**
* Unselects all values/options of a selection element
* If doc is null document the script is running will be used.
*/
function unselectAll( id, doc ) {
	if ( doc == null ) {
		doc = document;
	}
	var select = doc.getElementById( id );
	for (var i = 0; i < select.options.length; i++) {
		select.options[i].selected = false;
	}
}

/**
* set the option of a selection element which value matches the passed value to 
* be selected. If no matching value can be found nothing happens.
* If doc is null document the script is running will be used.
*/
function setSelectedValue( id, value, doc ) {
	if ( doc == null ) {
		doc = document;
	}
	var select = doc.getElementById( id );
	for (var i = 0; i < select.options.length; i++) {
		if ( select.options[i].value == value ) {
			select.options[i].selected = true;
		}
	}
}

/**
* set the option of a selection element with passed index to
* be selected. If no matching value can be found nothing happens.
* If doc is null document the script is running will be used.
*/
function setSelectedValueByIndex( id, index, doc ) {
	alert( id + " " + index);
	if ( doc == null ) {
		doc = document;
	}
	var select = doc.getElementById( id );
	select.options[index].selected = true;
}

/**
* set the option of a selection elements which values matches one of 
* the passed values to  be selected. If no matching value can be found 
* nothing happens.
* If doc is null document the script is running will be used.
*/
function setSelectedValues( id, values, doc ) {
	if ( doc == null ) {
		doc = document;
	}
	var select = doc.getElementById( id );
	for (var i = 0; i < select.options.length; i++) {
		for (var j = 0; j < values.length; j++) {
			if ( select.options[i].value == values[j] ) {
				select.options[i].selected = true;
			}
		}
	}
}

/**
* set the option of a selection elements with passed indexes to
* be selected. 
* If doc is null document the script is running will be used.
*/
function setSelectedValuesByIndex( id, indexes, doc ) {
	if ( doc == null ) {
		doc = document;
	}
	var select = doc.getElementById( id );
	for (var j = 0; j < indexes.length; j++) {
		select.options[indexes[j]].selected = true;
	}
}

/**
* returns the value of an input element identified by its ID. If no element with passed ID
* exists null will be retruned. If doc is null document the script is running will be used.
*/
function getInputValue(id, doc) {
	if ( doc == null ) {
		doc = document;
	}
	var elem = doc.getElementById( id );
	if ( elem == null ) {
		return null;
	} 
	return elem.value;
}

/**
* sets the value of an input element identified by its ID
* If doc is null document the script is running will be used
*/
function setInputValue(id, value, doc) {
	if ( doc == null ) {
		doc = document;
	}
	var elem = doc.getElementById( id );
	if ( elem == null ) {
		throw new Exception( "setInputValue", "element with id " + id + " does not exist" );
	} 
	elem.value = value;
}

/**
* returns the value of an element identified by its ID. If no element with passed ID
* exists null will be retruned. If doc is null document the script is running will be used.
*/
function getElementValue(id, doc) {
	if ( doc == null ) {
		doc = document;
	}
	var elem = doc.getElementById( id );
	if ( elem == null ) {
		return null;
	} 
	return elemt.innerHTML;
}

/**
* sets the value of an element identified by its ID
* If doc is null document the script is running will be used
*/
function setElementValue(id, value) {
	if ( doc == null ) {
		doc = document;
	}
	var elem = doc.getElementById( id );
	if ( elem == null ) {
		throw new Exception( "setInputValue", "element with id " + id + " does not exist" );
	} 
	elem.innerHTML = value;
}

/**
* works in IE 6, IE 7 and Firefox 2 & 3
* select: the select-element where to append the new option
* text: text of the option
* value: value of the option
*/
function appendOption( select, text, value, selected, tooltip, doc ){
	if (selected == null) {
		selected = false;
	}
	if ( doc == null ) {
		doc = document;
	}
	var newOption = doc.createElement('option');
	newOption.value = value;
	select.appendChild(newOption);
	newOption.text = text;
	if ( tooltip != null ) {
		newOption.title = tooltip;
	}
	newOption.selected = selected;
	return newOption; 
}

/**
 * removes all child nodes from passed element 
 * @param element
 */
function removeChildren(element) {
	while ( element.childNodes.length > 0 ) {
		element.removeChild( element.childNodes[0] );
	}
}

/**
 * removes passed element from dom tree 
 * @param element
 */
function removeElement(element) {
	element.parentNode.removeChild( element );
}

/**
 * 
 * @param id
 * @param doc
 * @return element identified 
 */
function getElement(id, doc) {
	if ( doc == null ) {
		doc = document;
	}
	return doc.getElementById( id );	
}

/**
 * 
 * @param tagName
 * @param id
 * @param name element name attribute
 * @param contentString set if element should contain a string (e.g. <a>my string</a>). If
 *						contentString is null it will be ignored 						
 * @param parentElement where new element should be append too. If parentElement is null
 * 		  the new element will not be append to any thing 
 * @param doc If doc is null document the script is running will be used
 * @return new element
 */
function createElement( tagName, id, name, contentString, parentElement, doc) {
	if ( doc == null ) {
		doc = document;
	}
	var element = doc.createElement( tagName );
	element.id = id;
	element.name = name;
	if ( contentString != null ) {
		element.appendChild( doc.createTextNode( contentString ) );
	}
	if ( parentElement != null ) {
		parentElement.appendChild( element );
	}
	return element;
}

function getRootURL() {
	var tmp = window.location.pathname.split( '/' );
    var s = '';
    for ( var i = 0; i < tmp.length-3; i++) {
        s += (tmp[i] + '/' ); 
    } 
    return window.location.protocol + '//' + window.location.host + s;
}
