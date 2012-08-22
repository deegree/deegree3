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

function Envelope(minx, miny, maxx, maxy) {
	this.minx = minx;
	this.miny = miny;
	this.maxx = maxx;
	this.maxy = maxy;

	// method declaration
	this.getBuffer = getBuffer;
	this.toString = toString;
	this.getWidth = getWidth;
	this.getHeight = getHeight;
	this.getCentroid = getCentroid;

	// implementation
	function getBuffer(buffer) {
		return new Envelope(this.minx - buffer, this.miny - buffer, this.maxx
				+ buffer, this.maxy + buffer);
	}

	function toString() {
		return this.minx + ';' + this.miny + ';' + this.maxx + ';' + this.maxy;
	}

	function getWidth() {
		return this.maxx - this.minx;
	}

	function getHeight() {
		return this.maxy - this.miny;
	}

	function getCentroid() {
		var dx = this.getWidth() / 2.0;
		var dy = this.getHeight() / 2.0;
		return new Point(this.minx + dx, this.miny + dy, null);
	}
}
