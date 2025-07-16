package tests;

import architecture.Architecture;

public class TestStackMemory {
    
    public static void main(String[] args) {
        System.out.println("=== TESTE MEMORIA DA STACK ===");
        
        try {
            Architecture arch = new Architecture(false);
            
            // Test stack memory region (100-127)
            System.out.println("Testando regiao da stack (enderecos 100-127):");
            
            // Test address 126 (where stack starts)
            arch.getExtbus1().put(126);
            arch.getMemory().store();
            arch.getExtbus1().put(789);
            arch.getMemory().store();
            
            System.out.println("Armazenado 789 no endereco 126");
            System.out.println("Valor direto no array: " + arch.getMemory().getDataList()[126]);
            
            // Read back
            arch.getExtbus1().put(126);
            arch.getMemory().read();
            arch.getMemory().read();
            System.out.println("Valor lido via bus: " + arch.getExtbus1().get());
            
            // Test other stack addresses
            System.out.println("\nTestando outros enderecos da stack:");
            for (int addr = 120; addr <= 127; addr++) {
                arch.getExtbus1().put(addr);
                arch.getMemory().store();
                arch.getExtbus1().put(addr * 10); // Store addr*10 at each address
                arch.getMemory().store();
                
                // Read back
                arch.getExtbus1().put(addr);
                arch.getMemory().read();
                arch.getMemory().read();
                int readValue = arch.getExtbus1().get();
                System.out.println("Endereco " + addr + ": armazenado " + (addr * 10) + ", lido " + readValue);
            }
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
