//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

import org.deegree.gml.GMLObject;

/**
 * {@link PropertyType} that defines a property with a {@link GMLObject} value.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLObjectPropertyType extends AbstractPropertyType {

    private final ValueRepresentation representation;

    /**
     * Creates a new {@link GMLObjectPropertyType} instance.
     * 
     * @param name
     * @param minOccurs
     * @param maxOccurs
     * @param isAbstract
     * @param isNillable
     * @param substitutions
     * @param representation
     */
    public GMLObjectPropertyType( QName name, int minOccurs, int maxOccurs, boolean isAbstract, boolean isNillable,
                                  List<PropertyType> substitutions, ValueRepresentation representation ) {
        super( name, minOccurs, maxOccurs, isAbstract, isNillable, substitutions );
        this.representation = representation;
    }

    /**
     * Returns the allowed representation form of the value object.
     * 
     * @return the allowed representation form, never <code>null</code>
     */
    public ValueRepresentation getAllowedRepresentation() {
        return representation;
    }
}