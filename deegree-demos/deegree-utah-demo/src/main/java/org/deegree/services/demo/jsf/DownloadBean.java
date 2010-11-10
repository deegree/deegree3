//$HeadURL$
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
package org.deegree.services.demo.jsf;

import static org.deegree.commons.utils.io.Zip.unzip;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import org.deegree.commons.utils.ProxyUtils;
import org.deegree.console.ConfigManager;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@ApplicationScoped
public class DownloadBean {

    private static final Logger LOG = getLogger( DownloadBean.class );

    @SuppressWarnings("unused")
    // for the love of jsf
    private boolean dataDownloaded;

    private File dataDir;

    /**
     * 
     */
    public DownloadBean() {
        dataDir = new File( FacesContext.getCurrentInstance().getExternalContext().getRealPath( "/" )
                            + "/WEB-INF/workspace/data" );
    }

    /**
     * @return the property
     */
    public boolean getDataDownloaded() {
        return dataDownloaded = dataDir.exists() && dataDir.isDirectory();
    }

    /**
     * @return '/index'
     */
    public String download() {

        InputStream is = null;
        try {
            dataDir.mkdirs();
            is = ProxyUtils.openURLConnection( new URL( "http://download.deegree.org/data/deegree3/wms/wms.zip" ) ).getInputStream();
            unzip( is, dataDir );
        } catch ( MalformedURLException e ) {
            LOG.trace( "Malformed url:", e );
        } catch ( IOException e ) {
            LOG.warn( "Trouble when downloading the data: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch ( IOException e ) {
                    LOG.trace( "Stack trace:", e );
                }
            }
        }

        new ConfigManager().applyChanges();

        return "/index";
    }

}
