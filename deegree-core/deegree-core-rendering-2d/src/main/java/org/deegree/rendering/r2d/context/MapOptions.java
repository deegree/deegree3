//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.rendering.r2d.context;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class MapOptions {

    private Quality quality;

    private Interpolation interpol;

    private Antialias antialias;

    private int maxFeatures;

    public MapOptions( Quality quality, Interpolation interpol, Antialias antialias, int maxFeatures ) {
        this.quality = quality;
        this.interpol = interpol;
        this.antialias = antialias;
        this.maxFeatures = maxFeatures;
    }

    /**
     * @return the quality
     */
    public Quality getQuality() {
        return quality;
    }

    /**
     * @param quality
     *            the quality to set
     */
    public void setQuality( Quality quality ) {
        this.quality = quality;
    }

    /**
     * @return the interpol
     */
    public Interpolation getInterpolation() {
        return interpol;
    }

    /**
     * @param interpol
     *            the interpol to set
     */
    public void setInterpolation( Interpolation interpol ) {
        this.interpol = interpol;
    }

    /**
     * @return the antialias
     */
    public Antialias getAntialias() {
        return antialias;
    }

    /**
     * @param antialias
     *            the antialias to set
     */
    public void setAntialias( Antialias antialias ) {
        this.antialias = antialias;
    }

    /**
     * @return the maxFeatures
     */
    public int getMaxFeatures() {
        return maxFeatures;
    }

    /**
     * @param maxFeatures
     *            the maxFeatures to set
     */
    public void setMaxFeatures( int maxFeatures ) {
        this.maxFeatures = maxFeatures;
    }

    /**
     * <code>Quality</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author: aschmitz $
     * 
     * @version $Revision: 32136 $, $Date: 2011-10-12 15:21:52 +0200 (Wed, 12 Oct 2011) $
     */
    public static enum Quality {
        /***/
        LOW, /***/
        NORMAL, /***/
        HIGH
    }

    /**
     * <code>Interpolation</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author: aschmitz $
     * 
     * @version $Revision: 32136 $, $Date: 2011-10-12 15:21:52 +0200 (Wed, 12 Oct 2011) $
     */
    public static enum Interpolation {
        /***/
        NEARESTNEIGHBOR, /***/
        NEARESTNEIGHBOUR, /***/
        BILINEAR, /***/
        BICUBIC
    }

    /**
     * <code>Antialias</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author: aschmitz $
     * 
     * @version $Revision: 32136 $, $Date: 2011-10-12 15:21:52 +0200 (Wed, 12 Oct 2011) $
     */
    public static enum Antialias {
        /***/
        IMAGE, /***/
        TEXT, /***/
        BOTH, /***/
        NONE
    }

}
