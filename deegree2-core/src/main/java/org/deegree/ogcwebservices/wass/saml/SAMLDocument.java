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

package org.deegree.ogcwebservices.wass.saml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parser class for the SAML elements.
 *
 * Namespace: http://urn:oasis:names:tc.SAML:1.0:assertion
 *
 * The classes in this package are INCOMPLETE and UNTESTED.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class SAMLDocument extends XMLFragment {

    private DatatypeFactory datatypeFactory;

    private static final long serialVersionUID = -1020160309145902779L;

    private static final String PRE = "saml:";

    /**
     * @throws DatatypeConfigurationException
     */
    public SAMLDocument() throws DatatypeConfigurationException {

        datatypeFactory = DatatypeFactory.newInstance();

    }

    /**
     * @param val
     * @return the decision
     * @throws XMLParsingException
     */
    private String parseDecision( String val )
                            throws XMLParsingException {

        if ( val.equals( "Permit" ) || val.equals( "Deny" ) || val.equals( "Indeterminate" ) ) {

            return val;
        }
        throw new XMLParsingException( "The value '" + val + "' is not allowed here." );
    }

    private URI parseAudience( Element root )
                            throws XMLParsingException {
        return XMLTools.getNodeAsURI( root, PRE + "Audience", nsContext, null );
    }

    private Conditions parseConditions( Element elem )
                            throws XMLParsingException {
        Element root = (Element) XMLTools.getNode( elem, PRE + "Conditions", nsContext );

        ArrayList<Condition> conditions = new ArrayList<Condition>();

        List<Node> audiences = XMLTools.getNodes( root, PRE + "AudienceRestrictionCondition", nsContext );
        for ( Object audience : audiences ) {
            conditions.add( new Condition( parseAudience( (Element) audience ) ) );
        }

        // seems strange that there can be an unlimited number of these conditions, but anyway...
        List<Node> caches = XMLTools.getNodes( root, PRE + "DoNotCacheCondition", nsContext );
        if ( caches.size() != 0 )
            conditions.add( new Condition( true ) );

        String notBeforeString = XMLTools.getAttrValue( root, null, "NotBefore", null );
        Date notBefore = null;
        if ( notBeforeString != null ) {
            notBefore = datatypeFactory.newXMLGregorianCalendar( notBeforeString ).toGregorianCalendar().getTime();
        }
        String notOnOrAfterString = XMLTools.getAttrValue( root, null, "NotOnOrAfter", null );
        Date notOnOrAfter = null;
        if ( notOnOrAfterString != null ) {
            notOnOrAfter = datatypeFactory.newXMLGregorianCalendar( notOnOrAfterString ).toGregorianCalendar().getTime();
        }

        return new Conditions( conditions, notBefore, notOnOrAfter );
    }

    private Subject parseSubject( Element elem )
                            throws XMLParsingException {

        Element root = (Element) XMLTools.getNode( elem, PRE + "Subject", nsContext );

        // parse name identifier, if any
        Element nameIdentifier = (Element) XMLTools.getNode( root, PRE + "NameIdentifier", nsContext );
        String name = null;
        String nameQualifier = null;
        URI format = null;
        if ( nameIdentifier != null ) {
            name = nameIdentifier.getNodeValue();
            nameQualifier = XMLTools.getAttrValue( nameIdentifier, null, PRE + "NameQualifier", null );
            format = XMLTools.getNodeAsURI( nameIdentifier, "@Format", nsContext, null );
        }

        URI[] confirmationMethods = null;
        String subjectConfirmationData = null;
        // ds:KeyInfo must be parsed as well TODO FIXME LOOKATME

        Element subjectConfirmation = (Element) XMLTools.getNode( root, PRE + "SubjectConfirmation", nsContext );

        if ( subjectConfirmation != null ) {
            confirmationMethods = XMLTools.getNodesAsURIs( subjectConfirmation, PRE + "ConfirmationMethod", nsContext );
            subjectConfirmationData = XMLTools.getNodeAsString( subjectConfirmation, PRE + "SubjectConfirmation",
                                                                nsContext, null );
        }

        if ( name == null ) {
            if ( ( confirmationMethods == null ) || ( confirmationMethods.length == 0 ) )
                throw new XMLParsingException( "Invalid content of the saml:Subject element." );

            return new Subject( confirmationMethods, subjectConfirmationData );
        }

        return new Subject( name, nameQualifier, format, confirmationMethods, subjectConfirmationData );
    }

    private Statement parseAuthenticationStatement( Element root )
                            throws XMLParsingException {

        Subject subject = parseSubject( root );

        Element locality = (Element) XMLTools.getNode( root, PRE + "SubjectLocality", nsContext );
        String ip = null;
        String dns = null;
        if ( locality != null ) {
            ip = XMLTools.getNodeAsString( locality, "@IPAddress", nsContext, null );
            dns = XMLTools.getNodeAsString( locality, "@DNSAddress", nsContext, null );
        }

        Element authorityBinding = (Element) XMLTools.getNode( root, PRE + "AuthorityBinding", nsContext );
        QualifiedName kind = null;
        URI location = null;
        URI binding = null;
        if ( authorityBinding != null ) {
            kind = XMLTools.getRequiredNodeAsQualifiedName( authorityBinding, "@AuthorityKind", nsContext );
            location = XMLTools.getRequiredNodeAsURI( authorityBinding, "@Location", nsContext );
            binding = XMLTools.getRequiredNodeAsURI( authorityBinding, "@Binding", nsContext );
        }

        URI authenticationMethod = XMLTools.getRequiredNodeAsURI( root, "@AuthenticationMethod", nsContext );
        Date authenticationInstant = datatypeFactory.newXMLGregorianCalendar(
                                                                              XMLTools.getRequiredNodeAsString(
                                                                                                                root,
                                                                                                                "@AuthenticationInstant",
                                                                                                                nsContext ) ).toGregorianCalendar().getTime();

        Statement statement = new Statement( subject, authenticationMethod, authenticationInstant );
        if ( ip != null )
            statement.setIP( ip );
        if ( dns != null )
            statement.setDNS( dns );

        if ( ( kind != null ) && ( ( location == null ) || ( binding == null ) ) )
            throw new XMLParsingException( "An saml:AuthorityBinding element requires all of its attributes." );
        if ( kind != null )
            statement.setAuthorityBinding( kind, location, binding );


        return statement;
    }

    private Statement parseAuthorizationDecisionStatement( Element root )
                            throws XMLParsingException {

        Subject subject = parseSubject( root );

        List<Node> actionNodes = XMLTools.getRequiredNodes( root, PRE + "Action", nsContext );
        ArrayList<String> actions = new ArrayList<String>();
        ArrayList<URI> actionNamespaces = new ArrayList<URI>();

        for ( Object node : actionNodes ) {
            actions.add( ( (Element) node ).getNodeValue() );
            actionNamespaces.add( XMLTools.getNodeAsURI( (Element) node, "@Namespace", nsContext, null ) );
        }

        Element evidence = (Element) XMLTools.getNode( root, PRE + "Evidence", nsContext );
        List<Node> assertionNodes = XMLTools.getNodes( evidence, PRE + "Assertion", nsContext );
        ArrayList<Assertion> assertions = new ArrayList<Assertion>();
        for ( Object node : assertionNodes ) {
            assertions.add( parseAssertion( (Element) node ) );
        }
        String[] assertionIDs = XMLTools.getNodesAsStrings( evidence, PRE + "AssertionIDReference", nsContext );

        URI resource = XMLTools.getRequiredNodeAsURI( root, "@Resource", nsContext );
        String decision = parseDecision( XMLTools.getRequiredNodeAsString( root, "@Decision", nsContext ) );


        return new Statement( subject, actions, actionNamespaces, assertions, assertionIDs, resource, decision );
    }

    private Statement parseAttributeStatement( Element root )
                            throws XMLParsingException {

        Subject subject = parseSubject( root );

        List<Element> attributes = XMLTools.getRequiredElements( root, PRE + "Attribute", nsContext );

        ArrayList<String> attributeNames = new ArrayList<String>();
        ArrayList<URI> attributeNamespaces = new ArrayList<URI>();
        ArrayList<String[]> attributeValues = new ArrayList<String[]>();

        for ( Element node : attributes ) {
            attributeNames.add( XMLTools.getRequiredNodeAsString( node, "@AttributeName", nsContext ) );
            attributeNamespaces.add( XMLTools.getRequiredNodeAsURI( node, "@AttributeNamespace", nsContext ) );
            attributeValues.add( XMLTools.getRequiredNodesAsStrings( node, PRE + "AttributeValue", nsContext ) );
        }


        return new Statement( subject, attributeNames, attributeNamespaces, attributeValues );
    }

    private Assertion parseAssertion( Element root )
                            throws XMLParsingException {

        Element node = (Element) XMLTools.getNode( root, PRE + "Conditions", nsContext );
        Conditions conditions = null;
        if ( node != null )
            conditions = parseConditions( node );

        node = (Element) XMLTools.getNode( root, PRE + "Advice", nsContext );

        ArrayList<Assertion> advices = new ArrayList<Assertion>();
        List<Node> assertionNodes = XMLTools.getNodes( node, PRE + "Assertion", nsContext );
        for ( Object elem : assertionNodes ) {
            advices.add( parseAssertion( (Element) elem ) );
        }
        String[] adviceIDs = XMLTools.getNodesAsStrings( node, PRE + "AssertionIDReference", nsContext );

        // other stuff is not processed

        ArrayList<Statement> statements = new ArrayList<Statement>();

        List<Node> authenticationStatements = XMLTools.getNodes( root, PRE + "AuthenticationStatement", nsContext );
        for ( Object elem : authenticationStatements ) {
            statements.add( parseAuthenticationStatement( (Element) elem ) );
        }

        List<Node> authorizationDecisionStatements = XMLTools.getNodes( root, PRE + "AuthorizationDecisionStatement",
                                                                  nsContext );
        for ( Object elem : authorizationDecisionStatements ) {
            statements.add( parseAuthorizationDecisionStatement( (Element) elem ) );
        }

        List<Node> attributeStatements = XMLTools.getNodes( root, PRE + "AttributeStatement", nsContext );
        for ( Object elem : attributeStatements ) {
            statements.add( parseAttributeStatement( (Element) elem ) );
        }

        if ( statements.size() == 0 )
            throw new XMLParsingException( "You must choose at least one Statement element." );

        // parse signature from ds namespace
        int majorVersion = Integer.parseInt( XMLTools.getRequiredNodeAsString( root, "@MajorVersion", nsContext ) );
        int minorVersion = Integer.parseInt( XMLTools.getRequiredNodeAsString( root, "@MinorVersion", nsContext ) );
        String assertionID = XMLTools.getRequiredNodeAsString( root, "@AssertionID", nsContext );
        String issuer = XMLTools.getRequiredNodeAsString( root, "@Issuer", nsContext );
        String issueInstantString = XMLTools.getRequiredNodeAsString( root, "@IssueInstant", nsContext );
        Date issueInstant = datatypeFactory.newXMLGregorianCalendar( issueInstantString ).toGregorianCalendar().getTime();


        return new Assertion( conditions, advices, adviceIDs, statements, majorVersion, minorVersion, assertionID,
                              issuer, issueInstant );
    }

}
