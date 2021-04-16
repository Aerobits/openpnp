/*
 * Copyright (C) 2011, 2020 Jason von Nieda <jason@vonnieda.org>, Tony Luken <tonyluken@att.net>
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

package org.openpnp.gui.processes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
	
import org.openpnp.gui.JobPanel;
import org.openpnp.gui.MainFrame;
import org.openpnp.gui.support.MessageBoxes;
import org.openpnp.model.BoardLocation;
import org.openpnp.model.Configuration;
import org.openpnp.model.Length;
import org.openpnp.model.LengthUnit;
import org.openpnp.model.Location;
import org.openpnp.model.Placement;
import org.openpnp.model.AbstractModelObject;
import org.openpnp.model.Board;
import org.openpnp.model.Board.Side;
import org.openpnp.model.Placement.Type;
import org.openpnp.spi.Camera;
import org.openpnp.spi.FiducialLocator;
import org.openpnp.util.IdentifiableList;
import org.openpnp.util.MovableUtils;
import org.openpnp.util.TravellingSalesman;
import org.openpnp.util.UiUtils;
import org.openpnp.util.Utils2D;
import org.pmw.tinylog.Logger;
import org.simpleframework.xml.Attribute;

/**
 * Guides the user through the multi-fiducial board location operation using step by step instructions.
 * 
 */
public class MultiFiducialBoardLocationProcess {
    private final MainFrame mainFrame;
    private final JobPanel jobPanel;
    private final Camera camera;

    private int step = 0;
    
    private Placement currentPlacement;
    private String placementId;
    private List<Placement> placements;
    private List<Location> expectedLocations;
    private List<Location> measuredLocations;
    private int nPlacements;
    private int idxPlacement = 0;
    private BoardLocation boardLocation;
    private Side boardSide;
    private Location savedBoardLocation;
    private AffineTransform savedPlacementTransform;
    private MultiFiducialBoardLocationProperties props;
    
    FiducialLocator fiducialLocator = Configuration.get().getMachine().getFiducialLocator();

    public static class MultiFiducialBoardLocationProperties {
        private double scalingTolerance = 0.05; //unitless
        private double shearingTolerance = 0.05; //unitless
        protected Length boardLocationTolerance = new Length(5.0, LengthUnit.Millimeters);
    }
    
    public MultiFiducialBoardLocationProcess(MainFrame mainFrame, JobPanel jobPanel)
            throws Exception {
        this.mainFrame = mainFrame;
        this.jobPanel = jobPanel;
        this.camera =
                MainFrame.get().getMachineControls().getSelectedTool().getHead().getDefaultCamera();
        
        currentPlacement = new Placement("");
        placementId = "";
        expectedLocations = new ArrayList<Location>();
        measuredLocations = new ArrayList<Location>();
        
        boardLocation = jobPanel.getSelection();
        boardSide = boardLocation.getSide();
        
        //Save the current board location and transform in case it needs to be restored
        savedBoardLocation = boardLocation.getLocation();
        savedPlacementTransform = boardLocation.getPlacementTransform();
        
        // Clear the current transform so it doesn't potentially send us to the wrong spot
        // to find the placements.
        boardLocation.setPlacementTransform(null);
        
        props = (MultiFiducialBoardLocationProperties) Configuration.get().getMachine().
                    getProperty("MultiFiducialBoardLocationProperties");
        
        if (props == null) {
            props = new MultiFiducialBoardLocationProperties();
            Configuration.get().getMachine().
                setProperty("MultiFiducialBoardLocationProperties", props);
        }
        Logger.trace("Board location tolerance = " + props.boardLocationTolerance);
        Logger.trace("Board scaling tolerance = " + props.scalingTolerance);
        Logger.trace("Board shearing tolerance = " + props.shearingTolerance);
        
        advance();
    }

    private void advance() {
        boolean stepResult = true;
        
        if (step == 0) {
            stepResult = step1();
        }
        else if (step == 1) {
            stepResult = step2();
        }
        else if (step == 2) {
            stepResult = step3();
        }

        if (!stepResult) {
            return;
        }
        
        step++;
        
//        if (currentPlacement.getType() == Type.Fiducial) {
//            Logger.error("Fiducial");
//        	  step2();
//        }
        
        if (step == 3) {
            mainFrame.hideInstructions();
        }
        else {
            String title = step == 2 ? "Done!" : "Set correct part location";
            String instructions = step == 2 ? "" : String.format("Move camera to '%s'.", currentPlacement.getId());
            String btnText = step == 2 ? "Finish" : "Next";
            
            mainFrame.showInstructions(
            		title, instructions, true, step == 3 ? false : true, btnText, 
            		cancelActionListener, proceedActionListener
            );
        }
    }
    
