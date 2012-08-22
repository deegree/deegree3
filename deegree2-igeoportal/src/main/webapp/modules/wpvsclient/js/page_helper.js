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

function setModeForClient( m ){
  overviewFrame.setMode( m );
  var button = null;
  for(var i=0;i< buttons.length;i++){
    button = document.getElementById( buttons[i] );
    var tmpButton = buttons[i];
    if( buttons[i].toLowerCase() == m.toLowerCase() ){
      tmpButton += "_a";
    }
    button.src = "../images/" + tmpButton + ".gif";
  }
}
	
function initGeoTransform(){
  var b = wpvsRequest.getBboxAsArray();

  gtrans = new GeoTransform( b[0], b[1], b[2], b[3], 0, 0, oviewWidth-1, oviewHeight-1 );

}

function setSplitter( s ){
  wpvsRequest.setSplitter( s );
  redraw(false);
}
	
function setFooterText( text ){
  setElementText( text, 'footerTxtArea' );
}

function setElementText( text, elementId ){
  var txtArea = document.getElementById( elementId );
		
  var node = txtArea.firstChild;
  if ( node != null && txtArea != null ){
    txtArea.removeChild( node );
  }
	
  node = document.createTextNode( text );
  txtArea.appendChild( node );
}	
