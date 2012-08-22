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

package org.deegree.portal.standard.csw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.portal.standard.context.control.ContextLoadListener;
import org.xml.sax.SAXException;

/**
 * A <code>${type_name}</code> class.<br/>
 * TODO class description
 * 
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 * 
 * @since 2.0
 */
public class MetadataTransformer {

    private static final ILogger LOG = LoggerFactory.getLogger( ContextLoadListener.class );

    /**
     * The <code>Transformer</code> object used in the transformation of a map context xml to html.
     */
    private Transformer transformer = null;

    private XSLTDocument xslt;

    /**
     * Creates a new MetadataTransformer and initializes it with the given <code>file</code> (path and name).
     * 
     * @param filePathName
     * @throws FileNotFoundException
     *             , if filePathName does not point to an existing file.
     */
    public MetadataTransformer( String filePathName ) throws FileNotFoundException {
        init( filePathName );
    }

    private void init( String filePathName ) {
        try {
            xslt = new XSLTDocument( new File( filePathName ).toURI().toURL() );
        } catch ( Exception e ) {
            LOG.logError( e );
            throw new RuntimeException( e );
        }
    }

    /**
     * Transforms the context pointed to by <code>context</code> into html using <code>xsltURL</code>
     * 
     * @param metadataXml
     *            The <code>Reader</code> containing the xml document to be transformed.
     * @param catalog
     *            The name of the catalog.
     * @param serviceCatalogs
     * @param hits
     *            The number of records matched for this catalog.
     * @param startPosition
     *            The position to start displaying the matched records from.
     * @param metaVersion
     *            The version of metadata to transform ( list, overview, detailed ).
     * @return Returns result of transformation.
     * @throws TransformerException
     * @throws IOException
     */
    public String transformMetadata( Reader metadataXml, String catalog, String[] serviceCatalogs, int hits,
                                     int startPosition, String metaVersion )
                            throws TransformerException, IOException {

        // turn array of Strings into one comma-separated String
        StringBuffer sb = new StringBuffer();
        if ( serviceCatalogs != null ) {
            for ( int i = 0; i < serviceCatalogs.length; i++ ) {
                sb.append( serviceCatalogs[i] );
                if ( i < serviceCatalogs.length - 1 ) {
                    sb.append( "," );
                }
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "CATALOG", catalog );
        params.put( "SERVICECATALOGS", sb.toString() );
        params.put( "HITS", new Integer( hits ) );
        params.put( "STARTPOS", new Integer( startPosition ) );
        params.put( "METAVERSION", metaVersion );

        XMLFragment xml = null;
        try {
            xml = new XMLFragment( metadataXml, XMLFragment.DEFAULT_URL );
        } catch ( SAXException e ) {
            throw new TransformerException( e );
        }

        xml = xslt.transform( xml, XMLFragment.DEFAULT_URL, new Properties(), params );

        return xml.getAsString();
    }

    @Override
    public String toString() {
        return transformer.toString();
    }

}
