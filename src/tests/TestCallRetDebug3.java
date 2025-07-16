package tests;

import architecture.Architecture;

public class TestCallRetDebug3 {
    
    public static void main(String[] args) {
        System.out.println("=== DEBUG MEMORIA ===");
        
        try {
            Architecture arch = new Architecture(false);
            
            // Test direct memory operations
            System.out.println("Testando operacoes diretas de memoria:");
            
            // Store value 42 at address 126
            arch.getExtbus1().put(126);
            arch.getMemory().store();
            arch.getExtbus1().put(42);
            arch.getMemory().store();
            System.out.println("Armazenado valor 42 no endereco 126");
            
            // Read value back
            arch.getExtbus1().put(126);
            arch.getMemory().read();
            arch.getMemory().read();
            int readValue = arch.getExtbus1().get();
            System.out.println("Valor lido do endereco 126: " + readValue);
            
            // Now test stack push with value 99
            System.out.println("\nTestando stack push com valor 99:");
            System.out.println("Stack TOP antes: " + arch.getStackTop());
            arch.stackPush(99);
            System.out.println("Stack TOP apos: " + arch.getStackTop());
            
            // Check what was stored
            int stackAddr = arch.getStackTop();
            arch.getExtbus1().put(stackAddr);
            arch.getMemory().read();
            arch.getMemory().read();
            int storedValue = arch.getExtbus1().get();
            System.out.println("Valor na stack endereco " + stackAddr + ": " + storedValue);
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
