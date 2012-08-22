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

package org.deegree.io.dbaseapi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import org.deegree.model.spatialschema.ByteUtils;

/**
 * <p>
 * A class for reading from and writing to DBase index files (*.ndx), maybe not 100% xbase
 * compatible!
 * </p>
 *
 * <p>
 * The fileformat is described at http://www.e-bachmann.dk/computing/databases/xbase/index.html
 * </p>
 *
 * <p>
 * This index is suitable for indexing both unique and non-unique columns. Unique indexing is much
 * faster than non-unique because it use a faster algorithm.
 * </p>
 *
 * <p>
 * The index file is a B+tree (sometimes called a paged B-tree) that consist of pages. There are two
 * page types, leaves and non-leafs. The starting page (eg. the page the search algorithm starts) is
 * the root page.
 * </p>
 *
 * <p>
 * <b>Searching goes as follows:</b>
 * <ul>
 * <li>load the root page (eg. the starting page)</li>
 * <li>if the page is a leaf
 * <ul>
 * <li>search for the requested key</li>
 * </ul>
 * </li>
 * <li>if the page is not a leaf (eg. the page has subpages)
 * <ul>
 * <li>search for a key that is equal to or bigger than the requested key</li>
 * <li>load the lower page</li>
 * <li>continue searching inside the lower page</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * Above algorithm is implemented in two different methods, one for unique indexes and one for
 * non-unique indexes. Searching unique indexes is easier because the algorithm is finished as soon
 * as it has found a key, the non-unique version of the algorithm has to find all keys present in
 * the index.
 * <p>
 *
 * <p>
 * <b>Inserting goes as follows:</b>
 * <ul>
 * <li>find the leaf page the key has to insert in</li>
 * <li>insert the key in the leaf page</li>
 * <li>if the leaf page is full (eg. validEntries > noOfKeysPerPage)
 * <ul>
 * <li>split the leaf page (results in two leaf pages)</li>
 * <li>add the first item of the new page to the parent page</li>
 * </ul>
 * </li>
 * <li>if the parent page is also full
 * <ul>
 * <li>split the parent page
 * <li>
 * <li>add the first item of the new page to it's parent page</li>
 * <li>etc.</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * If a page that splits does not have a parent page then a new page is created. This page is the
 * new starting page
 * </p>
 *
 * <p>
 * Handling different data types: The index can handle strings and numbers. Numbers are always
 * stored als IEEE doubles. The method addKey checks the given key and throws an exception if the
 * datatype of the key doesn't suit the index
 * </p>
 *
 * @author Reijer Copier, email: reijer.copier@idgis.nl
 */

public class DBaseIndex {
    // The filename we use (this variable is used by toString)
    private String fileName;

    // The random access file we use
    protected RandomAccessFile file;

    // Attributes stored in the .ndx header
    protected int startingPageNo;

    // Attributes stored in the .ndx header
    protected int numberOfPages;

    // Attributes stored in the .ndx header
    protected int sizeOfKeyRecord;

    // Attributes stored in the .ndx header
    protected int keyLength;

    // Attributes stored in the .ndx header
    protected int noOfKeysPerPage;

    // Attributes stored in the .ndx header
    protected int keyType;

    private boolean uniqueFlag;

    // Buffers
    protected byte[] b = new byte[4];

    // Buffers
    protected byte[] page = new byte[512];

    // Buffers
    protected byte[] keyBytes;

    // Cache size
    protected int cacheSize = 20;

    // Cache
    private Cache cache = new Cache();

    /**
     * Inner class for the cache. The cache remembers recently used pages.
     */
    public class Cache {

        /**
         * Inner class for the cache items
         */

        class Item implements Comparable {
            /**
             * Create a new item with the given page
             */
            Item( Page p ) {
                this.p = p;
                timeStamp = System.currentTimeMillis();
            }

            /**
             * Mark the item as used (eg. create a new time stamp)
             */
            void use() {
                timeStamp = System.currentTimeMillis();
            }

            long timeStamp;

            Page p;

