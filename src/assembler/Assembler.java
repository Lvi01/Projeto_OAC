package assembler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap; // Para um mapeamento mais robusto de registers
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import components.Opcodes; // Importa Opcodes diretamente

// Remover dependência de Architecture, pois Assembler deve ser independente da implementação do hardware
// import architecture.Architecture;
// import components.Register; // Não é mais necessário importar Register aqui se Opcodes.getRegisterId() for usado

public class Assembler {
    
    private ArrayList<String> lines; // Linhas do arquivo .dsf de entrada
    private ArrayList<String> objProgram; // Programa objeto (com labels/vars marcados com &)
    private ArrayList<String> execProgram; // Programa executável (com endereços resolvidos)

    // Removido: private Architecture arch; // Não mais dependente da instância de Architecture
    private int memorySize; // O tamanho da memória deve ser um parâmetro ou constante

    // Mapas para resolver labels e variáveis durante a montagem
    private Map<String, Integer> labelAddresses; // labelName -> address
    private Map<String, Integer> variableAddresses; // variableName -> address

    // Lista de comandos da arquitetura (para validação e lookup)
    private List<String> commands; 
    
    // Construtor: Agora recebe o tamanho da memória como parâmetro
    public Assembler(int memorySize) {
        this.memorySize = memorySize;
        this.lines = new ArrayList<>();
        this.objProgram = new ArrayList<>();
        this.execProgram = new ArrayList<>();
        this.labelAddresses = new HashMap<>();
        this.variableAddresses = new HashMap<>(); // Novo mapa para endereços de variáveis
        
        // Preenche a lista de comandos a partir de Opcodes.java
        // Isso é para o `findCommandNumber` e `proccessCommand` entenderem os comandos.
        // É uma lista que representa a "commandsList" do seu Architecture.java.
        // A ordem aqui deve corresponder aos opcodes 0-9.
        commands = new ArrayList<>();
        commands.add("add"); // 0
        commands.add("sub"); // 1
        commands.add("jmp"); // 2
        commands.add("jz");  // 3
        commands.add("jn");  // 4
        commands.add("read");// 5
        commands.add("store");//6
        commands.add("ldi"); // 7
        commands.add("inc"); // 8
        commands.add("moveregreg"); // 9 (para "move %regA %regB")
        
        // Adicionar os comandos restantes do assembly original que você forneceu,
        // mas que precisam ser mapeados para os opcodes 10+
        commands.add("jnz"); // 10
        commands.add("jeq"); // 11
        commands.add("jgt"); // 12
        commands.add("jlw"); // 13
        commands.add("call"); // 14
        commands.add("ret"); // 15
        commands.add("imul"); // 16
        commands.add("halt"); // 17
    }
    
    // --- Getters para TDD (mantidos) ---
    public ArrayList<String> getObjProgram() { return objProgram; }
    protected Map<String, Integer> getLabelsAddresses() { return labelAddresses; }
    protected Map<String, Integer> getVariablesAddresses() { return variableAddresses; }
    public ArrayList<String> getExecProgram() { return execProgram; }
    protected void setLines(ArrayList<String> lines) { this.lines = lines; } 
    protected void setExecProgram(ArrayList<String> lines) { this.execProgram = lines; } 
    
    /*
     * An assembly program is always in the following template
     * <variables>
     * <commands>
     * Obs.
     * variables names are always started with alphabetical char
     * variables names must contains only alphabetical and numerical chars
     * variables names never uses any command name
     * names ended with ":" identifies labels i.e. address in the memory
     * Commands are only that ones known in the architecture. No comments allowed
     * The assembly file must have the extention .dsf
     * The executable file must have the extention .dxf   
     */
    
