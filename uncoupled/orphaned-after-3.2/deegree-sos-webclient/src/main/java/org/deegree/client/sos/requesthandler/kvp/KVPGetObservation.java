package org.deegree.client.sos.requesthandler.kvp;

import java.util.List;

/**
 * Storage class containing the needed KVPs for a GetObservation request. These
 * will be displayed in a Form later.<br>
 * Offering-specific KVPs are stored in a list of KVPfromOffering objects.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class KVPGetObservation {
    
    private String host;

	private String version;

	private List<KVPfromOffering> kvpfromOffering;
	
	public String getHost() {
        return host;
    }

	public String getVersion() {
		return version;
	}

	public List<KVPfromOffering> getKVPfromOffering() {
		return kvpfromOffering;
	}
	
	public void setHost(String that) {
        host = that;
    }

	public void setVersion(String that) {
		version = that;
	}

	public void setKVPfromOffering(List<KVPfromOffering> that) {
		kvpfromOffering = that;
	}

}
