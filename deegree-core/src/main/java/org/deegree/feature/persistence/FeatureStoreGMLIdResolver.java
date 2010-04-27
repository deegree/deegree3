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
package org.deegree.feature.persistence;

import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLReferenceResolver;
import org.deegree.gml.ReferenceResolvingException;

/**
 * {@link GMLReferenceResolver} that uses a {@link FeatureStore} for resolving local object references.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureStoreGMLIdResolver implements GMLReferenceResolver {

    private final FeatureStore fs;

    /**
     * Creates a new {@link FeatureStoreGMLIdResolver} instance.
     * 
     * @param fs
     *            feature store to be used for retrieving local features, must not be <code>null</code>
     */
    public FeatureStoreGMLIdResolver( FeatureStore fs ) {
        this.fs = fs;
    }

    @Override
    public GMLObject getObject( String uri, String baseURL ) {
        if ( uri.startsWith( "#" ) ) {
            try {
                return fs.getObjectById( uri.substring( 1 ) );
            } catch ( FeatureStoreException e ) {
                throw new ReferenceResolvingException( e.getMessage(), e );
            }
        }
        throw new ReferenceResolvingException( "Resolving of remote references is not implemented yet." );
    }
}
