//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/

package org.deegree.model.filterencoding;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.feature.Feature;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Node;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FunctionNormalize extends Function {

    private static final ILogger LOG = LoggerFactory.getLogger( FunctionNormalize.class );

    private static XMLFragment rules;

    private static NamespaceContext nsContext;

    private static void initialize() {
        if ( rules == null ) {
            URL url = FunctionNormalize.class.getResource( "normalization_rules.xml" );
            try {
                rules = new XMLFragment( url );
                nsContext = CommonNamespaces.getNamespaceContext();
                nsContext.addNamespace( "dgfunct", new URI( "http://www.deegree.org/filter/function" ) );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }

        }
    }

    /**
     * default constructor initializing rules if not already done
     *
     */
    FunctionNormalize() {
        initialize();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.model.filterencoding.Function#evaluate(org.deegree.model.feature.Feature)
     */
    @Override
    public Object evaluate( Feature feature )
                            throws FilterEvaluationException {
        Literal literal = (Literal) args.get( 0 );
        String s = literal.getValue().toUpperCase();
        try {
            List<Node> nodes = XMLTools.getNodes( rules.getRootElement(), "//dgfunct:Rule", nsContext );
            for ( Node node : nodes ) {
                String regex = XMLTools.getRequiredNodeAsString( node, "dgfunct:RegExpr", nsContext );
                String replacement = XMLTools.getRequiredNodeAsString( node, "dgfunct:Replacement", nsContext );
                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    LOG.logDebug( StringTools.concat( 500, "Rule: Match '", "' replace with '", "'" ) );
                }
                s = s.replaceAll( regex, replacement );
                LOG.logDebug( "Result: ", s );
            }
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new FilterEvaluationException( e.getMessage() );
        }

        return s;
    }

}
