//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.ogcwebservices.wmps.operation;

import java.util.HashMap;
import java.util.Map;

import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.w3c.dom.Element;

/**
 * Can be requested as KVP:
 * <p>
 * ...?request=DescribeTemplate&version=1.0.0&template=MyTemplate
 * </p>
 * or as XML:
 * <p>
 * 
 * <pre>
 * ...
 * </pre>
 * 
 * </p>
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DescribeTemplate extends WMPSRequestBase {

    private static final long serialVersionUID = 536568315562438781L;

    private String template;

    /**
     * 
     * @param id
     * @param version
     * @param template
     * @param vendorSpecificParameter
     */
    public DescribeTemplate( String id, String version, String template, Map<String, String> vendorSpecificParameter ) {
        super( version, id, vendorSpecificParameter );
        this.template = template;
    }

    /**
     * 
     * @param parameter
     * @return new {@link DescribeTemplate}
     * @throws InconsistentRequestException 
     */
    public static DescribeTemplate create( Map<String, String> parameter ) throws InconsistentRequestException{
        String id = "" + System.currentTimeMillis();
        String version = retrieveVersionParameter( parameter );
        String tempplate = retrieveTemplateParameter( parameter );
        return new DescribeTemplate( id, version, tempplate, parameter );
    }

    /**
     * 
     * @param request
     * @return new {@link DescribeTemplate}
     * @throws XMLParsingException
     */
    public static DescribeTemplate create( Element request )
                            throws XMLParsingException {
        String id = "" + System.currentTimeMillis();
        String version;
        try {
            version = XMLTools.getRequiredAttrValue( "version", null, request );
        } catch ( Exception e ) {
            throw new XMLParsingException( "Error parsing required attribute 'Version'. " + e.getMessage() );
        }
        String template = null;
        try {
            version = XMLTools.getRequiredNodeAsString( request, "deegreewmps:Template",
                                                        CommonNamespaces.getNamespaceContext() );
        } catch ( Exception e ) {
            throw new XMLParsingException( "Error parsing required  element 'Template'. " + e.getMessage() );
        }
        return new DescribeTemplate( id, version, template, new HashMap<String, String>() );
    }

    /**
     * Parse 'Version' Parameter.
     * 
     * @param model
     * @return String version (default=1.0.0)
     */
    private static String retrieveVersionParameter( Map<String, String> model ) {

        String version = null;
        if ( model.containsKey( "VERSION" ) ) {
            version = (String) model.remove( "VERSION" );
        }
        if ( version == null ) {
            /** default value set as per the WMPS draft specifications. */
            version = "1.0.0";
        }

        return version;
    }

    /**
     * Parse 'Template' Parameter.
     * 
     * @param model
     * @return template to describe
     * @throws InconsistentRequestException 
     */
    private static String retrieveTemplateParameter( Map<String, String> model ) throws InconsistentRequestException{

        String template = null;
        if ( model.containsKey( "TEMPLATE" ) ) {
            template = (String) model.remove( "TEMPLATE" );
        }
        if ( template == null ) {
            throw new InconsistentRequestException( "parameter template must be set" );
        }

        return template;
    }
    
    /**
     * 
     * @return template name
     */
    public String getTemplate() {
        return template;
    }

}
