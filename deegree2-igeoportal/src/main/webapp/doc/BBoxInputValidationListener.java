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
package de.latlon;

import javax.servlet.ServletRequest;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;

/**
 * Example listener for creating new modules in iGeoPortal std.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BBoxInputValidationListener extends AbstractListener {

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.enterprise.control.AbstractListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpc = (RPCWebEvent) event;
        RPCMethodCall mc = rpc.getRPCMethodCall();

        // notice that a RPC parameter is not named but we know our method call
        // contains just one parameter
        RPCParameter param = mc.getParameters()[0];

        // we also know that the parameters value is an RPC structure (mapped to RPCStruct class)
        RPCStruct struct = (RPCStruct) param.getValue();

        // at least we know the names of the structure members and their data type
        Double minx = (Double) struct.getMember( "minx" ).getValue();
        Double miny = (Double) struct.getMember( "miny" ).getValue();
        Double maxx = (Double) struct.getMember( "maxx" ).getValue();
        Double maxy = (Double) struct.getMember( "maxy" ).getValue();

        double[] box = adjustBoundingBox( minx.doubleValue(), miny.doubleValue(), maxx.doubleValue(),
                                          maxy.doubleValue() );

        ServletRequest req = this.getRequest();
        req.setAttribute( "BBOX", box );
    }

    private double[] adjustBoundingBox( double minx, double miny, double maxx, double maxy ) {
        double t = 0;

        // adjust min/max values
        if ( minx > maxx ) {
            t = minx;
            minx = maxx;
            maxx = t;
        }
        if ( miny > maxy ) {
            t = miny;
            miny = maxy;
            maxy = t;
        }

        // adjust size
        if ( maxx - minx < 2 ) {
            maxx = minx + 2;
        }
        if ( maxy - miny < 2 ) {
            maxy = miny + 2;
        }
        return new double[] { minx, miny, maxx, maxy };
    }

}
