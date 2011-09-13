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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.domain.Domain;
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
