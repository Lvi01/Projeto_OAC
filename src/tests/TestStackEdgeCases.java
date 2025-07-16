package tests;

import architecture.Architecture;

public class TestStackEdgeCases {
    
    public static void main(String[] args) {
        System.out.println("=== TESTE DE CASOS EXTREMOS DA STACK ===");
        
        Architecture arch = new Architecture(false);
        
        try {
            // Test stack underflow
            System.out.println("\n1. Testando UNDERFLOW (stack vazia):");
            System.out.println("Stack esta vazia? " + arch.isStackEmpty());
            
            try {
                arch.stackPop();
                System.out.println("ERRO: Deveria ter lancado excecao!");
            } catch (RuntimeException e) {
                System.out.println("OK: Excecao capturada - " + e.getMessage());
            }
            
            // Test filling the stack
            System.out.println("\n2. Preenchendo a stack completamente:");
            int pushed = 0;
            try {
                for (int i = 1; i <= 30; i++) { // Tentar mais que 28 elementos
                    arch.stackPush(i * 10);
                    pushed++;
                    if (i % 10 == 0) {
                        System.out.println("Pushed " + pushed + " elementos - TOP: " + arch.getStackTop());
                    }
                }
            } catch (RuntimeException e) {
                System.out.println("Stack overflow apos " + pushed + " elementos: " + e.getMessage());
            }
            
            System.out.println("Stack esta cheia? " + arch.isStackFull());
            System.out.println("Elementos na stack: " + (127 - arch.getStackTop()));
            
            // Test emptying the stack
            System.out.println("\n3. Esvaziando a stack:");
            int popped = 0;
            try {
                while (!arch.isStackEmpty()) {
                    arch.stackPop();
                    popped++;
                }
                System.out.println("Removidos " + popped + " elementos da stack");
                System.out.println("Stack esta vazia? " + arch.isStackEmpty());
            } catch (RuntimeException e) {
                System.out.println("Erro ao esvaziar: " + e.getMessage());
            }
            
            System.out.println("\n=== TESTE DE CASOS EXTREMOS CONCLUIDO! ===");
            
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
