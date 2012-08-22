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
 * OutputDescription.java
 *
 * Created on 09.03.2006. 22:37:03h
 *
 * Description of a process Output.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author:wanhoff$
 *
 * @version $Revision$, $Date:20.03.2007$
 */
public class OutputDescription extends WPSDescription {

    /**
     * Indicates that this Output shall be a complex data structure (such as a GML fragment) that is
     * returned by the execute operation response. The value of this complex data structure can be
     * output either embedded in the execute operation response or remotely accessible to the
     * client. When this output form is indicated, the process produces only a single output, and
     * "store" is "false, the output shall be returned directly, without being embedded in the XML
     * document that is otherwise provided by execute operation response. This element also provides
     * a list of format, encoding, and schema combinations supported for this output. The client can
     * select from among the identified combinations of formats, encodings, and schemas to specify
     * the form of the output. This allows for complete specification of particular versions of GML,
     * or image formats.
     */
    protected ComplexData complexOutput;

    /**
     * Indicates that this output shall be a simple literal value (such as an integer) that is
     * embedded in the execute response, and describes that output.
     */
    protected LiteralOutput literalOutput;

    /**
     * Indicates that this output shall be a BoundingBox data structure, and provides a list of the
     * CRSs supported in these Bounding Boxes. This element shall be included when this process
     * output is an ows:BoundingBox element.
     */
    protected SupportedCRSs boundingBoxOutput;

    /**
     *
     * @param identifier
     * @param title
     * @param _abstract
     * @param boundingBoxOutput
     * @param complexOutput
     * @param literalOutput
     */
    public OutputDescription( Code identifier, String title, String _abstract,
                              SupportedCRSs boundingBoxOutput, ComplexData complexOutput,
                              LiteralOutput literalOutput ) {
        super( identifier, title, _abstract );
        this.boundingBoxOutput = boundingBoxOutput;
        this.complexOutput = complexOutput;
        this.literalOutput = literalOutput;
    }

    /**
     * @return Returns the complexOutput.
     */
    public ComplexData getComplexOutput() {
        return complexOutput;
    }

    /**
     * @param value
     *            The complexOutput to set.
     */
    public void setComplexOutput( ComplexData value ) {
        this.complexOutput = value;
    }

    /**
     * Gets the value of the literalOutput property.
     *
     * @return the value of the literalOutput property.
     */
    public LiteralOutput getLiteralOutput() {
        return literalOutput;
    }

    /**
     * Sets the value of the literalOutput property.
     *
     * @param value
     */
    public void setLiteralOutput( LiteralOutput value ) {
        this.literalOutput = value;
    }

    /**
     * Gets the value of the boundingBoxOutput property.
     *
     * @return possible object is {@link SupportedCRSs  }
     */
    public SupportedCRSs getBoundingBoxOutput() {
        return boundingBoxOutput;
    }

    /**
     * @param value
     *            The boundingBoxOutput to set.
     */
    public void setBoundingBoxOutput( SupportedCRSs value ) {
        this.boundingBoxOutput = value;
    }

}