            /**
             * Compare the time stamp from this object to the time stamp of another object
             */
            public int compareTo( Object o ) {
                return new Long( timeStamp ).compareTo( new Long( ( (Item) o ).timeStamp ) );
            }
        }

        private Hashtable<Integer, Item> pages;

        private LinkedList<Item> cacheItems;

        /**
         * Create a new cache
         */
        public Cache() {
            pages = new Hashtable<Integer, Item>();
            cacheItems = new LinkedList<Item>();
        }

        /**
         * Remove an item from the cache (this method searches for the last used item)
         */
        void removeItem()
                                throws IOException {
            Item i = cacheItems.removeFirst();

            if ( i.p.onStoreList )
                i.p.write();

            pages.remove( new Integer( i.p.number ) );
        }

        /**
         * Insert a new item into the cache
         */
        public void insert( int number, Page p )
                                throws IOException {
            Item i = new Item( p );

            pages.put( new Integer( number ), i );
            cacheItems.addLast( i );

            if ( cacheItems.size() > cacheSize )
                removeItem();
        }

        /**
         * Get a page form the cache
         *
         * @return returns the addressed page or <code>null</code>
         */
        public Page get( int number ) {
            Item item = pages.get( new Integer( number ) );

            if ( item != null ) {
                cacheItems.remove( item );
                item.use();
                cacheItems.addLast( item );

                return item.p;
            }
            return null;
        }

        /**
         * Flush the cache (eg. store modified pages)
         */
        public void flush() {
            ListIterator i = cacheItems.listIterator();

            while ( i.hasNext() ) {
                Item item = (Item) i.next();

                try {
                    if ( item.p.onStoreList ) {
                        item.p.write();
                    }
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }

            cacheItems.clear();
            pages.clear();
        }
    }

    /**
     * Inner class for the key entries
     */
    private class KeyEntry {
        // Lower pointer and record number
        int lower;

        // Lower pointer and record number
        int record;

        // Data
        Comparable data;

        /**
         * Construct a new KeyEntry
         */
        KeyEntry( int lower, int record, Comparable data ) {
            this.lower = lower;
            this.record = record;
            this.data = data;
        }

        /**
         * Read an existing KeyEntry
         */
        KeyEntry( int lower, int record ) throws IOException {
            this.lower = lower;
            this.record = record;
            read();
        }

        /**
         * Compare this key entry to another key
         */
        int compareTo( Comparable key ) {
            return this.data.compareTo( key );
        }

        /**
         * Read data from current file position
         */
        void read()
                                throws IOException {
            if ( keyType == 0 ) {
                file.read( keyBytes );
                data = new String( keyBytes ).trim();
            } else {
                data = new Double( file.readDouble() );
            }
        }

        /**
         * Write data to current file position
         */
        void write()
                                throws IOException {
            if ( keyType == 0 ) {
                byte[] currentKeyBytes = ( (String) data ).getBytes();
                file.write( currentKeyBytes );
                file.write( new byte[keyLength - currentKeyBytes.length] );
            } else {
                file.writeDouble( ( (Double) data ).doubleValue() );
            }
        }
    }

    /**
     * Inner class for the pages
     */

    private class Page {
        /**
         * Page numer, number of valid entries and the last lower pointer
         */
        int number;

        /**
         * Page numer, number of valid entries and the last lower pointer
         */
        int validEntries;

        /**
         * Page numer, number of valid entries and the last lower pointer
         */
        int lastLower;

        /**
         * An array with the key entries;
         */
        KeyEntry[] entries = new KeyEntry[noOfKeysPerPage + 1];

        /**
         * Is this page on the store list?
         */
        boolean onStoreList;

        /**
         * This constructor is only used by newPage(), it creates an empty page
         */
        Page() {
            validEntries = 0;
            lastLower = 0;
            onStoreList = true;
        }

