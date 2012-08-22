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
package org.deegree.io.datastore.shape;

import java.net.URL;

import org.deegree.io.datastore.DatastoreConfiguration;

/**
 * Represents the configuration for a {@link ShapeDatastore} instance.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ShapeDatastoreConfiguration implements DatastoreConfiguration {

    private URL file;

    /**
     * Creates a new instance of <code>ShapeDatastoreConfiguration</code> from the given parameters.
     *
     * @param file
     */
    public ShapeDatastoreConfiguration( URL file ) {
        this.file = file;
    }

    public Class<ShapeDatastore> getDatastoreClass() {
        return ShapeDatastore.class;
    }

    /**
     * Returns the shape file that this datastore operates upon.
     *
     * @return the shape file that this datastore operates upon
     */
    public URL getFile() {
        return this.file;
    }

    @Override
    public int hashCode() {
        StringBuffer sb = new StringBuffer();
        sb.append( ShapeDatastoreConfiguration.class.getName() );
        sb.append( this.file );
        return sb.toString().hashCode();
    }

    @Override
    public boolean equals( Object obj ) {
        if ( !( obj instanceof ShapeDatastoreConfiguration ) ) {
            return false;
        }
        ShapeDatastoreConfiguration that = (ShapeDatastoreConfiguration) obj;
        if ( !this.file.equals( that.file ) ) {
            return false;
        }
        return true;
    }
}
