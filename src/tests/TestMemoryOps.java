package tests;

import architecture.Architecture;

public class TestMemoryOps {

	public static void main(String[] args) {
		testMemoryReadWrite();
	}

	public static void testMemoryReadWrite() {
		System.out.println("=== TESTE MEMORY READ/WRITE ===");
		Architecture arch = new Architecture();
		
		// Teste 1: Configurar memoria[10] = 5
		System.out.println("1. Configurando memoria[10] = 5");
		arch.getExtbus1().put(10);
		arch.getMemory().store();
		arch.getExtbus1().put(5);
		arch.getMemory().store();
		
		// Verificar se foi configurado
		arch.getExtbus1().put(10);
		arch.getMemory().read();
		arch.getMemory().read();
		System.out.println("Valor lido de memoria[10]: " + arch.getExtbus1().get());
		
		// Teste 2: Incrementar e escrever de volta
		System.out.println("\n2. Testando escrita de novo valor");
		int newValue = 7;
		arch.getExtbus1().put(10);
		arch.getMemory().store(); // definir endereço
		arch.getExtbus1().put(newValue);
		arch.getMemory().store(); // escrever valor
		
		// Verificar se foi escrito
		arch.getExtbus1().put(10);
		arch.getMemory().read();
		arch.getMemory().read();
		int result = arch.getExtbus1().get();
		System.out.println("Novo valor em memoria[10]: " + result);
		
		if (result == 7) {
			System.out.println("OK - Operações de memoria funcionam");
		} else {
			System.out.println("ERRO - Esperado 7, obtido " + result);
		}
	}
}
