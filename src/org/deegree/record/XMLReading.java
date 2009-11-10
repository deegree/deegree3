//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.record;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class XMLReading extends Reader{
    String result ;
    boolean rdyflag = false ;
    long charsRead ; // in current Reader
    int sourceN ; // index of current Reader
    Object[] sources ;
    char eol = '\n' ;
    int[] lineCounts ; // per source






    
    public XMLReading(String result){
        this.result = result;
    }
    
    Reader rd = new StringReader(result);

    /* (non-Javadoc)
     * @see java.io.Reader#close()
     */
    @Override
    public void close()
                            throws IOException {
        rd.close() ; 
        rdyflag = false ;
        
    }
    
    public boolean ready() throws IOException { 
        return rd.ready() ;
     }
    
   
    /* (non-Javadoc)
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read( char[] cbuf, int off, int len )
                            throws IOException {
        int ct = rd.read( cbuf, off, len );
        if( ct == -1 ){
            if( nextReader() ){
              ct = rd.read( cbuf, off, len );
            } // if no next reader return -1
          }
          if( ct > 0 ){
             countLines( cbuf, off, ct );
          }
          charsRead += ct ;
          return ct ;

    }
    
    private boolean nextReader() throws IOException {
        close(); // sets rdyflag = false ;
        
        return rdyflag ;
      }

      // note that len is the number actually read
      private void countLines( char[] cbuf, int off, int len ){
        for( int i = 0 ; i < len ; i++ ){
          if( cbuf[ off++ ] == eol ){
            lineCounts[ sourceN ]++ ;
          }
        }
      }
      
      public String reportRelativeLine( int absN ){
          int runningLines = 0 ;
          for( int i = 0 ; i < sources.length ; i++ ){
            runningLines += lineCounts[i] ;
            if( absN <= runningLines ){
              int startN = runningLines - lineCounts[i] ;
              return "Source number: " + i +
                   " line: " + (absN - startN) ;
            }
          }
          return "Unable to locate line# " + absN ;
        }

}
