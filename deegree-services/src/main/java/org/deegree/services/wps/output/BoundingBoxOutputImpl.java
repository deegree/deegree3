//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps.output;

import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.services.jaxb.wps.BoundingBoxOutputDefinition;

/**
 * Identifies this {@link ProcessletOutput} to be a bounding box and provides a method for setting it.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class BoundingBoxOutputImpl extends ProcessletOutputImpl implements BoundingBoxOutput {

    private Envelope value;

    private static GeometryFactory geomFac = new GeometryFactory();

    public BoundingBoxOutputImpl( BoundingBoxOutputDefinition outputType, boolean isRequested ) {
        super( outputType, isRequested );
    }

    @Override
    public void setValue( double lowerX, double lowerY, double upperX, double upperY, String crsName ) {
        setValue( new double[] { lowerX, lowerY }, new double[] { upperX, upperY }, crsName );
    }

    @Override
    public void setValue( double[] lower, double[] upper, String crsName ) {
        setValue( geomFac.createEnvelope( lower, upper, new CRS (crsName) ) );
    }

    public void setValue( Envelope value ) {
        this.value = value;
    }

    public Envelope getValue() {
        return value;
    }
}
