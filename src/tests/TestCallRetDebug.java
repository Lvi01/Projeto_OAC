package tests;

import architecture.Architecture;

public class TestCallRetDebug {
    
    public static void main(String[] args) {
        System.out.println("=== DEBUG CALL/RET ===");
        
        try {
            Architecture arch = new Architecture(false);
            
            // Setup simple memory
            arch.getExtbus1().put(0);
            arch.getMemory().store();
            arch.getExtbus1().put(10); // CALL instruction
            arch.getMemory().store();
            
            arch.getExtbus1().put(1);
            arch.getMemory().store();
            arch.getExtbus1().put(50); // Target address
            arch.getMemory().store();
            
            // Set PC to 0
            arch.getExtbus1().put(0);
            arch.getPC().store();
            
            System.out.println("Antes do CALL:");
            System.out.println("PC: " + arch.getPC().getData());
            System.out.println("Stack TOP: " + arch.getStackTop());
            System.out.println("Stack vazia: " + arch.isStackEmpty());
            
            // Execute CALL
            arch.getPC().read();
            arch.getMemory().read();
            arch.getIR().store();
            arch.call();
            
            System.out.println("\nApos CALL:");
            System.out.println("PC: " + arch.getPC().getData());
            System.out.println("Stack TOP: " + arch.getStackTop());
            System.out.println("Stack vazia: " + arch.isStackEmpty());
            
            // Check what was stored in stack
            int stackAddr = arch.getStackTop();
            arch.getExtbus1().put(stackAddr);
            arch.getMemory().read();
            arch.getMemory().read();
            int storedValue = arch.getExtbus1().get();
            System.out.println("Valor armazenado na stack no endereco " + stackAddr + ": " + storedValue);
            
            // Now test RET
            System.out.println("\nTestando RET:");
            arch.getExtbus1().put(11); // RET instruction
            arch.getIR().store();
            arch.ret();
            
            System.out.println("Apos RET:");
            System.out.println("PC: " + arch.getPC().getData());
            System.out.println("Stack TOP: " + arch.getStackTop());
            System.out.println("Stack vazia: " + arch.isStackEmpty());
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
