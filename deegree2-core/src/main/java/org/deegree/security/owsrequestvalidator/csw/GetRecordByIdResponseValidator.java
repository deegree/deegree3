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

package org.deegree.security.owsrequestvalidator.csw;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterConstructionException;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.portal.standard.security.control.ClientHelper;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.model.Right;
import org.deegree.security.drm.model.RightSet;
import org.deegree.security.drm.model.RightType;
import org.deegree.security.drm.model.SecuredObject;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.Request;
import org.deegree.security.owsrequestvalidator.Policy;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetRecordByIdResponseValidator extends AbstractCSWResponseValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( GetRecordByIdResponseValidator.class );

    private static Map<QualifiedName, Filter> filterMap = new HashMap<QualifiedName, Filter>();

    /**
     *
     * @param policy
     */
    public GetRecordByIdResponseValidator( Policy policy ) {
        super( policy );
    }

    @Override
    public byte[] validateResponse( String service, byte[] response, String mime, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        Request req = policy.getRequest( service, "GetRecordById" );
        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPostConditions().isAny()) {
            return response;
        }

        Condition conditions = req.getPostConditions();

        if ( MimeTypeMapper.isKnownOGCType( mime ) ) {
            // if the mime-type isn't an image type but a known
            // OGC mime-type it must be an XML document.
            // probably it is an exception but it also could be
            // a GML document
            try {
                response = validateXML( response, conditions, user );
            } catch ( XMLException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            } catch ( SAXException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            } catch ( XMLParsingException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            }
        } else if ( mime.equals( "text/xml" ) ) {
            // if the mime-type isn't an image type but 'text/xml'
            // it could be an exception
            try {
                response = validateXML( response, conditions, user );
            } catch ( XMLException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            } catch ( SAXException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            } catch ( XMLParsingException e ) {
                LOG.logError( e.getMessage(), e );
                throw new InvalidParameterValueException( e.getMessage(), e );
            }
        } else {
            throw new InvalidParameterValueException( UNKNOWNMIMETYPE + mime );
        }

        return response;
    }

    private byte[] validateXML( byte[] response, Condition postConditions, User user )
                            throws XMLException, SAXException, IOException, InvalidParameterValueException,
                            UnauthorizedException, XMLParsingException {

        ByteArrayInputStream bis = new ByteArrayInputStream( response );
        XMLFragment xml = new XMLFragment();
        xml.load( bis, XMLFragment.DEFAULT_URL );
        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

        List<Node> nodes = XMLTools.getNodes( xml.getRootElement(), "*", nsc );
        for ( Node node : nodes ) {
            String ln = node.getLocalName();
            String nsu = node.getNamespaceURI();
            QualifiedName elementName = null;
            try {
                if ( nsu != null ) {
                    elementName = new QualifiedName( null, ln, new URI( nsu ) );
                } else {
                    elementName = new QualifiedName( ln );
                }
            } catch ( URISyntaxException e ) {
                // never happens
            }
            LOG.logDebug( "checking element: " + elementName.getFormattedString() );
            if ( postConditions.getOperationParameter( "instanceFilter" ) != null ) {
                Map<QualifiedName, Filter> localFilterMap;
                if ( postConditions.getOperationParameter( "instanceFilter" ).isUserCoupled() ) {
                    // read filterMap from constraints defined in deegree DRM
                    localFilterMap = readFilterFromDRM( user, elementName );
                } else {
                    fillFilterMap( postConditions );
                    // use filterMap read from policy document
                    localFilterMap = filterMap;
                }
                ComplexFilter filter = (ComplexFilter) localFilterMap.get( elementName );
                if ( filter == null ) {
                    throw new UnauthorizedException( Messages.getMessage( "OWSPROXY_GETRECBYID_NOT_ALLOWED" ) );
                }

                // check if returned dataset is valid against the instance filter. If not,
                // thrown an UnauthorizedException
                Operation op = filter.getOperation();
                if ( op instanceof LogicalOperation ) {
                    LogicalOperation lo = (LogicalOperation) op;
                    if ( lo.getOperatorId() == OperationDefines.AND ) {
                        handleAnd( xml, lo );
                    } else if ( lo.getOperatorId() == OperationDefines.OR ) {
                        handleOr( xml, lo );
                    } else {
                        throw new InvalidParameterValueException(
                                                                  Messages.getMessage( "OWSPROXY_GETRECBYID_INVALID_LOGICAL_OPERATOR" ) );
                    }
                } else {
                    Literal literal = (Literal) ( (PropertyIsCOMPOperation) op ).getSecondExpression();
                    String xpath = literal.getValue();
                    LOG.logDebug( "evaluated xpath expression: " + xpath );
                    List<Node> list = XMLTools.getNodes( xml.getRootElement(), xpath, nsc );
                    if ( list == null || list.size() == 0 ) {
                        // if the XPath do not return a result the user is not authorized
                        throw new UnauthorizedException( Messages.getMessage( "OWSPROXY_GETRECBYID_NOT_ALLOWED" ) );
                    }
                }

            }
        }

        return response;
    }

    /**
     * checks if a passed XML matches the XPath conditions contained in the passed OR operation. If
     * at least one XPath matches the user is authoried to see the XML
     *
     * @param xml
     * @param lo
     * @throws UnauthorizedException
     * @throws XMLParsingException
     */
    private void handleOr( XMLFragment xml, LogicalOperation lo )
                            throws UnauthorizedException, XMLParsingException {

        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
        // It is assumed that an XPath is contained within the operations literal (second
        // expression).
        List<Operation> ops = lo.getArguments();
        for ( Operation operation : ops ) {
            Literal literal = (Literal) ( (PropertyIsCOMPOperation) operation ).getSecondExpression();
            String xpath = literal.getValue();
            LOG.logDebug( "evaluated xpath expression: " + xpath );
            // check if the XML document matches the XPath. If none of xpath returns a result
            // the user is not authorized to see this dataset
            List<Node> list = XMLTools.getNodes( xml.getRootElement(), xpath, nsc );
            if ( list != null && list.size() > 0 ) {
                // at least one xpath returned more than nothing so the user is authorized
                return;
            }
        }
        throw new UnauthorizedException( Messages.getMessage( "OWSPROXY_GETRECBYID_NOT_ALLOWED" ) );
    }

    /**
     * checks if a passed XML matches the XPath conditions contained in the passed AND operation.
     * Just If all XPaths matches the user is authoried to see the XML
     *
     * @param xml
     * @param lo
     * @throws XMLParsingException
     * @throws UnauthorizedException
     */
    private void handleAnd( XMLFragment xml, LogicalOperation lo )
                            throws XMLParsingException, UnauthorizedException {

        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
        // It is assumed that an XPath is contained within the operations literal (second
        // expression).
        List<Operation> ops = lo.getArguments();
        for ( Operation operation : ops ) {
            Literal literal = (Literal) ( (PropertyIsCOMPOperation) operation ).getSecondExpression();
            String xpath = literal.getValue();
            LOG.logDebug( "evaluated xpath expression: " + xpath );
            // check if the XML document matches the XPath. If at least one of xpath returns no
            // result
            // the user is not authorized to see this dataset
            List<Node> list = XMLTools.getNodes( xml.getRootElement(), xpath, nsc );
            if ( list == null || list.size() == 0 ) {
                // if at least one XPath do not return a result the user is not authorized
                throw new UnauthorizedException( Messages.getMessage( "OWSPROXY_CSW_GETRECBYID_NOT_ALLOWED" ) );
            }
        }

    }

    /**
     *
     * @param postConditions
     */
    private void fillFilterMap( Condition postConditions ) {
        // TODO Auto-generated method stub
    }

    private Map<QualifiedName, Filter> readFilterFromDRM( User user, QualifiedName elementName )
                            throws UnauthorizedException, InvalidParameterValueException {

        Map<QualifiedName, Filter> map = new HashMap<QualifiedName, Filter>();
        try {
            SecurityAccessManager sam = SecurityAccessManager.getInstance();
            SecurityAccess access = sam.acquireAccess( user );
            String entity = elementName.getFormattedString();
            SecuredObject secObj = access.getSecuredObjectByName( entity, ClientHelper.TYPE_METADATASCHEMA );

            RightSet rs = user.getRights( access, secObj );
            Right right = rs.getRight( secObj, RightType.GETRECORDBYID_RESPONSE );
            // a constraint - if available - is constructed as a OGC Filter
            // one of the filter operations may is 'PropertyIsEqualTo' and
            // defines a ProperyName == 'instanceFilter'. The Literal of this
            // operation itself is a complete and valid Filter expression.
            if ( right != null ) {
                ComplexFilter filter = (ComplexFilter) right.getConstraints();
                if ( filter != null ) {
                    List<ComplexFilter> foundFilters = new ArrayList<ComplexFilter>();
                    // extract filter expression to be used as additional
                    // filter for a GetFeature request
                    extractInstanceFilter( filter.getOperation(), foundFilters );
                    if ( foundFilters.size() == 1 ) {
                        filter = foundFilters.get( 0 );
                    } else if ( foundFilters.size() > 1 ) {
                        List<org.deegree.model.filterencoding.Operation> list = new ArrayList<org.deegree.model.filterencoding.Operation>();
                        for ( ComplexFilter cf : foundFilters ) {
                            list.add( cf.getOperation() );
                        }
                        LogicalOperation lo = new LogicalOperation( OperationDefines.OR, list );
                        filter = new ComplexFilter( lo );
                    }
                    map.put( elementName, filter );
//                    if ( filter != null ) {
//                        map.put( elementName, filter );
//                    }
                }
            }
        } catch ( GeneralSecurityException e ) {
            LOG.logError( e.getMessage(), e );
            throw new UnauthorizedException( e.getMessage(), e );
        } catch ( FilterConstructionException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        } catch ( SAXException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }

        return map;
    }

}
