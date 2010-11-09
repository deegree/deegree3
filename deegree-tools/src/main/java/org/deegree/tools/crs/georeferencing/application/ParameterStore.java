//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.tools.crs.georeferencing.application;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;

/**
 * Helper class to store the parameters from the commandLine.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ParameterStore {

    private final URL mapURL;

    private final CRS CRS;

    private final String format;

    private final String layers;

    private final Envelope bbox;

    private final int qor;

    // private final Rectangle rasterEnvelope;

    private final static String separator = "\\p{Space}*[ ;/]\\p{Space}*";

    public ParameterStore( URL mapURL, CRS CRS, String format, String layers, Envelope bbox, int qor ) {

        this.mapURL = mapURL;
        this.CRS = CRS;
        this.format = format;
        this.layers = layers;
        this.bbox = bbox;
        this.qor = qor;
        // this.rasterEnvelope = new Rectangle( qor, qor );

    }

    public URL getMapURL() {
        return mapURL;
    }

    public CRS getCRS() {
        return CRS;
    }

    public String getFormat() {
        return format;
    }

    public String getLayers() {
        return layers;
    }

    public Envelope getBbox() {
        return bbox;
    }

    public double[] getBboxAsArray() {
        double[] d = new double[4];
        d[0] = bbox.getMin().get0();
        d[1] = bbox.getMin().get1();
        d[2] = bbox.getMax().get0();
        d[3] = bbox.getMax().get1();
        // d[0] = bbox.getMin().get1();
        // d[1] = bbox.getMin().get0();
        // d[2] = bbox.getMax().get1();
        // d[3] = bbox.getMax().get0();
        return d;
    }

    public List<Point3d> getBboxAsPoint3d() {
        List<Point3d> pointsList = new ArrayList<Point3d>();
        Point3d pMin = new Point3d( bbox.getMin().get0(), bbox.getMin().get1(), Double.NaN );
        Point3d pMax = new Point3d( bbox.getMax().get0(), bbox.getMax().get1(), Double.NaN );
        pointsList.add( pMin );
        pointsList.add( pMax );
        return pointsList;
    }

    public int getQor() {
        return qor;
    }

    // public Rectangle getRasterEnvelope() {
    // return rasterEnvelope;
    // }

}
