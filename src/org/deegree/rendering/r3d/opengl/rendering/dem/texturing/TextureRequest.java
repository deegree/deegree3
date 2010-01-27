//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.opengl.rendering.dem.texturing;

import org.deegree.rendering.r3d.opengl.rendering.dem.RenderMeshFragment;

/**
 * Represents the request for a {@link FragmentTexture} for a {@link RenderMeshFragment}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class TextureRequest extends TextureTileRequest {

    private RenderMeshFragment fragment;

    /**
     * Init a request
     * 
     * @param fragment
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param metersPerPixel
     */
    public TextureRequest( RenderMeshFragment fragment, double minX, double minY, double maxX, double maxY,
                           float metersPerPixel ) {
        super( minX, minY, maxX, maxY, metersPerPixel );
        this.fragment = fragment;
    }

    /**
     * @return the fragment this request is generated for.
     */
    public RenderMeshFragment getFragment() {
        return fragment;
    }

    @Override
    public boolean equals( Object o ) {
        if ( !( o instanceof TextureRequest ) ) {
            return false;
        }
        TextureRequest that = (TextureRequest) o;
        return this.fragment.getId() == that.fragment.getId()
               && ( getUnitsPerPixel() - that.getUnitsPerPixel() ) < 0.001f;
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2nd millionth prime, :-)
        long code = 32452843;
        code = code * 37 + this.fragment.getId();
        code = code * 37 + Float.floatToIntBits( getUnitsPerPixel() );
        return (int) ( code >>> 32 ) ^ (int) code;
    }

}
