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

function createLayer(parentDoc, name, id, offsetx, offsety, width, hght, zIdx, bgColor, visible) {
	width = Math.round( width );
	hght = Math.round( hght );
	removeHTMLLayer(parentDoc, id);
	var newLayer = parentDoc.createElement("div");	
	newLayer.setAttribute("id", id);
	newLayer.setAttribute("name", name);
	newLayer.style.position = "absolute";
	newLayer.style.top = offsety + 'px';
	newLayer.style.left = offsetx + 'px';
	newLayer.style.zIndex = zIdx;
	newLayer.style.width = width + 'px';
	newLayer.style.height =  hght + 'px';
	newLayer.style.overflow = 'hidden';
	newLayer.style.visibility = visible ? 'visible' : 'hidden';
	if ( bgColor != null ) {
		newLayer.style.backgroundColor = bgColor;
	}

	return newLayer;
}

function createImage(src, targetDocument, layer, width, height, name ){
	var newImage = targetDocument.createElement("img");
	newImage.setAttribute("id", "imgID:"+name);
	newImage.setAttribute("name", name);
	newImage.setAttribute("src", src);
	newImage.setAttribute("border", 0);
	newImage.setAttribute("width", width );
	newImage.setAttribute("height", height );
	layer.appendChild(newImage);
	return "imgID:"+name;
}

function removeHTMLLayer(targetDocument, id) {
	removeNodeById( targetDocument, id );
}

function removeNodeById(targetDocument, id) {
	var node = targetDocument.getElementById( id );	
	if ( node != null ) {
	  node.parentNode.removeChild( node );
	}
}

function pausecomp(millis) {
	date = new Date();
	var curDate = null;
	
	do { var curDate = new Date(); }
	while(curDate-date < millis);
} 
