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

package org.openpnp.gui;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.model.Part;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class PartSettingsPanel extends JPanel {
    private final Part part;
    
    private JPanel pickConditionsPanel;
    private JLabel lblPickRetryCount;
    private JTextField pickRetryCount;
    private JLabel lblPlaceRetryCount;
    private JTextField placeRetryCount;
    
    private JPanel partTapeSettingsPanel;
    private JLabel lblRotationInTape;
    private JTextField rotationInTape;
    private JLabel lblPitchInTape;
    private JTextField pitchInTape;
    
    public PartSettingsPanel(Part part) {
        this.part = part;
        createUi();
        initDataBindings();
    }
    
    private void createUi() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
		pickConditionsPanel = new JPanel();
		pickConditionsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				"Pick Conditions", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.add(pickConditionsPanel);
		 pickConditionsPanel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC}));
        
        lblPickRetryCount = new JLabel("Pick Retry Count");
        pickConditionsPanel.add(lblPickRetryCount, "2, 2, right, default");
        
        pickRetryCount = new JTextField();
        pickConditionsPanel.add(pickRetryCount, "4, 2, left, default");
        pickRetryCount.setColumns(10);
        
        lblPlaceRetryCount = new JLabel("Place Retry Count");
        pickConditionsPanel.add(lblPlaceRetryCount, "2, 4, right, default");
        
        placeRetryCount = new JTextField();
        pickConditionsPanel.add(placeRetryCount, "4, 4, left, default");
        placeRetryCount.setColumns(10);
        
        // Part's tape settings
        partTapeSettingsPanel = new JPanel();
        partTapeSettingsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				"Tape settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.add(partTapeSettingsPanel);
		partTapeSettingsPanel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC}));
        

		lblRotationInTape = new JLabel("Rotation in tape [deg]");
		partTapeSettingsPanel.add(lblRotationInTape, "2, 2, right, default");
        
        rotationInTape = new JTextField();
        partTapeSettingsPanel.add(rotationInTape, "4, 2, left, default");
        rotationInTape.setColumns(10);
        
        lblPitchInTape = new JLabel("Pitch in tape [mm]");
        partTapeSettingsPanel.add(lblPitchInTape, "2, 4, right, default");
        
        pitchInTape = new JTextField();
        partTapeSettingsPanel.add(pitchInTape, "4, 4, left, default");
        pitchInTape.setColumns(10);
        
    }
    
    protected void initDataBindings() {
        BeanProperty<Part, Integer> partBeanProperty = BeanProperty.create("pickRetryCount");
        BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
        AutoBinding<Part, Integer, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, part, partBeanProperty, pickRetryCount, jTextFieldBeanProperty);
        autoBinding.bind();
        ComponentDecorators.decorateWithAutoSelect(pickRetryCount);

        BeanProperty<Part, Integer> partPlaceBeanProperty = BeanProperty.create("placeRetryCount");
        BeanProperty<JTextField, String> jTextFieldBeanProperty2 = BeanProperty.create("text");
        AutoBinding<Part, Integer, JTextField, String> autoBinding2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, part, partPlaceBeanProperty, placeRetryCount, jTextFieldBeanProperty2);
        autoBinding2.bind();
        ComponentDecorators.decorateWithAutoSelect(placeRetryCount);  

        
        BeanProperty<Part, Integer> rotationPlaceBeanProperty = BeanProperty.create("rotationInTape");
        BeanProperty<JTextField, String> jTextFieldBeanProperty3 = BeanProperty.create("text");
        AutoBinding<Part, Integer, JTextField, String> autoBinding3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, part, rotationPlaceBeanProperty, rotationInTape, jTextFieldBeanProperty3);
        autoBinding3.bind();
        ComponentDecorators.decorateWithAutoSelect(rotationInTape);  

        
        BeanProperty<Part, Integer> pitchPlaceBeanProperty = BeanProperty.create("pitchInTape");
        BeanProperty<JTextField, String> jTextFieldBeanProperty4 = BeanProperty.create("text");
        AutoBinding<Part, Integer, JTextField, String> autoBinding4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, part, pitchPlaceBeanProperty, pitchInTape, jTextFieldBeanProperty4);
        autoBinding4.bind();
        ComponentDecorators.decorateWithAutoSelect(pitchInTape);  
    }
}
