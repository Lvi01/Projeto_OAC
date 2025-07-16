package architecture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;



import components.Bus;
import components.Demux;
import components.Memory;
import components.Register;
import components.Ula;

public class Architecture {
	
	private boolean simulation; //this boolean indicates if the execution is done in simulation mode.
								//simulation mode shows the components' status after each instruction
	
	
	private boolean halt;
	private Bus extbus1;
	private Bus intbus1;
	private Bus intbus2;
	private Memory memory;
	private Memory statusMemory;
	private int memorySize;
	private Register PC;
	private Register IR;
	private Register REG0;
	private Register REG1;
	private Register REG2;
	private Register REG3;
	private Register Flags;
	private Ula ula;
	private Demux demux; //only for multiple register purposes
	
	private ArrayList<String> commandsList;
	private ArrayList<Register> registersList;
	
	

	/**
	 * Instanciates all components in this architecture
	 */
	private void componentsInstances() {
		//don't forget the instantiation order
		//buses -> registers -> ula -> memory
		extbus1 = new Bus();
		intbus1 = new Bus();
		intbus2 = new Bus();
		PC = new Register("PC", extbus1, intbus1);
		IR = new Register("IR", extbus1, intbus1);
		REG0 = new Register("REG0", extbus1, intbus2);
		REG1 = new Register("REG1", extbus1, intbus2);
		REG2 = new Register("REG2", extbus1, intbus2);
		REG3 = new Register("REG3", extbus1, intbus2);
		Flags = new Register(2, intbus2);
		fillRegistersList();
		ula = new Ula(intbus1, intbus2);
		statusMemory = new Memory(2, extbus1);
		memorySize = 128;
		memory = new Memory(memorySize, extbus1);
		demux = new Demux(); //this bus is used only for multiple register operations
		
		fillCommandsList();
	}

	/**
	 * This method fills the registers list inserting into them all the registers we have.
	 * IMPORTANT!
	 * The first register to be inserted must be the default REG0
	 */
	private void fillRegistersList() {
		registersList = new ArrayList<Register>();
		registersList.add(REG0);
		registersList.add(REG1);
		registersList.add(REG2);
		registersList.add(REG3);
		registersList.add(PC);
		registersList.add(IR);
		registersList.add(Flags);
	}

	/**
	 * Constructor that instanciates all components according the architecture diagram
	 */
	public Architecture() {
		componentsInstances();
		
		//by default, the execution method is never simulation mode
		simulation = false;
	}

	
	public Architecture(boolean sim) {
		componentsInstances();
		
		//in this constructor we can set the simoualtion mode on or off
		simulation = sim;
	}



	//getters
	
	public Bus getExtbus1() {
		return extbus1;
	}

	public Bus getIntbus1() {
		return intbus1;
	}

	public Bus getIntbus2() {
		return intbus2;
	}

	public Memory getMemory() {
		return memory;
	}

	public Register getPC() {
		return PC;
	}

	public Register getIR() {
		return IR;
	}

	public Register getREG0() {
		return REG0;
	}

	public Register getREG1() {
		return REG1;
	}

	public Register getREG2() {
		return REG2;
	}

	public Register getREG3() {
		return REG3;
	}

	public Register getFlags() {
		return Flags;
	}

	public Ula getUla() {
		return ula;
	}

	public ArrayList<String> getCommandsList() {
		return commandsList;
	}
	
	// ==================== STACK ACCESS METHODS ====================
	
	/**
	 * Push a value onto the stack
	 * @param value the value to push
	 */
	public void stackPush(int value) {
		int stackAddress = ula.pushComplete(value); // Get the address where to store
		extbus1.put(stackAddress); // Set memory address
		memory.store(); // First call: store the address as target position
		extbus1.put(value); // Set value to store
		memory.store(); // Second call: store the value at the target position
	}
	
	/**
	 * Pop a value from the stack
	 * @return the address where the popped value was stored
	 */
	public int stackPop() {
		return ula.pop();
	}
	
