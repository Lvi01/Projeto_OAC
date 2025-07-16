package tests;

import components.Register;
import components.Bus;

public class TestRegisterSimple {

	public static void main(String[] args) {
		testBasic();
		System.out.println("TestRegisterSimple: Teste básico passou!");
	}
	
	public static void testBasic() {
		Bus bus = new Bus();
		Register reg = new Register(8, bus);
		
		// Teste básico
		bus.put(10);
		reg.store();
		reg.read();
		if (bus.get() == 10) {
			System.out.println("Register funcionando corretamente");
		} else {
			System.out.println("Erro: esperado 10, obtido " + bus.get());
		}
	}
}
