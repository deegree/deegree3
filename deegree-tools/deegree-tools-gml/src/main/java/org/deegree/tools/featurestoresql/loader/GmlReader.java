/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2021 lat/lon GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.featurestoresql.loader;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.reference.GmlDocumentIdContext;
import org.deegree.gml.reference.matcher.BaseUrlReferencePatternMatcher;
import org.deegree.gml.reference.matcher.MultipleReferencePatternMatcher;
import org.deegree.gml.reference.matcher.ReferencePatternMatcher;
import org.slf4j.Logger;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.core.io.Resource;

/**
 * Reads a GML 3.2 resource as well as wfs:FeatureCollections.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlReader extends AbstractItemStreamItemReader<Feature> implements
                                                                    ResourceAwareItemReaderItemStream<Feature> {

    private static final Logger LOG = getLogger( GmlReader.class );

    public static final QName WFS_20_MEMBER = new QName( WFS_200_NS, "member" );

    public static final QName GML_MEMBER = new QName( GMLNS, "featureMember" );

    public static final QName GML_MEMBERS = new QName( GMLNS, "featureMembers" );

    private final SQLFeatureStore sqlFeatureStore;

    private Resource resource;

    private InputStream inputStream;

    private XMLStreamReader xmlStreamReader;

    private FeatureInputStream featureStream;

    private Iterator<Feature> featureIterator;

    private int noOfFeaturesRead = 0;

    private List<String> disabledResources;

    /**
     * @param sqlFeatureStore
     *            the {@link SQLFeatureStore} used for insert, may be <code>null</code>
     */
    public GmlReader( SQLFeatureStore sqlFeatureStore ) {
        this.sqlFeatureStore = sqlFeatureStore;
    }

    @Override
    public void setResource( Resource resource ) {
        this.resource = resource;
    }

    @Override
    public Feature read()
                            throws Exception {
        if ( this.featureStream == null || this.featureIterator == null ) {
            return null;
        }
        if ( !featureIterator.hasNext() )
            return null;
        Feature feature = this.featureIterator.next();
        if ( feature != null )
            LOG.info( "Read feature with id " + feature.getId() + " (number " + ++noOfFeaturesRead + ") " );
        return feature;
    }

    @Override
    public void open( ExecutionContext executionContext ) {
        super.open( executionContext );
        if ( this.resource == null )
            throw new IllegalStateException( "Input resource must not be null." );
        if ( !this.resource.exists() )
            throw new IllegalStateException( "Input resource must exist." );
        if ( !this.resource.isReadable() )
            throw new IllegalStateException( "Input resource must be readable." );

        openFeatureStream();
    }

    @Override
    public void close() {
        super.close();
        try {
            if ( this.featureStream != null ) {
                this.featureStream.close();
            }
            if ( this.xmlStreamReader != null ) {
                this.xmlStreamReader.close();
            }
            if ( this.inputStream != null ) {
                this.inputStream.close();
            }
        } catch ( Exception var2 ) {
            throw new ItemStreamException( "Error while closing item reader", var2 );
        } finally {
            this.featureStream = null;
            this.xmlStreamReader = null;
            this.inputStream = null;
        }

    }

    public void setDisabledResources( List<String> disabledResources ) {
        this.disabledResources = disabledResources;
    }
    
    private void openFeatureStream() {
        try {
            this.inputStream = this.resource.getInputStream();
            GMLVersion version = GMLVersion.GML_32;
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            xmlInputFactory.setProperty( XMLInputFactory.IS_COALESCING, true );
            this.xmlStreamReader = xmlInputFactory.createXMLStreamReader( this.inputStream );
            XMLStreamReaderWrapper xmlStream = new XMLStreamReaderWrapper( xmlStreamReader, null );
            GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader( version, xmlStream );
            gmlStreamReader.setApplicationSchema( findSchema() );
            SkipInternalGmlDocumentIdContext resolver = new SkipInternalGmlDocumentIdContext( version );
            resolver.setReferencePatternMatcher( parseDisabledResources() );
            gmlStreamReader.setResolver( resolver );

            if ( new QName( WFS_200_NS, "FeatureCollection" ).equals( xmlStream.getName() ) ) {
                LOG.debug( "Features embedded in wfs20:FeatureCollection" );
                this.featureStream = new WfsFeatureInputStream( xmlStream, gmlStreamReader, WFS_20_MEMBER );
            } else if ( new QName( WFS_NS, "FeatureCollection" ).equals( xmlStream.getName() ) ) {
                LOG.debug( "Features embedded in wfs:FeatureCollection" );
                this.featureStream = new WfsFeatureInputStream( xmlStream, gmlStreamReader, GML_MEMBER, GML_MEMBERS );
            } else {
                LOG.debug( "Features embedded in gml:FeatureCollection" );
                this.featureStream = gmlStreamReader.readFeatureCollectionStream();
            }

            this.featureIterator = featureStream.iterator();
        } catch ( Exception e ) {
            throw new ItemStreamException( "Failed to initialize the reader", e );
        }
    }

    private AppSchema findSchema() {
        if ( sqlFeatureStore != null )
            return sqlFeatureStore.getSchema();
        return null;
    }

    private class WfsFeatureInputStream implements FeatureInputStream {

        private final XMLStreamReader xmlStream;

        private final GMLStreamReader gmlStream;

        private final List<QName> matchingNames;

        private Feature next;

        public WfsFeatureInputStream( XMLStreamReader xmlStream, GMLStreamReader gmlStream, QName... matchingNames ) {
            this.xmlStream = xmlStream;
            this.gmlStream = gmlStream;
            this.matchingNames = Arrays.asList( matchingNames );
            this.next = retrieveNext( xmlStream, gmlStream, this.matchingNames );
        }

        @Override
        public Iterator<Feature> iterator() {

            return new Iterator<Feature>() {
                @Override
                public boolean hasNext() {
                    return next != null;
                }

                @Override
                public Feature next() {
                    if ( next == null ) {
                        throw new NoSuchElementException();
                    }
                    Feature currentFeature = next;
                    next = retrieveNext( xmlStream, gmlStream, matchingNames );
                    return currentFeature;
                }
            };

        }

        @Override
        public void close() {

        }

        @Override
        public FeatureCollection toCollection() {
            return null;
        }

        @Override
        public int count() {
            return 0;
        }
    }

    private Feature retrieveNext( XMLStreamReader xmlStream, GMLStreamReader gmlStream, List<QName> matchingNames ) {
        try {
            while ( xmlStream.nextTag() == START_ELEMENT ) {
                QName elName = xmlStream.getName();
                if ( matchingNames.contains( elName ) ) {
                    xmlStream.nextTag();
                    Feature feature = gmlStream.readFeature();
                    xmlStream.nextTag();
                    return feature;
                } else {
                    LOG.debug( "Ignoring element '" + elName + "'" );
                    XMLStreamUtils.skipElement( xmlStream );
                }
            }
        } catch ( Exception e ) {
            LOG.error( "Failed", e );
        }
        return null;
    }

    private ReferencePatternMatcher parseDisabledResources() {
        if ( disabledResources != null && !disabledResources.isEmpty() ) {
            MultipleReferencePatternMatcher matcher = new MultipleReferencePatternMatcher();
            for ( String disabledResource : disabledResources ) {
                LOG.debug( "Added disabled resource pattern " + disabledResource );
                BaseUrlReferencePatternMatcher baseUrlMatcher = new BaseUrlReferencePatternMatcher( disabledResource );
                matcher.addMatcherToApply( baseUrlMatcher );
            }
            return matcher;
        }
        return null;
    }

    private class SkipInternalGmlDocumentIdContext extends GmlDocumentIdContext {

        public SkipInternalGmlDocumentIdContext( GMLVersion version ) {
            super( version );
        }

        @Override
        public GMLObject getObject( String id ) {
            if ( id.startsWith( "#" ) )
                return null;
            return super.getObject( id );
        }
    }

}