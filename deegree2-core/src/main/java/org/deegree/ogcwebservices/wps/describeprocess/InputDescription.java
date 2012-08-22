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
package org.deegree.ogcwebservices.wps.describeprocess;

import org.deegree.datatypes.Code;
import org.deegree.ogcwebservices.wps.WPSDescription;

/**
 * InputDescription.java
 *
 * Created on 09.03.2006. 22:33:58h
 *
 * Description of an input to a process.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author:wanhoff$
 *
 * @version $Revision$, $Date:20.03.2007$
 */
public class InputDescription extends WPSDescription {

    /**
     * Indicates that this input shall be a complex data structure (such as a GML document), and provides a list of
     * formats and encodings supported for this Input. The value of this ComplexData structure can be input either
     * embedded in the Execute request or remotely accessible to the server. This element also provides a list of
     * formats, encodings, and schemas supported for this output. The client can select from among the identified
     * combinations of formats, encodings, and schemas to specify the form of the output. This allows for complete
     * specification of particular versions of GML, or image formats.
     */
    protected ComplexData complexData;

    /**
     * Indicates that this input shall be a simple numeric value or character string that is embedded in the execute
     * request, and describes the possible values.
     */
    protected LiteralInput literalData;

    /**
     * Indicates that this input shall be a BoundingBox data structure that is embedded in the execute request, and
     * provides a list of the CRSs supported for this Bounding Box.
     */
    protected SupportedCRSs boundingBoxData;

    /**
     * The minimum number of times that values for this parameter are required. If MinimumOccurs is "0", this data input
     * is optional. If MinimumOccurs is "1" or if this element is omitted, this process input is required.
     */
    protected int minimumOccurs;

    /**
     *
     * @param identifier
     * @param title
     * @param _abstract
     * @param boundingBoxData
     * @param complexData
     * @param literalData
     * @param occurs
     */
    public InputDescription( Code identifier, String title, String _abstract, SupportedCRSs boundingBoxData,
                             ComplexData complexData, LiteralInput literalData, int occurs ) {
        super( identifier, title, _abstract );
        this.boundingBoxData = boundingBoxData;
        this.complexData = complexData;
        this.literalData = literalData;
        minimumOccurs = occurs;
    }

    /**
     * @return Returns the complexData.
     */
    public ComplexData getComplexData() {
        return complexData;
    }

    /**
     * @param value
     *            The complexData to set.
     */
    public void setComplexData( ComplexData value ) {
        this.complexData = value;
    }

    /**
     * @return Returns the literalData.
     */
    public LiteralInput getLiteralData() {
        return literalData;
    }

    /**
     * @param value
     *            The literalData to set.
     */
    public void setLiteralData( LiteralInput value ) {
        this.literalData = value;
    }

    /**
     * @return Returns the boundingBoxData.
     */
    public SupportedCRSs getBoundingBoxData() {
        return boundingBoxData;
    }

    /**
     * @param value
     *            The boundingBoxData to set.
     */
    public void setBoundingBoxData( SupportedCRSs value ) {
        this.boundingBoxData = value;
    }

    /**
     * @return Returns the minimumOccurs.
     */
    public int getMinimumOccurs() {
        return minimumOccurs;
    }

    /**
     * @param value
     *            The minimumOccurs to set.
     */
    public void setMinimumOccurs( int value ) {
        this.minimumOccurs = value;
    }

    /**
     * @return true if the data contains a bbox
     */
    public boolean isBoundingBoxData() {
        boolean isBoundingBoxData = false;
        if ( null != boundingBoxData ) {
            isBoundingBoxData = true;
        }
        return isBoundingBoxData;
    }

    /**
     * @return true if the data is complex
     */
    public boolean isComplexData() {
        boolean isComplexData = false;
        if ( null != complexData ) {
            isComplexData = true;
        }
        return isComplexData;
    }

    /**
     * @return true if the data is a literal
     */
    public boolean isLiteralData() {
        boolean isLiteralData = false;
        if ( null != literalData ) {
            isLiteralData = true;
        }
        return isLiteralData;
    }
}
