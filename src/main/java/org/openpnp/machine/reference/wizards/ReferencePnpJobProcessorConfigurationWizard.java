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

package org.openpnp.machine.reference.wizards;

import java.awt.List;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.openpnp.Translations;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.machine.reference.ReferencePnpJobProcessor;
import org.openpnp.machine.reference.ReferencePnpJobProcessor.JobBoardOrderHint;
import org.openpnp.machine.reference.ReferencePnpJobProcessor.JobPartOrderHint;
import org.openpnp.machine.reference.ReferencePnpJobProcessor.JobPlannerHint;
import org.openpnp.machine.reference.ReferencePnpJobProcessor.Neoden4PnpJobPlanner;
import org.openpnp.machine.reference.ReferencePnpJobProcessor.SimplePnpJobPlanner;
import org.openpnp.machine.reference.ReferencePnpJobProcessor.StraightforwardPnpJobPlanner;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class ReferencePnpJobProcessorConfigurationWizard extends AbstractConfigurationWizard {
    private final ReferencePnpJobProcessor jobProcessor;
    private JComboBox comboBoxJobPlanners;
    private JComboBox comboBoxJobBoardOrder;
    private JComboBox comboBoxJobPartOrder;
    private JTextField maxVisionRetriesTextField;

    public ReferencePnpJobProcessorConfigurationWizard(ReferencePnpJobProcessor jobProcessor) {
        this.jobProcessor = jobProcessor;
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel panelGeneral = new JPanel();
        panelGeneral.setBorder(new TitledBorder(null, "General", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));
        contentPanel.add(panelGeneral);
        panelGeneral.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,}));
        
        JLabel lblJobPlanner = new JLabel("Job planner");
        panelGeneral.add(lblJobPlanner, "2, 2, right, default");

        comboBoxJobPlanners = new JComboBox(JobPlannerHint.values());
        panelGeneral.add(comboBoxJobPlanners, "4, 2, right, default");

        JLabel lblJobBoardOrder = new JLabel("Board order");
        panelGeneral.add(lblJobBoardOrder, "2, 4, right, default");

        comboBoxJobBoardOrder = new JComboBox(JobBoardOrderHint.values());
        panelGeneral.add(comboBoxJobBoardOrder, "4, 4");

        JLabel lblJobPartOrder = new JLabel("Part order");
        panelGeneral.add(lblJobPartOrder, "2, 6, right, default");

        comboBoxJobPartOrder = new JComboBox(JobPartOrderHint.values());
        panelGeneral.add(comboBoxJobPartOrder, "4, 6");

        JLabel lblMaxVisionRetries = new JLabel(Translations.getString("MachineSetup.JobProcessors.ReferencePnpJobProcessor.Label.MaxVisionRetries"));
        panelGeneral.add(lblMaxVisionRetries, "2, 8, right, default");

        maxVisionRetriesTextField = new JTextField();
        panelGeneral.add(maxVisionRetriesTextField, "4, 8");
        maxVisionRetriesTextField.setColumns(10);
    }

    @Override
    public void createBindings() {
        IntegerConverter intConverter = new IntegerConverter();

        addWrappedBinding(jobProcessor, "jobPlanner", comboBoxJobPlanners, "selectedItem");
        addWrappedBinding(jobProcessor, "jobBoardOrder", comboBoxJobBoardOrder, "selectedItem");
        addWrappedBinding(jobProcessor, "jobPartOrder", comboBoxJobPartOrder, "selectedItem");
        addWrappedBinding(jobProcessor, "maxVisionRetries", maxVisionRetriesTextField, "text", intConverter);

        ComponentDecorators.decorateWithAutoSelect(maxVisionRetriesTextField);
    }
}
