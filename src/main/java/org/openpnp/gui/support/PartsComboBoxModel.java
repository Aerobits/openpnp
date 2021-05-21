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

package org.openpnp.gui.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import org.openpnp.gui.MainFrame;
import org.openpnp.model.BoardLocation;
import org.openpnp.model.Configuration;
import org.openpnp.model.Part;
import org.openpnp.model.Placement;
import org.openpnp.spi.Feeder;
import org.pmw.tinylog.Logger;

@SuppressWarnings("serial")
public class PartsComboBoxModel extends DefaultComboBoxModel implements PropertyChangeListener {
    private IdentifiableComparator<Part> comparator = new IdentifiableComparator<>();
    
    public static enum FILTER_TYPE {
    	FILTER_NONE,
    	FILTER_JOB,
    	FILTER_UNUSED
    }

    public PartsComboBoxModel() {
        addAllElements();
        Configuration.get().addPropertyChangeListener("parts", this);
    }

    private void addAllElements() {
        ArrayList<Part> parts = new ArrayList<>(Configuration.get().getParts());
        Collections.sort(parts, comparator);
        for (Part part : parts) {
            addElement(part);
        }
    }
    
	private void addJobElements() {
		List<BoardLocation> jobBoardLocations = MainFrame.get().getJobTab().getJob().getBoardLocations();
		if (jobBoardLocations.size() == 0)
			return;

		List<Placement> jobPlacements = jobBoardLocations.get(0).getBoard().getPlacements();
		if (jobPlacements.size() == 0)
			return;

		ArrayList<Part> parts = new ArrayList<>();
		for (Placement p : jobPlacements) {
			Part part = p.getPart();
			if (part != null) {
				parts.add(part);
			}
		}

		Collections.sort(parts, comparator);
		for (Part part : parts) {
			addElement(part);
		}
	}

	private void addUnusedElements() {
		ArrayList<Part> parts = new ArrayList<>(Configuration.get().getParts());
		List<Feeder> feeders = Configuration.get().getMachine().getFeeders();

		for (Feeder f : feeders) {
			parts.remove(f.getPart());
		}

		Collections.sort(parts, comparator);
		for (Part part : parts) {
			addElement(part);
		}
	}

	public void filterElements(FILTER_TYPE type) {
    	removeAllElements();
    	
		switch (type) {
			case FILTER_NONE: {
	    		addAllElements();
				break;
			}
			case FILTER_JOB: {
	    		addJobElements();
				break;
			}
			case FILTER_UNUSED: {
				addUnusedElements();
				break;
			}
		}
	}
	
	public void addPart(Part part) {
		if (getIndexOf(part) == -1) {
			addElement(part);
		}
	}

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        removeAllElements();
        addAllElements();
    }
}
