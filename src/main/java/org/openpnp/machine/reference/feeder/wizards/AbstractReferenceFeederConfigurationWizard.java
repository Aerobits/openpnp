/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.reference.feeder.wizards;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.components.LocationButtonsPanel;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.FeederPartSelectionPanel;
import org.openpnp.gui.support.Helpers;
import org.openpnp.gui.support.Icons;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.gui.support.MutableLocationProxy;
import org.openpnp.machine.reference.ReferenceFeeder;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.model.Part;
import org.openpnp.spi.Actuator;
import org.openpnp.util.UiUtils;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
/**
 * TODO: This should become it's own property sheet which the feeders can include.
 */
public abstract class AbstractReferenceFeederConfigurationWizard
        extends AbstractConfigurationWizard {
    private final ReferenceFeeder feeder;
    private final boolean includePickLocation;

    private FeederPartSelectionPanel partPanel;

    private JPanel generalPanel;
    private JLabel lblFeederRotation;
    private JTextField textFieldLocationRotation;
    private JTextField feedRetryCount;
    private JLabel lblPickRetryCount;
    private JTextField pickRetryCount;
    private JLabel lblFinalPickRotation;
    private JTextField finalPickRotation;
    private JButton btnRefreshFinalPickRotation;
    
    private JPanel panelLocation;
    private JLabel lblX_1;
    private JLabel lblY_1;
    private JLabel lblZ;
    private JTextField textFieldLocationX;
    private JTextField textFieldLocationY;
    private JTextField textFieldLocationZ;
    private LocationButtonsPanel locationButtonsPanel;


    /**
     * @wbp.parser.constructor
     */
    public AbstractReferenceFeederConfigurationWizard(ReferenceFeeder feeder) {
        this(feeder, true);
    }

    public AbstractReferenceFeederConfigurationWizard(ReferenceFeeder feeder,
            boolean includePickLocation) {
        this.feeder = feeder;
        this.includePickLocation = includePickLocation;
        
        partPanel = new FeederPartSelectionPanel(feeder);
        contentPanel.add(partPanel);
        
        generalPanel = new JPanel();
        generalPanel.setBorder(
                new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "General Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        contentPanel.add(generalPanel);
        generalPanel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC },
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,}));
        

      lblFeederRotation = new JLabel("Feeder rotation");
      generalPanel.add(lblFeederRotation, "2, 2, left, default");

      textFieldLocationRotation = new JTextField();
      generalPanel.add(textFieldLocationRotation, "4, 2, fill, default");
      textFieldLocationRotation.setColumns(4);
        
        JLabel lblRetryCount = new JLabel("Feed Retry Count");
        generalPanel.add(lblRetryCount, "2, 4, right, default");
        
        feedRetryCount = new JTextField();
        feedRetryCount.setText("3");
        generalPanel.add(feedRetryCount, "4, 4");
        feedRetryCount.setColumns(3);
        
        lblPickRetryCount = new JLabel("Pick Retry Count");
        generalPanel.add(lblPickRetryCount, "2, 6, right, default");
        
        pickRetryCount = new JTextField();
        pickRetryCount.setText("3");
        pickRetryCount.setColumns(3);
        generalPanel.add(pickRetryCount, "4, 6");
        
        lblFinalPickRotation = new JLabel("Final pick rotation");
        generalPanel.add(lblFinalPickRotation, "2, 8");
        
        finalPickRotation = new JTextField();
        finalPickRotation.setText("---");
        finalPickRotation.setColumns(3);
        finalPickRotation.setToolTipText("<-- Part rotation in tape + feeder rotation (read only)");
        finalPickRotation.setEditable(false);
        generalPanel.add(finalPickRotation, "4, 8");

        btnRefreshFinalPickRotation = new JButton(refreshFinalPickRotationAction);
        btnRefreshFinalPickRotation.setHideActionText(true);
        generalPanel.add(btnRefreshFinalPickRotation, "6, 8");
        
        if (includePickLocation) {
            panelLocation = new JPanel();
            panelLocation.setBorder(new TitledBorder(
                    new EtchedBorder(EtchedBorder.LOWERED, null, null), "Pick Location",
                    TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
            contentPanel.add(panelLocation);
            panelLocation
                    .setLayout(new FormLayout(
                            new ColumnSpec[] {
                            		FormSpecs.RELATED_GAP_COLSPEC,
                                    ColumnSpec.decode("default"),
                                    FormSpecs.RELATED_GAP_COLSPEC, 
                                    ColumnSpec.decode("default"),
                                    FormSpecs.RELATED_GAP_COLSPEC,
                                    ColumnSpec.decode("default"),
                                    FormSpecs.RELATED_GAP_COLSPEC,
                                    ColumnSpec.decode("left:default:grow"),},
                            new RowSpec[] {
                            		FormSpecs.RELATED_GAP_ROWSPEC, 
                            		FormSpecs.DEFAULT_ROWSPEC,
                                    FormSpecs.RELATED_GAP_ROWSPEC, 
                                    FormSpecs.DEFAULT_ROWSPEC,}));

            lblX_1 = new JLabel("X");
            panelLocation.add(lblX_1, "2, 2");

            lblY_1 = new JLabel("Y");
            panelLocation.add(lblY_1, "4, 2");

            lblZ = new JLabel("Z");
            panelLocation.add(lblZ, "6, 2");

            textFieldLocationX = new JTextField();
            panelLocation.add(textFieldLocationX, "2, 4");
            textFieldLocationX.setColumns(8);

            textFieldLocationY = new JTextField();
            panelLocation.add(textFieldLocationY, "4, 4");
            textFieldLocationY.setColumns(8);

            textFieldLocationZ = new JTextField();
            panelLocation.add(textFieldLocationZ, "6, 4");
            textFieldLocationZ.setColumns(8);

            locationButtonsPanel = new LocationButtonsPanel(textFieldLocationX, textFieldLocationY,
                    textFieldLocationZ, null);
            panelLocation.add(locationButtonsPanel, "8, 4");
        }
    }
    
	private Action refreshFinalPickRotationAction = new AbstractAction("Refresh final pick rotation", Icons.refresh) {
		{
			putValue(Action.SHORT_DESCRIPTION, "Refresh final pick rotation");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Part feederPart = feeder.getPart();
			if (feederPart == null) {
				MessageBoxes.errorBox(MainFrame.get(), 
						"Can't calculate final pick rotation",
						"Feeder part is not assigned");
			} 
			else {
				double finalRotation = feeder.getLocation().getRotation() + feederPart.getRotationInTape();
				finalPickRotation.setText(String.valueOf(finalRotation));
			}
		}
	};

    @Override
    public void createBindings() {
        LengthConverter lengthConverter = new LengthConverter();
        IntegerConverter intConverter = new IntegerConverter();
        DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());

        addWrappedBinding(feeder, "part", partPanel.comboBoxPart, "selectedItem");
        addWrappedBinding(feeder, "feedRetryCount", feedRetryCount, "text", intConverter);
        addWrappedBinding(feeder, "pickRetryCount", pickRetryCount, "text", intConverter);
        ComponentDecorators.decorateWithAutoSelect(feedRetryCount);
        ComponentDecorators.decorateWithAutoSelect(pickRetryCount);

        MutableLocationProxy location = new MutableLocationProxy();
        bind(UpdateStrategy.READ_WRITE, feeder, "location", location, "location");
        if (includePickLocation) {
            addWrappedBinding(location, "lengthX", textFieldLocationX, "text", lengthConverter);
            addWrappedBinding(location, "lengthY", textFieldLocationY, "text", lengthConverter);
            addWrappedBinding(location, "lengthZ", textFieldLocationZ, "text", lengthConverter);
            ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldLocationX);
            ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldLocationY);
            ComponentDecorators.decorateWithAutoSelectAndLengthConversion(textFieldLocationZ);
        }
        addWrappedBinding(location, "rotation", textFieldLocationRotation, "text", doubleConverter);
        ComponentDecorators.decorateWithAutoSelect(textFieldLocationRotation);
    }
}
