package org.deegree.tools.metadata;

import static org.deegree.commons.xml.CommonNamespaces.ISOAP10GCONS;
import static org.deegree.commons.xml.CommonNamespaces.ISOAP10GCO_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.ISOAP10GMDNS;
import static org.deegree.commons.xml.CommonNamespaces.ISOAP10GMD_PREFIX;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.MetadataRecordFactory;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreProvider;
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
 */

@Tool(value = "harvest metadata records and insert them in a CSW")
public class Harvester {

	private static final NamespaceBindings NAMESPACE_CONTEXT = CommonNamespaces.getNamespaceContext();

	private static final String xPathFileId = "//gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString";

	private static final String OPT_CSW_ADDRESS = "csw";

	private static final String OPT_SOURCE = "source";

	private static final String OPT_WORKSPACE = "workspace";

	private static final String OPT_SET_FI = "fileIdentifier";

	private static final String OPT_FI_METHOD = "method";

	public enum CREATEFI {

		RETAIN, REPLACE, MISSING

	}

	public enum METHODFI {

		UUID, FILENAME

	}

	private final boolean verbose;

	private final CREATEFI createFI;

	private final METHODFI methodFI;

	/**
	 * @param verbose
	 * @throws IOException
	 */
	public Harvester(boolean verbose, METHODFI methodFI, CREATEFI createFI) {
		this.verbose = verbose;
		this.methodFI = methodFI;
		this.createFI = createFI;
	}

	public void run(final String srcOpt, final String cswAddr) throws Exception {

		CSWHarvester harvester = new CSWHarvester() {

			@Override
			boolean insertRecord(OMElement record) throws Exception {
				try {
					CSWClient cswClient = new CSWClient(new URL(cswAddr));
					TransactionResponse insert = cswClient.insert(record);
					return insert.getNumberOfRecordsInserted() > 0;
				}
				catch (OWSExceptionReport e) {
					throw e;
				}
				catch (Exception e) {
					System.err.println("An error occured during inserting: " + e.getMessage());
					if (verbose)
						e.printStackTrace();
				}
				return false;
			}
		};
		harvester.run(srcOpt);
	}

	public void run(String srcOpt, String workspaceOpt, String storeOpt) throws Exception {
		DeegreeWorkspace dw = DeegreeWorkspace.getInstance(workspaceOpt);
		dw.initAll();
		final MetadataStore<?> metadataStore = dw.getNewWorkspace().getResource(MetadataStoreProvider.class, storeOpt);

		CSWHarvester harvester = new CSWHarvester() {

			@Override
			boolean insertRecord(OMElement record) throws Exception {
				MetadataStoreTransaction trans = metadataStore.acquireTransaction();
				try {
					List<String> performInsert = trans.performInsert(new InsertOperation(
							Collections.singletonList(MetadataRecordFactory.create(record)), null, null));
					trans.commit();
					if (performInsert.isEmpty())
						return false;
					return true;
				}
				catch (Exception e) {
					trans.rollback();
					throw e;
				}
			}

		};
		harvester.run(srcOpt);
	}

	private abstract class CSWHarvester {

		void run(String inputDir) throws Exception {
			// basic Transaction request without any filter constraints;
			int count = 0; // total files written
			int countFailed = 0;

			File folder = new File(inputDir);
			File[] listOfFiles = folder.listFiles();

			System.out.println("Reading XML files from: " + inputDir);
			for (int i = 0; i < listOfFiles.length; i++) {
				final String fileName = listOfFiles[i].getName();
				System.out.println(fileName);
				XMLAdapter xml = null;
				try {
					xml = new XMLAdapter(listOfFiles[i]);
					if (createFI != null && createFI.equals(CREATEFI.REPLACE)) {
						xml = setNewFileIdentifier(fileName, xml);
					}
					else if (createFI != null && createFI.equals(CREATEFI.MISSING)) {
						if (xml.getElement(xml.getRootElement(), new XPath(xPathFileId, NAMESPACE_CONTEXT)) == null) {
							xml = setNewFileIdentifier(fileName, xml);
						}
					}
					boolean inserted = insertRecord(xml.getRootElement());
					if (inserted)
						count++;
				}
				catch (Exception e) {
					countFailed++;
					if (xml != null) {
						final String fi = xml.getNodeAsString(xml.getRootElement(),
								new XPath(xPathFileId, NAMESPACE_CONTEXT), null);

						System.out
							.println("ignore record with fileIdentifier " + fi + ", insert failed: " + e.getMessage());
					}
					else {
						System.out.println("insert failed: " + e.getMessage());
					}
					if (verbose)
						e.printStackTrace();
				}
			}

			System.out.println("\n" + "Done. " + count + " records harvested.");
		}

