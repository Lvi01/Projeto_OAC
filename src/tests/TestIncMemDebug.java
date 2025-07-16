package tests;

import architecture.Architecture;

public class TestIncMemDebug {

	public static void main(String[] args) {
		debugIncMem();
	}

	public static void debugIncMem() {
		System.out.println("=== DEBUG INC MEM ===");
		Architecture arch = new Architecture();
		
		// Preparar memoria: posicao 10 com valor 5
		System.out.println("Configurando memoria[10] = 5");
		arch.getExtbus1().put(10);
		arch.getMemory().store();
		arch.getExtbus1().put(5);
		arch.getMemory().store();
		
		// Verificar se foi configurado
		arch.getExtbus1().put(10);
		arch.getMemory().read();
		System.out.println("Memoria[10] inicial: " + arch.getExtbus1().get());
		
		// Configurar PC para começar na posição 0
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		// Preparar parâmetros na memória
		// Posição 0: comando incMem (15)
		arch.getExtbus1().put(0);
		arch.getMemory().store();
		arch.getExtbus1().put(15);
		arch.getMemory().store();
		
		// Posição 1: endereco da memoria a incrementar (10)
		arch.getExtbus1().put(1);
		arch.getMemory().store();
		arch.getExtbus1().put(10);
		arch.getMemory().store();
		
		System.out.println("Parametros configurados na memoria");
		
		// Verificar memória de comando
		arch.getExtbus1().put(0);
		arch.getMemory().read();
		System.out.println("Memoria[0] (comando): " + arch.getExtbus1().get());
		
		arch.getExtbus1().put(1);
		arch.getMemory().read();
		System.out.println("Memoria[1] (endereco): " + arch.getExtbus1().get());
		
		// Configurar PC para começar na posição 0 novamente
		arch.getExtbus1().put(0);
		arch.getPC().store();
		
		System.out.println("PC configurado para 0");
		arch.getPC().read();
		System.out.println("PC atual: " + arch.getExtbus1().get());
		
		// Executar incMem diretamente
		try {
			System.out.println("Executando incMem...");
			arch.incMem();
			
			// Verificar resultado
			System.out.println("=== RESULTADOS ===");
			arch.getExtbus1().put(10);
			arch.getMemory().read();
			int result = arch.getExtbus1().get();
			System.out.println("Memoria[10] final: " + result);
			
			arch.getPC().read();
			System.out.println("PC final: " + arch.getExtbus1().get());
			
			if (result == 6) {
				System.out.println("OK - Resultado correto: Memoria[10] = " + result);
			} else {
				System.out.println("ERRO - Esperado 6, obtido " + result);
			}
			
		} catch (Exception e) {
			System.out.println("ERRO: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
