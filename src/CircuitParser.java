import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class CircuitParser {

	private File circuitFile;
	private Charset charset;
	private int numberOfNonXORGates;
	private int totalNumberOfInputs;
	private int numberOfAliceInputs;
	private String firstHeader;
	private String secondHeader;

	public CircuitParser(File circuitFile, Charset charset){
		this.circuitFile = circuitFile;
		this.charset = charset;
	}

	/**
	 * @return A list of gates in the given circuitFile
	 */
	public List<Gate> getParsedGates() {
		boolean counter = false;
		ArrayList<Gate> res = new ArrayList<Gate>();
		try {
			BufferedReader fbr = new BufferedReader(new InputStreamReader(
					new FileInputStream(circuitFile), charset));
			String line = "";
			while((line = fbr.readLine()) != null) {
				if (line.isEmpty()){
					continue;
				}

				/*
				 * Ignore meta-data info, we don't need it
				 */
				if(line.matches("[0-9]* [0-9]*")){
					firstHeader = line;
					
					counter = true;
					continue;
				}

				/*
				 * Parse number of input bits
				 */
				if (counter == true){
					secondHeader = line;
					String[] split = line.split(" ");
					numberOfAliceInputs = Integer.parseInt(split[0]);
					int numberOfBobInputs = Integer.parseInt(split[1]);
					totalNumberOfInputs = numberOfAliceInputs +
							numberOfBobInputs;
					counter = false;
					continue;
				}

				/*
				 * Parse each gate line and count numberOfNonXORGates
				 */
				Gate g = new GateAugment(line);
				if (!g.isXOR()){
					g.setGateNumber(numberOfNonXORGates);
					numberOfNonXORGates++;
				}
				res.add(g);
			}
			fbr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}
	
	public String[] getHeaders(int sizeOfCircuit, int nonXORGatesAdded){
		String[] res = new String[2];
		
		String[] split = firstHeader.split(" ");
		int numberOfNonXORGates = Integer.parseInt(split[1]);
		int totalNumberOfNonXORGates = numberOfNonXORGates+ nonXORGatesAdded;
		
		res[0] = sizeOfCircuit + " " +  totalNumberOfNonXORGates;
		
		String[] inputOutputInfo = secondHeader.split(" ");
		int newAliceInput = Integer.parseInt(inputOutputInfo[0]) *2;
		int newBobInput = Integer.parseInt(inputOutputInfo[1]) *2;
		int newOutput = Integer.parseInt(inputOutputInfo[1]) *2;
		
		res[1] = newAliceInput + " " + newBobInput + " " + inputOutputInfo[4] + " " +
		newOutput;
		
		return res;
		
	}
	
	public int getNumberOfInputs(){
		return totalNumberOfInputs;
	}
	
	public int getNumberOfAliceInputs(){
		return numberOfAliceInputs;
	}
	
	public int getNumberOfNonXORGates(){
		String[] split = firstHeader.split(" ");
		return Integer.parseInt(split[1]);
	}

}