		private XMLAdapter setNewFileIdentifier(String fileName, XMLAdapter xml) throws XMLStreamException {
			String newFi;
			if (methodFI.equals(METHODFI.UUID)) {
				newFi = UUID.randomUUID().toString();
			}
			else {
				newFi = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
			}
			System.out.println("New fileIdentifier: " + newFi);
			OMElement fiElem = xml.getElement(xml.getRootElement(), new XPath(xPathFileId, NAMESPACE_CONTEXT));
			if (fiElem == null) {
				OMElement mdMeta = xml.getElement(xml.getRootElement(),
						new XPath("//gmd:MD_Metadata", NAMESPACE_CONTEXT));
				OMFactory omFactory = mdMeta.getOMFactory();
				OMElement csElem = omFactory
					.createOMElement(new QName(ISOAP10GCONS, "CharacterString", ISOAP10GCO_PREFIX));
				csElem.addChild(omFactory.createOMText(newFi));
				OMElement newFiElem = omFactory
					.createOMElement(new QName(ISOAP10GMDNS, "fileIdentifier", ISOAP10GMD_PREFIX));
				newFiElem.addChild(csElem);
				mdMeta.getFirstElement().insertSiblingBefore(newFiElem);
			}
			else {
				fiElem.setText(newFi);
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			xml.getRootElement().serialize(bos);
			return new XMLAdapter(new ByteArrayInputStream(bos.toByteArray()));
		}

		abstract boolean insertRecord(OMElement record) throws Exception;

	}

	public static void main(String[] args) throws Exception {

		if (args.length == 0 || (args.length > 0 && (args[0].contains("help") || args[0].contains("?")))) {
			printHelp(initOptions());
		}
		boolean verbose = true;
		try {
			CommandLine cmdline = new PosixParser().parse(initOptions(), args);
			verbose = cmdline.hasOption(CommandUtils.OPT_VERBOSE);
			try {
				String cswOpt = cmdline.getOptionValue(OPT_CSW_ADDRESS);
				String srcOpt = cmdline.getOptionValue(OPT_SOURCE);
				String workspaceOpt = cmdline.getOptionValue(OPT_WORKSPACE);
				String methodOpt = cmdline.getOptionValue(OPT_FI_METHOD);
				String createOpt = cmdline.getOptionValue(OPT_SET_FI);

				METHODFI m = null;
				if (methodOpt != null) {
					m = METHODFI.valueOf(methodOpt.toUpperCase());
				}

				CREATEFI c = null;
				if (createOpt != null) {
					c = CREATEFI.valueOf(createOpt.toUpperCase());
				}

				if (c != null && !c.equals(CREATEFI.RETAIN) && m == null) {
					System.err.println("Option " + OPT_SET_FI + " with value" + c.name()
							+ " requires additional option " + OPT_FI_METHOD);
					System.exit(1);
				}
				Harvester csw = new Harvester(verbose, m, c);
				if (cswOpt == null && workspaceOpt == null) {
					System.err.println("The harvesting target is not passed. Either " + OPT_CSW_ADDRESS + " or "
							+ OPT_WORKSPACE + " must be passed!");
					System.exit(1);
				}
				if (cswOpt != null) {
					csw.run(srcOpt, cswOpt);
				}
				else {
					String[] split = workspaceOpt.split(":");
					if (split.length < 2) {
						System.err.println(
								"The workspace/store is not correct. Must be seperated by a ':', e.g. workspace:store");
						System.exit(1);
					}
					csw.run(srcOpt, split[0], split[1]);
				}
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				if (verbose)
					e.printStackTrace();
			}
		}
		catch (ParseException exp) {
			System.err.println("Could nor parse command line:" + exp.getMessage());
			if (verbose)
				exp.printStackTrace();
		}
	}

	private static Options initOptions() {
		Options opts = new Options();

		Option opt = new Option("s", OPT_SOURCE, true, "the directory containing the metadada records");
		opt.setRequired(true);
		opts.addOption(opt);

		opt = new Option("c", OPT_CSW_ADDRESS, true,
				"the GetCapabialites URL of the target CSW, either this parameter or " + OPT_WORKSPACE
						+ " must be passed");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("w", OPT_WORKSPACE, true,
				"the workspace/store containing the target metadatastore, workspace and store must be seperated by a ':', e.g: workspace:store. Either this parameter or the "
						+ OPT_CSW_ADDRESS + " must be passed.");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("m", OPT_FI_METHOD, true,
				"how to create/replace a new fileIdentifer, required if " + OPT_SET_FI + " is one of "
						+ CREATEFI.MISSING + " or " + CREATEFI.REPLACE + ". Must be one of \n   " + METHODFI.UUID
						+ ": create a new UUID\n   " + METHODFI.FILENAME + ": use name of the record file");
		opt.setRequired(false);
		opts.addOption(opt);

		opt = new Option("f", OPT_SET_FI, true,
				"handling with fileIdentifier, possible values are: \n   " + CREATEFI.RETAIN
						+ ": don't adjust the fileIdentifer (default, if this argument is missing)\n   "
						+ CREATEFI.REPLACE + ": create a new fileIdentifier for all records (replace existing).\n   "
						+ CREATEFI.MISSING + ": create a new fileIdentifer, when fileIdentifer is missing.\n   "
						+ "Use this option with option '" + OPT_FI_METHOD + "'");
		opt.setRequired(false);
		opts.addOption(opt);

		CommandUtils.addDefaultOptions(opts);
		return opts;
	}

	private static void printHelp(Options options) {
		String help = "Harvests metadata records from directory and insert them in the CSW.";
		CommandUtils.printHelp(options, Harvester.class.getSimpleName(), help, null);
	}

}