    private boolean step1() {
        // Get all fiducials' placements
    	placements = new ArrayList<>();
    	for (Placement placement : boardLocation.getBoard().getPlacements()) {
    		if (placement.getType() == Type.Fiducial || placement.getType() == Type.Fiducial_Manual) {
    			if (placement.getSide() == boardLocation.getSide() && placement.isEnabled()) {
    				placements.add(placement);
    			}
    		}
        }

        nPlacements = placements.size();
        if (nPlacements < 2) {
            MessageBoxes.errorBox(mainFrame, "Error", "Please select at least two placements.");
            return false;
        }
        
        //Optimize the visit order of the placements
        placements = optimizePlacementOrder(placements);

        //Move the camera near the first placement's location
        UiUtils.submitUiMachineTask(() -> {
            Location location = Utils2D.calculateBoardPlacementLocation(boardLocation,
                    placements.get(0).getLocation() );
            MovableUtils.moveToLocationAtSafeZ(camera, location);
            MovableUtils.fireTargetedUserAction(camera);
        });
        
        //Get ready for the first placement
        idxPlacement = 0;
    	currentPlacement = placements.get(idxPlacement);
        placementId = placements.get(0).getId();
        expectedLocations.add(placements.get(0).getLocation()
                .invert(boardSide==Side.Bottom, false, false, false));
        
        return true;
    }

    private boolean step2() {
    	
    	Location measuredLocation = null;

    	// Type.Fiducial 		-> let fiducialLocator find location
    	// Type.Fiducial_Manual -> let user set correct location
    	
    	if (currentPlacement.getType() == Type.Fiducial) {
    		try {
    			measuredLocation = fiducialLocator.getFiducialLocation(boardLocation, currentPlacement);
			} 
    		catch (Exception e) { 
    			e.printStackTrace();
    			MessageBoxes.errorBox(mainFrame, "Error", e.getMessage());
			}
    	}
    	else { // Fiducial_Manual
            measuredLocation = camera.getLocation();
    	}
    	
    	
        //Save the result of the current placement measurement
        if (measuredLocation == null) {
            MessageBoxes.errorBox(mainFrame, "Error", "Please position the camera.");
            return false;
        }
        measuredLocations.add(measuredLocation);
        
        //Move on the the next placement
        idxPlacement++;
        
        if (idxPlacement<nPlacements) {
            //There are more placements to be measured
            
            //Get ready for the next placement
        	currentPlacement = placements.get(idxPlacement);
            placementId = placements.get(idxPlacement).getId();
            expectedLocations.add(placements.get(idxPlacement).getLocation()
                    .invert(boardSide==Side.Bottom, false, false, false));
            
            //Move the camera near the next placement's expected location
            UiUtils.submitUiMachineTask(() -> {
                Location location = Utils2D.calculateBoardPlacementLocation(boardLocation,
                        placements.get(idxPlacement).getLocation() );
                MovableUtils.moveToLocationAtSafeZ(camera, location);
                MovableUtils.fireTargetedUserAction(camera);
            });
            
            //keep repeating step2 until all placements have been measured
            step--;
        } 
        else {
            //All the placements have been visited, so set final board location and placement transform
            setBoardLocationAndPlacementTransform();
            
            //Refresh the job panel so that the new board location is visible
            jobPanel.refreshSelectedRow();           
            
            //Check the results to make sure they are valid
            double boardOffset = boardLocation.getLocation().convertToUnits(LengthUnit.Millimeters).getLinearDistanceTo(savedBoardLocation);
            Logger.info("Board origin offset distance: " + boardOffset + " mm");
           
            Utils2D.AffineInfo ai = Utils2D.affineInfo(boardLocation.getPlacementTransform());
            Logger.info("Placement affine transform: " + ai);
            
            String errString = "";
            if (Math.abs(ai.xScale-1) > props.scalingTolerance) {
                errString += "x scaling = " + String.format("%.5f", ai.xScale) + " which is outside the expected range of [" +
                        String.format("%.5f", 1-props.scalingTolerance) + ", " + String.format("%.5f", 1+props.scalingTolerance) + "], ";
            }
            if (Math.abs(ai.yScale-1) > props.scalingTolerance) {
                errString += "y scaling = " + String.format("%.5f", ai.yScale) + " which is outside the expected range of [" +
                        String.format("%.5f", 1-props.scalingTolerance) + ", " + String.format("%.5f", 1+props.scalingTolerance) + "], ";
            }
            if (Math.abs(ai.xShear) > props.shearingTolerance) {
                errString += "x shearing = " + String.format("%.5f", ai.xShear) + " which is outside the expected range of [" +
                        String.format("%.5f", -props.shearingTolerance) + ", " + String.format("%.5f", props.shearingTolerance) + "], ";
            }
            if (boardOffset > props.boardLocationTolerance.convertToUnits(LengthUnit.Millimeters).getValue()) {
                errString += "the board origin moved " + String.format("%.4f", boardOffset) +
                        "mm which is greater than the allowed amount of " +
                        String.format("%.4f", props.boardLocationTolerance.convertToUnits(LengthUnit.Millimeters).getValue()) + "mm, ";
            }
            if (errString.length() > 0) {
                errString = errString.substring(0, errString.length()-2); //strip off the last comma and space
                MessageBoxes.errorBox(mainFrame, "Error", "Results invalid because " + errString + "; double check to ensure you are " +
                        "jogging the camera to the correct placements.  Other potential remidies include " +
                        "setting the initial board X, Y, Z, and Rotation in the Boards panel; using a different set of placements; " +
                        "or changing the allowable tolerances in the MultiPlacementBoardLocationProperties section of machine.xml.");
                cancel();
                return false;
            }
            
        }
        return true;
    }

