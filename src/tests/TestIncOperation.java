package tests;

import architecture.Architecture;

public class TestIncOperation {

	public static void main(String[] args) {
		testUlaIncrement();
	}

	public static void testUlaIncrement() {
		System.out.println("=== TESTE ULA INCREMENT ===");
		Architecture arch = new Architecture();
		
		// Testar operação básica de incremento na ULA
		System.out.println("Colocando 5 no extbus1 e armazenando no REG0");
		arch.getExtbus1().put(5);
		arch.getREG0().store();
		
		System.out.println("Lendo REG0 para intbus2");
		arch.getREG0().internalRead();
		System.out.println("Valor no intbus2: " + arch.getIntbus2().get());
		
		System.out.println("Armazenando valor na ULA posição 1");
		arch.getUla().internalStore(1);
		
		System.out.println("Executando incremento na ULA");
		arch.getUla().inc();
		
		System.out.println("Lendo resultado da ULA para intbus2");
		arch.getUla().internalRead(1);
		System.out.println("Resultado no intbus2: " + arch.getIntbus2().get());
		
		System.out.println("Armazenando resultado no REG0");
		arch.getREG0().internalStore();
		
		System.out.println("Lendo REG0 final");
		arch.getREG0().read();
		int result = arch.getExtbus1().get();
		System.out.println("REG0 final: " + result);
		
		if (result == 6) {
			System.out.println("OK - Incremento funcionou corretamente");
		} else {
			System.out.println("ERRO - Esperado 6, obtido " + result);
		}
	}
}
