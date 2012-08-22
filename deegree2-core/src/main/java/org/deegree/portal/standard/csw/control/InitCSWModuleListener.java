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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ParameterList;
import org.deegree.i18n.Messages;
import org.deegree.portal.context.AbstractFrontend;
import org.deegree.portal.context.GeneralExtension;
import org.deegree.portal.context.Module;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.standard.csw.CatalogClientException;
import org.deegree.portal.standard.csw.configuration.CSWClientConfiguration;

/**
 * This classes initializes the configurations of the the CSW-module from the WMC to be used by the CSW client
 * 
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InitCSWModuleListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( InitCSWModuleListener.class );

    @Override
    public void actionPerformed( FormEvent event ) {

        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession();
        ViewContext vc = (ViewContext) session.getAttribute( org.deegree.portal.Constants.CURRENTMAPCONTEXT );
        GeneralExtension gen = vc.getGeneral().getExtension();

        Module module = null;

        try {
            module = findCswClientModule( gen );
        } catch ( Exception e ) {
            LOG.logError( "Error in findCswClientModule: " + e.getMessage() );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_CLIENT_ERROR", e.getMessage() ) );
            return;
        }

        CSWClientConfiguration config = new CSWClientConfiguration();

        ParameterList parList = module.getParameter();

        try {
            initConfig( config, parList );
        } catch ( CatalogClientException e ) {
            LOG.logError( "Error when initializing: " + e.getMessage() );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CSW_CLIENT_ERROR", e.getMessage() ) );
            return;
        }

        // srs is available from context
        String srs = "EPSG:4236";
        srs = vc.getGeneral().getBoundingBox()[0].getCoordinateSystem().getIdentifier();
        config.setSrs( srs );

        session.setAttribute( Constants.CSW_CLIENT_CONFIGURATION, config );

        return;
    }

    /**
     * Extracts all the needed configurations from the WMC csw-module and fills the config nstance with them
     * 
     * @param config
     * @param parList
     * @throws CatalogClientException
     */
    protected void initConfig( CSWClientConfiguration config, ParameterList parList )
                            throws CatalogClientException {

        String value = extractOptionalSingleValue( parList, "maxRecords" );
        if ( value != null ) {
            config.setMaxRecords( Integer.valueOf( value ).intValue() );
        }

        String[][] kvp = null;

        String[] profileNames = extractMandatoryProfileNames( parList );
        for ( int i = 0; i < profileNames.length; i++ ) {
            HashMap<String, String> keyToXSL = new HashMap<String, String>();
            String[] profileValues = extractOptionalMultiValues( parList, profileNames[i] );
            kvp = extractKvpFromParamsList( profileValues );
            for ( int j = 0; j < kvp[0].length; j++ ) {
                keyToXSL.put( kvp[0][j], kvp[1][j] ); // elementSetName=kvp[0][j],xslFile=kvp[1][j]
            }
            config.addProfileXSL( profileNames[i], keyToXSL );
        }

        String[] catalogueValues = extractMandatoryMultiValues( parList, "Catalogues" );
        kvp = extractKvpFromParamsList( catalogueValues );
        for ( int i = 0; i < kvp[0].length; i++ ) {
            config.addCatalogueURL( kvp[0][i], kvp[1][i] );
        }

        String[] protocolValues = extractMandatoryMultiValues( parList, "Protocols" );
        kvp = extractKvpFromParamsList( protocolValues );
        for ( int i = 0; i < kvp[0].length; i++ ) {
            String[] protocols = kvp[1][i].split( "," );
            List<String> list = new ArrayList<String>( protocols.length );
            for ( int j = 0; j < protocols.length; j++ ) {
                list.add( protocols[j] );
            }
            config.addCatalogueProtocol( kvp[0][i], list );
        }

        String[] formatValues = extractMandatoryMultiValues( parList, "Formats" );
        kvp = extractKvpFromParamsList( formatValues );
        for ( int i = 0; i < kvp[0].length; i++ ) {
            String[] formats = kvp[1][i].split( "," );
            List<String> list = new ArrayList<String>( formats.length );
            for ( int j = 0; j < formats.length; j++ ) {
                list.add( formats[j] );
            }
            config.addCatalogueFormat( kvp[0][i], list );
        }

        // path to mapContextTemplate
        // is needed for shopping cart, but shopping cart is currently disabled.
        // TODO comment in again, if shopping cart is enabled again.
        // config.setMapContextTemplatePath( extractMandatorySingleValue( parList, "mapContextTemplate" ) );

        // all namspace bindings
        // config.setNamespaceBindings( extractMandatoryMultiValues( parList, "namespaceBindings" ) );

        // xPath in data catalog
        config.setXPathToDataIdentifier( extractMandatorySingleValue( parList, "XPathToDataId" ) );
        config.setXPathToDataTitle( extractMandatorySingleValue( parList, "XPathToDataTitle" ) );

        // xPath in service catalog
        config.setXPathToServiceIdentifier( extractMandatorySingleValue( parList, "XPathToServiceId" ) );
        config.setXPathToServiceTitle( extractMandatorySingleValue( parList, "XPathToServiceTitle" ) );
        config.setXPathToServiceOperatesOnTitle( extractMandatorySingleValue( parList, "XPathToServiceOperatesOnTitle" ) );
        config.setXPathToServiceAddress( extractMandatorySingleValue( parList, "XPathToServiceAddress" ) );
        config.setXPathToServiceType( extractMandatorySingleValue( parList, "XPathToServiceType" ) );
        config.setXPathToServiceTypeVersion( extractMandatorySingleValue( parList, "XPathToServiceTypeVersion" ) );

        /*
         * TODO implement or delete initialBbox String initialBbox = (String)parList.getParameter("InitialBbox"
         * ).getValue(); initialBbox = initialBbox.substring(1, initialBbox.length() - 1 ); Envelope env =
         * createBboxFromString( initialBbox );
         */
    }

    /**
     * Extracts the profile name parameter
     * 
     * @param paramList
     * @return Returns a String[] containing all profile names from the passed parameter list.
     * @throws CatalogClientException
     *             if the mandatory parameter is not part of the parameter list.
     */
    private String[] extractMandatoryProfileNames( ParameterList paramList )
                            throws CatalogClientException {

        String[] paramNames = paramList.getParameterNames();
        List<String> profileNames = new ArrayList<String>( paramNames.length );

        for ( int i = 0; i < paramNames.length; i++ ) {
            if ( paramNames[i].startsWith( "Profiles." ) ) {
                profileNames.add( paramNames[i] );
            }
        }
        if ( profileNames.size() < 1 ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_PROFILE" ) );
        }

        return profileNames.toArray( new String[profileNames.size()] );
    }

    /**
     * Extracts optional single parameter from the WMC csw-client
     * 
     * @param paramList
     * @param parameter
     * @return Returns the value for the passed parameter. May return null, if the parameter is not part of the passed
     *         parameter list.
     */
    private String extractOptionalSingleValue( ParameterList paramList, String parameter ) {

        String value = null;
        if ( paramList.getParameter( parameter ) != null ) {
            value = (String) paramList.getParameter( parameter ).getValue();

            if ( value.startsWith( "'" ) && value.endsWith( "'" ) ) {
                // strip ' from front and end of string
                value = value.substring( 1, value.length() - 1 );
            }
        }
        return value;
    }

    /**
     * Extracts optional comma separated multivalue parameter from the WMC csw-client
     * 
     * @param paramList
     * @param parameter
     * @return Returns a String[] containing all values (separated at ";") for the passed parameter. May return null, if
     *         the parameter is not part of the passed parameter list.
     */
    private String[] extractOptionalMultiValues( ParameterList paramList, String parameter ) {

        String multiValues = null;
        if ( paramList.getParameter( parameter ) != null ) {
            multiValues = (String) paramList.getParameter( parameter ).getValue();

            if ( multiValues.startsWith( "'" ) && multiValues.endsWith( "'" ) ) {
                // strip ' from front and end of string
                multiValues = multiValues.substring( 1, multiValues.length() - 1 );
            }
        }
        return multiValues == null ? null : multiValues.split( ";" );
    }

    /**
     * Extracts a mandatory parameter as a String from the WMC csw-module
     * 
     * @param paramList
     * @param parameter
     * @return Returns the single value for the passed parameter.
     * @throws CatalogClientException
     *             if the mandatory parameter is not part of the parameter list.
     */
    private String extractMandatorySingleValue( ParameterList paramList, String parameter )
                            throws CatalogClientException {
        String value = null;
        try {
            value = (String) paramList.getParameter( parameter ).getValue();
        } catch ( Exception e ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_MAND_PARAM", parameter ) );
        }
        if ( value.startsWith( "'" ) && value.endsWith( "'" ) ) {
            value = value.substring( 1, value.length() - 1 ); // strip ' from front and end of string
        }

        return value;
    }

    /**
     * Extracts comma separated values from the a given parameter in the WMC csw-client
     * 
     * @param paramList
     * @param parameter
     * @return Returns a String[] containing all values (separated by ";") for the passed parameter.
     * @throws CatalogClientException
     *             if the mandatory parameter is not part of the parameter list.
     */
    private String[] extractMandatoryMultiValues( ParameterList paramList, String parameter )
                            throws CatalogClientException {
        String multiValues = null;
        try {
            multiValues = (String) paramList.getParameter( parameter ).getValue();
        } catch ( Exception e ) {
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_MAND_PARAM", parameter ) );
        }
        if ( multiValues.startsWith( "'" ) && multiValues.endsWith( "'" ) ) {
            // strip ' from front and end of string
            multiValues = multiValues.substring( 1, multiValues.length() - 1 );
        }

        return multiValues.split( ";" );
    }

    /**
     * Extract the key/value pair parameters and returns a two dimensional array with their value
     * 
     * @param values
     * @return Returns the key value pairs for the passed values from the parameter list.
     */
    private String[][] extractKvpFromParamsList( String[] values ) {

        String[][] kvp = new String[2][];
        // kvp[0][i] = key[i]
        // kvp[1][i] = value[i]

        kvp[0] = new String[values.length];
        kvp[1] = new String[values.length];

        // FIXME unsafe! assuming AOK, but should catch exceptions for the split at "|"
        for ( int i = 0; i < values.length; i++ ) {
            String[] tmpKVP = values[i].split( "\\|" );
            kvp[0][i] = tmpKVP[0];
            kvp[1][i] = tmpKVP[1];
        }

        return kvp;
    }

    /**
     * Extracts the csw module from the GeneralExtension in the WebMapContext
     * 
     * @param gen
     *            the general extension of the WMC in which to search for a given module
     * @return Returns the (first) csw client module found in one of the GUI areas. Search order is north, east, south,
     *         west, center.
     * @throws CatalogClientException
     *             if the csw client module cannot be found.
     */
    protected Module findCswClientModule( GeneralExtension gen )
                            throws CatalogClientException {
        final String moduleName = "CswModule";

        AbstractFrontend fe = (AbstractFrontend) gen.getFrontend();
        Module[] mods = fe.getModulesByName( moduleName );
        if ( mods.length > 0 ) {
            return mods[0];
        } else {
            LOG.logError( Messages.getMessage( "IGEO_STD_CSW_MISSING_MODULE" ) );
            throw new CatalogClientException( Messages.getMessage( "IGEO_STD_CSW_MISSING_MODULE" ) );
        }
    }
}
