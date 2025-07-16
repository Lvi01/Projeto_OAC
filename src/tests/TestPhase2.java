package tests;

import architecture.Architecture;

public class TestPhase2 {

	public static void main(String[] args) {
		try {
			testAddRegMem();
			testSubRegMem();
			System.out.println("\n=== TODOS OS TESTES DA FASE 2 PASSARAM! ===");
		} catch (Exception e) {
			System.out.println("Erro nos testes da FASE 2: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void testAddRegMem() {
		Architecture arch = new Architecture();
		
		// Preparar registrador: REG1 = 10
		arch.getExtbus1().put(10);
		arch.getREG1().store();
		
		// Preparar memoria: posicao 20 com valor 5
		arch.getMemory().getDataList()[20] = 5;
		
		// Configurar PC para começar na posição 0
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Preparar parâmetros na memória para addRegMem
		// Posição 0: comando addRegMem (16)
		arch.getExtbus1().put(0);
		arch.getMemory().store();
		arch.getExtbus1().put(16);
		arch.getMemory().store();
		
		// Posição 1: ID do registrador (REG1 = ID 1)
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // REG1 ID
		arch.getMemory().store();
		
		// Posição 2: endereco da memoria (20)
		arch.getExtbus1().put(2);
		arch.getMemory().store();
		arch.getExtbus1().put(20); // memoria[20]
		arch.getMemory().store();
		
		// Configurar PC para começar na posição 0 novamente
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Executar addRegMem diretamente
		try {
			arch.addRegMem();
			
			// Verificar resultado: REG1 deveria ter 10 + 5 = 15
			arch.getREG1().read();
			int result = arch.getExtbus1().get();
			
			if (result == 15) {
				System.out.println("OK testAddRegMem: REG1 = " + result + " (10 + 5)");
			} else {
				System.out.println("ERRO testAddRegMem: esperado 15, obtido " + result);
			}
		} catch (Exception e) {
			System.out.println("ERRO testAddRegMem: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void testSubRegMem() {
		Architecture arch = new Architecture();
		
		// Preparar registrador: REG2 = 20
		arch.getExtbus1().put(20);
		arch.getREG2().store();
		
		// Preparar memoria: posicao 25 com valor 7
		arch.getMemory().getDataList()[25] = 7;
		
		// Configurar PC para começar na posição 0
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Preparar parâmetros na memória para subRegMem
		// Posição 0: comando subRegMem (17)
		arch.getExtbus1().put(0);
		arch.getMemory().store();
		arch.getExtbus1().put(17);
		arch.getMemory().store();
		
		// Posição 1: ID do registrador (REG2 = ID 2)
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		arch.getExtbus1().put(2); // REG2 ID
		arch.getMemory().store();
		
		// Posição 2: endereco da memoria (25)
		arch.getExtbus1().put(2);
		arch.getMemory().store();
		arch.getExtbus1().put(25); // memoria[25]
		arch.getMemory().store();
		
		// Configurar PC para começar na posição 0 novamente
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Executar subRegMem diretamente
		try {
			arch.subRegMem();
			
			// Verificar resultado: REG2 deveria ter 20 - 7 = 13
			arch.getREG2().read();
			int result = arch.getExtbus1().get();
			
			if (result == 13) {
				System.out.println("OK testSubRegMem: REG2 = " + result + " (20 - 7)");
			} else {
				System.out.println("ERRO testSubRegMem: esperado 13, obtido " + result);
			}
		} catch (Exception e) {
			System.out.println("ERRO testSubRegMem: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