        /**
         * This constructor is only used by getPage(), it loads a page from the file
         */
        Page( int number ) throws IOException {
            this.number = number;
            onStoreList = false;

            // Seek to the page
            file.seek( number * 512 );
            // Read the number of valid entries
            file.read( b );
            validEntries = ByteUtils.readLEInt( b, 0 );

            // Read the key entries
            for ( int i = 0; i < validEntries; i++ ) {
                int lower, record;

                // Read the lower pointer
                file.read( b );
                lower = ByteUtils.readLEInt( b, 0 );

                // Read the record number
                file.read( b );
                record = ByteUtils.readLEInt( b, 0 );

                // Store the key in the array
                entries[i] = new KeyEntry( lower, record );

                // Skip some unused bytes
                file.skipBytes( sizeOfKeyRecord - ( keyLength + 8 ) );
            }
            // Read the last lower pointer
            file.read( b );
            lastLower = ByteUtils.readLEInt( b, 0 );
        }

        /**
         * Write the page to disk
         */
        void write()
                                throws IOException {
            file.seek( number * 512 );
            // Write the number of valid entries
            ByteUtils.writeLEInt( b, 0, validEntries );
            file.write( b );

            // Write all the key entries
            for ( int i = 0; i < validEntries; i++ ) {
                // Write the lower pointer
                ByteUtils.writeLEInt( b, 0, entries[i].lower );
                file.write( b );

                // Write the the recordnumber
                ByteUtils.writeLEInt( b, 0, entries[i].record );
                file.write( b );

                // Write the key
                entries[i].write();

                for ( int j = 0; j < keyLength - keyBytes.length; j++ )
                    file.write( 0x20 );

                file.skipBytes( sizeOfKeyRecord - ( keyLength + 8 ) );
            }
            // Write the last lower pointer
            ByteUtils.writeLEInt( b, 0, lastLower );
            file.write( b );

            long size = ( ( number + 1 ) * 512 ) - file.getFilePointer();
            file.write( new byte[(int) size] );
        }

        /**
         * This method is called if saving is needed
         */
        void store() {
            onStoreList = true;
        }

        /**
         * Search in this page (and lower pages)
         */
        int search( Comparable key, Stack<Integer> searchStack )
                                throws IOException {
            if ( validEntries == 0 ) // Page is empty
            {
                return -number;
            }

            if ( entries[0].lower == 0 ) // This page is a leaf
            {
                for ( int i = 0; i < validEntries; i++ ) {
                    if ( entries[i].compareTo( key ) == 0 )

                        return entries[i].record;
                }

                return -number;
            }

            for ( int i = 0; i < validEntries; i++ ) {
                int compare = entries[i].compareTo( key );

                if ( compare == 0 || compare > 0 ) {
                    Page lowerPage = getPage( entries[i].lower );
                    if ( searchStack != null )
                        searchStack.push( new Integer( number ) );
                    return lowerPage.search( key, searchStack );
                }
            }

            Page lowerPage = getPage( lastLower );
            if ( searchStack != null )
                searchStack.push( new Integer( number ) );
            return lowerPage.search( key, searchStack );

        }

        /**
         * Search in this page (and lower pages), duplicates allowed
         */
        ArrayList<Integer> searchDup( Comparable key )
                                throws IOException {
            ArrayList<Integer> found = new ArrayList<Integer>( 100 );

            if ( validEntries != 0 ) // Page is not emtpy
            {
                if ( entries[0].lower == 0 ) // Page is a leaf
                {
                    for ( int i = 0; i < validEntries; i++ ) {
                        if ( entries[i].compareTo( key ) == 0 ) {
                            found.add( new Integer( entries[i].record ) );
                        }
                    }
                } else {
                    for ( int i = 0; i < validEntries; i++ ) {
                        if ( entries[i].compareTo( key ) >= 0 ) {
                            ArrayList<Integer> lowerFound = getPage( entries[i].lower ).searchDup( key );
                            if ( lowerFound.size() != 0 )
                                found.addAll( lowerFound );
                            else
                                return found;
                        }
                    }

                    found.addAll( getPage( lastLower ).searchDup( key ) );
                }
            }

            return found;
        }

