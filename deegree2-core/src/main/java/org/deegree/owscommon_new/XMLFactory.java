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
package org.deegree.owscommon_new;

import java.net.URI;
import java.util.List;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.CitedResponsibleParty;
import org.deegree.model.metadata.iso19115.Constraints;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;

/**
 * <code>XMLFactory</code> for generation of Capabilities XML documents according to the OWS
 * common specification 1.0.0.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class XMLFactory extends org.deegree.ogcbase.XMLFactory {

    private static URI OWS = CommonNamespaces.OWSNS;

    private static String POWS = CommonNamespaces.OWS_PREFIX + ":";

    /**
     * Exports the given capabilities as XML.
     *
     * @param root
     *            the root capabilities element according to the specification of the service
     * @param caps
     */
    public static void appendBaseCapabilities( Element root, OWSCommonCapabilities caps ) {
        ServiceIdentification identification = caps.getServiceIdentification();
        if ( identification != null )
            appendServiceIdentification( root, identification );

        ServiceProvider provider = caps.getServiceProvider();
        if ( provider != null )
            appendServiceProvider( root, provider );

        OperationsMetadata metadata = caps.getOperationsMetadata();
        if ( metadata != null )
            appendOperationsMetadata( root, metadata );

        root.setAttribute( "version", caps.getVersion() );

        String updateSequence = caps.getUpdateSequence();
        if ( updateSequence != null )
            root.setAttribute( "updateSequence", updateSequence );
    }

    /**
     * Appends a <code>ServiceIdentification</code> object as XML.
     *
     * @param root
     * @param identification
     */
    public static void appendServiceIdentification( Element root, ServiceIdentification identification ) {
        Element elem = XMLTools.appendElement( root, OWS, POWS + "ServiceIdentification" );

        String title = identification.getTitle();
        if ( title != null )
            XMLTools.appendElement( elem, OWS, POWS + "Title", title );

        String abstractString = identification.getAbstractString();
        if ( abstractString != null )
            XMLTools.appendElement( elem, OWS, POWS + "Abstract", abstractString );

        List<Keywords> keywords = identification.getKeywords();
        for ( Keywords keys : keywords )
            org.deegree.model.metadata.iso19115.XMLFactory.appendKeywords( elem, keys );

        Code serviceType = identification.getServiceType();
        if ( serviceType != null )
            org.deegree.model.metadata.iso19115.XMLFactory.appendCode( elem, "Code", serviceType );

        List<String> versions = identification.getServiceTypeVersions();
        for ( String version : versions )
            XMLTools.appendElement( elem, OWS, POWS + "ServiceTypeVersion", version );

        List<Constraints> accessConstraints = identification.getAccessConstraints();
        if ( accessConstraints.size() > 0 ) {
            // append the first fee data from the constraints, ignore the rest
            String fees = accessConstraints.get( 0 ).getFees();
            XMLTools.appendElement( elem, OWS, POWS + "Fees", fees );

            for ( Constraints constraints : accessConstraints )
                org.deegree.model.metadata.iso19115.XMLFactory.appendAccessConstraint( elem, constraints );
        }
    }

    /**
     * Appends a <code>ServiceProvider</code> object as XML.
     *
     * @param root
     *            where to append
     * @param provider
     *            the object to append
     */
    public static void appendServiceProvider( Element root, ServiceProvider provider ) {
        Element elem = XMLTools.appendElement( root, OWS, POWS + "ServiceProvider" );

        String name = provider.getProviderName();
        if ( name != null )
            XMLTools.appendElement( elem, OWS, POWS + "ProviderName", name );

        OnlineResource resource = provider.getProviderSite();
        if ( resource != null ) {
            Element resElement = XMLTools.appendElement( elem, OWS, POWS + "ProviderSite" );
            org.deegree.model.metadata.iso19115.XMLFactory.appendOnlineResource( resElement, resource );
        }

        CitedResponsibleParty party = provider.getServiceContact();
        org.deegree.model.metadata.iso19115.XMLFactory.appendCitedResponsibleParty( elem, party );
    }

    /**
     * Appends an <code>OperationsMetadata</code> object as XML.
     *
     * @param root
     *            where to append
     * @param data
     *            what to append
     */
    public static void appendOperationsMetadata( Element root, OperationsMetadata data ) {
        Element elem = XMLTools.appendElement( root, OWS, POWS + "OperationsMetadata" );

        List<Operation> operations = data.getOperations();
        for ( Operation operation : operations )
            appendOperation( elem, operation );

        List<Parameter> parameters = data.getParameters();
        for ( Parameter parameter : parameters ) {
            if ( parameter instanceof DomainType )
                appendDomainType( elem, "Parameter", (DomainType) parameter );
        }

        List<DomainType> constraints = data.getConstraints();
        for ( DomainType constraint : constraints )
            appendDomainType( elem, "Constraint", constraint );

        // extended capabilities are ignored
    }

    /**
     * Appends an <code>Operation</code> object as XML.
     *
     * @param root
     *            where to append
     * @param operation
     *            what to append
     */
    public static void appendOperation( Element root, Operation operation ) {
        Element elem = XMLTools.appendElement( root, OWS, POWS + "Operation" );

        List<DCP> dcps = operation.getDCP();
        for ( DCP dcp : dcps )
            appendDCP( elem, dcp );

        List<Parameter> parameters = operation.getParameters();
        for ( Parameter parameter : parameters ) {
            if ( parameter instanceof DomainType )
                appendDomainType( elem, "Parameter", (DomainType) parameter );
        }

        List<DomainType> constraints = operation.getConstraints();
        for ( DomainType constraint : constraints )
            appendDomainType( elem, "Constraint", constraint );

        Object md = operation.getMetadata();
        if ( md instanceof List ) {
            List<?> metadata = (List) md;
            for ( Object obj : metadata ) {
                if ( obj instanceof Metadata )
                    appendMetadata( elem, (Metadata) obj );
            }
        }

        elem.setAttribute( "name", operation.getName().getPrefixedName() );
    }

    /**
     * Appends a <code>DCP</code> object as XML.
     *
     * @param root
     *            where to append
     * @param dcp
     *            what to append
     */
    public static void appendDCP( Element root, DCP dcp ) {
        Element dcpElem = XMLTools.appendElement( root, OWS, POWS + "DCP" );

        if ( dcp instanceof HTTP )
            appendHTTP( dcpElem, (HTTP) dcp );
    }

    /**
     * Appends a <code>HTTP</code> object as XML.
     *
     * @param root
     *            where to append
     * @param http
     *            what to append
     */
    public static void appendHTTP( Element root, HTTP http ) {
        Element elem = XMLTools.appendElement( root, OWS, POWS + "HTTP" );

        List<HTTP.Type> types = http.getTypes();
        List<List<DomainType>> constraints = http.getConstraints();
        List<OnlineResource> links = http.getLinks();

        for ( int i = 0; i < types.size(); ++i ) {
            String type = ( types.get( i ) == HTTP.Type.Get ) ? "Get" : "Post";
            Element getpost = XMLTools.appendElement( elem, OWS, POWS + type );
            org.deegree.model.metadata.iso19115.XMLFactory.appendOnlineResource( getpost, links.get( i ) );
            List<DomainType> constraintList = constraints.get( i );
            for ( DomainType constraint : constraintList )
                appendDomainType( getpost, "Constraint", constraint );
        }
    }

    /**
     * Appends a <code>DomainType</code> object as XML.
     *
     * @param root
     *            where to append
     * @param tagName
     *            under which name to append
     * @param data
     *            what to append
     */
    public static void appendDomainType( Element root, String tagName, DomainType data ) {
        Element elem = XMLTools.appendElement( root, OWS, POWS + tagName );

        List<TypedLiteral> values = data.getValues();
        for ( TypedLiteral value : values )
            XMLTools.appendElement( elem, OWS, POWS + "Value", value.getValue() );

        Object md = data.getMetadata();
        if ( md instanceof List ) {
            List<?> metadata = (List) md;
            for ( Object obj : metadata ) {
                if ( obj instanceof Metadata )
                    appendMetadata( elem, (Metadata) obj );
            }
        }

        elem.setAttribute( "name", data.getName().getPrefixedName() );
    }

    /**
     * Appends a <code>Metadata</code> object as XML.
     *
     * @param root
     *            where to append
     * @param data
     *            what to append
     */
    public static void appendMetadata( Element root, Metadata data ) {
        Element elem = XMLTools.appendElement( root, OWS, POWS + "Metadata" );
        elem.setAttribute( "about", data.getAbout().toASCIIString() );
        appendSimpleLinkAttributes( elem, data.getLink() );
    }

}
