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
package org.deegree.framework.util;

/**
 * Helper class to check for supported mime types.
 *
 * @author <a href="mailto:schaefer@lat-lon.de">Axel Schaefer</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 */
public class MimeTypeMapper {

    /**
     * Returns true if the submitted content type is defined by the OGC.
     *
     * @param contentType
     *            mime type to check
     * @return true if the contentType starts with "application/vnd.ogc", false otherwise
     */
    public static boolean isOGCType( String contentType ) {
        return contentType.startsWith( "application/vnd.ogc" );
    }

    /**
     * Returns true if the submitted content type is an image type.
     *
     * @param contentType
     *            mime type to check
     * @return true if the contenttype starts with 'image', false otherwise
     */
    public static boolean isImageType( String contentType ) {
        return contentType.startsWith( "image" );
    }

    /**
     * Returns true if the submitted image content type is known by deegree.
     *
     * @param contentType
     *            mime type to check
     * @return true if the content type denotes an image mime-type known by deegree, false otherwise
     */
    public static boolean isKnownImageType( String contentType ) {
        return contentType.equalsIgnoreCase( "image/jpeg" ) || contentType.equalsIgnoreCase( "image/jpg" )
               || contentType.equalsIgnoreCase( "image/gif" ) || contentType.equalsIgnoreCase( "image/tif" )
               || contentType.equalsIgnoreCase( "image/tiff" ) || contentType.equalsIgnoreCase( "image/bmp" )
               || contentType.equalsIgnoreCase( "image/svg+xml" ) || contentType.equalsIgnoreCase( "image/png" )
               || contentType.equalsIgnoreCase( "image/png; mode=8bit" )
               || contentType.equalsIgnoreCase( "image/png; mode=24bit" );
    }

    /**
     * Returns true if the submitted content type is known by the deegree OWS implementations.
     *
     * @param contentType
     *            mime type to check
     * @return true if the mime type is known by deegree, false otherwise
     */
    public static boolean isKnownMimeType( String contentType ) {
        int paramIndex = contentType.indexOf( ";" );
        if ( paramIndex != -1 ) {
            contentType = contentType.substring( 0, paramIndex );
        }
        return contentType.equalsIgnoreCase( "image/jpeg" ) || contentType.equalsIgnoreCase( "image/jpg" )
               || contentType.equalsIgnoreCase( "image/gif" ) || contentType.equalsIgnoreCase( "image/tif" )
               || contentType.equalsIgnoreCase( "image/tiff" ) || contentType.equalsIgnoreCase( "image/png" )
               || contentType.equalsIgnoreCase( "text/html" ) || contentType.equalsIgnoreCase( "text/text" )
               || contentType.equalsIgnoreCase( "text/plain" ) || contentType.equalsIgnoreCase( "text/xml" )
               || contentType.equalsIgnoreCase( "image/bmp" ) || contentType.equalsIgnoreCase( "application/xml" )
               || contentType.equalsIgnoreCase( "application/vnd.ogc.se_xml" )
               || contentType.equalsIgnoreCase( "application/vnd.ogc.se_inimage" )
               || contentType.equalsIgnoreCase( "application/vnd.ogc.se_blank" )
               || contentType.equalsIgnoreCase( "application/vnd.ogc.wms_xml" )
               || contentType.equalsIgnoreCase( "application/vnd.ogc.gml" )
               || contentType.equalsIgnoreCase( "application/vnd.gdinrw.session_xml" )
               || contentType.equalsIgnoreCase( "application/vnd.gdinrw.secure_xml" )
               || contentType.equalsIgnoreCase( "image/svg+xml" );
    }

    /**
     *
     * @param contentType
     *
     * @return true if the submitted content type is a mime type defined by a OGC specification
     */
    public static boolean isKnownOGCType( String contentType ) {
        return contentType.equalsIgnoreCase( "application/xml" )
               || contentType.equalsIgnoreCase( "application/vnd.ogc.se_xml" )
               || contentType.equalsIgnoreCase( "application/vnd.ogc.se_inimage" )
               || contentType.equalsIgnoreCase( "application/vnd.ogc.se_blank" )
               || contentType.equalsIgnoreCase( "application/vnd.ogc.wms_xml" )
               || contentType.equalsIgnoreCase( "application/vnd.gdinrw.session_xml" )
               || contentType.equalsIgnoreCase( "application/vnd.gdinrw.secure_xml" )
               || contentType.equalsIgnoreCase( "application/vnd.ogc.gml" );
    }

    /**
     * maps a 'simple' format name like gif, jpg or text to the corresponding mime type --> e.g. image/gif, image/jpeg
     * or text/plain
     *
     * @param contentType
     *
     * @return the mapped content type.
     */
    public static String toMimeType( String contentType ) {

        String mimetype = "";

        if ( isKnownMimeType( contentType ) ) {
            mimetype = contentType;
        } else {
            if ( contentType.equalsIgnoreCase( "jpeg" ) ) {
                mimetype = "image/jpeg";
            } else if ( contentType.equalsIgnoreCase( "jpg" ) ) {
                mimetype = "image/jpeg";
            } else if ( contentType.equalsIgnoreCase( "gif" ) ) {
                mimetype = "image/gif";
            } else if ( contentType.equalsIgnoreCase( "png" ) ) {
                mimetype = "image/png";
            } else if ( contentType.equalsIgnoreCase( "bmp" ) ) {
                mimetype = "image/bmp";
            } else if ( contentType.equalsIgnoreCase( "tif" ) ) {
                mimetype = "image/tiff";
            } else if ( contentType.equalsIgnoreCase( "tiff" ) ) {
                mimetype = "image/tiff";
            } else if ( contentType.equalsIgnoreCase( "svg" ) ) {
                mimetype = "image/svg+xml";
            } else if ( contentType.equalsIgnoreCase( "xml" ) ) {
                mimetype = "text/xml";
            } else if ( contentType.equalsIgnoreCase( "gml" ) ) {
                mimetype = "text/gml";
            } else if ( contentType.equalsIgnoreCase( "text" ) ) {
                mimetype = "text/plain";
            } else if ( contentType.equalsIgnoreCase( "inimage" ) ) {
                mimetype = "application/vnd.ogc.se_inimage";
            } else if ( contentType.equalsIgnoreCase( "blank" ) ) {
                mimetype = "application/vnd.ogc.se_blank";
            } else if ( contentType.equalsIgnoreCase( "gml" ) ) {
                mimetype = "application/vnd.ogc.gml";
            } else {
                // unknown mimetype
                mimetype = "unknown/unknown";
            }
        }
        return mimetype;
    }
}
