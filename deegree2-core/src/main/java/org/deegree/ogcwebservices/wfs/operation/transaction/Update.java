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
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.w3c.dom.Node;

/**
 * Represents an <code>Update</code> operation as a part of a {@link Transaction} request.
 * <p>
 * WFS Specification OBC 04-094 (#12.2.5 Pg.68)
 * <p>
 * The <code>Update</code> element describes one update operation that is to be applied to a <code>Feature</code> or a
 * set of <code>Feature</code>s of a single <code>FeatureType</code>.
 * <p>
 * Multiple <code>Update</code> operations can be contained in a single <code>Transaction</code> request. An
 * <code>Update</code> element contains one or more <b>Property</b> elements that specify the name and replacement value
 * for a property that belongs to the <code>FeatureType</code> specified using the <b>mandatory typeName</b> attribute.
 * <p>
 * Additionally, a deegree specific addition to this specification is supported:<br>
 * Instead of a number of properties, it is also possible to specify a root feature that will replace the feature that
 * is matched by the filter. In this case, the filter must match exactly one (or zero) feature instances.
 * 
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Update extends TransactionOperation {

    private QualifiedName typeName;

    private Feature replacementFeature;

    private Map<PropertyPath, FeatureProperty> replacementProps;

    private Filter filter;

    private Map<PropertyPath, Node> rawProps;

    /**
     * Creates a new <code>Update</code> instance.
     * 
     * @param handle
     *            optional identifier for the operation (for error messsages)
     * @param typeName
     *            the name of the targeted feature type
     * @param replacementProps
     *            property paths and their replacement values
     * @param filter
     *            selects the feature instances to be updated
     */
    public Update( String handle, QualifiedName typeName, Map<PropertyPath, FeatureProperty> replacementProps,
                   Filter filter ) {
        super( handle );
        this.typeName = typeName;
        this.replacementProps = replacementProps;
        this.filter = filter;
    }

    /**
     * Creates a new <code>Update</code> instance.
     * <p>
     * NOTE: This constructor will be removed in the future, because it makes the DOM representation available and
     * breaks the layering (DOM should not be used on this level).
     * 
     * @param handle
     *            optional identifier for the operation (for error messsages)
     * @param typeName
     *            the name of the targeted feature type
     * @param replacementProps
     *            property paths and their replacement values
     * @param rawProps
     *            property paths and their DOM nodes
     * @param filter
     *            selects the feature instances to be updated
     * @deprecated This method breaks the layering -- it makes the DOM representation available.
     */
    @Deprecated
    Update( String handle, QualifiedName typeName, Map<PropertyPath, FeatureProperty> replacementProps,
            Map<PropertyPath, Node> rawProps, Filter filter ) {
        super( handle );
        this.typeName = typeName;
        this.replacementProps = replacementProps;
        this.rawProps = rawProps;
        this.filter = filter;
    }

    /**
     * Creates a new <code>Update</code> instance.
     * 
     * @param handle
     *            optional identifier for the operation (for error messsages)
     * @param typeName
     *            the name of the targeted feature type
     * @param replacementFeature
     *            property names and their replacement values
     * @param filter
     *            selects the (single) feature instance to be replaced
     */
    public Update( String handle, QualifiedName typeName, Feature replacementFeature, Filter filter ) {
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
     * TypeName setter method
     * 
     * @param typeName
     *            a {@QualifiedName}
     */
    public void setTypeName( QualifiedName typeName ) {
        this.typeName = typeName;
    }

    /**
     * Returns the filter that selects the feature instances to be updated.
     * 
     * @return the filter that selects the feature instances to be updated
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
     * Returns the feature that will replace the matched feature instance. If the returned value is null, this is a
     * "standard" update operation that updates a number of flat properties instead.
     * 
     * @return the feature that will replace the (single) matched feature instance
     */
    public Feature getFeature() {
        return this.replacementFeature;
    }

    /**
     * Return the properties and their replacement values that are targeted by this update operation.
     * 
     * @return the properties and their replacement values
     */
    public Map<PropertyPath, FeatureProperty> getReplacementProperties() {
        return this.replacementProps;
    }

    /**
     * Setter method for replacement properties
     * 
     * @param replacementProps
     *            a map between {@link PropertyPath}s and {@link FeatureProperty}s
     */
    public void setReplacementProperties( Map<PropertyPath, FeatureProperty> replacementProps ) {
        this.replacementProps = replacementProps;
    }

   

    @Override
    public List<QualifiedName> getAffectedFeatureTypes() {
        List<QualifiedName> featureTypes = new ArrayList<QualifiedName>( 1 );
        featureTypes.add( this.typeName );
        return featureTypes;
    }
}
