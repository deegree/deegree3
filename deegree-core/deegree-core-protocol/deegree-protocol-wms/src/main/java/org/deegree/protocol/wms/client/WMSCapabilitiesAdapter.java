//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wms.client;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.deegree.protocol.i18n.Messages.get;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.struct.Tree;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.ows.capabilities.OWSCapabilitiesAdapter;
import org.deegree.protocol.ows.metadata.Description;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.deegree.protocol.ows.metadata.domain.Domain;
import org.deegree.protocol.ows.metadata.operation.DCP;
import org.deegree.protocol.ows.metadata.operation.Operation;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class WMSCapabilitiesAdapter extends XMLAdapter implements OWSCapabilitiesAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( WMSCapabilitiesAdapter.class );

    /**
     * @param request
     * @return the image formats defined for the request, or null, if request is not supported
     */
    public LinkedList<String> getFormats( WMSRequestType request ) {
        if ( !isOperationSupported( request ) ) {
            return null;
        }
        XPath xp = new XPath( "//" + getPrefix() + request + "/" + getPrefix() + "Format", nsContext );
        LinkedList<String> list = new LinkedList<String>();
        Object res = evaluateXPath( xp, getRootElement() );
        if ( res instanceof List<?> ) {
            for ( Object o : (List<?>) res ) {
                list.add( ( (OMElement) o ).getText() );
            }
        }
        return list;
    }

    /**
     * Use parseOperationsMetadata().getGetUrls( request.name() ) or parseOperationsMetadata().getPostUrls(
     * request.name() ) instead
     * 
     * @param request
     * @param get
     *            true means HTTP GET, false means HTTP POST
     * @return the address, or null, if not defined or request unavailable
     */
    public String getAddress( WMSRequestType request, boolean get ) {
        if ( !isOperationSupported( request ) ) {
            return null;
        }
        List<URL> urls;
        if ( get ) {
            urls = parseOperationsMetadata().getGetUrls( request.name() );
        } else {
            urls = parseOperationsMetadata().getPostUrls( request.name() );
        }
        return urls.size() > 0 ? urls.get( 0 ).toExternalForm() : null;
    }

    /**
     * @param srs
     * @param layer
     * @return the envelope, or null, if none was found
     */
    public Envelope getBoundingBox( String srs, String layer ) {
        double[] min = new double[2];
        double[] max = new double[2];

        OMElement elem = getLayer( layer );
        while ( elem != null && elem.getLocalName().equals( "Layer" ) ) {
            OMElement bbox = getElement( elem, new XPath( getPrefix() + "BoundingBox[@" + getLayerCRSElementName()
                                                          + " = '" + srs + "']", nsContext ) );
            if ( bbox != null ) {
                try {
                    min[0] = Double.parseDouble( bbox.getAttributeValue( new QName( "minx" ) ) );
                    min[1] = Double.parseDouble( bbox.getAttributeValue( new QName( "miny" ) ) );
                    max[0] = Double.parseDouble( bbox.getAttributeValue( new QName( "maxx" ) ) );
                    max[1] = Double.parseDouble( bbox.getAttributeValue( new QName( "maxy" ) ) );
                    return new GeometryFactory().createEnvelope( min, max, CRSManager.getCRSRef( srs ) );
                } catch ( NumberFormatException nfe ) {
                    LOG.warn( get( "WMSCLIENT.SERVER_INVALID_NUMERIC_VALUE", nfe.getLocalizedMessage() ) );
                }
            } else {
                elem = (OMElement) elem.getParent();
            }
        }

        return null;
    }

    /**
     * @return the names of all layers that have a name
     */
    public List<String> getNamedLayers() {
        return asList( getNodesAsStrings( getRootElement(), new XPath( "//" + getPrefix() + "Layer/" + getPrefix()
                                                                       + "Name", nsContext ) ) );
    }

    /**
     * @param layer
     * @return true, if the WMS advertises a layer with that name
     */
    public boolean hasLayer( String layer ) {
        return getLayer( layer ) != null;
    }

    /**
     * @param name
     * @return all coordinate system names, also inherited ones
     */
    public LinkedList<String> getCoordinateSystems( String layer ) {
        LinkedList<String> list = new LinkedList<String>();
        if ( !hasLayer( layer ) ) {
            return list;
        }
        OMElement elem = getLayer( layer );
        String crsElementName = getPrefix() + getLayerCRSElementName();
        List<OMElement> es = getElements( elem, new XPath( crsElementName, nsContext ) );
        while ( ( elem = (OMElement) elem.getParent() ).getLocalName().equals( "Layer" ) ) {
            es.addAll( getElements( elem, new XPath( crsElementName, nsContext ) ) );
        }
        for ( OMElement e : es ) {
            if ( !list.contains( e.getText() ) ) {
                list.add( e.getText() );
            }
        }
        return list;
    }

    /**
     * @return return the elemnt name of the layer csr (without prefix)
     */
    protected abstract String getLayerCRSElementName();

    /**
     * @param layer
     * @return the envelope, or null, if none was found
     */
    public abstract Envelope getLatLonBoundingBox( String layer );

    /**
     * @param layers
     * @return a merged envelope of all the layer's envelopes
     */
    public Envelope getLatLonBoundingBox( List<String> layers ) {
        Envelope res = null;

        for ( String name : layers ) {
            if ( res == null ) {
                res = getLatLonBoundingBox( name );
            } else {
                res = res.merge( getLatLonBoundingBox( name ) );
            }
        }

        return res;
    }

    public Tree<LayerMetadata> getLayerTree() {
        Tree<LayerMetadata> tree = new Tree<LayerMetadata>();
        OMElement lay = getElement( getRootElement(), new XPath( "//" + getPrefix() + "Capability/" + getPrefix()
                                                                 + "Layer", nsContext ) );
        tree.value = extractMetadata( lay );
        buildLayerTree( tree, lay );
        return tree;
    }

    
    private void buildLayerTree( Tree<LayerMetadata> node, OMElement lay ) {
        for ( OMElement l : getElements( lay, new XPath( getPrefix() + "Layer", nsContext ) ) ) {
            Tree<LayerMetadata> child = new Tree<LayerMetadata>();
            child.value = extractMetadata( l );
            node.children.add( child );
            buildLayerTree( child, l );
        }
    }

    private LayerMetadata extractMetadata( OMElement lay ) {
        String name = getNodeAsString( lay, new XPath( getPrefix() + "Name", nsContext ), null );
        String title = getNodeAsString( lay, new XPath( getPrefix() + "Title", nsContext ), null );
        String abstract_ = getNodeAsString( lay, new XPath( getPrefix() + "Abstract", nsContext ), null );
        List<Pair<List<LanguageString>, CodeType>> keywords = null;
        OMElement kwlist = getElement( lay, new XPath( getPrefix() + "KeywordList", nsContext ) );
        if ( kwlist != null ) {
            keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
            Pair<List<LanguageString>, CodeType> p = new Pair<List<LanguageString>, CodeType>();
            p.first = new ArrayList<LanguageString>();
            keywords.add( p );
            String[] kws = getNodesAsStrings( kwlist, new XPath( getPrefix() + "Keyword", nsContext ) );
            for ( String kw : kws ) {
                p.first.add( new LanguageString( kw, null ) );
            }
        }

        Description desc = new Description( null, null, null, null );
        desc.setTitles( singletonList( new LanguageString( title, null ) ) );
        if ( abstract_ != null ) {
            desc.setAbstracts( singletonList( new LanguageString( abstract_, null ) ) );
        }
        desc.setKeywords( keywords );

        // use first envelope that we can find
        Envelope envelope = null;
        List<ICRS> crsList = new ArrayList<ICRS>();
        if ( name != null ) {
            envelope = getLatLonBoundingBox( name );
            for ( String crs : getCoordinateSystems( name ) ) {
                if ( envelope != null ) {
                    break;
                }
                envelope = getBoundingBox( crs, name );
            }
            for ( String crs : getCoordinateSystems( name ) ) {
                crsList.add( CRSManager.getCRSRef( crs, true ) );
            }
        }

        SpatialMetadata smd = new SpatialMetadata( envelope, crsList );
        LayerMetadata md = new LayerMetadata( name, desc, smd );

        String casc = lay.getAttributeValue( new QName( "cascaded" ) );
        if ( casc != null ) {
            try {
                md.setCascaded( Integer.parseInt( casc ) );
            } catch ( NumberFormatException nfe ) {
                md.setCascaded( 1 );
            }
        }
        md.setQueryable( getNodeAsBoolean( lay, new XPath( "@queryable" ), false ) );

        return md;
    }

    protected OMElement getLayer( String layer ) {
        return getElement( getRootElement(), new XPath( "//" + getPrefix() + "Layer[" + getPrefix() + "Name = '"
                                                        + layer + "']", nsContext ) );
    }
    
    /**
     * @param request
     * @return true, if an according section was found in the capabilities
     */
    public boolean isOperationSupported( WMSRequestType request ) {
        XPath xp = new XPath( "//" + getPrefix() + request, nsContext );
        return getElement( getRootElement(), xp ) != null;
    }

    // /**
    // * @return the system id of the capabilities document.
    // */
    // public String getSystemId() {
    // return getSystemId();
    // }

    @Override
    public List<String> parseLanguages()
                            throws XMLParsingException {
        return null;
    }

    @Override
    public ServiceProvider parseServiceProvider()
                            throws XMLParsingException {
        throw new UnsupportedOperationException( "ServiceProvider is not parsed, yet." );
    }

    @Override
    public ServiceIdentification parseServiceIdentification()
                            throws XMLParsingException {
        OMElement serviceIdEl = getElement( getRootElement(), new XPath( getPrefix() + "Service", nsContext ) );
        if ( serviceIdEl == null ) {
            return null;
        }

        String title = getRequiredNodeAsString( serviceIdEl, new XPath( getPrefix() + "Title", nsContext ) );
        List<LanguageString> titles = singletonList( new LanguageString( title, null ) );

        String name = getRequiredNodeAsString( serviceIdEl, new XPath( getPrefix() + "Name", nsContext ) );

        String _abstract = getNodeAsString( serviceIdEl, new XPath( getPrefix() + "Abstract", nsContext ), null );
        List<LanguageString> abstracts = _abstract != null ? singletonList( new LanguageString( _abstract, null ) )
                                                          : new ArrayList<LanguageString>();

        String[] keywordValues = getNodesAsStrings( serviceIdEl, new XPath( getPrefix() + "KeywordList/" + getPrefix()
                                                                            + "Keyword", nsContext ) );
        List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>(
                                                                                                                   keywordValues.length );

        List<LanguageString> keywordLS = new ArrayList<LanguageString>();
        for ( String keyword : keywordValues ) {
            if ( keyword != null ) {
                keywordLS.add( new LanguageString( keyword, null ) );
            }
        }
        keywords.add( new Pair<List<LanguageString>, CodeType>( keywordLS, null ) );

        CodeType serviceType = new CodeType( "WMS" );

        Version version = getNodeAsVersion( serviceIdEl, new XPath( "version", nsContext ), getServiceVersion() );
        List<Version> serviceTypeVersions = singletonList( version );

        String fees = getNodeAsString( serviceIdEl, new XPath( getPrefix() + "Fees", nsContext ), null );

        String constraintValues = getNodeAsString( serviceIdEl,
                                                   new XPath( getPrefix() + "AccessConstraints", nsContext ), null );
        List<String> constraints = constraintValues != null ? singletonList( constraintValues )
                                                           : new ArrayList<String>();

        return new ServiceIdentification( name, titles, abstracts, keywords, serviceType, serviceTypeVersions,
                                          new ArrayList<String>(), fees, constraints );

    }

    @Override
    public OperationsMetadata parseOperationsMetadata()
                            throws XMLParsingException {

        OMElement requestEl = getElement( getRootElement(), new XPath( getPrefix() + "Capability/" + getPrefix()
                                                                       + "Request", nsContext ) );
        if ( requestEl == null ) {
            return null;
        }
        XPath xpath = new XPath( "./*", nsContext );
        List<OMElement> opEls = getElements( requestEl, xpath );
        List<Operation> operations = new ArrayList<Operation>( opEls.size() );
        if ( opEls != null ) {
            for ( OMElement opEl : opEls ) {
                Operation op = parseOperation( opEl );
                operations.add( op );
            }
        }
        return new OperationsMetadata( operations, new ArrayList<Domain>(), new ArrayList<Domain>(),
                                       new ArrayList<OMElement>() );
    }

    private Operation parseOperation( OMElement opEl ) {

        String name = opEl.getLocalName();

        XPath xpath = new XPath( getPrefix() + "DCPType", nsContext );
        List<OMElement> dcpEls = getElements( opEl, xpath );
        List<DCP> dcps = new ArrayList<DCP>( dcpEls.size() );
        if ( dcpEls != null ) {
            for ( OMElement dcpEl : dcpEls ) {
                DCP dcp = parseDCP( dcpEl );
                dcps.add( dcp );
            }
        }
        return new Operation( name, dcps, new ArrayList<Domain>(), new ArrayList<Domain>(), new ArrayList<OMElement>() );
    }

    private DCP parseDCP( OMElement dcpEl ) {
        XPath xpath = new XPath( getPrefix() + "HTTP/" + getPrefix() + "Get", nsContext );

        List<OMElement> getEls = getElements( dcpEl, xpath );
        List<Pair<URL, List<Domain>>> getEndpoints = new ArrayList<Pair<URL, List<Domain>>>( getEls.size() );
        if ( getEls != null ) {
            for ( OMElement getEl : getEls ) {
                xpath = new XPath( getPrefix() + "OnlineResource/@xlink:href", nsContext );
                URL href = getNodeAsURL( getEl, xpath, null );
                getEndpoints.add( new Pair<URL, List<Domain>>( href, new ArrayList<Domain>() ) );
            }
        }

        xpath = new XPath( getPrefix() + "HTTP/" + getPrefix() + "Post", nsContext );
        List<OMElement> postEls = getElements( dcpEl, xpath );
        List<Pair<URL, List<Domain>>> postEndpoints = new ArrayList<Pair<URL, List<Domain>>>( postEls.size() );
        if ( postEls != null ) {
            for ( OMElement postEl : postEls ) {
                xpath = new XPath( getPrefix() + "OnlineResource/@xlink:href", nsContext );
                URL href = getNodeAsURL( postEl, xpath, null );

                postEndpoints.add( new Pair<URL, List<Domain>>( href, new ArrayList<Domain>() ) );
            }
        }

        return new DCP( getEndpoints, postEndpoints );
    }


    protected abstract Version getServiceVersion();

    protected abstract String getPrefix();

}