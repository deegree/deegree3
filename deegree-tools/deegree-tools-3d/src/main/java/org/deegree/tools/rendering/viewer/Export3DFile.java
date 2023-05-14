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

package org.deegree.tools.rendering.viewer;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.deegree.commons.utils.memory.MemoryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Export3DFile</code> shows a dialog to the user in which export parameters can be
 * set, and calls the appropriate export method.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class Export3DFile {

	private static Logger LOG = LoggerFactory.getLogger(Export3DFile.class);

	/**
	 * Convenient map, which holds the mapping of the parameters to the components
	 */
	Map<String, JComponent> paramFields;

	/**
	 * Holds all parameters set by the user
	 */
	Map<String, String> resultMap;

	/**
	 * Holds all parameters which can be given to the exporter.
	 */
	Map<String, String> originalMap;

	/**
	 * Will be true if the parameter dialog was ended with the 'ok' button.
	 */
	boolean okClicked = false;

	// private JDialog parameterDialog = null;

	/**
	 * the parent of all dialogs.
	 */
	private GLViewer parent;

	/**
	 * A list of class which are able to export and their description.
	 */
	Map<String, String> exportClasses = new HashMap<String, String>();

	/**
	 * Will hold a class name of the exporter selected by the user (set in the anonymous
	 * Actionlistener of the the ok-button of the ExportDialog)
	 */
	String selectedExporter = null;

	/**
	 * A button group to handle to activation of only one exporter.
	 */
	ButtonGroup exporterGroup = new ButtonGroup();

	/**
	 * Shows the export dialog (wizard)
	 */
	JDialog exportDialog;

	/**
	 * @param parent of the dialog
	 *
	 */
	public Export3DFile(GLViewer parent) {
		this.resultMap = new HashMap<String, String>();
		this.paramFields = new HashMap<String, JComponent>();
		this.parent = parent;

		findAvailableExports();
		createExportDialog();
	}

	/**
	 *
	 */
	private void findAvailableExports() {
		// exportClasses.put( new J3DToCityGMLExporter().getShortDescription(),
		// J3DToCityGMLExporter.class.getCanonicalName() );
	}

	private void createExportDialog() {
		JPanel yesNoPanel = new JPanel();
		JButton tmpButton = new JButton("yes");
		tmpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedExporter == null) {
					JOptionPane.showMessageDialog(exportDialog, "Please select one of the exporters.");
				}
				else {
					exportDialog.setVisible(false);
				}
			}
		});
		yesNoPanel.add(tmpButton);
		tmpButton = new JButton("cancel");
		tmpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedExporter = null;
				exportDialog.setVisible(false);
			}

		});
		yesNoPanel.add(tmpButton);
		JPanel exportPane = new JPanel(new GridBagLayout());
		GridBagConstraints gb = new GridBagConstraints();
		gb.gridx = 0;
		gb.gridy = 0;

		for (String desc : exportClasses.keySet()) {
			JRadioButton exporter = new JRadioButton(desc);
			exporter.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JRadioButton jb = (JRadioButton) e.getSource();
					if (jb.isSelected()) {
						selectedExporter = exportClasses.get(jb.getText());
						jb.setSelected(false);
					}
				}

			});
			gb.gridy++;
			exportPane.add(exporter, gb);
			exporterGroup.add(exporter);
		}
		exportDialog = new JDialog(parent, true);
		exportDialog.getContentPane().setLayout(new GridBagLayout());
		gb.gridy = 0;
		exportDialog.getContentPane().add(exportPane, gb);
		gb.gridy++;
		exportDialog.getContentPane().add(yesNoPanel, gb);
		exportDialog.pack();
		exportDialog.setVisible(false);
	}

	/**
	 * @param toExport the actual branchgroup
	 * @return the String representation of the exported branch group.
	 */
	public StringBuilder exportBranchgroup(MemoryAware toExport) {
		StringBuilder result = new StringBuilder(20000);
		this.selectedExporter = null;
		exportDialog.setLocationRelativeTo(parent);
		// will set the selectedExporter
		exportDialog.setVisible(true);
		if (selectedExporter != null) {
			J3DExporter tmpExporter = null;
			try {
				Class<?> c = Class.forName(selectedExporter);
				c.asSubclass(J3DExporter.class);
				Constructor<?> cons = c.getConstructor();
				tmpExporter = (J3DExporter) cons.newInstance();
			}
			catch (Exception e) {
				LOG.error(e.getMessage(), e);
				parent.showExceptionDialog("Could not create an exporter instance because:\n" + e.getMessage());
				return result;
			}
			JDialog parameterDialog = createParameterDialog(tmpExporter);
			parameterDialog.setLocationRelativeTo(parent);
			originalMap = tmpExporter.getParameterMap();
			parameterDialog.setVisible(true);
			if (isOkClicked()) {
				try {
					Constructor<?> constructor = tmpExporter.getClass().getConstructor(Map.class);
					tmpExporter = (J3DExporter) constructor.newInstance(resultMap);
					// toExport.detach();
					// tmpExporter.export( result, toExport );
				}
				catch (Exception e) {
					LOG.error(e.getMessage(), e);
					parent.showExceptionDialog("Could not create an exporter instance because:\n" + e.getMessage());
				}
			}
			originalMap = null;
		}
		return result;
	}

	/**
	 * Creates a parameter dialog which by clicking ok sets all the values fromt the
	 * original map to the result map if they were changed by the user.
	 * @param exporter
	 * @return the dialog.
	 */
	private JDialog createParameterDialog(J3DExporter exporter) {
		final JDialog parameterDialog = new JDialog(parent, true);// JOptionPane.QUESTION_MESSAGE,
		JPanel yesNoPanel = new JPanel();
		JButton tmpButton = new JButton("yes");
		tmpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okClicked = true;
				for (String key : originalMap.keySet()) {
					String value = originalMap.get(key);
					if (value != null && !"".equals(value.trim())) {
						JComponent paramField = paramFields.get(key);
						if (paramField != null) {
							String userInput = key;
							if (paramField instanceof JTextField) {
								userInput = ((JTextField) paramField).getText();
							}
							else if (paramField instanceof JCheckBox) {
								userInput = ((JCheckBox) paramField).isSelected() ? "yes" : "no";
							}
							if (!value.equalsIgnoreCase(userInput)) {
								resultMap.put(key, userInput);
							}
						}
					}
				}
				parameterDialog.setVisible(false);
			}
		});
		yesNoPanel.add(tmpButton);
		tmpButton = new JButton("cancel");
		tmpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parameterDialog.setVisible(false);
			}
		});
		yesNoPanel.add(tmpButton);
		Map<String, String> parameterMap = exporter.getParameterMap();
		JPanel parameterPane = new JPanel(new GridBagLayout());
		GridBagConstraints gb = new GridBagConstraints();
		gb.gridx = 0;
		gb.gridy = 0;
		gb.anchor = GridBagConstraints.WEST;
		for (String param : parameterMap.keySet()) {
			JLabel label = new JLabel(param + ": ");
			JComponent parameter = null;
			if (parameterMap.get(param).contains("(yes/no)")) {
				parameter = new JCheckBox();
			}
			else {
				parameter = new JTextField(parameterMap.get(param), 20);
			}
			JPanel totalField = new JPanel(new FlowLayout());
			totalField.add(label);
			totalField.add(parameter);
			gb.gridy++;
			gb.weightx = 1;
			parameterPane.add(totalField, gb);
			paramFields.put(param, parameter);
		}

		// JOptionPane.YES_NO_CANCEL_OPTION,
		// null, buttons, buttons[0] );
		parameterDialog.getContentPane().setLayout(new GridBagLayout());
		gb.gridy = 0;
		parameterDialog.getContentPane().add(parameterPane, gb);
		gb.gridy++;
		parameterDialog.getContentPane().add(yesNoPanel, gb);
		parameterDialog.pack();
		parameterDialog.setVisible(false);
		return parameterDialog;
	}

	/**
	 * @return the resultMap.
	 */
	public Map<String, String> getResultMap() {
		return resultMap;
	}

	/**
	 * @return the okClicked.
	 */
	public boolean isOkClicked() {
		return okClicked;
	}

}
