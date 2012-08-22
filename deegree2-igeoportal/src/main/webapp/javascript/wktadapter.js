//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) (2005) by:

 Florian Rengers / grit GmbH

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

 Florian Rengers, grit GmbH
 Landwehrstraße 143
 59368 Werne
 http://www.grit.de

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
 * 
 * @class
 *
 * Klasse erzeugt Geometrieobjekte aus Geometrien im Well Known Text Format (WKT)
 *
 * 
 * @constructor
 * @param editObject zentrales Objekt des Editier-Moduls 
 */ 
function WKTAdapter() {

	this.geometryFactory = new GeometryFactory();

    /**
     * Generiert Geometrien aus WKT.
     *
     * @param wkt Geometrie im WKT-Format              
     * @param fid Geometrie-Id
     * @return    Geometrie
     * @type      Geometry
     */
    this.geometryFromText = function(wkt, fid) {
        geomType = wkt.split( "(" , 1 )[0]; //JS 1.2 und IE4; Anzahl der Teilstücke begrenzt auf 1
        if (geomType == "POINT") return this.pointFromText(wkt, fid);
        else if (geomType == "LINESTRING") return this.lineFromText(wkt, fid);
        else if (geomType == "POLYGON") return this.polyFromText(wkt, fid);
        else if (geomType == "MULTIPOLYGON") return this.mPolyFromText(wkt, fid);
        //TODO: MultiLineString, MultiPoint, ...?
    }

    /**
     * Generiert Point-Geometrie aus WKT.
     *
     * @param wkt Geometrie im WKT-Format              
     * @param crs Coordinate Reference System
     * @return    Geometrie
     * @type      Point
     */
    this.pointFromText = function(wkt, crs) {        
        wkt = wkt.substring( 6, wkt.length-1 );
        coord = wkt.split(" ");        
        for (var i=0; i<coord.length; i++) {
            coord[i] = parseFloat(coord[i]);
        }
        return this.geometryFactory.createPoint(coord[0], coord[1], crs);
    }
    
    /**
     * Generiert Line-Geometrie aus WKT.
     *
     * @param wkt Geometrie im WKT-Format              
     * @param fid Geometrie-Id
     * @return    Geometrie
     * @type      LineString
     */
    this.lineFromText = function(wkt, fid) {
        wkt = wkt.substring(11, wkt.length-1);
        coordPairs = wkt.split(",");
        points = new Array();
		for (var i=0; i<coordPairs.length; i++) {
	    	coord = coordPairs[i].split(" ");
	   		points[i] = this.geometryFactory.createPoint( parseFloat(coord[0]), parseFloat(coord[1]), null);	    
        }
        return this.geometryFactory.createLineStringFromPoints( points, null );
    }
    
    /**
     * Generiert Polygon-Geometrie aus WKT.
     *
     * @param wkt Geometrie im WKT-Format              
     * @param fid Geometrie-Id
     * @return    Geometrie
     * @type      Polygon
     */
    this.polyFromText = function(wkt, fid) {
        var ext = new Array();
        //interiorRings = new Array();
        if (wkt.indexOf( "((" ) > 0) {
            wkt = wkt.substring( 9,  wkt.length-1 );
            var pos = wkt.indexOf( ")" );
            var tmp = wkt.substring(0, pos);
            //external ring
            coordPairs = new Array();
            coordPairs = tmp.split(",");
		    for (var i=0; i<coordPairs.length; i++) {
		        coord = coordPairs[i].split(" ");
	            ext[i] = this.geometryFactory.createPoint( parseFloat(coord[0]), parseFloat(coord[1]), null );
            }            
            /*
            ignore inner rings because can not be drawn by wz_graphics
            if (pos+3 < wkt.length) {
                wkt = wkt.substring( pos+3, wkt.length);
                var inters = -1;//index fuer ID-Vergabe
                while (wkt.indexOf( ")" ) > 0) {
                    inters++;
                    pos = wkt.indexOf( ")" );
                    tmp = wkt.substring( 0,  pos );
                    //internal ring(s)
                    coordPairs = new Array();
                    coordPairs = tmp.split(",");
                    intern = new Array();
                    for (var i=0; i<coordPairs.length; i++) {
		        coord = coordPairs[i].split(" ");
		        intern[i] = this.editObject.geometryFactory.createPoint(fid + "." + this.editObject.idGenerator.generate(), parseFloat(coord[0]), parseFloat(coord[1]), this.editObject.getClientWrapper().getEPSG());		        
                    }
                    interiorRings[interiorRings.length] = this.editObject.geometryFactory.createLinearRing(fid + "." + this.editObject.idGenerator.generate(), intern, this.editObject.getClientWrapper().getEPSG());                    
                    if (pos+3 < wkt.length) {
                        wkt = wkt.substring(pos+3, wkt.length);
                    } else {
                        break;
                    }
                }
            }
            */
        }
        return this.geometryFactory.createPolygonFromPoints( ext );
    }

    /**
     * Generiert MultiPolygon-Geometrie aus WKT.
     *
     * @param wkt Geometrie im WKT-Format              
     * @param fid Geometrie-Id
     * @return    Geometrie
     * @type      MultiPolygon
     */
    this.mPolyFromText = function(wkt, fid) {
        surfaces = new Array();
        srfcs = new Array();
        wkt = wkt.substring(13);
        // for each polygon
        var surfs = -1; //index fuer ID-Vergabe
        while ( wkt.indexOf( "((" ) > -1) {
            index++;
            ext = new Array();
            pos1 = wkt.indexOf( "))" );
            tmp = wkt.substring( 2, pos1+1 );
            pos = tmp.indexOf( ")" );
            tmp2 = tmp.substring( 0,  pos );
            coordPairs = new Array();
            coordPairs = tmp2.split(",");
            
            for (var i=0; i<coordPairs.length; i++) {
	        coord = coordPairs[i].split(" ");
		ext[i] = this.editObject.geometryFactory.createPoint(fid + "." + this.editObject.idGenerator.generate(), parseFloat(coord[0]), parseFloat(coord[1]), this.editObject.getClientWrapper().getEPSG());		
            }
            exteriorRing = this.editObject.geometryFactory.createLinearRing(fid + "." + this.editObject.idGenerator.generate(), ext, this.editObject.getClientWrapper().getEPSG());            
            interiorRings = new Array();
            if (pos+3 < tmp.length) {
                tmp = tmp.substring(pos+3,  tmp.length);
                var inters = -1;//index fuer ID-Vergabe
                while ( tmp.indexOf( ")" ) > 0 ) {
                    inters++;
                    pos = tmp.indexOf( ")" );
                    tmp2 = tmp.substring( 0,  pos );
            
                    coordPairs = new Array();
                    coordPairs = tmp2.split(",");           
                    intern = new Array();
                    for (var i=0; i<coordPairs.length; i++) {
	                coord = coordPairs[i].split(" ");
		        intern[i] = this.editObject.geometryFactory.createPoint(fid + "." + this.editObject.idGenerator.generate(), parseFloat(coord[0]), parseFloat(coord[1]), this.editObject.getClientWrapper().getEPSG());		        
                    }

                    interiorRings[interiorRings.length] = this.editObject.geometryFactory.createLinearRing(fid + "." + this.editObject.idGenerator.generate(), intern, this.editObject.getClientWrapper().getEPSG());                    
                    if (pos+3 < tmp.length) {
                        tmp = tmp.substring( pos+3, tmp.length);
                    } else {
                        break;
                    }
                }
            }
            surfaces[surfaces.length] = this.editObject.geometryFactory.createPolygon(fid + "." + this.editObject.idGenerator.generate(), exteriorRing, interiorRings, this.editObject.getClientWrapper().getEPSG());            
            wkt = wkt.substring( pos1+3 );
        }        
        return this.editObject.geometryFactory.createMultiPolygon(fid, surfaces, this.editObject.getClientWrapper().getEPSG());
    }
}
