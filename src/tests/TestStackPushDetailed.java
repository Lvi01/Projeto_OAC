package tests;

import architecture.Architecture;

public class TestStackPushDetailed {
    
    public static void main(String[] args) {
        System.out.println("=== DEBUG DETALHADO STACK PUSH ===");
        
        try {
            Architecture arch = new Architecture(false);
            
            System.out.println("Estado inicial:");
            System.out.println("Stack TOP: " + arch.getStackTop());
            
            // Test the pushComplete method directly
            System.out.println("\nTestando pushComplete diretamente:");
            int stackAddr = arch.getUla().pushComplete(555);
            System.out.println("pushComplete retornou endereco: " + stackAddr);
            System.out.println("Stack TOP apos pushComplete: " + arch.getStackTop());
            
            // Now store manually
            arch.getExtbus1().put(stackAddr);
            arch.getMemory().store();
            arch.getExtbus1().put(555);
            arch.getMemory().store();
            System.out.println("Armazenado manualmente 555 no endereco " + stackAddr);
            
            // Read back
            arch.getExtbus1().put(stackAddr);
            arch.getMemory().read();
            arch.getMemory().read();
            int readValue = arch.getExtbus1().get();
            System.out.println("Valor lido: " + readValue);
            
            // Now test stackPush method
            System.out.println("\nTestando stackPush method:");
            System.out.println("Stack TOP antes: " + arch.getStackTop());
            arch.stackPush(777);
            System.out.println("Stack TOP apos: " + arch.getStackTop());
            
            // Check what was stored
            int newStackAddr = arch.getStackTop();
            arch.getExtbus1().put(newStackAddr);
            arch.getMemory().read();
            arch.getMemory().read();
            int storedValue = arch.getExtbus1().get();
            System.out.println("Valor armazenado via stackPush: " + storedValue);
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
