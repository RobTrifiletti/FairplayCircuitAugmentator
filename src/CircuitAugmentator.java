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
	private final int augInputLength = 2;
	private int numberOfNonXORGatesAdded;
	private int numberOfNonOutputGatesAdded;

	public CircuitAugmentator(File circuitFile, File outputFile){
		this.outputFile = outputFile;
		charset = Charset.defaultCharset();
		circuitParser = new CircuitParser(circuitFile, charset);
		numberOfNonXORGatesAdded = 0;
		numberOfNonOutputGatesAdded = 0;
	}

	@Override
	public void run() {
		List<Gate> parsedGates = circuitParser.getParsedGates();
		int numberOfGates = circuitParser.getNumberOfNonXORGates();
		List<Gate> augGates = 
				getAugGates(circuitParser.getNumberOfInputs(), numberOfGates);
		int numberOfAddedGates = augGates.size();
		
		
		for(Gate g: parsedGates){
			if(!g.isXOR()){
				g.setGateNumber(g.getGateNumber() + numberOfNonXORGatesAdded);
			}
			g.setLeftWireIndex(g.getLeftWireIndex() + numberOfAddedGates);
			g.setRightWireIndex(g.getRightWireIndex() + numberOfAddedGates);
			g.setOutputWireIndex(g.getOutputWireIndex() + numberOfAddedGates);
		}
		List<Gate> augCircuit = new ArrayList<Gate>();
		augCircuit.addAll(parsedGates);
		augCircuit.addAll(augGates);

		writeOutput(augCircuit);
	}

	private List<Gate> getAugGates(int numberOfStandardInputs, int numberOfGates) {

		List<Gate> res = new ArrayList<Gate>();
		int t_a = circuitParser.getNumberOfAliceInputs();
		int totalInputSize = numberOfStandardInputs * augInputLength;
		int gateNumber = 0;
		int uptoAndIncludingFirstAugInput = totalInputSize - t_a;
		List<Gate> andGates = new ArrayList<Gate>();

		for(int s = uptoAndIncludingFirstAugInput; s < totalInputSize; s++){

			/**
			 * Add all the AND gates. We assume r is the 3rd input
			 */
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
				g.setGateNumber(gateNumber++);
				andGates.add(g);
			}
			res.addAll(andGates);
			numberOfNonXORGatesAdded += andGates.size();
			numberOfNonOutputGatesAdded += andGates.size();
			andGates.clear();
		}
//		for(Gate g: andGates){
//			System.out.println(g);
//			if(andGates.indexOf(g) > 512){
//				return null;
//			}
//		}



		/**
		 * Add all the XOR gates.
		 */
		List<Gate> xorGates = new ArrayList<Gate>();
		int xorGateStart = res.size() + totalInputSize;
		
		for (int s = uptoAndIncludingFirstAugInput; s <totalInputSize; s++){
			int multCounter = s - uptoAndIncludingFirstAugInput;
			
			int priorOutputWire = 0;
			int numberOfXORs = (t_a - 1);
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
				// creating a tree structure. The last gate in this list is the
				// output gate.
				Gate g = new GateAugment("2 1 " + leftWire + " " +
						rightWire + " " + outputWire + " 0110");
				xorGates.add(g);
			}

			Gate xorOutputGate = xorGates.get(xorGates.size() - 1);
			int xorOutputWireIndex = xorOutputGate.getOutputWireIndex();


			Gate outputGate = new GateAugment("2 1 " + xorOutputWireIndex +" " +
					s + " " + numberOfGates++ + " 0110");

			numberOfNonOutputGatesAdded += xorGates.size(); 
			res.addAll(xorGates);
			xorGates.clear();
			res.add(outputGate);
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

			/*
			 * Write the gates the the file, one layer at a time
			 */
			for(Gate g: augCircuit){

				// Write the gates in this layer
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
}

