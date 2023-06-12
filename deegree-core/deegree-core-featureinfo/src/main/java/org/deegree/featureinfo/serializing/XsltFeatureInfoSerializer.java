package org.deegree.featureinfo.serializing;

import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XsltUtils;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.AppSchema;
import org.deegree.featureinfo.FeatureInfoContext;
import org.deegree.featureinfo.FeatureInfoParams;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

public class XsltFeatureInfoSerializer implements FeatureInfoSerializer {

	private static final Logger LOG = getLogger(XsltFeatureInfoSerializer.class);

	private final GMLVersion gmlVersion;

	private final URL xslt;

	private final Workspace workspace;

	public XsltFeatureInfoSerializer(GMLVersion version, URL xslt, Workspace workspace) {
		this.gmlVersion = version;
		this.xslt = xslt;
		this.workspace = workspace;
	}

	@Override
	public void serialize(FeatureInfoParams params, FeatureInfoContext context) {
		Map<String, String> nsBindings = params.getNsBindings();
		FeatureCollection col = params.getFeatureCollection();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(workspace.getModuleClassLoader());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(bos);
			if (LOG.isDebugEnabled()) {
				out = new IndentingXMLStreamWriter(out);
			}
			GMLStreamWriter writer = createGMLStreamWriter(gmlVersion, out);
			if (nsBindings == null) {
				nsBindings = new HashMap<String, String>();
			}
			for (Feature f : col) {
				AppSchema schema = f.getType().getSchema();
				if (schema != null)
					nsBindings.putAll(schema.getNamespaceBindings());
			}
			writer.setNamespaceBindings(nsBindings);
			writer.write(col);
			writer.close();
			bos.flush();
			bos.close();
			if (LOG.isDebugEnabled()) {
				LOG.debug("GML before XSLT:\n{}", new String(bos.toByteArray(), "UTF-8"));
			}
			XsltUtils.transform(bos.toByteArray(), this.xslt, context.getOutputStream());
		}
		catch (Throwable e) {
			LOG.warn("Unable to transform GML for feature info: {}.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

}
