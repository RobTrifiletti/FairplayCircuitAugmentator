import java.io.File;


public class CircuitAugmentator implements Runnable {

	private File circuitFile;
	private File outputFile;
	
	public CircuitAugmentator(File circuitFile, File outputFile){
		this.circuitFile = circuitFile;
		this.outputFile = outputFile;
	}
	
	@Override
	public void run() {
		
	}
	
}
