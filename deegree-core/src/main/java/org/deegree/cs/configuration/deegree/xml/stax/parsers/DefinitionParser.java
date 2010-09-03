//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.cs.configuration.deegree.xml.stax.parsers;

import static org.deegree.commons.xml.stax.StAXParsingHelper.getSimpleUnboundedAsStrings;
import static org.deegree.commons.xml.stax.StAXParsingHelper.moveReaderToFirstMatch;
import static org.deegree.commons.xml.stax.StAXParsingHelper.nextElement;
import static org.deegree.cs.configuration.deegree.xml.stax.Parser.CRS_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Unit;
import org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider;
import org.deegree.cs.configuration.deegree.xml.stax.StAXResource;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.i18n.Messages;
import org.slf4j.Logger;

/**
 * The parent class of all parsers defines convenience methods common to all StAX based crs parsers as well as reading
 * in the locations of the crs components defintions.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get stacktraces if something goes wrong.")
public abstract class DefinitionParser {

    private static final Logger LOG = getLogger( DefinitionParser.class );

    private final Object LOCK = new Object();

    private DeegreeCRSProvider<StAXResource> provider = null;

    private XMLStreamReader configReader;

    private InputStream configStream;

    private final URL configURL;

    private boolean readEntireFile = false;

    /**
     * @param provider
     * @param configURL
     */
    protected DefinitionParser( DeegreeCRSProvider<StAXResource> provider, URL configURL ) {
        this.configURL = configURL;
        this.provider = provider;
    }

    /**
     * @return the top level element of a configuration file.
     */
    protected abstract QName expectedRootName();

    /**
     * Get the configuration reader. The stream will be before the end document event or <code>null</code> if the entire
     * file was read.
     * 
     * @return the configuration reader.
     * @throws XMLStreamException
     */
    protected XMLStreamReader getConfigReader()
                            throws XMLStreamException {
        synchronized ( LOCK ) {
            /**
             * rb: What to do about caching, if the cache is cleared {@link AbstractCRSProvider#clearCache()}, the
             * 'readEntireFile' flag must be set to false...
             */
            if ( configReader == null ) {
                openReader();
            } else {
                if ( !readEntireFile ) {
                    if ( configReader.getEventType() == XMLStreamConstants.END_DOCUMENT ) {
                        try {
                            this.configStream.close();
                        } catch ( IOException e ) {
                            LOG.debug( "Unable to close the stream to the configuration: " + this.configURL
                                       + " stack trace.", e );
                            LOG.debug( "Unable to close the stream to the configuration: " + this.configURL
                                       + " because: " + e.getLocalizedMessage() );
                        }
                        this.configReader.close();
                        this.readEntireFile = true;
                        // openReader();
                        this.configReader = null;
                    }
                }
            }
        }
        return configReader;
    }

    /**
     * Parses all elements of the identifiable object, it is assumed the reader is on a top level element, the next
     * element will be an id. After this method the reader will point to the first element after areasOfUse.
     * 
     * @param reader
     *            the xml-reader pointing to parent of the first id-element
     * @return the identifiable object or <code>null</code> if no id was given.
     * @throws CRSConfigurationException
     */
    protected CRSIdentifiable parseIdentifiable( XMLStreamReader reader )
                            throws CRSConfigurationException {
        QName parent = reader.getName();
        try {

            if ( !reader.isStartElement() ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NOT_START_STAX_ERROR",
                                                                          "CRSIdentifiable", parent ) );
            }
            // point to the next id element.
            nextElement( reader );

            String[] identifiers = getSimpleUnboundedAsStrings( reader, new QName( CRS_NS, "Id" ) );
            if ( identifiers.length == 0 ) {
                String msg = Messages.getMessage( "CRS_CONFIG_NO_ID", parent );
                throw new CRSConfigurationException( msg );
            }

            String[] names = getSimpleUnboundedAsStrings( reader, new QName( CRS_NS, "Name" ) );
            String[] versions = getSimpleUnboundedAsStrings( reader, new QName( CRS_NS, "Version" ) );
            String[] descriptions = getSimpleUnboundedAsStrings( reader, new QName( CRS_NS, "Description" ) );
            String[] areasOfUse = getSimpleUnboundedAsStrings( reader, new QName( CRS_NS, "AreaOfUse" ) );

            // convert the string IDs to CRSCodeTypes
            Set<CRSCodeType> codeSet = new HashSet<CRSCodeType>();
            int n = identifiers.length;
            for ( int i = 0; i < n; i++ ) {
                codeSet.add( CRSCodeType.valueOf( identifiers[i] ) );
            }
            return new CRSIdentifiable( codeSet.toArray( new CRSCodeType[codeSet.size()] ), names, versions,
                                        descriptions, areasOfUse );

        } catch ( XMLStreamException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "CRSIdentifiable",
                                                                      parent, e.getMessage() ), e );
        }
    }

    private void openReader() {
        try {
            synchronized ( LOCK ) {
                configStream = ProxyUtils.openURLConnection( configURL).getInputStream();
                this.configReader = XMLInputFactory.newInstance().createXMLStreamReader( configURL.toExternalForm(),
                                                                                         this.configStream );
                // move from start document.
                StAXParsingHelper.nextElement( configReader );
                if ( !expectedRootName().equals( configReader.getName() ) ) {
                    LOG.error( "The root element of the crs configuration at location: " + configURL
                               + " is not the expected: " + expectedRootName() + " is your configuration correct?" );
                }
            }
        } catch ( XMLStreamException e ) {
            LOG.debug( "Could not read config file from url: " + configURL + ", stack trace.", e );
            LOG.error( "Could not read config file from url: " + configURL + " because: " + e.getLocalizedMessage() );
        } catch ( FactoryConfigurationError e ) {
            LOG.debug( "Could not read config file from url: " + configURL + ", stack trace.", e );
            LOG.error( "Could not read config file from url: " + configURL + " because: " + e.getLocalizedMessage() );
        } catch ( IOException e ) {
            LOG.debug( "Could not read config file from url: " + configURL + ", stack trace.", e );
            LOG.error( "Could not read config file from url: " + configURL + " because: " + e.getLocalizedMessage() );
        }

    }

    /**
     * Parses a unit from the given xml-parent.
     * 
     * @param reader
     *            xml-reader to parse the unit from.
     * @param required
     *            if the unit is required.
     * 
     * @return the unit object or null if not required and not found.
     * @throws CRSConfigurationException
     *             if the unit object could not be created.
     */
    protected Unit parseUnit( XMLStreamReader reader, boolean required )
                            throws CRSConfigurationException {
        String unitId = null;
        if ( !reader.isStartElement() ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NOT_START_STAX_ERROR",
                                                                      "CRSIdentifiable" ) );
        }
        try {
            if ( required ) {
                unitId = StAXParsingHelper.getRequiredText( reader, new QName( CRS_NS, "Units" ), true );
            } else {
                unitId = StAXParsingHelper.getText( reader, new QName( CRS_NS, "Units" ), null, true );
                if ( unitId == null ) {
                    return null;
                }
            }
        } catch ( XMLStreamException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "Units", e.getMessage() ),
                                                 e );
        }
        Unit result = getProvider().getCachedIdentifiable( Unit.class, unitId );
        if ( result == null ) {
            result = Unit.createUnitFromString( unitId );
            if ( result == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "Units",
                                                                          "unknown unit: " + unitId ) );
            }
        }
        return result;
    }

    /**
     * @return the provider used for reversed look ups, will never be <code>null</code>
     */
    public DeegreeCRSProvider<StAXResource> getProvider() {
        return provider;
    }

    /**
     * Forwards the stream
     * 
     * @param reader
     *            to forward
     * @param elementName
     * @return true if the stream is pointing to an element with the given qname.
     * @throws XMLStreamException
     */
    public boolean moveReaderToNextIdentifiable( XMLStreamReader reader, QName elementName )
                            throws XMLStreamException {
        return moveReaderToFirstMatch( reader, elementName );
    }

    /**
     * Post: reader will be at {@link XMLStreamConstants#START_ELEMENT} of the next element.
     * 
     * @param reader
     *            to be used.
     * @param qName
     *            name of the element
     * @param required
     *            if true and the element is missing an exception will be thrown.
     * @param defaultValue
     * @return the value of the lat lon type or 0 if the element was not defined (and was not required.)
     * @throws XMLStreamException
     */
    protected double parseLatLonType( XMLStreamReader reader, QName qName, boolean required, double defaultValue )
                            throws XMLStreamException {
        double result = defaultValue;
        if ( reader.isStartElement() && qName.equals( reader.getName() ) ) {
            boolean inDegrees = StAXParsingHelper.getAttributeValueAsBoolean( reader, null, "inDegrees", true );
            result = StAXParsingHelper.getElementTextAsDouble( reader, qName, defaultValue, true );
            result = ( !Double.isNaN( result ) && result != 0 && inDegrees ) ? Math.toRadians( result ) : result;
        } else {
            if ( required ) {
                throw new XMLParsingException( reader, Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", qName ) );
            }
        }
        return result;
    }

    /**
     * Forwards the stream
     * 
     * @param reader
     *            to forward
     * @param allowedElements
     * @return true if the stream is pointing to an element with the given qname.
     * @throws XMLStreamException
     */
    public boolean moveReaderToNextIdentifiable( XMLStreamReader reader, Set<QName> allowedElements )
                            throws XMLStreamException {
        return StAXParsingHelper.moveReaderToFirstMatch( reader, allowedElements );
    }

    /**
     * @return true if the entire file has been read, false otherwise.
     */
    public boolean readEntireFile() {
        return this.readEntireFile;
    }

    /**
     * @return the configuration url.
     */
    protected URL getConfigURL() {
        return this.configURL;
    }

}
