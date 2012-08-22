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

package org.deegree.ogcwebservices.wfs.operation;

import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.xml.XMLTools.getStringFragmentAsElement;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureException;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcwebservices.DefaultOGCWebServiceResponse;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.xml.sax.SAXException;

/**
 * <code>GmlResult</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GmlResult extends DefaultOGCWebServiceResponse {
    private static final ILogger LOG = getLogger( GmlResult.class );

    private Feature feature;

    private Geometry geometry;

    private FormatType format;

    /**
     * @param request
     * @param exception
     */
    public GmlResult( OGCWebServiceRequest request, OGCWebServiceException exception ) {
        super( request, exception );
    }

    /**
     * @param request
     * @param feature
     * @param format
     */
    public GmlResult( OGCWebServiceRequest request, Feature feature, FormatType format ) {
        super( request );
        this.feature = feature;
        this.format = format;
    }

    /**
     * @param request
     * @param geometry
     * @param format
     */
    public GmlResult( OGCWebServiceRequest request, Geometry geometry, FormatType format ) {
        super( request );
        this.geometry = geometry;
        this.format = format;
    }

    /**
     * @param out
     * @throws FeatureException
     * @throws IOException
     * @throws GeometryException
     */
    public void writeResult( OutputStream out )
                            throws IOException, FeatureException, GeometryException {
        XMLFragment doc = null;
        if ( feature != null ) {
            try {
                doc = new GMLFeatureAdapter( ( (GetGmlObject) request ).getXLinkDepth() ).export( feature );
            } catch ( XMLException e ) {
                LOG.logError( "Unknown error", e );
            } catch ( SAXException e ) {
                LOG.logError( "Unknown error", e );
            }
        }
        if ( geometry != null ) {
            // the ugly hack is necessary to insert missing namespace bindings such as gml
            StringBuffer sb = GMLGeometryAdapter.export( geometry, ( (GetGmlObject) request ).getObjectId() );
            doc = new XMLFragment();
            try {
                doc.setRootElement( getStringFragmentAsElement( sb.toString() ) );
            } catch ( SAXException e ) {
                LOG.logError( "Unknown error", e );
            }
        }

        try {
            XSLTDocument xslt = new XSLTDocument( format.getOutFilter().toURL() );
            xslt.transform( doc, out );
        } catch ( SAXException e ) {
            LOG.logError( "Unknown error", e );
        } catch ( TransformerException e ) {
            LOG.logError( "Unknown error", e );
        }
    }

}
