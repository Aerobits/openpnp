"""
Script that tells machine to pick part from feeder, 
place it in net and calculate placement errors.

Finally it calculates better nozzle headOffsets 
based on mean error.

Parts are placed in given order (ex. 3x3 net):

        3  6  9
        2  5  8
        1  4  7
"""

# OpenPnP
from org.openpnp.util.UiUtils import submitUiMachineTask
from org.openpnp.model import LengthUnit, Location
from org.openpnp.util import VisionUtils, MovableUtils
from org.openpnp.model import Board
from org.openpnp.model import BoardLocation

# Java
import java.io.File
import java.lang.Boolean as JavaBoolean
from javax.swing.JOptionPane import showConfirmDialog, YES_NO_OPTION, YES_OPTION, ERROR_MESSAGE, QUESTION_MESSAGE
from javax.swing import JFrame, JPanel, JLabel, JButton, JTextArea, JScrollPane, JSpinner, JCheckBox, JComboBox
from javax.swing import SpringLayout, SpinnerNumberModel
from java.awt.event import ActionListener, WindowEvent, WindowAdapter
from java.awt import Dimension

# Python
import time
import math
import random

text_area_log = JTextArea(18, 80)
btn_start = JButton()
spinner_net_size = JSpinner()
checkbox_placed = JCheckBox()
checkbox_random_pick_offset = JCheckBox()
combo_box_nozzles = JComboBox()
combo_box_parts = JComboBox()
thrunnable = None
frame = None
start = False
selected_nozzle = None
selected_part = None

class JFrameListener(WindowAdapter):
    def windowClosing(self, window_event):
        print("EXIT")
        frame.dispose() # close JFrame
        time.sleep(0.5)
        thrunnable.cancel(JavaBoolean(True))

class ButtonListener(ActionListener):
    def actionPerformed(self, event):
        global start

        if event.getActionCommand() == "Start":
            start = True
            btn_start.setEnabled(JavaBoolean(False))
            
class ComboBoxListener(ActionListener):
    def actionPerformed(self, event):
        global selected_nozzle 
        global selected_part 
        
        if event.getActionCommand() == "combo_box_nozzles":
            selected_nozzle = combo_box_nozzles.getSelectedItem()
            selected_part = None
            
            # Update parts supported by nozzle
            nozzle_tip = machine.defaultHead.getNozzleByName(selected_nozzle).getNozzleTip()
            combo_box_parts.removeAllItems()
            parts = config.getParts()
            for p in parts:
                package = p.getPackage()
                if nozzle_tip in package.getCompatibleNozzleTips():
                    combo_box_parts.addItem(p.getId())
        
        if event.getActionCommand() == "combo_box_parts":
            selected_part = combo_box_parts.getSelectedItem()      

