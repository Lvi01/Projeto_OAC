package tests;

import architecture.Architecture;

public class TestStack {
    
    public static void main(String[] args) {
        System.out.println("=== TESTE DA STACK - ARQUITETURA C ===");
        
        // Create architecture instance
        Architecture arch = new Architecture(false); // sem simulation mode
        
        try {
            // Test initial stack state
            System.out.println("\n1. Estado inicial da stack:");
            System.out.println("Stack TOP: " + arch.getStackTop());
            System.out.println("Stack BOTTOM: " + arch.getStackBottom());
            System.out.println("Stack esta vazia? " + arch.isStackEmpty());
            System.out.println("Stack esta cheia? " + arch.isStackFull());
            
            // Test push operations
            System.out.println("\n2. Testando operacoes de PUSH:");
            arch.stackPush(10);
            System.out.println("Pushed 10 - Stack TOP agora: " + arch.getStackTop());
            
            arch.stackPush(20);
            System.out.println("Pushed 20 - Stack TOP agora: " + arch.getStackTop());
            
            arch.stackPush(30);
            System.out.println("Pushed 30 - Stack TOP agora: " + arch.getStackTop());
            
            System.out.println("Stack esta vazia? " + arch.isStackEmpty());
            
            // Test peek operation
            System.out.println("\n3. Testando PEEK:");
            int topAddress = arch.stackPeek();
            System.out.println("Endereco do topo da stack: " + topAddress);
            
            // Test pop operations
            System.out.println("\n4. Testando operacoes de POP:");
            int poppedAddr1 = arch.stackPop();
            System.out.println("Popped from address: " + poppedAddr1 + " - Stack TOP agora: " + arch.getStackTop());
            
            int poppedAddr2 = arch.stackPop();
            System.out.println("Popped from address: " + poppedAddr2 + " - Stack TOP agora: " + arch.getStackTop());
            
            int poppedAddr3 = arch.stackPop();
            System.out.println("Popped from address: " + poppedAddr3 + " - Stack TOP agora: " + arch.getStackTop());
            
            System.out.println("Stack esta vazia? " + arch.isStackEmpty());
            
            // Test reset
            System.out.println("\n5. Testando RESET da stack:");
            arch.resetStack();
            System.out.println("Apos reset - Stack TOP: " + arch.getStackTop());
            System.out.println("Stack esta vazia? " + arch.isStackEmpty());
            
            System.out.println("\n=== TESTE CONCLUIDO COM SUCESSO! ===");
            
        } catch (Exception e) {
            System.err.println("Erro durante o teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
