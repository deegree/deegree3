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

function LayerList(id)  {

    // variables declaration
    this.id = id;
    this.layerGroups = new Array();
    this.changed = true;

    // method declaration
    this.addLayerGroup = addLayerGroup;
    this.insertLayerGroupAt = insertLayerGroupAt;
    this.removeLayerGroupByIndex = removeLayerGroupByIndex;
    this.removeLayerGroupById = removeLayerGroupById;
    this.swapLayerGroupOrder = swapLayerGroupOrder;
    this.setLayerGroups = setLayerGroups;

    this.swapLayerOrder = swapLayerOrder;
    this.getLayerGroups = getLayerGroups;
    this.getLayerGroup = getLayerGroup;
    this.getLayerGroupByURL = getLayerGroupByURL;
    this.isChanged = isChanged;
    this.setChanged = setChanged;


    //implementation
    function getLayerGroups() {
        return this.layerGroups;
    }

    function getLayerGroup(index) {
        return this.layerGroups[index];
    }
    
    function getLayerGroupByURL(url) {
    	for (var j = 0; j < this.layerGroups.length; j++){
    		if ( this.layerGroups[j].getServiceURL() == url ) {
    			return this.layerGroups[j]; 
    		}
    	}
        return null;
    }

    function addLayerGroup(layerGroup, append) {
    	if ( append == null || append == true) {
    		this.layerGroups.push(layerGroup);
    	} else {
    		this.layerGroups.unshift(layerGroup);
    	}
        this.changed = true;
    }

    function insertLayerGroupAt(layerGroup, index) {
        this.layerGroups.push(layerGroup);
        for (var j = this.layerGroups.length-1; j > index; j--){
           this.layerGroups[j] = this.layerGroups[j-1];
        }
        this.layerGroups[index] = layerGroup;
        this.changed = true;
    }

    function removeLayerGroupByIndex(layerGroupIndex) {
        for (var j = layerGroupIndex; j < this.layerGroups.length-1; j++){
        	this.layerGroups[j] = this.layerGroups[j+1];
        }
        this.layerGroups.pop();
        this.changed = true;
    }

    function removeLayerGroupById(id) {
        for (var i = 0; i < this.layerGroups.length; i++){
            if ( this.layerGroups[i].id == id ) {
                this.removeLayerGroupByIndex(i);
                break;
            }
        }
        this.changed = true;
    }
    
    function setLayerGroups(layerGroups) {
    	this.layerGroups = layerGroups; 
    	this.changed = true;
    }

    function swapLayerGroupOrder(layerGroupIndex1, layerGroupIndex2) {
        if ( layerGroupIndex1 >= 0 && layerGroupIndex1 < this.layerGroups.length &&
             layerGroupIndex2 >= 0 && layerGroupIndex2 < this.layerGroups.length ) {
            var tmp  = this.layerGroups[layerGroupIndex1];
            this.layerGroups[layerGroupIndex1] = this.layerGroups[layerGroupIndex2];
            this.layerGroups[layerGroupIndex2] = tmp;
            this.changed = true;
        } else if ( layerGroupIndex1 < 0 || layerGroupIndex2 < 0) {
            alert( "you can't move layer group (WMS) up" );
        } else if ( layerGroupIndex1 >= this.layerGroups.length ||
                    layerGroupIndex2 >= this.layerGroups.length) {
            alert( "you can't move layer group (WMS) down" );
        }
    }

    function swapLayerOrder(layerIndex1, layerIndex2) {
        //TODO
    }

    function isChanged() {
        var c = false;
        for (var i = 0; i < this.layerGroups.length; i++) {
            c = this.layerGroups[i].isChanged();
            if ( c ) {
                break;
            }
        }
        return this.changed || c;
    }

    function setChanged(changed) {
        this.changed = changed;
        for (var i = 0; i < this.layerGroups.length; i++) {
            this.layerGroups[i].setChanged(changed);
        }
    }
}
