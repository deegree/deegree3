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

package org.deegree.portal.standard.wfs.control;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Parameter;
import org.deegree.framework.util.ParameterList;
import org.deegree.i18n.Messages;
import org.deegree.portal.context.AbstractFrontend;
import org.deegree.portal.context.GeneralExtension;
import org.deegree.portal.context.Module;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.standard.wfs.WFSClientException;
import org.deegree.portal.standard.wfs.configuration.DigitizerClientConfiguration;

/**
 * TODO describe function and usage of the class here.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class InitDigitizerModuleListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( InitDigitizerModuleListener.class );

    private final String moduleName = "DigitizerModule";

    private static final String DIGITIZER_CLIENT_CONFIGURATION = "DIGITIZER_CLIENT_CONFIGURATION";

    private static final String FEATURE_TYPES = "featureTypes";

    private static final String WFS_ADDRESSES = "wfsAddresses";

    private static final String FORM_TEMPLATES = "formTemplates";

    private static final String WFS_INSERT_TEMPLATES = "wfsInsertTemplates";

    private static final String WFS_UPDATE_TEMPLATES = "wfsUpdateTemplates";

    private static final String WFS_DELETE_TEMPLATES = "wfsDeleteTemplates";

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.enterprise.control.AbstractListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession();
        ViewContext vc = (ViewContext) session.getAttribute( org.deegree.portal.Constants.CURRENTMAPCONTEXT );
        GeneralExtension ge = vc.getGeneral().getExtension();

        Module module = null;
        DigitizerClientConfiguration config = new DigitizerClientConfiguration();

        try {
            module = findDigitizerModule( ge );
            initConfig( config, module );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            gotoErrorPage( Messages.getMessage( "IGEO_STD_CLIENT_ERROR", e.getLocalizedMessage() ) );
            return;
        }

        session.setAttribute( DIGITIZER_CLIENT_CONFIGURATION, config );
    }

    /**
     * Gets the first digitizer module (search order: north-east-south-west-center).
     *
     * @param ge
     * @return the (first) digitizer module
     * @throws WFSClientException
     *             if no digitizer module can be found in the passed ge.
     */
    private Module findDigitizerModule( GeneralExtension ge )
                            throws WFSClientException {

        AbstractFrontend fe = (AbstractFrontend) ge.getFrontend();
        Module[] modules = fe.getModulesByName( moduleName );
        if ( modules.length > 0 ) {
            return modules[0];
        } else {
            throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_MISSING_MODULE", moduleName ) );
        }
    }

    /**
     * Initialisation of module configuration. The fields featureTypeToAddressMap and featureTypeToTemplateMap in
     * <code>config</code> are initialized with the values from the passed module. If the module configuration is
     * invalid, an exception is thrown.
     *
     * @param config
     *            the client configuration object
     * @param module
     *            the module as configured in the current map context.
     * @throws WFSClientException
     *             if the module configuration is invalid.
     */
    private void initConfig( DigitizerClientConfiguration config, Module module )
                            throws WFSClientException {

        ParameterList paramList = module.getParameter();
        String[] wfsAddrs = null;
        String[] forms = null;
        String[] insertTempls = null;
        String[] fTypes = null;

        fTypes = extractMandatoryMultiValues( paramList, FEATURE_TYPES );
        wfsAddrs = extractMandatoryMultiValues( paramList, WFS_ADDRESSES );
        forms = extractMandatoryMultiValues( paramList, FORM_TEMPLATES );
        insertTempls = extractMandatoryMultiValues( paramList, WFS_INSERT_TEMPLATES );

        validateConfig( fTypes, wfsAddrs, forms, insertTempls );

        QualifiedName[] qualiNames = createQualifiedNamesFromFeatureTypes( fTypes );

        for ( int i = 0; i < fTypes.length; i++ ) {
            config.addFeatureTypeAddress( qualiNames[i], wfsAddrs[i] );
            config.addFeatureTypeFormTemplate( qualiNames[i], forms[i] );
            config.addFeatureTypeInsertTemplate( qualiNames[i], insertTempls[i] );
        }

        initOptionalConfig( config, module );
    }

    /**
     * @param config
     * @param module
     * @throws WFSClientException
     */
    private void initOptionalConfig( DigitizerClientConfiguration config, Module module )
                            throws WFSClientException {

        ParameterList paramList = module.getParameter();
        // String[] wfsAddrs = null;
        // String[] forms = null;
        String[] updateTemplates = null;
        String[] deleteTemplates = null;
        String[] fTypes = null;

        fTypes = extractMandatoryMultiValues( paramList, FEATURE_TYPES );
        updateTemplates = extractOptionalMultiValues( paramList, WFS_UPDATE_TEMPLATES );
        deleteTemplates = extractOptionalMultiValues( paramList, WFS_DELETE_TEMPLATES );

        if ( updateTemplates != null ) {
            // validate updateTemplate(s)
            for ( int i = 0; i < updateTemplates.length; i++ ) {
                StringBuffer template = new StringBuffer( 10000 );
                String path = getHomePath() + updateTemplates[i];
                try {
                    BufferedReader br = new BufferedReader( new FileReader( path ) );
                    String line = null;
                    while ( ( line = br.readLine() ) != null ) {
                        template.append( line );
                    }
                    br.close();
                } catch ( IOException e ) {
                    LOG.logError( e.getLocalizedMessage(), e );
                    throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_WRONG_TEMPLATE",
                                                                       updateTemplates[i] ) );
                }
            }
        }
        if ( deleteTemplates != null ) {
            // validate deleteTemplate(s)
            for ( int i = 0; i < deleteTemplates.length; i++ ) {
                StringBuffer template = new StringBuffer( 10000 );
                String path = getHomePath() + deleteTemplates[i];
                try {
                    BufferedReader br = new BufferedReader( new FileReader( path ) );
                    String line = null;
                    while ( ( line = br.readLine() ) != null ) {
                        template.append( line );
                    }
                    br.close();
                } catch ( IOException e ) {
                    LOG.logError( e.getLocalizedMessage(), e );
                    throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_WRONG_TEMPLATE",
                                                                       deleteTemplates[i] ) );
                }
            }
        }
        QualifiedName[] qualiNames = createQualifiedNamesFromFeatureTypes( fTypes );

        for ( int i = 0; i < fTypes.length; i++ ) {
            if ( updateTemplates != null ) {
                config.addFeatureTypeUpdateTemplate( qualiNames[i], updateTemplates[i] );
            }
            if ( deleteTemplates != null ) {
                config.addFeatureTypeDeleteTemplate( qualiNames[i], deleteTemplates[i] );
            }
        }
    }

    /**
     * @param paramList
     * @param parameter
     * @return Returns a String[] containing all values (separated by ";") for the passed parameter.
     * @throws WFSClientException
     *             if the mandatory parameter is not part of the parameter list.
     */
    private String[] extractMandatoryMultiValues( ParameterList paramList, String parameter )
                            throws WFSClientException {
        String multiValues = null;
        try {
            multiValues = (String) paramList.getParameter( parameter ).getValue();
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new WFSClientException( Messages.getMessage( "IGEO_STD_MISSING_MAND_PARAM", parameter ) );
        }
        if ( multiValues.startsWith( "'" ) && multiValues.endsWith( "'" ) ) {
            // strip ' from front and end of string
            multiValues = multiValues.substring( 1, multiValues.length() - 1 );
        }

        return multiValues.split( ";" );
    }

    /**
     * @param paramList
     * @param parameter
     * @return Returns a String[] containing all values (separated by ";") for the passed parameter, or null
     */
    private String[] extractOptionalMultiValues( ParameterList paramList, String parameter ) {

        Parameter param = paramList.getParameter( parameter );
        if ( param == null ) {
            return null;
        }
        String optValues = (String) param.getValue();
        if ( optValues.startsWith( "'" ) && optValues.endsWith( "'" ) ) {
            // strip ' from front and end of string
            optValues = optValues.substring( 1, optValues.length() - 1 );
        }
        return optValues.split( ";" );
    }

    /**
     * @param featureTypes
     * @param wfsAddrs
     * @param formTemplates
     * @param insertTemplates
     * @throws WFSClientException
     */
    private void validateConfig( String[] featureTypes, String[] wfsAddrs, String[] formTemplates,
                                 String[] insertTemplates )
                            throws WFSClientException {

        // validate number of param values
        if ( featureTypes.length != wfsAddrs.length ) {
            throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_WRONG_PARAMS_NUM", FEATURE_TYPES,
                                                               WFS_ADDRESSES ) );
        } else if ( featureTypes.length != formTemplates.length ) {
            throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_WRONG_PARAMS_NUM", FEATURE_TYPES,
                                                               FORM_TEMPLATES ) );
        } else if ( featureTypes.length != insertTemplates.length ) {
            throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_WRONG_PARAMS_NUM", FEATURE_TYPES,
                                                               WFS_INSERT_TEMPLATES ) );
        }

        // validate featureTypes as fully qualified names
        createQualifiedNamesFromFeatureTypes( featureTypes );

        // TODO validate module configuration
        // TODO validate wfsAddress(es).
        // TODO validate formTemplate(s): do they exist in the file system?

        // validate insertTemplate(s)
        for ( int i = 0; i < insertTemplates.length; i++ ) {
            StringBuffer template = new StringBuffer( 10000 );
            String path = getHomePath() + insertTemplates[i];
            try {
                BufferedReader br = new BufferedReader( new FileReader( path ) );
                String line = null;
                while ( ( line = br.readLine() ) != null ) {
                    template.append( line );
                }
                br.close();
            } catch ( IOException e ) {
                LOG.logError( e.getLocalizedMessage(), e );
                throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_WRONG_TEMPLATE", insertTemplates[i] ) );
            }
        }

    }

    /**
     * @param featureTypes
     * @return an array of QualifiedNames for the passed featureTypes
     * @throws WFSClientException
     *             if featureTypes can not be transformed to <code>QualifiedName</code>s.
     */
    private QualifiedName[] createQualifiedNamesFromFeatureTypes( String[] featureTypes )
                            throws WFSClientException {
        QualifiedName[] qualifiedNames = null;
        qualifiedNames = new QualifiedName[featureTypes.length];

        for ( int i = 0; i < featureTypes.length; i++ ) {
            // featureTypes[i]={http://hau.mich.blau.de/gruen}:Farbe
            String ns = featureTypes[i].substring( ( 1 + featureTypes[i].indexOf( "{" ) ),
                                                   featureTypes[i].indexOf( "}:" ) );
            String ftName = featureTypes[i].substring( 2 + featureTypes[i].indexOf( "}:" ) );

            try {
                qualifiedNames[i] = new QualifiedName( null, ftName, new URI( ns ) );
            } catch ( URISyntaxException e ) {
                LOG.logError( e.getLocalizedMessage(), e );
                throw new WFSClientException( Messages.getMessage( "IGEO_STD_WFS_INVALID_NS", featureTypes[i], ns ) );
            }
        }
        return qualifiedNames;
    }

}