        /**
         * Find the insert position for a key
         */
        int searchDupPos( Comparable key, Stack<Integer> searchStack )
                                throws IOException {
            if ( validEntries == 0 ) // Page is empty
                return number;

            if ( entries[0].lower == 0 ) // Page is a leaf
                return number;

            for ( int i = 0; i < validEntries; i++ ) {
                if ( entries[i].compareTo( key ) >= 0 ) {
                    Page lowerPage = getPage( entries[i].lower );
                    searchStack.push( new Integer( number ) );
                    return lowerPage.searchDupPos( key, searchStack );
                }
            }

            Page lowerPage = getPage( lastLower );
            searchStack.push( new Integer( number ) );
            return lowerPage.searchDupPos( key, searchStack );
        }

        /**
         * Add a node to this page, this method is only called if page is non-leaf page
         */
        void addNode( Comparable key, int left, int right, Stack searchStack )
                                throws IOException {
            for ( int i = 0; i < validEntries + 1; i++ ) {
                if ( i == validEntries ) {
                    entries[i] = new KeyEntry( left, 0, key );
                    lastLower = right;
                    break;
                }

                if ( left == entries[i].lower ) {
                    for ( int j = validEntries - 1; j >= i; j-- ) {
                        entries[j + 1] = entries[j];
                    }
                    entries[i] = new KeyEntry( left, 0, key );
                    entries[i + 1].lower = right;
                    break;
                }
            }

            validEntries++;

            if ( validEntries > noOfKeysPerPage ) // Split
            {
                Page newPage = newPage();

                int firstEntry = validEntries / 2;

                KeyEntry parentKey = entries[firstEntry];
                firstEntry++;

                int j = 0;
                for ( int i = firstEntry; i < validEntries; i++ ) {
                    newPage.entries[j] = entries[i];
                    j++;
                }

                newPage.validEntries = j;
                validEntries -= newPage.validEntries + 1;

                newPage.lastLower = lastLower;
                lastLower = parentKey.lower;

                Page parent;
                if ( searchStack.size() == 0 ) {
                    parent = newPage();
                    setRoot( parent );
                } else
                    parent = getPage( ( (Integer) searchStack.pop() ).intValue() );

                parent.addNode( parentKey.data, number, newPage.number, searchStack );
            }
            store();
        }

        /**
         * Add a key to this page, only for leaf nodes
         */
        void addKey( Comparable key, int record, Stack searchStack )
                                throws IOException {
            for ( int i = 0; i < validEntries + 1; i++ ) {
                if ( i == validEntries ) {
                    entries[validEntries] = new KeyEntry( 0, record, key );
                    break;
                }

                if ( entries[i].compareTo( key ) >= 0 ) {
                    for ( int j = validEntries - 1; j >= i; j-- ) {
                        entries[j + 1] = entries[j];
                    }
                    entries[i] = new KeyEntry( 0, record, key );
                    break;
                }
            }

            validEntries++;

            if ( validEntries == noOfKeysPerPage ) // Split
            {
                Page newPage = newPage();

                int firstEntry = validEntries / 2 + 1;
                if ( ( validEntries % 2 ) != 0 )
                    firstEntry++;

                int j = 0;
                for ( int i = firstEntry; i < validEntries; i++ ) {
                    newPage.entries[j] = entries[i];
                    j++;
                }

                newPage.validEntries = validEntries - firstEntry;
                validEntries -= newPage.validEntries;

                Page parent;
                if ( searchStack.size() == 0 ) {
                    parent = newPage();
                    setRoot( parent );
                } else
                    parent = getPage( ( (Integer) searchStack.pop() ).intValue() );
                parent.addNode( entries[validEntries - 1].data, number, newPage.number, searchStack );
            }

            store();
        }

        /**
         * Calculate the depth for this page
         */
        int getDepth()
                                throws IOException {
            if ( validEntries == 0 ) {
                throw new IOException( "valid entries must be > 0" );
            }

            if ( entries[0].lower == 0 ) {
                return 1;
            }
            Page lowerPage = getPage( entries[0].lower );
            return lowerPage.getDepth() + 1;

        }

        /**
         * Convert the page to a string (for debugging)
         */
        public String toString() {
            String s = "Number: " + number + "\nValidEntries: " + validEntries + "\n";

            for ( int i = 0; i < validEntries; i++ ) {
                s += "entry: " + i + "\n";

                KeyEntry key = entries[i];
                s += "  lower: " + key.lower + "\n";
                s += "  record: " + key.record + "\n";
                s += "  data: " + key.data + "\n";
            }
            s += "lower: " + lastLower;
            return s;
        }
    }

