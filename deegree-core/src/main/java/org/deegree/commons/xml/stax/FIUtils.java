//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/src/main/java/org/deegree/commons/xml/stax/FormattingXMLStreamWriter.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.commons.xml.stax;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
import com.sun.xml.fastinfoset.vocab.ParserVocabulary;
import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;

/**
 * Provides convenience methods for working with <a href="https://fi.dev.java.net/">Fast Infoset</a> encoded XML.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class FIUtils {

    /**
     * Creates a new {@link StAXDocumentSerializer} (Fast Infoset {@link XMLStreamWriter}) instance.
     * 
     * @param os
     *            binary stream to write to, must not be <code>null</code>
     * @param voc
     *            vocabulary to use, must not be <code>null</code>
     * @param vocUri
     *            uri used for declaring the external vocabulary, must not be <code>null</code> or empty
     * @return binary XML writer, never <code>null</code>
     */
    public static StAXDocumentSerializer getFIWriter( OutputStream os, SerializerVocabulary voc, String vocUri ) {
        SerializerVocabulary sVoc = new SerializerVocabulary();
        voc.setExternalVocabulary( vocUri, sVoc, false );
        StAXDocumentSerializer writer = new StAXDocumentSerializer();
        writer.setVocabulary( voc );
        writer.setOutputStream( os );
        return writer;
    }

    /**
     * Creates a new {@link StAXDocumentParser} (Fast Infoset {@link XMLStreamReader}) instance.
     * 
     * @param is
     *            binary stream to read from, must not be <code>null</code>
     * @param voc
     *            vocabulary to use, must not be <code>null</code>
     * @param vocUri
     *            uri used for declaring the external vocabulary, must not be <code>null</code> or empty
     * @return binary XML reader, never <code>null</code>
     */
    public static StAXDocumentParser getFIReader( InputStream is, ParserVocabulary voc, String vocUri ) {
        Map<String, ParserVocabulary> externalVocabularies = new HashMap<String, ParserVocabulary>();
        externalVocabularies.put( vocUri, voc );
        StAXDocumentParser reader = new StAXDocumentParser( is );
        reader.setExternalVocabularies( externalVocabularies );
        return reader;
    }
}