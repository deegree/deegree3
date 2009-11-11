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
package org.deegree.feature.gml;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.deegree.feature.GenericProperty;
import org.deegree.feature.Property;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Envelope;

/**
 * Allows to iterate over the GML 3.1.1 properties defined by a {@link StandardGMLFeatureProps} object.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GML311StandardPropertiesIterator implements Iterator<Property<?>> {

    private final StandardGMLFeatureProps props;

    private enum PROPERTY {
        BEFORE, DESCRIPTION, NAME, BOUNDED_BY, AFTER
    }

    private PROPERTY currentPropType = PROPERTY.BEFORE;

    private int currentPropIdx = 0;

    private final static PropertyType DESCRIPTION_TYPE = new SimplePropertyType(
                                                                                 new QName( GMLNS, "description" ),
                                                                                 0,
                                                                                 1,
                                                                                 SimplePropertyType.PrimitiveType.STRING );

    private final static PropertyType NAME_TYPE = new SimplePropertyType( new QName( GMLNS, "name" ), 0, -1,
                                                                          SimplePropertyType.PrimitiveType.STRING );

    private final static PropertyType BOUNDED_BY_TYPE = new EnvelopePropertyType( new QName( GMLNS, "boundedBy" ), 0, 1 );

    public GML311StandardPropertiesIterator( StandardGMLFeatureProps props ) {
        this.props = props;
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = false;
        switch ( currentPropType ) {
        case BEFORE:
            hasNext = props.getDescription() != null || props.getNames().length > 0 || props.getBoundedBy() != null;
            break;
        case DESCRIPTION:
            hasNext = props.getNames().length > 0 || props.getBoundedBy() != null;
            break;
        case NAME:
            hasNext = currentPropIdx < props.getNames().length - 1 || props.getBoundedBy() != null;
            break;
        case BOUNDED_BY:
        case AFTER:
            hasNext = false;
            break;
        }
        return hasNext;
    }

    @Override
    public Property<?> next() {
        if ( !hasNext() ) {
            throw new NoSuchElementException();
        }
        Property<?> prop = null;
        switch ( currentPropType ) {
        case BEFORE:
            if ( props.getDescription() != null ) {
                currentPropType = PROPERTY.DESCRIPTION;
                currentPropIdx = 0;
                prop = new GenericProperty<String>( DESCRIPTION_TYPE, null, props.getDescription().getString() );
            } else if ( props.getNames().length > 0 ) {
                currentPropType = PROPERTY.NAME;
                currentPropIdx = 0;
                // TODO what about the codespace?
                prop = new GenericProperty<String>( NAME_TYPE, null, props.getNames()[0].getCode() );
            } else if ( props.getBoundedBy() != null ) {
                currentPropType = PROPERTY.BOUNDED_BY;
                currentPropIdx = 0;
                prop = new GenericProperty<Envelope>( BOUNDED_BY_TYPE, null, props.getBoundedBy() );
            }
            break;
        case DESCRIPTION:
            if ( props.getNames().length > 0 ) {
                currentPropType = PROPERTY.NAME;
                currentPropIdx = 0;
                // TODO what about the codespace?
                prop = new GenericProperty<String>( NAME_TYPE, null, props.getNames()[0].getCode() );
            } else if ( props.getBoundedBy() != null ) {
                currentPropType = PROPERTY.BOUNDED_BY;
                currentPropIdx = 0;
                prop = new GenericProperty<Envelope>( BOUNDED_BY_TYPE, null, props.getBoundedBy() );
            }
            break;
        case NAME:
            if ( currentPropIdx < props.getNames().length - 1) {
                currentPropIdx++;
                // TODO what about the codespace?
                prop = new GenericProperty<String>( NAME_TYPE, null, props.getNames()[currentPropIdx].getCode() );
            } else if ( props.getBoundedBy() != null ) {
                currentPropType = PROPERTY.BOUNDED_BY;
                currentPropIdx = 0;
                prop = new GenericProperty<Envelope>( BOUNDED_BY_TYPE, null, props.getBoundedBy() );
            }
            break;
        }
        return prop;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
