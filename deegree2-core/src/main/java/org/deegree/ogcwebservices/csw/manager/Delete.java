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
package org.deegree.ogcwebservices.csw.manager;

import java.net.URI;

import org.deegree.model.filterencoding.Filter;

/**
 * A Delete object constains a constraint that defines a set of records that are to be deleted from
 * the catalogue. A constraint must be specified in order to prevent every record in the catalogue
 * from inadvertently being deleted.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Delete extends Operation {

    private URI typeName = null;

    private Filter constraint = null;

    /**
     *
     * @param handle
     * @param typeName
     * @param constraint
     */
    public Delete( String handle, URI typeName, Filter constraint ) {
        super( "Delete", handle );
        this.typeName = typeName;
        this.constraint = constraint;
    }

    /**
     * The number of records affected by a delete action is determined by the contents of the
     * constraint.
     *
     * @return the filter
     */
    public Filter getConstraint() {
        return constraint;
    }

    /**
     * sets the constraint to be considered with a Delete operation
     *
     * @param constraint
     */
    public void setConstraint( Filter constraint ) {
        this.constraint = constraint;
    }

    /**
     * The typeName attribute is used to specify the collection name from which records will be
     * deleted.
     *
     * @return the uri
     */
    public URI getTypeName() {
        return typeName;
    }
}
