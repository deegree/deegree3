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
package org.deegree.ogcwebservices.wms.configuration;

import java.net.URL;
import java.util.List;

import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities_1_3_0;
import org.deegree.owscommon_new.OperationsMetadata;
import org.deegree.owscommon_new.ServiceIdentification;
import org.deegree.owscommon_new.ServiceProvider;

/**
 * <code>WMSConfiguration_1_3_0</code> is an implementation of the
 * <code>WMSConfigurationType</code> interface, encapsulating the data
 * required to configure a WMS 1.3.0.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class WMSConfiguration_1_3_0 extends WMSCapabilities_1_3_0 implements WMSConfigurationType {

    private static final long serialVersionUID = -1145058631134066271L;

    private URL baseURL;

    private WMSDeegreeParams deegreeParams;

    /**
     * Generates a new <code>WFSConfiguration</code> instance from the given parameters.
     *
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param metadata
     * @param layer
     * @param deegreeParams
     * @param baseURL
     * @param layerLimit
     * @param exceptions
     */
    public WMSConfiguration_1_3_0( String version, String updateSequence,
                            ServiceIdentification serviceIdentification,
                            ServiceProvider serviceProvider,
                            OperationsMetadata metadata, Layer layer,
                            WMSDeegreeParams deegreeParams, URL baseURL,
                            int layerLimit, List<String> exceptions ) {
        super( version, updateSequence, serviceIdentification, serviceProvider,
               metadata, layer, layerLimit, deegreeParams.getMaxMapWidth(),
               deegreeParams.getMaxMapHeight(), exceptions );
        this.deegreeParams = deegreeParams;
        this.baseURL = baseURL;
    }

    /**
     * @return Returns the deegreeParams.
     */
    public WMSDeegreeParams getDeegreeParams() {
        return deegreeParams;
    }

    /**
     * @param deegreeParams
     *            The deegreeParams to set.
     */
    public void setDeegreeParams( WMSDeegreeParams deegreeParams ) {
        this.deegreeParams = deegreeParams;
    }

    /**
     * @return Gets the base URL which is used to resolve file resource (XSL sheets).
     */
    public URL getBaseURL() {
        return this.baseURL;
    }

    /* (non-Javadoc)
     * @see org.deegree.ogcwebservices.wms.configuration.WMSConfigurationType#calculateVersion(java.lang.String)
     */
    public String calculateVersion( String version ) {
        if( version == null ) return "1.3.0";

        if( "1.3.0".compareTo( version ) <= 0 ) return "1.3.0";

        return "1.1.1";
    }

}

