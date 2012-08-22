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

import org.deegree.datatypes.QualifiedName;

/**
 * Encapsulated data: Statement elements
 *
 * Namespace: http://urn:oasis:names:tc.SAML:1.0:assertion
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class Statement {

    private Subject subject = null;

    private URI authenticationMethod = null;

    private Date authenticationInstant = null;

    private String ip = null;

    private String dns = null;

    private QualifiedName kind = null;

    private URI location = null;

    private URI binding = null;

    private ArrayList<String> actions = null;

    private ArrayList<URI> actionNamespaces = null;

    private ArrayList<Assertion> assertions = null;

    private String[] assertionIDs = null;

    private URI resource = null;

    private String decision = null;

    private ArrayList<String> attributeNames = null;

    private ArrayList<URI> attributeNamespaces = null;

    private ArrayList<String[]> attributeValues = null;

    /**
     * @param subject
     * @param authenticationMethod
     * @param authenticationInstant
     */
    public Statement( Subject subject, URI authenticationMethod, Date authenticationInstant ) {
        this.subject = subject;
        this.authenticationMethod = authenticationMethod;
        this.authenticationInstant = authenticationInstant;
    }

    /**
     * @param subject
     * @param actions
     * @param actionNamespaces
     * @param assertions
     * @param assertionIDs
     * @param resource
     * @param decision
     */
    public Statement( Subject subject, ArrayList<String> actions, ArrayList<URI> actionNamespaces,
                     ArrayList<Assertion> assertions, String[] assertionIDs, URI resource,
                     String decision ) {
        this.subject = subject;
        this.actions = actions;
        this.actionNamespaces = actionNamespaces;
        this.assertions = assertions;
        this.assertionIDs = assertionIDs;
        this.resource = resource;
        this.decision = decision;
    }

    /**
     * @param subject
     * @param attributeNames
     * @param attributeNamespaces
     * @param attributeValues
     */
    public Statement( Subject subject, ArrayList<String> attributeNames,
                     ArrayList<URI> attributeNamespaces, ArrayList<String[]> attributeValues ) {
        this.subject = subject;
        this.attributeNames = attributeNames;
        this.attributeNamespaces = attributeNamespaces;
        this.attributeValues = attributeValues;
    }

    /**
     * @return true, if the encapsulated data is an AuthenticationStatement
     */
    public boolean isAuthenticationStatement() {
        return ( authenticationMethod != null ) && ( authenticationInstant != null );
    }

    /**
     * @return true, if the encapsulated data is an AuthorizationDecisionStatement
     */
    public boolean isAuthorizationDecisionStatement() {
        return ( actions != null ) && ( actionNamespaces != null ) && ( assertions != null )
               && ( assertionIDs != null ) && ( resource != null ) && ( decision != null );
    }

    /**
     * @return true, if the encapsulated data is an AttributeStatement
     */
    public boolean isAttributeStatement() {
        return ( attributeNames != null ) && ( attributeNamespaces != null )
               && ( attributeValues != null );
    }

    /**
     * @param ip
     */
    public void setIP( String ip ) {
        this.ip = ip;
    }

    /**
     * @param dns
     */
    public void setDNS( String dns ) {
        this.dns = dns;
    }

    /**
     * @param kind
     * @param location
     * @param binding
     */
    public void setAuthorityBinding( QualifiedName kind, URI location, URI binding ) {
        this.kind = kind;
        this.location = location;
        this.binding = binding;
    }

    /**
     * @return Returns the actionNamespaces.
     */
    public ArrayList<URI> getActionNamespaces() {
        return actionNamespaces;
    }

    /**
     * @return Returns the actions.
     */
    public ArrayList<String> getActions() {
        return actions;
    }

    /**
     * @return Returns the assertionIDs.
     */
    public String[] getAssertionIDs() {
        return assertionIDs;
    }

    /**
     * @return Returns the assertions.
     */
    public ArrayList<Assertion> getAssertions() {
        return assertions;
    }

    /**
     * @return Returns the attributeNames.
     */
    public ArrayList<String> getAttributeNames() {
        return attributeNames;
    }

    /**
     * @return Returns the attributeNamespaces.
     */
    public ArrayList<URI> getAttributeNamespaces() {
        return attributeNamespaces;
    }

    /**
     * @return Returns the attributeValues.
     */
    public ArrayList<String[]> getAttributeValues() {
        return attributeValues;
    }

    /**
     * @return Returns the authenticationInstant.
     */
    public Date getAuthenticationInstant() {
        return authenticationInstant;
    }

    /**
     * @return Returns the authenticationMethod.
     */
    public URI getAuthenticationMethod() {
        return authenticationMethod;
    }

    /**
     * @return Returns the binding.
     */
    public URI getBinding() {
        return binding;
    }

    /**
     * @return Returns the decision.
     */
    public String getDecision() {
        return decision;
    }

    /**
     * @return Returns the dns.
     */
    public String getDns() {
        return dns;
    }

    /**
     * @return Returns the ip.
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return Returns the kind.
     */
    public QualifiedName getKind() {
        return kind;
    }

    /**
     * @return Returns the location.
     */
    public URI getLocation() {
        return location;
    }

    /**
     * @return Returns the resource.
     */
    public URI getResource() {
        return resource;
    }

    /**
     * @return Returns the subject.
     */
    public Subject getSubject() {
        return subject;
    }

}
