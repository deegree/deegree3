//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/svn_classfile_header_template.xml $
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

package org.deegree.services.wps.input;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference to an input value that is provided by a web-accessible resource.
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: $
 *
 * @version $Revision: $, $Date: $
 */
public class InputReference {

    private static final Logger LOG = LoggerFactory.getLogger( InputReference.class );

    private URL href;

    private boolean usePostMethod;

    private Map<String, String> headers;

    private OMElement postBodyElement;

    private URL postBodyReference;

    /**
     * Creates a new {@link InputReference} that points to a web accessible resource which can be retrieved using a
     * simple HTTP Get operation.
     *
     * @param href
     *            http url of the resource
     * @param headers
     *            extra headers to be sent with the request
     */
    public InputReference( URL href, Map<String, String> headers ) {
        this.href = href;
        this.headers = headers;
    }

    /**
     * Creates a new {@link InputReference} that points to a web accessible resource which can be retrieved using a HTTP
     * Post operation (post body is specified as an {@link OMElement}).
     *
     * @param href
     *            http url of the resource
     * @param headers
     *            extra headers to be sent with the request
     * @param postBodyElement
     *            element that encapsulates the post body
     */
    public InputReference( URL href, Map<String, String> headers, OMElement postBodyElement ) {
        this.href = href;
        this.headers = headers;
        this.postBodyElement = postBodyElement;
        this.usePostMethod = true;
    }

    /**
     * Creates a new {@link InputReference} that points to a web accessible resource which can be retrieved using a HTTP
     * Post operation (post body is specified as an {@link URL} reference).
     *
     * @param href
     *            http url of the resource
     * @param headers
     *            extra headers to be sent with the request
     * @param postBodyReference
     *            URL of the post body content
     */
    public InputReference( URL href, Map<String, String> headers, URL postBodyReference ) {
        this.href = href;
        this.headers = headers;
        this.postBodyReference = postBodyReference;
        this.usePostMethod = true;
    }

    /**
     * Provides an {@link InputStream} for accessing the referenced content.
     *
     * @return {@link InputStream} that provides access to the referenced content
     * @throws IOException
     *             if accessing the referenced content fails
     */
    public InputStream openStream()
                            throws IOException {

        LOG.debug( "Opening HTTP stream for InputReference: " + this );
        InputStream is = null;
        if ( usePostMethod ) {
            LOG.debug( "POST" );
            InputStream postBodyInputStream = null;
            if ( postBodyReference != null ) {
                LOG.debug( "Using post body from: URL '" + postBodyReference + "'" );
                // retrieve the post body (may be a file URL, so don't use HttpUtils here)
                postBodyInputStream = postBodyReference.openStream();
            } else {
                // TODO does this work in all cases?
                OMElement childPostBodyElement = postBodyElement.getFirstElement();
                if ( childPostBodyElement != null ) {
                    String postBodyString = childPostBodyElement.toString();
                    LOG.debug( "Using post body '" + postBodyString + "'" );
                    // TODO what about the encoding here?
                    is = new ByteArrayInputStream( postBodyString.getBytes() );
                    postBodyInputStream = is;
                }
            }
            is = HttpUtils.post( HttpUtils.STREAM, href.toString(), postBodyInputStream, headers );
        } else {
            LOG.debug( "GET" );
            is = HttpUtils.get( HttpUtils.STREAM, href.toString(), headers );
        }
        return is;
    }

    /**
     * Returns the URL of the referenced resource.
     *
     * @return the URL of the referenced resource
     */
    public URL getURL() {
        return href;
    }

    @Override
    public String toString() {
        return "URL='" + href + "', method=" + ( usePostMethod ? "POST" : "GET" ) + ", post content: "
               + ( postBodyElement == null ? "inline" : "from URL '" + postBodyReference + "'" );
    }
}
