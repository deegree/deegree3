//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.geometry.standard.aggregate;

import java.util.List;

import org.deegree.crs.CRS;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.primitive.Solid;

/**
 * Default implementation of {@link MultiSolid}.
 *  
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DefaultMultiSolid extends DefaultMultiGeometry<Solid> implements MultiSolid {
    
    /**
     * Creates a new {@link DefaultMultiSolid} from the given parameters.
     * 
     * @param id
     *            identifier of the created geometry object
     * @param crs
     *            coordinate reference system
     * @param members
     */
    public DefaultMultiSolid( String id, CRS crs, List<Solid> members ) {
        super( id, crs, members );
    }

    @Override
    public double getVolume() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public MultiGeometryType getMultiGeometryType() {
        return MultiGeometryType.MULTI_SOLID;
    }
}
