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

package org.deegree.ogcwebservices.wpvs.capabilities;

import org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource;

/**
 * This class represents an <code>ElevationModel</code> object.
 *
 * This elevation model object may be either an OGC-ElevationModel or a deegree-ElevationModel.
 * The OGC-ElevationModel is created from and contains only a String.
 * The deegree-ElevationModel is created from and contains a String and an AbstractDataSource object.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 */
public class ElevationModel {

	private AbstractDataSource[] dataSources;
	private String name;
    private Dataset parentDataset;

	/**
	 * Creates a new OGC <code>ElevationModel</code> object from name.
	 *
	 * @param name
	 */
	public ElevationModel( String name ) {
		this.name = name;
	}

	/**
	 * Creates a new deegree <code>ElevationModel</code> object from name and dataSources.
	 *
	 * @param name
	 * @param dataSources for this ElevationModel
	 */
	public ElevationModel( String name, AbstractDataSource[] dataSources ) {
		this.name = name;
		this.dataSources = dataSources;
	}

	/**
	 * @return Returns an array of dataSources.
	 */
	public AbstractDataSource[] getDataSources() {
		return dataSources;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

    /**
     * @return the Dataset of this elevationmodel
     */
    public Dataset getParentDataset(){
        return this.parentDataset;
    }

	@Override
    public String toString(){
		return "Elevationmodel Name: " + getName();
	}

    /**
     * @param parentDataset the new parent Dataset
     */
    public void setParentDataset( Dataset parentDataset ) {
        this.parentDataset = parentDataset;
    }
}