    /**
     * Open an existing .ndx file
     */
    public DBaseIndex( String name ) throws IOException {
        File f = new File( name + ".ndx" );

        if ( !f.exists() )
            throw new FileNotFoundException();

        fileName = name;
        file = new RandomAccessFile( f, "rw" );

        file.read( b );
        startingPageNo = ByteUtils.readLEInt( b, 0 );

        file.read( b );
        numberOfPages = ByteUtils.readLEInt( b, 0 );

        file.skipBytes( 4 ); // Reserved
        file.read( b, 0, 2 );
        keyLength = ByteUtils.readLEShort( b, 0 );

        file.read( b, 0, 2 );
        noOfKeysPerPage = ByteUtils.readLEShort( b, 0 );

        file.read( b, 0, 2 );
        keyType = ByteUtils.readLEShort( b, 0 );

        file.read( b );
        sizeOfKeyRecord = ByteUtils.readLEInt( b, 0 );

        file.skipBytes( 1 ); // Reserved
        uniqueFlag = file.readBoolean();

        keyBytes = new byte[keyLength];
    }

    /**
     * Used by createIndex()
     */
    private DBaseIndex( String name, int startingPageNo, int numberOfPages, int sizeOfKeyRecord, int keyLength,
                        int noOfKeysPerPage, int keyType, boolean uniqueFlag, RandomAccessFile file ) {
        fileName = name;
        this.startingPageNo = startingPageNo;
        this.numberOfPages = numberOfPages;
        this.sizeOfKeyRecord = sizeOfKeyRecord;
        this.keyLength = keyLength;
        this.noOfKeysPerPage = noOfKeysPerPage;
        this.keyType = keyType;
        this.uniqueFlag = uniqueFlag;
        this.file = file;

        keyBytes = new byte[keyLength];
    }

    /**
     * Get a page
     */
    protected Page getPage( int number )
                            throws IOException {
        Page p;

        synchronized ( cache ) {
            p = cache.get( number );

            if ( p == null ) {
                p = new Page( number );
                cache.insert( number, p );
            }
        }

        if ( p.validEntries != 0 ) {
            if ( p.entries[0].lower != 0 ) {
                Hashtable<Integer, String> test = new Hashtable<Integer, String>();
                for ( int i = 0; i < p.validEntries; i++ ) {
                    test.put( new Integer( p.entries[i].lower ), "" );
                }
                test.put( new Integer( p.lastLower ), "" );
                if ( test.size() != p.validEntries + 1 ) {
                    throw new IOException( "Error in page " + p.number );
                }
            }
        }

        return p;
    }

    /**
     * Create a new page
     */
    protected Page newPage()
                            throws IOException {
        Page p;

        synchronized ( cache ) {
            numberOfPages++;

            p = new Page();
            p.number = numberOfPages;

            cache.insert( p.number, p );
            p.write();
        }
        return p;
    }

    /**
     * Set the root page
     */
    protected synchronized void setRoot( Page page ) {
        startingPageNo = page.number;
    }

