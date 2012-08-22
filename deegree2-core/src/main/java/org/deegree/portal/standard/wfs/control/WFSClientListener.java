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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.deegree.enterprise.WebUtils;
import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.portal.context.AbstractFrontend;
import org.deegree.portal.context.GeneralExtension;
import org.deegree.portal.context.Module;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.standard.wfs.WFSClientException;

/**
 * The WFSClientListener 1. receives an RPC request, 2. builds a request to a WFS service using the values given in the
 * RPC request, 3. sends the request to the specified WFS, 4. receives the result from the WFS and 5. forwards the
 * result to the WFSClient.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSClientListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( WFSClientListener.class );

    protected static final String INIT_TARGETSRS = "TARGETSRS";

    protected static final String INIT_XSLT = "XSLT";

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        RPCWebEvent rpcEvent = (RPCWebEvent) event;

        try {
            validateRequest( rpcEvent );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( "Invalid rpc request: \n" + e.getMessage() );

            return;
        }

        try {
            doGetFeature( rpcEvent );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            gotoErrorPage( "doGetFeature not successful: \n" + e.getMessage() );

            return;
        }

    }

    /**
     * TODO check validation: are all elements checked? which elements are mandatory? ...?
     *
     * @param rpcEvent
     * @throws WFSClientException
     */
    private void validateRequest( RPCWebEvent rpcEvent )
                            throws WFSClientException {

        RPCParameter[] params = rpcEvent.getRPCMethodCall().getParameters();
        if ( params.length != 1 ) {

            throw new WFSClientException( "Request/Method Call must contain one parameter, not: " + params.length );
        }

        RPCStruct rpcStruct = (RPCStruct) params[0].getValue();

        if ( rpcStruct.getMember( "MODULE" ) == null ) {

            throw new WFSClientException( "Request does not contain mandatory element MODULE." );
        }

        String tmp = (String) rpcStruct.getMember( "MODULE" ).getValue();
        if ( tmp == null || tmp.length() < 1 ) {

            throw new WFSClientException( "Mandatory element MODULE must be set." );
        }
        try {
            if ( rpcStruct.getMember( "FEATURETYPES" ) != null ) {
                tmp = (String) rpcStruct.getMember( "FEATURETYPES" ).getValue();
                // LOG.logDebug( "FEATURETYPES: " + tmp );
            }
            if ( rpcStruct.getMember( "QUERYTEMPLATE" ) != null ) {
                tmp = (String) rpcStruct.getMember( "QUERYTEMPLATE" ).getValue();
                // LOG.logDebug( "QUERYTEMPLATE: " + tmp );
            }
            if ( rpcStruct.getMember( "FILTER" ) != null ) {
                tmp = (String) rpcStruct.getMember( "FILTER" ).getValue();
                // LOG.logDebug( "FILTER: " + tmp );
            }
            if ( rpcStruct.getMember( "FILTERPROPERTIES" ) != null ) {
                tmp = (String) rpcStruct.getMember( "FILTERPROPERTIES" ).getValue();
                // LOG.logDebug( "FILTERPROPERTIES: " + tmp );
            }
            if ( rpcStruct.getMember( "RESULTFORMAT" ) != null ) {
                tmp = (String) rpcStruct.getMember( "RESULTFORMAT" ).getValue();
                // LOG.logDebug( "RESULTFORMAT: " + tmp );
            }
            if ( rpcStruct.getMember( "XMLNS" ) != null ) {
                tmp = (String) rpcStruct.getMember( "XMLNS" ).getValue();
                // LOG.logDebug( "XMLNS: " + tmp );
            }
            if ( rpcStruct.getMember( "NORMALIZE" ) != null ) {
                Boolean shouldNormalize = (Boolean) rpcStruct.getMember( "NORMALIZE" ).getValue();
                LOG.logDebug( "NORMALIZE: " + shouldNormalize );
            }
            if ( rpcStruct.getMember( "LOCALE" ) != null ) {
                tmp = (String) rpcStruct.getMember( "LOCALE" ).getValue();
                // LOG.logDebug( "LOCALE: " + tmp );
            }
            if ( rpcStruct.getMember( "SESSIONID" ) != null ) {
                tmp = (String) rpcStruct.getMember( "SESSIONID" ).getValue();
                // LOG.logDebug( "SESSIONID: " + tmp );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );

            throw new WFSClientException( "Member of rpc request must not be null or empty: ", e );
        }

    }

    /**
     *
     * @param rpcEvent
     * @throws WFSClientException
     */
    protected void doGetFeature( RPCWebEvent rpcEvent )
                            throws WFSClientException {

        RPCParameter[] params = rpcEvent.getRPCMethodCall().getParameters();
        RPCStruct rpcStruct = (RPCStruct) params[0].getValue();

        // xml namespaces
        String tmp = null;
        RPCMember rpcMember = rpcStruct.getMember( "XMLNS" );
        String[] xmlns = null;
        if ( rpcMember != null ) {
            tmp = (String) rpcMember.getValue();
        }
        if ( tmp == null ) {
            xmlns = new String[0];
        } else {
            xmlns = StringTools.toArray( tmp, ",", true );
        }

        // NORMALIZATION
        // check if the query string needs to be normalized
        Boolean getsNormalized = false;
        rpcMember = rpcStruct.getMember( "NORMALIZE" );
        if ( rpcMember != null ) {
            getsNormalized = (Boolean) rpcMember.getValue();
        }
        // set locale for normalization
        String locale = null;
        if ( getsNormalized ) {
            rpcMember = rpcStruct.getMember( "LOCALE" );
            if ( rpcMember != null ) {
                locale = (String) rpcMember.getValue();
            } else {
                locale = "default";
            }
        }

        // CREATE QUERY
        // featuretypes to query
        rpcMember = rpcStruct.getMember( "FEATURETYPES" );
        tmp = (String) rpcMember.getValue();
        String[] featureTypes = StringTools.toArray( tmp, ",", true );

        String moduleName = (String) rpcStruct.getMember( "MODULE" ).getValue();
        String query = null;

        // use template or filter to build query
        if ( rpcStruct.getMember( "QUERYTEMPLATE" ) != null ) {
            String queryTemplate = (String) rpcStruct.getMember( "QUERYTEMPLATE" ).getValue();
            RPCMember filterProperties = rpcStruct.getMember( "FILTERPROPERTIES" );
            RPCMember sessionId = rpcStruct.getMember( "SESSIONID" );
            try {
                query = createQueryFromTemplate( queryTemplate, moduleName, filterProperties, sessionId,
                                                 getsNormalized, locale );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );

                throw new WFSClientException( "Could not create query from template: " + e.getMessage() );
            }
            getRequest().setAttribute( "QUERYTEMPLATE", queryTemplate );

        } else if ( rpcStruct.getMember( "FILTER" ) != null ) {
            // TODO this is not implemented yet
            String filter = (String) rpcStruct.getMember( "FILTER" ).getValue();
            query = createQueryFromFilter( featureTypes, xmlns, filter, getsNormalized, locale );
        } else {
            // TODO reactivate this option
            // String filter = createFilterFromProperties();
            // query = createQueryFromFilter( featureTypes, xmlns, filter, getsNormalized );
            throw new WFSClientException( "could not create query" );
        }

        // PERFORM QUERY
        // LOG.logDebug( "\n***** QUERY *****\n" + query + "\n" );
        XMLFragment response = performQuery( query, moduleName );
        // LOG.logDebug( "\n***** RESPONSE *****\n" + response.getAsString() + "\n" );

        FeatureCollection fc = null;
        try {
            GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
            doc.setRootElement( response.getRootElement() );
            fc = doc.parse();
        } catch ( Exception e ) {
            // DOMPrinter.printNode( response.getRootElement(), " " );
            LOG.logError( e.getMessage(), e );

            throw new WFSClientException( "Could not build FeatureCollection from xml Document: " + e.getMessage() );
        }
        // TODO replace initParam
        // if ( getInitParam( INIT_TARGETSRS ) != null ) {
        // try {
        // // TODO comment in again when needed
        // fc = transformGeometries( fc );
        // } catch ( Exception e ) {
        // LOG.logError( e.getMessage(), e );
        //
        // throw new WFSClientException( "Could not transform geometries in FeatureCollection." );
        // }
        // }

        String resultFormat = null;
        rpcMember = rpcStruct.getMember( "RESULTFORMAT" );
        if ( rpcMember != null ) {
            resultFormat = (String) rpcMember.getValue();
        }
        if ( !"XML".equals( resultFormat ) && !"FEATURECOLLECTION".equals( resultFormat ) ) {
            throw new WFSClientException( "resultFormat" );
        }

        // WRITE FEATURE RESULTS BACK TO CLIENT
        try {
            writeGetFeatureResult( fc, resultFormat );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );

            throw new WFSClientException( "Could not write getFeatureResult: " + e.getMessage() );
        }

    }

    /**
     * Creates a WFS GetFeature query from a named template and a set of KVP-encoded properties.
     *
     * @param queryTemplate
     * @param moduleName
     * @param filterProperties
     * @param sessionId
     *            the users session id. may be null.
     * @param getsNormalized
     *            if true, the filter string gets normalized
     * @param locale
     *            the locale language defining the normalization rules to choose, e.g. "de"
     * @return Returns the WFS GetFeature query as String.
     * @throws WFSClientException
     */
    private String createQueryFromTemplate( String queryTemplate, String moduleName, RPCMember filterProperties,
                                            RPCMember sessionId, Boolean getsNormalized, String locale )
                            throws WFSClientException {

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( org.deegree.portal.Constants.CURRENTMAPCONTEXT );

        GeneralExtension ge = vc.getGeneral().getExtension();
        AbstractFrontend fe = (AbstractFrontend) ge.getFrontend();
        Module module = null;
        Module[] mods = fe.getModulesByName( moduleName );
        if ( mods.length > 0 ) {
            module = mods[0];
        } else {
            LOG.logError( "no module with the name" + moduleName + "could be found." );

            throw new WFSClientException( "The current map context does not contain the module:" + moduleName );
        }

        queryTemplate = (String) module.getParameter().getParameter( queryTemplate ).getValue();
        if ( queryTemplate.startsWith( "'" ) && queryTemplate.endsWith( "'" ) ) {
            // strip ' from front and end of string
            queryTemplate = queryTemplate.substring( 1, queryTemplate.length() - 1 );
        }

        if ( !( new File( queryTemplate ).isAbsolute() ) ) {
            queryTemplate = getHomePath() + queryTemplate;
            LOG.logDebug( "The template file now has an absolute path: " + queryTemplate );
        }
        StringBuffer template = new StringBuffer( 10000 );
        try {
            BufferedReader br = new BufferedReader( new FileReader( queryTemplate ) );
            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                template.append( line );
            }
            br.close();
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );

            throw new WFSClientException( "could not read query template: " + queryTemplate );
        }
        String query = template.toString();

        // SUBSTITUTE FILTER PROPERTIES
        String tmp = null;
        if ( filterProperties != null ) {
            tmp = (String) filterProperties.getValue(); // tmp={myKey=searchStringXXX}{key2=value2}
        }

        if ( tmp != null ) {
            String[] properties = StringTools.extractStrings( tmp, "{", "}" );

            for ( int i = 0; i < properties.length; i++ ) {
                if ( properties[i].startsWith( "{" ) ) {
                    properties[i] = properties[i].substring( 1, properties[i].length() );
                }
                if ( properties[i].endsWith( "}" ) ) {
                    properties[i] = properties[i].substring( 0, properties[i].length() - 1 );
                }

                String[] kvp = StringTools.toArray( properties[i], "=", false );
                // FIXME instead of using "%" as wildcard: read the wildcard character from the
                // template!
                kvp[1] = StringTools.replace( kvp[1], "XXX", "%", true );

                if ( getsNormalized ) {
                    try {
                        kvp[1] = StringTools.normalizeString( kvp[1], locale );
                    } catch ( Exception e ) {
                        LOG.logError( "the search string of the filter property could not be normalized", e );
                        throw new WFSClientException( e.getMessage(), e );
                    }
                }
                query = StringTools.replace( query, '$' + kvp[0], kvp[1], true );
            }
        }

        // SUBSTITUTE SESSION_ID
        tmp = null;
        if ( sessionId != null ) {
            tmp = (String) sessionId.getValue();
        }
        query = StringTools.replace( query, "$SESSION_ID", tmp, true );

        return query;
    }

    /**
     * Creates a WFS GetFeature query from a OGC filter expression sent from a client.
     *
     * TODO: implement support for getNormalized
     *
     * @param featureTypes
     * @param xmlns
     * @param filter
     * @param getsNormalized
     *            if true, the filter string gets normalized
     * @param locale
     *            the locale language defining the normalization rules to choose, e.g. "de"
     * @return Returns the WFS GetFeature query as String.
     */
    private String createQueryFromFilter( String[] featureTypes, String[] xmlns, String filter, Boolean getsNormalized,
                                          String locale ) {

        // TODO handle SESSIONID if it is part of the rpc request

        // normalize filter if needed
        if ( getsNormalized ) {
            // FIXME remove try-catch.
            // TODO Proper implementation of method doNormalizeFilter().
            try {
                filter = doNormalizeFilter( filter, locale );
            } catch ( Exception e ) {
                LOG.logError( "Could not normalize filter.", e );
            }
        }

        StringBuffer query = new StringBuffer( 20000 );
        String format = "text/xml; subtype=gml/3.1.1";
        String resultType = "results";

        // TODO get values for parameters from rpc Request
        // if ( parameter.get( "OUTPUTFORMAT" ) != null ) {
        // format = (String) parameter.get( "OUTPUTFORMAT" );
        // }
        // if ( parameter.get( "RESULTTYPE" ) != null ) {
        // resultType = (String) parameter.get( "RESULTTYPE" );
        // }

        query.append( "<wfs:GetFeature outputFormat='" ).append( format );
        query.append( " resultType='" ).append( resultType ).append( "' " );

        for ( int i = 0; i < xmlns.length; i++ ) {
            String[] tmp = StringTools.toArray( xmlns[i], "=", false );
            query.append( "xmlns:" ).append( tmp[0] ).append( "='" );
            query.append( tmp[1] ).append( "' " );
        }
        query.append( "xmlns:wfs='http://www.opengis.net/wfs' " );
        query.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
        query.append( "xmlns:gml='http://www.opengis.net/gml' " );
        query.append( ">" );

        query.append( "<wfs:Query " );
        for ( int i = 0; i < featureTypes.length; i++ ) {
            query.append( "typeName='" ).append( featureTypes[i] );
            if ( i < featureTypes.length - 1 ) {
                query.append( "," );
            }
        }
        query.append( "'>" );
        query.append( filter );
        query.append( "</wfs:Query></wfs:GetFeature>" );

        return query.toString();
    }

    // /**
    // * creates an OGC FE filter from a set of KVP-encode properties and logical operations
    // *
    // * @return
    // */
    // private String createFilterFromProperties() {
    // // TODO normalization
    // // TODO handle SESSIONID if it is part of the rpc request
    //
    // String tmp = (String) parameter.get( "FILTERPROPERTIES" );
    // if ( tmp != null ) {
    // String[] properties = StringExtend.extractArray( tmp, "{", "}" );
    // String logOp = (String) parameter.get( "LOGICALOPERATOR" );
    // StringBuffer filter = new StringBuffer( 10000 );
    // filter.append( "<ogc:Filter>" );
    // if ( properties.length > 1 ) {
    // filter.append( "<ogc:" ).append( logOp ).append( '>' );
    // }
    // for ( int i = 0; i < properties.length; i++ ) {
    // String[] prop = StringExtend.extractArray( tmp, "[", "]" );
    // if ( "!=".equals( prop[1] ) || "NOT LIKE".equals( prop[1] ) ) {
    // filter.append( "<ogc:Not>" );
    // }
    // if ( "=".equals( prop[1] ) || "!=".equals( prop[1] ) ) {
    // filter.append( "<ogc:PropertyIsEqualTo>" );
    // filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
    // filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
    // filter.append( "</ogc:PropertyIsEqualTo>" );
    // } else if ( ">=".equals( prop[1] ) ) {
    // filter.append( "<ogc:PropertyIsGreaterThanOrEqualTo>" );
    // filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
    // filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
    // filter.append( "</ogc:PropertyIsGreaterThanOrEqualTo>" );
    // } else if ( ">".equals( prop[1] ) ) {
    // filter.append( "<ogc:PropertyIsGreaterThan>" );
    // filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
    // filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
    // filter.append( "</ogc:PropertyIsGreaterThan>" );
    // } else if ( "<=".equals( prop[1] ) ) {
    // filter.append( "<ogc:PropertyIsLessThanOrEqualTo>" );
    // filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
    // filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
    // filter.append( "</ogc:PropertyIsLessThanOrEqualTo>" );
    // } else if ( "<".equals( prop[1] ) ) {
    // filter.append( "<ogc:PropertyIsLessThan>" );
    // filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
    // filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
    // filter.append( "</ogc:PropertyIsLessThan>" );
    // } else if ( "LIKE".equals( prop[1] ) || "NOT LIKE".equals( prop[1] ) ) {
    // filter.append( "<ogc:PropertyIsLike wildCard='%' singleChar='#' escape='!'>" );
    // filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
    // filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
    // filter.append( "</ogc:PropertyIsLike>" );
    // }
    // if ( "!=".equals( prop[1] ) || "NOT LIKE".equals( prop[1] ) ) {
    // filter.append( "</ogc:Not>" );
    // }
    // }
    // if ( properties.length > 1 ) {
    // filter.append( "</ogc:" ).append( logOp ).append( '>' );
    // }
    // filter.append( "</ogc:Filter>" );
    // return filter.toString();
    // }
    // return "";
    // }

    /**
     * Replace the filter string with a normalized version.
     *
     * @param filter
     * @param locale
     *            the locale language defining the normalization rules to choose, e.g. "de"
     * @return the query with a normalized filter if query cannot be transformed to XMLFragment
     */
    private String doNormalizeFilter( String filter, String locale ) {

        throw new UnsupportedOperationException( "this method is not fully implemented yet" );

        //
        //
        // XMLFragment xmlFilter = new XMLFragment();
        // try {
        // xmlFilter.load( new StringReader( filter ),
        // "http://www.deegree.org/portal/standard/WFSClientListener" );
        // xmlFilter.prettyPrint( System.out );
        // } catch ( Exception e ) {
        // LOG.logError( e.getMessage() );
        // throw new WFSClientException( "Could not transform filter into XMLFragment", e );
        // }
        //
        // //FIXME normalization of filter using the passed locale !
        //
        //
        // return xmlFilter.getAsString();
    }

    /**
     * Performs a GetFeature query against a WFS.
     *
     * The WFS address is defined in the module configuration: The default WFS is given with Parameter Name = WFS. If a
     * different WFS is to be used for a special querytemplate, this is defined by specifying ParameterName =
     * WFS:querytemplate
     *
     * @param query
     * @param moduleName
     * @return Returns the response to a WFS GetFeature request as xml Document.
     * @throws WFSClientException
     */
    private XMLFragment performQuery( String query, String moduleName )
                            throws WFSClientException {

        HttpSession session = ( (HttpServletRequest) this.getRequest() ).getSession( true );
        ViewContext vc = (ViewContext) session.getAttribute( org.deegree.portal.Constants.CURRENTMAPCONTEXT );

        // WFS to contact
        GeneralExtension ge = vc.getGeneral().getExtension();
        AbstractFrontend fe = (AbstractFrontend) ge.getFrontend();
        Module module = null;
        Module[] mods = fe.getModulesByName( moduleName );
        if ( mods.length > 0 ) {
            module = mods[0];
        } else {
            LOG.logError( "no module with the name" + moduleName + "could be found." );

            throw new WFSClientException( "The current map context does not contain the module:" + moduleName );
        }

        // get WFS address for template of current featuretype OR, if not available, the default WFS
        String wfsAddress = null;
        String queryTemplate = (String) getRequest().getAttribute( "QUERYTEMPLATE" );
        if ( module.getParameter().getParameter( "WFS:" + queryTemplate ) != null ) {
            wfsAddress = (String) module.getParameter().getParameter( "WFS:" + queryTemplate ).getValue();
        } else {
            wfsAddress = (String) module.getParameter().getParameter( "WFS" ).getValue();
        }
        if ( wfsAddress == null ) {

            throw new WFSClientException( "WFS is not known by the portal" );
        }
        if ( wfsAddress.startsWith( "'" ) && wfsAddress.endsWith( "'" ) ) {
            // strip ' from front and end of string
            wfsAddress = wfsAddress.substring( 1, wfsAddress.length() - 1 );
        }

        InputStream is = null;
        try {
            StringRequestEntity re = new StringRequestEntity( query, "text/xml", CharsetUtils.getSystemCharset() );
            PostMethod post = new PostMethod( wfsAddress );
            post.setRequestEntity( re );
            post.setRequestHeader( "Content-type", "text/xml;charset=" + CharsetUtils.getSystemCharset() );

            HttpClient client = new HttpClient();
            client = WebUtils.enableProxyUsage( client, new URL( wfsAddress ) );
            client.executeMethod( post );
            is = post.getResponseBodyAsStream();
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );

            throw new WFSClientException( "could not perform query against the WFS." );
        }

        XMLFragment xmlFrag = new XMLFragment();
        try {
            InputStreamReader isr = new InputStreamReader( is, CharsetUtils.getSystemCharset() );
            xmlFrag.load( isr, wfsAddress );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );

            throw new WFSClientException( "could not load xmlFragment. \n" + e.getMessage(), e );
        }
        return xmlFrag;
    }

    /**
     * writes the result into the forwarded request object
     *
     * @param fc
     * @param resultFormat
     *            the format of the result in the servlet request. Can be either 'XML' or 'FEATURECOLLECTION'.
     * @throws WFSClientException
     *             if the feature collection could not be exported to GML.
     * @throws RuntimeException
     *             if the resultFormat is not supported.
     */

    private void writeGetFeatureResult( FeatureCollection fc, String resultFormat )
                            throws WFSClientException {

        if ( "XML".equals( resultFormat ) ) {
            try {
                GMLFeatureCollectionDocument gmlFcDoc = new GMLFeatureAdapter().export( fc );
                getRequest().setAttribute( "RESULT", gmlFcDoc );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );

                throw new WFSClientException( "could not export feature collection as GML", e );
            }
        } else if ( "FEATURECOLLECTION".equals( resultFormat ) ) {
            getRequest().setAttribute( "RESULT", fc );
        } else {
            throw new RuntimeException( "'" + resultFormat + "' is not a supported format." );
        }

    }

}
