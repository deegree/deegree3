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

package org.deegree.owscommon_1_1_0;

import static org.deegree.framework.xml.XMLTools.getElement;
import static org.deegree.framework.xml.XMLTools.getElements;
import static org.deegree.framework.xml.XMLTools.getNodeAsString;
import static org.deegree.framework.xml.XMLTools.getNodesAsStringList;
import static org.deegree.framework.xml.XMLTools.getRequiredElement;
import static org.deegree.framework.xml.XMLTools.getRequiredNodeAsString;
import static org.deegree.ogcbase.CommonNamespaces.XLINK_PREFIX;

import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.XMLParsingException;
import org.w3c.dom.Element;

/**
 * <code>OWSCapabilitesDocument</code> parses the ows_1_1_0 Capabilities profile.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public abstract class OWSCommonCapabilitiesDocument extends CommonsDocument {

    /**
     * @return the ServiceIdentification bean or <code>null</code> if ServiceIdentification
     *         element is <code>null</code>;
     * @throws XMLParsingException
     *             if something could not be parsed.
     */
    public ServiceIdentification parseServiceIdentification()
                            throws XMLParsingException {
        Element serviceIdentification = getElement( getRootElement(), PRE_OWS + "ServiceIdentification", nsContext );
        if ( serviceIdentification == null ) {
            return null;
        }
        List<String> title = getNodesAsStringList( serviceIdentification, PRE_OWS + "Title", nsContext );
        List<String> abstracts = getNodesAsStringList( serviceIdentification, PRE_OWS + "Abstract", nsContext );
        List<Element> kws = getElements( serviceIdentification, PRE_OWS + "Keywords", nsContext );
        // Pair<List<keywords>, codetype>
        List<Keywords> keywords = new ArrayList<Keywords>( ( ( kws == null ) ? 0 : kws.size() ) );
        if ( kws != null ) {
            for ( Element keyword : kws ) {
                keywords.add( parseKeywords( keyword ) );
            }
        }
        Pair<String, String> serviceType = new Pair<String, String>(
                                                                     getRequiredNodeAsString( serviceIdentification,
                                                                                              PRE_OWS + "ServiceType",
                                                                                              nsContext ),
                                                                     getNodeAsString(
                                                                                      serviceIdentification,
                                                                                      PRE_OWS
                                                                                                              + "ServiceType/@codeSpace",
                                                                                      nsContext, null ) );
        List<String> serviceTypeVersions = getNodesAsStringList( serviceIdentification, PRE_OWS + "ServiceTypeVersion",
                                                                 nsContext );
        if ( serviceTypeVersions.size() == 0 ) {
            throw new XMLParsingException( PRE_OWS + "ServiceTypeVersion must be present at least once." );
        }
        List<String> profiles = getNodesAsStringList( serviceIdentification, PRE_OWS + "Profile", nsContext );
        String fees = getNodeAsString( serviceIdentification, PRE_OWS + "Fees", nsContext, null );
        List<String> accessConstraints = getNodesAsStringList( serviceIdentification, PRE_OWS + "AccessConstraints",
                                                               nsContext );
        return new ServiceIdentification( title, abstracts, keywords, serviceType, serviceTypeVersions, profiles, fees,
                                          accessConstraints );
    }

    /**
     * @return the ServiceProvider bean or <code>null</code> if the ServiceProvider element is
     *         <code>null</code>;
     * @throws XMLParsingException
     */
    public ServiceProvider parseServiceProvider()
                            throws XMLParsingException {
        Element serviceProvider = getElement( getRootElement(), PRE_OWS + "ServiceProvider", nsContext );
        if ( serviceProvider == null ) {
            return null;
        }
        String providerName = getRequiredNodeAsString( serviceProvider, PRE_OWS + "ProviderName", nsContext );
        String providerSite = getNodeAsString( serviceProvider, PRE_OWS + "ProviderSite/@" + XLINK_PREFIX + ":href",
                                               nsContext, null );
        ServiceContact serviceContact = parseServiceContact( getRequiredElement( serviceProvider, PRE_OWS
                                                                                                  + "ServiceContact",
                                                                                 nsContext ) );
        return new ServiceProvider( providerName, providerSite, serviceContact );
    }

    /**
     * @return the OperationsMetadata bean or <code>null</code> if the OperationsMetadata element
     *         is <code>null</code>;
     * @throws XMLParsingException
     */
    public OperationsMetadata parseOperationsMetadata()
                            throws XMLParsingException {
        Element operationsMetadata = getElement( getRootElement(), PRE_OWS + "OperationsMetadata", nsContext );
        if ( operationsMetadata == null ) {
            return null;
        }
        List<Operation> operations = parseOperations( getElements( operationsMetadata, PRE_OWS + "Operation", nsContext ) );
        List<Element> params = getElements( operationsMetadata, PRE_OWS + "Parameter", nsContext );
        List<DomainType> parameters = new ArrayList<DomainType>();
        if ( params != null && params.size() > 0 ) {
            for ( Element param : params ) {
                DomainType t = parseDomainType( param );
                if ( t != null ) {
                    parameters.add( t );
                }
            }
        }

        List<Element> consts = getElements( operationsMetadata, PRE_OWS + "Constraint", nsContext );
        List<DomainType> constraints = new ArrayList<DomainType>();
        if ( params != null && params.size() > 0 ) {
            for ( Element ce : consts ) {
                DomainType t = parseDomainType( ce );
                if ( t != null ) {
                    constraints.add( t );
                }
            }
        }
        Element extendedCapabilities = getElement( operationsMetadata, PRE_OWS + "ExtendedCapabilities", nsContext );

        return new OperationsMetadata( operations, parameters, constraints, extendedCapabilities );

    }

}
