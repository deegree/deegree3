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
package org.deegree.model.coverage.grid;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.parameter.GeneralOperationParameterIm;

/**
 * This interface is a discovery mechanism to determine the formats supported by a
 * {@link "org.opengis.coverage.grid.GridCoverageExchange"} implementation. A
 * <code>GC_GridCoverageExchange</code> implementation can support a number of file format or
 * resources.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Format implements Serializable {

    private static final long serialVersionUID = 3847909077719638612L;

    private String name = null;

    private String description = null;

    private String docURL = null;

    private String vendor = null;

    private String version = null;

    private List<GeneralOperationParameterIm> readParameters = null;

    private List<GeneralOperationParameterIm> writeParameters = null;

    /**
     * Initializes a format with a Code containing a code that will be used as format name and a
     * code space (optional) that will be interpreted as format vendor.
     *
     * @param code
     */
    public Format( Code code ) {
        this.name = code.getCode();
        if ( code.getCodeSpace() != null ) {
            vendor = code.getCodeSpace().toString();
        }
    }

    /**
     * @param description
     * @param docURL
     * @param name
     * @param vendor
     * @param version
     */
    public Format( String name, String description, String docURL, String vendor, String version ) {
        this.description = description;
        this.docURL = docURL;
        this.name = name;
        this.vendor = vendor;
        this.version = version;
    }

    /**
     * @param description
     * @param docURL
     * @param name
     * @param vendor
     * @param version
     * @param readParameters
     * @param writeParameters
     */
    public Format( String name, String description, String docURL, String vendor, String version,
                   GeneralOperationParameterIm[] readParameters, GeneralOperationParameterIm[] writeParameters ) {
        this.description = description;
        this.docURL = docURL;
        this.name = name;
        this.vendor = vendor;
        this.version = version;
        setReadParameters( readParameters );
        setWriteParameters( writeParameters );
    }

    /**
     * @param description
     *            The description to set.
     *
     */
    public void setDescription( String description ) {
        this.description = description;
    }

    /**
     * @param docURL
     *            The docURL to set.
     *
     */
    public void setDocURL( String docURL ) {
        this.docURL = docURL;
    }

    /**
     * @param name
     *            The name to set.
     *
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @param readParameters
     *            The readParameters to set.
     */
    public void setReadParameters( GeneralOperationParameterIm[] readParameters ) {
        if ( readParameters == null )
            readParameters = new GeneralOperationParameterIm[0];
        this.readParameters = Arrays.asList( readParameters );
    }

    /**
     * @param readParameter
     */
    public void addReadParameter( GeneralOperationParameterIm readParameter ) {
        this.readParameters.add( readParameter );
    }

    /**
     * @param vendor
     *            The vendor to set.
     *
     */
    public void setVendor( String vendor ) {
        this.vendor = vendor;
    }

    /**
     * @param version
     *            The version to set.
     *
     */
    public void setVersion( String version ) {
        this.version = version;
    }

    /**
     * @param writeParameters
     *            The writeParameters to set.
     */
    public void setWriteParameters( GeneralOperationParameterIm[] writeParameters ) {
        if ( writeParameters == null )
            writeParameters = new GeneralOperationParameterIm[0];
        this.writeParameters = Arrays.asList( writeParameters );
    }

    /**
     * @param writeParameter
     */
    public void addWriteParameter( GeneralOperationParameterIm writeParameter ) {
        this.readParameters.add( writeParameter );
    }

    /**
     * Name of the file format.
     *
     * @return the name of the file format.
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Description of the file format. If no description, the value will be <code>null</code>.
     *
     * @return the description of the file format.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Vendor or agency for the format.
     *
     * @return the vendor or agency for the format.
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Documentation URL for the format.
     *
     * @return the documentation URL for the format.
     */
    public String getDocURL() {
        return docURL;
    }

    /**
     * Version number of the format.
     *
     * @return the version number of the format.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the information Retrieve the parameter information for a
     *         {@link "org.opengis.coverage.grid.GridCoverageReader#read"} read operation.
     */
    public GeneralOperationParameterIm[] getReadParameters() {
        GeneralOperationParameterIm[] rp = new GeneralOperationParameterIm[readParameters.size()];
        return readParameters.toArray( rp );
    }

    /**
     * Retrieve the parameter information for a org.opengis.coverage.grid.GridCoverageWriter#write
     * operation.
     *
     * @return the parameter information for a org.opengis.coverage.grid.GridCoverageWriter#write
     *         operation.
     *
     */
    public GeneralOperationParameterIm[] getWriteParameters() {
        GeneralOperationParameterIm[] rp = new GeneralOperationParameterIm[writeParameters.size()];
        return writeParameters.toArray( rp );
    }

    /**
     * performs a test if the passed Object is equal to this Format. Two Formats are equal if their
     * names ar equal and (if not null) their vendors and versions are equal.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     * @param obj
     *            object to compare
     */
    @Override
    public boolean equals( Object obj ) {
        if ( obj == null || !( obj instanceof Format ) ) {
            return false;
        }
        Format other = (Format) obj;
        boolean eq = this.getName().equals( other.getName() );
        if ( getVendor() != null && other.getVendor() != null ) {
            eq = eq && getVendor().equals( other.getVendor() );
        } else if ( getVendor() == null && other.getVendor() != null ) {
            return false;
        } else if ( getVendor() != null && other.getVendor() == null ) {
            return false;
        }
        if ( getVersion() != null && other.getVersion() != null ) {
            eq = eq && getVersion().equals( other.getVersion() );
        } else if ( getVersion() == null && other.getVersion() != null ) {
            return false;
        } else if ( getVersion() != null && other.getVersion() == null ) {
            return false;
        }
        return eq;
    }

}
