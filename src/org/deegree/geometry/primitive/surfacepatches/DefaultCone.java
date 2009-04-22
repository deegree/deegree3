//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.geometry.primitive.surfacepatches;

import java.util.List;

import org.deegree.geometry.primitive.Point;

/**
 * TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class DefaultCone implements Cone {

    private List<List<Point>> grid;
    
    private int rows;
    
    private int columns;
    
    public DefaultCone( List<List<Point>> grid ) {
        this.grid = grid;
        this.rows = getNumRows();
        this.columns = getNumColumns();
    }
    
    public DefaultCone( List<List<Point>> grid, int rows, int columns ) {
        this.grid = grid;
        this.rows = rows;
        this.columns = columns;
    }

    @Override
    public GriddedSurfaceType getGriddedSurfaceType() {
        return GriddedSurfaceType.CONE;
    }

    @Override
    public int getNumColumns() {
        return grid.get( 0 ).size();
    }

    @Override
    public int getNumRows() {
        return grid.size();
    }

    @Override
    public List<Point> getRow( int rownum ) {
        return grid.get( rownum );
    }

    @Override
    public List<List<Point>> getRows() {
        return grid;
    }

    @Override
    public double getArea() {
        //TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCoordinateDimension() {
        return 3;
    }

    @Override
    public SurfacePatchType getSurfacePatchType() {
        return SurfacePatchType.GRIDDED_SURFACE_PATCH;
    }

}
