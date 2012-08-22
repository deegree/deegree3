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
package org.deegree.portal.standard.security.control;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCUtils;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.model.Right;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.Role;
import org.deegree.security.drm.model.SecuredObject;
import org.deegree.security.drm.model.Service;
import org.deegree.security.drm.model.User;
import org.w3c.dom.Document;

/**
 * This <code>Listener</code> reacts on RPC-StoreRights events.
 * 
 * Access constraints:
 * <ul>
 * <li>only users that have the 'SEC_ADMIN'-role are allowed</li>
 * </ul>
 * 
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StoreRightsListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( StoreRightsListener.class );

    private static final String MINX = "-180.0";

    private static final String MINY = "-90.0";

    private static final String MAXX = "180.0";

    private static final String MAXY = "90.0";

    @Override
    public void actionPerformed( FormEvent event ) {

        // the Role for which the rights are to be set
        int roleId = -1;
        // array of ints, ids of Layers (SecuredObjects) for which
        // the Role has access rights
        int[] layers = null;
        // corresponding maps of key (PropertyName) / value-pairs that
        // constitute access constraints
        Map<String, Object>[] layerConstraints = null;

        SecurityAccessManager manager = null;
        SecurityTransaction transaction = null;

        try {
            RPCWebEvent ev = (RPCWebEvent) event;
            RPCMethodCall rpcCall = ev.getRPCMethodCall();
            RPCParameter[] params = rpcCall.getParameters();

            // validates the incoming method call and extracts the roleID
            roleId = validate( params );

            boolean oldMode = params.length == 3;

            int serviceId = -1;
            boolean sldAllowed = false;
            String constraints = null;
            if ( !oldMode ) {
                try {
                    serviceId = Integer.parseInt( (String) params[4].getValue() );
                    sldAllowed = Boolean.parseBoolean( (String) params[5].getValue() );
                    constraints = (String) params[6].getValue();
                } catch ( Throwable e ) {
                    // ignore params
                }
            }

            RPCParameter[] layerGetMapParams = (RPCParameter[]) params[1].getValue();
            RPCParameter[] layerGetFeatureInfoParams = oldMode ? null : (RPCParameter[]) params[2].getValue();
            layers = new int[layerGetMapParams.length];
            boolean[] getFeatureInfoLayers = oldMode ? null : new boolean[layerGetMapParams.length];
            layerConstraints = new Map[layerGetMapParams.length];
            extractLayerValues( layers, getFeatureInfoLayers, layerConstraints, layerGetMapParams,
                                layerGetFeatureInfoParams );

            // extract FeatureType rights
            if ( !( params[oldMode ? 2 : 3].getValue() instanceof RPCParameter[] ) ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_STORERIGHTS_THIRD_PARAM" ) );
            }

            // array of ints, ids of FeatureTypes (SecuredObjects) for which
            // the Role has access rights
            FeatureTypeRight[] featureTypes = extractFeatureTypeValues( params );

            transaction = SecurityHelper.acquireTransaction( this );
            SecurityHelper.checkForAdminRole( transaction );

            manager = SecurityAccessManager.getInstance();
            User user = transaction.getUser();
            Role role = transaction.getRoleById( roleId );

            // perform access check
            if ( !user.hasRight( transaction, "update", role ) ) {
                getRequest().setAttribute( "SOURCE", this.getClass().getName() );
                String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_MISSING_RIGHTS", role.getName() );
                getRequest().setAttribute( "MESSAGE", s );
                setNextPage( "error.jsp" );
                return;
            }

            if ( !oldMode ) {
                Service service = transaction.getServiceById( serviceId );
                RightType right = transaction.getRightByName( "SLD" );
                transaction.setServiceRight( service, role, sldAllowed ? right : null );
                transaction.setConstraints( service, role, constraints );
            }

            // set/delete access rights for Layers
            SecuredObject[] presentLayers = transaction.getAllSecuredObjects( ClientHelper.TYPE_LAYER );
            setAccessRightsForLayers( layers, getFeatureInfoLayers, layerConstraints, transaction, role, presentLayers );

            // set/delete access rights for FeatureTypes
            SecuredObject[] presentFeatureTypes = transaction.getAllSecuredObjects( ClientHelper.TYPE_FEATURETYPE );
            setAccessRightsForFeatureTypes( featureTypes, transaction, role, presentFeatureTypes );

            manager.commitTransaction( transaction );
            transaction = null;
            String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_SUCCESS", role.getID() );
            getRequest().setAttribute( "MESSAGE", s );
        } catch ( RPCException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_INVALID_REQ", e.getMessage() );
            getRequest().setAttribute( "MESSAGE", s );
            setNextPage( "error.jsp" );
            LOG.logDebug( e.getMessage(), e );
        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_ERROR", e.getMessage() );
            getRequest().setAttribute( "MESSAGE", s );
            setNextPage( "error.jsp" );
            LOG.logDebug( e.getMessage(), e );
        } finally {
            if ( manager != null && transaction != null ) {
                try {
                    manager.abortTransaction( transaction );
                } catch ( GeneralSecurityException e ) {
                    LOG.logDebug( e.getMessage(), e );
                }
            }
        }

    }

    private void setAccessRightsForFeatureTypes( FeatureTypeRight[] featureTypes, SecurityTransaction transaction,
                                                 Role role, SecuredObject[] presentFeatureTypes )
                            throws GeneralSecurityException, UnauthorizedException {
        for ( int i = 0; i < presentFeatureTypes.length; i++ ) {
            boolean selected = false;
            SecuredObject featureType = presentFeatureTypes[i];
            FeatureTypeRight ftr = null;
            for ( int j = 0; j < featureTypes.length; j++ ) {
                ftr = featureTypes[j];
                if ( featureType.getID() == ftr.id && ftr.access ) {
                    selected = true;
                    break;
                }
            }
            if ( selected ) {
                List<RightType> setRights = new ArrayList<RightType>();
                List<RightType> removedRights = new ArrayList<RightType>();
                setRights.add( RightType.GETFEATURE );
                setRights.add( RightType.DESCRIBEFEATURETYPE );

                if ( ftr.insert ) {
                    setRights.add( RightType.INSERT );
                } else {
                    removedRights.add( RightType.INSERT );
                }
                if ( ftr.update ) {
                    setRights.add( RightType.UPDATE );
                } else {
                    removedRights.add( RightType.UPDATE );
                }
                if ( ftr.delete ) {
                    setRights.add( RightType.DELETE );
                } else {
                    removedRights.add( RightType.DELETE );
                }
                RightType[] rights = removedRights.toArray( new RightType[removedRights.size()] );
                transaction.removeRights( featureType, role, rights );
                rights = setRights.toArray( new RightType[setRights.size()] );
                transaction.addRights( featureType, role, rights );
            } else {
                RightType[] rights = new RightType[] { RightType.GETFEATURE, RightType.DESCRIBEFEATURETYPE,
                                                      RightType.INSERT, RightType.DELETE, RightType.UPDATE };
                transaction.removeRights( featureType, role, rights );
            }
        }
    }

    private void setAccessRightsForLayers( int[] layers, boolean[] fiLayers, Map[] layerConstraints,
                                           SecurityTransaction transaction, Role role, SecuredObject[] presentLayers )
                            throws RPCException, GeneralSecurityException, UnauthorizedException {
        for ( int i = 0; i < presentLayers.length; i++ ) {
            boolean isAccessible = false;
            Map constraintMap = null;
            SecuredObject layer = presentLayers[i];
            int layerIdx = 0;
            for ( ; layerIdx < layers.length; layerIdx++ ) {
                if ( layer.getID() == layers[layerIdx] ) {
                    isAccessible = true;
                    constraintMap = layerConstraints[layerIdx];
                    break;
                }
            }
            if ( isAccessible ) {
                Filter filter = null;
                if ( constraintMap != null ) {
                    String xml = buildGetMapFilter( constraintMap );
                    if ( xml != null ) {
                        try {
                            Document doc = XMLTools.parse( new StringReader( xml ) );
                            filter = AbstractFilter.buildFromDOM( doc.getDocumentElement(), false );
                        } catch ( Exception e ) {
                            String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_FILTER_PARSING_ERROR", e.getMessage() );
                            throw new GeneralSecurityException( s );
                        }
                    }
                    if ( filter != null ) {
                        LOG.logInfo( "Back to XML: " + filter.to110XML() );
                    }
                }
                Right[] rights;
                if ( fiLayers == null || fiLayers[layerIdx] ) {
                    rights = new Right[] { new Right( layer, RightType.GETMAP, filter ),
                                          new Right( layer, RightType.GETFEATUREINFO ),
                                          new Right( layer, RightType.GETLEGENDGRAPHIC ) };
                } else {
                    rights = new Right[] { new Right( layer, RightType.GETMAP, filter ),
                                          new Right( layer, RightType.GETLEGENDGRAPHIC ) };
                }
                transaction.setRights( layer, role, rights );
            } else {
                transaction.removeRights( layer, role, new RightType[] { RightType.GETMAP, RightType.GETFEATUREINFO,
                                                                        RightType.GETLEGENDGRAPHIC } );
            }
        }
    }

    private FeatureTypeRight[] extractFeatureTypeValues( RPCParameter[] params )
                            throws RPCException {
        FeatureTypeRight[] ftr;
        RPCParameter[] featureTypeParams = (RPCParameter[]) params[2].getValue();
        ftr = new FeatureTypeRight[featureTypeParams.length];
        for ( int i = 0; i < featureTypeParams.length; i++ ) {
            if ( featureTypeParams[i].getValue() instanceof String ) {
                // to be compliant to former versions
                int id = 0;
                try {
                    id = Integer.parseInt( (String) featureTypeParams[i].getValue() );
                } catch ( NumberFormatException e ) {
                    String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_INT_EXPECTED" );
                    throw new RPCException( s );
                }

                ftr[i] = new FeatureTypeRight( id, true, false, false, false );

            } else if ( featureTypeParams[i].getValue().getClass() == RPCParameter[].class ) {
                RPCParameter[] pm = (RPCParameter[]) featureTypeParams[i].getValue();

                int id = 0;
                try {
                    id = Integer.parseInt( (String) pm[0].getValue() );
                } catch ( NumberFormatException e ) {
                    String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_INT_EXPECTED" );
                    throw new RPCException( s );
                }

                boolean access = false;
                boolean delete = false;
                boolean insert = false;
                boolean update = false;
                RPCStruct struct = (RPCStruct) pm[1].getValue();
                String s = RPCUtils.getRpcPropertyAsString( struct, "ACCESS" );
                access = "true".equals( s );
                s = RPCUtils.getRpcPropertyAsString( struct, "INSERT" );
                insert = "true".equals( s );
                s = RPCUtils.getRpcPropertyAsString( struct, "UPDATE" );
                update = "true".equals( s );
                s = RPCUtils.getRpcPropertyAsString( struct, "DELETE" );
                delete = "true".equals( s );
                ftr[i] = new FeatureTypeRight( id, access, insert, update, delete );
            } else {
                throw new RPCException( Messages.getMessage( "IGEO_STD_STORERIGHTS_WRONGTYPE" ) );
            }
        }
        return ftr;
    }

    private void extractLayerValues( int[] layers, boolean[] hasGetFeatureInfo, Map<String, Object>[] layerConstraints,
                                     RPCParameter[] getMap, RPCParameter[] getFeatureInfo )
                            throws RPCException {
        HashSet<Integer> fiLayers = new HashSet<Integer>();
        if ( hasGetFeatureInfo != null ) {
            for ( RPCParameter p : getFeatureInfo ) {
                fiLayers.add( Integer.valueOf( (String) p.getValue() ) );
            }
        }
        for ( int i = 0; i < getMap.length; i++ ) {
            // is the layer access constrained?
            if ( getMap[i].getValue() instanceof RPCParameter[] ) {
                layerConstraints[i] = new HashMap<String, Object>();
                RPCParameter[] constrainParams = (RPCParameter[]) getMap[i].getValue();
                try {
                    layers[i] = Integer.parseInt( (String) constrainParams[0].getValue() );
                } catch ( NumberFormatException e ) {
                    String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_INT_EXPECTED" );
                    throw new RPCException( s );
                }
                RPCParameter param = constrainParams[1];
                RPCStruct constraints = (RPCStruct) param.getValue();
                RPCMember[] members = constraints.getMembers();
                for ( int j = 0; j < members.length; j++ ) {
                    String propertyName = members[j].getName();
                    Object value = members[j].getValue();
                    if ( value instanceof RPCParameter[] ) {
                        String[] values = new String[( (RPCParameter[]) value ).length];
                        for ( int k = 0; k < values.length; k++ ) {
                            values[k] = (String) ( (RPCParameter[]) value )[k].getValue();
                        }
                        layerConstraints[i].put( propertyName, values );
                    } else if ( value instanceof String ) {
                        layerConstraints[i].put( propertyName, value );
                    } else {
                        String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_LAYER_ACCESSCONSTRAINTS" );
                        throw new RPCException( s );
                    }
                }
            } else if ( getMap[i].getValue() instanceof String ) {
                try {
                    layers[i] = Integer.parseInt( (String) getMap[i].getValue() );
                    if ( hasGetFeatureInfo != null ) {
                        hasGetFeatureInfo[i] = fiLayers.contains( layers[i] );
                    }
                } catch ( NumberFormatException e ) {
                    String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_INT_EXPECTED" );
                    throw new RPCException( s );
                }
            } else {
                String s = Messages.getMessage( "IGEO_STD_STORERIGHTS_LAYER_ACCESSCONSTRAINTS" );
                throw new RPCException( s );
            }
        }
    }

    private int validate( RPCParameter[] params )
                            throws RPCException {

        // FIXME this is a workaround !!!
        // originaly, params.length needed to be exactly 3. (thus the error message)
        // now, pdfplot client uses an RPC with a params.length of 4.
        if ( params.length != 3 && params.length != 4 && params.length != 7 ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_PARAMS_NUM", "3" ) );
        }
        // if ( params.length != 3 ) {
        // throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_PARAMS_NUM", "3" ) );
        // }
        if ( !( params[0].getValue() instanceof String ) ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_STORERIGHTS_FIRST_PARAM" ) );
        }

        // extract role-id
        int roleId = -1;
        try {
            roleId = Integer.parseInt( (String) params[0].getValue() );
        } catch ( NumberFormatException e ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_STORERIGHTS_ROLE_PARAM" ) );
        }
        // extract Layer rights
        if ( !( params[1].getValue() instanceof RPCParameter[] ) ) {
            throw new RPCException( Messages.getMessage( "IGEO_STD_STORERIGHTS_SECOND_PARAM" ) );
        }
        return roleId;
    }

    /**
     * Builds a filter encoding-expression as a constraint for GetMap-operations from the values stored in the given
     * <code>Map</code>.
     * 
     * @param constraintMap
     * @return String
     * @throws RPCException
     */
    String buildGetMapFilter( Map constraintMap )
                            throws RPCException {

        int operands = 0;
        StringBuffer sb = new StringBuffer( 1000 );

        // bbox
        if ( constraintMap.get( "bbox" ) != null ) {
            operands++;
            String minx = MINX;
            String miny = MINY;
            String maxx = MAXX;
            String maxy = MAXY;

            String[] bbox = (String[]) constraintMap.get( "bbox" );
            if ( bbox.length != 4 ) {
                throw new RPCException( Messages.getMessage( "IGEO_STD_STORERIGHTS_BBOX_ERROR" ) );
            }
            minx = bbox[0];
            miny = bbox[1];
            maxx = bbox[2];
            maxy = bbox[3];

            sb.append( "<ogc:Within>" );
            sb.append( "<ogc:PropertyName>GEOM</ogc:PropertyName>" );
            sb.append( "<gml:Box>" );
            sb.append( "<gml:coordinates>" );
            sb.append( minx ).append( ',' ).append( miny ).append( ' ' );
            sb.append( maxx ).append( ',' ).append( maxy );
            sb.append( "</gml:coordinates>" );
            sb.append( "</gml:Box></ogc:Within>" );
        }

        // bgcolor
        String[] bgcolors = (String[]) constraintMap.get( "bgcolor" );
        if ( bgcolors != null && bgcolors.length > 0 ) {
            operands++;
            if ( bgcolors.length > 1 ) {
                sb.append( "<ogc:Or>" );
            }
            for ( int i = 0; i < bgcolors.length; i++ ) {
                sb.append( "<ogc:PropertyIsEqualTo>" );
                sb.append( "<ogc:PropertyName>bgcolor</ogc:PropertyName>" );
                sb.append( "<ogc:Literal><![CDATA[" + bgcolors[i] + "]]></ogc:Literal>" );
                sb.append( "</ogc:PropertyIsEqualTo>" );
            }
            if ( bgcolors.length > 1 ) {
                sb.append( "</ogc:Or>" );
            }
        }

        // transparent
        String transparent = (String) constraintMap.get( "transparent" );
        if ( transparent != null ) {
            operands++;
            sb.append( "<ogc:PropertyIsEqualTo>" );
            sb.append( "<ogc:PropertyName>transparent</ogc:PropertyName>" );
            sb.append( "<ogc:Literal><![CDATA[" + transparent + "]]></ogc:Literal>" );
            sb.append( "</ogc:PropertyIsEqualTo>" );
        }

        // format
        String[] formats = (String[]) constraintMap.get( "format" );
        if ( formats != null && formats.length > 0 ) {
            operands++;
            if ( formats.length > 1 ) {
                sb.append( "<ogc:Or>" );
            }
            for ( int i = 0; i < formats.length; i++ ) {
                sb.append( "<ogc:PropertyIsEqualTo>" );
                sb.append( "<ogc:PropertyName>format</ogc:PropertyName>" );
                sb.append( "<ogc:Literal><![CDATA[" + formats[i] + "]]></ogc:Literal>" );
                sb.append( "</ogc:PropertyIsEqualTo>" );
            }
            if ( formats.length > 1 ) {
                sb.append( "</ogc:Or>" );
            }
        }

        // resolution
        String resolution = (String) constraintMap.get( "resolution" );
        if ( resolution != null ) {
            operands++;
            sb.append( "<ogc:PropertyIsGreaterThanOrEqualTo>" );
            sb.append( "<ogc:PropertyName>resolution</ogc:PropertyName>" );
            sb.append( "<ogc:Literal>" ).append( resolution ).append( "</ogc:Literal>" );
            sb.append( "</ogc:PropertyIsGreaterThanOrEqualTo>" );
        }

        // width
        String width = (String) constraintMap.get( "width" );
        if ( width != null ) {
            operands++;
            sb.append( "<ogc:PropertyIsLessThanOrEqualTo>" );
            sb.append( "<ogc:PropertyName>width</ogc:PropertyName>" );
            sb.append( "<ogc:Literal>" ).append( width ).append( "</ogc:Literal>" );
            sb.append( "</ogc:PropertyIsLessThanOrEqualTo>" );
        }

        // height
        String height = (String) constraintMap.get( "height" );
        if ( height != null ) {
            operands++;
            sb.append( "<ogc:PropertyIsLessThanOrEqualTo>" );
            sb.append( "<ogc:PropertyName>height</ogc:PropertyName>" );
            sb.append( "<ogc:Literal>" ).append( height ).append( "</ogc:Literal>" );
            sb.append( "</ogc:PropertyIsLessThanOrEqualTo>" );
        }

        // exceptions
        String[] exceptions = (String[]) constraintMap.get( "exceptions" );
        if ( exceptions != null && exceptions.length > 0 ) {
            operands++;
            if ( exceptions.length > 1 ) {
                sb.append( "<ogc:Or>" );
            }
            for ( int i = 0; i < exceptions.length; i++ ) {
                sb.append( "<ogc:PropertyIsEqualTo>" );
                sb.append( "<ogc:PropertyName>exceptions</ogc:PropertyName>" );
                sb.append( "<ogc:Literal><![CDATA[" ).append( exceptions[i] );
                sb.append( "]]></ogc:Literal>" );
                sb.append( "</ogc:PropertyIsEqualTo>" );
            }
            if ( exceptions.length > 1 ) {
                sb.append( "</ogc:Or>" );
            }
        }

        if ( operands == 0 ) {
            return null;
        } else if ( operands >= 2 ) {
            StringBuffer tmp = new StringBuffer( 500 );
            tmp.append( "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" " );
            tmp.append( "xmlns:gml=\"http://www.opengis.net/gml\">" );
            tmp.append( "<ogc:And>" ).append( sb ).append( "</ogc:And></ogc:Filter>" );
            sb = tmp;
        } else {
            StringBuffer tmp = new StringBuffer();
            tmp.append( "<ogc:Filter xmlns:ogc=\"http://www.opengis.net/ogc\" " );
            tmp.append( "xmlns:gml=\"http://www.opengis.net/gml\">" );
            tmp.append( sb ).append( "</ogc:Filter>" );
            sb = tmp;
        }
        return sb.toString();
    }

    /**
     * private class for temporary storing rights enabled on a featureType
     * 
     * 
     * @version $Revision$
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
     * @author last edited by: $Author$
     * 
     * @version 1.0. $Revision$, $Date$
     * 
     * @since 2.0
     */
    private class FeatureTypeRight {

        /**
         *
         */
        public int id = 0;

        /**
         *
         */
        public boolean access = true;

        /**
         *
         */
        public boolean delete = true;

        /**
         *
         */
        public boolean insert = true;

        /**
         *
         */
        public boolean update = true;

        /**
         * 
         * @param id
         * @param access
         * @param insert
         * @param update
         * @param delete
         */
        FeatureTypeRight( int id, boolean access, boolean insert, boolean update, boolean delete ) {
            this.id = id;
            this.access = access;
            this.insert = insert;
            this.update = update;
            this.delete = delete;
        }

    }

}
