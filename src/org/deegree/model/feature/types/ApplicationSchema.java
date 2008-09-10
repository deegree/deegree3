//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.feature.types;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Defines a number of {@link FeatureType}s and their substitution relations.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class ApplicationSchema {

    private Map<QName,FeatureType> ftNameToFt = new HashMap<QName,FeatureType>();

    public FeatureType[] getFeatureTypes () {
        return null;
    }    
    
    public FeatureType getFeatureType (QName ftName) {
        return ftNameToFt.get( ftName );
    }

    public FeatureType getSubstitutions (FeatureType ft) {
        return null;
    }

    public FeatureType getConcreteSubstitutions (FeatureType ft) {
        return null;
    }
    
    public boolean isValidSubstitution (FeatureType ft, FeatureType substitution ) {
        return false;
    }
    
}
