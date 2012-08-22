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

package org.deegree.portal.context;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * Singleton class capsulating a <code>javax.xml.transform.Transformer</code>. This class is used
 * to transform a map context xml document in a html document used the transformation (xslt)
 * provided.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 *
 */
public class ContextTransformer {

    private static final ILogger LOG = LoggerFactory.getLogger( ContextTransformer.class );

    /**
     * The <code>Transformer</code> object used in the transformation of a map context xml to
     * html.
     */
    protected static Transformer transformer = null;

    private static final ContextTransformer INSTANCE = new ContextTransformer();


    private ContextTransformer() {
        // Forbid instantiation
    }

    private static void initTransformer( InputStream is ) {

        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            transformer = tFactory.newTransformer( new StreamSource( is ) );
        } catch ( TransformerConfigurationException e ) {
            e.printStackTrace();
        } catch ( TransformerFactoryConfigurationError e ) {
            e.printStackTrace();
        }
    }

    /**
     * @return the singleton.
     */
    public static ContextTransformer getInstance() {
        return INSTANCE;
    }

    /**
     * Transforms the context pointed to by <code>context</code> into html using
     * <code>xsltURL</code> (though this is currently fixed; there's really no need to define
     * one's wn xsl).
     *
     * @param xsl
     *            the <code>InputStream</code> containing the xls
     * @param contxt
     *            the <code>InputStream</code> containing the context to be transformed
     * @return the converted string.
     * @throws TransformerException
     */
    public String transformContext( InputStream xsl, InputStream contxt )
                            throws TransformerException {

        StringWriter sw = new StringWriter();

        if ( transformer == null ) { // needs to instantiate transformer
            initTransformer( xsl );
        }
        if ( transformer == null ) {
            LOG.logInfo( "transformer is still null" );
        }
        StreamResult sr = new StreamResult( sw );
        StreamSource sSrc = new StreamSource( contxt );
        transformer.transform( sSrc, sr );

        try {
            sw.close();
        } catch ( IOException e ) {
            LOG.logError( "Unable to close string writer.", e );
        }

        return sw.toString();
    }

    @Override
    public String toString() {
        return transformer.toString();
    }
}
