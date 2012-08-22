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
package org.deegree.ogcwebservices.wms.capabilities;



/**
 * The Extent element indicates what _values_ along a dimension are valid.
 * <p>----------------------------------------------------------------------</p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$
 */
public class Extent {
    private String default_ = null;
    private String name = null;
    private boolean useNearestValue = false;
    private String value = null;

    /**
    * constructor initializing the class with the <Extent>
    */
    Extent( String name, String default_, boolean useNearestValue ) {
        setName( name );
        setDefault( default_ );
        setUseNearestValue( useNearestValue );
    }

    /**
     * constructor initializing the class with the <Extent>
     * @param name
     * @param default_
     * @param useNearestValue
     * @param value
     */
    public Extent( String name, String default_, boolean useNearestValue, String value ) {
         this( name, default_, useNearestValue );
         this.value = value;
     }

    /**
     * @return the name of the extent
     */
    public String getName() {
        return name;
    }

    /**
    * sets the name of the extent
     * @param name
    */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @return the default extent
     */
    public String getDefault() {
        return default_;
    }

    /**
    * sets the default extent
     * @param default_
    */
    public void setDefault( String default_ ) {
        this.default_ = default_;
    }

    /**
     * @return true if a WMS should use the extent that is nearest to
     * the requested level.
     */
    public boolean useNearestValue() {
        return useNearestValue;
    }

    /**
    * sets true if a WMS should use the extent that is nearest to
    * the requested level.
     * @param useNearestValue
    */
    public void setUseNearestValue( boolean useNearestValue ) {
        this.useNearestValue = useNearestValue;
    }

    /**
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @param value
     */
    public void setValue( String value ) {
        this.value = value;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "name = " + name + "\n";
        ret += ( "default_ = " + default_ + "\n" );
        ret += ( "useNearestValue = " + useNearestValue + "\n" );
        return ret;
    }

}
