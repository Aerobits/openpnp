'''
	Script that allow you to choose part and 
	dispense it n times in given location.
	
	The location is the same as down-looking-camera 
	at the moment when you start script, so the 
	usage is as follow:
	  - move camera to location that you want to 
	    dispense parts
	  - run script
	  
	There is no error handling, so if you have set
	pressure detect on part pick, it will throw error
	and close frame. 
'''

# OpenPnP
from org.openpnp.util.UiUtils import submitUiMachineTask
from org.openpnp.model import LengthUnit, Location
from org.openpnp.util import MovableUtils

# Java
import java.lang.Boolean as JavaBoolean
from javax.swing.JOptionPane import showMessageDialog, INFORMATION_MESSAGE
from javax.swing import JFrame, JPanel, JLabel, JButton, JSpinner, JComboBox, JProgressBar
from javax.swing import SpringLayout, SpinnerNumberModel,  BorderFactory
from java.awt.event import ActionListener, WindowEvent, WindowAdapter
from java.awt import Dimension, Color, Font, FontMetrics, BorderLayout, BasicStroke

# Python
import time

combo_box_parts = JComboBox()
spinner_parts_count = JSpinner()
btn_start = JButton()
progress_bar = JProgressBar(0, 1)
selected_part = None
thrunnable = None
frame = None
start = False

class JFrameListener(WindowAdapter):
    def windowClosing(self, window_event):
        print("EXIT")
        frame.dispose() # close JFrame
        time.sleep(0.5)
        thrunnable.cancel(JavaBoolean(True))

class ButtonListener(ActionListener):
    def actionPerformed(self, event):
        global start

        if start == False:
            start = True
            btn_start.setText("Stop")
        else:
            start = False
            btn_start.setEnabled(JavaBoolean(False))
      
class ComboBoxListener(ActionListener):
    def actionPerformed(self, event):
        global selected_part 
  
        if event.getActionCommand() == "combo_box_parts":
            selected_part = combo_box_parts.getSelectedItem()      

def init_frame():
    global frame
    frame = JFrame("GIMME PARTS")
    frame.setSize(500, 210) 
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE)
    window_listener = JFrameListener()
    frame.addWindowListener(window_listener)

    # Main panel
    layout_center = SpringLayout()
    panel_main = JPanel(layout_center)

    # Label part
    label_part = JLabel("Select part:")
    layout_center.putConstraint(SpringLayout.NORTH, label_part, 15, SpringLayout.NORTH, panel_main)
    layout_center.putConstraint(SpringLayout.WEST, label_part, 25, SpringLayout.WEST, panel_main)
    # Parts combo box
    combo_box_parts.setActionCommand("combo_box_parts")
    combo_box_parts.addActionListener(ComboBoxListener())
    for p in config.getParts():
        combo_box_parts.addItem(p.getId())
    layout_center.putConstraint(SpringLayout.WEST, combo_box_parts, 10, SpringLayout.EAST, label_part)
    layout_center.putConstraint(SpringLayout.VERTICAL_CENTER, combo_box_parts, 0, SpringLayout.VERTICAL_CENTER, label_part)
    
    # Label passes count
    label_spinner_parts_count = JLabel("Parts count:")
    layout_center.putConstraint(SpringLayout.NORTH, label_spinner_parts_count, 25, SpringLayout.SOUTH, combo_box_parts)
    layout_center.putConstraint(SpringLayout.WEST, label_spinner_parts_count, 25, SpringLayout.WEST, panel_main)
    # Passes spinner
    spinner_parts_count.setModel(SpinnerNumberModel(1, 1, 50, 1))
    layout_center.putConstraint(SpringLayout.WEST, spinner_parts_count, 10, SpringLayout.EAST, label_spinner_parts_count)
    layout_center.putConstraint(SpringLayout.VERTICAL_CENTER, spinner_parts_count, 0, SpringLayout.VERTICAL_CENTER, label_spinner_parts_count)
     
    # Progress bar    
    layout_center.putConstraint(SpringLayout.HORIZONTAL_CENTER, progress_bar, 0, SpringLayout.HORIZONTAL_CENTER, panel_main)
    layout_center.putConstraint(SpringLayout.SOUTH, progress_bar, 0, SpringLayout.SOUTH, panel_main)
    progress_bar.setPreferredSize(Dimension(500, 20))
    progress_bar.setStringPainted(True)
    progress_bar.setValue(0)

    # Start button
    buttons_listener = ButtonListener()
    btn_start.setText("Start")
    btn_start.setPreferredSize(Dimension(450, 40))
    btn_start.addActionListener(buttons_listener)
    layout_center.putConstraint(SpringLayout.SOUTH, btn_start, -25, SpringLayout.SOUTH, panel_main)
    layout_center.putConstraint(SpringLayout.HORIZONTAL_CENTER, btn_start, 0, SpringLayout.HORIZONTAL_CENTER, panel_main)
    
    # Panel
    panel_main.add(label_part)
    panel_main.add(combo_box_parts)
    panel_main.add(label_spinner_parts_count)
    panel_main.add(spinner_parts_count)
    panel_main.add(progress_bar)
    panel_main.add(btn_start)
    
    frame.add(panel_main)
    frame.setVisible(JavaBoolean(True))   


def main():    
    global thrunnable
    thrunnable = submitUiMachineTask(start_job)  
   
def start_job():
    init_frame()
    
    while start == False:
        time.sleep(0.2)

    part = config.getPart(selected_part)

    # Find feeder for part
    feeder = None
    for f in machine.getFeeders():
        print(f.isEnabled())
        if f.isEnabled() == True:
            if f.getPart() == part:
                feeder = f
                break
    if feeder == None:
        showMessageDialog(None, "{}\n\n{}".format(
            "Error", 
            "Can't find part's feeder. Make sure that you have enabled feeder with selected part assigned."))
        frame.dispose()
        return

    # Find nozzle for part
    nozzle = None 
    for n in machine.defaultHead.getNozzles():
        if n.getNozzleTip() in part.getPackage().getCompatibleNozzleTips():
            nozzle = n
    if nozzle == None:
        showMessageDialog(None, "{}\n\n{}".format(
            "Error", 
            "Can't find proper nozzle to pick part. Make sure that you have nozzle tip assigned to part's package."))
        frame.dispose()
        return

    t_start = time.time()
    parts_count = spinner_parts_count.getValue()
    pick_location = feeder.getPickLocation()
    progress_bar.setMaximum(parts_count)
    place_location = nozzle.getLocation() # set place_location as nozzle location during script run
    
    for i in range(parts_count):
        if not start:
            print("Script stopped!")
            break
        
        # Pick part
        MovableUtils.moveToLocationAtSafeZ(nozzle, pick_location)
        nozzle.pick(part)

        # Move to place location and place part
        MovableUtils.moveToLocationAtSafeZ(nozzle, place_location)
        nozzle.moveTo(place_location)
        nozzle.place()
        
        progress_bar.setValue(i+1)


    park_location = machine.defaultHead.getParkLocation()
    MovableUtils.moveToLocationAtSafeZ(nozzle, park_location)

    print("Done in {}s".format(round(time.time()-t_start, 1)))
    showMessageDialog(None, "Done successfully in {}s!".format(round(time.time()-t_start, 1)))
    frame.dispose()

main()
