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
package org.deegree.model.feature.schema;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.model.feature.Feature;
import org.deegree.model.feature.Property;

/**
 * A {@link FeatureType} describes a class of {@link Feature}s.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface FeatureType {

    /**
     * Returns the name that features of this type have.
     * <p>
     * In the GML representation, this corresponds to the feature's element name.
     * </p>
     * 
     * @return the name of the feature instance
     */
    public QName getName();

    /**
     * Returns the property declarations of this feature type
     * 
     * @return the property declarations
     */
    public List<PropertyDeclaration> getPropertyDeclarations();

    /**
     * Creates a new {@link Feature} instance (that is of this type).
     * 
     * @param fid
     *            feature id (null means feature has no id)
     * @param props
     *            properties
     * @return a new <code>Feature</code> instance
     */
    public Feature newFeature( String fid, List<Property<?>> props );
}
