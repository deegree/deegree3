//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/


package org.deegree.protocol.wps.ap.wcts;

/**
 * The <code>WCSTConstants</code> based on the inspire Draft Technical Guidance for INSPIRE Coordinate Transformation
 * Services Version 2 (07-09-2009).
 * 
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class WCTSConstants {

    /**
     * 
     * Inspire WPS application profile (WCTS) exception codes.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    public enum ExceptionCodes {
        /** No input data available from a specified source for the input. */
        NoInputData, 
        /** One or more points are outside of the the domainOfValidity of the in coordinate operation. */
        InvalidArea,
        /** Returned in case of a computation error occurring during a coordinate operation. */
        TransformException,
        /** Request is for an operation that is not supported by this service instance. */
        OperationNotSupported,
        /** Operation request contains an output CRS, which can not used within the output format. */
        UnsupportedCombination,
        /** The coordinate transformation with the defined parameters is possible <b>not an actual exception</b> */
        Transformable,
        /** The coordinate transformation can not be performed. */
        NotTransformable,
        /** The multiplicities of the input parameters are mutually in contradiction. */
        MutualExclusionException,
        
    }


}
