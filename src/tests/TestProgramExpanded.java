package tests;

import architecture.Architecture;
import java.io.IOException;

/**
 * Teste nao-interativo do programa expandido
 */
public class TestProgramExpanded {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== TESTE DO PROGRAMA EXPANDIDO ===");
            
            // Criar arquitetura SEM modo de simulacao (nao interativo)
            Architecture arch = new Architecture(false);
            
            // Carregar programa simples
            arch.readExec("program_simple");
            
            System.out.println("Estado inicial:");
            printRegisters(arch);
            
            // Executar programa
            arch.controlUnitEexec();
            
            System.out.println("\nEstado final:");
            printRegisters(arch);
            
            // Verificar se addRegReg funcionou
            // Esperado: REG1 = 5 + 3 = 8
            arch.getREG1().read();
            int reg1Value = arch.getExtbus1().get();
            
            System.out.println("\nResultado addRegReg:");
            System.out.println("REG1 = " + reg1Value + " (esperado: 8)");
            
            if (reg1Value == 8) {
                System.out.println("OK addRegReg (12) funcionou corretamente!");
            } else {
                System.out.println("ERRO addRegReg (12) nao funcionou como esperado.");
            }
            
            // Verificar memória[100]
            arch.getMemory().getDataList();
            int mem100 = arch.getMemory().getDataList()[100];
            System.out.println("Memoria[100] = " + mem100);
            
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erro durante execucao: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printRegisters(Architecture arch) {
        arch.getREG0().read();
        System.out.println("REG0: " + arch.getExtbus1().get());
        
        arch.getREG1().read();
        System.out.println("REG1: " + arch.getExtbus1().get());
        
        arch.getREG2().read();
        System.out.println("REG2: " + arch.getExtbus1().get());
        
        arch.getREG3().read();
        System.out.println("REG3: " + arch.getExtbus1().get());
        
        arch.getPC().read();
        System.out.println("PC: " + arch.getExtbus1().get());
        
        System.out.println("Flags: " + arch.getFlags().getData());
    }
}
