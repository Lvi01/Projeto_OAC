package tests;

import components.Bus;
import components.Ula;

public class TestUla {

	public static void main(String[] args) {
		testInternalExternalStoreRead();
		testSum();
		testMoveReg();
		testInc();
		testStackOperations();
		System.out.println("TestUla: Todos os testes passaram!");
	}

	public static void testInternalExternalStoreRead() {
		Bus bus = new Bus();
		Bus bus2 = new Bus();
		Ula ula = new Ula(bus, bus2);
		
		// testing externalStore and internalRead
		bus.put(-1);
		ula.internalRead();
		assert bus.get() == 0 : "Erro: após internalRead, bus deveria ter 0, obtido " + bus.get();
		
		// testing various operations
		for (int i = 0; i < 10; i++) {
			bus.put(3);
			bus2.put(6);
			ula.externalStore();
			ula.internalRead();
			if (i % 2 == 0) {
				assert bus.get() == 6 : "Erro: esperado 6, obtido " + bus.get();
			} else {
				assert bus.get() == 3 : "Erro: esperado 3, obtido " + bus.get();
			}
		}
		
		// testing internalStore and externalRead
		for (int i = 0; i < 10; i++) {
			bus.put(3);
			bus2.put(6);
			ula.internalStore();
			ula.externalRead();
			if (i % 2 == 0) {
				assert bus2.get() == 6 : "Erro: esperado 6, obtido " + bus2.get();
			} else {
				assert bus2.get() == 3 : "Erro: esperado 3, obtido " + bus2.get();
			}
		}
	}

	public static void testSum() {
		Bus bus = new Bus();
		Bus bus2 = new Bus();
		Ula ula = new Ula(bus, bus2);
		
		// test 4 + 5 = 9
		bus.put(4);
		bus2.put(5);
		ula.sum();
		assert bus.get() == 9 : "Erro: 4 + 5 deveria ser 9, obtido " + bus.get();
		
		// test 3 + (-6) = -3
		bus.put(3);
		bus2.put(-6);
		ula.sum();
		assert bus.get() == -3 : "Erro: 3 + (-6) deveria ser -3, obtido " + bus.get();
		
		// test 0 + 0 = 0
		bus.put(0);
		bus2.put(0);
		ula.sum();
		assert bus.get() == 0 : "Erro: 0 + 0 deveria ser 0, obtido " + bus.get();
	}

	public static void testMoveReg() {
		Bus bus = new Bus();
		Bus bus2 = new Bus();
		Ula ula = new Ula(bus, bus2);
		
		// test moving positive value
		bus.put(3);
		bus2.put(999);
		ula.moveReg();
		assert bus.get() == 3 : "Erro: moveReg deveria manter 3, obtido " + bus.get();
		
		// test moving negative value
		bus.put(-4);
		bus2.put(999);
		ula.moveReg();
		assert bus.get() == -4 : "Erro: moveReg deveria manter -4, obtido " + bus.get();
		
		// test moving zero
		bus.put(0);
		bus2.put(999);
		ula.moveReg();
		assert bus.get() == 0 : "Erro: moveReg deveria manter 0, obtido " + bus.get();
	}

	public static void testInc() {
		Bus bus = new Bus();
		Bus bus2 = new Bus();
		Ula ula = new Ula(bus, bus2);
		
		// test increment positive
		bus.put(9);
		bus2.put(999);
		ula.inc();
		assert bus.get() == 10 : "Erro: incremento de 9 deveria ser 10, obtido " + bus.get();
		
		// test increment negative
		bus.put(-10);
		bus2.put(999);
		ula.inc();
		assert bus.get() == -9 : "Erro: incremento de -10 deveria ser -9, obtido " + bus.get();
	}

	public static void testStackOperations() {
		Bus bus = new Bus();
		Bus bus2 = new Bus();
		Ula ula = new Ula(bus, bus2);
		
		// Test stack initialization
		ula.getStkTOP().read();
		assert bus.get() == 100 : "Erro: StkTOP inicial deveria ser 100, obtido " + bus.get();
		
		ula.getStkBOT().read();
		assert bus.get() == 127 : "Erro: StkBOT inicial deveria ser 127, obtido " + bus.get();
		
		// Test push operation
		bus.put(42);
		ula.push();
		
		// Verify stack pointer moved
		ula.getStkTOP().read();
		assert bus.get() == 101 : "Erro: após push, StkTOP deveria ser 101, obtido " + bus.get();
		
		// Test pop operation
		ula.pop();
		
		// Verify value popped and stack pointer moved back
		assert bus.get() == 42 : "Erro: valor recuperado deveria ser 42, obtido " + bus.get();
		
		ula.getStkTOP().read();
		assert bus.get() == 100 : "Erro: após pop, StkTOP deveria ser 100, obtido " + bus.get();
	}
}
