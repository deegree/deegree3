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
package org.deegree.services.wps.provider.jrxml.contentprovider;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;

/**
 * Implementing classes helps to map the jrxml file to a WPS Process description and to create a report out of the
 * process execute request
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public interface JrxmlContentProvider {

    /**
     * inspects the parameters found in the xml and converts them to WPSProcess input parameters
     * 
     * @param parameterDescriptions
     *            description of a single parameter out of the process definition
     * @param inputs
     *            list of {@link ProcessletInputDefinition}s, never <code>null</code>, append new inputs here
     * @param jrxmlAdapter
     *            adapter containing the jrxml
     * @param parameters
     *            list of all parameters out of the jrxml file
     * @param handledParameters
     *            list of parameters out of the jrxml file, which are handled already!
     */
    void inspectInputParametersFromJrxml( Map<String, ParameterDescription> parameterDescriptions,
                                          List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                          XMLAdapter jrxmlAdapter, Map<String, String> parameters,
                                          List<String> handledParameters );

    /**
     * prepare the jrxml and read input parameters from WPSProcess providers to append them in the list of parameters to
     * fill the jrxml report with
     * 
     * @param the
     *            jrxml as {@link InputStream}
     * @param params
     *            list of parameters to fill the jrxml report with, append parameters read from WPS PRocess input
     *            parameters here, never <code>null</code>
     * @param in
     *            contains the WPSProcess input parameters, never <code>null</code>
     * @param processedIds
     *            a list if ids which are already precessed. insert ids here, if they are processed. never
     *            <code>null</code>
     * @param parameters
     *            metainformation about the parameters (name, type)
     * @return the adjusted jrxml as {@link InputStream} and an information if a datasource was inserted or not
     */
    Pair<InputStream, Boolean> prepareJrxmlAndReadInputParameters( InputStream jrxml, Map<String, Object> params,
                                                                   ProcessletInputs in, List<CodeType> processedIds,
                                                                   Map<String, String> parameters )
                            throws ProcessletException;
}
