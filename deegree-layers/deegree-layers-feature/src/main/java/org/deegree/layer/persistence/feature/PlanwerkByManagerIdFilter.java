package org.deegree.layer.persistence.feature;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.filter.Expression;
import org.deegree.filter.Filters;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.Or;
import org.deegree.layer.LayerQuery;

import javax.xml.namespace.QName;

import static org.deegree.filter.MatchAction.ANY;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class PlanwerkByManagerIdFilter {

	private static final String PARAM_NAME_MANAGERID = "PLANWERK_MANAGERID";

	private static final QName PROP_NAME_MANAGERID = new QName("http://www.deegree.org/xplanung/1/0", "xplanMgrPlanId");

	private PlanwerkByManagerIdFilter() {
	}

	/**
	 * Adds a filter selecting the requested plans by the passed manager id.
	 * @param query which may contain the requested manager id as vendor specific
	 * parameter, mever <code>null</code>
	 * @param filter to append the filter by manager id, never <code>null</code>
	 * @return
	 */
	public static OperatorFilter addFilter(LayerQuery query, OperatorFilter filter) {
		if (query.getParameters().containsKey(PARAM_NAME_MANAGERID)) {
			String[] requestedManagerIds = retrieveRequestedManagerIds(query);
			if (requestedManagerIds.length == 1) {
				PropertyIsEqualTo planNameFilter = createFilterExpression(requestedManagerIds[0]);
				return Filters.and(filter, new OperatorFilter(planNameFilter));
			}
			else if (requestedManagerIds.length > 1) {
				PropertyIsEqualTo[] planFilter = new PropertyIsEqualTo[requestedManagerIds.length];
				for (int index = 0; index < requestedManagerIds.length; index++) {
					planFilter[index] = createFilterExpression(requestedManagerIds[index]);
				}
				Or or = new Or(planFilter);
				return Filters.and(filter, new OperatorFilter(or));
			}
		}
		return filter;
	}

	private static PropertyIsEqualTo createFilterExpression(String requestedManagerId) {
		Expression propName = new ValueReference(PROP_NAME_MANAGERID);
		Expression literal = new Literal<PrimitiveValue>(requestedManagerId);
		return new PropertyIsEqualTo(propName, literal, false, ANY);
	}

	private static String[] retrieveRequestedManagerIds(LayerQuery query) {
		String requestedManagerIds = query.getParameters().get(PARAM_NAME_MANAGERID);
		return requestedManagerIds.split(",");
	}

}