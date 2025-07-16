package tests;

import architecture.Architecture;

public class TestMemoryBasic {

	public static void main(String[] args) {
		testMemoryInitialization();
	}

	public static void testMemoryInitialization() {
		System.out.println("=== TESTE MEMORY BASIC ===");
		Architecture arch = new Architecture();
		
		// Verificar se podemos ler/escrever diretamente na memória
		System.out.println("Tamanho da memoria: " + arch.getMemorySize());
		
		// Tentar acessar memoria diretamente através do objeto Memory
		System.out.println("Testando acesso direto à memoria...");
		
		// Configurar diretamente nos dados da memória
		arch.getMemory().getDataList()[10] = 5;
		System.out.println("Configurado memoria[10] = 5 diretamente");
		
		// Ler usando extbus
		arch.getExtbus1().put(10);
		arch.getMemory().read();
		arch.getMemory().read();
		int value = arch.getExtbus1().get();
		System.out.println("Lido da memoria[10]: " + value);
		
		// Tentar incrementar para 6
		arch.getMemory().getDataList()[10] = 6;
		System.out.println("Configurado memoria[10] = 6 diretamente");
		
		// Ler novamente
		arch.getExtbus1().put(10);
		arch.getMemory().read();
		arch.getMemory().read();
		int newValue = arch.getExtbus1().get();
		System.out.println("Novo valor memoria[10]: " + newValue);
		
		if (newValue == 6) {
			System.out.println("OK - Acesso direto funciona");
		} else {
			System.out.println("ERRO - Acesso direto falhou");
		}
	}
}
