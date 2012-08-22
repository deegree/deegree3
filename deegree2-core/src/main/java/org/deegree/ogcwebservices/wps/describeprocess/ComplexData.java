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

import java.util.ArrayList;
import java.util.List;

/**
 * ComplexData.java
 *
 * Created on 09.03.2006. 22:40:34h
 *
 * Indicates that this input shall be a complex data structure (such as a GML document), and
 * provides a list of formats and encodings supported for this Input. The value of this ComplexData
 * structure can be input either embedded in the Execute request or remotely accessible to the
 * server. This element also provides a list of formats, encodings, and schemas supported for this
 * output. The client can select from among the identified combinations of formats, encodings, and
 * schemas to specify the form of the output. This allows for complete specification of particular
 * versions of GML, or image formats.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author:wanhoff$
 *
 * @version $Revision$, $Date:20.03.2007$
 */
public class ComplexData {

    /**
     *
     */
    protected List<SupportedComplexData> supportedComplexData;

    /**
     * Reference to the default encoding supported for this input or output. The process will expect
     * input using or produce output using this encoding unless the Execute request specifies
     * another supported encoding. This parameter shall be included when the default Encoding is
     * other than the encoding of the XML response document (e.g. UTF-8). This parameter shall be
     * omitted when there is no Encoding required for this input/output.
     */
    protected String defaultEncoding;

    /**
     * Identifier of the default Format supported for this input or output. The process shall expect
     * input in or produce output in this Format unless the Execute request specifies another
     * supported Format. This parameter shall be included when the default Format is other than
     * text/XML. This parameter is optional if the Format is text/XML.
     */
    protected String defaultFormat;

    /**
     * Reference to the definition of the default XML element or type supported for this input or
     * output. This XML element or type shall be defined in a separate XML Schema Document. The
     * process shall expect input in or produce output conformant with this XML element or type
     * unless the Execute request specifies another supported XML element or type. This parameter
     * shall be omitted when there is no XML Schema associated with this input/output (e.g., a GIF
     * file). This parameter shall be included when this input/output is XML encoded using an XML
     * schema. When included, the input/output shall validate against the referenced XML Schema.
     * Note: If the input/output uses a profile of a larger schema, the server administrator should
     * provide that schema profile for validation purposes.
     */
    protected String defaultSchema;

    /**
     * @param defaultEncoding
     * @param defaultFormat
     * @param defaultSchema
     * @param supportedComplexData
     */
    public ComplexData( String defaultEncoding, String defaultFormat, String defaultSchema,
                        List<SupportedComplexData> supportedComplexData ) {
        this.defaultEncoding = defaultEncoding;
        this.defaultFormat = defaultFormat;
        this.defaultSchema = defaultSchema;
        this.supportedComplexData = supportedComplexData;
    }

    /**
     * @return Returns the supportedComplexData.
     */
    public List<SupportedComplexData> getSupportedComplexData() {
        if ( supportedComplexData == null ) {
            supportedComplexData = new ArrayList<SupportedComplexData>();
        }
        return this.supportedComplexData;
    }

    /**
     * @return Returns the defaultEncoding.
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * @param value
     *            The defaultEncoding to set.
     */
    public void setDefaultEncoding( String value ) {
        this.defaultEncoding = value;
    }

    /**
     * @return the defaultFormat.
     */
    public String getDefaultFormat() {
        return defaultFormat;
    }

    /**
     * @param value
     *            The defaultFormat to set.
     */
    public void setDefaultFormat( String value ) {
        this.defaultFormat = value;
    }

    /**
     * @return the defaultSchema.
     */
    public String getDefaultSchema() {
        return defaultSchema;
    }

    /**
     * @param value
     *            The defaultSchema to set.
     */
    public void setDefaultSchema( String value ) {
        this.defaultSchema = value;
    }

}
