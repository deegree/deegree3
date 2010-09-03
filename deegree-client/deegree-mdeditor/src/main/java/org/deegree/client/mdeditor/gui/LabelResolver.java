//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.client.mdeditor.gui;

import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class LabelResolver extends ELResolver {

    @Override
    public Object getValue( ELContext context, Object base, Object property )
                            throws NullPointerException, PropertyNotFoundException, ELException {
        if ( context == null ) {
            throw new NullPointerException();
        }
        if ( ( base instanceof ResourceBundle ) && ( property != null ) ) {
            try {
                Object result = ( (ResourceBundle) base ).getObject( property.toString() );
                context.setPropertyResolved( true );
                return result;
            } catch ( MissingResourceException mre ) {
                context.setPropertyResolved( true );
                return property.toString();
            }
        }
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType( ELContext context, Object base ) {
        if ( base instanceof ResourceBundle ) {
            return String.class;
        }
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors( ELContext context, Object base ) {
        List<FeatureDescriptor> list = Collections.emptyList();
        return list.iterator();
    }

    @Override
    public Class<?> getType( ELContext context, Object base, Object property ) {

        if ( context == null ) {
            throw new NullPointerException();
        }

        if ( base instanceof ResourceBundle ) {
            context.setPropertyResolved( true );
        }

        return null;
    }

    @Override
    public boolean isReadOnly( ELContext arg0, Object arg1, Object arg2 )
                            throws NullPointerException, PropertyNotFoundException, ELException {
        return true;
    }

    @Override
    public void setValue( ELContext arg0, Object arg1, Object arg2, Object arg3 )
                            throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException,
                            ELException {
    }

}
