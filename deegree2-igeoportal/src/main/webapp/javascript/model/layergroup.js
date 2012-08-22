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

/*
 * represent a group of layers served by the same web service
 */
function LayerGroup(id, serviceType, serviceName, serviceURL, layers) {

    // variables declariation
    // layers --> Array
    this.layers = layers;
    this.id = id;
    this.serviceName = serviceName;
    this.serviceURL = serviceURL;
    this.serviceType = serviceType;
    this.changed = true;
    this.format = 'image/gif';
    this.transparency = true;
    this.bgColor = '0xFFFFFF';

    // method declaration
    this.getLayers = getLayers;
    this.setLayers = setLayers;
    this.getLayer = getLayer;
    this.swapLayers = swapLayers;
    this.insertLayer = insertLayer;
    this.addLayer = addLayer;
    this.removeLayer = removeLayer;
    this.removeLayerByIndex = removeLayerByIndex;
    this.getServiceName = getServiceName;
    this.getServiceURL = getServiceURL;
    this.getServiceType = getServiceType;
    this.getId = getId;
    this.isChanged = isChanged;
    this.setChanged = setChanged;
    this.getLayersLength = getLayersLength;

    this.getTransparency = getTransparency;
    this.setTransparency = setTransparency;
    this.getFormat = getFormat;
    this.setFormat = setFormat;
    this.getBGColor = getBGColor;
    this.setBGColor = setBGColor;
    this.getLayersLength = getLayersLength;
    this.getElevation = getElevation;
    this.setElevation = setElevation;
    this.getTime = getTime;
    this.setTime = setTime;

    //implementation
    function getId() {
        return this.id;
    }

    function getLayers() {
        return this.layers;
    }

    function setLayers(layers) {
        this.layers = layers;
        this.setChanged( true );
    }

    function getLayer(index) {
        return this.layers[index];
    }

    function getLayersLength(){
        return this.layers.length;
    }

    function swapLayers(index1, index2) {
        var tmp  = this.layers[index1];
        this.layers[index1] = this.layers[index2];
        this.layers[index2] = tmp;
        this.setChanged( true );
    }

    function insertLayer(index, layer) {
        this.layers.push(layer);
        for (var j = this.layers.length-1; j > index; j--){
            this.layers[j] = this.layers[j-1];
        }
        this.layers[index] = layer;
        this.setChanged( true );
    }

    function addLayer(layer) {
        this.layers.push(layer);
        this.setChanged( true );
    }

    function removeLayer(layer) {
        for (var i = 0; i < this.layers.length; i++){
            if(this.layers[i] == layer) {
                this.removeLayerByIndex(i);
                break;
            }
        }
        this.setChanged( true );
    }

    function removeLayerByIndex(index) {
        for (var j = index; j < this.layers.length-1; j++){
            this.layers[j] = this.layers[j+1];
        }
        this.layers.pop();
        this.setChanged( true );
    }

    function getServiceName() {
        return this.serviceName;
    }

    function getServiceURL() {
        return this.serviceURL;
    }

    function getServiceType() {
        return this.serviceType;
    }

    function isChanged() {
        var c = false;
        for (var i = 0; i < this.layers.length; i++) {
            c = this.layers[i].isChanged();
            if ( c ) {
                break;
            }
        }
        return this.changed || c;
    }

    function setChanged(changed) {
        this.changed = changed;
        for (var i = 0; i < this.layers.length; i++) {
            this.layers[i].setChanged(changed);
        }
    }

    function getElevation (){
        return this.elevation;
    }

    function setElevation(elevation){
        this.elevation = elevation;
        this.changed = true;
    }

    function getTime () {
        return this.time;
    }

    function setTime(time){
        this.time = time;
        this.changed = true;
    }

    function getTransparency() {
        return this.transparency;
    }

    function setTransparency(transparency) {
        this.transparency = transparency;
        this.changed = true;
    }

    function getFormat() {
        return this.format;
    }

    function setFormat(format) {
        this.format = format;
        this.changed = true;
    }

    function getBGColor() {
        return this.bgColor;
    }

    function setBGColor(bgColor) {
        this.bgColor = bgColor;
        this.changed = true;
    }
}
