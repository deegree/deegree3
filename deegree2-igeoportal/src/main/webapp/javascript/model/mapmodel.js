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

function MapModel(layerlist, srs, boundingBox, width, height) {
	
	// attributes
	this.layerlist = layerlist;
	this.srs = srs;
	this.initialBBox = boundingBox;
	this.boundingBox = boundingBox;
	this.width = width;
	this.height = height;
	this.changed = true;
    this.scale = 0;
	this.featureCollections = new Array();
	
	// method declaration
	this.getLayerList = getLayerList;
	this.getSrs = getSrs;
	this.getBoundingBox = getBoundingBox;
	this.getInitialBoundingBox = getInitialBoundingBox;
	this.getWidth = getWidth;
	this.getHeight = getHeight;	
	this.setLayerList = setLayerList;
	this.setSrs = setSrs;
	this.setBoundingBox = setBoundingBox;
	this.setWidth = setWidth;
	this.setHeight = setHeight;	
	this.isChanged = isChanged;
	this.setChanged = setChanged;
    this.getScale = getScale;
    this.getScaleDenominator = getScaleDenominator;
	this.addFeatureCollection = addFeatureCollection;	
	this.removeFeatureCollection = removeFeatureCollection;	
	this.getFeatureCollections = getFeatureCollections;
	this.getFeatureCollection = getFeatureCollection;
	this.getLayerByName = getLayerByName;
	this.getLayerById = getLayerById;
	this.getLayer = getLayer;
	this.removeLayer = removeLayer;
	this.getTransformer = getTransformer;
	
	// implementation
	
	function getLayerList(){
		return this.layerlist;
	}
	
	function setLayerList(layerlist){
		this.layerlist = layerlist;
		this.setChanged(true);
	}
	
	function getBoundingBox(){
		return this.boundingBox;
	}
	
	function getInitialBoundingBox(){
		return this.initialBBox;
	}
	
	function setBoundingBox(boundingBox){
		this.boundingBox = boundingBox;
		this.setChanged(true);
	}
	
	function getSrs(){
		return this.srs;
	}
	
	function setSrs(srs){
		this.srs = srs;
		this.setChanged(true);
	}
	
	function getWidth(){
		return this.width;
	}
	
	function setWidth(width){
		this.width = width;
		this.setChanged(true);
	}
	
	function getHeight() {
		return this.height;
	}
	
	function setHeight(height){
		this.height = height
		this.setChanged(true);
	}
	
	function isChanged() {
		return this.changed || layerlist.isChanged();
	}
	
	function setChanged(changed) {
		this.changed = changed;
		layerlist.setChanged( changed );
	}
    
    function getScale() {
        var dx = (this.boundingBox.maxx-this.boundingBox.minx) / this.width;
        var dy = (this.boundingBox.maxy-this.boundingBox.miny) / this.height;
        this.scale = Math.sqrt((dx*dx)+(dy*dy)); 
        return this.scale;
    }
    
    function getScaleDenominator() {
    	var sqpxsize = Math.sqrt( 0.00028 * 0.00028 * 2 );
        return Math.round( this.getScale()/ sqpxsize );
    }
	
	function addFeatureCollection(name, featureCollection, rendering) {
		var l = this.featureCollections.length;
		this.featureCollections[l] = new Object();
		this.featureCollections[l]["name"] = name;
		this.featureCollections[l]["FC"] = featureCollection;
		this.featureCollections[l]["REND"] = rendering;
	}
	
	function removeFeatureCollection(name) {
		for (var i = 0; i < this.featureCollections.length; i++) {
			if ( this.featureCollections[i]["name"] == name ) {
				this.featureCollections.splice( i, 1 );
			}
		}
	}
	
	function getFeatureCollections() {
		return this.featureCollections;
	}
	
	function getFeatureCollection(name) {
		for (var i = 0; i < this.featureCollections.length; i++) {
			if ( this.featureCollections[i]["name"] == name ) {
				return this.featureCollections[i]["FC"];
			}
		}
	}
	
	function getLayerByName(name) {
		var groups = this.layerlist.getLayerGroups();
		for (var i = 0; i < groups.length; i++ ) {
			var layers = groups[i].getLayers();
			for (var j = 0; j < layers.length; j++ ) {
				if ( layers[j].getName() == name ) {
					return layers[j];
				}
			}
		}
	}
	
	function getLayerById(identifier) {
		var groups = this.layerlist.getLayerGroups();
		for (var i = 0; i < groups.length; i++ ) {
			var layers = groups[i].getLayers();
			for (var j = 0; j < layers.length; j++ ) {
				if ( layers[j].getIdentifier() == identifier ) {
					return layers[j];
				}
			}
		}
	}
	
	function getLayer(name, serviceURL) {
		var groups = this.layerlist.getLayerGroups();
		for (var i = 0; i < groups.length; i++ ) {
			var layers = groups[i].getLayers();
			if ( groups[i].getServiceURL() == serviceURL ) {
				for (var j = 0; j < layers.length; j++ ) {
					if ( layers[j].getName() == name ) {
						return layers[j];
					}
				}
			}
		}
	}
	
	function removeLayer(layer) {
		var groups = this.layerlist.getLayerGroups();
		for (var i = 0; i < groups.length; i++ ) {
			var layers = groups[i].getLayers();
			for (var j = 0; j < layers.length; j++ ) {
				if ( layers[j].getName() == layer.getName() ) {
					groups[i].removeLayer( layer );
					break;
				}
			}
			if ( groups[i].getLayers().length == 0 ) {
				this.layerlist.removeLayerGroupById( groups[i].id );
				break;
			}
		}
	}
	
	function getTransformer() {
		return new GeoTransform( this.boundingBox.minx, this.boundingBox.miny, 
				                 this.boundingBox.maxx, this.boundingBox.maxy, 
                                 0, 0, this.width - 1, this.height - 1 );
	}

}
