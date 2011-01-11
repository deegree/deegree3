//$HeadURL$
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

package org.deegree.services.wpvs.controller.getview;

/**
 * The <code>GetViewResponseParameters</code> class wraps the response parameters.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 *
 */
public class GetViewResponseParameters {

    private final boolean transparency;

    private final String format;

    private final double quality;

    private final String exceptionFormat;

    /**
     * @param transparency
     * @param format
     * @param quality
     * @param exceptionFormat
     */
    public GetViewResponseParameters( boolean transparency, String format, double quality, String exceptionFormat ) {
        this.transparency = transparency;
        this.format = format;
        this.quality = quality;
        this.exceptionFormat = exceptionFormat;
    }

    /**
     * @return the transparency
     */
    public final boolean isTransparency() {
        return transparency;
    }

    /**
     * @return the format
     */
    public final String getFormat() {
        return format;
    }

    /**
     * @return the quality
     */
    public final double getQuality() {
        return quality;
    }

    /**
     * @return the exceptionFormat
     */
    public final String getExceptionFormat() {
        return exceptionFormat;
    }

}
