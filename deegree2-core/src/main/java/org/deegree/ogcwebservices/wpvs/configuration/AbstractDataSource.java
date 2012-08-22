//$$HeadURL$$
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

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wpvs.capabilities.OWSCapabilities;

/**
 *
 * NB. this class is very similar to AbstractDataSource from wms
 * TODO -> re-use ? put into common package?
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 *
 */
public abstract class AbstractDataSource {

    /**
     * Identifier for a local wcs
     */
    public static final int LOCAL_WCS = 0;
    /**
     * Identifier for a local wfs
     */
    public static final int LOCAL_WFS = 1;
    /**
     * Identifier for a local wms
     */
    public static final int LOCAL_WMS = 2;
    /**
     * Identifier for a remote wfs
     */
    public static final int REMOTE_WFS = 3;
    /**
     * Identifier for a remote wcs
     */
    public static final int REMOTE_WCS = 4;
    /**
     * Identifier for a remote wms
     */
    public static final int REMOTE_WMS = 5;

    /**
     * The list of supported services
     */
    protected static final String[] SERVICE_TYPE_TO_NAME = {
    	"LOCAL_WCS", "LOCAL_WFS", "LOCAL_WMS", "REMOTE_WFS", "REMOTE_WCS","REMOTE_WMS" };


    private int serviceType ;

    private final QualifiedName name;

    private OWSCapabilities owsCapabilities;

    private Surface validArea;

    private double minScaleDenominator;

    private double maxScaleDenominator;

    private Object filterCondition;



    /**
     * TODO pre-conditions.
     * @param serviceType
     * @param name
     * @param owsCapabilities
     * @param validArea
     * @param minScaleDenominator
     * @param maxScaleDenominator
     * @param filterCondition
     */
    public AbstractDataSource( int serviceType, QualifiedName name, OWSCapabilities owsCapabilities,
    						   Surface validArea, double minScaleDenominator,
    						   double maxScaleDenominator, Object filterCondition ) {

    	setServiceType( serviceType );

        if ( name == null ){
            throw new NullPointerException( "QualifiedName cannot be null.");
        }
        this.name = name;

        this.owsCapabilities = owsCapabilities;
        this.validArea = validArea;

        //TODO min < max?
        this.minScaleDenominator = minScaleDenominator;
        this.maxScaleDenominator = maxScaleDenominator;

        this.filterCondition = filterCondition;
    }

    /**
     * @return Returns the serviceType (WCS, WFS, remote WMS etc...)
     */
    public int getServiceType() {
        return serviceType ;
    }

    /**
     * Sets the type of service. A service type means whether the service is a WFS, WCS, remote WMS,
     *  etc.  Allowed values are LOCAL_WCS, LOCAL_WFS, LOCAL_WMS, REMOTE_WFS, REMOTE_WCS or
     * REMOTE_WMS.
     *
     * @param serviceType the service type.
     * @throws IllegalArgumentException if the serviceType is not of know type
     */
    public void setServiceType( int serviceType ) {
        if ( serviceType < LOCAL_WCS || serviceType > REMOTE_WMS ) {
            throw new IllegalArgumentException("serviceType must be one of: " +
            		"LOCAL_WCS, LOCAL_WFS, LOCAL_WMS, REMOTE_WFS, REMOTE_WCS or " +
            		"REMOTE_WMS");
        }
        this.serviceType = serviceType;
    }

	/**
	 * @return Returns the maxScaleDenominator.
	 */
	public double getMaxScaleDenominator() {
		return maxScaleDenominator;
	}

	/**
	 * @return Returns the minScaleDenominator.
	 */
	public double getMinScaleDenominator() {
		return minScaleDenominator;
	}

	/**
	 * @return Returns the name.
	 */
	public QualifiedName getName() {
		return name;
	}

	/**
	 * @return Returns the owsCapabilities.
	 */
	public OWSCapabilities getOwsCapabilities() {
		return owsCapabilities;
	}

	/**
	 * @return Returns the validArea.
	 */
	public Surface getValidArea() {
		return validArea;
	}

	/**
	 * @return Returns the filterCondition.
	 */
	public Object getFilterCondition() {
		return filterCondition;
	}

    /**
     * Returns an instance of the <code>OGCWebService</code> that represents the
     * datasource. Notice: if more than one datasets uses data that are offered by
     * the same OWS, deegree WPVS will use  just one instance for accessing
     * the OWS
     * @return an OGCWebService which represents this datasource
     * @throws OGCWebServiceException if an error occurs while creating the webservice instance
     *
     */
    public abstract OGCWebService getOGCWebService() throws OGCWebServiceException;

    //protected abstract OGCWebService createOGCWebService();

	@Override
    public String toString(){

		StringBuilder sb = new StringBuilder(512);

		sb.append( "DataSource: ").append( getName() )
			.append( "\n\t serviceType: " ).append( SERVICE_TYPE_TO_NAME[ getServiceType() ] )
			.append( "\n\t minScaleDenominator: " ).append( getMinScaleDenominator() )
			.append( "\n\t maxScaleDenominator: " ).append( getMaxScaleDenominator() )
			.append( "\n\t validArea: " ).append( getValidArea() )
			.append( "\n\t format: " ).append( getOwsCapabilities().getFormat() )
			.append( "\n\t onlineResource: " ).append( getOwsCapabilities().getOnlineResource() );

		return sb.toString();
	}

}
