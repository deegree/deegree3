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
package org.deegree.ogcbase;

import org.deegree.datatypes.QualifiedName;

/**
 * {@link PropertyPathStep} implementation that selects an element's value (using the element name
 * as property name).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 * @see PropertyPathStep
 */
public class ElementStep extends PropertyPathStep {

    /**
     * Creates a new instance of <code>ElementStep</code> that selects the property with the given
     * name.
     *
     * @param elementName
     */
    public ElementStep( QualifiedName elementName ) {
        super( elementName );
    }

    @Override
    public boolean equals( Object obj ) {
        if ( !( obj instanceof ElementStep ) ) {
            return false;
        }
        ElementStep that = (ElementStep) obj;
        return this.propertyName.equals( that.propertyName );
    }

    @Override
    public String toString() {
        return this.propertyName.getPrefixedName();
    }
}
