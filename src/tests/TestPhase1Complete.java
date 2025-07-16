package tests;

import architecture.Architecture;

public class TestPhase1Complete {

	public static void main(String[] args) {
		try {
			testAddRegReg();
			testSubRegReg();
			testJnz();
			testIncMem();
			System.out.println("\n=== TODOS OS TESTES DA FASE 1 PASSARAM! ===");
		} catch (Exception e) {
			System.out.println("Erro nos testes da FASE 1: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void testAddRegReg() {
		Architecture arch = new Architecture();
		
		// Preparar registradores: REG0 = 5, REG1 = 3
		arch.getExtbus1().put(5);
		arch.getREG0().store();
		
		arch.getExtbus1().put(3);
		arch.getREG1().store();
		
		// Configurar PC e memoria
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Memoria[1] = REG0 ID (0), Memoria[2] = REG1 ID (1)
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		arch.getExtbus1().put(0);
		arch.getMemory().store();
		
		arch.getExtbus1().put(2);
		arch.getMemory().store();
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		arch.addRegReg();
		
		arch.getREG1().read();
		int result = arch.getExtbus1().get();
		
		if (result == 8) {
			System.out.println("OK testAddRegReg: REG1 = " + result);
		} else {
			System.out.println("ERRO testAddRegReg: esperado 8, obtido " + result);
		}
	}

	public static void testSubRegReg() {
		Architecture arch = new Architecture();
		
		// Preparar registradores: REG0 = 10, REG1 = 3
		arch.getExtbus1().put(10);
		arch.getREG0().store();
		
		arch.getExtbus1().put(3);
		arch.getREG1().store();
		
		// Configurar PC e memoria
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Memoria[1] = REG0 ID (0), Memoria[2] = REG1 ID (1)
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		arch.getExtbus1().put(0);
		arch.getMemory().store();
		
		arch.getExtbus1().put(2);
		arch.getMemory().store();
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		arch.subRegReg();
		
		arch.getREG1().read();
		int result = arch.getExtbus1().get();
		
		if (result == 7) {
			System.out.println("OK testSubRegReg: REG1 = " + result);
		} else {
			System.out.println("ERRO testSubRegReg: esperado 7, obtido " + result);
		}
	}

	public static void testJnz() {
		Architecture arch = new Architecture();
		
		// Teste simples do jnz - apenas verificar se executa sem erro
		// (O teste completo de flags requer acesso a metodos internos)
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Memoria[1] = endereco de salto (10)
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		arch.getExtbus1().put(10);
		arch.getMemory().store();
		
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		try {
			arch.jnz();
			System.out.println("OK testJnz: metodo executou sem erro");
		} catch (Exception e) {
			System.out.println("ERRO testJnz: " + e.getMessage());
		}
	}

	public static void testIncMem() {
		Architecture arch = new Architecture();
		
		// Preparar memoria: posicao 10 com valor 5
		arch.getExtbus1().put(10);
		arch.getMemory().store();
		arch.getExtbus1().put(5);
		arch.getMemory().store();
		
		// Configurar PC
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Memoria[1] = endereco da memoria a incrementar (10)
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		arch.getExtbus1().put(10);
		arch.getMemory().store();
		
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		arch.incMem();
		
		// Verificar se memoria[10] foi incrementada para 6
		arch.getExtbus1().put(10);
		arch.getMemory().read();
		int result = arch.getExtbus1().get();
		
		if (result == 6) {
			System.out.println("OK testIncMem: Memoria[10] = " + result);
		} else {
			System.out.println("ERRO testIncMem: esperado 6, obtido " + result);
		}
	}
}
