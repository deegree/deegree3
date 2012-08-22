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

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <code>CommonsDocument</code> supplies helper methods for all common ows (version 1.1.0) xml elements.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class CommonsDocument extends XMLFragment {

    private static ILogger LOG = LoggerFactory.getLogger( CommonsDocument.class );

    /**
     *
     */
    private static final long serialVersionUID = -7211342372381557201L;

    /**
     * The ows 1.1 prefix with a ':' (colon) suffix.
     */
    protected static String PRE_OWS = CommonNamespaces.OWS_1_1_0PREFIX + ":";

    /**
     * @param operationElements
     *            to be parsed.
     * @return a list of operations which may be empty but never <code>null</code>
     * @throws XMLParsingException
     */
    protected List<Operation> parseOperations( List<Element> operationElements )
                            throws XMLParsingException {
        if ( operationElements == null || operationElements.size() < 2 ) {
            throw new XMLParsingException( "At least two " + PRE_OWS + "operations must be defined in the " + PRE_OWS
                                           + "OperationMetadata element." );
        }
        List<Operation> operations = new ArrayList<Operation>( operationElements.size() );
        for ( Element op : operationElements ) {
            /**
             * from owsOperationsMetadata<br/> Unordered list of Distributed Computing Platforms (DCPs) supported for
             * this operation. At present, only the HTTP DCP is defined, so this element will appear only once.
             */
            Element dcp = getRequiredElement( op, PRE_OWS + "DCP", nsContext );
            Element http = getRequiredElement( dcp, PRE_OWS + "HTTP", nsContext );
            List<Element> getters = getElements( http, PRE_OWS + "Get", nsContext );
            List<Pair<String, List<DomainType>>> getURLs = new ArrayList<Pair<String, List<DomainType>>>();
            if ( getters != null && getters.size() > 0 ) {
                for ( Element get : getters ) {
                    Pair<String, List<DomainType>> t = parseHTTPChild( get );
                    if ( t != null ) {
                        getURLs.add( t );
                    }
                }
            }

            List<Element> posts = getElements( http, PRE_OWS + "Post", nsContext );
            List<Pair<String, List<DomainType>>> postURLs = new ArrayList<Pair<String, List<DomainType>>>();
            if ( posts != null && posts.size() > 0 ) {
                for ( Element post : posts ) {
                    Pair<String, List<DomainType>> t = parseHTTPChild( post );
                    if ( t != null ) {
                        postURLs.add( t );
                    }
                }
            }

            List<Element> params = getElements( op, PRE_OWS + "Parameter", nsContext );
            List<DomainType> parameters = new ArrayList<DomainType>();
            if ( params != null && params.size() > 0 ) {
                for ( Element param : params ) {
                    DomainType t = parseDomainType( param );
                    if ( t != null ) {
                        parameters.add( t );
                    }
                }
            }

            List<Element> consts = getElements( op, PRE_OWS + "Constraint", nsContext );
            List<DomainType> constraints = new ArrayList<DomainType>();
            if ( params != null && params.size() > 0 ) {
                for ( Element ce : consts ) {
                    DomainType t = parseDomainType( ce );
                    if ( t != null ) {
                        constraints.add( t );
                    }
                }
            }
            List<Metadata> metadataAttribs = parseMetadatas( getElements( op, PRE_OWS + "MetaData", nsContext ) );
            String name = getRequiredNodeAsString( op, "@name", nsContext );
            operations.add( new Operation( getURLs, postURLs, parameters, constraints, metadataAttribs, name ) );
        }
        return operations;
    }

    /**
     * parses the post or get information beneath a http element.
     *
     * @param getOrPost
     * @return the pair containing the xlink, list&lt;contraint&gt;. or <code>null</code> if attribute and elements
     *         were not found.
     * @throws XMLParsingException
     */

    protected Pair<String, List<DomainType>> parseHTTPChild( Element getOrPost )
                            throws XMLParsingException {
        if ( getOrPost == null ) {
            return null;
        }
        String httpURL = getNodeAsString( getOrPost, "@" + XLINK_PREFIX + ":href", nsContext, null );
        List<DomainType> getConstraints = null;

        List<Element> getConstElements = getElements( getOrPost, PRE_OWS + "Constraint", nsContext );
        if ( getConstElements != null && getConstElements.size() > 0 ) {
            for ( Element gce : getConstElements ) {
                DomainType getConstraint = parseDomainType( gce );
                if ( getConstraint != null ) {
                    // create a list instance.
                    if ( getConstraints == null ) {
                        getConstraints = new ArrayList<DomainType>( getConstElements.size() );
                    }
                    getConstraints.add( getConstraint );
                }
            }
        }
        if ( getConstraints != null || httpURL != null ) {
            return new Pair<String, List<DomainType>>( httpURL, getConstraints );
        }
        return null;
    }

    /**
     * @param domainType
     *            to parse
     * @return a bean representation of the domainType or <code>null</code> if given element is <code>null</code>.
     * @throws XMLParsingException
     */
    protected DomainType parseDomainType( Element domainType )
                            throws XMLParsingException {
        if ( domainType == null ) {
            return null;
        }
        boolean allowedValues = getElement( domainType, PRE_OWS + "AllowedValues", nsContext ) != null;
        List<String> values = null;
        List<Range> ranges = null;
        if ( allowedValues ) {
            values = getNodesAsStringList( domainType, PRE_OWS + "AllowedValues/" + PRE_OWS + "Value", nsContext );
            ranges = parseRanges( getElements( domainType, PRE_OWS + "AllowedValues/" + PRE_OWS + "Range", nsContext ) );
            if ( ( values == null || values.size() == 0 ) && ( ranges == null || ranges.size() == 0 ) ) {
                throw new XMLParsingException( "One of the following values must be defined in an " + PRE_OWS
                                               + "AllowedValues: - " + PRE_OWS + "Values or " + PRE_OWS + "Range" );
            }
        }
        boolean anyValue = getElement( domainType, PRE_OWS + "AnyValue", nsContext ) != null;
        boolean noValues = getElement( domainType, PRE_OWS + "NoValues", nsContext ) != null;
        Pair<String, String> valuesReference = parseDomainMetadataType( getElement( domainType, PRE_OWS
                                                                                                + "ValuesReference",
                                                                                    nsContext ) );
        if ( valuesReference != null && valuesReference.second == null ) {
            throw new XMLParsingException( "The reference attribute of the " + PRE_OWS + "DomatainType/" + PRE_OWS
                                           + "ValuesReference must be set." );
        }
        if ( !( allowedValues || anyValue || noValues || valuesReference != null ) ) {
            throw new XMLParsingException( "One of the following values must be defined in an " + PRE_OWS
                                           + "DomainType: - " + PRE_OWS + "AllowedValues, " + PRE_OWS + "AnyValue, "
                                           + PRE_OWS + "NoValues or " + PRE_OWS + "ValuesReference" );
        }
        String defaultValue = getNodeAsString( domainType, PRE_OWS + "DefaultValue", nsContext, null );
        Pair<String, String> meaning = parseDomainMetadataType( getElement( domainType, PRE_OWS + "Meaning", nsContext ) );
        Pair<String, String> dataType = parseDomainMetadataType( getElement( domainType, PRE_OWS + "DataType",
                                                                             nsContext ) );
        Element valuesUnit = getElement( domainType, PRE_OWS + "ValuesUnit", nsContext );
        Pair<String, String> uom = null;
        Pair<String, String> referenceSystem = null;
        if ( valuesUnit != null ) {
            uom = parseDomainMetadataType( getElement( valuesUnit, PRE_OWS + "UOM", nsContext ) );
            referenceSystem = parseDomainMetadataType( getElement( valuesUnit, PRE_OWS + "ReferenceSystem", nsContext ) );
            if ( uom == null && referenceSystem == null ) {
                throw new XMLParsingException( "Either " + PRE_OWS + "UOM or " + PRE_OWS
                                               + "ReferenceSystem are required in a " + PRE_OWS + "ValuesUnit element." );
            }
        }
        List<Metadata> metadataAttribs = parseMetadatas( getElements( domainType, PRE_OWS + "MetaData", nsContext ) );

        String name = getRequiredNodeAsString( domainType, "@name", nsContext );

        return new DomainType( values, ranges, anyValue, noValues, valuesReference, defaultValue, meaning, dataType,
                               uom, referenceSystem, metadataAttribs, name );

    }

    /**
     * @param metadataElements
     *            to be parsed.
     * @return a List of pairs of optional &lt;xlink:href, about&gt; attributes, or an empty list <code>null</code> if
     *         no elements were found.
     */
    protected List<Metadata> parseMetadatas( List<Element> metadataElements ) {
        if ( metadataElements == null || metadataElements.size() == 0 ) {
            return null;
        }
        List<Metadata> result = new ArrayList<Metadata>( metadataElements.size() );
        for ( Element mde : metadataElements ) {

            try {
                Metadata md = parseMetadata( mde );
                if ( md != null ) {
                    result.add( md );
                }
            } catch ( XMLParsingException e ) {
                LOG.logError( e );
            }

        }
        if ( result.size() == 0 ) {
            return null;
        }
        return result;

    }

    /**
     * @param metadataElement
     *            to be parsed.
     * @return the pair of optional &lt;xlink:href, about&gt; attributes.
     * @throws XMLParsingException
     *             if the abstract metadatas could not be parsed.
     */
    protected Metadata parseMetadata( Element metadataElement )
                            throws XMLParsingException {
        if ( metadataElement == null ) {
            return null;
        }
        String metadataHref = metadataElement.getAttributeNS( CommonNamespaces.XLNNS.toASCIIString(), ":href" );
        metadataHref = "".equals( metadataHref ) ? null : metadataHref;
        String metadataAbout = metadataElement.getAttribute( "about" );
        metadataAbout = "".equals( metadataAbout ) ? null : metadataAbout;
        Element abstractElement = getElement( metadataElement, "*[1]", nsContext );
        Element result = null;
        if ( abstractElement != null ) {
            Document doc = XMLTools.create();
            Element t = (Element) doc.importNode( abstractElement, true );
            result = (Element) doc.appendChild( t );
        }
        if ( metadataAbout != null || metadataHref != null || result != null ) {
            return new Metadata( metadataHref, metadataAbout, result );
        }
        return null;
    }

    /**
     * Parses the domain metadata type and it's reference attribute and puts them in a pair, like name, reference, which
     * may be null
     *
     * @param domainMDElements
     * @return a list of pairs containing the values or <code>null</code> if given list is <code>null</code> or it
     *         does not contain any domain elements value.
     * @throws XMLParsingException
     */
    protected List<Pair<String, String>> parseDomainMetadataTypes( List<Element> domainMDElements )
                            throws XMLParsingException {
        if ( domainMDElements == null || domainMDElements.size() == 0 ) {
            return null;
        }
        List<Pair<String, String>> domainMDTypes = new ArrayList<Pair<String, String>>( domainMDElements.size() );
        for ( Element mdT : domainMDElements ) {
            Pair<String, String> t = parseDomainMetadataType( mdT );
            if ( t != null ) {
                domainMDTypes.add( t );
            }
        }
        return ( domainMDTypes.size() == 0 ) ? null : domainMDTypes;
    }

    /**
     * Parses the domain metadata type and it's reference attribute and puts them in a pair, like name, reference, the
     * latter may be null
     *
     * @param domainMDElement
     *            to parse
     * @return a pair containing the values or <code>null</code> if given element is null or it does not contain a
     *         value.
     * @throws XMLParsingException
     */
    protected Pair<String, String> parseDomainMetadataType( Element domainMDElement )
                            throws XMLParsingException {
        if ( domainMDElement == null ) {
            return null;
        }
        String valuesReference = getNodeAsString( domainMDElement, ".", nsContext, null );
        String reference = getNodeAsString( domainMDElement, "@reference", nsContext, null );
        if ( valuesReference != null ) {
            return new Pair<String, String>( valuesReference, reference );
        }
        return null;
    }

    /**
     * @param rangeElements
     * @return the ranges or <code>null</code> if no elements were given.
     * @throws XMLParsingException
     */
    private List<Range> parseRanges( List<Element> rangeElements )
                            throws XMLParsingException {
        if ( rangeElements == null || rangeElements.size() == 0 ) {
            return null;
        }
        List<Range> ranges = new ArrayList<Range>( rangeElements.size() );
        for ( Element range : rangeElements ) {
            String minimumValue = getNodeAsString( range, PRE_OWS + "Range/" + PRE_OWS + "MinimumValue", nsContext,
                                                   null );
            String maximumValue = getNodeAsString( range, PRE_OWS + "Range/" + PRE_OWS + "MaximumValue", nsContext,
                                                   null );
            String spacing = getNodeAsString( range, PRE_OWS + "Range/" + PRE_OWS + "Spacing", nsContext, null );
            String rangeClosure = getNodeAsString( range, PRE_OWS + "Range/@rangeClosure", nsContext, "closed" );
            ranges.add( new Range( minimumValue, maximumValue, spacing, rangeClosure ) );
        }
        return ranges;
    }

    /**
     * @param serviceContact
     * @return the service contact bean
     * @throws XMLParsingException
     */
    protected ServiceContact parseServiceContact( Element serviceContact )
                            throws XMLParsingException {
        String individualName = getNodeAsString( serviceContact, PRE_OWS + "IndividualName", nsContext, null );
        String positionName = getNodeAsString( serviceContact, PRE_OWS + "PositionName", nsContext, null );
        ContactInfo contactInfo = parseContactInfo( getElement( serviceContact, PRE_OWS + "ContactInfo", nsContext ) );
        Pair<String, String> role = null;
        String roleS = getNodeAsString( serviceContact, PRE_OWS + "Role", nsContext, null );
        if ( roleS != null ) {
            role = new Pair<String, String>( roleS, getNodeAsString( serviceContact, PRE_OWS + "Role/@codeSpace",
                                                                     nsContext, null ) );
        }
        return new ServiceContact( individualName, positionName, contactInfo, role );
    }

    /**
     * @param contactInfo
     * @return the contactinfo or <code>null</code> if all underlying elements are empty.
     * @throws XMLParsingException
     */
    protected ContactInfo parseContactInfo( Element contactInfo )
                            throws XMLParsingException {
        if ( contactInfo == null ) {
            return null;
        }

        Pair<List<String>, List<String>> phone = null;
        Element phoneElement = getElement( contactInfo, PRE_OWS + "Phone", nsContext );
        if ( phoneElement != null ) {
            phone = new Pair<List<String>, List<String>>( getNodesAsStringList( phoneElement, PRE_OWS + "Voice",
                                                                                nsContext ),
                                                          getNodesAsStringList( phoneElement, PRE_OWS + "Facsimile",
                                                                                nsContext ) );
        }

        List<String> deliveryPoint = null;
        String city = null;
        String administrativeArea = null;
        String postalCode = null;
        String country = null;
        List<String> electronicMailAddress = null;
        Element address = getElement( contactInfo, PRE_OWS + "Address", nsContext );
        boolean hasAdress = false;
        if ( address != null ) {
            deliveryPoint = getNodesAsStringList( address, PRE_OWS + "DeliveryPoint", nsContext );
            city = getNodeAsString( address, PRE_OWS + "City", nsContext, null );
            administrativeArea = getNodeAsString( address, PRE_OWS + "AdministrativeArea", nsContext, null );
            postalCode = getNodeAsString( address, PRE_OWS + "PostalCode", nsContext, null );
            country = getNodeAsString( address, PRE_OWS + "Country", nsContext, null );
            electronicMailAddress = getNodesAsStringList( address, PRE_OWS + "ElectronicMailAddress", nsContext );
            if ( electronicMailAddress.size() == 0 ) {
                electronicMailAddress = null;
            }
            hasAdress = ( deliveryPoint != null || city != null || administrativeArea != null || postalCode != null
                          || country != null || electronicMailAddress != null );
        }

        String onlineResource = getNodeAsString( contactInfo, PRE_OWS + "OnlineResource/@" + XLINK_PREFIX + ":href",
                                                 nsContext, null );
        String hoursOfService = getNodeAsString( contactInfo, PRE_OWS + "HoursOfService", nsContext, null );
        String contactInstructions = getNodeAsString( contactInfo, PRE_OWS + "ContactInstructions", nsContext, null );
        if ( ( phone != null ) && !hasAdress && onlineResource == null && hoursOfService == null
             && contactInstructions == null ) {
            return null;
        }
        return new ContactInfo( phone, hasAdress, deliveryPoint, city, administrativeArea, postalCode, country,
                                electronicMailAddress, onlineResource, hoursOfService, contactInstructions );
    }

    /**
     * @param basicIdentificationType
     *            to be parsed.
     * @return the {@link BasicIdentification} bean representation or <code>null</code> if the given element is
     *         <code>null</code>.
     * @throws XMLParsingException
     *             if the given param can not be parsed.
     */
    protected BasicIdentification parseBasicIdentificationType( Element basicIdentificationType )
                            throws XMLParsingException {
        if ( basicIdentificationType == null ) {
            return null;
        }
        List<String> title = getNodesAsStringList( basicIdentificationType, PRE_OWS + "Title", nsContext );
        List<String> abstracts = getNodesAsStringList( basicIdentificationType, PRE_OWS + "Abstract", nsContext );
        List<Element> kws = getElements( basicIdentificationType, PRE_OWS + "Keywords", nsContext );
        // Pair<List<keywords>, codetype>
        List<Keywords> keywords = new ArrayList<Keywords>( ( ( kws == null ) ? 0 : kws.size() ) );
        if ( kws != null ) {
            for ( Element keyword : kws ) {
                keywords.add( parseKeywords( keyword ) );
            }
        }
        Pair<String, String> identifier = parseIdentifier( basicIdentificationType );
        List<Metadata> metadatas = parseMetadatas( getElements( basicIdentificationType, PRE_OWS + "Metadata",
                                                                nsContext ) );
        return new BasicIdentification( title, abstracts, keywords, identifier, metadatas );
    }

    /**
     * @param root
     *            element to be parsed for the identifier.
     * @return a &lt;value, codeSpace&gt; pair or <code>null</code> if the given element is <code>null</code> or if
     *         the identifier has no value and codeSpace.
     * @throws XMLParsingException
     */
    protected Pair<String, String> parseIdentifier( Element root )
                            throws XMLParsingException {
        if ( root == null ) {
            return null;
        }
        Pair<String, String> identifier = null;
        Element id = getElement( root, PRE_OWS + "Identifier", nsContext );
        if ( id != null ) {
            String value = getNodeAsString( id, ".", nsContext, "" );
            String codeSpace = id.getAttribute( "codeSpace" );
            if ( !"".equals( value ) || !"".equals( codeSpace.trim() ) ) {
                identifier = new Pair<String, String>( value, codeSpace );
            }
        }
        return identifier;
    }

    /**
     * Return the ows_1_1_0 keywords..
     *
     * @param keywords
     *            element to parse from
     * @return a <List<Keywords>,Type> pair.
     * @throws XMLParsingException
     */
    protected Keywords parseKeywords( Element keywords )
                            throws XMLParsingException {
        if ( keywords == null ) {
            return null;
        }
        String codetype = getNodeAsString( keywords, PRE_OWS + "Type", nsContext, null );
        String typeSpace = null;
        Pair<String, String> type = null;
        if ( codetype != null ) {
            typeSpace = getNodeAsString( keywords, PRE_OWS + "Type/@codeSpace", nsContext, null );
            type = new Pair<String, String>( codetype, typeSpace );
        }
        return new Keywords( getNodesAsStringList( keywords, PRE_OWS + "Keyword", nsContext ), type );

    }

}
