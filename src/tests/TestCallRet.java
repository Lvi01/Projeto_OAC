package tests;

import architecture.Architecture;

public class TestCallRet {
    
    public static void main(String[] args) {
        System.out.println("=== TESTE DAS INSTRUCOES CALL e RET ===");
        
        try {
            // Create architecture instance
            Architecture arch = new Architecture(false);
            
            System.out.println("\n1. Estado inicial:");
            System.out.println("PC inicial: " + arch.getPC().getData());
            System.out.println("Stack esta vazia? " + arch.isStackEmpty());
            System.out.println("Stack TOP: " + arch.getStackTop());
            
            // Simular um programa simples com CALL e RET
            // Vamos preparar a memoria com:
            // Posicao 0: CALL 10 (instrucao 10, parametro no endereco 1)
            // Posicao 1: 50 (endereco da subrotina)
            // Posicao 2: LDI 99 (instrucao apos CALL - endereco de retorno)
            // Posicao 3: 99 (valor do LDI)
            // ...
            // Posicao 50: LDI 77 (inicio da subrotina)
            // Posicao 51: 77 (valor do LDI na subrotina)
            // Posicao 52: RET (instrucao 11)
            
            // Setup memory for CALL test
            System.out.println("\n2. Configurando memoria para teste:");
            
            // Instruction CALL 50 at position 0-1
            arch.getExtbus1().put(0);
            arch.getMemory().store();
            arch.getExtbus1().put(10); // CALL instruction code
            arch.getMemory().store();
            
            arch.getExtbus1().put(1);
            arch.getMemory().store();
            arch.getExtbus1().put(50); // Target address (subroutine start)
            arch.getMemory().store();
            
            // Instruction LDI 99 at position 2-3 (return point)
            arch.getExtbus1().put(2);
            arch.getMemory().store();
            arch.getExtbus1().put(7); // LDI instruction code
            arch.getMemory().store();
            
            arch.getExtbus1().put(3);
            arch.getMemory().store();
            arch.getExtbus1().put(99); // LDI value
            arch.getMemory().store();
            
            // Subroutine at position 50-52
            // LDI 77
            arch.getExtbus1().put(50);
            arch.getMemory().store();
            arch.getExtbus1().put(7); // LDI instruction code
            arch.getMemory().store();
            
            arch.getExtbus1().put(51);
            arch.getMemory().store();
            arch.getExtbus1().put(77); // LDI value in subroutine
            arch.getMemory().store();
            
            // RET
            arch.getExtbus1().put(52);
            arch.getMemory().store();
            arch.getExtbus1().put(11); // RET instruction code
            arch.getMemory().store();
            
            // Halt instruction at position 4
            arch.getExtbus1().put(4);
            arch.getMemory().store();
            arch.getExtbus1().put(-1); // Halt
            arch.getMemory().store();
            
            System.out.println("Memoria configurada:");
            System.out.println("  Pos 0-1: CALL 50");
            System.out.println("  Pos 2-3: LDI 99 (ponto de retorno)");
            System.out.println("  Pos 4: HALT");
            System.out.println("  Pos 50-51: LDI 77 (subrotina)");
            System.out.println("  Pos 52: RET");
            
            // Set PC to start of program
            arch.getExtbus1().put(0);
            arch.getPC().store();
            
            System.out.println("\n3. Executando programa:");
            
            // Step 1: Fetch and execute CALL
            System.out.println("\nPasso 1 - Executando CALL 50:");
            System.out.println("PC antes: " + arch.getPC().getData());
            System.out.println("REG0 antes: " + arch.getREG0().getData());
            System.out.println("Stack vazia antes? " + arch.isStackEmpty());
            
            // Simulate fetch
            arch.getPC().read();
            arch.getMemory().read();
            arch.getIR().store();
            
            // Execute CALL
            arch.call();
            
            System.out.println("PC apos CALL: " + arch.getPC().getData());
            System.out.println("Stack vazia apos CALL? " + arch.isStackEmpty());
            System.out.println("Stack TOP apos CALL: " + arch.getStackTop());
            
            // Step 2: Execute LDI 77 in subroutine
            System.out.println("\nPasso 2 - Executando LDI 77 na subrotina:");
            System.out.println("PC antes: " + arch.getPC().getData());
            System.out.println("REG0 antes: " + arch.getREG0().getData());
            
            // Fetch
            arch.getPC().read();
            arch.getMemory().read();
            arch.getIR().store();
            
            // Execute LDI
            arch.ldi();
            
            System.out.println("PC apos LDI: " + arch.getPC().getData());
            System.out.println("REG0 apos LDI: " + arch.getREG0().getData());
            
            // Step 3: Execute RET
            System.out.println("\nPasso 3 - Executando RET:");
            System.out.println("PC antes: " + arch.getPC().getData());
            System.out.println("Stack TOP antes: " + arch.getStackTop());
            
            // Fetch
            arch.getPC().read();
            arch.getMemory().read();
            arch.getIR().store();
            
            // Execute RET
            arch.ret();
            
            System.out.println("PC apos RET: " + arch.getPC().getData());
            System.out.println("Stack vazia apos RET? " + arch.isStackEmpty());
            System.out.println("Stack TOP apos RET: " + arch.getStackTop());
            
            // Step 4: Execute LDI 99 at return point
            System.out.println("\nPasso 4 - Executando LDI 99 no ponto de retorno:");
            System.out.println("PC antes: " + arch.getPC().getData());
            System.out.println("REG0 antes: " + arch.getREG0().getData());
            
            // Fetch
            arch.getPC().read();
            arch.getMemory().read();
            arch.getIR().store();
            
            // Execute LDI
            arch.ldi();
            
            System.out.println("PC apos LDI: " + arch.getPC().getData());
            System.out.println("REG0 apos LDI: " + arch.getREG0().getData());
            
            System.out.println("\n=== TESTE CALL/RET CONCLUIDO COM SUCESSO! ===");
            
            // Verify results
            System.out.println("\n4. Verificacao dos resultados:");
            System.out.println("REG0 final deve ser 99 (valor do LDI no retorno): " + arch.getREG0().getData());
            System.out.println("PC final deve ser 4 (proxima instrucao apos retorno): " + arch.getPC().getData());
            System.out.println("Stack deve estar vazia: " + arch.isStackEmpty());
            
            if (arch.getREG0().getData() == 99 && arch.getPC().getData() == 4 && arch.isStackEmpty()) {
                System.out.println("✓ TESTE PASSOU - CALL/RET funcionando corretamente!");
            } else {
                System.out.println("✗ TESTE FALHOU - Verifique a implementacao");
            }
            
        } catch (Exception e) {
            System.err.println("Erro durante o teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
