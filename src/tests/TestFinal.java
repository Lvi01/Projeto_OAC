import architecture.Architecture;

public class TestFinal {
    public static void main(String[] args) {
        try {
            Architecture arch = new Architecture(false);
            
            System.out.println("=== TESTE FINAL: CALL e RET ===");
            
            // Check commands list contains CALL and RET
            System.out.println("Lista de comandos:");
            for (int i = 0; i < arch.getCommandsList().size(); i++) {
                System.out.println(i + ": " + arch.getCommandsList().get(i));
            }
            
            System.out.println("\n=== Teste de CALL/RET ===");
            
            // Test 1: Direct CALL/RET
            System.out.println("PC inicial: 0");
            
            // CALL 100
            arch.getExtbus1().put(100);
            arch.call();
            
            // Verify PC and stack
            arch.getPC().read();
            int pcAfterCall = arch.getExtbus1().get();
            System.out.println("PC depois de CALL 100: " + pcAfterCall);
            System.out.println("Pilha top: " + arch.getStackTop());
            
            // Verify return address on stack
            int stackAddr = arch.getStackTop();
            arch.getExtbus1().put(stackAddr);
            arch.getMemory().read();
            int returnAddr = arch.getExtbus1().get();
            System.out.println("Endereco de retorno na pilha: " + returnAddr);
            
            // RET
            arch.ret();
            arch.getPC().read();
            int pcAfterRet = arch.getExtbus1().get();
            System.out.println("PC depois de RET: " + pcAfterRet);
            System.out.println("Pilha top depois de RET: " + arch.getStackTop());
            
            if (pcAfterCall == 100 && returnAddr == 2 && pcAfterRet == 2) {
                System.out.println("\nSUCESSO! CALL e RET funcionando perfeitamente!");
            } else {
                System.out.println("\nERRO na implementacao!");
            }
            
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
