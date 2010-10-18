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
package org.deegree.services.wfs.format;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for retrieving {@link OutputFormatProvider} instances that are registered via Java SPI.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OutputFormatManager {

    private static final Logger LOG = LoggerFactory.getLogger( OutputFormatManager.class );

    private static ServiceLoader<OutputFormatProvider> formatLoader = ServiceLoader.load( OutputFormatProvider.class );

    private static Map<String, OutputFormatProvider> wknToProvider;

    /**
     * Returns all available {@link OutputFormatProvider}s.
     * 
     * @return all available format providers, keys: WKN/class names, value: WFSFormatProvider
     */
    public static synchronized Map<String, OutputFormatProvider> getFormatProviders() {
        if ( wknToProvider == null ) {
            wknToProvider = new HashMap<String, OutputFormatProvider>();
            try {
                for ( OutputFormatProvider format : formatLoader ) {
                    LOG.debug( "Format: " + format + ", element name: " + format.getWKN() );
                    if ( wknToProvider.containsKey( format.getWKN() ) ) {
                        LOG.error( "Multiple FormatProvider instances for WKN: '" + format.getWKN()
                                   + "' on classpath -- omitting '" + format.getClass().getName() + "'." );
                    } else {
                        wknToProvider.put( format.getWKN(), format );
                    }
                    wknToProvider.put( format.getClass().getName(), format );
                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            }
        }
        return wknToProvider;
    }

    /**
     * Returns the {@link OutputFormatProvider} for the given WKN / class name.
     * 
     * @param name
     *            name of the format, must not be <code>null</code>
     * @return format provider, or <code>null</code> if there is no format provider with this WKN / class name
     */
    public static OutputFormatProvider getFormatProvider( String name ) {
        return getFormatProviders().get( name );
    }
}