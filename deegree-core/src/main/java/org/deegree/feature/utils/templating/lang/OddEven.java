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
package org.deegree.feature.utils.templating.lang;

import static java.util.Collections.singletonList;
import static org.deegree.commons.utils.JavaUtils.generateToString;

import java.util.HashMap;

import org.deegree.feature.Feature;
import org.deegree.feature.property.Property;

/**
 * <code>OddEven</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OddEven {

    private String name;

    private boolean odd;

    /**
     * @param name
     * @param odd
     */
    public OddEven( String name, boolean odd ) {
        this.name = name;
        this.odd = odd;
    }

    /**
     * @param sb
     * @param defs
     * @param obj
     * @param idx
     * @param geometries
     */
    public void eval( StringBuilder sb, HashMap<String, Object> defs, Object obj, int idx, boolean geometries ) {
        if ( idx % 2 == 0 ^ odd ) {
            return;
        }

        if ( obj instanceof Feature ) {
            new FeatureTemplateCall( name, singletonList( "*" ), false ).eval( sb, defs, obj, geometries );
        }
        if ( obj instanceof Property ) {
            new PropertyTemplateCall( name, singletonList( "*" ), false ).eval( sb, defs, obj, geometries );
        }
    }

    @Override
    public String toString() {
        return generateToString( this );
    }

}
