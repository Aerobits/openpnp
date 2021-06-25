'''
	Script that tests microstepping accuracy.
	You can read more about it there:
	https://hackaday.com/2016/08/29/how-accurate-is-microstepping-really/
	
	Used method is based on vision, so make sure that
	you have calibrated lens distorsion and px/mm value
	very well. 
	
	What it does is move about 'line_step_um' in 
	both axis, measure the home fiducial lication and 
	plots it on chart. Ideally the line should be 
	straight.
	
	To run this script you need to have JFreeChart
	dependency added to your pom.xml
'''

# OpenPnP
from org.openpnp.util.UiUtils import submitUiMachineTask
from org.openpnp.model import LengthUnit, Location
from org.openpnp.util import VisionUtils, MovableUtils
from org.openpnp.model import Board
from org.openpnp.model import BoardLocation

# Java
import java.io.File 
import java.io.FileOutputStream
import java.lang.Boolean as JavaBoolean
from javax.swing.JOptionPane import showMessageDialog, INFORMATION_MESSAGE
from javax.swing import JFrame, JPanel, JLabel, JButton, JTextArea, JScrollPane, JSpinner, JCheckBox
from javax.swing import SpringLayout, SpinnerNumberModel, BorderFactory, BoxLayout
from java.awt.event import ActionListener, WindowEvent, WindowAdapter
from java.awt import Dimension, Color, Font, FontMetrics, BorderLayout, BasicStroke

# JFreeChart
from org.jfree.chart import JFreeChart, ChartFactory, ChartUtils, ChartPanel
from org.jfree.chart.plot import PlotOrientation, XYPlot
from org.jfree.data.xy import XYSeries, XYDataset, XYSeriesCollection
from org.jfree.chart.renderer.xy import XYLineAndShapeRenderer


# Python
import time
import math 
import random

spinner_passes = JSpinner()
text_area_log = JTextArea(12, 80)
btn_start = JButton()
thrunnable = None
frame = None
start = False
stop = False
chart = None

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
 
def create_chart(data_x, data_y):
    series_x = XYSeries("Error X", False, True)
    series_y = XYSeries("Error Y", False, True)
    series_x_mean = XYSeries("Error X mean", False, False)
    series_y_mean = XYSeries("Error Y mean", False, False)
    
    if len(data_x) > 0:
        for (ms, off) in data_x:
            series_x.add(ms, off)
            
    if len(data_y) > 0:
        for (ms, off) in data_y:
            series_y.add(ms, off)
        
    dataset = XYSeriesCollection()
    dataset.addSeries(series_x)
    dataset.addSeries(series_y)
    dataset.addSeries(series_x_mean)
    dataset.addSeries(series_y_mean)

    chart = ChartFactory.createXYLineChart(None, "Microstep", "Error [um]", dataset, PlotOrientation.VERTICAL, True, False, False)

    renderer = XYLineAndShapeRenderer()
    renderer.setSeriesShapesVisible(0, True)
    renderer.setSeriesShapesVisible(1, True)
    renderer.setSeriesShapesVisible(2, False)
    renderer.setSeriesShapesVisible(3, False)
    renderer.setSeriesLinesVisible(0, False)
    renderer.setSeriesLinesVisible(1, False)
    renderer.setSeriesLinesVisible(2, True)
    renderer.setSeriesLinesVisible(3, True)
    renderer.setSeriesPaint(0, Color(0.0, 0.0, 1.0, 0.33))
    renderer.setSeriesStroke(0, BasicStroke(1.0))
    renderer.setSeriesPaint(1, Color(1.0, 0.0, 0.0, 0.33))
    renderer.setSeriesStroke(1, BasicStroke(1.0))
    renderer.setSeriesPaint(2, Color(0.0, 0.0, 1.0, 1.0))
    renderer.setSeriesStroke(2, BasicStroke(2.0))
    renderer.setSeriesPaint(3, Color(1.0, 0.0, 0.0, 1.0))
    renderer.setSeriesStroke(3, BasicStroke(2.0))
    
    plot = chart.getXYPlot()
    plot.setRenderer(renderer)
    plot.setBackgroundPaint(Color.WHITE)
    plot.setRangeGridlinesVisible(True)
    plot.setRangeGridlinePaint(Color.BLACK)
    plot.setDomainGridlinesVisible(True)
    plot.setDomainGridlinePaint(Color.BLACK)
    # plot.getRangeAxis(0).setRange(-60, 60); # Y range
    plot.getDomainAxis(0).setRange(-1, 33); # X range
    
    return chart 
 
