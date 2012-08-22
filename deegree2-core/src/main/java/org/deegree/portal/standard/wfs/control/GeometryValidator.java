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

package org.deegree.portal.standard.wfs.control;

import javax.servlet.http.HttpServletRequest;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCUtils;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.portal.PortalException;

import com.vividsolutions.jts.algorithm.CGAlgorithms;

/**
 * This listener validates simple geometries. The geometries to be validated are passed in as the value of the
 * <code>'GEOMETRY'</code> parameter of such an RPC request:
 *
 * <pre>
 *   &lt;methodCall&gt;
 *   &lt;methodName&gt;dig:checkGeometry&lt;/methodName&gt;
 *   &lt;params&gt;
 *   &lt;param&gt;&lt;value&gt;&lt;struct&gt;&lt;member&gt;
 *   &lt;name&gt;GEOMETRY&lt;/name&gt;
 *   &lt;value&gt;&lt;string&gt;POLYGON(( 7.38 49.64, 12.03 48.7, 11.34 51.66, 11.34 51.66, 7.38 49.64 ))&lt;/string&gt;&lt;/value&gt;
 *   &lt;/member&gt;&lt;/struct&gt;&lt;/value&gt;&lt;/param&gt;
 *   &lt;/params&gt;
 *   &lt;/methodCall&gt;
 * </pre>
 *
 * Subclasses may override {@link #validateRequest( RPCWebEvent rpcEvent )},
 * {@link #createGeometry( RPCWebEvent rpcEvent )} and/or validateGeometry() if they want to change the behaviour
 * regarding request validation, geometry creation and/or geometry validation, respectively. Especially the last method
 * may be different according to use-cases. The result of the validation is put in the session under the key
 * <code>GeometryValidator.GEOMETRY_STATE</code>.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GeometryValidator extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( GeometryValidator.class );

    /** Attribute name for the geometry validity state */
    public static final String GEOMETRY_STATE = "GEOMETRY_STATE";

    /** Indicates that the geometry is perfectly valid */
    public static final String VALID_GEOMETRY = "VALID_GEOMETRY";

    /** Indicates that the geometry is invalid (self intersection, missing points, etc) */
    public static final String INVALID_GEOMETRY = "INVALID_GEOMETRY";

    /** Indicates that the geometry is OK, although it may not be considered formally correct in most GIS contexts. */
    public static final String INFORMALLY_VALID_GEOMETRY = "INFORMALLY_VALID_GEOMETRY";

    protected Geometry geometry;

    /**
     * This method validates the RPC-encoded request ({@link #validateRequest(RPCWebEvent)}), create some geometry
     * from the request parameters ({@link #createGeometry(RPCWebEvent)} and then validate this geometry ({@link #validateGeometry()}.
     * The result of this validate is made available to the request object as an attribute under the key
     * <code>GeometryValidator.GEOMETRY_STATE</code>.
     *
     * @param event
     *            the event containing the RPC-emcoded request.
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpcEvent = (RPCWebEvent) event;

        try {
            validateRequest( rpcEvent );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_INVALID_RPC", e.getLocalizedMessage() ) );
            return;
        }

        Object validationResult = INVALID_GEOMETRY;

        try {
            geometry = createGeometry( rpcEvent );
        } catch ( Exception e ) {
            // do not consider this as an exception - and thus return the error
            // mesg back to the page -, but as a failure to create a valid geometry
            LOG.logDebug( e.getMessage(), e );
        }

        try {
            validationResult = validateGeometry();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            // TODO i18nize
            gotoErrorPage( e.getLocalizedMessage() );
            return;
        }

        HttpServletRequest req = (HttpServletRequest) getRequest();
        req.setAttribute( GEOMETRY_STATE, validationResult );
    }

    /**
     * Validates the incoming RPc request. This method looks for an RPC parameter (struct) named <code>'GEOMETRY'</code>,
     * checks if the valeu is empty and looks no further. The expected value is a well-known geometry representation.
     *
     * @param rpcEvent
     *            the event containing at least a struct with a string type parameter named <code>'GEOMETRY'</code>.
     * @throws Exception
     *             If no such parameter is found or if the value is empty. This is interpreted as an exception and the
     *             resulting message is sent back to the client.
     */
    protected void validateRequest( RPCWebEvent rpcEvent )
                            throws Exception {

        RPCParameter[] pars = rpcEvent.getRPCMethodCall().getParameters();
        if ( pars == null || pars.length != 1 ) {
            throw new PortalException( "The RPC request must contain a 'GEOMETRY' parameter." );
        }
        RPCStruct struct = (RPCStruct) pars[0].getValue();

        String geomString = RPCUtils.getRpcPropertyAsString( struct, "GEOMETRY" );
        if ( geomString == null || geomString.length() == 0 ) {
            // TODO i18nize
            throw new PortalException( "The RPC request does not contains a well-known "
                                       + " text representation of a geometry." );
        }
    }

    /**
     * Creates the geometry from the data provided it the request (rpcEvent).
     *
     * @see #validateRequest(RPCWebEvent)
     * @param rpcEvent
     *            the <code>RPCWebEvent</code> conatining the request
     * @return a new geometry. This method uses {@link WKTAdapter#wrap(String, org.deegree.model.crs.CoordinateSystem)}
     *         to create the new <code>Geometry</code>.
     * @throws Exception
     *             if the <code>Geometry</code> creation failed.
     */
    protected Geometry createGeometry( RPCWebEvent rpcEvent )
                            throws Exception {
        RPCParameter[] pars = rpcEvent.getRPCMethodCall().getParameters();
        RPCStruct struct = (RPCStruct) pars[0].getValue();
        String geomString = RPCUtils.getRpcPropertyAsString( struct, "GEOMETRY" );
        LOG.logDebug( "Digitized geometry: \n" + geomString );
        return WKTAdapter.wrap( geomString, null );
    }

    /**
     * Validates this object's <code>Geometry</code> and return an object, preferably a code, indicating the validity
     * state of the geometry. There are three type of geometry validity states.
     * <ul>
     * <li><code>GeometryValidator.VALID_GEOMETRY</code>: The geometry is perfecly OK,</li>
     * <li><code>GeometryValidator.INVALID_GEOMETRY</code>: The geometry is not OK (self intersection, missing
     * points, etc),</li>
     * <li><code>GeometryValidator.INFORMALLY_VALID_GEOMETRY</code>: The geometry is OK, although it may not be
     * considered formally correct in most GIS contexts. For example, polygons are supposed to have counter-clockwise
     * outter rings, lines may no self-intersect, etc.</li>
     * </ul>
     * <br/> The meaning of those may also vary according to the use-case, e.g. a polygon that is counter-clockwise but
     * is required to have at least 4 vertices. <br/> This method employs the Java Topology Suite to check for geometry
     * validity ({@link com.vividsolutions.jts.geom.Geometry#isValid()}) and, in the case of polygons, for the order
     * of the coordinates (in other words, whether they are counter-clockwise}. <br/> The result of the validation is
     * available in the request under the key <code>GeometryValidator.GEOMETRY_STATE</code>.
     *
     * @return the geometry's validity state
     */
    protected Object validateGeometry() {

        String result = INVALID_GEOMETRY;

        try {
            com.vividsolutions.jts.geom.Geometry jtsGeom = JTSAdapter.export( geometry );
            if ( jtsGeom.isValid() ) { // means is OK, really
                result = VALID_GEOMETRY;
                if ( jtsGeom instanceof com.vividsolutions.jts.geom.Polygon ) {
                    if ( !CGAlgorithms.isCCW( jtsGeom.getCoordinates() ) ) {
                        result = INFORMALLY_VALID_GEOMETRY;
                    }
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            // just in case
            result = INVALID_GEOMETRY;
        }

        LOG.logInfo( "Geometry validation result: " + result );

        return result;
    }
}
