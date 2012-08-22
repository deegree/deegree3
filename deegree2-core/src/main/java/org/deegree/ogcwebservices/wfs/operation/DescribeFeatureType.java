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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.KVP2Map;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.WFService;
import org.w3c.dom.Element;

/**
 * Represents a <code>DescribeFeatureType</code> request to a web feature service.
 * <p>
 * The function of the DescribeFeatureType interface is to provide a client the means to request a
 * schema definition of any feature type that a particular WFS can service. The description that is
 * generated will define how a WFS expects a client application to express the state of a feature to
 * be created or the new state of a feature to be updated. The result of a DescribeFeatureType
 * request is an XML document, describing one or more feature types serviced by the WFS.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DescribeFeatureType extends AbstractWFSRequest {

    private static final long serialVersionUID = 4403179045869238426L;

    private static ILogger LOG = LoggerFactory.getLogger( DescribeFeatureType.class );

    private String outputFormat;

    private QualifiedName[] typeNames;

    /**
     * Creates a new <code>DescribeFeatureType</code> instance.
     *
     * @param version
     * @param id
     * @param handle
     * @param outputFormat
     * @param typeNames
     * @param vendorspecificParameter
     */
    DescribeFeatureType( String version, String id, String handle, String outputFormat, QualifiedName[] typeNames,
                         Map<String, String> vendorspecificParameter ) {
        super( version, id, handle, vendorspecificParameter );
        this.outputFormat = outputFormat;
        this.typeNames = typeNames;
    }

    /**
     * Creates a <code>DescribeFeatureType</code> instance from a document that contains the DOM
     * representation of the request.
     *
     * @param id
     * @param root
     *            element that contains the DOM representation of the request
     * @return DescribeFeatureType instance
     * @throws OGCWebServiceException
     */
    public static DescribeFeatureType create( String id, Element root )
                            throws OGCWebServiceException {
        DescribeFeatureTypeDocument doc = new DescribeFeatureTypeDocument();
        doc.setRootElement( root );
        DescribeFeatureType request;
        try {
            request = doc.parse( id );
        } catch ( Exception e ) {
            throw new OGCWebServiceException( "DescribeFeatureType", e.getMessage() );
        }
        return request;
    }

    /**
     * Creates a new <code>DescribeFeatureType</code> instance from the given key-value pair
     * encoded request.
     *
     * @param id
     *            request identifier
     * @param request
     * @return new <code>DescribeFeatureType</code> request
     * @throws InvalidParameterValueException
     * @throws InconsistentRequestException
     * @throws MissingParameterValueException
     */
    public static DescribeFeatureType create( String id, String request )
                            throws InconsistentRequestException, InvalidParameterValueException,
                            MissingParameterValueException {
        Map<String, String> map = KVP2Map.toMap( request );
        map.put( "ID", id );
        return create( map );
    }

    /**
     * Creates a new <code>DescribeFeatureType</code> request from the given map.
     *
     * @param request
     * @return new <code>DescribeFeatureType</code> request
     * @throws InconsistentRequestException
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static DescribeFeatureType create( Map<String, String> request )
                            throws InconsistentRequestException, InvalidParameterValueException,
                            MissingParameterValueException {
        checkServiceParameter( request );
        String version = checkVersionParameter( request );
        boolean is100 = version.equals( "1.0.0" );
        // I guess XMLSCHEMA should be used here for 1.0.0, but what the heck, it's also kind of
        // GML2...
        // the problem is probably inheritance, since the AbstractWFSRequest defines the Strings
        String outputFormat = getParam( "OUTPUTFORMAT", request, is100 ? FORMAT_GML2_WFS100 : FORMAT_GML3 );
        if ( outputFormat.equalsIgnoreCase( "xmlschema" ) ) {
            outputFormat = FORMAT_GML2_WFS100;
        }
        QualifiedName[] typeNames = extractTypeNames( request );

        long l = IDGenerator.getInstance().generateUniqueID();
        String id = Long.toString( l );
        return new DescribeFeatureType( version, id, null, outputFormat, typeNames, request );
    }

    /**
     * Returns the value of the outputFormat attribute.
     * <p>
     * The outputFormat attribute, is used to indicate the schema description language that should
     * be used to describe a feature schema. The only mandated format is XML-Schema denoted by the
     * XMLSCHEMA element; other vendor specific formats specified in the capabilities document are
     * also possible.
     *
     * @return the value of the outputFormat attribute.
     */
    public String getOutputFormat() {
        return this.outputFormat;
    }

    /**
     * Returns the names of the feature types for which the schema is requested.
     * <p>
     *
     * @return the names of the feature types for which the schema is requested.
     */
    public QualifiedName[] getTypeNames() {
        return typeNames;
    }

    /**
     * Adds missing namespaces in the names of requested feature types.
     * <p>
     * If the {@link QualifiedName} of a requested type has a null namespace, the first qualified
     * feature type name of the given {@link WFService} with the same local name is used instead.
     * <p>
     * Note: The method changes this request (the feature type names) and should only be called by
     * the <code>WFSHandler</code> class.
     *
     * @param wfs
     *            {@link WFService} instance that is used for the lookup of proper (qualified)
     *            feature type names
     */
    public void guessMissingNamespaces( WFService wfs ) {
        for ( int i = 0; i < typeNames.length; i++ ) {
            QualifiedName typeName = typeNames[i];
            if ( typeName.getNamespace() == null ) {
                if ( typeName.getLocalName().equals( typeName.getLocalName() ) ) {
                    LOG.logWarning( "Requested feature type name has no namespace information. Guessing namespace for feature type '"
                                    + typeName.getLocalName() + "' (quirks lookup mode)." );
                    for ( QualifiedName ftName : wfs.getMappedFeatureTypes().keySet() ) {
                        if ( ftName.getLocalName().equals( typeName.getLocalName() ) ) {
                            LOG.logWarning( "Using feature type '" + ftName + "'." );
                            typeNames[i] = ftName;
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        String ret = this.getClass().getName() + ":\n";
        ret += ( outputFormat + "\n" );
        if ( typeNames != null ) {
            for ( int i = 0; i < typeNames.length; i++ ) {
                ret += ( typeNames[i] + "\n" );
            }
        }
        return ret;
    }

    @Override
    public String getRequestParameter()
                            throws OGCWebServiceException {
        Map<URI, String> namespaces = new HashMap<URI, String>();
        String typename = "";
        String namespace = "";
        for ( QualifiedName type : typeNames ) {
            if ( typename.length() != 0 ) {
                typename += ',';
            }
            typename += type.getPrefixedName();

            if ( type.getNamespace() != null ) {
                if ( namespaces.get( type.getNamespace() ) != null ) {
                    namespaces.put( type.getNamespace(), type.getPrefix() );
                    if ( namespace.length() != 0 ) {
                        namespace += ',';
                    }
                    namespace += type.getPrefix() + '=' + type.getNamespace();
                }
            }
        }

        String request = "request=DescribeFeatureType&version=" + this.getVersion() + "&typename=" + typename;
        if ( namespace.length() != 0 ) {
            request += "&namespace=xmlns(" + namespace + ')';
        }
        return request;
    }

}
