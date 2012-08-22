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
package org.deegree.model.feature;

import static org.deegree.framework.xml.XMLTools.getChildElements;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.feature.schema.GMLSchemaDocument;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Parser and wrapper class for GML feature collections.
 * <p>
 * Extends {@link GMLFeatureDocument}, as a feature collection is a feature in the GML type hierarchy.
 * <p>
 * 
 * TODO Remove hack for xlinked feature members (should be easy after fixing model package).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 * @see GMLFeatureDocument
 */
public class GMLFeatureCollectionDocument extends GMLFeatureDocument {

    private static final long serialVersionUID = -6923435144671685710L;

    private static final ILogger LOG = LoggerFactory.getLogger( GMLFeatureCollectionDocument.class );

    private Collection<String> xlinkedMembers = new ArrayList<String>();

    private boolean keepCollectionName = false;

    /**
     * Creates a new instance of <code>GMLFeatureCollectionDocument</code>.
     * <p>
     * Simple types encountered during parsing are "guessed", i.e. the parser tries to convert the values to double,
     * integer, calendar, etc. However, this may lead to unwanted results, e.g. a property value of "054604" is
     * converted to "54604".
     * </p>
     * <p>
     * Note, the featurecollection Document created with this constructor will return wfs-1.1 bound FeatureCollections.
     * If you want to return the same namespace bound feature collection as the incoming feature collection, please use
     * the {@link #GMLFeatureCollectionDocument(boolean, boolean)}
     * </p>
     */
    public GMLFeatureCollectionDocument() {
        super();
    }

    /**
     * Creates a new instance of <code>GMLFeatureCollectionDocument</code>.
     * <p>
     * Note, the featurecollection Document created with this constructor will return wfs-1.1 bound FeatureCollections.
     * If you want to return the same namespace bound feature collection as the incoming feature collection, please use
     * the {@link #GMLFeatureCollectionDocument(boolean, boolean)}
     * </p>
     * 
     * @param guessSimpleTypes
     *            set to true, if simple types should be "guessed" during parsing
     */
    public GMLFeatureCollectionDocument( boolean guessSimpleTypes ) {
        super( guessSimpleTypes );
    }

    /**
     * Creates a new instance of <code>GMLFeatureCollectionDocument</code>.
     * <p>
     * Instead of the other constructors, this one will be namespace aware of the incoming featureCollection. This
     * means, that the incoming top root element will hold it's namespace binding and will not automatically be
     * overwritten with the wfs:1.1 namespace binding.
     * </p>
     * 
     * @param guessSimpleTypes
     *            set to true, if simple types should be "guessed" during parsing
     * @param keepCollectionName
     *            if true, the returned FeatureCollection will have the same name as the incoming FeatureCollection
     *            document. If set to false this constructor equals the {@link #GMLFeatureCollectionDocument(boolean)}.
     */
    public GMLFeatureCollectionDocument( boolean guessSimpleTypes, boolean keepCollectionName ) {
        this( guessSimpleTypes );
        this.keepCollectionName = keepCollectionName;
    }

    /**
     * Returns the object representation of the underlying feature collection document.
     * 
     * @return object representation of the underlying feature collection document.
     * @throws XMLParsingException
     */
    public FeatureCollection parse()
                            throws XMLParsingException {
        readFeatureType( this.getRootElement() );
        FeatureCollection fc = parse( this.getRootElement() );
        resolveXLinkReferences();
        addXLinkedMembers( fc );
        return fc;
    }

