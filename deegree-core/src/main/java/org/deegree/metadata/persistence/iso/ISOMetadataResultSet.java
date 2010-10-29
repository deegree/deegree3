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
package org.deegree.metadata.persistence.iso;

import org.deegree.metadata.MetadataResultType;
import org.deegree.metadata.persistence.MetadataCollection;
import org.deegree.metadata.persistence.MetadataResultSet;

/**
 * Implementation of {@link MetadataResultSet} for the ISO Application Profile.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISOMetadataResultSet implements MetadataResultSet {

    private MetadataResultSet rs;

    private MetadataCollection collection;

    private MetadataResultType type;

    public ISOMetadataResultSet( MetadataCollection collection, MetadataResultType type ) {
        this.collection = collection;
        this.type = type;
    }

    @Override
    public void close() {
        rs.close();

    }

    @Override
    public String encoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MetadataCollection getMembers() {

        return this.collection;
    }

    @Override
    public MetadataResultType getResultType() {

        return this.type;
    }

}