    /**
     * Create a new index
     */
    public static DBaseIndex createIndex( String name, String column, int keyLength, boolean uniqueFlag, boolean numbers )
                            throws IOException {
        RandomAccessFile file = new RandomAccessFile( name + ".ndx", "rw" );

        int startingPageNo = 1, numberOfPages = 1, sizeOfKeyRecord, noOfKeysPerPage, keyType = numbers ? 1 : 0;

        if ( numbers )
            keyLength = 8;

        sizeOfKeyRecord = 8 + keyLength;

        while ( ( sizeOfKeyRecord % 4 ) != 0 )
            sizeOfKeyRecord++;

        noOfKeysPerPage = 504 / sizeOfKeyRecord;

        byte[] b = new byte[4];

        ByteUtils.writeLEInt( b, 0, startingPageNo );
        file.write( b );

        ByteUtils.writeLEInt( b, 0, numberOfPages );
        file.write( b );

        file.writeInt( 0 ); // Reserved

        ByteUtils.writeLEShort( b, 0, keyLength );
        file.write( b, 0, 2 );

        ByteUtils.writeLEShort( b, 0, noOfKeysPerPage );
        file.write( b, 0, 2 );

        ByteUtils.writeLEShort( b, 0, keyType );
        file.write( b, 0, 2 );

        ByteUtils.writeLEInt( b, 0, sizeOfKeyRecord );
        file.write( b );

        file.write( 0 ); // Reserved

        file.writeBoolean( uniqueFlag );

        file.write( column.getBytes() );

        for ( int i = 0; i < 180 - column.length(); i++ )
            file.write( 0x20 );

        for ( int i = 0; i < 820; i++ )
            file.write( 0 );

        return new DBaseIndex( name, startingPageNo, numberOfPages, sizeOfKeyRecord, keyLength, noOfKeysPerPage,
                               keyType, uniqueFlag, file );
    }

    /**
     * Flush all the buffers
     */
    public void flush()
                            throws IOException {
        file.seek( 0 );
        ByteUtils.writeLEInt( b, 0, startingPageNo );
        file.write( b );

        ByteUtils.writeLEInt( b, 0, numberOfPages );
        file.write( b );

        cache.flush();
    }

    /**
     * Close the index file
     */
    public void close()
                            throws IOException {
        flush();
        file.close();
    }

    public int[] search( Comparable key )
                            throws IOException, KeyNotFoundException, InvalidKeyTypeException {
        if ( key == null )
            throw new NullPointerException();

        if ( ( keyType == 0 && !( key instanceof String ) ) || ( keyType == 1 && !( key instanceof Number ) ) ) {
            throw new InvalidKeyTypeException( key, this );
        }

        if ( keyType == 1 && !( key instanceof Double ) ) {
            key = new Double( ( (Number) key ).doubleValue() );
        }

        Page root = getPage( startingPageNo );

        if ( uniqueFlag ) {
            int[] retval = new int[1];
            retval[0] = root.search( key, null );
            if ( retval[0] < 0 ) {
                throw new KeyNotFoundException( key, this );
            }
            return retval;
        }
        ArrayList searchResult = root.searchDup( key );
        if ( searchResult.size() == 0 ) {
            throw new KeyNotFoundException( key, this );
        }
        int[] retval = new int[searchResult.size()];
        for ( int i = 0; i < retval.length; i++ )
            retval[i] = ( (Integer) searchResult.get( i ) ).intValue();
        return retval;

    }

    /**
     * Add a key to the index
     */
    public void addKey( Comparable key, int record )
                            throws IOException, KeyAlreadyExistException, InvalidKeyTypeException, KeyTooLongException {
        if ( key == null )
            throw new NullPointerException();

        if ( ( keyType == 0 && !( key instanceof String ) ) || ( keyType == 1 && !( key instanceof Number ) ) ) {
            throw new InvalidKeyTypeException( key, this );
        }

        if ( keyType == 1 && !( key instanceof Double ) ) {
            key = new Double( ( (Number) key ).doubleValue() );
        }

        if ( key instanceof String ) {
            if ( ( (String) key ).length() > keyLength ) {
                throw new KeyTooLongException( key, this );
            }
        }

        Page root = getPage( startingPageNo );
        Stack<Integer> stack = new Stack<Integer>();

        if ( uniqueFlag ) {
            int searchResult = root.search( key, stack );
            if ( searchResult >= 0 ) {
                throw new KeyAlreadyExistException( key, this );
            }

            getPage( -searchResult ).addKey( key, record, stack );
        } else {
            int searchResult = root.searchDupPos( key, stack );
            getPage( searchResult ).addKey( key, record, stack );
        }
    }

    /**
     * Calculate the depth for the index
     */
    public int getDepth()
                            throws IOException {
        Page root = getPage( startingPageNo );
        return root.getDepth();
    }

    /**
     * Contains this index unique values?
     */
    public boolean isUnique() {
        return uniqueFlag;
    }

    public String toString() {
        return fileName;
    }
}
