package tests;

import components.Bus;

public class TestBus {

	public static void main(String[] args) {
		testPutGet();
		System.out.println("TestBus: Todos os testes passaram!");
	}
	
	public static void testPutGet() {
		Bus bus = new Bus();
		assert bus.get() == 0 : "Erro: esperado 0, obtido " + bus.get();
		bus.put(1);
		assert bus.get() == 1 : "Erro: esperado 1, obtido " + bus.get();
		bus.put(2);
		assert bus.get() == 2 : "Erro: esperado 2, obtido " + bus.get();
	}

}
