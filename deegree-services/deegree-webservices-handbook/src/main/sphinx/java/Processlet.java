package org.deegree.services.wps;

public interface Processlet {

    /**
     * Called by the {@link ProcessManager} to perform an execution of this {@link Processlet}.
     * <p>
     * The typical workflow is:
     * <ol>
     * <li>Get inputs from <code>in</code> parameter</li>
     * <li>Parse inputs into the required format (e.g. GML)</li>
     * <li>Do computation.</li>
     * <li>Transform computational results into required format (e.g. GML)</li>
     * <li>Write results to <code>out</code> parameter</li>
     * </ol>
     * 
     * @param in
     *            input arguments to be processed, never <code>null</code>
     * @param out
     *            used to store the process outputs, never <code>null</code>
     * @param info
     *            can be used to provide execution information, i.e. percentage completed and start/success messages
     *            that it wants to make known to clients, never <code>null</code>
     * @throws ProcessletException
     *             may be thrown by the processlet to indicate a processing exception
     */
    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException;

    /**
     * Called by the {@link ProcessManager} to indicate to a {@link Processlet} that it is being placed into service.
     */
    public void init();

    /**
     * Called by the {@link ProcessManager} to indicate to a {@link Processlet} that it is being taken out of service.
     * <p>
     * This method gives the {@link Processlet} an opportunity to clean up any resources that are being held (for
     * example, memory, file handles, threads) and make sure that any persistent state is synchronized with the
     * {@link Processlet}'s current state in memory.
     * </p>
     */
    public void destroy();
}

