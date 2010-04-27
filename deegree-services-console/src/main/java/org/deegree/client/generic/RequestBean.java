//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.client.generic;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.commons.httpclient.HttpException;
import org.deegree.commons.utils.HttpUtils;
import org.slf4j.Logger;

/**
 * A session scoped bean handling the requests.
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@SessionScoped
public class RequestBean {

    private static final Logger LOG = getLogger( RequestBean.class );

    private String url;

    private String targetUrl;

    private String selectedService;

    private String selectedReqProfile;

    private String selectedRequest;

    private List<String> services;

    private List<String> requestProfiles;

    private List<SelectItem> requests;

    private String request;

    private String response = "Select or enter a request in the upper frame and click the \"SEND\" button. After processing, the service response will be displayed in the lower frame.";

    // SERVICE
    // -- PROFILE
    // ----REQUESTTYPE
    // --------xml
    // --------REQUEST
    private HashMap<String, Map<String, Map<String, List<String>>>> allRequests = new HashMap<String, Map<String, Map<String, List<String>>>>();

    @PostConstruct
    public void init() {

        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        URL url;
        try {
            url = new URL( ctx.getRequestScheme(), ctx.getRequestServerName(), ctx.getRequestServerPort(),
                           ctx.getRequestContextPath() );
            this.url = url.toExternalForm() + "/services";
        } catch ( MalformedURLException e ) {
            LOG.debug( "Constructing the url was a problem..." );
            LOG.trace( "Stack trace:", e );
        }
        setTargetUrl( this.url );

        initRequestMap();

        List<String> services = new ArrayList<String>();
        for ( String service : allRequests.keySet() ) {
            services.add( service );
        }
        this.services = services;
        if ( services.size() > 0 )
            this.selectedService = services.get( 0 );

        setReqestProfiles();
        if ( requestProfiles.size() > 0 )
            this.selectedReqProfile = requestProfiles.get( 0 );

        setRequests();
        if ( requests.size() > 0 ) {
            for ( SelectItem sel : requests ) {
                if ( !( sel instanceof SelectItemGroup ) ) {
                    this.selectedRequest = (String) sel.getValue();
                }
            }
        }
        loadExample();
    }

    public void sendRequest() {
        LOG.debug( "Try to send the following request to " + targetUrl + " : \n" + request );
        if ( targetUrl != null && targetUrl.length() > 0 && request != null && request.length() > 0 ) {
            Map<String, String> header = new HashMap<String, String>();
            InputStream is = new ByteArrayInputStream( request.getBytes() );
            try {
                this.response = HttpUtils.post( HttpUtils.UTF8STRING, targetUrl, is, header );

            } catch ( HttpException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public List<String> getServices() {
        return services;
    }

    public List<String> getRequestProfiles() {
        setReqestProfiles();
        return requestProfiles;
    }

    public List<SelectItem> getRequests() {
        setRequests();
        return requests;
    }

    public void setSelectedService( String selectedService ) {
        LOG.debug( "selected service: " + selectedService );
        this.selectedService = selectedService;
    }

    public String getSelectedService() {
        return selectedService;
    }

    public void setSelectedReqProfile( String selectedReqProfile ) {
        LOG.debug( "selected request profile: " + selectedReqProfile );
        this.selectedReqProfile = selectedReqProfile;
    }

    public String getSelectedReqProfile() {
        return selectedReqProfile;
    }

    public void setSelectedRequest( String selectedRequest ) {
        LOG.debug( "selected request: " + selectedRequest );
        this.selectedRequest = selectedRequest;
    }

    public String getSelectedRequest() {
        return selectedRequest;
    }

    public void setRequest( String request ) {
        this.request = request;
    }

    public String getRequest() {
        loadExample();
        return request;
    }

    public void setTargetUrl( String targetUrl ) {
        this.targetUrl = targetUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getResponse() {
        return response;
    }

    private void initRequestMap() {
        // TODO: replace with user directory, if available
        String realPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath( "/requests" );
        File requestsBaseDir = new File( realPath );
        String[] serviceTypes = requestsBaseDir.list();
        if ( serviceTypes != null && serviceTypes.length > 0 ) {
            Arrays.sort( serviceTypes );
            for ( String serviceType : serviceTypes ) {
                if ( ignoreFile( serviceType ) ) {
                    continue;
                }
                // for each service subdir (wfs, wms, etc.)
                File serviceDir = new File( requestsBaseDir, serviceType );
                String[] profileDirs = serviceDir.list();
                Map<String, Map<String, List<String>>> requestProfiles = new HashMap<String, Map<String, List<String>>>();
                if ( profileDirs != null && profileDirs.length > 0 ) {
                    Arrays.sort( profileDirs );
                    for ( String profile : profileDirs ) {
                        if ( ignoreFile( profile ) ) {
                            continue;
                        }
                        // for each profile subdir (demo, philosopher, etc.)
                        File profileDir = new File( serviceDir, profile );
                        String[] requestTypeDirs = profileDir.list();
                        Map<String, List<String>> requestTypes = new HashMap<String, List<String>>();
                        if ( requestTypeDirs != null && requestTypeDirs.length > 0 ) {
                            Arrays.sort( requestTypeDirs );
                            for ( String requestType : requestTypeDirs ) {
                                if ( ignoreFile( requestType ) ) {
                                    continue;
                                }
                                // for each request type subdir (GetCapabilities, GetFeature, etc.)
                                File requestTypeDir = new File( profileDir, requestType + File.separator + "xml" );
                                String[] requests = requestTypeDir.list( new FilenameFilter() {
                                    public boolean accept( File dir, String name ) {
                                        if ( name.toLowerCase().endsWith( ".xml" ) ) {
                                            return true;
                                        }
                                        return false;
                                    }
                                } );
                                List<String> requestUrls = new ArrayList<String>();
                                if ( requests != null && requests.length > 0 ) {
                                    Arrays.sort( requests );
                                    for ( int l = 0; l < requests.length; l++ ) {
                                        String requestUrl = "requests/" + serviceType + "/" + profile + "/"
                                                            + requestType + "/xml/" + requests[l];
                                        // for each request example
                                        requestUrls.add( requestUrl );
                                    }
                                }
                                requestTypes.put( requestType, requestUrls );
                            }
                        }
                        requestProfiles.put( profile, requestTypes );
                    }
                }
                allRequests.put( serviceType, requestProfiles );
            }
        }
    }

    boolean ignoreFile( String name ) {
        return name.endsWith( "CVS" ) || name.startsWith( ".svn" );
    }

    private void setReqestProfiles() {
        List<String> profiles = new ArrayList<String>();
        if ( selectedService != null ) {
            for ( String s : allRequests.keySet() ) {
                if ( selectedService.equals( s ) ) {
                    for ( String profile : allRequests.get( s ).keySet() ) {
                        profiles.add( profile );
                    }
                }
            }
        }
        this.requestProfiles = profiles;
    }

    private void setRequests() {
        selectedRequest = null;
        List<SelectItem> types = new ArrayList<SelectItem>();
        for ( String s : allRequests.keySet() ) {
            if ( selectedService != null && selectedService.equals( s ) ) {
                for ( String p : allRequests.get( s ).keySet() ) {
                    if ( selectedReqProfile != null && selectedReqProfile.equals( p ) ) {
                        Map<String, List<String>> ts = allRequests.get( s ).get( p );
                        for ( String t : ts.keySet() ) {
                            SelectItem[] urls = new SelectItem[ts.get( t ).size()];
                            int i = 0;
                            for ( String url : ts.get( t ) ) {
                                String fileName = url.substring( url.lastIndexOf( File.separator ) + 1, url.length() );
                                urls[i++] = new SelectItem( url, fileName );
                                if ( selectedRequest == null )
                                    selectedRequest = url;
                            }
                            SelectItemGroup typeGrp = new SelectItemGroup( t, "", false, urls );
                            types.add( typeGrp );
                        }
                    }
                }
            }
        }
        this.requests = types;
    }

    private void loadExample() {
        if ( selectedRequest != null ) {
            LOG.debug( "load request " + selectedRequest );
            String realPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath( selectedRequest );
            try {
                BufferedReader reader = new BufferedReader( new FileReader( realPath ) );
                String req = "";
                String line = null;
                while ( ( line = reader.readLine() ) != null ) {
                    req += line;
                }
                setRequest( req );
            } catch ( FileNotFoundException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // try {
            // String realPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath( selectedRequest );
            // XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( new FileReader( realPath )
            // );
            // StAXOMBuilder builder = new StAXOMBuilder( reader );
            // OMElement rootElement = builder.getDocumentElement();
            //
            // StringWriter writer = new StringWriter();
            // XMLOutputFactory factory = XMLOutputFactory.newInstance();
            // factory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );
            // XMLStreamWriter xmlWriter = new FormattingXMLStreamWriter( factory.createXMLStreamWriter( writer ) );
            //
            // rootElement.serialize( xmlWriter );
            // request = writer.toString();
            // } catch ( FileNotFoundException e ) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch ( XMLStreamException e ) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // } catch ( FactoryConfigurationError e ) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
        }
    }

}
