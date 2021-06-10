package org.openpnp.machine.neoden4;

import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.neoden4.wizards.Neoden4SignalerConfigurationWizard;
import org.openpnp.model.Configuration;
import org.openpnp.spi.Driver;
import org.openpnp.spi.base.AbstractJobProcessor;
import org.openpnp.spi.base.AbstractMachine;
import org.openpnp.spi.base.AbstractSignaler;
import org.simpleframework.xml.Attribute;

public class Neoden4Signaler extends AbstractSignaler {

    @Attribute
    protected boolean enableErrorSound;

    @Attribute
    protected boolean enableFinishedSound;

    private ClassLoader classLoader = getClass().getClassLoader();
    
//    private NeoDen4Driver neoden4Driver;
//    
//    public Neoden4Signaler() {
//    	this.neoden4Driver = getNeoden4Driver();
//    }
    
	private NeoDen4Driver getNeoden4Driver() {
		NeoDen4Driver driver = null;

		for (Driver d : Configuration.get().getMachine().getDrivers()) {
			if (d instanceof NeoDen4Driver) {
				driver = (NeoDen4Driver) d;
				break;
			}
		}
		return driver;
	}

	public void playErrorSound() {
		try {
			NeoDen4Driver neoden4Driver = getNeoden4Driver();
			neoden4Driver.setBuzzer(true);
			Thread.sleep(250);
			neoden4Driver.setBuzzer(false);
			Thread.sleep(250);
			neoden4Driver.setBuzzer(true);
			Thread.sleep(250);
			neoden4Driver.setBuzzer(false);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void playSuccessSound() {
		try {
			NeoDen4Driver neoden4Driver = getNeoden4Driver();
			neoden4Driver.setBuzzer(true);
			Thread.sleep(30);
			neoden4Driver.setBuzzer(false);
			Thread.sleep(30);
			neoden4Driver.setBuzzer(true);
			Thread.sleep(30);
			neoden4Driver.setBuzzer(false);
			Thread.sleep(30);
			neoden4Driver.setBuzzer(true);
			Thread.sleep(30);
			neoden4Driver.setBuzzer(false);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public void signalMachineState(AbstractMachine.State state) {
        switch (state) {
            case ERROR: {
                playErrorSound();
                break;
            }
        }
    }

    @Override
    public void signalJobProcessorState(AbstractJobProcessor.State state) {
        switch (state) {
            case ERROR: {
                playErrorSound();
                break;
            }

            case FINISHED: {
                playSuccessSound();
                break;
            }
        }
    }

    @Override
    public Wizard getConfigurationWizard() {
        return new Neoden4SignalerConfigurationWizard(this);
    }

    public boolean isEnableErrorSound() {
        return enableErrorSound;
    }

    public void setEnableErrorSound(boolean enableErrorSound) {
        this.enableErrorSound = enableErrorSound;
    }

    public boolean isEnableFinishedSound() {
        return enableFinishedSound;
    }

    public void setEnableFinishedSound(boolean enableFinishedSound) {
        this.enableFinishedSound = enableFinishedSound;
    }
}