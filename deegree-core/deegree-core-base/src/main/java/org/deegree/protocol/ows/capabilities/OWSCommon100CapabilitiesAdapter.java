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

import static org.deegree.commons.xml.CommonNamespaces.OWS_NS;

import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.domain.AllowedValues;
import org.deegree.protocol.ows.metadata.domain.Domain;
import org.deegree.protocol.ows.metadata.domain.PossibleValues;
import org.deegree.protocol.ows.metadata.domain.Value;
import org.deegree.protocol.ows.metadata.domain.Values;
import org.deegree.protocol.ows.metadata.operation.DCP;
import org.deegree.protocol.ows.metadata.operation.Operation;

/**
 * {@link OWSCapabilitiesAdapter} for capabilities documents that comply to the <a
 * href="http://www.opengeospatial.org/standards/common">OWS Common 1.0.0</a> specification.
 * <p>
 * Known OWS Common 1.0.0-based specifications:
 * <ul>
 * <li>WFS 1.1.0</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OWSCommon100CapabilitiesAdapter extends AbstractOWSCommonCapabilitiesAdapter {

    /**
     * Creates a new {@link OWSCommon100CapabilitiesAdapter} instance.
     */
    public OWSCommon100CapabilitiesAdapter() {
        super( OWS_NS );
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
        List<Domain> params = new ArrayList<Domain>( paramEls.size() );
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

        return new Operation( name, dcps, params, constraints, metadataEls );
    }

    @Override
    protected Domain parseDomain( OMElement domainEl ) {

        // <attribute name="name" type="string" use="required">
        String name = getNodeAsString( domainEl, new XPath( "@name", nsContext ), null );

        // <element name="Value" type="string" maxOccurs="unbounded">
        String[] valuesArray = getNodesAsStrings( domainEl, new XPath( "ows:Value", nsContext ) );
        List<Values> values = new ArrayList<Values>( valuesArray.length );
        for ( String value : valuesArray ) {
            values.add( new Value( value ) );
        }

        PossibleValues possibleValues = new AllowedValues( values );

        // <element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded">
        List<OMElement> metadataEls = getElements( domainEl, new XPath( "ows:Metadata", nsContext ) );

        return new Domain( name, possibleValues, null, null, null, null, null, metadataEls );
    }
}
