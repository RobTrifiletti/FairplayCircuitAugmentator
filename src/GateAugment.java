
public class GateAugment implements Gate {

	private static final int numberOfInputWires = 2;

	private int counter;
	private int time;
	private int leftWireIndex;
	private int rightWireIndex;
	private int outputWireIndex;
	private String gate;
	private int gateNumber;
	private int inputWires;
	private int outputWires;

	public GateAugment(String s){

		//Example string: 2 1 96 99 256 0110 
		String[] split = s.split(" ");
		counter = numberOfInputWires;
		time = -1;
		inputWires = Integer.parseInt(split[0]);
		outputWires = Integer.parseInt(split[1]);
		leftWireIndex = Integer.parseInt(split[2]);
		rightWireIndex = Integer.parseInt(split[3]);
		outputWireIndex = Integer.parseInt(split[4]);

		gate = split[5].replaceFirst("^0*", ""); //Removes leading 0's
		gateNumber = -1;
	}

	/* (non-Javadoc)
	 * @see Gate#getLeftWireIndex()
	 */
	@Override
	public int getLeftWireIndex(){
		return leftWireIndex;
	}

	/* (non-Javadoc)
	 * @see Gate#getRightWireIndex()
	 */
	@Override
	public int getRightWireIndex(){
		return rightWireIndex;
	}

	/* (non-Javadoc)
	 * @see Gate#getOutputWireIndex()
	 */
	@Override
	public int getOutputWireIndex(){
		return outputWireIndex;
	}

	/* (non-Javadoc)
	 * @see Gate#getCounter()
	 */
	@Override
	public int getCounter(){
		return counter;
	}

	/* (non-Javadoc)
	 * @see Gate#decCounter()
	 */
	@Override
	public void decCounter(){
		counter--;
	}

	/* (non-Javadoc)
	 * @see Gate#getTime()
	 */
	@Override
	public int getTime(){
		return time;
	}

	/* (non-Javadoc)
	 * @see Gate#setTime(int)
	 */
	@Override
	public void setTime(int time){
		this.time = Math.max(this.time, time);
	}

	/* (non-Javadoc)
	 * @see Gate#getGate()
	 */
	@Override
	public String getGate(){
		return gate;
	}

	/* (non-Javadoc)
	 * @see Gate#toString()
	 */
	@Override
	public String toString(){
		return inputWires + " " +  outputWires + " " + getLeftWireIndex() + " " + getRightWireIndex() +
				" " + getOutputWireIndex() + " " + getGate();
	}

	/* (non-Javadoc)
	 * @see Gate#isXOR()
	 */
	@Override
	public boolean isXOR(){
		if (gate.matches("110")){
			return true;
		}
		else return false;
	}

	/* (non-Javadoc)
	 * @see Gate#setGateNumber(int)
	 */
	@Override
	public void setGateNumber(int gateNumber){
		this.gateNumber = gateNumber;
	}

	/* (non-Javadoc)
	 * @see Gate#getGateNumber()
	 */
	@Override
	public int getGateNumber(){
		return gateNumber;
	}

	@Override
	public void setLeftWireIndex(int index) {
		leftWireIndex = index;
		
	}

	@Override
	public void setRightWireIndex(int index) {
		rightWireIndex = index;
	}

	@Override
	public void setOutputWireIndex(int index) {
		outputWireIndex = index;
		
	}
}
