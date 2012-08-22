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
package org.deegree.ogcwebservices.wfs.operation.transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.Feature;
import org.deegree.model.filterencoding.Filter;

/**
 * Represents a <code>Delete</code> operation as a part of a {@link Transaction} request.
 * <p>
 * WFS Specification OGC 04-094 (#12.2.6 Pg.71):
 * <p>
 * The <code>Delete</code> element is used to indicate that one of more feature instances should be deleted. The scope
 * of the delete operation is constrained by using the <code>Filter</code> element as described in the <b>Filter
 * Encoding Specification[3]</b>.<br>
 * In the event, that the {@link Filter} element does not identify any {@link Feature} instances to <code>Delete</code>,
 * the <code>Delete</code> action will simply have no effect. <br>
 * <b>This is not an exception condition</b>.
 * 
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Delete extends TransactionOperation {

    private QualifiedName typeName;

    private Filter filter;

    /**
     * Creates a new <code>Delete<code> instance.
     * 
     * @param handle
     *            optional identifier for the operation (for error messsages)
     * @param typeName
     *            name of the targeted feature type
     * @param filter
     *            selects the feature instances to be deleted
     */
    public Delete( String handle, QualifiedName typeName, Filter filter ) {
        super( handle );
        this.typeName = typeName;
        this.filter = filter;
    }

    /**
     * Returns the name of the targeted feature type.
     * 
     * @return the name of the targeted feature type.
     */
    public QualifiedName getTypeName() {
        return this.typeName;
    }

    /**
     * Setter method for the type name
     * 
     * @param typeName
     *            a {@link QualifiedName}
     */
    public void setTypeName( QualifiedName typeName ) {
        this.typeName = typeName;
    }

    /**
     * Return <code>Filter</code>.
     * 
     * @return Filter filter
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * sets the filter condition for an delete operation. This method may be use by classes/moduls that need to
     * manipulate an update operation e.g. to ensure security constraints.
     * 
     * @param filter
     */
    public void setFilter( Filter filter ) {
        this.filter = filter;
    }

    /**
     * Returns the names of the feature types that are affected by the operation.
     * <p>
     * For the <code>Delete</code> operation, this is a list with a single entry - the value of the "typeName"
     * attribute.
     * 
     * @return the names of the affected feature types.
     */
    @Override
    public List<QualifiedName> getAffectedFeatureTypes() {
        List<QualifiedName> featureTypes = new ArrayList<QualifiedName>( 1 );
        featureTypes.add( this.typeName );
        return featureTypes;
    }

    /**
     * Creates <code>Delete</code> instances from a KVP request.
     * 
     * @param typeFilter
     * @return Delete instances
     */
    protected static List<Delete> create( Map<QualifiedName, Filter> typeFilter ) {

        List<Delete> deletes = new ArrayList<Delete>();
        if ( typeFilter != null ) {
            Iterator<QualifiedName> iter = typeFilter.keySet().iterator();
            while ( iter.hasNext() ) {
                QualifiedName typeName = iter.next();
                Filter filter = typeFilter.get( typeName );
                deletes.add( new Delete( null, typeName, filter ) );
            }
        }
        return deletes;
    }
}
