import javax.swing.SwingUtilities;

public class FlowVolumeRunner implements Runnable {

	public void run() {
		FlowVolumeViewer c = new FlowVolumeViewer();
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new FlowVolumeRunner());
	}

}
