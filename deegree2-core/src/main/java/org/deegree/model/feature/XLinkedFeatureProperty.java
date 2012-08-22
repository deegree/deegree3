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
package org.deegree.model.feature;

import org.deegree.datatypes.QualifiedName;

/**
 * Feature property instance that does not specify it's content inline, but references a feature
 * instance via an XLink.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class XLinkedFeatureProperty implements FeatureProperty {

    private QualifiedName name;

    private String targetFeatureId;

    private Feature targetFeature;

    /**
     * Creates a new instance of <code>XLinkedFeatureProperty</code> from the given parameters.
     * <p>
     * NOTE: After creating, this property has no value. The reference to the target feature has to
     * be resolved first by calling #setValue(java.lang.Object).
     *
     * @see #setValue(java.lang.Object)
     *
     * @param name
     *            feature name
     * @param targetFeatureId
     *            id of the feature that this property contains
     */
    public XLinkedFeatureProperty( QualifiedName name, String targetFeatureId ) {
        this.name = name;
        this.targetFeatureId = targetFeatureId;
    }

    /**
     * Returns the name of the property.
     *
     * @return the name of the property.
     */
    public QualifiedName getName() {
        return this.name;
    }

    /**
     * Returns the value of the property.
     *
     * @return the value of the property.
     */
    public Object getValue() {
        checkResolved();
        return this.targetFeature;
    }

    /**
     * Returns the value of the property.
     *
     * @return the value of the property.
     */
    public Object getValue( Object defaultValue ) {
        checkResolved();
        if ( this.targetFeature == null ) {
            return defaultValue;
        }
        return this.targetFeature;
    }

    /**
     * Sets the target feature instance that this feature property refers to.
     *
     * @param value
     *            feature instance that this feature property refers to
     * @throws RuntimeException
     *             if the reference has already been resolved
     */
    public void setValue( Object value ) {
        if ( this.targetFeature != null ) {
            String msg = Messages.format( "ERROR_REFERENCE_ALREADY_RESOLVED", this.targetFeatureId );
            throw new RuntimeException( msg );
        }
        this.targetFeature = (Feature) value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.model.feature.FeatureProperty#getOwner()
     */
    public Feature getOwner() {
        return null;
    }

    /**
     * Returns the feature id of the target feature.
     *
     * @return the feature id of the target feature.
     */
    public String getTargetFeatureId() {
        return this.targetFeatureId;
    }

    /**
     * Ensures that the reference to the target feature has been resolved.
     *
     * @throws RuntimeException
     *             if the reference has not been resolved
     */
    private void checkResolved() {
        if ( this.targetFeature == null ) {
            String msg = Messages.format( "ERROR_XLINK_NOT_RESOLVED", this.targetFeatureId );
            throw new RuntimeException( msg );
        }
    }
}
