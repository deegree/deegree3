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
package org.deegree.client.mdeditor.mapping;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.client.mdeditor.model.FormField;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class MappingExporter {

    private static final Logger LOG = getLogger( MappingExporter.class );

    public static void export( File file, LinkedHashMap<String, String> mappingElements,
                               Map<String, FormField> formFields ) {
        try {

            // validate mapping elements and create a list for further use
            Map<String, List<QName>> mappingSteps = new LinkedHashMap<String, List<QName>>();
            for ( String formFieldPath : mappingElements.keySet() ) {
                String schemaPath = mappingElements.get( formFieldPath );
                Expr xpath = new BaseXPath( schemaPath, null ).getRootExpr();

                if ( !( xpath instanceof LocationPath ) ) {
                    LOG.debug( "Unable to map schemaPath '" + schemaPath
                               + "': the root expression is not a LocationPath." );
                }

                List<QName> steps = new ArrayList<QName>();
                for ( Object step : ( (LocationPath) xpath ).getSteps() ) {
                    if ( !( step instanceof NameStep ) ) {
                        LOG.debug( "Unable to map schemaPath '" + schemaPath
                                   + "': contains an expression that is not a NameStep." );
                    }
                    NameStep namestep = (NameStep) step;
                    String prefix = namestep.getPrefix();
                    String localPart = namestep.getLocalName();
                    // TODO
                    String namespace = "http://www.namespace.de";
                    steps.add( new QName( namespace, localPart, prefix ) );
                }
                mappingSteps.put( formFieldPath, steps );
            }

            // write!
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            FileOutputStream fos = new FileOutputStream( file );

            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( fos );
            writer.writeStartDocument();

            Iterator<String> it = mappingSteps.keySet().iterator();
            if ( it.hasNext() ) {
                List<QName> currentSteps = mappingSteps.get( it.next() );
                int currentIndex = 0;
                while ( it.hasNext() ) {
                    String ffPath = it.next();
                    if ( formFields.containsKey( ffPath ) ) {

                        List<QName> nextSteps = mappingSteps.get( ffPath );
                        for ( ; currentIndex < currentSteps.size(); currentIndex++ ) {
                            QName qName = currentSteps.get( currentIndex );
                            writer.writeCharacters( "\n" );
                            writer.writeStartElement( qName.getPrefix(), qName.getLocalPart(), qName.getNamespaceURI() );
                        }
                        writer.writeCharacters( formFields.get( ffPath ).getValue().toString() );
                        currentIndex = finishStepsUntilNextCommon( writer, currentSteps, nextSteps );
                        currentSteps = nextSteps;
                    }
                }
            }

            writer.writeEndDocument();
            writer.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( JaxenException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static int finishStepsUntilNextCommon( XMLStreamWriter writer, List<QName> currentSteps, List<QName> nextSteps )
                            throws XMLStreamException {
        int stepsToClose = 0;
        int equalSteps = 0;
        for ( int i = 0; i < currentSteps.size() && i < nextSteps.size(); i++ ) {
            if ( currentSteps.get( i ).equals( nextSteps.get( i ) ) ) {
                equalSteps++;
            } else {
                stepsToClose = currentSteps.size() - equalSteps;
                break;
            }
        }
        for ( int i = 0; i < stepsToClose; i++ ) {
            writer.writeEndElement();
            writer.writeCharacters( "\n" );
        }
        return equalSteps;
    }
}
