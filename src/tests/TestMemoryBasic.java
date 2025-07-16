package tests;

import architecture.Architecture;

public class TestMemoryBasic {
    
    public static void main(String[] args) {
        System.out.println("=== TESTE BASICO DE MEMORIA ===");
        
        try {
            Architecture arch = new Architecture(false);
            
            // Check memory array directly
            System.out.println("Tamanho da memoria: " + arch.getMemory().getDataList().length);
            
            // Store and read using different approach
            System.out.println("Testando com endereco 10:");
            
            // Method 1: Direct array access for comparison
            arch.getMemory().getDataList()[10] = 123;
            System.out.println("Valor direto no array posicao 10: " + arch.getMemory().getDataList()[10]);
            
            // Method 2: Using bus operations
            arch.getExtbus1().put(10); // Set address
            arch.getMemory().store(); // Memory ready to store
            arch.getExtbus1().put(456); // Set value
            arch.getMemory().store(); // Store value
            
            System.out.println("Valor no array apos bus store: " + arch.getMemory().getDataList()[10]);
            
            // Read back using bus
            arch.getExtbus1().put(10);
            arch.getMemory().read();
            arch.getMemory().read();
            System.out.println("Valor lido via bus: " + arch.getExtbus1().get());
            
            // Check a few memory positions
            System.out.println("\nPrimeiras posicoes da memoria:");
            for (int i = 0; i < 15; i++) {
                System.out.println("Mem[" + i + "] = " + arch.getMemory().getDataList()[i]);
            }
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
