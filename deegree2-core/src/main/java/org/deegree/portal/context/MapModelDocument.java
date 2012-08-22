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
package org.deegree.portal.context;

import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class MapModelDocument extends XMLFragment {

    private static final long serialVersionUID = -248476317451177157L;

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * 
     * @param root
     */
    MapModelDocument( Element root ) {
        super( root );
    }

    /**
     * 
     * @return MapModel including hierarchyly arranged layers and layer groups
     * @throws XMLParsingException
     */
    MapModel parseMapModel()
                            throws XMLParsingException {
        MapModel mapModel = new MapModel();
        List<LayerGroup> layerGroups = new ArrayList<LayerGroup>();
        List<Element> elements = XMLTools.getElements( getRootElement(), "./dgcntxt:LayerGroup", nsContext );
        for ( Element element : elements ) {
            String identifier = XMLTools.getRequiredNodeAsString( element, "./@identifier", nsContext );
            String title = XMLTools.getRequiredNodeAsString( element, "./@title", nsContext );
            String tmp = XMLTools.getNodeAsString( element, "./@hidden", nsContext, "false" );
            boolean hidden = "true".equals( tmp ) || "1".equals( tmp );
            tmp = XMLTools.getNodeAsString( element, "./@expanded", nsContext, "false" );
            boolean expanded = "true".equals( tmp ) || "1".equals( tmp );
            LayerGroup layerGroup = new LayerGroup( identifier, title, hidden, expanded, null, mapModel );
            layerGroups.add( layerGroup );
            appendMapModelEntries( element, layerGroup, mapModel );
        }
        mapModel.setLayerGroups( layerGroups );

        return mapModel;
    }

    /**
     * @param lgElement
     * @param layerGroup
     * @param mapModel
     * @throws XMLParsingException
     */
    private void appendMapModelEntries( Element lgElement, LayerGroup layerGroup, MapModel mapModel )
                            throws XMLParsingException {
        List<Element> elements = XMLTools.getElements( lgElement, "./dgcntxt:LayerGroup | ./dgcntxt:Layer", nsContext );
        for ( Element element : elements ) {
            if ( element.getLocalName().equals( "Layer" ) ) {
                String layerId = XMLTools.getRequiredNodeAsString( element, "./@layerId", nsContext );
                layerGroup.addLayer(  new MMLayer( layerId, layerGroup, mapModel, null ) );
            } else {
                String tmp = XMLTools.getNodeAsString( element, "./@hidden", nsContext, "false" );                
                boolean hidden = "true".equals( tmp ) || "1".equals( tmp );
                tmp = XMLTools.getNodeAsString( element, "./@expanded", nsContext, "false" );
                boolean expanded = "true".equals( tmp ) || "1".equals( tmp );
                String title = XMLTools.getRequiredNodeAsString( element, "./@title", nsContext );
                String identifier = XMLTools.getRequiredNodeAsString( element, "./@identifier", nsContext );
                LayerGroup lg = new LayerGroup( identifier, title, hidden, expanded, layerGroup, mapModel );
                layerGroup.addLayerGroup( lg );
                appendMapModelEntries( element, lg, mapModel );
            }
        }
        
    }
}
