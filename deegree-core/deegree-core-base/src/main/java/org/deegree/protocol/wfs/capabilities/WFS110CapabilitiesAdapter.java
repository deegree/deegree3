package org.deegree.protocol.wfs.capabilities;

import java.util.List;

import org.deegree.protocol.ows.OWS100CapabilitiesAdapter;
import org.deegree.protocol.wfs.metadata.WFSFeatureType;

/**
 * {@link WFSCapabilitiesAdapter} for documents that comply to the <a
 * href="http://www.opengeospatial.org/standards/wfs>WFS 1.1.0</a> specification.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WFS110CapabilitiesAdapter extends OWS100CapabilitiesAdapter implements WFSCapabilitiesAdapter {

    @Override
    public List<WFSFeatureType> parseFeatureTypeList() {
        return null;
    }

    @Override
    public Object parseFilterCapabilities() {
        return null;
    }
}
