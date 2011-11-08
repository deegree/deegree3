package org.deegree.tools.metadata;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.MetadataRecordFactory;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreManager;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.protocol.csw.client.CSWClient;
import org.deegree.protocol.csw.client.transaction.TransactionResponse;
import org.deegree.protocol.ows.exception.OWSExceptionReport;

/**
 * 
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author: admin $
 * 
 * @version $Revision: $, $Date: $
 */

@Tool(value = "harvest metadata records and insert them in a CSW")
public class Harvester {

    private static final String xPathFileId = "//gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString";

    private static final String xPathId = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/*/gmd:code/gco:CharacterString";

    private static final String OPT_CSW_ADDRESS = "csw";

    private static final String OPT_SOURCE = "source";

    private static final String OPT_WORKSPACE = "workspace";

    private final boolean verbose;

    /**
     * 
     * @param verbose
     * @throws IOException
     */
    public Harvester( boolean verbose ) {
        this.verbose = verbose;
    }

    public void run( final String srcOpt, final String cswAddr )
                            throws Exception {

        CSWHarvester harvester = new CSWHarvester() {

            @Override
            boolean insertRecord( OMElement record )
                                    throws Exception {
                try {
                    CSWClient cswClient = new CSWClient( new URL( cswAddr ) );
                    TransactionResponse insert = cswClient.insert( record );
                    return insert.getNumberOfRecordsInserted() > 0;
                } catch ( OWSExceptionReport e ) {
                    throw e;
                } catch ( Exception e ) {
                    System.err.println( "An error occured during inserting: " + e.getMessage() );
                    if ( verbose )
                        e.printStackTrace();
                }
                return false;
            }
        };
        harvester.run( srcOpt );
    }

    public void run( String srcOpt, String workspaceOpt, String storeOpt )
                            throws Exception {
        DeegreeWorkspace dw = DeegreeWorkspace.getInstance( workspaceOpt );
        dw.initAll();
        MetadataStoreManager manager = dw.getSubsystemManager( MetadataStoreManager.class );
        final MetadataStore<?> metadataStore = manager.get( storeOpt );

        CSWHarvester harvester = new CSWHarvester() {

            @Override
            boolean insertRecord( OMElement record )
                                    throws Exception {
                MetadataStoreTransaction trans = metadataStore.acquireTransaction();
                try {
                    List<String> performInsert = trans.performInsert( new InsertOperation(
                                                                                           Collections.singletonList( MetadataRecordFactory.create( record ) ),
                                                                                           null, null ) );
                    trans.commit();
                    if ( performInsert.isEmpty() )
                        return false;
                    return true;
                } catch ( Exception e ) {
                    trans.rollback();
                    throw e;
                }
            }

        };
        harvester.run( srcOpt );
    }

    private abstract class CSWHarvester {

        void run( String inputDir )
                                throws Exception {
            // basic Transaction request without any filter constraints;
            int count = 0; // total files written
            int countFailed = 0;

            File folder = new File( inputDir );
            File[] listOfFiles = folder.listFiles();

            System.out.println( "Reading XML files from: " + inputDir );
            for ( int i = 0; i < listOfFiles.length; i++ ) {
                System.out.println( listOfFiles[i].getName() );
                XMLAdapter xml = new XMLAdapter( listOfFiles[i] );
                // TODO
                // if ( redefineIDs ) {
                // OMElement elem = xml.getElement( xml.getRootElement(),
                // new XPath( xPathFileId, CommonNamespaces.getNamespaceContext() ) );
                // XMLTools.setNodeValue( elem, UUID.randomUUID().toString() );
                // elem = xml.getElement( xml.getRootElement(),
                // new XPath( xPathId, CommonNamespaces.getNamespaceContext() ) );
                // sFile = xml.getAsPrettyString();
                // }
                try {
                    boolean inserted = insertRecord( xml.getRootElement() );
                    if ( inserted )
                        count++;
                } catch ( Exception e ) {
                    countFailed++;
                    final String fi = xml.getNodeAsString( xml.getRootElement(),
                                                           new XPath( xPathFileId,
                                                                      CommonNamespaces.getNamespaceContext() ), null );
                    System.out.println( "ignore record with fileIdentifier " + fi + ", insert failed: "
                                        + e.getMessage() );
                    if ( verbose )
                        e.printStackTrace();
                }
            }

            System.out.println( "\n" + "Done. " + count + " records harvested." );
        }

        abstract boolean insertRecord( OMElement record )
                                throws Exception;
    }

    public static void main( String[] args )
                            throws Exception {

        if ( args.length == 0 || ( args.length > 0 && ( args[0].contains( "help" ) || args[0].contains( "?" ) ) ) ) {
            printHelp( initOptions() );
        }
        boolean verbose = true;
        try {
            CommandLine cmdline = new PosixParser().parse( initOptions(), args );
            verbose = cmdline.hasOption( CommandUtils.OPT_VERBOSE );
            try {
                String cswOpt = cmdline.getOptionValue( OPT_CSW_ADDRESS );
                String srcOpt = cmdline.getOptionValue( OPT_SOURCE );
                String workspaceOpt = cmdline.getOptionValue( OPT_WORKSPACE );

                Harvester csw = new Harvester( verbose );
                if ( cswOpt == null && workspaceOpt == null ) {
                    System.err.println( "The harvesting target is not passed. Either " + OPT_CSW_ADDRESS + " or "
                                        + OPT_WORKSPACE + " must be passed!" );
                    System.exit( 1 );
                }
                if ( cswOpt != null ) {
                    csw.run( srcOpt, cswOpt );
                } else {
                    String[] split = workspaceOpt.split( ":" );
                    if ( split.length < 2 ) {
                        System.err.println( "The workspace/store is not correct. Must be seperated by a ':', e.g. workspace:store" );
                        System.exit( 1 );
                    }
                    csw.run( srcOpt, split[0], split[1] );
                }
            } catch ( Exception e ) {
                System.out.println( e.getMessage() );
                if ( verbose )
                    e.printStackTrace();
            }
        } catch ( ParseException exp ) {
            System.err.println( "Could nor parse command line:" + exp.getMessage() );
            if ( verbose )
                exp.printStackTrace();
        }
    }

    private static Options initOptions() {
        Options opts = new Options();

        Option opt = new Option( "s", OPT_SOURCE, true, "the directory containing the metadada records" );
        opt.setRequired( true );
        opts.addOption( opt );

        opt = new Option( "c", OPT_CSW_ADDRESS, true,
                          "the GetCapabialites URL of the target CSW, either this parameter or " + OPT_WORKSPACE
                                                  + " must be passed" );
        opt.setRequired( false );
        opts.addOption( opt );

        opt = new Option(
                          "w",
                          OPT_WORKSPACE,
                          true,
                          "the workspace/store containing the target metadatastore, workspace and store must be seperated by a ':', e.g: workspace:store. Either this parameter or the "
                                                  + OPT_CSW_ADDRESS + " must be passed." );
        opt.setRequired( false );
        opts.addOption( opt );

        CommandUtils.addDefaultOptions( opts );
        return opts;
    }

    private static void printHelp( Options options ) {
        String help = "Reads records from the passed CSW, validates them against the INSPIRE metadata validator and write all records and the validation results in the output directory.";
        CommandUtils.printHelp( options, Harvester.class.getSimpleName(), help, null );
    }

}
