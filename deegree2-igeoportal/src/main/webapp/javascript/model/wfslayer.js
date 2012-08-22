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
 * represent one layer from a WFS
 */
function WFSLayer(wfsName, featureType, filter, sld, visible, selected) {
	
	// name of the WFS the feature type belongs to
	this.wfsName = wfsName;
	this.name = name;
	this.filter =filter;
	this.sld = sld;
	this.visible = visible;
	this.selected = selected;
	
	this.getWMSName = getWMSName;
	this.getName = getName;
	this.getFilter = getFilter;
	this.setFilter = setFilter;
	this.getSLD = getSLD;
	this.setSLD = setSLD;
	this.isVisible = isVisible;
	this.setVisible = setVisible;
	this.isSelected = isSelected;
	this.setSelected = setSelected;
	
	function getName() {
		return name;
	}
	
	function getWFSName() {
		return wfsName;
	}
	
	function getFilter() {
		return filter;	
	}
	
	function setFilter(filter) {
		this.filter = filter;
	}
	
	function getSLD() {
		return sld;
	}
	
	function setSLD(sld) {
		this.sld = sld;
	}
	
	function isVisible() {
		return visible;
	}
	
	function setVisible(visible) {
		this.visible = visible;
	}
	
	function isSelected() {
		return selected;
	}
	
	function setSelected(selected) {
		return selected;
	}
	
}
