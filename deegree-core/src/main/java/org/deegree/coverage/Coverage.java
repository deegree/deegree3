//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.coverage;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SampleResolution;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;

/**
 * 
 * A Coverage, as defined by the OGC and ISO/TC 211, is a function describing the distribution of some set of properties
 * over a spatial-temporal region. It is a mathematical function from a spatial-temporal set (called the domain of a
 * function) to some value set (called the range of the function).
 * 
 * <p>
 * Coverage are not always gridded data. Although a Coverage might be based on a grid (e.g. a Raster) or a rectified
 * grid, it might also be based on a collection of curves, triangles or other geometries. Common examples of coverages
 * include remotely sensed images and aerial photographs, as well as soil, rock type, temperature and elevation
 * distributions. <cite>From GML-Geography Mark-Up Language, by Ron Lake et. all (Wiley & sons Ltd., published 2004)
 * page 234/235.</cite>
 * 
 * <p>
 * The above idea is captured by this Interface, to get a sampled part of the coverage the getAsRaster method can be
 * used.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface Coverage {

    /**
     * Returns the (spatial) domain of this coverage.
     * 
     * @return The envelope of the coverage.
     */
    public Envelope getEnvelope();

    /**
     * Returns the native coordinate system this coverage is defined in.
     * 
     * @return the coordinate system of the coverage
     */
    public CRS getCoordinateSystem();

    /**
     * Returns information about the possible sample resolutions of this coverage.
     * 
     * @return information about the possible sample resolutions.
     */
    public ResolutionInfo getResolutionInfo();

    /**
     * Get a rasterized extent of this coverage by applying the given sample resolution to the given spatial extent of
     * the coverage and applying the given interpolation if needed. Note this method also should support srs
     * transformations.
     * 
     * @param spatialExtent
     *            the area of interest of resulting raster
     * @param resolution
     *            the resolution to use for sampling the given extent.
     * @param interpolation
     *            the interpolation to use, if the resolution does not match the 'native' resolution of the coverage.
     * @return a rasterized extent of this coverage.
     */
    public AbstractRaster getAsRaster( Envelope spatialExtent, SampleResolution resolution,
                                       InterpolationType interpolation );

}