import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class CircuitAugmentator implements Runnable {

	private File outputFile;
	private Charset charset;
	private CircuitParser circuitParser;
	private int numberOfNonXORGatesAdded;
	private int largestOutputGate;
	private List<Gate> outputGates;

	public CircuitAugmentator(File circuitFile, File outputFile){
		this.outputFile = outputFile;
		charset = Charset.defaultCharset();
		circuitParser = new CircuitParser(circuitFile, charset);
		outputGates = new ArrayList<Gate>();
		numberOfNonXORGatesAdded = 0;
		largestOutputGate = 0;
	}

	@Override
	public void run() {
		List<Gate> parsedGates = circuitParser.getParsedGates();
		int numberOfInputs = circuitParser.getNumberOfInputs(); //256
		List<Gate> augGates = 
				getAugGates(numberOfInputs);
		
		List<Gate> augCircuit = new ArrayList<Gate>();
		List<Gate> incrementedGates = getIncrementedGates(parsedGates, numberOfInputs);
		List<Gate> augOutputGates = getOutputGates();//Must be called after getIncrementedGates()
		augCircuit.addAll(augGates);
		augCircuit.addAll(incrementedGates);
		augCircuit.addAll(augOutputGates);
		
		writeOutput(augCircuit);
	}

	private List<Gate> getAugGates(int numberOfStandardInputs) {

		List<Gate> res = new ArrayList<Gate>();
		int t_a = circuitParser.getNumberOfAliceInputs();
		int totalInputSize = numberOfStandardInputs * 2;
		int gateNumber = 0;
		int uptoAndIncludingFirstAugInput = totalInputSize - t_a;
		List<Gate> andGates = new ArrayList<Gate>();

		/**
		 * Add all the AND gates. We assume r is the 3rd input
		 */
		for(int s = uptoAndIncludingFirstAugInput; s < totalInputSize; s++){
			for(int i = 0; i < t_a; i++){
				int shift = s - uptoAndIncludingFirstAugInput;
				int leftWire = i;
				int rightWire = (shift + i + numberOfStandardInputs) % 
						uptoAndIncludingFirstAugInput;
				if (rightWire < numberOfStandardInputs){
					rightWire += numberOfStandardInputs;
				}
				int outputWire = shift * t_a + (i + totalInputSize);

				Gate g = new GateAugment("2 1 "+ leftWire + " " + rightWire +
						" " + outputWire + " 0001");
				g.setGateNumber(gateNumber);
				gateNumber++;
				andGates.add(g);
			}
			res.addAll(andGates);
			numberOfNonXORGatesAdded += andGates.size();
			andGates.clear();
		}

		/**
		 * Add all the XOR gates.
		 */
		List<Gate> xorGates = new ArrayList<Gate>();
		//How far we filled up the res array with and gates
		int xorGateStart = res.size() + totalInputSize;
		
		for (int s = uptoAndIncludingFirstAugInput; s < totalInputSize; s++){
			int multCounter = s - uptoAndIncludingFirstAugInput;
			int priorOutputWire = 0;
			int numberOfXORs = t_a - 1;
			for (int i = 0; i < numberOfXORs; i++){
				int leftWire;
				int rightWire;
				if (i == 0){
					leftWire = totalInputSize + t_a * multCounter;
					rightWire = totalInputSize + t_a * multCounter + 1;
				}
				else{
					leftWire = priorOutputWire;
					rightWire = totalInputSize + t_a * multCounter + 1 + i;
				}
				int outputWire = xorGateStart + 1 + i + 
						(s - uptoAndIncludingFirstAugInput) * numberOfXORs;
				priorOutputWire = outputWire;

				// We make each xor dependant on the following xor, thus
				// creating a chain structure. The last gate in this list is the
				// output gate.
				Gate g = new GateAugment("2 1 " + leftWire + " " +
						rightWire + " " + outputWire + " 0110");
				xorGates.add(g);
			}

			//We identify the last xor-gate of the chain and xor this
			//with the current s bit. The output of this xor is the s'th
			//output bit of the augmentation output.
			Gate xorOutputGate = xorGates.get(xorGates.size() - 1);
			int xorOutputWireIndex = xorOutputGate.getOutputWireIndex();

			Gate outputGate = new GateAugment("2 1 " + xorOutputWireIndex +" " +
					s + " " + multCounter + " 0110");
			
			outputGates.add(outputGate);
			res.addAll(xorGates);
			xorGates.clear();
		}

		return res;

	}
	
	private List<Gate> getIncrementedGates(List<Gate> parsedGates, 
			int numberOfInputs){
		List<Gate> res = new ArrayList<Gate>();
		int numberOfAddedGates = parsedGates.size();
		
		for(Gate g: parsedGates){
			if(!g.isXOR()){
				g.setGateNumber(g.getGateNumber() + numberOfNonXORGatesAdded);
			}
			if(g.getLeftWireIndex() > numberOfInputs - 1){
				int newIndex = g.getLeftWireIndex() + numberOfAddedGates;
				g.setLeftWireIndex(newIndex);
			}
			if(g.getRightWireIndex() > numberOfInputs - 1){
				int newIndex = g.getRightWireIndex() + numberOfAddedGates;
				g.setRightWireIndex(newIndex);
			}
			if(g.getOutputWireIndex() > numberOfInputs - 1){
				int newIndex = g.getOutputWireIndex() + numberOfAddedGates;
				g.setOutputWireIndex(newIndex);
			}
			largestOutputGate = Math.max(largestOutputGate, g.getOutputWireIndex());
			res.add(g);
		}
		return res;
	}

	/**
	 * @param layersOfGates
	 * Writes the given lists of lists to a file
	 */
	private void writeOutput(List<Gate> augCircuit) {
		BufferedWriter fbw = null;
		try {
			fbw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile), charset));
			String[] headers = circuitParser.getHeaders(augCircuit.size(),
					numberOfNonXORGatesAdded);
			fbw.write(headers[0]);
			fbw.newLine();
			fbw.write(headers[1]);
			fbw.newLine();
			fbw.newLine();

			for(Gate g: augCircuit){
				fbw.write(g.toString());
				fbw.newLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally { 
			try {
				fbw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private List<Gate> getOutputGates(){
		int startIndex = largestOutputGate + 1;
		for(Gate g: outputGates){
			g.setOutputWireIndex(startIndex);
			startIndex++;
		}
		return outputGates;
	}
}

