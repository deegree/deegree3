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
package org.deegree.enterprise.control;

import java.io.Reader;
import java.util.Date;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Factory for creating RPC methodCall and methodResponse objects from their XML representation.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$ $Date$
 */
public class RPCFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( RPCFactory.class );

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * Creates an instance of <code>RPCMethodCall</code> from an XML document that can be accessed through the passed
     * <code>Reader</code>.
     *
     * @param reader
     *            reader to access an XML document
     * @return an RPCMethodCall
     * @throws RPCException
     */
    public static RPCMethodCall createRPCMethodCall( Reader reader )
                            throws RPCException {

        Document doc = null;
        try {
            doc = XMLTools.parse( reader );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RPCException( e.toString() );
        }

        return createRPCMethodCall( doc );
    }

    /**
     * Creates an instance of <code>RPCMethodCall</code> from the given XML document.
     *
     * @param doc
     *            XML document containing a RPC method call
     * @return an RPCMethodCall
     * @throws RPCException
     */
    public static RPCMethodCall createRPCMethodCall( Document doc )
                            throws RPCException {

        RPCMethodCall mc = null;
        try {
            Element root = doc.getDocumentElement();
            // get methode name - mandatory
            String methodName = XMLTools.getRequiredStringValue( "methodName", null, root );

            Element params = XMLTools.getChildElement( "params", null, root );

            RPCParameter[] parameters = null;
            if ( params != null ) {
                ElementList el = XMLTools.getChildElements( params );
                if ( el != null ) {
                    parameters = new RPCParameter[el.getLength()];
                    for ( int i = 0; i < el.getLength(); i++ ) {
                        parameters[i] = createRPCParam( el.item( i ) );
                    }
                }
            } else {
                parameters = new RPCParameter[0];
            }

            mc = new RPCMethodCall( methodName, parameters );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RPCException( e.toString() );
        }

        return mc;
    }

    /**
     * Creates an instance of <code>RPCMethodResponse</code> from an XML document that can be accessed through the
     * passed <code>Reader</code>.
     *
     * @param reader
     *            reader to access an XML document
     * @return created <code>RPCMethodResponse</code>
     * @throws RPCException
     */
    public static RPCMethodResponse createRPCMethodResponse( Reader reader )
                            throws RPCException {

        Document doc = null;
        try {
            doc = XMLTools.parse( reader );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RPCException( e.toString() );
        }

        return createRPCMethodResponse( doc );
    }

    /**
     * Creates an instance of {@link RPCMethodResponse} from the given XML document.
     *
     * @param doc
     *            XML document containing a RPC method call
     * @return created <code>RPCMethodResponse</code>
     * @throws RPCException
     */
    public static RPCMethodResponse createRPCMethodResponse( Document doc )
                            throws RPCException {

        RPCMethodResponse mc = null;
        try {
            Element root = doc.getDocumentElement();

            Element params = XMLTools.getChildElement( "params", null, root );

            if ( params != null ) {
                ElementList el = XMLTools.getChildElements( params );
                RPCParameter[] parameters = null;
                if ( el != null ) {
                    parameters = new RPCParameter[el.getLength()];
                    for ( int i = 0; i < el.getLength(); i++ ) {
                        parameters[i] = createRPCParam( el.item( i ) );
                    }
                }
                mc = new RPCMethodResponse( parameters );
            } else {
                // a fault is contained instead of the expected result
                Element fault = XMLTools.getChildElement( "fault", null, root );
                RPCFault rpcFault = createRPCFault( fault );
                mc = new RPCMethodResponse( rpcFault );
            }

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RPCException( e.toString() );
        }

        return mc;
    }

    /**
     * Creates an {@link RPCMethodResponse} from the given parameters.
     *
     * @param par
     * @return corresponding <code>RPCMethodResponse</code> object
     */
    public static RPCMethodResponse createRPCMethodResponse( RPCParameter[] par ) {
        RPCMethodResponse mc = null;
        if ( par != null ) {
            RPCParameter[] params = par;
            mc = new RPCMethodResponse( params );
        } else {
            LOG.logError( "Error in RPCFactory.createRPCMethodResponse(): par = null." );
        }
        return mc;
    }

    /**
     * Creates an <code>RPCParameter</code> from its XML representation.
     *
     * @param param
     *            element containing an RPC parameter
     * @return created <code>RPCParameter</code>
     */
    private static RPCParameter createRPCParam( Element param )
                            throws RPCException {

        RPCParameter parameter = null;
        try {
            Element value = XMLTools.getChildElement( "value", null, param );
            Element child = XMLTools.getFirstChildElement( value );
            Object o = null;
            Class cl = null;
            if ( child.getNodeName().equals( "struct" ) ) {
                o = createRPCStruct( child );
                cl = RPCStruct.class;
            } else if ( child.getNodeName().equals( "string" ) ) {
                o = XMLTools.getRequiredStringValue( "string", null, value );
                cl = String.class;
            } else if ( child.getNodeName().equals( "int" ) ) {
                double d = XMLTools.getRequiredNodeAsDouble( value, "./int", nsContext );
                o = new Integer( (int) d );
                cl = Integer.class;
            } else if ( child.getNodeName().equals( "i4" ) ) {
                double d = XMLTools.getRequiredNodeAsDouble( value, "./i4", nsContext );
                o = new Integer( (int) d );
                cl = Integer.class;
            } else if ( child.getNodeName().equals( "double" ) ) {
                double d = XMLTools.getRequiredNodeAsDouble( value, "./double", nsContext );
                o = new Double( d );
                cl = Double.class;
            } else if ( child.getNodeName().equals( "boolean" ) ) {
                o = Boolean.valueOf( child.getFirstChild().getNodeValue().equals( "1" ) );
                cl = Boolean.class;
            } else if ( child.getNodeName().equals( "dateTime.iso8601" ) ) {
                String s = XMLTools.getRequiredStringValue( "dateTime.iso8601", null, value );
                o = TimeTools.createCalendar( s ).getTime();
                cl = Date.class;
            } else if ( child.getNodeName().equals( "base64" ) ) {
            } else if ( child.getNodeName().equals( "array" ) ) {
                o = createArray( child );
                cl = RPCParameter[].class;
            }
            parameter = new RPCParameter( cl, o );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RPCException( e.toString() );
        }

        return parameter;
    }

    /**
     * Creates an RPC structure object from the passed <code>Element</code>.
     *
     * @param struct
     *            element containing an RPC struct
     * @return created <code>RPCStruct</code>
     */
    private static RPCStruct createRPCStruct( Element struct )
                            throws RPCException {

        RPCStruct structure = null;
        try {
            ElementList el = XMLTools.getChildElements( struct );
            RPCMember[] members = null;
            if ( el != null ) {
                members = new RPCMember[el.getLength()];
                for ( int i = 0; i < el.getLength(); i++ ) {
                    members[i] = createRPCMember( el.item( i ) );
                }
            }
            structure = new RPCStruct( members );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RPCException( e.toString() );
        }

        return structure;
    }

    /**
     * Creates an RPC structure member object from the passed <code>Element</code>.
     *
     * @param member
     *            element containing an RPC member
     * @return created <code>RPCMember</code>
     */
    private static RPCMember createRPCMember( Element member )
                            throws RPCException {

        RPCMember mem = null;
        try {
            String name = XMLTools.getRequiredStringValue( "name", null, member );
            Element value = XMLTools.getChildElement( "value", null, member );
            Element child = XMLTools.getFirstChildElement( value );
            Object o = null;
            Class<?> cl = null;
            if ( child.getNodeName().equals( "struct" ) ) {
                o = createRPCStruct( child );
                cl = RPCStruct.class;
            } else if ( child.getNodeName().equals( "string" ) ) {
                o = XMLTools.getRequiredStringValue( "string", null, value );
                cl = String.class;
            } else if ( child.getNodeName().equals( "int" ) ) {
                double d = XMLTools.getRequiredNodeAsDouble( value, "./int", nsContext );
                o = new Integer( (int) d );
                cl = Integer.class;
            } else if ( child.getNodeName().equals( "i4" ) ) {
                double d = XMLTools.getRequiredNodeAsDouble( value, "./i4", nsContext );
                o = new Integer( (int) d );
                cl = Integer.class;
            } else if ( child.getNodeName().equals( "double" ) ) {
                double d = XMLTools.getRequiredNodeAsDouble( value, "./double", nsContext );
                o = new Double( d );
                cl = Double.class;
            } else if ( child.getNodeName().equals( "boolean" ) ) {
                o = Boolean.valueOf( child.getFirstChild().getNodeValue().equals( "1" ) );
                cl = Boolean.class;
            } else if ( child.getNodeName().equals( "dateTime.iso8601" ) ) {
                String s = XMLTools.getRequiredStringValue( "dateTime.iso8601", null, value );
                o = TimeTools.createCalendar( s ).getTime();
                cl = Date.class;
            } else if ( child.getNodeName().equals( "base64" ) ) {
                // TODO
            } else if ( child.getNodeName().equals( "array" ) ) {
                o = createArray( child );
                cl = RPCParameter[].class;
            }
            mem = new RPCMember( cl, o, name );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RPCException( e.toString() );
        }

        return mem;
    }

    /**
     * Creates an array of {@link RPCParameter} objects from the passed <code>Element</code>.
     *
     * @param array
     * @return created array of <code>RPCParameter</code> objects
     */
    private static RPCParameter[] createArray( Element array )
                            throws RPCException {

        RPCParameter[] param = null;
        try {
            Element data = XMLTools.getChildElement( "data", null, array );
            ElementList el = XMLTools.getChildElements( data );
            if ( el != null ) {
                param = new RPCParameter[el.getLength()];
                for ( int i = 0; i < el.getLength(); i++ ) {
                    Element child = XMLTools.getFirstChildElement( el.item( i ) );
                    Object o = null;
                    Class cl = null;
                    if ( child.getNodeName().equals( "struct" ) ) {
                        o = createRPCStruct( child );
                        cl = RPCStruct.class;
                    } else if ( child.getNodeName().equals( "string" ) ) {
                        o = XMLTools.getRequiredStringValue( "string", null, el.item( i ) );
                        cl = String.class;
                    } else if ( child.getNodeName().equals( "int" ) ) {
                        double d = XMLTools.getRequiredNodeAsDouble( el.item( i ), "./int", nsContext );
                        o = new Integer( (int) d );
                        cl = Integer.class;
                    } else if ( child.getNodeName().equals( "i4" ) ) {
                        double d = XMLTools.getRequiredNodeAsDouble( el.item( i ), "./i4", nsContext );
                        o = new Integer( (int) d );
                        cl = Integer.class;
                    } else if ( child.getNodeName().equals( "double" ) ) {
                        double d = XMLTools.getRequiredNodeAsDouble( el.item( i ), "./double", nsContext );
                        o = new Double( d );
                        cl = Double.class;
                    } else if ( child.getNodeName().equals( "boolean" ) ) {
                        o = Boolean.valueOf( child.getFirstChild().getNodeValue().equals( "1" ) );
                        cl = Boolean.class;
                    } else if ( child.getNodeName().equals( "dateTime.iso8601" ) ) {
                        String s = XMLTools.getRequiredStringValue( "dateTime.iso8601", null, el.item( i ) );
                        o = TimeTools.createCalendar( s ).getTime();
                        cl = Date.class;
                    } else if ( child.getNodeName().equals( "base64" ) ) {
                    } else if ( child.getNodeName().equals( "array" ) ) {
                        o = createArray( child );
                        cl = RPCParameter[].class;
                    }
                    param[i] = new RPCParameter( cl, o );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RPCException( e.toString() );
        }

        return param;
    }

    /**
     * Creates an {@link RPCFault} object from the passed <code>Element</code>.
     *
     * @param fault
     *            fault element
     * @return created <code>RPCFault</code> object
     */
    private static RPCFault createRPCFault( Element fault )
                            throws RPCException {

        RPCFault rpcFault = null;
        try {
            Element value = XMLTools.getChildElement( "value", null, fault );
            Element child = XMLTools.getFirstChildElement( value );
            RPCStruct struct = createRPCStruct( child );
            String s1 = null;
            String s2 = null;
            Object o = struct.getMember( "faultCode" ).getValue();
            if ( o != null ) {
                s1 = o.toString();
            }
            o = struct.getMember( "faultString" ).getValue();
            if ( o != null ) {
                s2 = o.toString();
            }
            rpcFault = new RPCFault( s1, s2 );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new RPCException( e.toString() );
        }
        return rpcFault;
    }
}