    /**
     * @param rootElement
     */
    private void readFeatureType( Element rootElement ) {
        boolean loaded = false;
        try {
            String v = XMLTools.getAttrValue( rootElement, URI.create( "http://www.w3.org/2001/XMLSchema-instance" ),
                                              "schemaLocation", null );
            String[] tmp = StringTools.toArray( v, " ", false );
            Map<URI, GMLSchema> gmlSchemaMap = new HashMap<URI, GMLSchema>();
            for ( int i = 0; i < tmp.length; i++ ) {
                if ( tmp[i].indexOf( "=DescribeFeatureType" ) > -1 ) {
                    // maybe should be de-activated if WFS delivering data is located behind a owsProxy
                    GMLSchemaDocument schemaDoc = new GMLSchemaDocument();
                    LOG.logInfo( "read schema from: ", tmp[i] );
                    InputStream is = HttpUtils.performHttpGet( tmp[i], null, 60000, null, null, null ).getResponseBodyAsStream();
                    schemaDoc.load( is, tmp[i] );
                    GMLSchema schema = schemaDoc.parseGMLSchema();
                    gmlSchemaMap.put( schemaDoc.getTargetNamespace(), schema );
                    setSchemas( gmlSchemaMap );
                    loaded = true;
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            LOG.logWarning( "can not create feature type from schema reference; going on with default/heuristic mechanism" );
        }
        if ( !loaded ) {
            LOG.logWarning( "can not create feature type from schema reference; going on with default/heuristic mechanism" );
        }
    }

    /**
     * Ugly hack that adds the "xlinked" feature members to the feature collection.
     * 
     * TODO remove this
     * 
     * @param fc
     * @throws XMLParsingException
     */
    private void addXLinkedMembers( FeatureCollection fc )
                            throws XMLParsingException {
        Iterator<String> iter = this.xlinkedMembers.iterator();
        while ( iter.hasNext() ) {
            String fid = iter.next();
            Feature feature = this.featureMap.get( fid );
            if ( feature == null ) {
                String msg = Messages.format( "ERROR_XLINK_NOT_RESOLVABLE", fid );
                throw new XMLParsingException( msg );
            }
            fc.add( feature );
        }
    }

    /**
     * Returns the object representation for the given feature collection element.
     * 
     * @return object representation for the given feature collection element.
     * @throws XMLParsingException
     */
    private FeatureCollection parse( Element element )
                            throws XMLParsingException {

        String fcId = parseFeatureId( element );
        // generate id if necessary (use feature type name + a unique number as id)
        if ( "".equals( fcId ) ) {
            fcId = element.getLocalName();
            fcId += IDGenerator.getInstance().generateUniqueID();
        }

        String srsName = XMLTools.getNodeAsString( element, "gml:boundedBy/*[1]/@srsName", nsContext, null );

        ElementList el = XMLTools.getChildElements( element );
        List<Feature> list = new ArrayList<Feature>( el.getLength() );

        for ( int i = 0; i < el.getLength(); i++ ) {
            Feature member = null;
            Element propertyElement = el.item( i );
            String propertyName = propertyElement.getNodeName();

            if ( !propertyName.endsWith( "boundedBy" ) && !propertyName.endsWith( "name" )
                 && !propertyName.endsWith( "description" ) ) {
                // the first child of a feature member must always be a feature
                // OR it's a featureMembers element (so we have MANY features)...
                ElementList featureList = getChildElements( el.item( i ) );
                for ( int k = 0; k < featureList.getLength(); ++k ) {
                    Element featureElement = featureList.item( k );
                    if ( featureElement == null ) {
                        // check if feature content is xlinked
                        // TODO remove this ugly hack
                        Text xlinkHref = (Text) XMLTools.getNode( propertyElement, "@xlink:href/text()", nsContext );
                        if ( xlinkHref == null ) {
                            String msg = Messages.format( "ERROR_INVALID_FEATURE_PROPERTY", propertyName );
                            throw new XMLParsingException( msg );
                        }
                        String href = xlinkHref.getData();
                        if ( !href.startsWith( "#" ) ) {
                            String msg = Messages.format( "ERROR_EXTERNAL_XLINK_NOT_SUPPORTED", href );
                            throw new XMLParsingException( msg );
                        }
                        String fid = href.substring( 1 );
                        this.xlinkedMembers.add( fid );
                    } else {
                        try {
                            member = parseFeature( featureElement, srsName );
                            list.add( member );
                        } catch ( Exception e ) {
                            throw new XMLParsingException( "Error creating feature instance from element '"
                                                           + featureElement.getLocalName() + "': " + e.getMessage(), e );
                        }
                    }
                }
            }
        }

        Feature[] features = list.toArray( new Feature[list.size()] );
        FeatureCollection fc = null;
        if ( keepCollectionName ) {
            String prefix = element.getPrefix();
            String namespaceURI = element.getNamespaceURI();
            if ( prefix != null && !"".equals( prefix.trim() ) ) {
                String tmp = element.lookupNamespaceURI( prefix );
                if ( tmp != null && !"".equals( tmp.trim() ) ) {
                    namespaceURI = tmp;
                }
            }
            if ( namespaceURI == null || "".equals( namespaceURI.trim() )
                 || CommonNamespaces.WFSNS.toASCIIString().equals( namespaceURI ) ) {
                fc = FeatureFactory.createFeatureCollection( fcId, features );
            } else {
                QualifiedName name = null;
                URI ns = null;
                try {
                    ns = new URI( namespaceURI );
                    name = new QualifiedName( prefix, element.getLocalName(), ns );
                } catch ( URISyntaxException e ) {
                    // a failure while creating the namespace uri, the name will be null and the
                    // wfs:FeatureCollection
                    // will be the default, just to be safe.
                }
                fc = FeatureFactory.createFeatureCollection( fcId, features, name );
            }
        } else {
            // the old (default) behavior, just use the wfs-namespace for all feature collections.
            fc = FeatureFactory.createFeatureCollection( fcId, features );
        }
        String nof = element.getAttribute( "numberOfFeatures" );
        if ( nof == null ) {
            nof = "" + features.length;
        }
        fc.setAttribute( "numberOfFeatures", nof );
        return fc;
    }
}
