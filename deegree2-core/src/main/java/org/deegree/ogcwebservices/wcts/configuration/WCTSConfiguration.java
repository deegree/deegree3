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

package org.deegree.ogcwebservices.wcts.configuration;

import java.util.Timer;
import java.util.TimerTask;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.ogcwebservices.wcts.capabilities.Content;
import org.deegree.ogcwebservices.wcts.capabilities.WCTSCapabilities;
import org.deegree.owscommon_1_1_0.OperationsMetadata;
import org.deegree.owscommon_1_1_0.ServiceIdentification;
import org.deegree.owscommon_1_1_0.ServiceProvider;

/**
 * <code>WCTSConfiguration</code> holds some configuration options as well as a timer to rebuild the contents of the
 * capabilities.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class WCTSConfiguration extends WCTSCapabilities {

    final static ILogger LOG = LoggerFactory.getLogger( WCTSConfiguration.class );

    /**
     * 
     */
    private static final long serialVersionUID = 3906808063695620386L;

    private final WCTSDeegreeParams deegreeParams;

    /**
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param contents
     * @param deegreeParams
     *            which will hold this service specific parameters.
     */
    public WCTSConfiguration( String version, String updateSequence, ServiceIdentification serviceIdentification,
                              ServiceProvider serviceProvider, OperationsMetadata operationsMetadata, Content contents,
                              final WCTSDeegreeParams deegreeParams ) {
        super( version, updateSequence, serviceIdentification, serviceProvider, operationsMetadata, contents );
        this.deegreeParams = deegreeParams;
    }

    /**
     * @param capabilities
     * @param deegreeParams
     */
    public WCTSConfiguration( WCTSCapabilities capabilities, final WCTSDeegreeParams deegreeParams ) {
        super( capabilities );
        this.deegreeParams = deegreeParams;
        if ( deegreeParams.getUpdateSequence() > -1 ) {
            // set to one minute if smaller
            Timer t = new Timer();
            t.scheduleAtFixedRate( new TimerTask() {

                @Override
                public void run() {
                    LOG.logInfo( "Refreshing contents from cache (as set in the wcts updateSequence configuration document)." );
                    Content c = getContents();
                    c.updateFromProvider( deegreeParams.getConfiguredCRSProvider() );
                }
            }, 0, deegreeParams.getUpdateSequence() );
        }

    }

    /**
     * @return the deegreeParams.
     */
    public final WCTSDeegreeParams getDeegreeParams() {
        return deegreeParams;
    }

}
