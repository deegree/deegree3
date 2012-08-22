//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/model/table/TableTest.java $
/*
 * Created on 11.02.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.deegree.model.table;

import junit.framework.TestCase;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;

import alltests.AllTests;

/**
 * @author sncho
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TableTest extends TestCase {
	private static ILogger LOG = LoggerFactory.getLogger( TableTest.class );
	protected String tableName;
	protected String[] columnNames;
	protected int[] columnTypes;
	protected Object[][] datas;
	protected DefaultTable table = null;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		tableName = "myTable";
		
		columnNames = new String[3];				
		columnNames[0] = "ID";
		columnNames[1] = "columnA";
		columnNames[2] = "columnB";
		
		columnTypes = new int[3];
		columnTypes[0] = 1;
		columnTypes[1] = 2;
		columnTypes[2] = 3;
		
		datas = new Object[2][3];
		Geometry point = GeometryFactory.createPoint(1,2,null);
		datas[0][0] = new Double(1);
		datas[0][1] = "StringData1";
		datas[0][2] = point;
		
		Geometry point2 = GeometryFactory.createPoint(3,4,null);
		datas[1][0] = new Double(2);
		datas[1][1] = "StringData2";
		datas[1][2] = point2;
		
		try {
			table = new DefaultTable(tableName,
										columnNames,
										columnTypes,
										datas);
		}catch (Exception e) {
			LOG.logError("\tUnit failed failed \n" +
									e.getMessage(), e);
			fail(e.getMessage());	
		}
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Constructor for TableTest.
	 * @param arg0
	 */
	public TableTest(String arg0) {
		super(arg0);
	}
	
	public void testTable() {
		
		LOG.logInfo("--- \t testTable \t ---");		

		
		String name = table.getTableName();
		LOG.logInfo("\t defTab.getTableName: "+name);
		LOG.logInfo("\t Expected: "+tableName);
		assertEquals("Names expected to be equal ", name,tableName);
		

		LOG.logInfo("\t ColumnNames \n");
		String[] cNames = table.getColumnNames();
		LOG.logInfo("\t cNames.length: "+cNames.length);
		LOG.logInfo("\t Expected: "+columnNames.length);
		assertTrue("columnNames lengths expected to be equal ",
					cNames.length==columnNames.length);
		
		for (int i = 0; i < columnNames.length; i++) {
		
			LOG.logInfo("\t cNames["+i+"]: "+cNames[i]);
			LOG.logInfo("\t Expected: "+columnNames[i] +"\n");
			//FIXME is ignorecase ok? ===> check ADDColumn!!!
			assertEquals("Names expected to be equal ",
						 cNames[i],columnNames[i].toUpperCase());
		}
		
		LOG.logInfo("\t ColumnTypes \n");
		int[] cTypes = table.getColumnTypes();
		LOG.logInfo("\t cNames.length: "+cTypes.length);
		LOG.logInfo("\t Expected: "+columnTypes.length);
		assertTrue("columnNames lengths expected to be equal ",
				cTypes.length==columnTypes.length);
		
		for (int i = 0; i < columnTypes.length; i++) {
		
			LOG.logInfo("\t cNames["+i+"]: "+cTypes[i]);
			LOG.logInfo("\t Expected: "+columnTypes[i]+"\n");
			//FIXME is ignorecase ok? ignorecase
			assertEquals("Names expected to be equal ",
						 cTypes[i],columnTypes[i]);
		}
				
		int size = table.getRowCount();
		LOG.logInfo("\t #of rows: "+size);
		LOG.logInfo("\t Expected: "+datas.length);
		assertTrue("Expected data length to be equal ",
					size==datas.length);
		
		for (int i = 0; i < datas.length; i++) {
			
			LOG.logInfo("\t ***** rows["+i+"] \t *****");
			Object[] row = table.getRow(i);
			assertTrue("Expected row["+i+"]"+"and  datas["+i+"]"+
					   "lengths to be equal ",
						row.length==datas[i].length);
			
			for (int j = 0; j < datas[i].length; j++) {
				
				LOG.logInfo("\t col["+j+"]");
				Object o1 = row[j];
				Object o2 = datas[i][j];
				
				Class c1 = row[j].getClass();
				Class c2 = datas[i][j].getClass();
				
				LOG.logInfo("\t class: "+c1);
				LOG.logInfo("\t Expected: "+c2);
				assertEquals("Expected data length to be equal ",
						c1,c2);		
				LOG.logInfo("\t value: "+o1);
				LOG.logInfo("\t Expected: "+o2);
				assertEquals("Object values expected to be equal ",o1,o2);

			}
			LOG.logInfo("\t ***************\n");			
		}
		
		LOG.logInfo("-----------------------------");
	}
	
	public void testAppendRow() {
		LOG.logInfo("--- \t testAppendRow \t ---");
		Object[] newData = new Object[3]; 
		newData[0] = new Double(2);
		newData[1] = "newDataString";	
		//TODO type checking ===> Integer isn't of type 3
		newData[2] = new Integer(-5);
		
		try {
			table.appendRow(newData);
		}catch (Exception e) {
			LOG.logError("\tUnit failed failed \n" +
								  e.getMessage(), e);
			fail(e.getMessage());
		}
		LOG.logInfo(table.toString());
		LOG.logInfo("-----------------------------");
	}
	public void testremoveRow() {

		LOG.logInfo("--- \t removeRow \t ---");
		int i = table.getRowCount()-1;
		Object[] row = table.getRow(i);
		Object[] removed = table.removeRow(i);
		
		assertTrue("row length expected to be equal ", 
					row.length==removed.length);
		
		for (int j=0; j<removed.length;j++) {
			
			LOG.logInfo("\t removed["+j+"]="+removed[j]);	
			LOG.logInfo("\t expeted["+j+"]="+row[j]);
			assertEquals("object values length expected to be equal ", 
						  row[j],removed[j]);
			
		}		
		LOG.logInfo("-----------------------------");
	}
	public void testAddColumn() {
		
		LOG.logInfo("--- \t addColumn \t ---");
		int type = 4;
		String columnName = "columnC";
		table.addColumn(columnName,type);
		
		String nameAdded = table.getColumnName(table.getColumnCount()-1);
		int typeAdded = table.getColumnType(table.getColumnCount()-1);	
		
		LOG.logInfo("\t nameAdded: "+nameAdded);	
		LOG.logInfo("\t expeted: "+columnName);		
		//FIXME is ignoreCase ok? added not brought to Upper Case Column
		/*
		assertEquals("Expected row names  to be equal ", 
					  nameAdded,columnName.toUpperCase());
		*/
		assertEquals("Expected row names  to be equal ", 
				  nameAdded,columnName);
		
		LOG.logInfo("\t nameAdded: "+typeAdded);	
		LOG.logInfo("\t expeted: "+type);		
		assertEquals("Expected row names  to be equal ", 
					  typeAdded,type);
		
		LOG.logInfo("-----------------------------");
	}


	public void printtable() {
		
	}
}