	/**
	 * Peek at the top of the stack without removing
	 * @return the address of the top stack element
	 */
	public int stackPeek() {
		return ula.peek();
	}
	
	/**
	 * Get the current stack top pointer value
	 * @return current stack top address
	 */
	public int getStackTop() {
		return ula.getStackTop();
	}
	
	/**
	 * Get the stack bottom pointer value  
	 * @return stack bottom address
	 */
	public int getStackBottom() {
		return ula.getStackBottom();
	}
	
	/**
	 * Check if the stack is empty
	 * @return true if stack is empty
	 */
	public boolean isStackEmpty() {
		return ula.isStackEmpty();
	}
	
	/**
	 * Check if the stack is full
	 * @return true if stack is full
	 */
	public boolean isStackFull() {
		return ula.isStackFull();
	}
	
	/**
	 * Reset the stack to empty state
	 */
	public void resetStack() {
		ula.resetStack();
	}



	//all the microprograms must be impemented here
	//the instructions table is
	/*
	 *
			add addr (reg0 <- reg0 + addr)
			sub addr (reg0 <- reg0 - addr)
			jmp addr (pc <- addr)
			jz addr  (se bitZero pc <- addr)
			jn addr  (se bitneg pc <- addr)
			read addr (reg0 <- addr)
			store addr  (addr <- reg0)
			ldi x    (reg0 <- x. x must be an integer)
			inc    (reg0++)
			move regA regB (regA <- regB)
	 */
	
	/**
	 * This method fills the commands list arraylist with all commands used in this architecture
	 */
	protected void fillCommandsList() {
		commandsList = new ArrayList<String>();
		commandsList.add("add");   //0
		commandsList.add("sub");   //1
		commandsList.add("jmp");   //2
		commandsList.add("jz");    //3
		commandsList.add("jn");    //4
		commandsList.add("read");  //5
		commandsList.add("store"); //6
		commandsList.add("ldi");   //7
		commandsList.add("inc");   //8		
		commandsList.add("moveRegReg"); //9
		commandsList.add("call");  //10
		commandsList.add("ret");   //11
	}

	
	/**
	 * This method is used after some ULA operations, setting the flags bits according the result.
	 * @param result is the result of the operation
	 * NOT TESTED!!!!!!!
	 */
	private void setStatusFlags(int result) {
		Flags.setBit(0, 0);
		Flags.setBit(1, 0);
		if (result==0) { //bit 0 in flags must be 1 in this case
			Flags.setBit(0,1);
		}
		if (result<0) { //bit 1 in flags must be 1 in this case
			Flags.setBit(1,1);
		}
	}

