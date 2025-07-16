package tests;

import architecture.Architecture;

public class TestPhase1Debug {

	public static void main(String[] args) {
		debugAddRegReg();
	}

	public static void debugAddRegReg() {
		System.out.println("=== DEBUG ADD REG REG ===");
		Architecture arch = new Architecture();
		
		// Limpar registradores primeiro
		arch.getExtbus1().put(0);
		arch.getREG0().store();
		arch.getREG1().store();
		
		// Preparar registradores: REG0 = 5, REG1 = 3
		System.out.println("Configurando REG0 = 5");
		arch.getExtbus1().put(5);
		arch.getREG0().store();
		
		System.out.println("Configurando REG1 = 3");
		arch.getExtbus1().put(3);
		arch.getREG1().store();
		
		// Verificar se foram configurados corretamente
		arch.getREG0().read();
		System.out.println("REG0 inicial: " + arch.getExtbus1().get());
		
		arch.getREG1().read();
		System.out.println("REG1 inicial: " + arch.getExtbus1().get());
		
		// Preparar PC para apontar para posição 0
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Preparar parâmetros na memória
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
		
		System.out.println("Parametros configurados na memoria");
		
		// Verificar memória
		arch.getExtbus1().put(0);
		arch.getMemory().read();
		System.out.println("Memoria[0]: " + arch.getExtbus1().get());
		
		arch.getExtbus1().put(1);
		arch.getMemory().read();
		System.out.println("Memoria[1]: " + arch.getExtbus1().get());
		
		arch.getExtbus1().put(2);
		arch.getMemory().read();
		System.out.println("Memoria[2]: " + arch.getExtbus1().get());
		
		// Configurar PC para começar na posição 0 novamente
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		System.out.println("PC configurado para 0");
		arch.getPC().read();
		System.out.println("PC atual: " + arch.getExtbus1().get());
		
		// Executar addRegReg diretamente
		try {
			System.out.println("Executando addRegReg...");
			arch.addRegReg();
			
			// Verificar resultado
			System.out.println("=== RESULTADOS ===");
			arch.getREG0().read();
			System.out.println("REG0 final: " + arch.getExtbus1().get());
			
			arch.getREG1().read();
			int result = arch.getExtbus1().get();
			System.out.println("REG1 final: " + result);
			
			arch.getPC().read();
			System.out.println("PC final: " + arch.getExtbus1().get());
			
			if (result == 8) {
				System.out.println("OK - Resultado correto: REG1 = " + result);
			} else {
				System.out.println("ERRO - Esperado 8, obtido " + result);
			}
			
		} catch (Exception e) {
			System.out.println("ERRO: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
