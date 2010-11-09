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

package org.deegree.cs.configuration.resources;

import java.io.IOException;

/**
 * The <code>XLinkResolver</code> interface defines methods for the resolving of an xlink:href uri. Use-cases could be
 * the resolving of an uri in a database, a localfile, a server or anything to which an URI could point.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 * @param <T>
 *            the type of the implementation
 *
 */
public interface XLinkResolver<T> {

    /**
     * Opens a stream to a resource which is defined by the uri.
     *
     * @param uri
     *            to locate
     * @return an opened InputStream to the given resource or <code>null</code> if no resource was found.
     * @throws IOException
     *             If an error occurred while locating or opening the resource.
     */
    public T getURIAsType( String uri )
                            throws IOException;

}
