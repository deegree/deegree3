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

function GeoTransform(srcx1, srcy1, srcx2, srcy2, destx1, desty1, destx2, desty2) {
    this.qx = 0.0;
    this.qy = 0.0;
    
    this.srcx1 = srcx1;
    this.srcy1 = srcy1;
    this.srcx2 = srcx2;
    this.srcy2 = srcy2;
    this.destx1 = destx1;
    this.desty1 = desty1;
    this.destx2 = destx2;
    this.desty2 = desty2;

    this.getDestX = getDestX;
    this.getDestY = getDestY;
    this.getSourceX = getSourceX;
    this.getSourceY = getSourceY;
    this.calculateQX = calculateQX;
    this.calculateQY = calculateQY;
    this.calculateQX();
    this.calculateQY();
        
}

function getDestX( srcx ) {
    return this.destx1 + (srcx - this.srcx1) * this.qx;
}

function getDestY( srcy ) {
    return this.desty1 + (this.desty2-this.desty1) - (srcy - this.srcy1) * this.qy;
}

function getSourceX( destx ) {  	  
    return (destx - this.destx1) / this.qx - (-this.srcx1);
}

function getSourceY( desty ) {
    return ( (this.desty2 - this.desty1) - (desty - this.desty1) ) / this.qy - (- this.srcy1);
}

function calculateQX() {
    this.qx = (this.destx2-this.destx1)/(this.srcx2-this.srcx1);
}
    
function calculateQY() {
    this.qy = (this.desty2-this.desty1)/(this.srcy2-this.srcy1);
}
