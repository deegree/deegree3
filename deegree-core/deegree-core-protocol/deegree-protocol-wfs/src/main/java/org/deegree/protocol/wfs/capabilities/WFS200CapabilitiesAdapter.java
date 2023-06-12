package org.deegree.protocol.wfs.capabilities;

import java.util.List;

import org.deegree.protocol.ows.capabilities.OWSCommon110CapabilitiesAdapter;
import org.deegree.protocol.wfs.metadata.WFSFeatureType;

/**
 * {@link WFSCapabilitiesAdapter} for documents that comply to the <a
 * href="http://www.opengeospatial.org/standards/wfs>WFS 2.0.0</a> specification.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class WFS200CapabilitiesAdapter extends OWSCommon110CapabilitiesAdapter implements WFSCapabilitiesAdapter {

	@Override
	public List<WFSFeatureType> parseFeatureTypeList() {
		return null;
	}

	@Override
	public Object parseFilterCapabilities() {
		return null;
	}

}
