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

function BBoxInput1 (id) {	
	this.id = id;
	this.targetDocument = null;
	this.parentNode = null;

	this.paint = paint;		
	this.repaint = repaint;
	this.updateMap = updateMap;        
	
	function paint(targetDocument, parentNode) {
		this.targetDocument = targetDocument;
		this.parentNode = parentNode;	
                this.repaint();
	}
	
	function repaint() {
		var bbox = parent.controller.mapModel.getBoundingBox();
		var minx = this.targetDocument.getElementById( 'WEST' );
		minx.value = bbox.minx;
		var maxx = this.targetDocument.getElementById( 'EAST' );
		maxx.value = bbox.maxx;
		var miny = this.targetDocument.getElementById( 'SOUTH' );
		miny.value = bbox.miny;
		var maxy = this.targetDocument.getElementById( 'NORTH' );
		maxy.value = bbox.maxy;
	}

	function updateMap( minx, miny, maxx, maxy ) {
		var env = new Envelope(  minx, miny, maxx, maxy );
        	var event = new Event( '', 'BBOX', env );
                parent.controller.actionPerformed( event );
	}
}