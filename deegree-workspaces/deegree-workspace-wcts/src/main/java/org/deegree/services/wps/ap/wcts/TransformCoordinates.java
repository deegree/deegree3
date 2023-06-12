/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.services.wps.ap.wcts;

import static org.deegree.commons.utils.StringUtils.isSet;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.fromMimeType;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.OutsideCRSDomainException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.transformations.Transformation;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.utils.XMLTransformer;
import org.deegree.protocol.wps.ap.wcts.WCTSConstants;
import org.deegree.services.wps.ExceptionAwareProcesslet;
import org.deegree.services.wps.ExceptionCustomizer;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.slf4j.Logger;

/**
 * The <code>TransformCoordinates</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */

public class TransformCoordinates implements ExceptionAwareProcesslet {

	private final static GMLVersion defaultGML;

	private boolean inspireCompliant;

	private static final Logger LOG = getLogger(TransformCoordinates.class);

	final static String IN_INPUTDATA = "InputData";

	final static String IN_TEST_TRANSFORM = "TestTransformation";

	final static String IN_TRANSFORM = "Transformation";

	final static String IN_SCRS = "SourceCRS";

	final static String IN_TCRS = "TargetCRS";

	final static String OUT_DATA = "TransformedData";

	static {
		URL config = FileUtils.loadDeegreeConfiguration(TransformCoordinates.class, "wcts-configuration.properties");
		GMLVersion configuredVersion = GML_31;
		if (config != null) {
			Properties props = new Properties();
			try {
				props.load(config.openStream());
				String gmlVersion = props.getProperty("GML_VERSION");
				if (gmlVersion != null) {
					try {
						configuredVersion = GMLVersion.valueOf(gmlVersion.toUpperCase());
					}
					catch (Exception e) {
						LOG.debug("Your gml version: " + gmlVersion + " could not be mapped, it should be one of: "
								+ Arrays.toString(GMLVersion.values()));
					}
				}
			}
			catch (IOException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not load configuration: " + e.getMessage(), e);
				}
				else {
					LOG.error("Could not load configuration: " + e.getMessage());
				}
			}
		}
		defaultGML = configuredVersion;
	}

	@Override
	public void destroy() {
		// destroy...
	}

	@Override
	public void init() {
		this.inspireCompliant = false;
	}

	@Override
	public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info)
			throws ProcessletException {
		// required by description
		ComplexInput xmlInput = (ComplexInput) in.getParameter(IN_INPUTDATA);
		String mime = xmlInput.getMimeType();
		String inSchema = xmlInput.getSchema();
		GMLVersion gmlVersion = fromMimeType(mime, defaultGML);
		XMLStreamReader inputData = null;
		try {
			inputData = xmlInput.getValueAsXMLStream();
			// StAXParsingHelper.nextElement( inputData );
		}
		catch (IOException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception while getting stream from input data: " + e.getMessage(), e);
			}
			else {
				LOG.error("Exception while getting stream from input data: " + e.getMessage());
			}
		}
		catch (XMLStreamException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception while getting stream from input data: " + e.getMessage(), e);
			}
			else {
				LOG.error("Exception while getting stream from input data: " + e.getMessage());
			}
		}
		catch (NullPointerException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception (no next element) while getting stream from input data: " + e.getMessage(), e);
			}
			else {
				LOG.error("Exception (no next element) while getting stream from input data: " + e.getMessage());
			}
		}

		if (inputData == null) {
			StringBuilder sb = new StringBuilder("No input data given.");
			String execCode = WCTSConstants.ExceptionCodes.NoInputData.name();
			throw new ProcessletException(new OWSException(sb.toString(), execCode));
		}
		InputParams evaluatedInput = evaluateInput(in);

		ComplexOutput xmlOutput = (ComplexOutput) out.getParameter(OUT_DATA);
		String outMime = xmlOutput.getRequestedMimeType();
		if (outMime != null && !outMime.equals(mime)) {
			throw new ProcessletException(new OWSException(
					"The inspire directive specifies that the output schema equals the input schema, therefore the mimetypes of the incoming data ("
							+ mime + " and the requested (transformed) outgoing data (" + outMime + " must be equal.",
					OWSException.INVALID_PARAMETER_VALUE));
		}
		String outSchema = xmlOutput.getRequestedSchema();
		if (outSchema != null && !outSchema.equals(inSchema)) {
			throw new ProcessletException(
					new OWSException(
							"The inspire directive specifies that the input schema ( " + inSchema
									+ ") and the output schema (" + outSchema + ") must be equal.",
							OWSException.INVALID_PARAMETER_VALUE));
		}
		LOG.debug("Setting XML output (requested=" + xmlOutput.isRequested() + ")");
		XMLStreamWriter writer = null;
		try {
			writer = xmlOutput.getXMLStreamWriter();
			// writer.writeStartDocument();
		}
		catch (XMLStreamException e) {
			LOG.error(e.getMessage());
			throw new ProcessletException("Could not create an outputstream." + e.getLocalizedMessage());
		}

		// result will not be null
		transform(evaluatedInput, inputData, writer, gmlVersion);

		try {
			// write the end document.
			writer.writeEndDocument();
		}
		catch (XMLStreamException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception message: " + e.getMessage(), e);
			}
			throw new ProcessletException(e.getLocalizedMessage());
		}
	}

	private void transform(InputParams evaluatedInput, XMLStreamReader inputData, XMLStreamWriter writer,
			GMLVersion gmlVersion) throws ProcessletException {
		try {
			List<Transformation> requestedTransformation = null;
			if (evaluatedInput.defaultTransform != null) {
				requestedTransformation = new ArrayList<Transformation>();
				requestedTransformation.add(evaluatedInput.defaultTransform);
			}
			evaluatedInput.transformer.transform(inputData, writer, evaluatedInput.sourceCRS, gmlVersion, true,
					requestedTransformation);
		}
		catch (XMLParsingException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception message: " + e.getMessage(), e);
			}
			throw new ProcessletException(e.getLocalizedMessage());
		}
		catch (IllegalArgumentException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception message: " + e.getMessage(), e);
			}
			throw new ProcessletException(
					new OWSException(e.getLocalizedMessage(), OWSException.INVALID_PARAMETER_VALUE));
		}
		catch (XMLStreamException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception message: " + e.getMessage(), e);
			}
			throw new ProcessletException(
					new OWSException(e.getLocalizedMessage(), OWSException.INVALID_PARAMETER_VALUE));
		}
		catch (UnknownCRSException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception message: " + e.getMessage(), e);
			}
			throw new ProcessletException(
					new OWSException(e.getLocalizedMessage(), OWSException.INVALID_PARAMETER_VALUE));
		}
		catch (TransformationException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception message: " + e.getMessage(), e);
			}
			throw new ProcessletException(
					new OWSException(e.getLocalizedMessage(), WCTSConstants.ExceptionCodes.NotTransformable.name()));
		}
		catch (OutsideCRSDomainException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Exception message: " + e.getMessage(), e);
			}
			throw new ProcessletException(
					new OWSException(e.getLocalizedMessage(), WCTSConstants.ExceptionCodes.InvalidArea.name()));
		}

	}

	/**
	 * @param in
	 * @return the instantiated source crs and a geometry transformer
	 */
	private InputParams evaluateInput(ProcessletInputs in) throws ProcessletException {
		XMLTransformer transformer = null;
		String sCrs = getLiteralInputValue(in, IN_SCRS);
		String tCrs = getLiteralInputValue(in, IN_TCRS);
		String transId = getLiteralInputValue(in, IN_TRANSFORM);
		boolean testTransformation = testTransformation(in);

		int val = isSet(sCrs) ? 1 : 0;
		val += isSet(tCrs) ? 2 : 0;
		val += isSet(transId) ? 4 : 0;
		StringBuilder sb = null;
		String execCode = OWSException.MISSING_PARAMETER_VALUE;
		ICRS sourceCRS = null;
		ICRS targetCRS = null;
		Transformation requestedTransform = null;
		switch (val) {
			case 0:
				sb = new StringBuilder("None of, ");
				sb.append(IN_SCRS).append(", ").append(IN_TCRS).append(" and ").append(IN_TRANSFORM);
				sb.append(" given.");
				break;
			case 1:
				// only sCrs
				sb = new StringBuilder("Missing ").append(IN_TCRS);
				break;
			case 2:
				if (inspireCompliant) {
					// only tCRS, the crs must be defined in the geometries.
					sb = new StringBuilder("Missing ").append(IN_SCRS).append(" or ").append(IN_TRANSFORM);
				}
				else {
					targetCRS = getCRS(tCrs);
					if (targetCRS == null) {
						sb = new StringBuilder();
						execCode = OWSException.INVALID_PARAMETER_VALUE;
						sb.append(IN_TCRS).append(" (").append(tCrs).append(") references an unknown crs.");
					}
					else {
						transformer = new XMLTransformer(targetCRS);
					}
				}
				break;
			case 3:
				sourceCRS = getCRS(sCrs);
				targetCRS = getCRS(tCrs);
				if (targetCRS == null || sourceCRS == null) {
					sb = new StringBuilder();
					execCode = OWSException.INVALID_PARAMETER_VALUE;
					if (sourceCRS == null) {
						sb.append(IN_SCRS).append(" (").append(sCrs).append(") references an unknown crs.");
					}
					if (targetCRS == null) {
						sb.append(IN_TCRS).append(" (").append(tCrs).append(") references an unknown crs.");
					}
				}
				else {
					transformer = new XMLTransformer(targetCRS);
				}
				break;
			case 4:
				// rb: inspire conform is not available.
				sb = new StringBuilder("No, ");
				sb.append(IN_TCRS).append(" given, it is required.");
				break;
			case 5:
				//
				sb = new StringBuilder("Invalid combination ").append(IN_SCRS).append(" and ").append(IN_TRANSFORM);
				execCode = OWSException.INVALID_PARAMETER_VALUE;
				break;
			case 6:
				ICRS tarCRS = getCRS(tCrs);
				requestedTransform = CRSManager.getTransformation(null, transId);
				if (requestedTransform == null || tarCRS == null) {
					sb = new StringBuilder();
					execCode = OWSException.INVALID_PARAMETER_VALUE;
					if (tarCRS == null) {
						sb.append(IN_TCRS).append(" (").append(tCrs).append(") references an unknown crs.");
					}
					if (requestedTransform == null) {
						sb.append(IN_TRANSFORM)
							.append(" (")
							.append(transId)
							.append(") references an unknown transformation.");
					}
				}
				else {
					transformer = new XMLTransformer(targetCRS);
				}
				break;
			case 7:
				if (inspireCompliant) {
					// rb: the inspire directive says mutual exclusive.
					sb = new StringBuilder("Mutual exclusion, ");
					sb.append(IN_SCRS).append(", ").append(IN_TCRS).append(" and ").append(IN_TRANSFORM);
					sb.append(" were given, allowed are either: ");
					sb.append(IN_SCRS).append(" and ").append(IN_TCRS).append(" or ");
					sb.append(IN_TRANSFORM).append(" and ").append(IN_TCRS);
					execCode = WCTSConstants.ExceptionCodes.MutualExclusionException.name();
				}
				else {
					sourceCRS = getCRS(sCrs);
					targetCRS = getCRS(tCrs);
					requestedTransform = CRSManager.getTransformation(null, transId);
					if (targetCRS == null || sourceCRS == null || requestedTransform == null) {
						sb = new StringBuilder();
						execCode = OWSException.INVALID_PARAMETER_VALUE;
						if (sourceCRS == null) {
							sb.append(IN_SCRS).append(" (").append(sCrs).append(") references an unknown crs.");
						}
						if (targetCRS == null) {
							sb.append(IN_TCRS).append(" (").append(tCrs).append(") references an unknown crs.");
						}
						if (requestedTransform == null) {
							sb.append(IN_TRANSFORM)
								.append(" (")
								.append(transId)
								.append(") references an unknown transformation.");
						}
					}
					else {
						transformer = new XMLTransformer(targetCRS);
					}
				}
				break;
		}

		if (sb != null) {
			if (testTransformation) {
				execCode = WCTSConstants.ExceptionCodes.NotTransformable.name();
			}
			throw new ProcessletException(new OWSException(sb.toString(), execCode));
		}
		if (inspireCompliant) {
			if (sourceCRS == null || transformer == null) {
				if (testTransformation) {
					execCode = WCTSConstants.ExceptionCodes.NotTransformable.name();
				}
				else {
					execCode = WCTSConstants.ExceptionCodes.OperationNotSupported.name();
				}
				sb = new StringBuilder("Unable to fullfill transformation.");
				if (sourceCRS == null) {
					sb.append(IN_SCRS).append(", could not be created.");
				}
				if (transformer == null) {
					sb.append("No geometry transformer could be created.");
				}
				throw new ProcessletException(new OWSException(sb.toString(), execCode));
			}
		}
		else {
			if (transformer == null) {
				if (testTransformation) {
					execCode = WCTSConstants.ExceptionCodes.NotTransformable.name();
				}
				else {
					execCode = WCTSConstants.ExceptionCodes.OperationNotSupported.name();
				}
				sb = new StringBuilder("Unable to fullfill transformation.");
				sb.append("No geometry transformer could be created (e.g. no transformation path available).");
				throw new ProcessletException(new OWSException(sb.toString(), execCode));
			}
		}
		if (testTransformation) {
			execCode = WCTSConstants.ExceptionCodes.Transformable.name();
			// rb: wow, the transformation can be applied, but we throw an exception
			// anyway (as the 'spec' says)
			// ;-)
			throw new ProcessletException(new OWSException("", execCode));
		}
		return new InputParams(sourceCRS, transformer, requestedTransform);
	}

	/**
	 * @param crs
	 * @return an instantiated crs.(created from provider)
	 */
	private ICRS getCRS(String crs) {
		ICRS result = null;
		try {
			result = CRSManager.lookup(crs);
		}
		catch (UnknownCRSException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e.getMessage(), e);
			}
		}
		return result;
	}

	private boolean testTransformation(ProcessletInputs in) {
		String isT = getLiteralInputValue(in, IN_TEST_TRANSFORM);
		return !isSet(isT) ? false : Boolean.parseBoolean(isT);
	}

	private String getLiteralInputValue(ProcessletInputs in, String paramId) {
		LiteralInput input = ((LiteralInput) in.getParameter(paramId));
		return input == null ? null : input.getValue();
	}

	@Override
	public ExceptionCustomizer getExceptionCustomizer() {
		return new org.deegree.services.wps.ap.wcts.ExceptionCustomizer(new CodeType("TransformCoordinates"));
	}

	private class InputParams {

		final ICRS sourceCRS;

		final XMLTransformer transformer;

		final Transformation defaultTransform;

		InputParams(ICRS sourceCRS, XMLTransformer transformer, Transformation defaultTransform) {
			this.sourceCRS = sourceCRS;
			this.transformer = transformer;
			this.defaultTransform = defaultTransform;

		}

	}

}
