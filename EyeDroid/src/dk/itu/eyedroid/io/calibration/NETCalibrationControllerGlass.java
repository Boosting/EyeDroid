package dk.itu.eyedroid.io.calibration;

import java.io.IOException;

import org.opencv.core.Point;

import android.content.Context;
import android.util.Log;
import dk.itu.eyedroid.io.NetClientConfig;

public class NETCalibrationControllerGlass extends NETCalibrationController {

	/**
	 * Default constructor
	 * 
	 * @param server
	 * @param mapper
	 *            Calibration mapper
	 */
	public NETCalibrationControllerGlass(CalibrationMapper mapper, Context context) {
		super(mapper);
	}

	/**
	 * Calibration process
	 * 
	 * @param receivePacket
	 *            Packet receieved from client.
	 * @throws IOException
	 * @throws InterruptedException
	 */

	public void calibrate() throws IOException {

		int[] message;
		boolean error = false;

		if (mCalibrationCallbacks != null)
			mCalibrationCallbacks.onCalibrationStarted();
		
		this.mCalibrationMapper.clean();

		try {
			super.mServer.send(NetClientConfig.TO_CLIENT_CALIBRATE_DISPLAY, -1,
					-1);
			
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int counter = 0;
			while (counter < NetClientConfig.NO_POINTS) {

				message = super.mServer.read();
				while (message[0] == -1) {
					message = super.mServer.read();
				}
				if (NetClientConfig.TO_EYEDROID_READY != message[0]) {
					Log.i(NetClientConfig.TAG,
							"Mesasge is not TO_EYEDROID_READY " + message[0]);
					continue;
				}
				// get the calibration point from the mapper and send it
				// to the cliend
				Point clientPoint = NETCalibrationControllerGlass.this.mCalibrationMapper
						.getCalibrationPoint(counter);

				super.mServer.send(NetClientConfig.TO_CLIENT_CALIBRATE_DISPLAY,
						(int) clientPoint.x, (int) clientPoint.y);

				final Point serverPoint = getSampleFromCore();

				setUpPointsToMapper(clientPoint, serverPoint);

				counter++;
			}

			if (!error) {
				super.mServer.send(NetClientConfig.TO_CLIENT_CALIBRATE_DISPLAY,
						-2, -2);
				NETCalibrationControllerGlass.this.mCalibrationMapper
						.calibrate();
				if (mCalibrationCallbacks != null)
					mCalibrationCallbacks.onCalibrationFinished();
			} else {
				if (mCalibrationCallbacks != null)
					mCalibrationCallbacks.onCalibrationError();
			}
		} catch (IOException e) {
			if (mCalibrationCallbacks != null)
				mCalibrationCallbacks.onCalibrationError();
		}

	}

	@Override
	protected Point getSampleFromCore() {

		int[] xy;
		int sumX = 0, sumY = 0;
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (int j = 0; j < NetClientConfig.NO_SAMPLES; j++) {
//			Thread.currentThread();
			try {
				Thread.sleep(NetClientConfig.WAIT_TO_SAMPLE);
				if (mOutputProtocol != null) {
					xy = this.mOutputProtocol.getXY();
					sumX += xy[0];
					sumY += xy[1];
				}
			} catch (InterruptedException e) {
			}

		}
		return new Point(sumX / NetClientConfig.NO_SAMPLES, sumY
				/ NetClientConfig.NO_SAMPLES);
	}
}
