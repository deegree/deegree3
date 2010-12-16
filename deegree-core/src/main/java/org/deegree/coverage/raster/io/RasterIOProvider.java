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
package org.deegree.coverage.raster.io;

import java.util.ServiceLoader;
import java.util.Set;

/**
 * This interface is for all classes that provide raster IO. It uses the new Java 6 ServiceLoader.
 * 
 * <p>
 * To add your own raster IO to deegree you have to implement this interface and put the class name of _your_
 * implementing class in META-INF/services/org.deegree.model.coverage.raster within _your_ .jar or classes directory.
 * Then you have to add your implementation to the classpath of deegree.
 * 
 * @see ServiceLoader
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public interface RasterIOProvider {
    /**
     * @param type
     * @return a raster reader of the requested type, or null
     */
    public RasterReader getRasterReader( String type );

    /**
     * @param type
     * @return a raster writer of the requested type, or null
     */
    public RasterWriter getRasterWriter( String type );

    /**
     * @return a {@link Set} of (image) formats the raster writer can read.
     */
    public Set<String> getRasterReaderFormats();

    /**
     * @return a {@link Set} of (image) formats the raster writer support.
     */
    public Set<String> getRasterWriterFormats();

}
