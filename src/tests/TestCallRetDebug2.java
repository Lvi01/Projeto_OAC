package tests;

import architecture.Architecture;

public class TestCallRetDebug2 {
    
    public static void main(String[] args) {
        System.out.println("=== DEBUG DETALHADO CALL/RET ===");
        
        try {
            Architecture arch = new Architecture(false);
            
            // Set PC to 5 to test with different value
            arch.getExtbus1().put(5);
            arch.getPC().store();
            
            System.out.println("PC inicial: " + arch.getPC().getData());
            
            // Test address calculation manually
            arch.getPC().internalRead();
            System.out.println("PC no intbus1: " + arch.getIntbus1().get());
            
            arch.getUla().store(1);
            arch.getUla().inc();
            arch.getUla().read(1);
            System.out.println("PC+1 no intbus1: " + arch.getIntbus1().get());
            
            arch.getUla().inc();
            arch.getUla().read(1);
            System.out.println("PC+2 no intbus1: " + arch.getIntbus1().get());
            int returnAddress = arch.getIntbus1().get();
            System.out.println("Return address calculado: " + returnAddress);
            
            // Now test stack push manually
            System.out.println("\nTestando stack push manual:");
            System.out.println("Stack TOP antes: " + arch.getStackTop());
            arch.stackPush(returnAddress);
            System.out.println("Stack TOP apos: " + arch.getStackTop());
            
            // Check stored value
            int stackAddr = arch.getStackTop();
            arch.getExtbus1().put(stackAddr);
            arch.getMemory().read();
            arch.getMemory().read();
            int storedValue = arch.getExtbus1().get();
            System.out.println("Valor armazenado: " + storedValue);
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
