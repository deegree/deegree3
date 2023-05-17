package org.deegree.client.sos.requesthandler.kvp;

import java.util.List;

import org.deegree.commons.utils.Pair;

/**
 * Storage class containing the needed KVPs for a DescribeSensor request. These
 * will be displayed in a Form later.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class KVPDescribeSensor {

	private String host;

	private String version;

	private List<String> outputFormat;

	private List<Pair<String, String>> procedure;

	public KVPDescribeSensor() {

	}

	public String getHost() {
		return host;
	}

	public String getVersion() {
		return version;
	}

	public List<String> getOutputFormat() {
		return outputFormat;
	}

	public List<Pair<String, String>> getProcedure() {
		return procedure;
	}

	public void setHost(String that) {
		host = that;
	}

	public void setVersion(String that) {
		version = that;
	}

	public void setOutputFormat(List<String> that) {
		outputFormat = that;
	}

	public void setProcedure(List<Pair<String, String>> that) {
		procedure = that;
	}

}
