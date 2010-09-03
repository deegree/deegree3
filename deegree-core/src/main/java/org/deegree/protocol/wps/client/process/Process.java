//$HeadURL$
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
package org.deegree.protocol.wps.client.process;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.input.type.InputType;
import org.deegree.protocol.wps.client.output.type.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the properties of a process offered by a WPS instance (id, title, abstract, input parameter types,
 * output parameter types, etc.) and provides access to a {@link ProcessExecution} context for executing it.
 * 
 * @see WPSClient
 * @see ProcessExecution
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Process {

    private static final Logger LOG = LoggerFactory.getLogger( Process.class );

    private final WPSClient client;

    private final ProcessInfo processInfo;

    private ProcessDetails processDetails;

    /**
     * Creates a new {@link Process} instance.
     * 
     * @param client
     *            associated client instance, must not be <code>null</code>
     * @param processInfo
     *            brief process info, must not be <code>null</code>
     */
    public Process( WPSClient client, ProcessInfo processInfo ) {
        this.client = client;
        this.processInfo = processInfo;
    }

    /**
     * Returns the identifier of the process.
     * 
     * @return the identifier, never <code>null</code>
     */
    public CodeType getId() {
        return processInfo.getId();
    }

    /**
     * Returns the version of the process.
     * 
     * @return the version, never <code>null</code>
     */
    public String getVersion() {
        return processInfo.getVersion();
    }

    /**
     * Returns the title of the process.
     * 
     * @return the title, never <code>null</code>
     */
    public LanguageString getTitle() {
        return processInfo.getTitle();
    }

    /**
     * Returns the abstract of the process.
     * 
     * @return the abstract, can be <code>null</code> (if the process description does not define an abstract)
     */
    public LanguageString getAbstract() {
        return processInfo.getAbstract();
    }

    /**
     * Returns the descriptions for all input parameters of the process.
     * 
     * @return the descriptions for all input parameters, can be empty, but never <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public InputType[] getInputTypes()
                            throws IOException, OWSException {
        List<InputType> inputs = getProcessDetails().getInputs();
        return inputs.toArray( new InputType[inputs.size()] );
    }

    /**
     * Returns the descriptions for all output parameters of the process.
     * 
     * @return the descriptions for all output parameters, can be empty, but never <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public OutputType[] getOutputTypes()
                            throws IOException, OWSException {
        List<OutputType> outputs = getProcessDetails().getOutputs();
        return outputs.toArray( new OutputType[outputs.size()] );
    }

    /**
     * Returns whether the process supports storing of the response document (=asynchronous execution).
     * 
     * @return true, if the process supports storing the response document, false otherwise
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public boolean getStoreSupported()
                            throws OWSException, IOException {
        return getProcessDetails().getStoreSupported();
    }

    /**
     * Returns whether the process supports polling of status information during asynchronous execution.
     * 
     * @return true, if the process supports polling status information, false otherwise
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public boolean getStatusSupported()
                            throws OWSException, IOException {
        return getProcessDetails().getStatusSupported();
    }

    /**
     * Prepares a new {@link ProcessExecution} context that allows to execute the process.
     * <p>
     * This method will request the response using the WPS <code>ResponseDocument</code> mode. If you're unsure, this is
     * most probably what you want, as it is the most flexible way of executing WPS processes.
     * </p>
     * 
     * @return new process execution context, never <code>null</code>
     */
    public ProcessExecution prepareExecution() {
        return new ProcessExecution( client, this );
    }

    /**
     * Prepares a new {@link RawProcessExecution} context that allows to execute the process using the WPS
     * <code>RawOutput</code> mode.
     * <p>
     * If you're unsure, then you most probably want to use {@link #prepareExecution()}, as the <code>RawOutput</code>
     * mode is rather restricted: just a single (complex) output, no asynchronous execution, no storing of output.
     * </p>
     * 
     * @return new process execution context, never <code>null</code>
     */
    public RawProcessExecution prepareRawExecution() {
        return new RawProcessExecution( client, this );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "ProcessIdentifier: " + processInfo.getId() + "\n" );
        return sb.toString();
    }

    /**
     * Returns the (cached) {@link ProcessDetails} for this process.
     * 
     * @return process details, never <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    private ProcessDetails getProcessDetails()
                            throws IOException, OWSException {

        if ( processDetails == null ) {
            URL url = client.getDescribeProcessURL( false );

            if ( processInfo.getId().getCodeSpace() != null ) {
                LOG.warn( "Performing DescribeProcess using GET, but process identifier ('" + processInfo.getId()
                          + "') has a code space (which cannot be expressed using the GET binding). "
                          + "Omitting the code space and hoping for the best..." );
            }

            String finalURLStr = url.toExternalForm() + "?service=WPS&version="
                                 + URLEncoder.encode( client.getServiceVersion(), "UTF-8" )
                                 + "&request=DescribeProcess&identifier="
                                 + URLEncoder.encode( processInfo.getId().getCode(), "UTF-8" );
            URL finalURL = new URL( finalURLStr );
            XMLAdapter describeProcessResponse = new XMLAdapter( finalURL );
            processDetails = new ProcessDetails( describeProcessResponse );
        }
        return processDetails;
    }
}
