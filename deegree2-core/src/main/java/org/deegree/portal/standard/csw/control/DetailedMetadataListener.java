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

package org.deegree.portal.standard.csw.control;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.FormEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.standard.csw.configuration.CSWClientConfiguration;

/**
 * A <code>${type_name}</code> class.<br/> TODO class description
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class DetailedMetadataListener extends OverviewMetadataListener {
    // extends OverviewMetadataListener --> SimpleSearchListener --> AbstractListener

    private static final ILogger LOG = LoggerFactory.getLogger( DetailedMetadataListener.class );

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        // get Metadata from the users sesssion
        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        config = (CSWClientConfiguration) session.getAttribute( Constants.CSW_CLIENT_CONFIGURATION );

        nsContext = CommonNamespaces.getNamespaceContext();

        // get transformation file name
        String fileName = "metaDetails2html.xsl"; // default value
        // FIXME replace format with current value
        String format = "Profiles.ISO19115";
        HashMap<?,?> xslMap = config.getProfileXSL( format );
        if ( xslMap != null ) {
            if ( xslMap.get( "full" ) != null ) {
                fileName = (String) xslMap.get( "full" );
            }
        }
        String pathToXslFile = "file:" + getHomePath() + "WEB-INF/conf/igeoportal/" + fileName;

        Object o = session.getAttribute( SESSION_METADATA );
        if ( o != null ) {
            try {
                handleResult( o, pathToXslFile, "detailed" );
            } catch ( Exception e ) {
                gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_ERROR_HANDLE_RESULT", e.getMessage() ) );
                LOG.logError( e.getMessage(), e );
                return;
            }
        } else {
            // create error message if no metadata object is contained in the users session
            setNextPage( "error.jsp" );
            try {
                getRequest().setAttribute( Constants.MESSAGE, Messages.getMessage( "IGEO_STD_CSW_MISSING_METADATA" ) );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
        }

    }

}
