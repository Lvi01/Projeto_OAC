package components;

public class Ula {
	
	private Bus intBus;
	private Bus extBus1;
	private Bus extBus2;
	private Register reg1;
	private Register reg2;
	
	// Stack components for Architecture C
	private Register stkTOP;  // Stack Top Pointer
	private Register stkBOT;  // Stack Bottom Pointer
	
	
	public Ula(Bus extBus1, Bus extBus2) {
		super();
		this.extBus1 = extBus1;
		this.extBus2 = extBus2;
		intBus = new Bus();
		reg1 = new Register("UlaReg0", extBus1, intBus);
		reg2 = new Register("UlaReg1", extBus1, intBus);
		
		// Initialize stack components
		stkTOP = new Register("StkTOP", extBus1, extBus2);
		stkBOT = new Register("StkBOT", extBus1, extBus2);
		
		// Initialize stack pointers - assuming stack grows downward
		// StkBOT points to the highest address, StkTOP to current top
		initializeStack();
	}
	
	/**
	 * Initialize the stack pointers
	 * Assuming the stack occupies memory addresses from 100 to 127 (last 28 positions)
	 */
	private void initializeStack() {
		// Stack bottom points to the lowest address (100)
		extBus1.put(100);
		stkBOT.store();
		
		// Stack top initially points to the highest address + 1 (empty stack)
		extBus1.put(127);
		stkTOP.store();
	}

	/**
	 * This method adds the reg1 and reg2 values, storing the result in reg2.
	 */
	public void add() {
		int res=0;
		intBus.put(0);
		reg1.internalRead(); //puts its data into the internal bus
		res = intBus.get(); //stored for operation
		reg2.internalRead(); //puts the internal data into the internal bus
		res += intBus.get(); //the operation was performed
		intBus.put(res);
		reg2.internalStore(); //saves the result into internal store
	}
	
	/**
	 * This method sub the reg2 value from reg1 value, storing the result in reg2
	 * This processing uses a Ula's internal bus
	 */
	public void sub() {
				
		int res=0;
		intBus.put(0);
		reg1.internalRead(); //puts its data into the internal bus
		res = intBus.get(); //stored for operation
		reg2.internalRead(); //puts the internal data into the internal bus
		res -= intBus.get(); //the operation was performed
		intBus.put(res);
		reg2.internalStore(); //saves the result into internal store
		
	}
	
	/**
	 * This method increments by 1 the value stored into reg2
	 */
	public void inc() {
		
		reg2.internalRead();
		int res = intBus.get();
		res ++;
		intBus.put(res);
		reg2.internalStore();
		
	}
	
	/**
	 * This method stores the value found in the external bus into the #reg
	 * @param reg
	 */
	public void store(int reg) {
		if (reg==0)
			reg1.store();
		else
			reg2.store();
	}
	
	/**
	 * This method reads the value from #reg stores it into the external bus
	 * @param reg
	 */
	public void read (int reg) {
		if (reg==0)
			reg1.read();
		else
			reg2.read();
	}
	
	/**
	 * This method stores the value found in the internal bus into the #reg
	 * @param reg
	 */
	public void internalStore(int reg) {
		extBus1.put(extBus2.get()); //moving the data from a bus to another
		//inserting the data in the correct register
		if (reg==0)
			reg1.store();
		else
			reg2.store();
	}
	
	/**
	 * This method reads the value from #reg stores it into the internal bus
	 * @param reg
	 */
	public void internalRead (int reg) {
		if (reg==0)
			reg1.read();
		else
			reg2.read();
		extBus2.put(extBus1.get()); //moving the data from a bus to another
	}
	
	// ==================== STACK OPERATIONS ====================
	
	/**
	 * Push a value onto the stack
	 * The value to be pushed must be in the external bus
	 * Stack grows downward (TOP decreases)
	 */
	public void push() {
		// Get current stack top
		stkTOP.read();
		int currentTop = extBus1.get();
		
		// Check for stack overflow (if TOP reaches BOT-1, stack is full)
		stkBOT.read();
		int stackBottom = extBus1.get();
		
		if (currentTop <= stackBottom) { // Stack has 28 positions (100-127)
			throw new RuntimeException("Stack Overflow: Cannot push, stack is full");
		}
		
		// Decrement stack top (stack grows downward)
		currentTop--;
		extBus1.put(currentTop);
		stkTOP.store();
		
		// The value to be pushed should already be in extBus1
		// This method assumes the calling code has put the value there
	}
	
