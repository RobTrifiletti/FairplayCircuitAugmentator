import java.io.File;


public class Driver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 1){
			System.out.println("Incorrect number of arguments, please specify inputfile");
			return;
		}

		String circuitFilename = null;
		String outputFilename = null;

		/*
		 * Parse the arguments
		 */
		for(int param = 0; param < args.length; param++){			
			if (circuitFilename == null) {
				circuitFilename = args[param];
			}

			else if (outputFilename == null) {
				outputFilename = args[param];
			}

			else System.out.println("Unparsed: " + args[param]); 
		}
		if(outputFilename == null) {
			outputFilename = "data/out.txt";
		}
		File circuitFile = new File(circuitFilename);

		File outputFile = new File(outputFilename);

		if (!circuitFile.exists()){
			System.out.println("Inputfile: " + circuitFile.getName() + " not found");
			return;
		}
		
		CircuitAugmentator ca = new CircuitAugmentator(circuitFile, outputFile);
		ca.run();
	}

}
