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

import static org.deegree.commons.xml.stax.StAXParsingHelper.getRequiredText;
import static org.deegree.commons.xml.stax.StAXParsingHelper.moveReaderToFirstMatch;
import static org.deegree.commons.xml.stax.StAXParsingHelper.nextElement;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.Parser;
import org.deegree.client.mdeditor.model.CodeList;
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

    private static QName ROOT = new QName( NS, "CodeListConfiguration" );

    private static QName CODELIST_ELEMENT = new QName( NS, "CodeList" );

    private List<CodeList> codeLists = new ArrayList<CodeList>();

    CodeListConfiguration parseConfiguration( String configurationURL )
                            throws ConfigurationException {
        try {
            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                             new FileReader(
                                                                                                             configurationURL ) );
            if ( !moveReaderToFirstMatch( xmlStream, ROOT ) ) {
                throw new ConfigurationException( "could not parse code list configuration" + configurationURL
                                                  + ": root element does not exist" );
            }

            while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( ROOT ) ) ) {
                QName elementName = xmlStream.getName();
                if ( CODELIST_ELEMENT.equals( elementName ) ) {
                    parseCodeList( xmlStream );
                } else {
                    nextElement( xmlStream );
                }
            }

            return new CodeListConfiguration( codeLists );
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private void parseCodeList( XMLStreamReader xmlStream )
                            throws XMLStreamException, ConfigurationException {
        String clId = getId( xmlStream );
        nextElement( xmlStream );
        CodeList cl = new CodeList( clId );
        LOG.debug( "Found CodeList with id " + clId );
        while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( CODELIST_ELEMENT ) ) ) {
            if ( xmlStream.isStartElement() && new QName( NS, "Code" ).equals( xmlStream.getName() ) ) {
                nextElement( xmlStream );
                String value = getRequiredText( xmlStream, new QName( NS, "value" ), true );
                String label = getRequiredText( xmlStream, new QName( NS, "label" ), true );
                cl.addCode( value, label );
            } else {
                nextElement( xmlStream );
            }
        }
        LOG.debug( "Found " + cl.getCodes().size() + " codes for codeList " + clId );
        codeLists.add( cl );
        nextElement( xmlStream );
    }
}
