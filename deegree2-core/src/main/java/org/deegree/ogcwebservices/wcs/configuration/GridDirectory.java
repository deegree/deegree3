// $HeadURL$
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
package org.deegree.ogcwebservices.wcs.configuration;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;

/**
 * An instance of <tt>GridDirectory</tt> describes a directory in the file system containing grid
 * coverages within the envelope assigned to the <tt>Directory</tt>. The name of the
 * <tt>Directory</tt> may is build from variable indicated by a leadin '$' (e.g.
 * C:/rasterdata/luftbilder/775165/$YEAR/$MONTH/$DAY/$ELEVATION/l0.5) in this case the variable
 * parts of the name can be replaced by an application with concrete values. It is in the
 * responsibility of the application to use valid values for the variables. Known variable names
 * are:
 * <ul>
 * <li>$YEAR
 * <li>$MONTH
 * <li>$DAY
 * <li>$HOUR
 * <li>$MINUTE
 * <li>$ELEVATION
 * </ul>
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class GridDirectory extends Directory {

    private double tileWidth = 0;

    private double tileHeight = 0;

    /**
     * file extentions will be empty. this will cause that all files in the directory will be
     * recognized
     *
     * @param name
     *            name of the directory
     * @param envelope
     *            enclosing envelope of the tiles in the directory
     * @param crs
     *            CRS of the data
     * @param tileWidth
     *            width (pixels) of the files in the directory
     * @param tileHeight
     *            height (pixels) of the files in the directory
     */
    public GridDirectory( String name, Envelope envelope, CoordinateSystem crs, double tileWidth, double tileHeight ) {
        super( name, envelope, crs );
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    /**
     * @param name
     *            name of the directory
     * @param envelope
     *            enclosing envelope of the tiles in the directory
     * @param crs
     *            CRS of the data
     * @param fileExtensions
     *            list of reconized file extensions
     * @param tileWidth
     *            width (pixels) of the files in the directory
     * @param tileHeight
     *            height (pixels) of the files in the directory
     */
    public GridDirectory( String name, Envelope envelope, CoordinateSystem crs, String[] fileExtensions,
                          double tileWidth, double tileHeight ) {
        super( name, envelope, crs, fileExtensions );
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    /**
     * @return Returns the tileHeight.
     *
     */
    public double getTileHeight() {
        return tileHeight;
    }

    /**
     * @param tileHeight
     *            The tileHeight to set.
     *
     */
    public void setTileHeight( double tileHeight ) {
        this.tileHeight = tileHeight;
    }

    /**
     * @return Returns the tileWidth.
     *
     */
    public double getTileWidth() {
        return tileWidth;
    }

    /**
     * @param tileWidth
     *            The tileWidth to set.
     *
     */
    public void setTileWidth( double tileWidth ) {
        this.tileWidth = tileWidth;
    }

}
