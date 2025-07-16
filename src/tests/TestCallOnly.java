package tests;

import architecture.Architecture;

public class TestCallOnly {
    
    public static void main(String[] args) {
        System.out.println("=== TESTE APENAS DO CALL ===");
        
        try {
            Architecture arch = new Architecture(false);
            
            // Setup simple call
            arch.getExtbus1().put(0);
            arch.getMemory().store();
            arch.getExtbus1().put(10); // CALL
            arch.getMemory().store();
            
            arch.getExtbus1().put(1);
            arch.getMemory().store();
            arch.getExtbus1().put(50); // Target
            arch.getMemory().store();
            
            // Set PC to 0
            arch.getExtbus1().put(0);
            arch.getPC().store();
            
            System.out.println("Antes do CALL:");
            System.out.println("PC: " + arch.getPC().getData());
            
            // Execute CALL manually step by step
            System.out.println("\nCalculando endereco de retorno:");
            arch.getPC().internalRead();
            int currentPC = arch.getIntbus1().get();
            System.out.println("Current PC: " + currentPC);
            int returnAddress = currentPC + 2;
            System.out.println("Return address (PC+2): " + returnAddress);
            
            // Check what stackPush will store
            System.out.println("\nTestando stackPush com endereco " + returnAddress + ":");
            arch.stackPush(returnAddress);
            
            // Check what was actually stored
            int stackAddr = arch.getStackTop();
            arch.getExtbus1().put(stackAddr);
            arch.getMemory().read();
            arch.getMemory().read();
            int storedValue = arch.getExtbus1().get();
            System.out.println("Endereco da stack: " + stackAddr);
            System.out.println("Valor armazenado na stack: " + storedValue);
            
            // Now test what stackPop returns
            System.out.println("\nTestando stackPop:");
            int poppedAddr = arch.stackPop();
            System.out.println("Endereco retornado pelo stackPop: " + poppedAddr);
            
            // Read value from that address
            arch.getExtbus1().put(poppedAddr);
            arch.getMemory().read();
            arch.getMemory().read();
            int poppedValue = arch.getExtbus1().get();
            System.out.println("Valor no endereco retornado: " + poppedValue);
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
