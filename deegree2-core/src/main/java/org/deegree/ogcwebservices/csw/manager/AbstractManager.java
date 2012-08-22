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

package org.deegree.ogcwebservices.csw.manager;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.csw.configuration.CatalogueConfiguration;
import org.deegree.ogcwebservices.csw.configuration.CatalogueDeegreeParams;
import org.deegree.ogcwebservices.csw.manager.HarvestRepository.ResourceType;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
abstract class AbstractManager implements Manager {

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractManager.class );

    protected static Map<ResourceType, AbstractHarvester> harvester = null;

    protected XSLTDocument IN_XSL = null;

    protected XSLTDocument OUT_XSL = null;

    protected WFService wfsService;

    /**
     * starts all known/registered harvester. This method can be used to start harvesting using
     * requests e.g. if a server has been shutdown and restarted.
     * 
     * @param version
     *            the version of the  CSW
     */
    public static void startAllHarvester( String version ) {
        initHarvester( version );
        Collection<AbstractHarvester> con = harvester.values();
        for ( Iterator<AbstractHarvester> iter = con.iterator(); iter.hasNext(); ) {
            AbstractHarvester h = iter.next();
            if ( !h.isRunning() ) {
                h.startHarvesting();
            }
        }
    }

    /**
     * stpos all known/registered harvester. This method can be used to stop harvesting using
     * requests e.g. if a server has shall be shut down.
     * 
     */
    public static void stopAllHarvester() {
        if ( harvester != null ) {
            Collection<AbstractHarvester> con = harvester.values();
            for ( Iterator<AbstractHarvester> iter = con.iterator(); iter.hasNext(); ) {
                AbstractHarvester h = iter.next();
                if ( h.isRunning() ) {
                    LOG.logInfo( "stop harvesting for: " + h.getClass().getName() );
                    h.stopHarvesting();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.ogcwebservices.csw.manager.Manager#getWfsService()
     */
    public WFService getWFService() {
        return wfsService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.ogcwebservices.csw.manager.Manager#init(org.deegree.ogcwebservices.wfs.WFService,
     * org.deegree.ogcwebservices.csw.configuration.CatalogueConfiguration)
     */
    public void init( WFService wfsService, CatalogueConfiguration catalogueConfiguration )
                            throws MissingParameterValueException {
        this.wfsService = wfsService;

        // try {
        CatalogueDeegreeParams cdp = catalogueConfiguration.getDeegreeParams();
        URL url = cdp.getTransformationInputXSLT().getLinkage().getHref();
        IN_XSL = new XSLTDocument();
        try {
            IN_XSL.load( url );
        } catch ( IOException e ) {
            String s = "If a CS-W is defined to handle Transaction and/or Harvest requests, XSLT scripts for request transformations (e.g. mapping the input schema to gml) must be defined in the deegreeParams section of the capabilities document. While trying to read an xslt script from: '"
                       + url.toString()
                       + "' (which was defined for the transformation of incoming request), the following error occurred: "
                       + e.getMessage();
            LOG.logError( s, e );
            throw new MissingParameterValueException( getClass().getName(), s );
        } catch ( SAXException e ) {
            String s = "The xslt script (transforming incoming requests) read from the location: '" + url
                       + "' could not be parsed because: " + e.getMessage();
            LOG.logError( s, e );
            throw new MissingParameterValueException( getClass().getName(), s );
        }
        url = cdp.getTransformationOutputXSLT().getLinkage().getHref();
        OUT_XSL = new XSLTDocument();
        try {
            OUT_XSL.load( url );
        } catch ( IOException e ) {
            String s = "If a CS-W is defined to handle Transaction and/or Harvest requests, XSLT scripts for request transformations (e.g. mapping the input schema to gml) must be defined in the deegreeParams section of the capabilities document. While trying to read an xslt script from: '"
                       + url.toString()
                       + "' (which was defined for the transformation of the response), the following error occurred: "
                       + e.getMessage();
            LOG.logError( s, e );
            throw new MissingParameterValueException( getClass().getName(), s );
        } catch ( SAXException e ) {
            String s = "The xslt script (transforming the response) read from the location: '" + url
                       + "' could not be parsed because: " + e.getMessage();
            LOG.logError( s, e );
            throw new MissingParameterValueException( getClass().getName(), s );
        }
        WFSCapabilities capa = wfsService.getCapabilities();

        List<String> versions = Arrays.asList( catalogueConfiguration.getServiceIdentification().getServiceTypeVersions() );
        Collections.sort( versions );

        initHarvester( versions.get( versions.size() - 1 ) );

        LOG.logInfo( "CSW Manager initialized with WFS resource, wfs version:" + capa.getVersion() );

    }

    /**
     * initializes a static Map containing a harvester for other coatalogues, for services and for
     * single CSW-profile documents.
     * 
     * @param version
     *            the version of the CSW
     */
    static void initHarvester( String version ) {
        if ( harvester == null ) {
            harvester = new HashMap<ResourceType, AbstractHarvester>();
            harvester.put( ResourceType.catalogue, CatalogueHarvester.getInstance( version ) );
            harvester.put( ResourceType.service, ServiceHarvester.getInstance( version ) );
            harvester.put( ResourceType.csw_profile, CSWProfileHarvester.getInstance( version ) );
        }
    }

}
