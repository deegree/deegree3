//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
function Nominatim_search() {

	// variables
	this.parentDoc; 
	this.parentElement;
	this.url;
	
	// method declaration
	this.paint = paint;
	this.repaint = repaint;
	this.search = search;
	this.zoomTo = zoomTo;
	this.setURL = setURL;
	
	function paint(parentDoc, parentElement) {
		this.parentDoc = parentDoc; 
		this.parentElement = parentElement;
	}
	
	function repaint() {
	}
	
	function setURL(url) {
		this.url = url; 
	}
	
	/**
	 * starts a query against Nominatim by sending required informations to a server sided listener
	 * @param url
	 * 
	 */
	function search() {
		var queryString = getInputValue( 'queryString', this.parentDoc );
		try {
            submitGetRequest( this.url + "?action=searchNominatim&queryString=" + queryString, handleSearchNominatim, null, false );
        } catch(e) {
            alert( 1 + JSON.stringify( e ) );
        }
	}
	
	/**
	 * zooms to bounding box identified by passed osm_id 
	 * @param osm_id
	 */
	function zoomTo(osm_id) {
		try {
            submitGetRequest( this.url + "?action=getBBOX4OSM_ID&OSM_ID=" + osm_id, handleGetBBOX4OSM_ID, null, false );
        } catch(e) {
            alert( 1 + JSON.stringify( e ) );
        }
	}
	
}

function handleSearchNominatim(result) {
	var places = JSON.parse( result );
	var ul = getElement( 'resultList', controller.vNominatim_search.parentDoc );
	removeChildren( ul );
	var s = "";
	for ( var i = 0; i < places.length; i++ ) {		
		s = s + "<div class='pNominatim_searchLI'><div style='margin:10px'><a href='javascript:parent.controller.vNominatim_search.zoomTo(\"";
		s = s + places[i][1] + "\")'>" + places[i][0] + "</a></div></div>"; 
	}
	ul.innerHTML = s;
}

function handleGetBBOX4OSM_ID(result) {
	if ( result != 'ERROR' ) {
		var tmp = result.split( ',' );
	    var env = new Envelope( parseFloat( tmp[0] ), parseFloat( tmp[1] ), parseFloat( tmp[2] ), parseFloat( tmp[3] ) );
	    env = env.getBuffer( env.getWidth() / 20 );
	    controller.actionPerformed( new Event( this, "BBOX", env ) );
	} else {
		alert( 'ERROR' );
	}
}