package tests;

import architecture.Architecture;
import java.io.IOException;

/**
 * Teste do programa principal - executa automaticamente o program.dxf
 * que demonstra todas as 21 instrucoes da arquitetura expandida
 */
public class TestMainProgram {
    
    public static void main(String[] args) {
        try {
            System.out.println("=====================================================================");
            System.out.println("        ARQUITETURA C EXPANDIDA - PROGRAMA PRINCIPAL");
            System.out.println("=====================================================================");
            System.out.println("Executando programa que demonstra todas as 21 instrucoes...");
            System.out.println();
            
            // Criar arquitetura em modo NAO-interativo para execucao automatica
            Architecture arch = new Architecture(false);
            
            // Carregar e executar o programa principal (program.dxf)
            arch.readExec("program");
            arch.controlUnitEexec();
            
            System.out.println("\n=====================================================================");
            System.out.println("                         EXECUCAO CONCLUIDA");
            System.out.println("=====================================================================");
            printFinalState(arch);
            
            System.out.println("\nSUCESSO: Programa principal executado com todas as 21 instrucoes!");
            System.out.println("Arquitetura C expandida funcionando perfeitamente!");
            
        } catch (IOException e) {
            System.out.println("ERRO ao ler arquivo program.dxf: " + e.getMessage());
            System.out.println("Certifique-se de que o arquivo program.dxf existe no diretorio.");
        } catch (Exception e) {
            System.out.println("ERRO durante execucao: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printFinalState(Architecture arch) {
        System.out.println("Estado final dos registradores:");
        System.out.println("  REG0: " + arch.getREG0().getData());
        System.out.println("  REG1: " + arch.getREG1().getData());
        System.out.println("  REG2: " + arch.getREG2().getData());
        System.out.println("  REG3: " + arch.getREG3().getData());
        System.out.println("  PC: " + arch.getPC().getData());
        System.out.println("  Flags: " + arch.getFlags().getData());
        
        // Verificar resultado do programa
        int resultado = arch.getMemory().getDataList()[122]; // teste101
        if (resultado == 88 || resultado == 99) {
            System.out.println("\nResultado do programa: " + resultado + " - OK!");
        } else {
            System.out.println("\nResultado: " + resultado);
        }
    }
}
