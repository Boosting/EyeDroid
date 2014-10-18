package dk.itu.eyedroid;

import java.io.IOException;

import dk.itu.eyedroid.io.InputNetStreamingProtocol;
import dk.itu.spcl.jlpf.core.Filter;
import dk.itu.spcl.jlpf.core.ProcessingCore;
import dk.itu.spcl.jlpf.io.IOController;
import dk.itu.spcl.jlpf.io.IOProtocolReader;
import dk.itu.spcl.jlpf.io.IOProtocolWriter;
import dk.itu.spcl.jlpf.io.IORWDefaultImpl;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class TestFragment extends Fragment {
	final String URL = "http://217.197.157.7:7070/axis-cgi/mjpg/video.cgi?resolution=320x240";

	private View mRootView;
	private ImageView mImageView;

	private ProcessingCore core;
	private IOController ioController;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRootView = inflater.inflate(R.layout.streaming_layout, container,
				false);
		mImageView = (ImageView) mRootView.findViewById(R.id.mjpeg_view);

		InputNetStreamingProtocol inProtocol = new InputNetStreamingProtocol(
				URL);
		IORWDefaultImpl readerWriter = new IORWDefaultImpl(inProtocol,
				new TestWriter());

		core = new ProcessingCore();
		core.addFilter(new TestFilter());
		core.addFilter(new TestFilter());

		ioController = new AndroidIOController(core, readerWriter, readerWriter);

		return mRootView;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		core.start(1);
		ioController.start();

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		core.stop();
		ioController.stop();
	}

	public class AndroidIOController extends IOController {

		private ReadingThread readerThread;
		private WritingThread writerThread;

		public AndroidIOController(ProcessingCore core,
				dk.itu.spcl.jlpf.io.InputReader reader,
				dk.itu.spcl.jlpf.io.OutputWriter writer) {
			super(core, reader, writer);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void init() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void onExecute() {

			readerThread = new ReadingThread();
			writerThread = new WritingThread();

			readerThread.start();
			writerThread.start();

		}

		@Override
		protected void onStop() {
			// TODO Auto-generated method stub

			readerThread.stopThread();
			writerThread.stopThread();

		}

		public class ReadingThread extends Thread {

			private volatile boolean isStopped = false;

			@Override
			public void run() {
				((IORWDefaultImpl) InputReader).initReader();
				while (!isStopped) {
					try {
						Log.i("------------------",
								"reader thread is fucking running");
						AndroidIOController.this.read();
					} catch (IOException e) {
						((IORWDefaultImpl) InputReader).cleanup();
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
				}

			}

			public void stopThread() {
				isStopped = true;
			}
		}

		public class WritingThread extends Thread {

			private volatile boolean isStopped = false;

			public void stopThread() {
				isStopped = true;
			}

			@Override
			public void run() {
				while (!isStopped) {
					try {
						Log.i("------------------",
								"writer thread is fucking running");
						AndroidIOController.this.write();
					} catch (IOException e) {
						((IORWDefaultImpl) OutputWriter).cleanup();
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
				}
			}
		}
	}

	public class TestWriter implements IOProtocolWriter {

		@Override
		public void cleanup() {

		}

		@Override
		public void init() {

		}

		@Override
		public void write(dk.itu.spcl.jlpf.common.Bundle arg0)
				throws IOException {

			Log.i("-----------------------------", "Writer is active");

			final Bitmap bitmap = (Bitmap) arg0
					.get(InputNetStreamingProtocol.INPUT_BITMAP);
			TestFragment.this.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mImageView.setImageBitmap(bitmap);

				}
			});

			arg0 = null;

		}
	}

	public class TestFilter extends Filter {

		@Override
		protected dk.itu.spcl.jlpf.common.Bundle execute(
				dk.itu.spcl.jlpf.common.Bundle arg0) {
			// TODO Auto-generated method stub
			Log.i("-------------------------", "in filter");
			return arg0;
		}

	}

}
