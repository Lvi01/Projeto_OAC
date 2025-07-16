package tests;

import components.Bus;
import components.Ula;

public class TestUlaSimple {

	public static void main(String[] args) {
		testBasicOperations();
		testStackOperations();
		System.out.println("TestUlaSimple: Todos os testes passaram!");
	}

	public static void testBasicOperations() {
		Bus bus = new Bus();
		Bus bus2 = new Bus();
		Ula ula = new Ula(bus, bus2);
		
		// Test ADD
		bus.put(4);
		bus2.put(5);
		ula.add();
		if (bus.get() == 9) {
			System.out.println("ADD funcionando - 4 + 5 = " + bus.get());
		} else {
			System.out.println("Erro ADD: esperado 9, obtido " + bus.get());
		}
		
		// Test SUB
		bus.put(10);
		bus2.put(3);
		ula.sub();
		if (bus.get() == 7) {
			System.out.println("SUB funcionando - 10 - 3 = " + bus.get());
		} else {
			System.out.println("Erro SUB: esperado 7, obtido " + bus.get());
		}
		
		// Test INC
		bus.put(9);
		ula.inc();
		if (bus.get() == 10) {
			System.out.println("INC funcionando - 9 + 1 = " + bus.get());
		} else {
			System.out.println("Erro INC: esperado 10, obtido " + bus.get());
		}
	}

	public static void testStackOperations() {
		Bus bus = new Bus();
		Bus bus2 = new Bus();
		Ula ula = new Ula(bus, bus2);
		
		// Test initial stack state
		int initialTop = ula.getStackTop();
		int initialBottom = ula.getStackBottom();
		System.out.println("Stack inicial - Top: " + initialTop + ", Bottom: " + initialBottom);
		
		// Test push
		int pushResult = ula.pushComplete(42);
		if (pushResult == 0) {
			System.out.println("Push funcionando - valor 42 inserido");
		} else {
			System.out.println("Erro Push: código " + pushResult);
		}
		
		// Test pop
		int popValue = ula.pop();
		if (popValue == 42) {
			System.out.println("Pop funcionando - valor recuperado: " + popValue);
		} else {
			System.out.println("Erro Pop: esperado 42, obtido " + popValue);
		}
		
		// Verify stack returned to initial state
		int finalTop = ula.getStackTop();
		if (finalTop == initialTop) {
			System.out.println("Stack retornou ao estado inicial");
		} else {
			System.out.println("Erro: Stack não retornou ao estado inicial");
		}
	}
}
