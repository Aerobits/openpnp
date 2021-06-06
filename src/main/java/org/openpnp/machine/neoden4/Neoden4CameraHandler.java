package org.openpnp.machine.neoden4;

public final class Neoden4CameraHandler implements Neoden4CamDll {

	public Neoden4CameraHandler() {
	}

	private static Neoden4CameraHandler instance;

	public static synchronized Neoden4CameraHandler getInstance() {
		if (instance == null) {
			instance = new Neoden4CameraHandler();

			int cameras = Neoden4CameraHandler.getInstance().img_init();
			if (cameras < 2) {
				System.out.println(String.format("Bummer, detected %d cameras...", cameras));
			} else {
				System.out.println(String.format("Detected %d neoden cameras...", cameras));
			}
		}
		return instance;
	}

	@Override
	public boolean img_capture(int which_camera) {
		return INSTANCE.img_capture(which_camera);
	}

	@Override
	public int img_init() {
		return INSTANCE.img_init();
	}

	@Override
	public boolean img_led(int camera, short mode) {
		return INSTANCE.img_led(camera, mode);
	}

	@Override
	public int img_read(int which_camera, byte[] pFrameBuffer, int BytesToRead, int timeoutMs) {
		return INSTANCE.img_read(which_camera, pFrameBuffer, BytesToRead, timeoutMs);
	}

	@Override
	public int img_readAsy(int which_camera, byte[] pFrameBuffer, int BytesToRead, int timeoutMs) {
		return INSTANCE.img_readAsy(which_camera, pFrameBuffer, BytesToRead, timeoutMs);
	}

	@Override
	public int img_reset(int which_camera) {
		return INSTANCE.img_reset(which_camera);
	}

	@Override
	public boolean img_set_exp(int which_camera, short exposure) {
		return INSTANCE.img_set_exp(which_camera, exposure);
	}

	@Override
	public boolean img_set_gain(int which_camera, short gain) {
		return INSTANCE.img_set_gain(which_camera, gain);
	}

	@Override
	public boolean img_set_lt(int which_camera, short a2, short a3) {
		return INSTANCE.img_set_lt(which_camera, a2, a3);
	}

	@Override
	public boolean img_set_wh(int which_camera, short w, short h) {
		return INSTANCE.img_set_wh(which_camera, w, h);
	}

}