    /**
     * This method reads an entire file in assembly (.dsf)
     * and stores its lines.
     * @param filenameWithoutExtension
     * @throws IOException 
     */
    public void read(String filenameWithoutExtension) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filenameWithoutExtension + ".dsf"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                // Limpeza básica da linha: remove comentários e espaços extras
                int commentIndex = linha.indexOf('#'); // Assumindo # para comentários
                if (commentIndex != -1) {
                    linha = linha.substring(0, commentIndex);
                }
                linha = linha.trim();
                if (!linha.isEmpty()) {
                    lines.add(linha);
                }
            }
        }
    }
    
    /**
     * This method scans the strings in lines, performing a two-pass assembly.
     * Pass 1: Identifies labels and their addresses.
     * Pass 2: Processes commands, variables, and replaces labels/variables with addresses.
     */
    public void parse() {
        // --- Passagem 1: Mapear Labels e Coletar Variáveis ---
        // A lógica do seu parse() original misturava. Vamos separar para clareza.
        int currentProgramAddress = 0; // Endereço de início do código de máquina (após variáveis, mas ainda não sabemos o tamanho das vars)
                                     // No seu modelo, o objProgram.size() é o endereço do próximo comando.
        
        // Percorre as linhas para identificar labels e estimar o tamanho do código
        for (String s : lines) {
            String[] tokens = s.split("\\s+"); // Divide por um ou mais espaços
            if (tokens.length == 0) continue; // Linha vazia após trim

            if (tokens[0].endsWith(":")) { // É uma label
                String label = tokens[0].substring(0, tokens[0].length() - 1);
                if (labelAddresses.containsKey(label)) {
                    throw new IllegalArgumentException("Erro de montagem: Label duplicada encontrada: " + label);
                }
                // O endereço da label é a posição ATUAL no programa objeto (antes de adicionar a instrução)
                labelAddresses.put(label, currentProgramAddress);
                
                // Se houver uma instrução na mesma linha que a label
                if (tokens.length > 1) {
                    // O comando e seus parâmetros ocuparão posições de memória
                    currentProgramAddress += getInstructionSize(tokens[0].substring(0, tokens[0].length() - 1), tokens);
                }
            } else { // É uma instrução ou uma variável
                if (findCommandNumber(tokens[0]) >= 0) { // É uma instrução
                    currentProgramAddress += getInstructionSize(tokens[0], tokens);
                } else { // Presume-se que é uma declaração de variável
                    // Variáveis não contribuem para o `currentProgramAddress` do código.
                    // Elas serão alocadas a partir do final da memória.
                    // Apenas coletamos seus nomes nesta passagem.
                    if (!variables.contains(tokens[0])) { // Evita duplicatas se houver inicialização
                        variables.add(tokens[0]);
                    }
                }
            }
        }

        // --- Passagem 2: Gerar Programa Objeto (`objProgram`) e Alocar Variáveis ---
        int programCodeStartOffset = variables.size(); // Offset para o início do código se as vars estivessem no início.
                                                      // Mas as vars estão no fim da memória.
                                                      // No seu modelo, objProgram.size() já é o "endereço".

        // Reinicia o objProgram para a segunda passagem
        objProgram = new ArrayList<>(); 

        // Adiciona as variáveis ao objProgram temporariamente (apenas para a contagem de endereços iniciais)
        // Isso é se as variáveis fossem alocadas ANTES do código.
        // No seu `makeExecutable`, as variáveis são alocadas do fim da memória.
        // Então, o `objProgram` deve começar com o código.

        for (String s : lines) {
            String[] tokens = s.split("\\s+");
            if (tokens.length == 0) continue;

            if (tokens[0].endsWith(":")) { // É uma label
                // Se a label está sozinha em uma linha, não adiciona nada ao objProgram agora
                if (tokens.length == 1) {
                    // Já foi mapeada, apenas ignora
                } else {
                    // Label na mesma linha da instrução: processa a instrução
                    proccessCommand(tokens[1], getRemainingTokens(tokens, 2)); // Pula a label e o comando
                }
            } else { // É uma instrução ou variável
                if (findCommandNumber(tokens[0]) >= 0) { // É uma instrução
                    proccessCommand(tokens[0], getRemainingTokens(tokens, 1));
                } else { // Variável. Ignora, pois já foi coletada e será tratada em makeExecutable
                    // No seu Assembler, as variáveis não são adicionadas ao objProgram no parse().
                    // Elas são apenas coletadas na lista `variables`.
                    // Isso significa que `objProgram.size()` *não* incluirá as variáveis.
                }
            }
        }
        
        // --- Alocar endereços para variáveis (no final da memória, decrescente) ---
        // Isso é feito em `makeExecutable` no seu código, mas um bom lugar para prepará-las é aqui.
        // No seu `makeExecutable`, `replaceAllVariables` faz isso. Vamos manter lá.
    }

    /**
     * Calcula o número de palavras de memória que uma instrução ocupa.
     * No seu modelo, é 1 (opcode) + número de parâmetros.
     * @param commandName O nome do comando (ex: "add", "move")
     * @param tokens Todos os tokens da linha.
     * @return O número de palavras que a instrução ocupa na memória.
     */
    private int getInstructionSize(String commandName, String[] tokens) {
        // Com base nos comandos definidos em Opcodes e na sua estrutura de parâmetros
        // (opcode na primeira palavra, parâmetros nas subsequentes).
        String baseCommand = commandName.toLowerCase();
        if (baseCommand.endsWith(":")) { // Se veio uma label, pega o comando real
             baseCommand = tokens[1].toLowerCase();
        }

        switch (Opcodes.getOpcode(baseCommand)) {
            case Opcodes.ADD:
            case Opcodes.SUB:
            case Opcodes.JMP:
            case Opcodes.JZ:
            case Opcodes.JN:
            case Opcodes.READ:
            case Opcodes.STORE:
            case Opcodes.LDI:
                return 2; // Opcode + 1 parâmetro (endereço ou imediato)
            case Opcodes.INC:
            case Opcodes.RET:
            case Opcodes.HALT: // Assumindo HALT existe e não tem operandos
                return 1; // Apenas o opcode
            case Opcodes.MOVE_REG_REG: // move %regA %regB
                return 3; // Opcode + 2 parâmetros (IDs de registradores)
            case Opcodes.JEQ: // jeq %regA %regB <mem>
            case Opcodes.JGT:
            case Opcodes.JLW:
                return 4; // Opcode + 2 regs + 1 addr
            case Opcodes.CALL: // call <mem>
            case Opcodes.IMUL: // imul %regA %regB (assumindo dois regs)
                return 3; // Opcode + 2 regs
            case Opcodes.JNZ: // jnz <mem>
                return 2; // Opcode + 1 parâmetro (endereço)

            // Caso existam variações de ADD/SUB/MOVE/INC com outros tipos de operandos
            // que não estão na commandsList principal, e que você queira suportar:
            // Por exemplo, add <mem> %regA teria que ser um opcode diferente ou tratado aqui.
            // Pelo seu código, parece que `add` é sempre `add addr`.
            default:
                throw new IllegalArgumentException("Comando não reconhecido ou formato desconhecido para cálculo de tamanho: " + commandName);
        }
    }


    /**
     * This method processes a command, putting it and its parameters (if they have)
     * into the final array (`objProgram`).
     * @param command The command string (e.g., "add", "moveRegReg").
     * @param operands The array of operand strings.
     */
    protected void proccessCommand(String command, String[] operands) {
        // Encontra o número do comando (opcode)
        int commandNumber = Opcodes.getOpcode(command);
        if (commandNumber == -1) { // Isso não deve acontecer se `findCommandNumber` for bem feito
             throw new IllegalArgumentException("Comando desconhecido: " + command);
        }

        // Adiciona o opcode ao objProgram
        objProgram.add(Integer.toString(commandNumber));

        // Processa os operandos com base no comando
        switch (commandNumber) {
            case Opcodes.ADD:
            case Opcodes.SUB:
            case Opcodes.JMP:
            case Opcodes.JZ:
            case Opcodes.JN:
            case Opcodes.READ:
            case Opcodes.STORE:
                // Estes comandos têm 1 parâmetro (endereço ou label)
                if (operands.length != 1) throw new IllegalArgumentException("Comando " + command + " espera 1 operando de endereço/label.");
                // Adiciona o parâmetro, prefixando com '&' para indicar que é um endereço ou label/variável
                objProgram.add("&" + operands[0]);
                break;
            case Opcodes.LDI:
                // ldi tem 1 parâmetro (valor imediato)
                if (operands.length != 1) throw new IllegalArgumentException("Comando LDI espera 1 operando imediato.");
                // Parâmetros imediatos não são prefixados com '&', pois são valores literais
                objProgram.add(operands[0]);
                break;
            case Opcodes.INC:
            case Opcodes.RET:
            case Opcodes.HALT: // Se HALT for implementado como 0-operand
                // Estes comandos não têm parâmetros
                if (operands.length != 0) throw new IllegalArgumentException("Comando " + command + " não espera operandos.");
                break;
            case Opcodes.MOVE_REG_REG:
                // moveRegReg tem 2 parâmetros (REG A e REG B)
                if (operands.length != 2) throw new IllegalArgumentException("Comando MOVEREGREG espera 2 operandos de registrador.");
                // Registradores são prefixados com '%'
                objProgram.add(operands[0]); // ex: %REG0
                objProgram.add(operands[1]); // ex: %REG1
                break;
            // --- Novos comandos do assembly original que você precisa adicionar ---
            case Opcodes.JNZ:
                if (operands.length != 1) throw new IllegalArgumentException("Comando JNZ espera 1 operando de endereço.");
                objProgram.add("&" + operands[0]);
                break;
            case Opcodes.JEQ:
            case Opcodes.JGT:
            case Opcodes.JLW:
                if (operands.length != 3) throw new IllegalArgumentException("Comando " + command + " espera 3 operandos (regA, regB, mem).");
                objProgram.add(operands[0]); // RegA
                objProgram.add(operands[1]); // RegB
                objProgram.add("&" + operands[2]); // Memória/label
                break;
            case Opcodes.CALL:
                if (operands.length != 1) throw new IllegalArgumentException("Comando CALL espera 1 operando de endereço.");
                objProgram.add("&" + operands[0]);
                break;
            case Opcodes.IMUL:
                // Assumindo imul %regA %regB, como no seu Opcodes.java
                if (operands.length != 2) throw new IllegalArgumentException("Comando IMUL espera 2 operandos de registrador.");
                objProgram.add(operands[0]); // RegA
                objProgram.add(operands[1]); // RegB
                break;
            // Adicionar outros casos conforme a implementação na Architecture.java
            default:
                throw new IllegalArgumentException("Comando " + command + " não suportado ou com formato de operando inválido.");
        }
    }

    /**
     * Helper para pegar os tokens de operando.
     * @param allTokens Todos os tokens da linha.
     * @param startIndex O índice a partir do qual os operandos começam.
     * @return Um array de strings com os operandos.
     */
    private String[] getRemainingTokens(String[] allTokens, int startIndex) {
        if (allTokens.length <= startIndex) {
            return new String[0];
        }
        String[] operands = new String[allTokens.length - startIndex];
        System.arraycopy(allTokens, startIndex, operands, 0, operands.length);
        return operands;
    }


    /**
     * This method uses the tokens to search a command
     * in the commands list and returns its id.
     * @param commandToken The first token of the line, which is the command name.
     * @return The command ID (opcode), or -1 if not found.
     */
    private int findCommandNumber(String commandToken) {
        // Usa Opcodes.getOpcode diretamente
        Integer opcode = Opcodes.getOpcode(commandToken.toLowerCase());
        return (opcode != null) ? opcode : -1;
    }

    /**
     * This method creates the executable program from the object program.
     * Steps:
     * 1. Check if all variables and labels are declared.
     * 2. Allocate memory addresses for variables (from the end of memory, decreasing).
     * 3. Replace labels and variables with their corresponding memory addresses.
     * 4. Replace register names with their IDs.
     * 5. Save the executable file.
     * @param filenameWithoutExtension 
     * @throws IOException 
     */
    public void makeExecutable(String filenameWithoutExtension) throws IOException {
        if (!checkLabelsAndVariables()) { // Renomeado para maior clareza
            System.err.println("Montagem falhou devido a labels/variáveis não declaradas.");
            return;
        }

        // Clona o programa objeto para começar a gerar o executável
        execProgram = new ArrayList<>(objProgram); 

        // Aloca endereços para as variáveis no final da memória e as substitui no código
        allocateAndReplaceVariables();

        // Substitui labels por seus endereços reais no código
        replaceLabelsInExecutable(); 

        // Substitui nomes de registradores por seus IDs no código
        replaceRegistersInExecutable(); 

        // Salva o programa executável no arquivo .dxf
        saveExecFile(filenameWithoutExtension);
        System.out.println("Montagem concluída. Arquivo executável gerado: " + filenameWithoutExtension + ".dxf");
    }

    /**
     * Checks if all labels and variables referenced in the object program are declared.
     * @return true if all references are valid, false otherwise.
     */
    protected boolean checkLabelsAndVariables() { // Renomeado de checkLabels()
        System.out.println("Verificando labels e variáveis...");
        for (String item : objProgram) {
            if (item.startsWith("&")) { // É uma referência a label ou variável
                String name = item.substring(1); // Remove o '&'
                if (!labelAddresses.containsKey(name) && !variables.contains(name)) { // Assume `variables` lista apenas nomes
                    System.err.println("ERRO FATAL: Variável ou label '" + name + "' referenciada, mas não declarada!");
                    return false;
                }
            }
            // Verifica também registradores, se não forem IDs numéricos ainda
            if (item.startsWith(Opcodes.REG_PREFIX)) { // ex: %REG0
                 try {
                     Opcodes.getRegisterId(item.toLowerCase());
                 } catch (IllegalArgumentException e) {
                     System.err.println("ERRO FATAL: Nome de registrador inválido: " + item);
                     return false;
                 }
            }
        }
        System.out.println("Verificação de labels e variáveis concluída com sucesso.");
        return true;
    }


    /**
     * Allocates memory addresses for variables starting from the end of memory
     * and replaces their names with addresses in the `execProgram`.
     */
    protected void allocateAndReplaceVariables() {
        // Calcula o endereço de início das variáveis
        // O último endereço da memória é memorySize - 1.
        // A primeira variável estará em memorySize - 1, a segunda em memorySize - 2, etc.
        int currentVariableAddress = memorySize - 1; 

        for (String varName : variables) {
            variableAddresses.put(varName, currentVariableAddress);
            currentVariableAddress--; // Decrementa para a próxima variável
        }

        // Agora substitui as referências a variáveis no `execProgram` pelos seus endereços
        for (int i = 0; i < execProgram.size(); i++) {
            String item = execProgram.get(i);
            if (item.startsWith("&")) { // Se for uma referência a endereço ou label
                String name = item.substring(1);
                if (variableAddresses.containsKey(name)) { // Se é uma variável
                    execProgram.set(i, Integer.toString(variableAddresses.get(name)));
                }
                // Labels são tratadas em replaceLabelsInExecutable()
            }
        }
        System.out.println("Variáveis alocadas: " + variableAddresses);
    }

    /**
     * Replaces all labels in the `execProgram` with their corresponding memory addresses.
     */
    protected void replaceLabelsInExecutable() {
        for (int i = 0; i < execProgram.size(); i++) {
            String item = execProgram.get(i);
            if (item.startsWith("&")) { // Se for uma referência a label
                String labelName = item.substring(1);
                if (labelAddresses.containsKey(labelName)) {
                    execProgram.set(i, Integer.toString(labelAddresses.get(labelName)));
                } else {
                    // Isso não deveria acontecer se `checkLabelsAndVariables` passou
                    throw new IllegalStateException("Erro interno: Label '" + labelName + "' não resolvida, mas esperada.");
                }
            }
        }
    }

    /**
     * Replaces all register names (e.g., "%reg0") with their corresponding IDs (e.g., "0").
     */
    protected void replaceRegistersInExecutable() {
        for (int i = 0; i < execProgram.size(); i++) {
            String item = execProgram.get(i);
            if (item.startsWith(Opcodes.REG_PREFIX)) { // Se começar com '%reg'
                try {
                    int regId = Opcodes.getRegisterId(item.toLowerCase());
                    execProgram.set(i, Integer.toString(regId));
                } catch (IllegalArgumentException e) {
                    // Isso não deveria acontecer se `checkLabelsAndVariables` passou
                    throw new IllegalStateException("Erro interno: Registrador inválido '" + item + "'", e);
                }
            }
        }
    }

    /**
     * Saves the `execProgram` to the output file (.dxf).
     * @param filenameWithoutExtension The base filename.
     * @throws IOException If there is an error writing the file.
     */
    private void saveExecFile(String filenameWithoutExtension) throws IOException {
        File file = new File(filenameWithoutExtension + ".dxf");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String l : execProgram) {
                writer.write(l + "\n");
            }
            writer.write("-1\n"); // -1 is a flag indicating that the program is finished for Architecture.readExec()
        }
    }

    // --- Métodos de TDD (mantidos) ---
    // Você tinha `getLabels()`, `getLabelsAddresses()`, etc.
    // Adaptei `getLabelsAddresses()` e `getVariablesAddresses()` para os Maps.

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Uso: java assembler.Assembler <nome_do_arquivo_assembly_sem_extensao>");
            System.out.println("Exemplo: java assembler.Assembler program");
            return;
        }

        String filename = args[0];
        // O tamanho da memória deve ser conhecido pelo Assembler para alocar variáveis.
        // Isso pode vir de uma configuração ou ser um valor padrão.
        // Por agora, vou usar um valor fixo.
        // A CPU/Architecture também terá seu próprio tamanho de memória.
        // É importante que eles sejam compatíveis.
        int defaultMemorySize = 128; // Use o mesmo tamanho que sua Architecture.java
                                     // Ou o loader deve receber o memorySize da Architecture.
                                     // Para este teste, vamos assumir 128.
                                     // O ideal é que Architecture.getMemorySize() seja usado.

        Assembler assembler = new Assembler(defaultMemorySize); 
        System.out.println("Lendo arquivo assembly de origem: " + filename + ".dsf");
        assembler.read(filename);
        System.out.println("Gerando o programa objeto...");
        assembler.parse();
        System.out.println("Gerando executável: " + filename + ".dxf");
        assembler.makeExecutable(filename);
    }
}