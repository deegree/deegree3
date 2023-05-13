//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/main/java/org/deegree/metadata/iso/persistence/ISOMetadataStoreProvider.java $
package org.deegree.metadata.persistence;

import java.util.Iterator;
import java.util.List;

import org.deegree.metadata.MetadataRecord;
import org.deegree.protocol.csw.MetadataStoreException;

/**
 * Implementation of an {@link MetadataResultSet} encapsulating a {@link List} of
 * {@link MetadataRecord}s.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * @version $Revision: 30992 $, $Date: 2011-05-31 16:09:20 +0200 (Di, 31. Mai 2011) $
 */
public class ListMetadataResultSet<T extends MetadataRecord> implements MetadataResultSet<T> {

	private int requestedRecords = 0;

	private Iterator<T> iterator;

	private List<T> foundRecords;

	public ListMetadataResultSet(List<T> foundRecords) {
		this.foundRecords = foundRecords;
		iterator = foundRecords.iterator();
	}

	@Override
	public void close() throws MetadataStoreException {
		// nothing to do
	}

	@Override
	public boolean next() throws MetadataStoreException {
		return iterator.hasNext();
	}

	@Override
	public void skip(int rows) throws MetadataStoreException {
		throw new UnsupportedOperationException("skip is not implemented yet");
	}

	@Override
	public synchronized int getRemaining() throws MetadataStoreException {
		return foundRecords.size() - requestedRecords;
	}

	@Override
	public synchronized T getRecord() throws MetadataStoreException {
		requestedRecords++;
		return iterator.next();
	}

}
