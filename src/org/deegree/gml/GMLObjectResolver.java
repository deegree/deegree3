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
package org.deegree.gml;

import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;

/**
 * Provides the functionality to retrieve {@link GMLObject} instances by a URI (which may be local or remote).
 * <p>
 * A local reference is always constructed as <code># + id</code>.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface GMLObjectResolver {

    /**
     * Returns the {@link GMLObject} that is referenced by the given URI.
     * 
     * @param uri
     *            URI that identifies the object, must not be <code>null</code>
     * @param baseURL
     *            optional baseURL for resolving URIs that are relative URLs
     * @return the referenced object or <code>null</code> if no such object exists
     */
    public GMLObject getObject( String uri, String baseURL );

    /**
     * Returns the {@link Feature} that is referenced by the given URI.
     * 
     * @param uri
     *            URI that identifies the feature, must not be <code>null</code>
     * @param baseURL
     *            optional baseURL for resolving URIs that are relative URLs
     * @return the referenced feature or <code>null</code> if no such feature exists
     */
    public Feature getFeature( String uri, String baseURL );

    /**
     * Returns the {@link Geometry} that is referenced by the given URI.
     * 
     * @param uri
     *            URI that identifies the geometry, must not be <code>null</code>
     * @param baseURL
     *            optional baseURL for resolving URIs that are relative URLs
     * @return the referenced geometry or <code>null</code> if no such geometry exists
     */
    public Geometry getGeometry( String uri, String baseURL );

    // TODO methods for other GML object types (Topology, Coverage, CRS, ...)
}