def init_frame():
    global frame
    frame = JFrame("CALIBRATION TOOLBOX")
    frame.setSize(700, 650) 
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE)
    window_listener = JFrameListener()
    frame.addWindowListener(window_listener)

    # Main panel
    layout_center = SpringLayout()
    panel_main = JPanel(layout_center)
    
    # Text area log
    text_area_log_scroll = JScrollPane(text_area_log, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    layout_center.putConstraint(SpringLayout.HORIZONTAL_CENTER, text_area_log_scroll, 0, SpringLayout.HORIZONTAL_CENTER, panel_main)
    layout_center.putConstraint(SpringLayout.NORTH, text_area_log_scroll, 25, SpringLayout.NORTH, panel_main)
    
    # Chart
    global chart
    chart = create_chart([], [])
    chart_panel = ChartPanel(chart, False)
    chart_panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15))
    chart_panel.setPreferredSize(Dimension(675, 250))
    chart_panel.setBackground(frame.getBackground())
    layout_center.putConstraint(SpringLayout.HORIZONTAL_CENTER, chart_panel, 0, SpringLayout.HORIZONTAL_CENTER, panel_main)
    layout_center.putConstraint(SpringLayout.NORTH, chart_panel, 25, SpringLayout.SOUTH, text_area_log_scroll)
    
    # Label passes count
    label_spinner_passes = JLabel("Passes count:")
    layout_center.putConstraint(SpringLayout.WEST, label_spinner_passes, 25, SpringLayout.WEST, chart_panel)
    layout_center.putConstraint(SpringLayout.VERTICAL_CENTER, label_spinner_passes, 0, SpringLayout.VERTICAL_CENTER, btn_start)
    # Passes spinner
    spinner_passes.setModel(SpinnerNumberModel(3, 1, 10, 1))
    layout_center.putConstraint(SpringLayout.WEST, spinner_passes, 10, SpringLayout.EAST, label_spinner_passes)
    layout_center.putConstraint(SpringLayout.VERTICAL_CENTER, spinner_passes, 0, SpringLayout.VERTICAL_CENTER, label_spinner_passes)
     
    # Start button
    buttons_listener = ButtonListener()
    btn_start.setText("Start")
    btn_start.setPreferredSize(Dimension(150, 30))
    btn_start.addActionListener(buttons_listener)
    layout_center.putConstraint(SpringLayout.SOUTH, btn_start, -15, SpringLayout.SOUTH, panel_main)
    layout_center.putConstraint(SpringLayout.EAST, btn_start, -25, SpringLayout.EAST, panel_main)
    
    # Panel
    panel_main.add(text_area_log_scroll)
    panel_main.add(chart_panel)
    panel_main.add(spinner_passes)
    panel_main.add(label_spinner_passes)
    panel_main.add(btn_start)
    
    frame.add(panel_main)
    frame.setVisible(JavaBoolean(True))   

def main():    
    global thrunnable
    thrunnable = submitUiMachineTask(start_job)  

def mean(data):
    n = len(data)
    return sum(data) / n
    
def standard_deviation(data):
    n = len(data)
    mean = sum(data) / n
    deviations = [(x - mean) ** 2 for x in data]
    variance = sum(deviations) / n
    return math.sqrt(variance)
   
def add_to_log(message):
    text_area_log.append(message)
    text_area_log.setCaretPosition(text_area_log.getDocument().getLength())
   
