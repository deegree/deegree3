package org.deegree.model.filter;

import org.deegree.model.feature.Feature;
import org.deegree.model.filter.expression.PropertyName;
import org.jaxen.JaxenException;

/**
 * Interface for objects that may be filtered, i.e. a {@link Filter} expression may be evaluated on them.
 * <p>
 * Therefore the objects must provide access to their property values using XPath-expressions.
 * 
 * @see Filter
 * @see Feature
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface MatchableObject {

    /**
     * Returns the value of a certain property of this object.
     * 
     * @param propName
     *            XPath expression that identifies the property
     * @return the property value
     * @throws JaxenException
     *             if an exception occurs during the evaluation of the XPath expression
     */
    public Object getPropertyValue( PropertyName propName )
                            throws JaxenException;

}
