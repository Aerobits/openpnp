package org.openpnp.vision.pipeline.stages;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.prefs.Preferences;

import org.opencv.imgcodecs.Imgcodecs;
import org.openpnp.gui.JobPanel;
import org.openpnp.model.Configuration;
import org.openpnp.model.Part;
import org.openpnp.spi.Nozzle;
import org.openpnp.util.LogUtils;
import org.openpnp.vision.pipeline.CvPipeline;
import org.openpnp.vision.pipeline.CvStage;
import org.pmw.tinylog.Logger;
import org.simpleframework.xml.Attribute;

/**
 * Save the working image as an image file in the debug directory using the specified prefix and
 * suffix. The suffix should be a file extension (including the period).
 */
public class ImageWriteDebug extends CvStage {
    @Attribute
    private String prefix = "debug";

    @Attribute
    private String suffix = ".png";

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
 
    @Override
    public Result process(CvPipeline pipeline) throws Exception {
        if (!LogUtils.isDebugEnabled()) {
            return null;
        }
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss.SS");
		LocalDateTime now = LocalDateTime.now();
		String nozzleAndPartInfo = ""; // String.format("%s_%s", dtf.format(now), prefix);

		Nozzle n = (Nozzle) pipeline.getProperty("nozzle");
		if (n != null) {
			nozzleAndPartInfo += String.format("_%s", n.getId());
		}
		
		Part p = (Part) pipeline.getProperty("part");
		if (p != null) {
			nozzleAndPartInfo += String.format("_%s", p.getId());
		}

		File directory = new File(Configuration.get().getConfigurationDirectory(), getClass().getSimpleName());
		if (!directory.exists()) {
			directory.mkdirs();
		}

		// TODO: with prefix it doesn't work - multiple datetimes in filename
		String filename = String.format("%s%s%s", dtf.format(now), nozzleAndPartInfo, suffix);
		File file = new File(directory, filename);
		Imgcodecs.imwrite(file.getAbsolutePath(), pipeline.getWorkingImage());

		return null;
	}
}
