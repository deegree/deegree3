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
package org.deegree.ogcwebservices.wfs.operation;

import org.deegree.model.feature.FeatureCollection;
import org.deegree.ogcwebservices.AbstractOGCWebServiceRequest;
import org.deegree.ogcwebservices.DefaultOGCWebServiceResponse;

/**
 * Represents the response to a {@link GetFeature} request.
 * <p>
 * The response to a {@link GetFeature} request is controlled by the outputFormat attribute. The
 * default value for the outputFormat attribute shall be GML indicating that a WFS must generate a
 * GML document of the result set that conforms to the Geography Markup Language (GML) 3.1.1
 * specification. Vendor specific output formats can also be generated but they must be declared in
 * the capabilities document.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FeatureResult extends DefaultOGCWebServiceResponse {

    private FeatureCollection featureCollection;

    /**
     * Creates a new instance of <code>FeatureResult</code>.
     *
     * @param request
     * @param featureCollection
     */
    public FeatureResult( AbstractOGCWebServiceRequest request, FeatureCollection featureCollection ) {
        super( request );
        this.featureCollection = featureCollection;
    }

    /**
     * Returns the result as a {@link FeatureCollection}.
     *
     * @return the result as a FeatureCollection
     */
    public Object getResponse() {
        return this.featureCollection;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        String ret = this.getClass().getName()
            + ":\n";
        ret = getClass().getName()
            + ":\n";
        ret += ( "response = "
            + featureCollection + "\n" );
        return ret;
    }
}
