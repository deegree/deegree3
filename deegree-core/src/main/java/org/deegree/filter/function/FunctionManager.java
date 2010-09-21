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
package org.deegree.filter.function;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for retrieving {@link FunctionProvider} instances that are registered via Java SPI.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FunctionManager {

    private static final Logger LOG = LoggerFactory.getLogger( FunctionManager.class );

    private static ServiceLoader<FunctionProvider> functionLoader = ServiceLoader.load( FunctionProvider.class );

    private static Map<String, FunctionProvider> nameToFunction;

    /**
     * Returns all available {@link FunctionProvider}s. Multiple functions with the same case ignored name are not
     * allowed.
     * 
     * @return all available functions, keys: name, value: FunctionProvider
     */
    public static synchronized Map<String, FunctionProvider> getFunctionProviders() {
        if ( nameToFunction == null ) {
            nameToFunction = new HashMap<String, FunctionProvider>();
            try {
                for ( FunctionProvider function : functionLoader ) {
                    LOG.debug( "Function: " + function + ", name: " + function.getName() );
                    String name = function.getName().toLowerCase();
                    if ( nameToFunction.containsKey( name ) ) {
                        LOG.error( "Multiple CustomFunction instances for name: '" + name
                                   + "' on classpath -- omitting '" + function.getClass().getName() + "'." );
                        continue;
                    }
                    nameToFunction.put( name, function );
                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            }
        }
        return nameToFunction;
    }

    /**
     * Returns the {@link FunctionProvider} for the given name.
     * 
     * @param name
     *            name of the function, must not be <code>null</code>
     * @return custom function instance, or <code>null</code> if there is no function with this name
     */
    public static FunctionProvider getFunctionProvider( String name ) {
        return getFunctionProviders().get( name.toLowerCase() );
    }
}