    private boolean step3() {
        UiUtils.submitUiMachineTask(() -> {
            Location location = jobPanel.getSelection().getLocation();
            MovableUtils.moveToLocationAtSafeZ(camera, location);
            MovableUtils.fireTargetedUserAction(camera);
        });

        return true;
    }

    private Location setBoardLocationAndPlacementTransform() {
        AffineTransform tx = Utils2D.deriveAffineTransform(expectedLocations, measuredLocations);
        
        //Set the transform
        boardLocation.setPlacementTransform(tx);
        
        // Compute the compensated board location
        Location origin = new Location(LengthUnit.Millimeters);
        if (boardSide == Side.Bottom) {
            origin = origin.add(boardLocation.getBoard().getDimensions().derive(null, 0., 0., 0.));
        }
        Location newBoardLocation = Utils2D.calculateBoardPlacementLocation(boardLocation, origin);
        newBoardLocation = newBoardLocation.convertToUnits(boardLocation.getLocation().getUnits());
        newBoardLocation = newBoardLocation.derive(null, null, boardLocation.getLocation().getZ(), null);

        //Set the board's new location
        boardLocation.setLocation(newBoardLocation);

        //Need to set transform again because setting the location clears the transform - shouldn't the 
        //BoardLocation.setPlacementTransform method perform the above calculations and set the location
        //itself since it already has all the needed information???
        boardLocation.setPlacementTransform(tx);
        
        return newBoardLocation;
    }

    private List<Placement> optimizePlacementOrder(List<Placement> placements) {
        // Use a traveling salesman algorithm to optimize the path to visit the placements
        TravellingSalesman<Placement> tsm = new TravellingSalesman<>(
                placements, 
                new TravellingSalesman.Locator<Placement>() { 
                    @Override
                    public Location getLocation(Placement locatable) {
                        return Utils2D.calculateBoardPlacementLocation(boardLocation, locatable.getLocation());
                    }
                }, 
                // start from current camera location
                camera.getLocation(),
                // and end at the board origin
                boardLocation.getLocation());

        // Solve it using the default heuristics.
        tsm.solve();
        
        return tsm.getTravel();
    }

    private void cancel() {
        //Restore the old settings
        boardLocation.setLocation(savedBoardLocation);
        boardLocation.setPlacementTransform(savedPlacementTransform);
        jobPanel.refreshSelectedRow();
        
        mainFrame.hideInstructions();
    }

    private final ActionListener proceedActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            advance();
        }
    };

    private final ActionListener cancelActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            cancel();
        }
    };
}
