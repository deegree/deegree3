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

package org.deegree.model.metadata.iso19115;

import java.util.ArrayList;

/**
 * Represents a contact phone number.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$
 *
 * @since 2.0
 */
public class Phone {

    private ArrayList<String> facsimile;

    private ArrayList<String> other;

    private ArrayList<String> othertype;

    private ArrayList<String> voice;

    private Phone() {
        this.facsimile = new ArrayList<String>();
        this.other = new ArrayList<String>();
        this.othertype = new ArrayList<String>();
        this.voice = new ArrayList<String>();
    }

    /**
     * Creates a new instance of Phone
     *
     * @param facsimile
     * @param voice
     */
    public Phone( String[] facsimile, String[] voice ) {
        this();

        setFacsimile( facsimile );
        setVoice( voice );
    }

    /**
     * Creates a new instance of Phone
     *
     * @param facsimile
     * @param other
     * @param othertype
     * @param voice
     */
    public Phone( String[] facsimile, String[] other, String[] othertype, String[] voice ) {
        this();

        setFacsimile( facsimile );
        setOther( other );
        setOtherType( othertype );
        setVoice( voice );
    }

    /**
     * @return fax
     */
    public String[] getFacsimile() {
        return facsimile.toArray( new String[facsimile.size()] );
    }

    /**
     * @see #getFacsimile()
     * @param facsimile
     */
    public void addFacsimile( String facsimile ) {
        this.facsimile.add( facsimile );
    }

    /**
     * @see #getFacsimile()
     * @param facsimile
     */
    public void setFacsimile( String[] facsimile ) {
        this.facsimile.clear();
        for ( int i = 0; i < facsimile.length; i++ ) {
            this.facsimile.add( facsimile[i] );
        }
    }

    /**
     * @return other phones
     */
    public String[] getOther() {
        return other.toArray( new String[other.size()] );
    }

    /**
     * @see #getOther()
     * @param other
     */
    public void addOther( String other ) {
        this.other.add( other );
    }

    /**
     * @see #getOther()
     * @param other
     */
    public void setOther( String[] other ) {
        this.other.clear();
        if ( other != null ) {
            for ( int i = 0; i < other.length; i++ ) {
                this.other.add( other[i] );
            }
        }
    }

    /**
     * @return other types
     *
     */
    public String[] getOtherType() {
        return othertype.toArray( new String[othertype.size()] );
    }

    /**
     * @see #getOtherType()
     * @param othertype
     */
    public void addOtherType( String othertype ) {
        this.othertype.add( othertype );
    }

    /**
     * @see #getOtherType()
     * @param othertype
     */
    public void setOtherType( String[] othertype ) {
        this.othertype.clear();
        if ( othertype != null ) {
            for ( int i = 0; i < othertype.length; i++ ) {
                this.othertype.add( othertype[i] );
            }
        }
    }

    /**
     * @return voice
     */
    public String[] getVoice() {
        return voice.toArray( new String[voice.size()] );
    }

    /**
     * @see #getVoice()
     * @param voice
     */
    public void addVoice( String voice ) {
        this.voice.add( voice );
    }

    /**
     * @see #getVoice()
     * @param voice
     */
    public void setVoice( String[] voice ) {
        this.voice.clear();
        for ( int i = 0; i < voice.length; i++ ) {
            this.voice.add( voice[i] );
        }
    }

    /**
     * to String method
     *
     * @return string representation
     */
    public String toString() {
        String ret = null;
        ret = "facsimile = " + facsimile + "\n";
        ret += "other = " + other + "\n";
        ret += "othertype = " + othertype + "\n";
        ret += "voice = " + voice + "\n";
        return ret;
    }

}
