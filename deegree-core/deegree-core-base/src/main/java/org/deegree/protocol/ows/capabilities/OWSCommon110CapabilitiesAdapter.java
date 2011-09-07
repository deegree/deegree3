//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.ows.capabilities;

import static org.deegree.commons.xml.CommonNamespaces.OWS_11_NS;
import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.metadata.DCP;
import org.deegree.protocol.ows.metadata.Domain;
import org.deegree.protocol.ows.metadata.Operation;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.PossibleValues;
import org.deegree.protocol.ows.metadata.Range;
import org.deegree.protocol.ows.metadata.ValuesUnit;

/**
 * {@link OWSCapabilitiesAdapter} for capabilities documents that comply to the <a
 * href="http://www.opengeospatial.org/standards/common">OWS 1.1.0</a> specification.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OWSCommon110CapabilitiesAdapter extends AbstractOWSCommonCapabilitiesAdapter {

    /**
     * Creates a new {@link OWSCommon110CapabilitiesAdapter} instance.
     */
    public OWSCommon110CapabilitiesAdapter() {
        super( OWS_11_NS );
        nsContext.addNamespace( "wps", WPS_100_NS );
    }

    @Override
    public OperationsMetadata parseOperationsMetadata() {

        OMElement opMetadataEl = getElement( getRootElement(), new XPath( "ows:OperationsMetadata", nsContext ) );
        if ( opMetadataEl == null ) {
            return null;
        }

        XPath xpath = new XPath( "ows:Operation", nsContext );
        List<OMElement> opEls = getElements( opMetadataEl, xpath );
        List<Operation> operations = new ArrayList<Operation>( opEls.size() );
        if ( opEls != null ) {
            for ( OMElement opEl : opEls ) {
                Operation op = parseOperation( opEl );
                operations.add( op );
            }
        }

        xpath = new XPath( "ows:Parameter", nsContext );
        List<OMElement> paramEls = getElements( opMetadataEl, xpath );
        List<Domain> params = new ArrayList<Domain>( opEls.size() );
        if ( paramEls != null ) {
            for ( OMElement paramEl : paramEls ) {
                Domain parameter = parseDomain( paramEl );
                params.add( parameter );
            }
        }

        xpath = new XPath( "ows:Constraint", nsContext );
        List<OMElement> constaintEls = getElements( opMetadataEl, xpath );
        List<Domain> constraints = new ArrayList<Domain>( constaintEls.size() );
        if ( constaintEls != null ) {
            for ( OMElement constaintEl : constaintEls ) {
                Domain constraint = parseDomain( constaintEl );
                constraints.add( constraint );
            }
        }

        xpath = new XPath( "ows:ExtendedCapabilities", nsContext );
        OMElement extededCapab = getElement( opMetadataEl, xpath );

        return new OperationsMetadata( operations, params, constraints, extededCapab );
    }

    /**
     * @param opEl
     *            context {@link OMElement}
     * @return an {@link Operation} instance, never <code>null</code>
     */
    private Operation parseOperation( OMElement opEl ) {

        XPath xpath = new XPath( "@name", nsContext );
        String name = getNodeAsString( opEl, xpath, null );

        xpath = new XPath( "ows:DCP", nsContext );
        List<OMElement> dcpEls = getElements( opEl, xpath );
        List<DCP> dcps = new ArrayList<DCP>( dcpEls.size() );
        if ( dcpEls != null ) {
            for ( OMElement dcpEl : dcpEls ) {
                DCP dcp = parseDCP( dcpEl );
                dcps.add( dcp );
            }
        }

        xpath = new XPath( "ows:Parameter", nsContext );
        List<OMElement> paramEls = getElements( opEl, xpath );
        List<Domain> params = new ArrayList<Domain>( paramEls.size() );
        if ( paramEls != null ) {
            for ( OMElement paramEl : paramEls ) {
                Domain parameter = parseDomain( paramEl );
                params.add( parameter );
            }
        }

        xpath = new XPath( "ows:Constraint", nsContext );
        List<OMElement> constaintEls = getElements( opEl, xpath );
        List<Domain> constraints = new ArrayList<Domain>( constaintEls.size() );
        if ( constaintEls != null ) {
            for ( OMElement constaintEl : constaintEls ) {
                Domain constraint = parseDomain( constaintEl );
                constraints.add( constraint );
            }
        }

        xpath = new XPath( "ows:Metadata", nsContext );
        List<OMElement> metadataEls = getElements( opEl, xpath );
        List<Pair<URL, URL>> metadata = new ArrayList<Pair<URL, URL>>( metadataEls.size() );
        if ( metadataEls != null ) {
            for ( OMElement metadataEl : metadataEls ) {
                xpath = new XPath( "@xlink:href", nsContext );
                URL ref = getNodeAsURL( metadataEl, xpath, null );

                xpath = new XPath( "@about", nsContext );
                URL about = getNodeAsURL( metadataEl, xpath, null );
                metadata.add( new Pair<URL, URL>( ref, about ) );
            }
        }

        return new Operation( name, dcps, params, constraints, metadata );
    }

    /**
     * @param domainEl
     *            context {@link OMElement}
     * @return an {@link Operation} instance, never <code>null</code>
     */
    private Domain parseDomain( OMElement domainEl ) {
        Domain domain = new Domain();

        XPath xpath = new XPath( "@name", nsContext );
        domain.setName( getNodeAsString( domainEl, xpath, null ) );

        PossibleValues possbileVals = parsePossibleValues( domainEl );
        domain.setPossibleValues( possbileVals );

        xpath = new XPath( "ows:DefaultValue", nsContext );
        domain.setDefaultValue( getNodeAsString( domainEl, xpath, null ) );

        xpath = new XPath( "ows:Meaning", nsContext );
        OMElement meaningEl = getElement( domainEl, xpath );
        String meaningRef = meaningEl.getAttributeValue( new QName( OWS_11_NS, "reference" ) );
        domain.setMeaningURL( meaningRef );
        String meangingText = meaningEl.getText();
        domain.setMeaningName( meangingText );

        xpath = new XPath( "ows:DataType", nsContext );
        OMElement datatypeEl = getElement( domainEl, xpath );
        String datatypeRef = datatypeEl.getAttributeValue( new QName( OWS_11_NS, "reference" ) );
        domain.setDataTypeURL( datatypeRef );
        String datatypeText = datatypeEl.getText();
        domain.setDataTypeName( datatypeText );

        ValuesUnit vals = parseValuesUnit( domainEl );
        domain.setValuesUnit( vals );

        xpath = new XPath( "ows:Metadata", nsContext );
        List<OMElement> metadataEls = getElements( domainEl, xpath );
        for ( OMElement metadataEl : metadataEls ) {
            xpath = new XPath( "@xlink:href", nsContext );
            URL ref = getNodeAsURL( metadataEl, xpath, null );

            xpath = new XPath( "@about", nsContext );
            URL about = getNodeAsURL( metadataEl, xpath, null );
            domain.getMetadata().add( new Pair<URL, URL>( ref, about ) );
        }

        return domain;
    }

    /**
     * @param domainEl
     *            context {@link OMElement}
     * @return an {@link ValuesUnit} instance, never <code>null</code>
     */
    private ValuesUnit parseValuesUnit( OMElement domainEl ) {
        ValuesUnit values = new ValuesUnit();

        XPath xpath = new XPath( "ows:ValueUnit", nsContext );
        OMElement valueUnitEl = getElement( domainEl, xpath );
        xpath = new XPath( "ows:UOM", nsContext );
        OMElement uomEl = getElement( valueUnitEl, xpath );
        String uomReference = uomEl.getAttributeValue( new QName( OWS_11_NS, "reference" ) );
        values.setUomURI( uomReference );
        String uomText = uomEl.getText();
        values.setUomName( uomText );

        xpath = new XPath( "ows:ReferenceSystem", nsContext );
        OMElement refSysEl = getElement( valueUnitEl, xpath );
        String refSysReference = refSysEl.getAttributeValue( new QName( OWS_11_NS, "reference" ) );
        values.setReferenceSystemURL( refSysReference );
        String refSysText = refSysEl.getText();
        values.setReferenceSystemName( refSysText );

        return values;
    }

    /**
     * @param domainEl
     *            context {@link OMElement}
     * @return an {@link PossibleValues} instance, never <code>null</code>
     */
    private PossibleValues parsePossibleValues( OMElement domainEl ) {
        PossibleValues possibleVals = new PossibleValues();

        XPath xpath = new XPath( "ows:AllowedValues", nsContext );
        OMElement allowedEl = getElement( domainEl, xpath );
        xpath = new XPath( "ows:Value", nsContext );
        String[] values = getNodesAsStrings( allowedEl, xpath );
        for ( int i = 0; i < values.length; i++ ) {
            possibleVals.getValue().add( values[i] );
        }

        xpath = new XPath( "ows:Range", nsContext );
        List<OMElement> rangeEls = getElements( domainEl, xpath );
        for ( OMElement rangeEl : rangeEls ) {
            Range range = parseRange( rangeEl );
            possibleVals.getRange().add( range );
        }

        xpath = new XPath( "ows:AnyValue", nsContext );
        if ( getNode( domainEl, xpath ) != null ) {
            possibleVals.setAnyValue();
        }

        xpath = new XPath( "ows:NoValues", nsContext );
        if ( getNode( domainEl, xpath ) != null ) {
            possibleVals.setNoValue();
        }

        xpath = new XPath( "ows:ValuesReference", nsContext );
        OMElement valuesRefEl = getElement( domainEl, xpath );
        String valuesRef = valuesRefEl.getAttributeValue( new QName( OWS_11_NS, "reference" ) );
        possibleVals.setReferenceURL( valuesRef );
        String valuesRefName = valuesRefEl.getText();
        possibleVals.setReferenceName( valuesRefName );

        return possibleVals;
    }

    /**
     * @param rangeEl
     *            context {@link OMElement}
     * @return an {@link Range} instance, never <code>null</code>
     */
    private Range parseRange( OMElement rangeEl ) {
        Range range = new Range();

        XPath xpath = new XPath( "ows:MinimumValue", nsContext );
        range.setMinimumValue( getNodeAsString( rangeEl, xpath, null ) );
        xpath = new XPath( "ows:MaximumValue", nsContext );
        range.setMaximumValue( getNodeAsString( rangeEl, xpath, null ) );
        xpath = new XPath( "ows:Spacing", nsContext );
        range.setSpacing( getNodeAsString( rangeEl, xpath, null ) );
        xpath = new XPath( "@ows:rangeClosure", nsContext );
        range.setRangeClosure( getNodeAsString( rangeEl, xpath, null ) );

        return range;
    }

    /**
     * @param dcpEl
     *            context {@link OMElement}
     * @return an {@link DCP} instance, never <code>null</code>
     */
    private DCP parseDCP( OMElement dcpEl ) {
        DCP dcp = new DCP();

        XPath xpath = new XPath( "ows:HTTP/ows:Get", nsContext );
        List<OMElement> getEls = getElements( dcpEl, xpath );
        if ( getEls != null ) {
            for ( OMElement getEl : getEls ) {
                xpath = new XPath( "@xlink:href", nsContext );
                URL href = getNodeAsURL( getEl, xpath, null );

                xpath = new XPath( "ows:Constraint", nsContext );
                List<OMElement> constaintEls = getElements( getEl, xpath );
                List<Domain> domains = new ArrayList<Domain>();
                for ( OMElement constaintEl : constaintEls ) {
                    Domain constraint = parseDomain( constaintEl );
                    domains.add( constraint );
                }

                dcp.getGetURLs().add( new Pair<URL, List<Domain>>( href, domains ) );
            }
        }

        xpath = new XPath( "ows:HTTP/ows:Post", nsContext );
        List<OMElement> postEls = getElements( dcpEl, xpath );
        if ( postEls != null ) {
            for ( OMElement postEl : postEls ) {
                xpath = new XPath( "@xlink:href", nsContext );
                URL href = getNodeAsURL( postEl, xpath, null );

                xpath = new XPath( "ows:Constraint", nsContext );
                List<OMElement> constaintEls = getElements( postEl, xpath );
                List<Domain> domains = new ArrayList<Domain>();
                for ( OMElement constaintEl : constaintEls ) {
                    Domain constraint = parseDomain( constaintEl );
                    domains.add( constraint );
                }

                dcp.getPostURLs().add( new Pair<URL, List<Domain>>( href, domains ) );
            }
        }

        return dcp;
    }

    /**
     * Returns the URL for the specified operation and HTTP method.
     * 
     * @param operation
     *            name of the operation, must not be <code>null</code>
     * @param post
     *            if set to true, the URL for POST requests will be returned, otherwise the URL for GET requests will be
     *            returned
     * @return the operation URL (trailing question marks are stripped), can be <code>null</code> (if the
     *         operation/method is not announced by the service)
     * @throws MalformedURLException
     *             if the announced URL is malformed
     */
    public URL getOperationURL( String operation, boolean post )
                            throws MalformedURLException {

        String xpathStr = "ows:OperationsMetadata/ows:Operation[@name='" + operation + "']/ows:DCP/ows:HTTP/ows:"
                          + ( post ? "Post" : "Get" ) + "/@xlink:href";
        URL url = null;
        String href = getNodeAsString( getRootElement(), new XPath( xpathStr, nsContext ), null );
        if ( href != null ) {
            if ( href.endsWith( "?" ) ) {
                href = href.substring( 0, href.length() - 1 );
            }
            url = new URL( href );
        }
        return url;
    }
}
