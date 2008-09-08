//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
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
package org.deegree.model.gml;

import java.util.HashMap;
import java.util.Map;

import org.deegree.model.feature.Feature;
import org.deegree.model.geometry.Geometry;

/**
 * Allows the lookup GML objects
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLIdContext {

    private Map<String, Object> idToObject = new HashMap<String, Object>();

    private Map<String, Feature> idToFeature = new HashMap<String, Feature>();

    private Map<String, Geometry> idToGeometry = new HashMap<String, Geometry>();

    public Object getObject( String id ) {
        return idToObject.get( id );
    }

    public Feature getFeature( String id ) {
        return idToFeature.get( id );
    }

    public Geometry getGeometry( String id ) {
        return idToGeometry.get( id );
    }

    public void addFeature( Feature feature ) {
        idToFeature.put( feature.getId(), feature );
    }

    // public void addGeometry (Geometry geometry) {
    // }
}