	/**
	 * Push a specific value onto the stack
	 * @param value the value to push
	 */
	public void pushValue(int value) {
		// Get current stack top
		stkTOP.read();
		int currentTop = extBus1.get();
		
		// Check for stack overflow
		stkBOT.read();
		int stackBottom = extBus1.get();
		
		if (currentTop <= stackBottom) { // Stack has 28 positions
			throw new RuntimeException("Stack Overflow: Cannot push, stack is full");
		}
		
		// Decrement stack top (stack grows downward)
		currentTop--;
		extBus1.put(currentTop);
		stkTOP.store();
		
		// The value to be pushed should be handled by the caller
		// This method only updates the stack pointer
	}
	
	/**
	 * Complete push operation - updates stack pointer and returns the address where value should be stored
	 * @param value the value to push
	 * @return the memory address where the value should be stored
	 */
	public int pushComplete(int value) {
		// Get current stack top
		stkTOP.read();
		int currentTop = extBus1.get();
		
		// Check for stack overflow
		stkBOT.read();
		int stackBottom = extBus1.get();
		
		if (currentTop <= stackBottom) { // Stack has 28 positions
			throw new RuntimeException("Stack Overflow: Cannot push, stack is full");
		}
		
		// Decrement stack top (stack grows downward)
		currentTop--;
		extBus1.put(currentTop);
		stkTOP.store();
		
		// Return the address where the value should be stored
		return currentTop;
	}
	
	/**
	 * Pop a value from the stack
	 * The popped value will be available in the external bus
	 * @return the popped value
	 */
	public int pop() {
		// Get current stack top
		stkTOP.read();
		int currentTop = extBus1.get();
		
		// Check for stack underflow (empty stack when currentTop > 127)
		if (currentTop > 127) {
			throw new RuntimeException("Stack Underflow: Cannot pop, stack is empty");
		}
		
		// The value at current top should be read by memory operations
		// Increment stack top (removing the top element)
		currentTop++;
		extBus1.put(currentTop);
		stkTOP.store();
		
		// Return the address where the value was stored
		return currentTop - 1;
	}
	
	/**
	 * Peek at the top value of the stack without removing it
	 * @return the address of the top stack element
	 */
	public int peek() {
		// Get current stack top
		stkTOP.read();
		int currentTop = extBus1.get();
		
		// Check if stack is empty
		if (currentTop > 127) {
			throw new RuntimeException("Stack is empty: Cannot peek");
		}
		
		return currentTop;
	}
	
	/**
	 * Check if the stack is empty
	 * @return true if stack is empty, false otherwise
	 */
	public boolean isStackEmpty() {
		stkTOP.read();
		int currentTop = extBus1.get();
		
		return currentTop > 127; // Empty when stack pointer is above 127
	}
	
	/**
	 * Check if the stack is full
	 * @return true if stack is full, false otherwise
	 */
	public boolean isStackFull() {
		stkTOP.read();
		int currentTop = extBus1.get();
		
		stkBOT.read();
		int stackBottom = extBus1.get();
		
		return currentTop <= stackBottom; // Full when stack pointer reaches bottom
	}
	
	/**
	 * Get the current stack top pointer value
	 * @return current top pointer
	 */
	public int getStackTop() {
		stkTOP.read();
		return extBus1.get();
	}
	
	/**
	 * Get the stack bottom pointer value
	 * @return stack bottom pointer
	 */
	public int getStackBottom() {
		stkBOT.read();
		return extBus1.get();
	}
	
	/**
	 * Reset the stack to empty state
	 */
	public void resetStack() {
		// Reset stack top to 127 (empty state)
		extBus1.put(127);
		stkTOP.store();
	}
	
	
}