def start_job():
    print("Starting 'microstep_accuracy_test.py")
    init_frame()
    
    down_looking_camera = machine.defaultHead.getDefaultCamera()
    fiducial_locator = machine.getFiducialLocator()
    home_fid_location = machine.defaultHead.getHomingFiducialLocation() 
    home_fiducial_part = config.getPart("FIDUCIAL-HOME")
    
    while start == False:
        time.sleep(0.2)
    
    t_start = time.time()
    
    # Locate home fiducial accurate location
    home_fid_location = fiducial_locator.getHomeFiducialLocation(home_fid_location, home_fiducial_part)
    down_looking_camera.moveTo(home_fid_location)
    home_fid_location = fiducial_locator.getFiducialLocationWithoutMove(home_fid_location, home_fiducial_part)
        
    center_x = home_fid_location.getX()
    center_y = home_fid_location.getY()
    print("saved fiducial center: {}, {}".format(center_x, center_y))
    
    errors_x_mean = [0 for i in range(0, 32)]
    errors_y_mean = [0 for i in range(0, 32)]
    passes_count = spinner_passes.getValue() 
	
	line_step_um = 10.0 # adjust the value to change test distance 
    
    for i in range(passes_count): 
        for j in range(1, 32+1): # 1-33
            # Go to 'j' location of line
            print("offset = {}".format(((j - 16.0) * (line_step_um / 1000.0))))
            move_x = center_x + ((j - 16) * (line_step_um / 1000.0))
            move_y = center_y + ((j - 16) * (line_step_um / 1000.0))
            print("move to {}, {}".format(move_x, move_y))
            down_looking_camera.moveTo(Location(LengthUnit.Millimeters, move_x, move_y, 0, 0))
            
            # Locate fiducial location
            current_home_fid_location = fiducial_locator.getFiducialLocationWithoutMove(home_fid_location, home_fiducial_part)
            print(current_home_fid_location)
            
            # Calculate errors
            error_x = (center_x - current_home_fid_location.getX()) * 1000.0  
            error_y = (center_y - current_home_fid_location.getY()) * 1000.0 
            
            errors_x_mean[(i*32+j-1)%32] += error_x
            errors_y_mean[(i*32+j-1)%32] += error_y

            chart.getXYPlot().getDataset().getSeries("Error X").addOrUpdate((i*32+j-1)%32, error_x)
            chart.getXYPlot().getDataset().getSeries("Error Y").addOrUpdate((i*32+j-1)%32, error_y)
            
            log_string = "\n{}/{} | ".format(j + (i*32), 32*passes_count)
            log_string += "X:{}um\tY:{}um".format(int(error_x), int(error_y))
            log_string += "\tline_offset: X:{}um\tY:{}um".format(line_step_um*(j-16), line_step_um*(j-16))
                
            add_to_log(log_string)
        
    print("Measurements done")
    
    for i in range(0, 32):
        errors_x_mean[i] = errors_x_mean[i] / passes_count
        errors_y_mean[i] = errors_y_mean[i] / passes_count
        chart.getXYPlot().getDataset().getSeries("Error X mean").addOrUpdate(i, errors_x_mean[i])
        chart.getXYPlot().getDataset().getSeries("Error Y mean").addOrUpdate(i, errors_y_mean[i])
        
    # Calculate average and standard deviation
    average_x = mean(errors_x_mean)
    average_y = mean(errors_y_mean)
    print("average_x: {}um".format(average_x))
    print("average_y: {}um".format(average_y))
    std_dev_x = standard_deviation(errors_x_mean)
    std_dev_y = standard_deviation(errors_y_mean)
    print("std_dev_x: {}um".format(std_dev_x))
    print("std_dev_y: {}um".format(std_dev_y))
   
    err_string = "\nErrors:\n"
    err_string += "average_x: {}um\naverage_y: {}um\n".format(int(average_x), int(average_y))
    err_string += "std_dev_x: {}um\nstd_dev_y: {}um\n".format(int(std_dev_x), int(std_dev_y))
    add_to_log(err_string)
    
    print("Done in {}s!".format(time.time() - t_start))
    
    #confirm = showMessageDialog(frame, 
    #    err_string,
    #    "Calculated offsets", 
    #    INFORMATION_MESSAGE
    #)
    
    
main()
