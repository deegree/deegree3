//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.ogcwebservices.wpvs.configuration;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.i18n.Messages;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.WMServiceFactory;
import org.deegree.ogcwebservices.wms.configuration.WMSConfigurationDocument;
import org.deegree.ogcwebservices.wms.configuration.WMSConfigurationType;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wpvs.capabilities.OWSCapabilities;
import org.xml.sax.SAXException;

/**
 * This class represents a local WMS dataSource object.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class LocalWMSDataSource extends AbstractDataSource {

    private Color[] transparentColors;

    private static Map<URL, WMSConfigurationType> cache = new ConcurrentHashMap<URL, WMSConfigurationType>();

    /**
     * Creates a new <code>LocalWMSDataSource</code> object from the given parameters.
     *
     * @param name
     * @param owsCapabilities
     * @param validArea
     * @param minScaleDenominator
     * @param maxScaleDenominator
     * @param filterCondition
     * @param transparentColors
     */
    public LocalWMSDataSource( QualifiedName name, OWSCapabilities owsCapabilities,
                               Surface validArea, double minScaleDenominator,
                               double maxScaleDenominator, GetMap filterCondition,
                               Color[] transparentColors ) {

        super( AbstractDataSource.LOCAL_WMS, name, owsCapabilities, validArea, minScaleDenominator,
               maxScaleDenominator, filterCondition );
        this.transparentColors = transparentColors;
    }

    /**
     * The <code>filterCondition</code> is a map of key-value-pairs of an incomplete WMSRequest.
     *
     * @return Returns the filterCondition as a map of key-value-pairs.
     */
    public GetMap getPartialGetMapRequest() {
        return (GetMap) getFilterCondition();
    }

    /**
     * @return Returns the transparentColors.
     */
    public Color[] getTransparentColors() {
        return transparentColors;
    }

    /**
     * @throws OGCWebServiceException
     * @see org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource#getOGCWebService()
     */
    @Override
    public OGCWebService getOGCWebService()
                            throws OGCWebServiceException {
        WMSConfigurationType wmsConfig = null;
        synchronized ( this ) {
            URL url = getOwsCapabilities().getOnlineResource();
            wmsConfig = cache.get( url );
            if ( !cache.containsKey( url ) || wmsConfig == null ) {
                WMSConfigurationDocument wmsDoc = new WMSConfigurationDocument();
                try {
                    wmsDoc.load( getOwsCapabilities().getOnlineResource() );
                    wmsConfig = wmsDoc.parseConfiguration();
                    cache.put( url, wmsConfig );
                } catch ( IOException e ) {
                    throw new OGCWebServiceException(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                } catch ( SAXException e ) {
                    throw new OGCWebServiceException(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                } catch ( InvalidConfigurationException e ) {
                    throw new OGCWebServiceException(
                                                      Messages.getMessage(
                                                                           "WPVS_DATASOURCE_CAP_ERROR",
                                                                           toString() )
                                                                              + e.getMessage() );
                }
            }
        }
        return WMServiceFactory.getWMSInstance( wmsConfig );
    }

}
