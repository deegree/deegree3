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
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.Feature;
import org.deegree.model.filterencoding.Filter;

/**
 * Represents a <code>Replace</code> operation as a part of a {@link Transaction} request.
 * <p>
 * This operation is designed after the Replace operation of the upcoming WFS 2.0.0 specification. One or more feature
 * instances (specified by a filter expression) are replaced by a given feature instance. The replacement is assumed to
 * be performed property by property, so the targeted features are identical to the specified feature afterwards, but
 * keep their original feature ids.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Replace extends TransactionOperation {

    private QualifiedName typeName;

    private Feature replacementFeature;

    private Filter filter;

    /**
     * Creates a new <code>Replace</code> instance.
     * 
     * @param handle
     *            optional identifier for the operation (for error messsages)
     * @param typeName
     *            the name of the targeted feature type
     * @param replacementFeature
     *            feature that specifies all properties
     * @param filter
     *            selects the feature instances to be replaced
     */
    public Replace( String handle, QualifiedName typeName, Feature replacementFeature, Filter filter ) {
        super( handle );
        this.typeName = typeName;
        this.replacementFeature = replacementFeature;
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
     * Returns the filter that selects the feature instances to be replaced.
     * 
     * @return the filter that selects the feature instances to be replaced
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * Sets the filter that determines the features that are affected by the operation.
     * 
     * @param filter
     *            determines the features that are affected by the operation
     */
    public void setFilter( Filter filter ) {
        this.filter = filter;
    }

    /**
     * Returns the feature that will be used to replace the properties of the matched feature instances.
     * 
     * @return the feature that will be used to replace the properties of the matched feature instances
     */
    public Feature getFeature() {
        return this.replacementFeature;
    }

    @Override
    public List<QualifiedName> getAffectedFeatureTypes() {
        List<QualifiedName> featureTypes = new ArrayList<QualifiedName>( 1 );
        featureTypes.add( this.typeName );
        return featureTypes;
    }
}
