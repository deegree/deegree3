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

function LayerTree(id) {
	this.id = id;
	this.tree;
	
	this.paint = paint;
	this.repaint = repaint;
	this.setTree = setTree;
	this.checkForVisibility = checkForVisibility;
	
	function paint(targetDocument, parentNode) {
		this.repaint();
	}
	
	function repaint() {
		if ( this.tree != null ) {
			try {
				var root = this.tree.getRootNode();
				// traverse tree
				this.checkForVisibility( root );
			} catch(e) {}
		}
	}
		
	function checkForVisibility( node ) {
		var scale = controller.mapModel.getScale();
		for ( var i = 0; i < node.childNodes.length; i++ ) {
			if ( node.childNodes[i].isLeaf() ) {
				var layer = controller.mapModel.getLayerById( node.childNodes[i].id );
				if ( layer != null ) {
					if ( scale < layer.getMinScale() || scale > layer.getMaxScale() ) {
						node.childNodes[i].disable();
					} else {
						node.childNodes[i].enable();
					}
				}
			} else {
				this.checkForVisibility( node.childNodes[i] ); 
			}
		}
	}
	
	function setTree( tree ) {
		this.tree = tree;
	}
}



