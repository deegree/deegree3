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
* feature class
*/
function Feature(id, type, properties, geometry) {
	
	// attributes
	this.id = id;
	this.type = type;
	this.properties = properties;
	this.geometry = geometry;
	
	// methods
	this.getGeometry = getGeometry;
	this.setGeometry = setGeometry;
	this.getProperty = getProperty;
	this.setProperty = setProperty;
	this.getProperties = getProperties;
	this.getFeatureType = getFeatureType;
	
	// implementation
	
	function getGeometry() {
		return this.geometry;
	}
	
	function setGeometry(geom) {
		this.geometry = geom;
	}
	
	function getProperty(localname) {
		return this.properties[0][name];
	}
	
	function setProperty(localname,value) {
		this.properties[0][localname] = value;
	}
	
	function getProperties() {
		return this.properties;
	}
	
	function getFeatureType() {
		return this.type;
	}
	
}


/**
* feature collection class
*/
function FeatureCollection(id, features) {
	
	// attributes
	this.id = id;
	this.features = features;
	
	// methods
	this.size = size;
	this.getFeatures = getFeatures;
	this.getFeature = getFeature;
	this.addFeature = addFeature;
	this.removeAll = removeAll;
	
	// implementation
	function size() {
		return this.features.length;
	}
	
	function getFeatures() {
		return this.features;	
	}
	
	function getFeature(index) {
		return this.features[index];	
	}
	
	function addFeature(feature) {
		this.features.push( feature );
	}
	
	function removeAll() {
		while ( this.features.length > 0 ) {
			this.features.pop();
		}
	}
}
