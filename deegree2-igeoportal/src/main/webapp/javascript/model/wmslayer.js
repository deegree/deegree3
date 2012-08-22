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
 * represent one layer from a WMS
 */
function WMSLayer( wmsName, name, title, layerAbstract, styleName, SLDRef, visible, selected, queryable, 
	               minScale, maxScale, legendURL, metadataURL, dsResource, dsGeomType, dsFeatureType,
	               identifier, tiled) {

    this.wmsName = wmsName;     // name of the WMS the layer belongs to
    this.name = name;
    this.identifier = identifier || name;
    this.title = title;
    this.layerAbstract = layerAbstract;
    this.styleName = styleName; // styleName shall be null if SLDRef isn't and vice versa
    this.sldRef = null;
    this.visible = visible;     // check box - mark layer to show in map
    this.selected = selected;   // highlighting - mark layer to enable moving up/down
    this.changed = true;
    this.queryable = queryable; // radio button - mark layer to enable FeatureInfo query
    this.minScale = minScale;   // value is taken from wmc: min-scale-hint; else: taken from WMS; else: default = 0) -> pixel-diagonale
    this.maxScale = maxScale;   // value is taken from wmc: max-scale-hint; else: taken from WMS; else: default = infinity)
    this.legendURL = legendURL;
    this.metadataURL = metadataURL;
    this.tiled = tiled;
    // needed for download of wfs data
    this.dsResource = dsResource;       // Extension/deegree:DataService/Server/OnlineResource
    this.dsGeomType = dsGeomType;       // Extension/deegree:DataService/deegree:GeometryType
    this.dsFeatureType = dsFeatureType; // Extension/deegree:DataService/deegree:FeatureType    

    this.getWMSName = getWMSName;
    this.getName = getName;
    this.getIdentifier = getIdentifier;
    this.getTitle = getTitle;
    this.getAbstract = getAbstract;
    this.getStyleName = getStyleName;
    this.setStyleName = setStyleName;
    this.getSLDRef = getSLDRef;
    this.setSLDRef = setSLDRef;
    this.isVisible = isVisible;
    this.setVisible = setVisible;
    this.isSelected = isSelected;
    this.setSelected = setSelected;
    this.isQueryable = isQueryable;
    this.setQueryable = setQueryable;
    this.isChanged = isChanged;
    this.setChanged = setChanged;
    this.getMinScale = getMinScale;
    this.getMaxScale = getMaxScale;
    this.getLegendURL = getLegendURL;
    this.getMetadataURL = getMetadataURL;
    this.getDSResource = getDSResource;
    this.getDSGeomType = getDSGeomType;
    this.getDSFeatureType = getDSFeatureType;
    this.isTiled = isTiled;

    function getName() {
        return this.name;
    }
    
    function getIdentifier() {
        return this.identifier;
    }

    function getTitle() {
        return this.title;
    }

    function getAbstract() {
        return this.layerAbstract;
    }
    
    function isTiled() {
    	return this.tiled != null && tiled;
    }

    function getWMSName() {
        return this.wmsName;
    }

    function getStyleName() {
        return this.styleName;
    }

    function setStyleName(styleName) {
        this.sldRef = null;
        this.styleName = styleName;
        changed = true;
    }

    function getSLDRef() {
        return this.sldRef;
    }

    function setSLDRef(sldRef) {
        this.styleName = null;
        this.sldRef = sldRef;
        changed = true;
    }

    function isVisible() {
        return this.visible;
    }

    function setVisible(visible) {
        if ( this.visible != visible ) {
            changed = true;
        }
        this.visible = visible;
    }

    function isQueryable() {
        return this.queryable;
    }

    function setQueryable(queryable) {
        this.queryable = queryable;
    }

    function isSelected() {
        return this.selected;
    }

    function setSelected(selected) {
        if ( this.selected != selected ) {
            this.changed = true;
        }
        this.selected = selected;
    }

    function isChanged() {
        return this.changed;
    }

    function setChanged(changed) {
        this.changed = changed;
    }

    function getMinScale() {
        return this.minScale;
    }

    function getMaxScale() {
        return this.maxScale;
    }

    function getLegendURL() {
        return this.legendURL;
    }

    function getMetadataURL() {
        return this.metadataURL;
    }
    
    function getDSResource() {
        return this.dsResource;
    }

    function getDSGeomType() {
        return this.dsGeomType;
    }

    function getDSFeatureType() {
        return this.dsFeatureType;
    }
}