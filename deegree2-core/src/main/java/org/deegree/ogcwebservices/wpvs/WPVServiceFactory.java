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

package org.deegree.ogcwebservices.wpvs;

import java.io.IOException;
import java.net.URL;

import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.ogcwebservices.wpvs.configuration.WPVSConfiguration;
import org.deegree.ogcwebservices.wpvs.configuration.WPVSConfigurationDocument;
import org.xml.sax.SAXException;

/**
 * Factory class for creating instances of <code>WFService</code>.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 */
public class WPVServiceFactory {

    private static WPVSConfiguration CONFIG;

    private WPVServiceFactory() {
        // prevent instantiation
    }

    /**
     * Creates an instance of a WPVService
     *
     * @return a wpvService class with the WPVSConfiguration if the WPVSConfiguration ==
     *         <code>null</code> null will be returned.
     */
    public static WPVService createInstance() {
        if ( CONFIG != null )
            return new WPVService( CONFIG );
        return null;
    }

    /**
     * Creates a new instance of a WPVService based on <code>config</code>
     *
     * @param config
     *            the configuration object used to initalize the new instance
     * @return a new WPVService Instance, instantiated with the given WPVSConfiguration
     */
    public static WPVService getInstance( WPVSConfiguration config ) {
        return new WPVService( config );
    }

    /**
     * Sets the <code>WPVSConfiguration</code>. Afterwards, all <code>WPVSService</code>
     * instances returned by <code>createInstance()</code> will use this configuration.
     *
     * @param wpvsConfiguration
     */
    public synchronized static void setConfiguration( WPVSConfiguration wpvsConfiguration ) {
        CONFIG = wpvsConfiguration;
    }

    /**
     * Sets the service configuration used in this object to be that pointed at by the
     * serviceConfigurationURL
     *
     * @param serviceConfigurationURL
     *            the URL pointing at the configuration file for a WPV Service
     * @throws InvalidConfigurationException
     *             if the configuration is invalid
     * @throws IOException
     */
    public synchronized static void setConfiguration( URL serviceConfigurationURL )
                            throws InvalidConfigurationException, IOException {
        try {
            WPVSConfigurationDocument wpvsConfigDoc = new WPVSConfigurationDocument();
            wpvsConfigDoc.load( serviceConfigurationURL );
            WPVServiceFactory.setConfiguration( wpvsConfigDoc.parseConfiguration() );
        } catch ( InvalidConfigurationException e ) {
            throw new InvalidConfigurationException( "WPVServiceFactory", e.getMessage() );
        } catch ( SAXException e ) {
            throw new InvalidConfigurationException( "WPVServiceFactory", e.getMessage() );
        }
    }
}
