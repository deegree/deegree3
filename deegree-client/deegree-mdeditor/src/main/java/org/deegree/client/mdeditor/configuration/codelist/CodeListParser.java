//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.client.mdeditor.configuration.codelist;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.Parser;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.dictionary.Dictionary;
import org.deegree.gml.dictionary.GMLDictionaryReader;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class CodeListParser extends Parser {

    private static final Logger LOG = getLogger( CodeListParser.class );

    public static Dictionary parseDictionary( URL url )
                            throws ConfigurationException {
        try {
            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.toExternalForm(),
                                                                                             url.openStream() );
            GMLDocumentIdContext idContext = new GMLDocumentIdContext( GMLVersion.GML_32 );
            GMLDictionaryReader reader = new GMLDictionaryReader( GMLVersion.GML_32, xmlStream, idContext );

            while ( !xmlStream.isStartElement() && !( XMLStreamConstants.END_DOCUMENT == xmlStream.getEventType() ) ) {
                xmlStream.next();
            }
            if ( XMLStreamConstants.END_DOCUMENT == xmlStream.getEventType() ) {
                throw new ConfigurationException( "could not parse code list configuration" + url
                                                  + ": root element does not exist" );
            }
            return reader.readDictionary();
        } catch ( Exception e ) {
            LOG.debug( "could not parse code list configuration" + url, e );
            throw new ConfigurationException( "could not parse code list configuration" + url );
        }
    }

}
