package org.openpnp.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openpnp.model.Configuration;

public class CalibrationLogger {

	public static void addToLog(String str) {
		try {
			File configurationDirectory = Configuration.get().getConfigurationDirectory();
			File logFile = new File(configurationDirectory, "calibration.log");
			FileWriter fr = new FileWriter(logFile, true);
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			
			fr.append(String.format(String.format("\n%s\t%s", dtf.format(now), str)));
			fr.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
