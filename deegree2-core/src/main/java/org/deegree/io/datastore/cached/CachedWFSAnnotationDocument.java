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

package org.deegree.io.datastore.cached;

import java.net.MalformedURLException;
import java.net.URL;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.datastore.AnnotationDocument;
import org.w3c.dom.Element;

/**
 * Handles the annotation parsing for datastores that caches feature instances in memory.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CachedWFSAnnotationDocument extends AnnotationDocument {

    private static final long serialVersionUID = -2684991587989405351L;

    private static final ILogger LOG = LoggerFactory.getLogger( CachedWFSAnnotationDocument.class );

    @Override
    public CachedWFSDatastoreConfiguration parseDatastoreConfiguration()
                            throws XMLParsingException {
        Element appinfoElement = (Element) XMLTools.getRequiredNode( getRootElement(), "xs:annotation/xs:appinfo",
                                                                     nsContext );
        QualifiedName ft = XMLTools.getRequiredNodeAsQualifiedName( appinfoElement, "deegreewfs:FeatureType/text()",
                                                                    nsContext );

        String tmp = XMLTools.getRequiredNodeAsString( appinfoElement, "deegreewfs:SchemaLocation/text()", nsContext );
        URL url = null;
        try {
            url = resolve( tmp );
        } catch ( MalformedURLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( e.getMessage(), e );
        }
        return new CachedWFSDatastoreConfiguration( ft, url );
    }
}
