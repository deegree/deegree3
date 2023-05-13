package org.deegree.console.datastore.feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.FeatureType;

public class AppSchemaInfo {

	private final AppSchema schema;

	public AppSchemaInfo(AppSchema schema) {
		this.schema = schema;
	}

	public SortedSet<String> getSchemaComponents() {
		TreeSet<String> components = new TreeSet<String>();
		for (String ns : schema.getGMLSchema().getSchemaNamespaces()) {
			components.addAll(schema.getGMLSchema().getComponentLocations(ns));
		}
		return components;
	}

	public List<NamespaceBinding> getNamespaces() {
		Set<NamespaceBinding> namespaces = new TreeSet<NamespaceBinding>();
		for (FeatureType ft : schema.getFeatureTypes()) {
			String prefix = ft.getName().getPrefix();
			String ns = ft.getName().getNamespaceURI();
			namespaces.add(new NamespaceBinding(prefix, ns));
		}
		return new ArrayList<NamespaceBinding>(namespaces);
	}

	public String getNumFtsTotal() {
		int numFtsTotal = schema.getFeatureTypes(null, false, true).size();
		return "" + numFtsTotal;
	}

	public String getNumFtsAbstract() {
		int numFtsTotal = schema.getFeatureTypes(null, false, true).size();
		int numFtsConcrete = schema.getFeatureTypes(null, false, false).size();
		return "" + (numFtsTotal - numFtsConcrete);
	}

	public String getNumFtsConcrete() {
		int numFtsConcrete = schema.getFeatureTypes(null, false, false).size();
		return "" + numFtsConcrete;
	}

	public String getFtInfo() throws IOException {
		StringBuffer sb = new StringBuffer();
		FeatureType[] fts = schema.getRootFeatureTypes();

		// sort the types by name
		Arrays.sort(fts, new Comparator<FeatureType>() {
			public int compare(FeatureType a, FeatureType b) {
				int order = a.getName().getNamespaceURI().compareTo(b.getName().getNamespaceURI());
				if (order == 0) {
					order = a.getName().getLocalPart().compareTo(b.getName().getLocalPart());
				}
				return order;
			}
		});

		for (FeatureType ft : fts) {
			appendFtInfo(ft, sb, "");
			sb.append("<br/>");
		}
		return sb.toString();
	}

	public String getFcInfo() throws IOException {
		StringBuffer sb = new StringBuffer();
		FeatureType[] fts = schema.getRootFeatureTypes();

		// sort the types by name
		Arrays.sort(fts, new Comparator<FeatureType>() {
			public int compare(FeatureType a, FeatureType b) {
				int order = a.getName().getNamespaceURI().compareTo(b.getName().getNamespaceURI());
				if (order == 0) {
					order = a.getName().getLocalPart().compareTo(b.getName().getLocalPart());
				}
				return order;
			}
		});

		for (FeatureType ft : fts) {
			appendFcInfo(ft, sb, "");
		}
		return sb.toString();
	}

	private void appendFcInfo(FeatureType ft, StringBuffer sb, String indent) throws IOException {
		if (ft instanceof FeatureCollectionType) {
			if (ft.isAbstract()) {
				sb.append(indent + "- <i>" + ft.getName().getPrefix() + ":" + ft.getName().getLocalPart()
						+ " (abstract)</i><br/>");
			}
			else {
				sb.append(indent + "- " + ft.getName().getPrefix() + ":" + ft.getName().getLocalPart() + "<br/>");
			}
		}
		FeatureType[] fts = ft.getSchema().getDirectSubtypes(ft);
		Arrays.sort(fts, new Comparator<FeatureType>() {
			public int compare(FeatureType a, FeatureType b) {
				int order = a.getName().getNamespaceURI().compareTo(b.getName().getNamespaceURI());
				if (order == 0) {
					order = a.getName().getLocalPart().compareTo(b.getName().getLocalPart());
				}
				return order;
			}
		});
		for (FeatureType childType : fts) {
			appendFtInfo(childType, sb, indent + "&nbsp;&nbsp;");
		}
	}

	private void appendFtInfo(FeatureType ft, StringBuffer sb, String indent) throws IOException {
		if (ft instanceof FeatureCollectionType) {
			return;
		}
		if (ft.isAbstract()) {
			sb.append(indent + "- <i>" + ft.getName().getPrefix() + ":" + ft.getName().getLocalPart()
					+ " (abstract)</i><br/>");
		}
		else {
			sb.append(indent + "- " + ft.getName().getPrefix() + ":" + ft.getName().getLocalPart() + "<br/>");
		}
		FeatureType[] fts = ft.getSchema().getDirectSubtypes(ft);
		Arrays.sort(fts, new Comparator<FeatureType>() {
			public int compare(FeatureType a, FeatureType b) {
				int order = a.getName().getNamespaceURI().compareTo(b.getName().getNamespaceURI());
				if (order == 0) {
					order = a.getName().getLocalPart().compareTo(b.getName().getLocalPart());
				}
				return order;
			}
		});
		for (FeatureType childType : fts) {
			appendFtInfo(childType, sb, indent + "&nbsp;&nbsp;");
		}
	}

}