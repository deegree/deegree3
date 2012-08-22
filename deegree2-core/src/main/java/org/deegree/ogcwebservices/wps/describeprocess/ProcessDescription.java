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

import org.deegree.datatypes.Code;
import org.deegree.ogcwebservices.MetadataType;
import org.deegree.ogcwebservices.wps.ProcessBrief;

/**
 *
 * ProcessDescription.java
 *
 * Created on 09.03.2006. 22:39:07h
 *
 * Full description of a process.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author:wanhoff$
 *
 * @version $Revision$, $Date:20.03.2007$
 */
public class ProcessDescription extends ProcessBrief {

    /**
     *
     * @param resonsibleClass
     * @param identifier
     * @param title
     * @param _abstract
     * @param processVersion
     * @param metadata
     * @param inputs
     * @param outputs
     * @param statusSupported
     * @param storeSupported
     */
    public ProcessDescription( String resonsibleClass, Code identifier, String title, String _abstract,
                               String processVersion, List<MetadataType> metadata, DataInputs inputs,
                               ProcessOutputs outputs, Boolean statusSupported, Boolean storeSupported ) {
        super( identifier, title, _abstract, processVersion, metadata );
        this.responsibleClass = resonsibleClass;
        this.dataInputs = inputs;
        this.processOutputs = outputs;
        this.statusSupported = statusSupported;
        this.storeSupported = storeSupported;
    }

    /**
     *
     */
    protected String responsibleClass;

    /**
     * List of the inputs to this process. In almost all cases, at least one process input is required. However, no
     * process inputs may be identified when all the inputs are predetermined fixed resources. In this case, those
     * resources shall be identified in the ows:Abstract element that describes the process
     */
    protected DataInputs dataInputs;

    /**
     * List of outputs which will or can result from executing the process.
     */
    protected ProcessOutputs processOutputs;

    /**
     * Indicates if the Execute operation response can be returned quickly with status information, or will not be
     * returned until process execution is complete. If "statusSupported" is "true", the Execute operation request may
     * include "status" equals "true", directing that the Execute operation response be returned quickly with status
     * information. By default, status information is not provided for this process, and the Execute operation response
     * is not returned until process execution is complete.
     */
    protected Boolean statusSupported;

    /**
     * Indicates if the ComplexData outputs from this process can be stored by the WPS server as web-accessible
     * resources. If "storeSupported" is "true", the Execute operation request may include "store" equals "true",
     * directing that all ComplexData outputs of the process be stored so that the client can retrieve them as required.
     * By default for this process, storage is not supported and all outputs are returned encoded in the Execute
     * response.
     */
    protected Boolean storeSupported;

    /**
     * @return Returns the dataInputs.
     */
    public DataInputs getDataInputs() {
        return dataInputs;
    }

    /**
     * @param value
     *            The dataInputs to set.
     */
    public void setDataInputs( DataInputs value ) {
        this.dataInputs = value;
    }

    /**
     * @return Returns the processOutputs.
     */
    public ProcessOutputs getProcessOutputs() {
        return processOutputs;
    }

    /**
     * @param value
     *            The processOutputs to set.
     */
    public void setProcessOutputs( ProcessOutputs value ) {
        this.processOutputs = value;
    }

    /**
     *
     * @return true if status request is supported.
     */
    public boolean isStatusSupported() {
        return statusSupported;
    }

    /**
     * @param value
     *            The statusSupported to set.
     */
    public void setStatusSupported( Boolean value ) {
        this.statusSupported = value;
    }

    /**
     *
     * @return true if the process can store.
     */
    public boolean isStoreSupported() {
        return storeSupported;
    }

    /**
     * @param value
     *            The storeSupported to set.
     */
    public void setStoreSupported( Boolean value ) {
        this.storeSupported = value;
    }

    /**
     * TODO add documentation here
     *
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
     * @author last edited by: $Author:wanhoff$
     *
     * @version $Revision$, $Date:20.03.2007$
     */
    public static class DataInputs {

        /**
         *
         *
         * Unordered list of one or more descriptions of the inputs that can be accepted by this process, including all
         * required and optional inputs. Where an input is optional because a default value exists, that default value
         * must be identified in the "ows:Abstract" element for that input, except in the case of LiteralData, where the
         * default must be indicated in the corresponding ows:DefaultValue element. Where an input is optional because
         * it depends on the value(s) of other inputs, this must be indicated in the ows:Abstract element for that
         * input.
         *
         *
         */
        private List<InputDescription> inputDescriptions;

        /**
         * @return Returns the input.
         */
        public List<InputDescription> getInputDescriptions() {
            if ( inputDescriptions == null ) {
                inputDescriptions = new ArrayList<InputDescription>();
            }
            return this.inputDescriptions;
        }

        /**
         * @param inputDescriptions
         */
        public void setInputDescriptions( List<InputDescription> inputDescriptions ) {
            this.inputDescriptions = inputDescriptions;
        }

    }

    /**
     * TODO add documentation here
     *
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
     * @author last edited by: $Author:wanhoff$
     *
     * @version $Revision$, $Date:20.03.2007$
     */
    public static class ProcessOutputs {

        /**
         * Unordered list of one or more descriptions of all the outputs that can result from executing this process. At
         * least one output is required from each process.
         */
        protected List<OutputDescription> output;

        /**
         * @return Returns the output.
         */
        public List<OutputDescription> getOutput() {
            if ( output == null ) {
                output = new ArrayList<OutputDescription>();
            }
            return this.output;
        }

    }

    /**
     * @return Returns the responsibleClass.
     */
    public String getResponsibleClass() {
        return responsibleClass;
    }

}
