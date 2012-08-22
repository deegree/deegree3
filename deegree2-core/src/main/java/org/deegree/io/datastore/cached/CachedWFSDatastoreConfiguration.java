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
package org.deegree.io.datastore.cached;

import java.net.URL;

import org.deegree.datatypes.QualifiedName;
import org.deegree.io.datastore.DatastoreConfiguration;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CachedWFSDatastoreConfiguration implements DatastoreConfiguration {

    private QualifiedName featureType;
    private URL schemLocation;

    /**
     *
     * @param featureType
     * @param url schema location
     */
    public CachedWFSDatastoreConfiguration( QualifiedName featureType, URL url ) {
        this.featureType = featureType;
        this.schemLocation = url;
    }

    /**
     * @return datastore class
     */
    public Class<CachedWFSDatastore> getDatastoreClass() {
        return CachedWFSDatastore.class;
    }

    /**
     *
     * @return name of the cached feature type
     */
    public QualifiedName getFeatureType() {
        return featureType;
    }

    /**
     *
     * @return schema location
     */
    public URL getSchemaLocation() {
        return schemLocation;
    }
}