def init_frame():
    global frame
    frame = JFrame("CALIBRATION TOOLBOX")
    frame.setSize(700, 700) 
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE)
    window_listener = JFrameListener()
    frame.addWindowListener(window_listener)

    # Main panel
    layout_center = SpringLayout()
    panel_main = JPanel(layout_center)
    
    # Label net size
    label_net_size_spinner = JLabel("Net size:")
    layout_center.putConstraint(SpringLayout.WEST, label_net_size_spinner, 25, SpringLayout.WEST, panel_main)
    layout_center.putConstraint(SpringLayout.NORTH, label_net_size_spinner, 25, SpringLayout.NORTH, panel_main)
    # Net size spinner
    spinner_net_size.setModel(SpinnerNumberModel(2, 1, 20, 1))
    layout_center.putConstraint(SpringLayout.WEST, spinner_net_size, 50, SpringLayout.EAST, label_net_size_spinner)
    layout_center.putConstraint(SpringLayout.VERTICAL_CENTER, spinner_net_size, 0, SpringLayout.VERTICAL_CENTER, label_net_size_spinner)
    
    # Label nozzle
    label_nozzle = JLabel("Select nozzle:")
    layout_center.putConstraint(SpringLayout.WEST, label_nozzle, 25, SpringLayout.WEST, panel_main)
    layout_center.putConstraint(SpringLayout.NORTH, label_nozzle, 15, SpringLayout.SOUTH, spinner_net_size)
    # Nozzles combo box
    combo_box_listener = ComboBoxListener()
    combo_box_nozzles.setActionCommand("combo_box_nozzles")
    combo_box_nozzles.addActionListener(combo_box_listener)
    nozzles = machine.defaultHead.getNozzles()
    for n in nozzles:
        combo_box_nozzles.addItem(n.name)
    layout_center.putConstraint(SpringLayout.WEST, combo_box_nozzles, 0, SpringLayout.WEST, spinner_net_size)
    layout_center.putConstraint(SpringLayout.VERTICAL_CENTER, combo_box_nozzles, 0, SpringLayout.VERTICAL_CENTER, label_nozzle)
    
    # Label part
    label_part = JLabel("Select part:")
    layout_center.putConstraint(SpringLayout.WEST, label_part, 25, SpringLayout.WEST, panel_main)
    layout_center.putConstraint(SpringLayout.NORTH, label_part, 15, SpringLayout.SOUTH, combo_box_nozzles)
    # Parts combo box
    combo_box_parts.setActionCommand("combo_box_parts")
    combo_box_parts.addActionListener(combo_box_listener)
    layout_center.putConstraint(SpringLayout.WEST, combo_box_parts, 0, SpringLayout.WEST, combo_box_nozzles)
    layout_center.putConstraint(SpringLayout.VERTICAL_CENTER, combo_box_parts, 0, SpringLayout.VERTICAL_CENTER, label_part)

    # Is placed checkbox
    checkbox_placed.setText(" Is part placed?")
    checkbox_placed.setSelected(True)
    layout_center.putConstraint(SpringLayout.WEST, checkbox_placed, 25, SpringLayout.WEST, panel_main)
    layout_center.putConstraint(SpringLayout.NORTH, checkbox_placed, 25, SpringLayout.SOUTH, combo_box_parts)
    
    # Random offset checkbox
    checkbox_random_pick_offset.setText(" Apply random pick offset?")
    checkbox_random_pick_offset.setSelected(True)
    layout_center.putConstraint(SpringLayout.WEST, checkbox_random_pick_offset, 25, SpringLayout.WEST, panel_main)
    layout_center.putConstraint(SpringLayout.NORTH, checkbox_random_pick_offset, 10, SpringLayout.SOUTH, checkbox_placed)

    # Text area log
    text_area_log_scroll = JScrollPane(text_area_log, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    layout_center.putConstraint(SpringLayout.HORIZONTAL_CENTER, text_area_log_scroll, 0, SpringLayout.HORIZONTAL_CENTER, panel_main)
    layout_center.putConstraint(SpringLayout.NORTH, text_area_log_scroll, 25, SpringLayout.SOUTH, checkbox_random_pick_offset)
    
    # Start button
    buttons_listener = ButtonListener()
    btn_start.setText("Start")
    btn_start.setPreferredSize(Dimension(150, 30))
    btn_start.addActionListener(buttons_listener)
    layout_center.putConstraint(SpringLayout.SOUTH, btn_start, -15, SpringLayout.SOUTH, panel_main)
    layout_center.putConstraint(SpringLayout.EAST, btn_start, -25, SpringLayout.EAST, panel_main)
    
    # Panel
    panel_main.add(label_net_size_spinner)
    panel_main.add(label_nozzle)
    panel_main.add(label_part)
    panel_main.add(spinner_net_size)
    panel_main.add(checkbox_placed)
    panel_main.add(checkbox_random_pick_offset)
    panel_main.add(combo_box_nozzles)
    panel_main.add(combo_box_parts)
    panel_main.add(btn_start)
    panel_main.add(text_area_log_scroll)
    
    frame.add(panel_main)
    frame.setVisible(JavaBoolean(True))   

def main():    
    global thrunnable
    thrunnable = submitUiMachineTask(start_job)  
    
def get_bottom_vision():
    #for c in machine.getAllCameras():
    #    if c.getName() == "UP":
    #        return c
            
    part_alignments = machine.getPartAlignments()
    if part_alignments.size() != 1:
        finish_job("Wrong number of part_alignments!") # multiple up looking cameras?#

    return part_alignments[0]

def get_part_feeder(part):
    for f in machine.getFeeders():
        if f.isEnabled() == False:
            continue
        if f.getPart() == part:
            return f

def mean(data):
    n = len(data)
    return sum(data) / n
    
def standard_deviation(data):
    n = len(data)
    mean = sum(data) / n
    deviations = [(x - mean) ** 2 for x in data]
    variance = sum(deviations) / n
    return math.sqrt(variance)

def get_random_offset():
    offset = Location(LengthUnit.Millimeters, 0, 0, 0, 0)
    offset_x = Location(LengthUnit.Millimeters, 0.1, 0, 0, 0)
    offset_y = Location(LengthUnit.Millimeters, 0, 0.1, 0, 0)
    
    if random.random() < 0.5:
        if random.random() < 0.5:
            offset = offset.add(offset_x)
        else:
            offset = offset.subtract(offset_x)
           
    if random.random() < 0.5: 
        if random.random() < 0.5:
            offset = offset.add(offset_y)
        else:
            offset = offset.subtract(offset_y)
        
    return offset
    
def calculate_place_location(root_location, i, j):
    offset = 1.0 # mm
    net_offset_x = Location(LengthUnit.Millimeters, offset*i, 0, 0, 0)
    net_offset_y = Location(LengthUnit.Millimeters, 0, offset*j, 0, 0)
    
    return root_location.add(net_offset_x).add(net_offset_y)

def start_job():
    print("Start")
    init_frame()
    
    # combo_box_nozzles.setSelectedItem("N2")
    # combo_box_parts.setSelectedItem("CALIBRATION_N2_1.2x1.2")
    text_area_log.append("    |\tX\tY\t| bottom vision\t\t|random offset\n")
    text_area_log.append('-'*80)
    
    while start == False:
        time.sleep(0.2)
    
    
    nozzle = machine.defaultHead.getNozzleByName(selected_nozzle)
    part = config.getPart(selected_part)
    bottom_vision = get_bottom_vision()
    print(bottom_vision.getName());
    print("nozzle: {}".format(nozzle))
    print("part: {}".format(part))
    
    calibration_feeder = machine.getFeederByName("calibration-loose-feeder")
    root_place_location = calibration_feeder.getLocation()
    print("root_place_location: {}".format(root_place_location))
    print("calibration_feeder: {}".format(calibration_feeder))
    
    # Find pick location -> 
    # if placed - calibration feeder location, 
    # if not placed - root feeder of part 
    pick_location = None    
    if checkbox_placed.isSelected() == False:
        # If part is not placed at "root_place_location", place it first
        feeder = get_part_feeder(part)
        feeder.feed(nozzle)
        pick_location = feeder.getPickLocation()
    else:
        calibration_feeder.feed(nozzle)
        pick_location = calibration_feeder.getPickLocation()

    errors_x = []
    errors_y = []
    net_size = spinner_net_size.getValue()

    for i in range(net_size):
        for j in range(net_size):    
        
            # Apply random pick offset
            random_pick_offset = None
            if checkbox_random_pick_offset.isSelected() == True:
                random_pick_offset = get_random_offset()
                print("random_pick_offset={}".format(random_pick_offset))
                pick_location = pick_location.add(random_pick_offset)
                
            # Pick part
            MovableUtils.moveToLocationAtSafeZ(nozzle, pick_location)
            nozzle.pick(part)
            
            # Calculate place location
            place_location = calculate_place_location(root_place_location, i, j)
            print("place_location: {}".format(place_location))
            calibration_feeder.setLocation(place_location)
            
            # Calculate bottom vision offset
            print("Calculating bottom vision offsets...")
            bottom_vision_offset = bottom_vision.findOffsets(
                    part,           # Part (for vision pipeline)
                    None,           # BoardLocation 
                    place_location, # Location 
                    nozzle          # Nozzle
                ).location          # returns PartAlignmentOffset object (Location and PreRotated)
            place_location_corrected = place_location.subtractWithRotation(bottom_vision_offset).add(Location(LengthUnit.Millimeters, 0, 0, part.getHeight().getValue(), 0))
            print("place_location_corrected: {}".format(place_location_corrected))
                
            # Place part
            MovableUtils.moveToLocationAtSafeZ(nozzle, place_location_corrected)
            nozzle.moveTo(place_location_corrected)
            nozzle.place()
            
            # Detect placed part location
            calibration_feeder.feed(nozzle)
            pick_location = calibration_feeder.getPickLocation()
            
            # Calculate errors
            error_x = place_location.getX() - pick_location.getX()
            error_y = place_location.getY() - pick_location.getY()
            errors_x.append(error_x)
            errors_y.append(error_y)

            print("Location {}/{} found:\n".format(i*net_size+j+1, net_size*net_size))
            log_string = "\n{}/{} |".format(i*net_size+j+1, net_size*net_size)
            log_string += "\t{}um\t{}um".format(
                int(error_x * 1000), 
                int(error_y * 1000))
            log_string += "\t|  X:{}um, Y:{}um".format(
                int(bottom_vision_offset.getX() * 1000), 
                int(bottom_vision_offset.getY() * 1000))       
            if random_pick_offset != None:
                log_string += "\t|  X:{}um, Y:{}um".format(
                    int(random_pick_offset.getX() * 1000), 
                    int(random_pick_offset.getY() * 1000))       
            text_area_log.append(log_string)
            text_area_log.setCaretPosition(text_area_log.getDocument().getLength())
            
    print("Measurements done")
    text_area_log.append("\n" + '-' * 80 + "\n")
    
    # Re-assign root feeder location
    calibration_feeder.setLocation(root_place_location)
    
    # Calculate average and standard deviation
    average_x = mean(errors_x)
    average_y = mean(errors_y)
    
    std_dev_x = standard_deviation(errors_x)
    std_dev_y = standard_deviation(errors_y)
    
    print("average_x: {}".format(round(average_x, 3)))
    print("average_y: {}".format(round(average_y, 3)))
    print("std_dev_x: {}".format(round(std_dev_x, 3)))
    print("std_dev_y: {}".format(round(std_dev_y, 3)))
   
    
    # Calculate and apply offsets
    #
    # example offsets N2:
    # x: 9.080
    # y: 36.080
    # ->
    # average_x: -0.131
    # average_y: 0.043
    # ->
    # 9.080 - (-0.131) = 9.211
    # 36.080 - 0.043 = 36.037
    nozzle_offset = nozzle.getHeadOffsets() 
    corrected_nozzle_offset = Location(LengthUnit.Millimeters, 
        nozzle_offset.getX() - average_x, 
        nozzle_offset.getY() - average_y, 
        nozzle_offset.getZ(), 
        nozzle_offset.getRotation()
    )
    print("nozzle_offset: {}".format(nozzle_offset))
    print("corrected_nozzle_offset: {}".format(corrected_nozzle_offset))
    
    err_string = "Errors:\n"
    err_string += "average_x: {}um\naverage_y: {}um\n".format(int(average_x * 1000), int(average_y * 1000))
    err_string += "std_dev_x: {}um\nstd_dev_y: {}um\n".format(int(std_dev_x * 1000), int(std_dev_y * 1000))
    
    text_area_log.append(err_string)
    text_area_log.setCaretPosition(text_area_log.getDocument().getLength())
    
    confirm = showConfirmDialog(frame, 
        err_string,
        "Do you want to apply offsets?", 
        YES_NO_OPTION, QUESTION_MESSAGE
    )
    
    if confirm == YES_OPTION:
        nozzle.setHeadOffsets(corrected_nozzle_offset)

    #frame.dispose() # close JFrame
    
    
main()
