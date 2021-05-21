package org.openpnp.gui.support;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.logging.Log;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.support.PartsComboBoxModel.FILTER_TYPE;
import org.openpnp.model.Board;
import org.openpnp.model.Configuration;
import org.openpnp.model.Part;
import org.openpnp.model.Placement;
import org.openpnp.spi.Feeder;
import org.pmw.tinylog.Logger;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class FeederPartSelectionPanel extends JPanel{
	
	public JComboBox comboBoxPart;
	
	private PartsComboBoxModel partsComboBoxModel;
	private JLabel lblPartInfo;
	private Feeder feeder;

	public FeederPartSelectionPanel(Feeder feeder) {
		this.feeder = feeder;
		
		setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), 
				"Part", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

        setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC}));
	
        JLabel lblPart = new JLabel("Part");
		add(lblPart, "2, 2, right, default");

        partsComboBoxModel = new PartsComboBoxModel();
    	comboBoxPart = new JComboBox();
		comboBoxPart.setModel(partsComboBoxModel);
		comboBoxPart.setRenderer(new IdentifiableListCellRenderer<Part>());
		comboBoxPart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePartInfo(e);
			}
		});
		add(comboBoxPart, "4, 2, left, default");

		JLabel lblShowFilter = new JLabel("Show filter: ");
		add(lblShowFilter, "6, 2, right, default");

		JRadioButton rbShowPartAll = new JRadioButton("all");
		JRadioButton rbShowPartJob = new JRadioButton("job's");
		JRadioButton rbShowPartUnused = new JRadioButton("unused");

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rbShowPartAll);
		buttonGroup.add(rbShowPartJob);
		buttonGroup.add(rbShowPartUnused);
		
		add(rbShowPartAll, "8, 2, center, default");
		add(rbShowPartJob, "10, 2, center, default");
		add(rbShowPartUnused, "12, 2, center, default");

		JButton btnShowPart = new JButton(showPartAction);
		add(btnShowPart, "14, 2, left, default");

		rbShowPartAll.addActionListener(radioButtonsAction);
		rbShowPartJob.addActionListener(radioButtonsAction);
		rbShowPartUnused.addActionListener(radioButtonsAction);

		
		lblPartInfo = new JLabel("");
		add(lblPartInfo, "4, 4, left, default");
		
		
		// Set showing unused parts as default
		rbShowPartUnused.setSelected(true);
		partsComboBoxModel.filterElements(FILTER_TYPE.FILTER_UNUSED);
		partsComboBoxModel.addPart(feeder.getPart());
		comboBoxPart.setSelectedItem(feeder.getPart());
	}
	
	private ActionListener radioButtonsAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			switch (event.getActionCommand()) {
				case "all": {
					partsComboBoxModel.filterElements(FILTER_TYPE.FILTER_NONE);
					break;
				}
				case "job's": {
					partsComboBoxModel.filterElements(FILTER_TYPE.FILTER_JOB);
					break;
				}
				case "unused": {
					partsComboBoxModel.filterElements(FILTER_TYPE.FILTER_UNUSED);
					break;
				}
			}
			
			partsComboBoxModel.setSelectedItem(feeder.getPart());
		}
	};
	

    public final Action showPartAction = new AbstractAction() {
        {
            putValue(SMALL_ICON, Icons.editFeeder);
            putValue(NAME, "Show feeder's part");
            putValue(SHORT_DESCRIPTION, "Show feeder's associated part definition.");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            MainFrame.get().getPartsTab().showPartForFeeder(feeder);
        }
    };
	
	private void updatePartInfo(ActionEvent e) {
		int count = 0;
		Part feederPart = (Part) comboBoxPart.getSelectedItem();

		for (Board board : Configuration.get().getBoards()) {
			for (Placement p : board.getPlacements()) {
				if (p.getPart() == feederPart) {
					count++;
				}
			}
		}
		if (count > 0) {
			String lbl = Integer.toString(count) + " used by current job";
			lblPartInfo.setText(lbl);
		} 
		else {
			lblPartInfo.setText("");
		}
	}

}
