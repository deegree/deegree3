//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps.execute;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.services.jaxb.wps.ProcessletOutputDefinition;

/**
 * Definition of a format, encoding, schema, and unit-of-measure for an output to be returned from a process as part of
 * {@link ResponseDocument}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class RequestedOutput {

    private ProcessletOutputDefinition outputType;

    private boolean asReference;

    private String mimeType;

    private String encoding;

    private String schemaURL;

    private String uom;

    private LanguageString title;

    private LanguageString summary;

    /**
     * Creates a new {@link RequestedOutput} instance.
     * 
     * @param outputType
     *            type information of the targeted output parameter (includes the identifier)
     * @param asReference
     *            specifies if this output should be stored by the process as a web-accessible resource
     * @param mimeType
     *            the format requested for this output (e.g., text/xml), or null for the default format
     * @param encoding
     *            the encoding of requested for this output (e.g., UTF-8), or null for the default encoding
     * @param schemaURL
     *            web-accessible XML schema document that defines the content model of the output, may be null
     * @param uom
     *            reference to the unit of measure (if any) requested for this output, may be null
     * @param title
     *            title of the process output parameter, normally available for display to a human
     * @param summary
     *            brief narrative description of the process output parameter, normally available for display to a human
     */
    public RequestedOutput( ProcessletOutputDefinition outputType, boolean asReference, String mimeType,
                            String encoding, String schemaURL, String uom, LanguageString title, LanguageString summary ) {
        this.asReference = asReference;
        this.outputType = outputType;
        this.mimeType = mimeType;
        this.schemaURL = schemaURL;
        this.encoding = encoding;
        this.uom = uom;
        this.title = title;
        this.summary = summary;
    }

    /**
     * Returns the identifier of the output parameter.
     * 
     * @return the identifier of the output parameter
     */
    public CodeType getIdentifier() {
        return new CodeType( outputType.getIdentifier().getValue(), outputType.getIdentifier().getCodeSpace() );
    }

    /**
     * Returns the definition of the output parameter from the process description.
     * 
     * @return the definition of the output parameter
     */
    public ProcessletOutputDefinition getOutputType() {
        return outputType;
    }

    /**
     * Returns whether this output should be stored by the process as a web-accessible resource.
     * 
     * @return true, if this output should be stored by the process as a web-accessible resource, false otherwise
     */
    public boolean getAsReference() {
        return asReference;
    }

    /**
     * Returns the requested format for this output (e.g., text/xml).
     * 
     * @return the requested format for this output, or null if unspecified
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the requested encoding for this output (e.g., UTF-8).
     * 
     * @return the requested encoding for this output, or null if unspecified
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns the location of a web-accessible XML schema document that defines the content model of the output.
     * 
     * @return the location of a web-accessible XML schema document, or null if unspecified
     */
    public String getSchemaURL() {
        return schemaURL;
    }

    /**
     * Returns the reference to the unit of measure requested for this output.
     * 
     * @return the reference to the unit of measure, or null if unspecified
     */
    public String getUom() {
        return uom;
    }

    /**
     * Returns the title of the process output parameter, normally available for display to a human.
     * 
     * @return the title of the process output parameter, null if unspecified
     */
    public LanguageString getTitle() {
        return title;
    }

    /**
     * Returns the abstract of the process output parameter, normally available for display to a human.
     * 
     * @return the abstract of the process output parameter, null if unspecified
     */
    public LanguageString getAbstract() {
        return summary;
    }
}
