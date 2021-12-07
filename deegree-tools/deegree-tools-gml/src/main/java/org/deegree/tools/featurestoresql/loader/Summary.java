package org.deegree.tools.featurestoresql.loader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.batch.core.StepExecution;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Summary {

    private String commitFailed;

    private int numberOfFeatures = 0;

    private Set<String> unresolvableReferences = new HashSet<>();

    private StepExecution stepExecution;

    /**
     * @param increaseBy
     *            integer to add (positive integer or null)
     */
    public void increaseNumberOfFeatures( int increaseBy ) {
        this.numberOfFeatures = this.numberOfFeatures + increaseBy;
    }

    /**
     * @return the number of features processed (positive integer or null)
     */
    public int getNumberOfFeatures() {
        return numberOfFeatures;
    }

    /**
     * @param unresolvableReferences
     *            list of unresolvable references, may be <code>null</code>
     */
    public void setUnresolvableReferences( Set<String> unresolvableReferences ) {
        this.unresolvableReferences = unresolvableReferences;
    }

    /**
     * @return <code>true</code> if unresolvable references are not empty, <code>false</code> otherwise
     */
    public boolean hasUnresolvableReferences() {
        return unresolvableReferences != null && !unresolvableReferences.isEmpty();
    }

    /**
     * @return list of unresolvable references, may be <code>null</code>
     */
    public Set<String> getUnresolvableReferences() {
        return unresolvableReferences;
    }

    /**
     * @param commitFailed
     *            the failure message of the commit
     */
    public void setCommitFailed( String commitFailed ) {
        this.commitFailed = commitFailed;
    }

    /**
     * @return <code>true</code> if the commit failed, <code>false</code> otherwise
     */
    public boolean isCommitFailed() {
        return commitFailed != null;
    }

    /**
     * @return the failure message of the commit
     */
    public String getCommitFailed() {
        return this.commitFailed;
    }

}