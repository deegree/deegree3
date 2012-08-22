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

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public interface Extension {

    /**
     *
     */
    final String FILEBASED = "file";

    /**
     *
     */
    final String NAMEINDEXED = "nameIndexed";

    /**
     *
     */
    final String SHAPEINDEXED = "shapeIndexed";

    /**
     *
     */
    final String DATABASEINDEXED = "databaseIndexed";

    /**
     *
     */
    final String ORACLEGEORASTER = "OracleGeoRaster";

    /**
     *
     */
    final String SCRIPTBASED = "script";

    /**
     * returns the type of the coverage source that is described be an extension
     *
     * @return the type of the coverage source that is described be an extension
     */
    String getType();

    /**
     * returns the minimum scale of objects that are described by an <tt>Extension</tt> object
     *
     * @return the minimum scale of objects that are described by an <tt>Extension</tt> object
     */
    double getMinScale();

    /**
     * returns the maximum scale of objects that are described by an <tt>Extension</tt> object
     *
     * @return the maximum scale of objects that are described by an <tt>Extension</tt> object
     */
    double getMaxScale();

    /**
     * returns the offset of the data. 0 will be returned if no offset is defined. Data first must
     * be divided by the scale factor (@see #getScaleFactor()) before sustracting the offset
     *
     * @return the offset of the data. 0 will be returned if no offset is defined. Data first must
     *         be divided by the scale factor (@see #getScaleFactor()) before sustracting the offset
     */
    double getOffset();

    /**
     * returns the scale factor of the data. If no scale factor is defined 1 will be returned. Data
     * first must be divided by the scale factor (@see #getScaleFactor()) before sustracting the
     * offset
     *
     * @return the scale factor of the data. If no scale factor is defined 1 will be returned. Data
     *         first must be divided by the scale factor (@see #getScaleFactor()) before sustracting
     *         the offset
     */
    double getScaleFactor();

    /**
     * returns all <tt>Resolution</tt>s. If no <tt>Resolution</tt> can be found for the passed
     * scale an empty array will be returned.
     *
     * @return <tt>Resolution</tt>s matching the passed scale
     */
    Resolution[] getResolutions();

    /**
     * returns the <tt>Resolution</tt>s matching the passed scale. If no <tt>Resolution</tt>
     * can be found for the passed scale an empty array will be returned.
     *
     * @param scale
     *            scale the returned resolutions must fit
     *
     * @return <tt>Resolution</tt>s matching the passed scale
     */
    Resolution[] getResolutions( double scale );

    /**
     * @param resolution
     */
    void addResolution( Resolution resolution );

}
