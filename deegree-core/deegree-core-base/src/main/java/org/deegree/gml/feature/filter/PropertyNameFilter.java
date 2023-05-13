package org.deegree.gml.feature.filter;

import org.deegree.commons.tom.ResolveParams;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.projection.PropertyName;
import org.jaxen.expr.DefaultNameStep;
import org.jaxen.expr.LocationPath;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class PropertyNameFilter {

	private final List<PropertyName> propertyNames = new ArrayList<>();

	public void add(PropertyName propName) {
		propertyNames.add(propName);
	}

	public List<PropertyName> getPropertyNames() {
		return propertyNames;
	}

	public ResolveParams getResolveParams() {
		if (!propertyNames.isEmpty())
			return propertyNames.get(0).getResolveParams();
		return null;
	}

	public boolean isRequested(PathTracker pathTracker) {
		for (PropertyName propName : propertyNames) {
			LocationPath xPath = (LocationPath) propName.getPropertyName().getAsXPath();
			List<QName> path = new ArrayList<>();
			boolean isFirst = true;
			for (Object o : xPath.getSteps()) {
				QName qName = stepAsQName(propName.getPropertyName(), (DefaultNameStep) o);
				if (!firstMatchesAndFeatureName(pathTracker, isFirst, qName)) {
					path.add(qName);
				}
				isFirst = false;
			}
			List<QName> currentPath = pathTracker.getCurrentPath();
			if (matchesCurrentPath(currentPath, path)) {
				return true;
			}
		}
		return false;
	}

	private boolean firstMatchesAndFeatureName(PathTracker pathTracker, boolean isFirst, QName qName) {
		return isFirst && qName.equals(pathTracker.getFeatureName());
	}

	private QName stepAsQName(ValueReference valueReference, DefaultNameStep nameStep) {
		String prefix = nameStep.getPrefix();
		String namespaceURI = valueReference.getNsContext().translateNamespacePrefixToUri(prefix);
		return new QName(namespaceURI, nameStep.getLocalName(), prefix);
	}

	private boolean matchesCurrentPath(List<QName> currentPath, List<QName> propNamePath) {
		boolean isMatching = true;
		int maxIndex = Math.min(currentPath.size(), propNamePath.size());
		for (int i = 0; i < maxIndex; i++) {
			if (!propNamePath.get(i).equals(currentPath.get(i)))
				isMatching = false;
		}
		return isMatching;
	}

}
