//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.services.wps.provider.sextante;

import java.util.HashMap;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.gml.GMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be use to write a {@link FeatureCollection}, {@link Feature} for {@link Feature} on a Stream.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class SextanteFeatureCollectionStreamWriter {

    private static final Logger LOG = LoggerFactory.getLogger( SextanteFeatureCollectionStreamWriter.class );

    private final XMLStreamWriter sw;

    private final GMLStreamWriter gmlWriter;

    private boolean firstFeature = true;
    private boolean onlyOne = true;

    private String gmlNSPrefix = "gml";

    private String gmlNS = "http://www.opengis.net/gml";

    public SextanteFeatureCollectionStreamWriter( XMLStreamWriter sw, GMLStreamWriter gmlWriter ) {
        this.sw = sw;
        this.gmlWriter = gmlWriter;
    }

    /**
     * Writes the start element of the feature collection.
     * 
     * @param f
     *            {@link Feature}.
     * @throws XMLStreamException
     */
    private void writeStartElement( Feature f )
                            throws XMLStreamException {

        sw.writeStartDocument();
        
        // determine and set namespaces
        HashMap<String, String> namespaces = SextanteProcesslet.determinePropertyNamespaces( f );
        Set<String> namespaceURIs = namespaces.keySet();
        for ( String uri : namespaceURIs ) {
            String prefix = namespaces.get( uri );
            if ( prefix.equals( gmlNSPrefix ) )
                gmlNS = uri;
            sw.setPrefix( prefix, uri );
        }

        // write start element for FeatureCollection
        sw.writeStartElement( gmlNSPrefix, "FeatureCollection", gmlNS );
    }

    /**
     * Writes a {@link Feature} into a {@link FeatureCollection} on stream.
     * 
     * @param f
     *            {@link Feature}.
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void writeFeature( Feature f )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        if ( f != null ) {

            LOG.info( "WRITE FEATURE" );

            if ( firstFeature ) {
                writeStartElement( f );
                firstFeature = false;
            }

            if(onlyOne){
                onlyOne = false;
           
            
            // write start element for featureMember
            sw.writeStartElement( gmlNSPrefix, "featureMember" ,gmlNS);

            // write Feature
            //gmlWriter.write( f );

            // write end element for featureMember
            sw.writeEndElement();
            }

        }

    }

    /**
     * Closes the Stream.
     * 
     * @throws XMLStreamException
     */
    public void close()
                            throws XMLStreamException {
        // write end element
       //sw.writeEndElement();
       sw.writeEndDocument();
        
       gmlWriter.close();
       sw.close();
    }
}
