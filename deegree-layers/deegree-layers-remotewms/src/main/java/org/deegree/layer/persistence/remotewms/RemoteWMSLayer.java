package org.deegree.layer.persistence.remotewms;

import static java.util.Collections.singletonList;
import static org.deegree.commons.utils.RequestUtils.replaceParameters;
import static org.deegree.protocol.oldwms.WMSConstants.VERSION_111;
import static org.deegree.protocol.oldwms.WMSConstants.VERSION_130;
import static org.deegree.protocol.wms.WMSConstants.WMSRequestType.GetMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.featureinfo.parsing.DefaultFeatureInfoParser;
import org.deegree.featureinfo.parsing.FeatureInfoParser;
import org.deegree.featureinfo.parsing.XsltFeatureInfoParser;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.metadata.XsltFile;
import org.deegree.layer.persistence.remotewms.jaxb.ParameterScopeType;
import org.deegree.layer.persistence.remotewms.jaxb.ParameterUseType;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType.DefaultCRS;
import org.deegree.layer.persistence.remotewms.jaxb.RequestOptionsType.Parameter;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.protocol.wms.ops.EXCEPTIONS_FORMAT;
import org.deegree.protocol.wms.ops.GetFeatureInfo;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.style.StyleRef;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class RemoteWMSLayer extends AbstractLayer {

	private static final Logger LOG = getLogger(RemoteWMSLayer.class);

	private final WMSClient client;

	private ICRS crs;

	private boolean alwaysUseDefaultCrs;

	private String format;

	private boolean transparent = true;

	private HashMap<String, String> defaultParametersGetMap = new HashMap<String, String>();

	private HashMap<String, String> defaultParametersGetFeatureInfo = new HashMap<String, String>();

	private HashMap<String, String> hardParametersGetMap = new HashMap<String, String>();

	private HashMap<String, String> hardParametersGetFeatureInfo = new HashMap<String, String>();

	private final String originalName;

	private FeatureInfoParser featureInfoParser;

	RemoteWMSLayer(String originalName, LayerMetadata md, WMSClient client, RequestOptionsType opts) {
		this(originalName, md, client, opts, null);
	}

	RemoteWMSLayer(String originalName, LayerMetadata md, WMSClient client, RequestOptionsType opts,
			XsltFile xsltFile) {
		super(md);
		this.originalName = originalName;
		this.featureInfoParser = createFeatureInfoParser(xsltFile);
		md.setCascaded(md.getCascaded() + 1);
		this.client = client;
		if (opts != null) {
			if (opts.getDefaultCRS() != null) {
				DefaultCRS crs = opts.getDefaultCRS();
				this.crs = CRSManager.getCRSRef(crs.getValue(), true);
				alwaysUseDefaultCrs = crs.isUseAlways();
			}
			if (opts.getImageFormat() != null) {
				this.format = opts.getImageFormat().getValue();
				this.transparent = opts.getImageFormat().isTransparent();
			}
			extractParameters(opts.getParameter());
		}
		// set default values if not configured
		if (this.crs == null) {
			this.crs = CRSManager.getCRSRef(client.getCoordinateSystems(originalName).getFirst());
		}
		if (this.format == null) {
			LinkedList<String> fs = client.getFormats(GetMap);
			if (fs.contains("image/png")) {
				format = "image/png";
			}
			else {
				format = fs.getFirst();
			}
		}
	}

	private void extractParameters(List<Parameter> params) {
		if (params != null && !params.isEmpty()) {
			for (Parameter p : params) {
				String name = p.getName();
				String value = p.getValue();
				ParameterUseType use = p.getUse();
				ParameterScopeType scope = p.getScope();
				switch (use) {
					case ALLOW_OVERRIDE:
						switch (scope) {
							case GET_MAP:
								defaultParametersGetMap.put(name, value);
								break;
							case GET_FEATURE_INFO:
								defaultParametersGetFeatureInfo.put(name, value);
								break;
							default:
								defaultParametersGetMap.put(name, value);
								defaultParametersGetFeatureInfo.put(name, value);
								break;
						}
						break;
					case FIXED:
						switch (scope) {
							case GET_MAP:
								hardParametersGetMap.put(name, value);
								break;
							case GET_FEATURE_INFO:
								hardParametersGetFeatureInfo.put(name, value);
								break;
							default:
								hardParametersGetMap.put(name, value);
								hardParametersGetFeatureInfo.put(name, value);
								break;
						}
						break;
				}
			}
		}
	}

	@Override
	public RemoteWMSLayerData mapQuery(LayerQuery query, List<String> headers) {
		Map<String, String> extraParams = new HashMap<>();
		replaceParameters(extraParams, query.getParameters(), defaultParametersGetMap, hardParametersGetMap);
		EXCEPTIONS_FORMAT exceptionsFormat = EXCEPTIONS_FORMAT
			.findByParamValue(query.getParameters().get("EXCEPTIONS"));
		CrsAndEnvelope result = retriveCrsAndEnvelope(query);
		GetMap gm = new GetMap(singletonList(originalName), query.getWidth(), query.getHeight(), result.envelope(),
				result.crs(), format, transparent, exceptionsFormat);
		return new RemoteWMSLayerData(client, gm, extraParams);
	}

	@Override
	public RemoteWMSLayerData infoQuery(LayerQuery query, List<String> headers) {
		Map<String, String> extraParams = new HashMap<>();
		replaceParameters(extraParams, query.getParameters(), defaultParametersGetFeatureInfo,
				hardParametersGetFeatureInfo);
		CrsAndEnvelope result = retriveCrsAndEnvelope(query);
		GetFeatureInfo gfi = new GetFeatureInfo(Collections.singletonList(originalName), query.getWidth(),
				query.getHeight(), query.getX(), query.getY(), result.envelope(), result.crs(),
				query.getFeatureCount());
		return new RemoteWMSLayerData(client, gfi, extraParams, featureInfoParser);
	}

	@Override
	public boolean isStyleApplicable(StyleRef style) {
		if ("default".equals(style.getName())) {
			return true;
		}
		return resolveStyleRef(style) != null;
	}

	private FeatureInfoParser createFeatureInfoParser(XsltFile xsltFile) {
		if (xsltFile != null)
			return new XsltFeatureInfoParser(xsltFile.getXsltFile(), xsltFile.getTargetGmlVersion());
		return new DefaultFeatureInfoParser();
	}

	private CrsAndEnvelope retriveCrsAndEnvelope(LayerQuery query) {
		ICRS crs = this.crs;
		Envelope envelope = query.getEnvelope();
		if (!alwaysUseDefaultCrs) {
			ICRS envCrs = envelope.getCoordinateSystem();
			if (client.getCoordinateSystems(originalName).contains(envCrs.getAlias())) {
				crs = envCrs;
				// overwrite envelope with swapped axis if wms client is not in the same
				// version as the GetMap request and the CRS is not axis aware
				if (!client.getWmsVersion().toString().equals(query.getParameters().get("VERSION"))
						&& !CRSUtils.isAxisAware(crs)) {
					envelope = new GeometryFactory().createEnvelope(envelope.getMin().get1(), envelope.getMin().get0(),
							envelope.getMax().get1(), envelope.getMax().get0(), crs);
				}
			}
			// use original crs and overwrite envelope with the requested CRS
			else if (client.getWmsVersion().equals(VERSION_111)
					&& client.getCoordinateSystems(originalName).contains(query.getParameters().get("SRS"))) {
				crs = getOriginalCrs(query, "SRS", this.crs);
				envelope = new GeometryFactory().createEnvelope(envelope.getMin().get0(), envelope.getMin().get1(),
						envelope.getMax().get0(), envelope.getMax().get1(), crs);
			}
			// use original crs and overwrite envelope with the requested CRS
			else if (client.getWmsVersion().equals(VERSION_130)
					&& client.getCoordinateSystems(originalName).contains(query.getParameters().get("CRS"))) {
				crs = getOriginalCrs(query, "CRS", this.crs);
				envelope = new GeometryFactory().createEnvelope(envelope.getMin().get0(), envelope.getMin().get1(),
						envelope.getMax().get0(), envelope.getMax().get1(), crs);
			}
		}
		return new CrsAndEnvelope(crs, envelope);
	}

	private ICRS getOriginalCrs(LayerQuery query, String paramName, ICRS defaultCrs) {
		try {
			return CRSManager.lookup(query.getParameters().get(paramName));
		}
		catch (UnknownCRSException e) {
			return defaultCrs;
		}
	}

	private record CrsAndEnvelope(ICRS crs, Envelope envelope) {
	}

}
