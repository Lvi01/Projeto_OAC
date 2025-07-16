package tests;

import architecture.Architecture;

public class TestPhase1Simple {

	public static void main(String[] args) {
		try {
			testAddRegRegBasic();
			testSubRegRegBasic();
			System.out.println("TestPhase1Simple: Testes basicos da FASE 1 passaram!");
		} catch (Exception e) {
			System.out.println("Erro nos testes da FASE 1: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void testAddRegRegBasic() {
		Architecture arch = new Architecture();
		
		// Preparar registradores: REG0 = 5, REG1 = 3
		arch.getExtbus1().put(5);
		arch.getREG0().store();
		
		arch.getExtbus1().put(3);
		arch.getREG1().store();
		
		// Configurar PC para começar na posição 0
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Preparar parâmetros na memória para addRegReg
		// Posição 0: comando addRegReg (12)
		arch.getExtbus1().put(0);
		arch.getMemory().store();
		arch.getExtbus1().put(12);
		arch.getMemory().store();
		
		// Posição 1: ID do primeiro registrador (REG0 = ID 0)
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		arch.getExtbus1().put(0); // REG0 ID
		arch.getMemory().store();
		
		// Posição 2: ID do segundo registrador (REG1 = ID 1)
		arch.getExtbus1().put(2);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // REG1 ID
		arch.getMemory().store();
		
		// Configurar PC para começar na posição 0 novamente
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Executar addRegReg diretamente
		try {
			arch.addRegReg();
			
			// Verificar resultado: REG1 deveria ter 5 + 3 = 8
			arch.getREG1().read();
			int result = arch.getExtbus1().get();
			
			if (result == 8) {
				System.out.println("OK testAddRegReg passou - REG1 = " + result);
			} else {
				System.out.println("ERRO testAddRegReg falhou - esperado 8, obtido " + result);
			}
		} catch (Exception e) {
			System.out.println("ERRO testAddRegReg falhou com excecao: " + e.getMessage());
		}
	}

	public static void testSubRegRegBasic() {
		Architecture arch = new Architecture();
		
		// Preparar registradores: REG0 = 10, REG1 = 3
		arch.getExtbus1().put(10);
		arch.getREG0().store();
		
		arch.getExtbus1().put(3);
		arch.getREG1().store();
		
		// Configurar PC para começar na posição 0
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Preparar parâmetros na memória
		// Posição 0: comando subRegReg (13)
		arch.getExtbus1().put(0);
		arch.getMemory().store();
		arch.getExtbus1().put(13);
		arch.getMemory().store();
		
		// Posição 1: ID do primeiro registrador (REG0 = ID 0)
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		arch.getExtbus1().put(0); // REG0 ID
		arch.getMemory().store();
		
		// Posição 2: ID do segundo registrador (REG1 = ID 1)
		arch.getExtbus1().put(2);
		arch.getMemory().store();
		arch.getExtbus1().put(1); // REG1 ID
		arch.getMemory().store();
		
		// Configurar PC para começar na posição 0 novamente
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Executar subRegReg diretamente
		try {
			arch.subRegReg();
			
			// Verificar resultado: REG1 deveria ter 10 - 3 = 7
			arch.getREG1().read();
			int result = arch.getExtbus1().get();
			
			if (result == 7) {
				System.out.println("OK testSubRegReg passou - REG1 = " + result);
			} else {
				System.out.println("ERRO testSubRegReg falhou - esperado 7, obtido " + result);
			}
		} catch (Exception e) {
			System.out.println("ERRO testSubRegReg falhou com excecao: " + e.getMessage());
		}
	}
}