	/**
	 * This method implements the microprogram for
	 * 					ADD address
	 * In the machine language this command number is 0, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture
	 * The method reads the value from memory (position address) and 
	 * performs an add with this value and that one stored in the RPG (the first register in the register list).
	 * The final result must be in RPG (the first register in the register list).
	 * The logic is
	 * 1. pc -> intbus1 //pc.internalRead()
	 * 2. ula <-  intbus1 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus1 //ula.read()
	 * 5. pc <- intbus1 //pc.internalStore() now pc points to the parameter
	 * 6. reg0 -> intbus2 //reg0.internalRead() the current reg0 value must go to the ula 
	 * 7. ula <- intbus2 //ula.internalStore()
	 * 8. pc -> extbus (pc.read())
	 * 9. memory reads from extbus //this forces memory to write the data position in the extbus
	 * 10. memory reads from extbus //this forces memory to write the data value in the extbus
	 * 11. reg0 <- extbus (reg0.store())
	 * 12. reg0 -> intbus2 (reg0.internalRead())
	 * 13. ula  <- intbus2 //ula.internalStore()
	 * 14. Flags <- zero //the status flags are reset
	 * 15. ula adds
	 * 16. ula -> intbus2 //ula.internalRead()
	 * 17. ChangeFlags //informations about flags are set according the result 
	 * 18. reg0 <- intbus2 //reg0.internalStore() - the add is complete.
	 * 19. pc -> intbus1 //pc.internalRead() now pc must point the next instruction address
	 * 20. ula <- intbus1 //ula.store()
	 * 21. ula incs
	 * 22. ula -> intbus1 //ula.internalRead()
	 * 23. pc <- intbus1 //pc.internalStore() 
	 * end
	 * @param address
	 */
	public void add() {
		PC.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the parameter address
		REG0.internalRead();
		ula.internalStore(0); //the reg0 value is in ULA (0). This is the first parameter
		PC.read(); 
		memory.read(); // the parameter is now in the external bus. 
						//but the parameter is an address and we need the value
		memory.read(); //now the value is in the external bus
		REG0.store();
		REG0.internalRead();
		ula.internalStore(1); //the reg0 value is in ULA (1). This is the second parameter 
		ula.add(); //the result is in the second ula's internal register
		ula.internalRead(1);; //the operation result is in the internalbus 2
		setStatusFlags(intbus2.get()); //changing flags due the end of the operation
		REG0.internalStore(); //now the add is complete
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	

	/**
	 * This method implements the microprogram for
	 * 					SUB address
	 * In the machine language this command number is 1, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture
	 * The method reads the value from memory (position address) and 
	 * performs an SUB with this value and that one stored in the rpg (the first register in the register list).
	 * The final result must be in RPG (the first register in the register list).
	 * The logic is
	 * 1. pc -> intbus1 //pc.internalRead()
	 * 2. ula <-  intbus1 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus1 //ula.read()
	 * 5. pc <- intbus1 //pc.internalStore() now pc points to the parameter
	 * 6. reg0 -> intbus2 //reg0.internalRead() the current reg0 value must go to the ula 
	 * 7. ula <- intbus2 //ula.internalStore()
	 * 8. pc -> extbus (pc.read())
	 * 9. memory reads from extbus //this forces memory to write the data position in the extbus
	 * 10. memory reads from extbus //this forces memory to write the data value in the extbus
	 * 11. reg0 <- extbus (reg0.store())
	 * 12. reg0 -> intbus2 (reg0.internalRead())
	 * 13. ula  <- intbus2 //ula.internalStore()
	 * 14. Flags <- zero //the status flags are reset
	 * 15. ula subs
	 * 16. ula -> intbus2 //ula.internalRead()
	 * 17. ChangeFlags //informations about flags are set according the result 
	 * 18. reg0 <- intbus2 //reg0.internalStore() - the sub is complete.
	 * 19. pc -> intbus1 //pc.internalRead() now pc must point the next instruction address
	 * 20. ula <- intbus1 //ula.store()
	 * 21. ula incs
	 * 22. ula -> intbus1 //ula.read()
	 * 23. pc <- intbus1 //pc.internalStore() 
	 * end
	 * @param address
	 */
	public void sub() {
		PC.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the parameter address
		REG0.internalRead();
		ula.internalStore(0); //the reg0 value is in ULA (0). This is the first parameter
		PC.read(); 
		memory.read(); // the parameter is now in the external bus. 
						//but the parameter is an address and we need the value
		memory.read(); //now the value is in the external bus
		REG0.store();
		REG0.internalRead();
		ula.internalStore(1); //the reg0 value is in ULA (1). This is the second parameter
		ula.sub(); //the result is in the second ula's internal register
		ula.internalRead(1);; //the operation result is in the internalbus 2
		setStatusFlags(intbus2.get()); //changing flags due the end of the operation
		REG0.internalStore(); //now the sub is complete
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	/**
	 * This method implements the microprogram for
	 * 					JMP address
	 * In the machine language this command number is 2, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture (where the PC is redirecto to)
	 * The method reads the value from memory (position address) and 
	 * inserts it into the PC register.
	 * So, the program is deviated
	 * The logic is
	 * 1. pc -> intbus1 //pc.internalRead()
	 * 2. ula <-  intbus1 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus1 //ula.read()
	 * 5. pc <- intbus1 //pc.internalStore() now pc points to the parameter
	 * 6. pc -> extbus //pc.read()
	 * 7. memory reads from extbus //this forces memory to write the data position in the extbus
	 * 8. pc <- extbus //pc.store() //pc was pointing to another part of the memory
	 * end
	 * @param address
	 */
	public void jmp() {
		PC.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read();
		memory.read();
		PC.store();
	}
	
	/**
	 * This method implements the microprogram for
	 * 					JZ address
	 * In the machine language this command number is 3, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture (where 
	 * the PC is redirected to, but only in the case the ZERO bit in Flags is 1)
	 * The method reads the value from memory (position address) and 
	 * inserts it into the PC register if the ZERO bit in Flags register is setted.
	 * So, the program is deviated conditionally
	 * The logic is
	 * 1. pc -> intbus1 //pc.internalRead()
	 * 2. ula <-  intbus1 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus1 //ula.read()
	 * 5. pc <- intbus1 //pc.internalStore() now pc points to the parameter
	 * 6. pc -> extbus1 //pc.read() now the parameter address is in the extbus1
	 * 7. Memory -> extbus1 //memory.read() the address (if jz) is in external bus 1
	 * 8. statusMemory(1)<- extbus1 // statusMemory.storeIn1()
	 * 9. ula incs
	 * 10. ula -> intbus1 //ula.read()
	 * 11. PC <- intbus1 // PC.internalStore() PC is now pointing to next instruction
	 * 12. PC -> extbus1 // PC.read() the next instruction address is in the extbus
	 * 13. statusMemory(0)<- extbus1 // statusMemory.storeIn0()
	 * 14. Flags(bitZero) -> extbus1 //the ZERO bit is in the external bus
	 * 15. statusMemory <- extbus // the status memory returns the correct address according the ZERO bit
	 * 16. PC <- extbus1 // PC stores the new address where the program is redirected to
	 * end
	 * @param address
	 */
	public void jz() {
		PC.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore();//now PC points to the parameter address
		PC.read();
		memory.read();// now the parameter value (address of the jz) is in the external bus
		statusMemory.storeIn1(); //the address is in position 1 of the status memory
		ula.inc();
		ula.read(1);
		PC.internalStore();//now PC points to the next instruction
		PC.read();//now the bus has the next istruction address
		statusMemory.storeIn0(); //the address is in the position 0 of the status memory
		extbus1.put(Flags.getBit(0)); //the ZERO bit is in the external bus 
		statusMemory.read(); //gets the correct address (next instruction or parameter address)
		PC.store(); //stores into PC
	}
	
	/**
	 * This method implements the microprogram for
	 * 					jn address
	 * In the machine language this command number is 4, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture (where 
	 * the PC is redirected to, but only in the case the NEGATIVE bit in Flags is 1)
	 * The method reads the value from memory (position address) and 
	 * inserts it into the PC register if the NEG bit in Flags register is setted.
	 * So, the program is deviated conditionally
	 * The logic is
	 * 1. pc -> intbus1 //pc.internalRead()
	 * 2. ula <-  intbus1 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus1 //ula.read()
	 * 5. pc <- intbus1 //pc.internalStore() now pc points to the parameter
	 * 6. pc -> extbus1 //pc.read() now the parameter address is in the extbus1
	 * 7. Memory -> extbus1 //memory.read() the address (if jn) is in external bus 1
	 * 8. statusMemory(1)<- extbus1 // statusMemory.storeIn1()
	 * 9. ula incs
	 * 10. ula -> intbus1 //ula.read()
	 * 11. PC <- intbus1 // PC.internalStore() PC is now pointing to next instruction
	 * 12. PC -> extbus1 // PC.read() the next instruction address is in the extbus
	 * 13. statusMemory(0)<- extbus1 // statusMemory.storeIn0()
	 * 14. Flags(bitNEGATIVE) -> extbus1 //the NEGATIVE bit is in the external bus
	 * 15. statusMemory <- extbus // the status memory returns the correct address according the NEG bit
	 * 16. PC <- extbus1 // PC stores the new address where the program is redirected to
	 * end
	 * @param address
	 */
	public void jn() {
		PC.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore();//now PC points to the parameter address
		PC.read();
		memory.read();// now the parameter value (address of the jn) is in the external bus
		statusMemory.storeIn1(); //the address is in position 1 of the status memory
		ula.inc();
		ula.read(1);
		PC.internalStore();//now PC points to the next instruction
		PC.read();//now the bus has the next istruction address
		statusMemory.storeIn0(); //the address is in the position 0 of the status memory
		extbus1.put(Flags.getBit(1)); //the NEGATIVE bit is in the external bus 
		statusMemory.read(); //gets the correct address (next instruction or parameter address)
		PC.store(); //stores into PC
	}
	
	/**
	 * This method implements the microprogram for
	 * 					read address
	 * In the machine language this command number is 5, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture 
	 * The method reads the value from memory (position address) and 
	 * inserts it into the RPG register (the first register in the register list)
	 * The logic is
	 * 1. pc -> intbus1 //pc.internalRead()
	 * 2. ula <-  intbus1 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus1 //ula.read()
	 * 5. pc <- intbus1 //pc.internalStore() now pc points to the parameter
	 * 6. pc -> extbus //(pc.read())the address where is the position to be read is now in the external bus 
	 * 7. memory reads from extbus //this forces memory to write the address in the extbus
	 * 8. memory reads from extbus //this forces memory to write the stored data in the extbus
	 * 9. REG0 <- extbus //the data is read
	 * 10. pc -> intbus1 //pc.internalRead() now pc must point the next instruction address
	 * 11. ula <- intbus1 //ula.store()
	 * 12. ula incs
	 * 13. ula -> intbus1 //ula.read()
	 * 14. pc <- intbus1 //pc.internalStore() 
	 * end
	 * @param address
	 */
	public void read() {
		PC.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read(); 
		memory.read(); // the address is now in the external bus.
		memory.read(); // the data is now in the external bus.
		REG0.store();
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	/**
	 * This method implements the microprogram for
	 * 					store address
	 * In the machine language this command number is 6, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture 
	 * The method reads the value from RPG (the first register in the register list) and 
	 * inserts it into the memory (position address) 
	 * The logic is
	 * 1. pc -> intbus1 //pc.internalRead()
	 * 2. ula <-  intbus1 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus1 //ula.read()
	 * 5. pc <- intbus1 //pc.internalStore() now pc points to the parameter
	 * 6. pc -> extbus //(pc.read())the parameter address is the external bus
	 * 7. memory reads // memory reads the data in the parameter address. 
	 * 					// this data is the address where the REG0 value must be stores 
	 * 8. memory stores //memory reads the address and wait for the value
	 * 9. REG0 -> Externalbus //REG0.read()
	 * 10. memory stores //memory receives the value and stores it
	 * 11. pc -> intbus1 //pc.internalRead() now pc must point the next instruction address
	 * 12. ula <- intbus1 //ula.store()
	 * 13. ula incs
	 * 14. ula -> intbus1 //ula.read()
	 * 15. pc <- intbus1 //pc.internalStore() 
	 * end
	 * @param address
	 */
	public void store() {
		PC.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read(); 
		memory.read();   //the parameter address (pointing to the addres where data must be stored
		                 //is now in externalbus1
		memory.store(); //the address is in the memory. Now we must to send the data
		REG0.read();
		memory.store(); //the data is now stored
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	/**
	 * This method implements the microprogram for
	 * 					ldi immediate
	 * In the machine language this command number is 7, and the immediate value
	 *        is in the position next to him
	 *    
	 * The method moves the value (parameter) into the REG0 
	 * (the first register in the register list) 
	 * The logic is
	 * 1. pc -> intbus1 //pc.internalRead()
	 * 2. ula <-  intbus1 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus1 //ula.read()
	 * 5. pc <- intbus1 //pc.internalStore() now pc points to the parameter
	 * 6. pc -> extbus //(pc.read())the address where is the position to be read is now in the external bus 
	 * 7. memory reads from extbus //this forces memory to write the stored data in the extbus
	 * 8. REG0 <- extbus //REG0.store()
	 * 9. pc -> intbus1 //pc.internalRead() now pc must point the next instruction address
	 * 10. ula <- intbus1 //ula.store()
	 * 11. ula incs
	 * 12. ula -> intbus1 //ula.read()
	 * 13. pc <- intbus1 //pc.internalStore() 
	 * end
	 * @param address
	 */
	public void ldi() {
		PC.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the parameter address
		PC.read(); 
		memory.read(); // the immediate is now in the external bus.
		REG0.store();   //REG0 receives the immediate
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	/**
	 * This method implements the microprogram for
	 * 					inc 
	 * In the machine language this command number is 8
	 *    
	 * The method moves the value in reg0 (the first register in the register list)
	 *  into the ula and performs an inc method
	 * 		-> inc works just like add reg0 (the first register in the register list)
	 *         with the number 1 stored into the memory
	 * 		-> however, inc consumes lower amount of cycles  
	 * 
	 * The logic is
	 * 
	 * 1. reg0 -> intbus2 //reg0.internalRead()
	 * 2. ula  <- intbus2 //ula.internalStore()
	 * 3. Flags <- zero //the status flags are reset
	 * 4. ula incs
	 * 5. ula -> intbus2 //ula.internalRead()
	 * 6. ChangeFlags //informations about flags are set according the result
	 * 7. reg0 <- intbus2 //reg0.internalStore()
	 * 8. pc -> intbus1 //pc.internalRead() now pc must point the next instruction address
	 * 9. ula <- intbus1 //ula.store()
	 * 10. ula incs
	 * 11. ula -> intbus1 //ula.read()
	 * 12. pc <- intbus1 //pc.internalStore()
	 * end
	 * @param address
	 */
	public void inc() {
		REG0.internalRead();
		ula.internalStore(1);
		ula.inc();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		REG0.internalStore();
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	/**
	 * This method implements the microprogram for
	 * 					move <reg1> <reg2> 
	 * In the machine language this command number is 9
	 *    
	 * The method reads the two register ids (<reg1> and <reg2>) from the memory, in positions just after the command, and
	 * copies the value from the <reg1> register to the <reg2> register
	 * 
	 * 1. pc -> intbus1 //pc.internalRead()
	 * 2. ula <-  intbus1 //ula.store()
	 * 3. ula incs
	 * 4. ula -> intbus1 //ula.read()
	 * 5. pc <- intbus1 //pc.internalStore() now pc points to the first parameter
	 * 6. pc -> extbus //(pc.read())the address where is the position to be read is now in the external bus 
	 * 7. memory reads from extbus //this forces memory to write the parameter (first regID) in the extbus
	 * 8. pc -> intbus1 //pc.internalRead() //getting the second parameter
	 * 9. ula <-  intbus1 //ula.store()
	 * 10. ula incs
	 * 11. ula -> intbus1 //ula.read()
	 * 12. pc <- intbus1 //pc.internalStore() now pc points to the second parameter
	 * 13. demux <- extbus //now the register to be operated is selected
	 * 14. registers -> intbus2 //this performs the internal reading of the selected register 
	 * 15. PC -> extbus (pc.read())the address where is the position to be read is now in the external bus 
	 * 16. memory reads from extbus //this forces memory to write the parameter (second regID) in the extbus
	 * 17. demux <- extbus //now the register to be operated is selected
	 * 18. registers <- intbus2 //this performs the internal store of the register identified in the extbus
	 * 19. pc -> intbus1 //pc.internalRead() now pc must point the next instruction address
	 * 20. ula <- intbus1 //ula.store()
	 * 21. ula incs
	 * 22. ula -> intbus1 //ula.read()
	 * 23. pc <- intbus1 //pc.internalStore()  
	 * 		  
	 */
	public void moveRegReg() {
		PC.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the first parameter (the first reg id)
		PC.read(); 
		memory.read(); // the first register id is now in the external bus.
		PC.internalRead();
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the second parameter (the second reg id)
		demux.setValue(extbus1.get()); //points to the correct register
		registersInternalRead(); //starts the read from the register identified into demux bus
		PC.read();
		memory.read(); // the second register id is now in the external bus.
		demux.setValue(extbus1.get());//points to the correct register
		registersInternalStore(); //performs an internal store for the register identified into demux bus
		PC.internalRead(); //we need to make PC points to the next instruction address
		ula.store(1);
		ula.inc();
		ula.read(1);
		PC.internalStore(); //now PC points to the next instruction. We go back to the FETCH status.
	}
	
	/**
	 * This method implements the microprogram for
	 * 					CALL address
	 * In the machine language this command number is 10, and the address is in the position next to him
	 *    
	 * where address is a valid position in this memory architecture (where the subroutine starts)
	 * The method:
	 * 1. Calculates the return address (PC + 2, pointing to instruction after CALL)
	 * 2. Pushes the return address onto the stack
	 * 3. Jumps to the specified address (like JMP)
	 * 
	 * The logic is:
	 * 1. pc -> intbus1 //pc.internalRead() - get current PC
	 * 2. ula <- intbus1 //ula.store() - store PC in ULA
	 * 3. ula incs //increment to point to parameter
	 * 4. ula -> intbus1 //ula.read() - get PC+1
	 * 5. pc <- intbus1 //pc.internalStore() - now PC points to parameter
	 * 6. ula incs //increment again to get return address (PC+2)
	 * 7. ula -> intbus1 //ula.read() - get PC+2 (return address)
	 * 8. push return address onto stack
	 * 9. pc -> extbus //pc.read() - get parameter address
	 * 10. memory reads from extbus //get the target address
	 * 11. pc <- extbus //pc.store() - jump to target address
	 * end
	 */
	public void call() {
		// Step 1: Calculate return address (PC + 2)
		PC.internalRead(); // Get current PC into intbus1
		int currentPC = intbus1.get(); // Save current PC value
		int returnAddress = currentPC + 2; // Calculate return address directly
		
		// Step 2: Get target address (it's already in extbus1)
		int targetAddress = extbus1.get(); // Save target address from extbus1
		
		// Step 3: Push return address onto stack
		stackPush(returnAddress); // Save return address to stack
		
		// Step 4: Jump to target address
		extbus1.put(targetAddress); // Put target address back in extbus1
		PC.store(); // Set PC to target address
	}
	
	/**
	 * This method implements the microprogram for
	 * 					RET 
	 * In the machine language this command number is 11 (no parameters)
	 *    
	 * The method:
	 * 1. Pops the return address from the stack
	 * 2. Sets PC to the return address
	 * 3. Continues execution from the return address
	 * 
	 * The logic is:
	 * 1. Pop return address from stack
	 * 2. Get the value from memory at the popped address
	 * 3. Store it in PC
	 * end
	 */
	public void ret() {
		// Pop return address from stack - this gives us the address where the value is stored
		int returnAddressLocation = stackPop(); // Get address of top stack element
		
		// Read the return address value from memory
		extbus1.put(returnAddressLocation); // Put the address on external bus
		memory.read(); // Read from memory at that address - puts value in extbus1
		
		// Set PC to the return address (value is now in extbus1)
		PC.store(); // Store the return address in PC
	}
	
	
	public ArrayList<Register> getRegistersList() {
		return registersList;
	}

	/**
	 * This method performs an (external) read from a register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersRead() {
		registersList.get(demux.getValue()).read();
	}
	
	/**
	 * This method performs an (internal) read from a register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersInternalRead() {
		registersList.get(demux.getValue()).internalRead();;
	}
	
	/**
	 * This method performs an (external) store toa register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersStore() {
		registersList.get(demux.getValue()).store();
	}
	
	/**
	 * This method performs an (internal) store toa register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersInternalStore() {
		registersList.get(demux.getValue()).internalStore();;
	}



	/**
	 * This method reads an entire file in machine code and
	 * stores it into the memory
	 * NOT TESTED
	 * @param filename
	 * @throws IOException 
	 */
	public void readExec(String filename) throws IOException {
		   BufferedReader br = new BufferedReader(new		 
		   FileReader(filename+".dxf"));
		   String linha;
		   int i=0;
		   while ((linha = br.readLine()) != null) {
			     extbus1.put(i);
			     memory.store();
			   	 extbus1.put(Integer.parseInt(linha));
			     memory.store();
			     i++;
			}
			br.close();
	}
	
	/**
	 * This method executes a program that is stored in the memory
	 */
	public void controlUnitEexec() {
		halt = false;
		while (!halt) {
			fetch();
			decodeExecute();
		}

	}
	

	/**
	 * This method implements The decode proccess,
	 * that is to find the correct operation do be executed
	 * according the command.
	 * And the execute proccess, that is the execution itself of the command
	 */
	private void decodeExecute() {
		IR.internalRead(); //the instruction is in the intbus1
		int command = intbus1.get();
		simulationDecodeExecuteBefore(command);
		switch (command) {
		case 0:
			add();
			break;
		case 1:
			sub();
			break;
		case 2:
			jmp();
			break;
		case 3:
			jz();
			break;
		case 4:
			jn();
			break;
		case 5:
			read();
			break;
		case 6:
			store();
			break;
		case 7:
			ldi();
			break;
		case 8:
			inc();
			break;
		case 9:
			moveRegReg();
			break;
		case 10:
			call();
			break;
		case 11:
			ret();
			break;
		default:
			halt = true;
			break;
		}
		if (simulation)
			simulationDecodeExecuteAfter();
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED
	 * @param command 
	 */
	private void simulationDecodeExecuteBefore(int command) {
		System.out.println("----------BEFORE Decode and Execute phases--------------");
		String instruction;
		int parameter = 0;
		for (Register r:registersList) {
			System.out.println(r.getRegisterName()+": "+r.getData());
		}
		if (command !=-1)
			instruction = commandsList.get(command);
		else
			instruction = "END";
		if (hasOperands(instruction)) {
			parameter = memory.getDataList()[PC.getData()+1];
			System.out.println("Instruction: "+instruction+" "+parameter);
		}
		else
			System.out.println("Instruction: "+instruction);
		if ("read".equals(instruction))
			System.out.println("memory["+parameter+"]="+memory.getDataList()[parameter]);
		
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED 
	 */
	private void simulationDecodeExecuteAfter() {
		String instruction;
		System.out.println("-----------AFTER Decode and Execute phases--------------");
		System.out.println("Internal Bus 1: "+intbus1.get());
		System.out.println("Internal Bus 2: "+intbus2.get());
		System.out.println("External Bus 1: "+extbus1.get());
		for (Register r:registersList) {
			System.out.println(r.getRegisterName()+": "+r.getData());
		}
		Scanner entrada = new Scanner(System.in);
		System.out.println("Press <Enter>");
		String mensagem = entrada.nextLine();
	}

	/**
	 * This method uses PC to find, in the memory,
	 * the command code that must be executed.
	 * This command must be stored in IR
	 * NOT TESTED!
	 */
	private void fetch() {
		PC.read();
		memory.read();
		IR.store();
		simulationFetch();
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED!!!!!!!!!
	 */
	private void simulationFetch() {
		if (simulation) {
			System.out.println("-------Fetch Phase------");
			System.out.println("PC: "+PC.getData());
			System.out.println("IR: "+IR.getData());
		}
	}

	/**
	 * This method is used to show in a correct way the operands (if there is any) of instruction,
	 * when in simulation mode
	 * NOT TESTED!!!!!
	 * @param instruction 
	 * @return
	 */
	private boolean hasOperands(String instruction) {
		if ("inc".equals(instruction) || "ret".equals(instruction)) //inc and ret are instructions having no operands
			return false;
		else
			return true;
	}

	/**
	 * This method returns the amount of positions allowed in the memory
	 * of this architecture
	 * NOT TESTED!!!!!!!
	 * @return
	 */
	public int getMemorySize() {
		return memorySize;
	}
	
	public static void main(String[] args) throws IOException {
		Architecture arch = new Architecture(true);
		arch.readExec("program");
		arch.controlUnitEexec();
	}
	

}
