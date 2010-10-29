//$HeadURL$
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
package org.deegree.commons.xml.jaxb;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Field;

import org.slf4j.Logger;

import com.sun.xml.bind.v2.ClassFactory;
import com.sun.xml.bind.v2.runtime.Coordinator;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JAXBUtils {

    private static final Logger LOG = getLogger( JAXBUtils.class );

    /**
     * Call this once you're done in the thread that uses jaxb (un-)marshalling.
     */
    public static void fixThreadLocalLeaks() {
        try {
            Field f = ClassFactory.class.getDeclaredField( "tls" );
            f.setAccessible( true );
            ( (ThreadLocal<?>) f.get( null ) ).set( null );
            f = Coordinator.class.getDeclaredField( "activeTable" );
            f.setAccessible( true );
            ( (ThreadLocal<?>) f.get( null ) ).set( null );
        } catch ( java.lang.SecurityException e ) {
            LOG.error( "Failed to plug thread local leaks of jaxb." );
            LOG.trace( "Stack trace:", e );
        } catch ( NoSuchFieldException e ) {
            LOG.error( "Failed to plug thread local leaks of jaxb." );
            LOG.trace( "Stack trace:", e );
        } catch ( IllegalArgumentException e ) {
            LOG.error( "Failed to plug thread local leaks of jaxb." );
            LOG.trace( "Stack trace:", e );
        } catch ( IllegalAccessException e ) {
            LOG.error( "Failed to plug thread local leaks of jaxb." );
            LOG.trace( "Stack trace:", e );
        }
    }

}
