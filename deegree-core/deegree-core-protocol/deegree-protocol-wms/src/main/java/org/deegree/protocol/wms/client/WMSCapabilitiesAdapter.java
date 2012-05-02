//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wms.client;

import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.struct.Tree;
import org.deegree.geometry.Envelope;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.ows.capabilities.OWSCapabilitiesAdapter;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public interface WMSCapabilitiesAdapter extends OWSCapabilitiesAdapter {

    /**
     * @param request
     * @return the image formats defined for the request, or null, if request is not supported
     */
    public abstract LinkedList<String> getFormats( WMSRequestType request );

    /**
     * Use parseOperationsMetadata().getGetUrls( request.name() ) or parseOperationsMetadata().getPostUrls(
     * request.name() ) instead
     * 
     * @param request
     * @param get
     *            true means HTTP GET, false means HTTP POST
     * @return the address, or null, if not defined or request unavailable
     */
    public abstract String getAddress( WMSRequestType request, boolean get );

    /**
     * @param srs
     * @param layer
     * @return the envelope, or null, if none was found
     */
    public abstract Envelope getBoundingBox( String srs, String layer );

    /**
     * @return the names of all layers that have a name
     */
    public abstract List<String> getNamedLayers();

    /**
     * @param name
     * @return true, if the WMS advertises a layer with that name
     */
    public abstract boolean hasLayer( String name );

    /**
     * @param name
     * @return all coordinate system names, also inherited ones
     */
    public abstract LinkedList<String> getCoordinateSystems( String name );

    /**
     * @param layer
     * @return the envelope, or null, if none was found
     */
    public abstract Envelope getLatLonBoundingBox( String layer );

    /**
     * @param layers
     * @return a merged envelope of all the layer's envelopes
     */
    public abstract Envelope getLatLonBoundingBox( List<String> layers );

    public abstract Tree<LayerMetadata> getLayerTree();

    /**
     * @param request
     * @return true, if an according section was found in the capabilities
     */
    public abstract boolean isOperationSupported( WMSRequestType request );

    /**
     * @return the system id of the capabilities document.
     */
    public abstract String getSystemId();

}