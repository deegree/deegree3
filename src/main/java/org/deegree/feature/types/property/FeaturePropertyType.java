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

package org.deegree.feature.types.property;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.types.FeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PropertyType} that defines a property with a {@link Feature} value.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class FeaturePropertyType extends AbstractPropertyType {

    private static final Logger LOG = LoggerFactory.getLogger( FeaturePropertyType.class );

    private QName valueFtName;

    private FeatureType valueFt;

    private final ValueRepresentation representation;

    public FeaturePropertyType( QName name, int minOccurs, int maxOccurs, QName valueFtName, boolean isAbstract,
                                List<PropertyType> substitutions, ValueRepresentation representation ) {
        super( name, minOccurs, maxOccurs, isAbstract, substitutions );
        this.valueFtName = valueFtName;
        this.representation = representation;
    }

    /**
     * Returns the name of the contained feature type.
     * 
     * @return the name of the contained feature type, or null if unrestricted (any feature type is allowed)
     */
    public QName getFTName() {
        return valueFtName;
    }

    /**
     * Returns the contained feature type.
     * 
     * @return the contained feature type, or null if unrestricted (any feature type is allowed)
     */
    public FeatureType getValueFt() {
        // if ( valueFt == null ) {
        // String msg = "Internal error. Reference to feature type '" + valueFtName + "' has not been resolved.";
        // throw new RuntimeException (msg);
        // }
        return valueFt;
    }

    public void resolve( FeatureType valueFt ) {
        if ( valueFt == null ) {
            LOG.warn( "Setting reference to feature type '" + valueFtName
                      + "' to null -- repairing definition by clearing value feature type name as well." );
            valueFtName = null;
        }
        // TODO (reenable?)
//        if ( this.valueFt != null ) {
//            String msg = "Internal error. Reference to feature type '" + valueFtName + "' has already been resolved.";
//            throw new IllegalArgumentException( msg );
//        }
        this.valueFt = valueFt;
    }

    /**
     * Returns the allowed representation form of the value object.
     * 
     * @return the allowed representation form, never <code>null</code>
     */
    public ValueRepresentation getAllowedRepresentation() {
        return representation;
    }

    @Override
    public String toString() {
        String s = "- feature property type: '" + name + "', minOccurs=" + minOccurs + ", maxOccurs=" + maxOccurs
                   + ", value feature type: " + valueFtName;
        return s;
    }

    @Override
    public boolean isNillable() {
        // TODO pipe this value through
        return true;
    }
}
