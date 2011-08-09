//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.remoteows.wms;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public interface WMSClient {

    public void refreshCapabilities();

    public boolean isOperationSupported( WMSRequestType request );

    public LinkedList<String> getFormats( WMSRequestType request );

    public String getAddress( WMSRequestType request, boolean get );

    public boolean hasLayer( String name );

    public LinkedList<String> getCoordinateSystems( String name );

    public Envelope getLatLonBoundingBox( String layer );

    public Envelope getLatLonBoundingBox( List<String> layers );

    public Envelope getBoundingBox( String srs, String layer );

    public List<String> getNamedLayers();

    public Envelope getBoundingBox( String srs, List<String> layers );

    public Pair<BufferedImage, String> getMap( List<String> layers, int width, int height, Envelope bbox, ICRS srs,
                                               String format, boolean transparent, boolean errorsInImage, int timeout,
                                               boolean validate, List<String> validationErrors,
                                               Map<String, String> hardParameters )
                            throws IOException;

    public FeatureCollection getFeatureInfo( List<String> queryLayers, int width, int height, int x, int y,
                                             Envelope bbox, ICRS srs, int count, Map<String, String> hardParameters )
                            throws IOException;

}
