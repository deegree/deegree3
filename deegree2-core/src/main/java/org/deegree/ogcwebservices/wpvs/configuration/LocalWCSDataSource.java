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

package org.deegree.ogcwebservices.wpvs.configuration;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.CodeList;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.SupportedFormats;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wcs.CoverageOfferingBrief;
import org.deegree.ogcwebservices.wcs.WCService;
import org.deegree.ogcwebservices.wcs.configuration.InvalidConfigurationException;
import org.deegree.ogcwebservices.wcs.configuration.WCSConfiguration;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageDescription;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;
import org.deegree.ogcwebservices.wcs.getcapabilities.ContentMetadata;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilities;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wpvs.capabilities.OWSCapabilities;
import org.xml.sax.SAXException;

/**
 * This class represents a local WCS dataSource object.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 */
public class LocalWCSDataSource extends AbstractDataSource {

    private static final ILogger LOG = LoggerFactory.getLogger( LocalWCSDataSource.class );

    private Color[] transparentColors;

    private double configuredMinimalDGMResolution;

    private static Map<URL, WCSConfiguration> cache = new ConcurrentHashMap<URL, WCSConfiguration>();

    private String defaultFormat;

    // private static WCSConfiguration wcsConfig = null;

    /**
     * Creates a new <code>LocalWCSDataSource</code> object from the given parameters.
     *
     * @param name
     * @param owsCapabilities
     * @param validArea
     * @param minScaleDenominator
     * @param maxScaleDenominator
     * @param filterCondition
     *            a base request //TODO give an example
     * @param transparentColors
     */
    public LocalWCSDataSource( QualifiedName name, OWSCapabilities owsCapabilities, Surface validArea,
                               double minScaleDenominator, double maxScaleDenominator, GetCoverage filterCondition,
                               Color[] transparentColors ) {

        super( LOCAL_WCS, name, owsCapabilities, validArea, minScaleDenominator, maxScaleDenominator, filterCondition );
        this.transparentColors = transparentColors;
        configuredMinimalDGMResolution = 0;
        StringBuilder sb = new StringBuilder( "Couldn't determine default csw format because: " );
        if ( filterCondition != null ) {
            defaultFormat = filterCondition.getOutput().getFormat().getCode();
        }
        if ( defaultFormat == null || "".equals( defaultFormat.trim() ) ) {
            try {
                WCService service = (WCService) getOGCWebService();
                if ( service != null ) {
                    WCSCapabilities caps = (WCSCapabilities) service.getCapabilities();
                    if ( caps != null ) {
                        ContentMetadata md = caps.getContentMetadata();
                        if ( md != null ) {
                            CoverageOfferingBrief[] cobs = md.getCoverageOfferingBrief();
                            if ( cobs != null ) {
                                for ( int i = 0; i < cobs.length && defaultFormat == null; ++i ) {
                                    CoverageOfferingBrief cob = cobs[i];
                                    String cobName = cob.getName();
                                    if ( cobName != null && !"".equals( cobName.trim() ) ) {
                                        if ( name.getLocalName().equals( cobName.trim() ) ) {
                                            LOG.logDebug( "Found a BriefCoverage Offering with the name of this datasource: " + name.getLocalName() );
                                            URL url = cob.getConfiguration();
                                            CoverageDescription cd = CoverageDescription.createCoverageDescription( url );
                                            if ( cd != null ) {
                                                CoverageOffering[] cos = cd.getCoverageOfferings();
                                                if ( cos != null ) {
                                                    for ( int coCount = 0; coCount < cos.length && defaultFormat == null; coCount++ ) {
                                                        CoverageOffering co = cos[coCount];
                                                        if ( co != null ) {
                                                            SupportedFormats sfs = co.getSupportedFormats();
                                                            if ( sfs != null ) {
                                                                Code nativeFormat = sfs.getNativeFormat();
                                                                String nf = null;
                                                                if ( nativeFormat != null ) {
                                                                    nf = nativeFormat.getCode();
                                                                    if ( nf != null ) {
                                                                        nf = nf.trim();
                                                                    }
                                                                }
                                                                CodeList[] cls = sfs.getFormats();
                                                                if ( cls != null ) {
                                                                    for ( int clCount = 0; clCount < cls.length && defaultFormat == null; clCount++ ) {
                                                                        CodeList cl = cls[clCount];
                                                                        if ( cl != null ) {
                                                                            String[] codes = cl.getCodes();
                                                                            if ( codes != null ) {
                                                                                for ( int codeCount = 0; codeCount < codes.length && defaultFormat == null; ++codeCount ) {
                                                                                    String code = codes[codeCount];
                                                                                    if ( code != null && !"".equals( code.trim() ) ) {
                                                                                        if ( code.toLowerCase().contains( "tif" ) ) {
                                                                                            defaultFormat = code;
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }// codelists != null
                                                            }// supportedformats !=null
                                                        } // co !=null
                                                    }// for coverageofferings.
                                                }// coverage offerings != null
                                            }// no CoverageDescriptor
                                        }// name fits
                                    }// name not empty
                                }// for brief coverages
                                if ( defaultFormat == null ) {
                                    sb.append( "No brief coverage Offering found with datasource name: " )
                                      .append( name.getLocalName() );
                                }
                            } else {
                                sb.append( "no Brief Coverage Offerings found." );
                            }
                        } else {
                            sb.append( "no ContentMetadata found." );
                        }
                    } else {
                        sb.append( "no WCSCapabilities found." );
                    }
                } else {
                    sb.append( "no WCService found." );
                }
            } catch ( OGCWebServiceException e ) {
                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    e.printStackTrace();
                }
                sb.append( e.getMessage() );
            } catch ( IOException e ) {
                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    e.printStackTrace();
                }
                sb.append( e.getMessage() );
            } catch ( SAXException e ) {
                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    e.printStackTrace();
                }
                sb.append( e.getMessage() );
            }
        }
        if ( defaultFormat == null ) {
            sb.append( "\nsetting defaultFormat to tiff." );
            LOG.logWarning( sb.toString() );
            defaultFormat = "tiff";
        }
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "Default GetCoverage request format is: " + defaultFormat );
        }
    }

    /**
     * The <code>filterCondition</code> is a GetCoverage object which extends the WCSRequestBase.
     *
     * @return Returns the filterCondition as a GetCoverage object.
     */
    public GetCoverage getCoverageFilterCondition() {
        return (GetCoverage) getFilterCondition();
    }

    /**
     * @return Returns the transparentColors.
     */
    public Color[] getTransparentColors() {
        return transparentColors;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer( super.toString() );

        Color[] colors = getTransparentColors();
        for ( int i = 0; i < colors.length; i++ ) {
            sb.append( "\n color : " ).append( colors[i] );
        }

        GetCoverage filter = getCoverageFilterCondition();
        try {
            sb.append( "\n\t filter : " );
            sb.append( "\n\t  -version : " );
            sb.append( filter.getVersion() );
            sb.append( "\n\t  -id : " );
            sb.append( filter.getId() );
            sb.append( "\n\t  -serviceName : " );
            sb.append( filter.getServiceName() );
            sb.append( "\n\t  -sourceCoverage: " );
            sb.append( filter.getSourceCoverage() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * @throws OGCWebServiceException
     * @see org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource#getOGCWebService()
     */
    @Override
    public OGCWebService getOGCWebService()
                                           throws OGCWebServiceException {
        WCSConfiguration wcsConfig = null;
        synchronized ( this ) {
            URL url = getOwsCapabilities().getOnlineResource();
            wcsConfig = cache.get( url );
            if ( !cache.containsKey( url ) || wcsConfig == null ) {

                URL caps = getOwsCapabilities().getOnlineResource();
                try {
                    wcsConfig = WCSConfiguration.create( caps );
                    cache.put( url, wcsConfig );
                } catch ( InvalidCapabilitiesException e ) {
                    throw new OGCWebServiceException( Messages.getMessage( "WPVS_DATASOURCE_CAP_ERROR", toString() ) + e.getMessage() );
                } catch ( InvalidConfigurationException e ) {
                    throw new OGCWebServiceException( Messages.getMessage( "WPVS_DATASOURCE_CAP_ERROR", toString() ) + e.getMessage() );
                } catch ( IOException e ) {
                    throw new OGCWebServiceException( Messages.getMessage( "WPVS_DATASOURCE_CAP_ERROR", toString() ) + e.getMessage() );
                } catch ( SAXException e ) {
                    throw new OGCWebServiceException( Messages.getMessage( "WPVS_DATASOURCE_CAP_ERROR", toString() ) + e.getMessage() );
                }
            }
            this.notifyAll();
        }
        return new WCService( wcsConfig );
    }

    /**
     * @return the configuredMinimalDGMResolution.
     */
    public double getConfiguredMinimalDGMResolution() {
        return configuredMinimalDGMResolution;
    }

    /**
     * @param configuredMinimalDGMResolution
     *            An other configuredMinimalDGMResolution value.
     */
    public void setConfiguredMinimalDGMResolution( double configuredMinimalDGMResolution ) {
        this.configuredMinimalDGMResolution = configuredMinimalDGMResolution;
    }

    /**
     * @return the defaultFormat to be used for GetCoverage requests
     */
    public final String getDefaultFormat() {
        return defaultFormat;
    }
}
