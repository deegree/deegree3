//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.tools.metadata.importhandling;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_PUBLICATION_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.DCT_NS;
import static org.deegree.protocol.csw.CSWConstants.DCT_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.DC_NS;
import static org.deegree.protocol.csw.CSWConstants.DC_PREFIX;
import static org.deegree.protocol.wps.WPSConstants.WPS_100_NS;
import static org.deegree.protocol.wps.WPSConstants.WPS_PREFIX;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.persistence.genericmetadatastore.ISOMetadataStore;
import org.deegree.services.controller.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.slf4j.Logger;

/**
 * Abstract class for transforming any service data into a CSW conform managable dataformat.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractDCImporter extends XMLAdapter {
    private static final Logger LOG = getLogger( ISOMetadataStore.class );

    protected OMFactory factory;

    protected static OMNamespace namespaceCSW;

    protected static OMNamespace namespaceDC;

    protected static QName subjectElem;

    protected static QName titleElem;

    protected static QName identifierElem;

    protected static QName abstract_Elem;

    protected static QName modifiedElem;

    protected static QName typeElem;

    protected static QName creatorElem;

    protected static QName publisherElem;

    protected static QName contributorElem;

    protected static QName formatElem;

    protected static QName sourceElem;

    protected static QName languageElem;

    protected static QName relationElem;

    protected static QName bboxElem;

    protected static QName rightsElem;

    protected static String schemaLocation = CSW_202_NS + " " + CSW_202_PUBLICATION_SCHEMA;

    static {
        nsContext = new NamespaceContext( XMLAdapter.nsContext );
        nsContext.addNamespace( WPS_PREFIX, WPS_100_NS );
        nsContext.addNamespace( OWSCapabilitiesXMLAdapter.OWS_PREFIX, OWSCapabilitiesXMLAdapter.OWS110_NS );
        subjectElem = new QName( DC_NS, "subject", DC_PREFIX );
        titleElem = new QName( DC_NS, "title", DC_PREFIX );
        identifierElem = new QName( DC_NS, "identifier", DC_PREFIX );
        abstract_Elem = new QName( DCT_NS, "abstract", DCT_PREFIX );// description, as well??
        modifiedElem = new QName( DCT_NS, "modified", DCT_PREFIX );
        typeElem = new QName( DC_NS, "type", DC_PREFIX );
        creatorElem = new QName( DC_NS, "creator", DC_PREFIX );
        publisherElem = new QName( DC_NS, "publisher", DC_PREFIX );
        contributorElem = new QName( DC_NS, "contributor", DC_PREFIX );
        formatElem = new QName( DC_NS, "format", DC_PREFIX );
        sourceElem = new QName( DC_NS, "source", DC_PREFIX );
        languageElem = new QName( DC_NS, "language", DC_PREFIX );
        relationElem = new QName( DC_NS, "relation", DC_PREFIX );
        // languageElem = new QName( DC_NS, "coverage", DC_PREFIX );
        rightsElem = new QName( DC_NS, "rights", DC_PREFIX );

    }

    public AbstractDCImporter( File file ) {

        super( getB( file ) );
        factory = OMAbstractFactory.getOMFactory();
        namespaceCSW = factory.createOMNamespace( CSW_202_NS, CSW_PREFIX );
        namespaceDC = factory.createOMNamespace( DC_NS, DC_PREFIX );

    }

    private static BufferedInputStream getB( File file ) {
        BufferedInputStream bis = null;
        try {

            bis = new BufferedInputStream( new FileInputStream( file ) );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return bis;
    }

    /**
     * Imports all of the service metadata files.
     * 
     * @param folderUrl
     *            a folder where the data is stored
     * @return
     */
    public OMElement importFile() {

        // try {
        // BufferedInputStream bis;
        // bis = new BufferedInputStream( new FileInputStream( folderUrl ) );
        // InputStreamReader in = new InputStreamReader( bis, "UTF-8" );

        // XMLAdapter ad = new XMLAdapter( bis );
        // rootElement = ad.getRootElement();
        // reads the hole document
        OMElement element = getRequiredElement( rootElement, new XPath( ".", nsContext ) );
        // readXMLFragment( in );
        // System.out.println( list );

        // in.close();

        // } catch ( IOException e ) {
        // e.printStackTrace();
        // }

        return element;

    }

    /**
     * 
     * @param element
     * @param writer
     */
    public void readXMLFragment( OMElement element, XMLStreamWriter writer ) {

        // XMLStreamReader xmlReaderOut;
        if ( element != null ) {
            XMLStreamReader xmlReader = element.getXMLStreamReader();

            try {

                // skip START_DOCUMENT
                xmlReader.nextTag();

                XMLAdapter.writeElement( writer, xmlReader );

                xmlReader.close();

            } catch ( XMLStreamException e ) {
                LOG.debug( "error: " + e.getMessage(), e );
            } catch ( FactoryConfigurationError e ) {
                LOG.debug( "error: " + e.getMessage(), e );
            }
        }
    }

}
