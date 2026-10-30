package org.deegree.protocol.wms.ops;

import static org.deegree.protocol.wms.ops.SLDParser.parse;
import static org.slf4j.LoggerFactory.getLogger;

import javax.xml.stream.XMLInputFactory;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.ServiceLoader;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.utils.Triple;
import org.deegree.filter.OperatorFilter;
import org.deegree.layer.LayerRef;
import org.deegree.style.StyleRef;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class SldParamRetriever {

	private static final Logger LOG = getLogger(SldParamRetriever.class);

	private static final ServiceLoader<SldProvider> sldProviderLoader;

	static {
		sldProviderLoader = ServiceLoader.load(SldProvider.class);
		for (SldProvider sldProvider : sldProviderLoader) {
			sldProvider.init();
		}
	}

	/**
	 * @param sld the value of the SLD param, may be <code>null</code>
	 * @param sldBody the value of the SLD_BODY param, may be <code>null</code>,
	 * overwrites the sld param value if sld and sldBody are not <code>null</code>
	 * @param gm the {@link RequestBase} instance, never <code>null</code>
	 * @return the parsed sld, if sld or sldBody not <code>null</code>, <code>null</code>
	 * if sld and sldBody are <code>null</code>
	 * @throws OWSException if sld or sldBody could not be parsed
	 */
	Triple<LinkedList<LayerRef>, LinkedList<StyleRef>, LinkedList<OperatorFilter>> handleSldParam(String sld,
			String sldBody, RequestBase gm) throws OWSException {
		XMLInputFactory xmlfac = XMLInputFactory.newInstance();
		Triple<LinkedList<LayerRef>, LinkedList<StyleRef>, LinkedList<OperatorFilter>> triple = null;
		if (sld != null) {
			try {
				for (SldProvider sldProvider : sldProviderLoader) {
					String sldBodyFromProvider = sldProvider.retrieveSldBody(sld);
					if (sldBodyFromProvider != null)
						triple = parse(xmlfac.createXMLStreamReader(new StringReader(sldBodyFromProvider)), gm);
				}
				if (triple == null)
					return parse(xmlfac.createXMLStreamReader(sld, new URL(sld).openStream()), gm);
			}
			catch (ParseException e) {
				LOG.trace("Stack trace:", e);
				throw new OWSException(
						"The embedded dimension value in the SLD parameter value was invalid: " + e.getMessage(),
						"InvalidDimensionValue", "sld");
			}
			catch (Throwable e) {
				LOG.trace("Stack trace:", e);
				throw new OWSException("Error when parsing the SLD parameter: " + e.getMessage(),
						"InvalidParameterValue", "sld");
			}
		}
		if (sldBody != null) {
			try {
				triple = parse(xmlfac.createXMLStreamReader(new StringReader(sldBody)), gm);
			}
			catch (ParseException e) {
				LOG.trace("Stack trace:", e);
				throw new OWSException(
						"The embedded dimension value in the SLD_BODY parameter value was invalid: " + e.getMessage(),
						"InvalidDimensionValue", "sld_body");
			}
			catch (Throwable e) {
				LOG.trace("Stack trace:", e);
				throw new OWSException("Error when parsing the SLD_BODY parameter: " + e.getMessage(),
						"InvalidParameterValue", "sld_body");
			}
		}
		return triple;
	}

}
