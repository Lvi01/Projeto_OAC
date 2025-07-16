package tests;

import components.Memory;
import components.Bus;

public class TestMemory {

	public static void main(String[] args) {
		testReadWrite();
		testSetAddress();
		System.out.println("TestMemory: Todos os testes passaram!");
	}
	
	public static void testReadWrite() {
		Bus bus = new Bus();
		Memory memory = new Memory(16, bus); //creates a 16 positions memory filled with zeros, attached to the bus
		bus.put(-1);
		assert bus.get() == -1 : "Erro: esperado -1, obtido " + bus.get();
		for (int i=0;i<16;i++) {
			bus.put(i);
			memory.read();
			assert bus.get() == 0 : "Erro: esperado 0, obtido " + bus.get(); //checks if all positions were initialized with zeroes
		}
		//now, inserting numbers into the memory
		for (int i=0;i<16;i++) {
			bus.put(i);
			memory.store(); //the position is defined
			memory.store(); //storing in each position a number equals its address
		}
		//testing if the numbers into the memory are the ones we just inserted
		for (int i=0;i<16;i++) {
			bus.put(i);
			memory.read();
			assert bus.get() == i : "Erro: esperado " + i + ", obtido " + bus.get(); //the value is equals to the position
		}
		//all positions being equals to the square of the position
		for (int i=0;i<16;i++) {
			bus.put(i);
			memory.store(); //setting the position
			bus.put(i*i);
			memory.store(); //storing the data
		}
		//testing if the numbers into the memory are the ones we just inserted
		for (int i=0;i<16;i++) {
			bus.put(i);
			memory.read();
			assert bus.get() == i*i : "Erro: esperado " + (i*i) + ", obtido " + bus.get(); //the value is equals to the 2nd power of the position
		}
		
		//trying to access addresses out of the memory range makes no effect into the bus
		bus.put(-5);
		memory.read();
		assert bus.get() == -5 : "Erro: esperado -5, obtido " + bus.get();
		memory.store();
		assert bus.get() == -5 : "Erro: esperado -5, obtido " + bus.get();
		
		bus.put(30);
		memory.read();
		assert bus.get() == 30 : "Erro: esperado 30, obtido " + bus.get();
		memory.store();
		assert bus.get() == 30 : "Erro: esperado 30, obtido " + bus.get();
	}

	public static void testSetAddress() {
		Bus bus = new Bus();
		Memory memory = new Memory(16, bus); //creates a 16 positions memory filled with zeros, attached to the bus
		//storing data using bus for address
		for (int i=0;i<16;i++) {
			bus.put(i);
			memory.store(); //set the address
			bus.put(i*i*i); //storing the cubic value in each position
			memory.store(); //store the data
		}
		//testing if the cubic values are being stored
		for (int i=0;i<16;i++) {
			bus.put(i);
			memory.read();
			assert bus.get() == i*i*i : "Erro: esperado " + (i*i*i) + ", obtido " + bus.get();
		}
	}
}
