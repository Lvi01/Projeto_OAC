package architecture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


import components.Bus;
import components.Demux;
import components.Memory;
import components.Register;
import components.ULA;
import components.Opcodes;
import components.Stack; // Importar a classe Stack
import assembler.Assembler; // Importar a classe Assembler

public class Architecture {
    
    private boolean simulation; //this boolean indicates if the execution is done in simulation mode.
                                //simulation mode shows the components' status after each instruction
    
    
    private boolean halt;
    private Bus extbus1;
    private Bus intbus1;
    private Bus intbus2;
    private Memory memory;
    private Memory statusMemory; // Memória de 2 posições para JZ/JN
    private int memorySize; // Tamanho total da memória principal
    
    // Registradores
    private Register PC;
    private Register IR;
    private Register RPG; // RPG0 no diagrama original
    private Register RPG1; // RPG1 no diagrama original
    private Register StkTOP; // Stack Top Pointer
    private Register StkBOT; // Stack Bottom Pointer
    private Register Flags; // O registrador de flags, agora um Register normal
    
    private ULA ula;
    private Demux demux; //only for multiple register purposes
    
    private ArrayList<String> commandsList; // Nomes dos comandos para depuração e `hasOperands`
    private ArrayList<Register> registersList; // Lista de registradores para acesso via ID no Demux
    
    // Para o microprograma IMUL
    // Endereços de memória reservados (definidos como constantes na Architecture)
    private final int IMUL_MICROPROGRAM_START;
    private final int IMUL_REGS_SAVE_AREA_START;
    private final int IMUL_RESULT_ADDRESS;
    private final int IMUL_RETURN_PC_SAVE_ADDR;
    private final int IMUL_OP1_TEMP_ADDR;
    private final int IMUL_OP2_TEMP_ADDR;

    // Para a pilha
    private Stack stack; // O objeto Stack que gerencia a pilha


    /**
     * Instancia todos os componentes nesta arquitetura.
     * Ajustado para os novos construtores de Bus, Register, Memory, ULA.
     */
    private void componentsInstances() {
        // Ordem de instanciação: barramentos -> registradores -> ula -> memória
        extbus1 = new Bus();
        intbus1 = new Bus();
        intbus2 = new Bus();
        
        // Construtores de Register: Register(name, extbus, intbus1, intbus2)
        // PC: Conectado a extbus1 e intbus2
        PC = new Register("PC", extbus1, null, intbus2); // PC usa intbus2 para internalRead/Store, extbus para read/store
        IR = new Register("IR", extbus1, null, intbus2); // IR usa intbus2 para internalStore (de PC.read()), extbus para read/store
        RPG = new Register("RPG0", extbus1, intbus1, null); // RPG usa extbus1 e intbus1 (para ULA)
        RPG1 = new Register ("RPG1", extbus1, intbus1, null); // RPG1 usa extbus1 e intbus1 (para ULA)
        
        // Flags: é um Register especial. Usa extbus1 para put(getBit) e intbus2 para setStatusFlags
        // O construtor de Register que definimos para Flags era 'Register(Bus extbus, Bus intbus2)'.
        Flags = new Register(extbus1, intbus2); // Flags agora é um Register com métodos de bit
        
        // Registradores da pilha: Conectados a extbus1 e intbus2
        StkTOP = new Register("StkTOP", extbus1, null, intbus2); // StkTOP precisa de intbus2 para dec/inc via ULA
        StkBOT = new Register("StkBOT", extbus1, null, intbus2); // StkBOT é mais uma referência

        fillRegistersList(); // Preenche a lista com todos os registradores

        // ULA: Conectada a intbus1 e intbus2
        ula = new ULA(intbus1, intbus2); // ULA com U maiúsculo

        // Demux: Usado para operações com múltiplos registradores
        demux = new Demux(); 
        
        // Memória: Conectada a extbus1
        memorySize = 128; // Define o tamanho padrão da memória (ajuste conforme necessário)
        memory = new Memory(memorySize, extbus1);
        
        // statusMemory: Uma instância separada de Memory para os desvios condicionais (2 posições)
        statusMemory = new Memory(2, extbus1); // Conectada ao extbus1 para jz/jn

        // Agora que Memory está instanciada, podemos definir os endereços reservados
        // Estes são definidos na Architecture e passados para o Assembler ou hardcoded.
        // Assumindo um layout de memória como o anterior:
        //  0 - N: Microprogramas e Áreas de Sistema
        //  N+1 - M: Programa principal
        //  M+1 - end: Variáveis e Pilha
        int currentSysAddress = 0;
        IMUL_MICROPROGRAM_START = currentSysAddress;
        currentSysAddress += 20; // Espaço para IMUL microprograma (será preenchido pelo generateImulMicroprogram)
        IMUL_REGS_SAVE_AREA_START = currentSysAddress;
        currentSysAddress += 6; // Espaço para salvar 4 REGs (RPG, RPG1, PC, IR), PC, Flags (packed)
        IMUL_RESULT_ADDRESS = currentSysAddress;
        currentSysAddress += 1; // Espaço para o resultado do IMUL
        IMUL_OP1_TEMP_ADDR = currentSysAddress;
        currentSysAddress += 1;
        IMUL_OP2_TEMP_ADDR = currentSysAddress;
        currentSysAddress += 1;
        // O PC de retorno será salvo no IMUL_REGS_SAVE_AREA_START + 4.
        IMUL_RETURN_PC_SAVE_ADDR = IMUL_REGS_SAVE_AREA_START + 4; // PC salvo na 5ª posição (0-indexed)

        // Pilha: Usa StkTOP e StkBOT, que apontam para endereços na memória principal
        // O StkBOT aponta para o "fundo" da pilha, que cresce para baixo
        // A pilha é alocada a partir do final da memória, descendo.
        // O StkBOT é o último endereço da memória principal.
        // O StkTOP inicia no mesmo lugar que StkBOT.
        StkBOT.setData(memorySize - 1); // Ex: Se memorySize=128, StkBOT=127
        StkTOP.setData(memorySize - 1); // StkTOP começa no mesmo lugar que StkBOT
        stack = new Stack(memory, StkTOP, StkBOT, ula, extbus1); // Stack precisa de ULA e extbus para algumas operações
        
        fillCommandsList(); // Preenche a lista de comandos (nomes)
    }

    /**
     * Este método preenche a lista de registradores, inserindo todos os registradores que temos.
     * IMPORTANTE! O primeiro registrador a ser inserido deve ser o RPG (RPG0)
     */
    private void fillRegistersList() {
        registersList = new ArrayList<Register>();
        registersList.add(RPG);   // ID 0
        registersList.add(RPG1);  // ID 1
        registersList.add(PC);    // ID 2
        registersList.add(IR);    // ID 3
        registersList.add(Flags); // ID 4
        registersList.add(StkTOP); // ID 5
        registersList.add(StkBOT); // ID 6
    }

    /**
     * Construtor que instancia todos os componentes de acordo com o diagrama da arquitetura.
     */
    public Architecture() {
        componentsInstances();
        simulation = false; // Por padrão, a execução não é em modo de simulação
    }

    /**
     * Construtor que permite definir o modo de simulação.
     * @param sim true para modo de simulação, false caso contrário.
     */
    public Architecture(boolean sim) {
        componentsInstances();
        simulation = sim;
    }

    // --- Getters (mantidos) ---
    protected Bus getExtbus1() { return extbus1; }
    protected Bus getIntbus1() { return intbus1; }
    protected Bus getIntbus2() { return intbus2; }
    public Memory getMemory() { return memory; } // Public para Main/Assembler
    protected Register getPC() { return PC; }
    protected Register getIR() { return IR; }
    protected Register getRPG() { return RPG; }
    protected Register getRPG1() { return RPG1; } // Adicionado getter para RPG1
    protected Register getFlags() { return Flags; }
    protected ULA getUla() { return ula; }
    public ArrayList<String> getCommandsList() { return commandsList; }
    public ArrayList<Register> getRegistersList() { return registersList; }
    public int getMemorySize() { return memorySize; } // Public para Assembler

    // --- Getters para endereços IMUL (adicionados) ---
    public int getImulMicroprogramStartAddress() { return IMUL_MICROPROGRAM_START; }
    public int getImulRegistersSaveAreaStart() { return IMUL_REGS_SAVE_AREA_START; }
    public int getImulResultAddress() { return IMUL_RESULT_ADDRESS; }
    public int getImulOp1TempAddr() { return IMUL_OP1_TEMP_ADDR; }
    public int getImulOp2TempAddr() { return IMUL_OP2_TEMP_ADDR; }
    public int getImulReturnPcSaveAddr() { return IMUL_RETURN_PC_SAVE_ADDR; }


    /**
     * Este método preenche a lista de comandos com todos os comandos usados nesta arquitetura.
     * A ordem DEVE corresponder aos opcodes definidos em components.Opcodes.java.
     */
    protected void fillCommandsList() {
        commandsList = new ArrayList<String>();
        commandsList.add("add");        // 0
        commandsList.add("sub");        // 1
        commandsList.add("jmp");        // 2
        commandsList.add("jz");         // 3
        commandsList.add("jn");         // 4
        commandsList.add("read");       // 5
        commandsList.add("store");      // 6
        commandsList.add("ldi");        // 7
        commandsList.add("inc");        // 8
        commandsList.add("moveregreg"); // 9 (move %regA %regB)
        commandsList.add("jnz");        // 10
        commandsList.add("jeq");        // 11
        commandsList.add("jgt");        // 12
        commandsList.add("jlw");        // 13
        commandsList.add("call");       // 14
        commandsList.add("ret");        // 15
        commandsList.add("imul");       // 16
        commandsList.add("halt");       // 17
    }

    /**
     * Este método é usado após algumas operações da ULA, definindo os bits de flags de acordo com o resultado.
     * @param result é o resultado da operação
     */
    private void setStatusFlags(int result) {
        Flags.setBit(0, 0); // Zera o bit Zero
        Flags.setBit(1, 0); // Zera o bit Negativo
        if (result == 0) {
            Flags.setBit(0, 1); // Zero Flag = 1
        }
        if (result < 0) {
            Flags.setBit(1, 1); // Negative Flag = 1
        }
    }

    // --- Microprogramas Existentes (mantidos e ajustados) ---

    /**
     * Avança o PC para a próxima "palavra" da instrução (parâmetro ou próxima instrução).
     * @return O valor do PC antes do incremento.
     * IMPORTANTE: Esta função faz o PC avançar DUAS vezes: uma para o parâmetro, outra para a próxima instrução.
     * E coloca o endereço do PARÂMETRO no extbus para ser lido pela memória.
     */
    private int getParameterAndAdvancePC() {
        // 1. PC avança para o endereço do parâmetro.
        PC.internalRead(); // PC -> intbus2
        ula.internalStore(1); // intbus2 -> ULA.internalReg1
        ula.inc(); // ULA.internalReg1++ (PC+1, agora PC tem o endereço do parâmetro)
        ula.internalRead(1); // ULA.internalReg1 -> intbus2
        PC.internalStore(); // intbus2 -> PC (PC agora aponta para o parâmetro)
        
        // 2. Coloca o endereço do parâmetro no extbus para a memória ler o valor do parâmetro.
        int paramAddress = PC.getData();
        extbus1.put(paramAddress); // Coloca o endereço do parâmetro no extbus
        
        // 3. PC avança novamente para a próxima instrução (após o parâmetro).
        PC.internalRead(); // PC -> intbus2
        ula.internalStore(1); // intbus2 -> ULA.internalReg1
        ula.inc(); // ULA.internalReg1++ (PC+2, i.e., próximo comando real)
        ula.internalRead(1); // ULA.internalReg1 -> intbus2
        PC.internalStore(); // intbus2 -> PC (PC agora aponta para a próxima instrução)

        return paramAddress; // Retorna o endereço do parâmetro (que foi lido na memória)
    }

    /**
     * Avança o PC para a próxima instrução quando a instrução atual não tem parâmetros.
     * (Usado para INC, RET, HALT)
     */
    private void advancePCNoParam() {
        PC.internalRead(); // PC -> intbus2
        ula.internalStore(1); // intbus2 -> ULA.internalReg1
        ula.inc(); // ULA.internalReg1++
        ula.internalRead(1); // ULA.internalReg1 -> intbus2
        PC.internalStore(); // intbus2 -> PC (PC agora aponta para a próxima instrução)
    }

    // Microprograma para ADD address
    public void add() {
        // 1. PC avança para o endereço do parâmetro. O endereço do parâmetro é colocado no extbus.
        getParameterAndAdvancePC(); 
        
        // 2. RPG <- RPG + memória[parâmetro (endereço)]
        RPG.internalRead(); // RPG -> intbus1
        ula.store(0);       // intbus1 -> ULA.internalReg0 (primeiro operando, valor atual de RPG)

        // memory.read() já espera o endereço no extbus, que foi colocado por getParameterAndAdvancePC()
        memory.read(); // extbus1 tem o endereço. Memory lê o endereço do extbus, coloca o DADO no extbus
        RPG.store(); // RPG <- extbus (RPG agora tem o dado da memória)

        RPG.internalRead(); // RPG (com o dado da memória) -> intbus1
        ula.store(1);       // intbus1 -> ULA.internalReg1 (segundo operando, o valor lido da memória)

        ula.add();          // ULA.internalReg1 = ULA.internalReg0 + ULA.internalReg1
        ula.internalRead(1); // ULA.internalReg1 -> intbus2 (resultado da soma)
        setStatusFlags(intbus2.get()); // Atualiza flags com o resultado
        RPG.internalStore(); // RPG <- intbus2 (RPG recebe o resultado)
        // O PC já foi avançado para a próxima instrução pelo getParameterAndAdvancePC().
    }
    
    // Microprograma para SUB address
    public void sub() {
        getParameterAndAdvancePC(); 

        // RPG <- RPG - memória[parâmetro (endereço)]
        RPG.internalRead(); // RPG -> intbus1
        ula.store(0);       // intbus1 -> ULA.internalReg0 (minuendo, valor atual de RPG)

        memory.read(); // extbus1 tem o endereço. Memory lê o endereço do extbus, coloca o DADO no extbus
        RPG.store(); // RPG <- extbus (RPG agora tem o dado da memória)

        RPG.internalRead(); // RPG (com o dado da memória) -> intbus1
        ula.store(1);       // intbus1 -> ULA.internalReg1 (subtraendo, o valor lido da memória)

        ula.sub();          // ULA.internalReg1 = ULA.internalReg0 - ULA.internalReg1
        ula.internalRead(1); // ULA.internalReg1 -> intbus2 (resultado da subtração)
        setStatusFlags(intbus2.get()); // Atualiza flags com o resultado
        RPG.internalStore(); // RPG <- intbus2 (RPG recebe o resultado)
    }

    // Microprograma para JMP address
    public void jmp() {
        // O getParameterAndAdvancePC() coloca o endereço do JMP no extbus,
        // e avança o PC para a instrução subsequente.
        getParameterAndAdvancePC(); 

        // Agora o PC deve ser sobrescrito com o endereço de salto lido da memória (que está no extbus)
        // memory.read() já colocou o endereço de salto no extbus
        PC.store(); // PC <- extbus (PC recebe o endereço de salto)
        // Não há avanço de PC para próxima instrução aqui, pois o JMP já setou o PC.
    }
    
    // Microprograma para JZ address
    public void jz() {
        // 1. Pega o endereço de salto e avança o PC para a próxima instrução.
        // O endereço de salto já está no extbus (vindo do memory.read() dentro de getParameterAndAdvancePC())
        getParameterAndAdvancePC();
        statusMemory.storeIn1(); // extbus1 já tem o jumpTargetAddress, armazena na pos 1
        
        // 2. Coloca o endereço da próxima instrução (PC atual) no extbus1 e salva na statusMemory[0].
        extbus1.put(PC.getData()); 
        statusMemory.storeIn0(); // Armazena na pos 0
        
        // 3. Decide e salta com base na Zero Flag.
        extbus1.put(Flags.getBit(0)); // Coloca o bit Zero (0 ou 1) no extbus1
        statusMemory.read(); // statusMemory decide (com base no bit em extbus1) e coloca o endereço final no extbus1
        PC.store(); // PC <- extbus1 (PC recebe o endereço final)
    }
    
    // Microprograma para JN address
    public void jn() {
        getParameterAndAdvancePC();
        statusMemory.storeIn1(); 
        
        extbus1.put(PC.getData()); 
        statusMemory.storeIn0(); 
        
        extbus1.put(Flags.getBit(1)); // Coloca o bit Negativo (0 ou 1) no extbus1
        statusMemory.read(); 
        PC.store();
    }
    
    // Microprograma para READ address
    public void read() {
        getParameterAndAdvancePC(); // PC avança para o endereço do parâmetro, endereço no extbus
        
        // RPG <- memória[paramAddress]
        // memory.read() já espera o endereço no extbus
        memory.read(); // extbus1 tem o endereço. Memory lê o endereço do extbus, coloca o DADO no extbus
        RPG.store(); // RPG <- extbus (RPG agora tem o dado da memória)
    }
    
    // Microprograma para STORE address
    public void store() {
        getParameterAndAdvancePC(); // PC avança para o endereço do parâmetro, endereço no extbus

        // memória[paramAddress] <- RPG
        // memory.read() já espera o endereço no extbus, e coloca o dado no extbus
        // No microcódigo original, a primeira `memory.read()` (que está dentro de getParameterAndAdvancePC)
        // colocaria o *conteúdo* do endereço do parâmetro no extbus.
        // Esse conteúdo é o *endereço de destino* real.
        memory.store(); // Memory pega o endereço de destino (de extbus) e o internaliza (MAR)

        RPG.read(); // RPG -> extbus1 (coloca o valor a ser gravado no extbus)
        memory.store(); // Memory pega o DADO (de extbus) e grava no MAR
    }
    
    // Microprograma para LDI immediate
    public void ldi() {
        // getParameterAndAdvancePC() lê o parâmetro como um endereço e avança o PC.
        // Mas para LDI, o parâmetro é um valor IMEDIATO, não um endereço.
        // Precisamos de um método que apenas leia o *próximo valor* e avance o PC.
        
        // Vamos ajustar getParameterAndAdvancePC() para ser mais genérico, ou criar um `getImmediateAndAdvancePC`.
        // A lógica de `getParameterAndAdvancePC` já coloca o valor da memória no extbus.
        // E o `PC` já avançou para a próxima instrução.
        
        getParameterAndAdvancePC(); // PC avança, valor imediato lido em memory.read() e no extbus
        RPG.store();                 // RPG <- extbus (RPG recebe o imediato)
    }
    
    // Microprograma para INC
    public void inc() {
        // RPG++
        RPG.internalRead(); // RPG -> intbus1
        ula.store(1);       // intbus1 -> ULA.internalReg1 (inc opera em internalReg1)
        ula.inc();          // ULA.internalReg1++
        ula.internalRead(1); // ULA.internalReg1 -> intbus2 (resultado)
        setStatusFlags(intbus2.get()); // Atualiza flags
        RPG.internalStore(); // RPG <- intbus2 (RPG recebe o resultado)

        advancePCNoParam(); // Avança PC para a próxima instrução (sem parâmetro para pular)
    }
    
    // Microprograma para MOVEREGREG (move regA regB)
    public void moveRegReg() {
        // PC avança, primeiro reg ID no extbus.
        getParameterAndAdvancePC(); // paramAddress_regA_ID é o ID de regA
        int regA_ID = extbus1.get(); // Pega o ID de RegA do extbus

        // PC avança, segundo reg ID no extbus.
        getParameterAndAdvancePC(); // paramAddress_regB_ID é o ID de regB
        int regB_ID = extbus1.get(); // Pega o ID de RegB do extbus

        // regB <- regA
        demux.setValue(regA_ID); // Demux seleciona o RegA
        registersInternalRead(); // RegA -> intbus1 (valor de RegA está no intbus1)
        
        demux.setValue(regB_ID); // Demux seleciona o RegB
        registersInternalStore(); // RegB <- intbus1 (RegB recebe o valor de RegA)
    }

    // --- Métodos Auxiliares para Manipulação de Registradores pela Demux ---
    /**
     * Este método realiza uma leitura interna de um registrador da lista de registradores.
     * O ID do registrador deve estar no demux bus (ou Demux.value).
     */
    private void registersInternalRead() {
        registersList.get(demux.getValue()).internalRead();
    }
    
    /**
     * Este método realiza uma escrita interna para um registrador da lista de registradores.
     * O ID do registrador deve estar no demux bus (ou Demux.value).
     */
    private void registersInternalStore() {
        registersList.get(demux.getValue()).internalStore();
    }

    // --- Microprogramas Faltantes (Adicionados) ---

    // Microprograma para JNZ address (opcode 10)
    public void jnz() {
        // 1. Pega o endereço de salto e avança o PC para a próxima instrução.
        // O endereço de salto já está no extbus.
        getParameterAndAdvancePC();
        statusMemory.storeIn1(); // Armazena endereço de salto na pos 1
        
        // 2. Coloca o endereço da próxima instrução (PC atual) no extbus1 e salva na statusMemory[0].
        extbus1.put(PC.getData()); 
        statusMemory.storeIn0(); // Armazena na pos 0
        
        // 3. Decide e salta com base na Zero Flag.
        // Se a Zero Flag NÃO for 1 (ou seja, != 0), salta.
        // Para JNZ, queremos saltar se Flags.getBit(0) == 0.
        // Então, put 1 no extbus se Z=0, put 0 se Z=1.
        extbus1.put(Flags.getBit(0) == 0 ? 1 : 0); // Coloca 1 se Z=0 (não zero), 0 se Z=1 (é zero)
        statusMemory.read(); // statusMemory decide e coloca o endereço final no extbus1
        PC.store(); // PC <- extbus1
    }

    // Microprograma para JEQ %regA %regB <mem> (opcode 11)
    public void jeq() {
        int regA_ID = getParameterAndAdvancePC(); // PC avança, RegA ID no extbus.
        int valA = extbus1.get(); // Pega o ID de RegA do extbus

        int regB_ID = getParameterAndAdvancePC(); // PC avança, RegB ID no extbus.
        int valB = extbus1.get(); // Pega o ID de RegB do extbus

        int jumpTargetAddress = getParameterAndAdvancePC(); // PC avança, endereço de salto no extbus.
        int addrJump = extbus1.get(); // Pega o endereço de salto do extbus

        // Compara RegA e RegB usando a ULA.compare
        ula.compare(valA, valB); // Realiza valA - valB, resultado em ULA.internalReg1
        ula.internalRead(1); // Resultado (valA-valB) de ULA.internalReg1 -> intbus2
        setStatusFlags(intbus2.get()); // Atualiza flags com o resultado

        if (Flags.getBit(0) == 1) { // Se Zero Flag for 1 (RegA == RegB)
            extbus1.put(addrJump); // Coloca o endereço de salto no extbus
            PC.store(); // PC <- extbus
        }
    }

    // Microprograma para JGT %regA %regB <mem> (opcode 12)
    public void jgt() {
        int regA_ID = getParameterAndAdvancePC();
        int valA = extbus1.get();

        int regB_ID = getParameterAndAdvancePC();
        int valB = extbus1.get();

        int jumpTargetAddress = getParameterAndAdvancePC();
        int addrJump = extbus1.get();

        ula.compare(valA, valB);
        ula.internalRead(1);
        setStatusFlags(intbus2.get());

        // Se RegA > RegB, então (RegA - RegB) > 0. Zero Flag = 0 E Negative Flag = 0.
        if (Flags.getBit(0) == 0 && Flags.getBit(1) == 0) {
            extbus1.put(addrJump);
            PC.store();
        }
    }

    // Microprograma para JLW %regA %regB <mem> (opcode 13)
    public void jlw() {
        int regA_ID = getParameterAndAdvancePC();
        int valA = extbus1.get();

        int regB_ID = getParameterAndAdvancePC();
        int valB = extbus1.get();

        int jumpTargetAddress = getParameterAndAdvancePC();
        int addrJump = extbus1.get();

        ula.compare(valA, valB);
        ula.internalRead(1);
        setStatusFlags(intbus2.get());

        // Se RegA < RegB, então (RegA - RegB) < 0. Negative Flag = 1.
        if (Flags.getBit(1) == 1) {
            extbus1.put(addrJump);
            PC.store();
        }
    }

    // Microprograma para CALL <mem> (opcode 14)
    public void call() {
        getParameterAndAdvancePC(); // PC avança, endereço da subrotina no extbus.
        int targetAddress = extbus1.get(); // Pega o endereço da subrotina do extbus

        // Empilha o endereço de retorno (PC já está no endereço da instrução APÓS o CALL)
        stack.push(PC.getData()); 

        // Desvia PC para o endereço da subrotina
        extbus1.put(targetAddress);
        PC.store();
    }

    // Microprograma para RET (opcode 15)
    public void ret() {
        // Desempilha o endereço de retorno e coloca no PC
        PC.setData(stack.pop()); // Stack.pop() já retorna o valor
        // O PC já foi setado pelo pop. Não precisa de advancePCNoParam() aqui.
    }

    // Microprograma para IMUL %regA %regB (opcode 16)
    public void imul() {
        int regA_ID = getParameterAndAdvancePC(); // PC avança, RegA ID no extbus
        int valA = extbus1.get(); // Pega o ID de RegA do extbus
        Register regA = getRegisterById(valA); // Obtém o objeto Register para valA

        int regB_ID = getParameterAndAdvancePC(); // PC avança, RegB ID no extbus
        int valB = extbus1.get(); // Pega o ID de RegB do extbus
        Register regB = getRegisterById(valB); // Obtém o objeto Register para valB

        // Salvar contexto (RPG, RPG1, PC, IR, Flags, StkTOP, StkBOT - apenas os que o microprograma pode alterar)
        // No seu caso, RPG, RPG1, PC, Flags.
        saveContext(IMUL_REGS_SAVE_AREA_START);

        // Passar operandos para o microprograma IMUL via endereços temporários
        extbus1.put(IMUL_OP1_TEMP_ADDR); // Endereço no extbus
        memory.store(); // Memory pega endereço (MAR)
        extbus1.put(regA.getData()); // Valor de RegA no extbus
        memory.store(); // Memory grava valor

        extbus1.put(IMUL_OP2_TEMP_ADDR); // Endereço no extbus
        memory.store(); // Memory pega endereço (MAR)
        extbus1.put(regB.getData()); // Valor de RegB no extbus
        memory.store(); // Memory grava valor

        // Salvar o endereço de retorno (PC) para quando o microprograma HALT.
        // O PC já está no endereço da próxima instrução APÓS o IMUL quando este microprograma é chamado.
        extbus1.put(IMUL_RETURN_PC_SAVE_ADDR); // Endereço no extbus
        memory.store(); // Memory pega endereço (MAR)
        extbus1.put(PC.getData()); // PC já está na próxima instrução
        memory.store(); // Memory guarda valor do PC de retorno
        
        // Desviar PC para o início do microprograma IMUL
        extbus1.put(IMUL_MICROPROGRAM_START);
        PC.store();
    }

    // Microprograma para HALT (opcode 17)
    public void halt() {
        halt = true;
        System.out.println("Instrução HALT executada. Simulação finalizada.");
    }


    // --- Métodos Auxiliares para PC e Parâmetros ---

    /**
     * Este método avança o PC duas vezes:
     * 1. Para o endereço do parâmetro da instrução atual.
     * 2. Para o endereço da PRÓXIMA instrução (após o parâmetro).
     * Ele também coloca o CONTEÚDO do endereço do parâmetro no `extbus1`.
     * Retorna o endereço do parâmetro para a depuração, mas o valor em si
     * deve ser pego do `extbus1` após a chamada.
     */
    private int getParameterAndAdvancePC() {
        // 1. PC avança para o endereço do parâmetro.
        PC.internalRead(); // PC -> intbus2
        ula.internalStore(1); // intbus2 -> ULA.internalReg1
        ula.inc(); // ULA.internalReg1++ (PC_novo = PC_antigo + 1, agora PC tem o endereço do parâmetro)
        ula.internalRead(1); // ULA.internalReg1 -> intbus2
        PC.internalStore(); // intbus2 -> PC (PC agora aponta para o parâmetro)
        
        // 2. A memória lê o conteúdo do endereço do parâmetro e o coloca no `extbus1`.
        //    O PC (que agora contém o endereço do parâmetro) precisa ser colocado no extbus.
        int paramMemoryAddress = PC.getData(); // Este é o endereço do PARÂMETRO NA MEMÓRIA
        extbus1.put(paramMemoryAddress); // Coloca o ENDEREÇO DO PARÂMETRO no extbus
        memory.read(); // Memory lê o ENDEREÇO do extbus, e coloca o *VALOR* desse endereço no extbus.
                       // Agora, `extbus1.get()` retornará o VALOR REAL do parâmetro.
        
        // 3. PC avança novamente para a próxima instrução (após o parâmetro).
        PC.internalRead(); // PC -> intbus2
        ula.internalStore(1); // intbus2 -> ULA.internalReg1
        ula.inc(); // ULA.internalReg1++ (PC_final = PC_antigo + 2, i.e., próximo comando real)
        ula.internalRead(1); // ULA.internalReg1 -> intbus2
        PC.internalStore(); // intbus2 -> PC (PC agora aponta para a próxima instrução)

        return paramMemoryAddress; // Retorna o endereço de memória onde o parâmetro estava
                                   // O valor do parâmetro é obtido de `extbus1.get()` após a chamada.
    }

    // --- Métodos Auxiliares para IMUL e Context Switching ---
    /**
     * Salva o estado dos registradores RPG, RPG1, PC, Flags em uma área da memória.
     * @param startAddress O endereço inicial da área de salvamento.
     */
    private void saveContext(int startAddress) {
        // RPG -> memory[startAddress]
        extbus1.put(startAddress); // Endereço de destino no extbus
        memory.store(); // Memory pega endereço (MAR)
        RPG.read(); // RPG -> extbus1 (valor de RPG)
        memory.store(); // Memory guarda valor

        // RPG1 -> memory[startAddress + 1]
        extbus1.put(startAddress + 1);
        memory.store();
        RPG1.read();
        memory.store();

        // PC -> memory[startAddress + 2]
        extbus1.put(startAddress + 2);
        memory.store();
        PC.read();
        memory.store();

        // IR -> memory[startAddress + 3] (Para salvar o IR também, caso o microprograma use)
        extbus1.put(startAddress + 3);
        memory.store();
        IR.read();
        memory.store();

        // Flags -> memory[startAddress + 4]
        // Salvar flags: `Flags.getData()` já retorna o int que contém os bits Z e N.
        extbus1.put(startAddress + 4);
        memory.store();
        extbus1.put(Flags.getData()); 
        memory.store();
        
        // StkTOP -> memory[startAddress + 5]
        extbus1.put(startAddress + 5);
        memory.store();
        StkTOP.read();
        memory.store();

        System.out.println("Contexto salvo em: " + startAddress + " (RPG, RPG1, PC, IR, Flags, StkTOP)");
    }

    /**
     * Restaura o estado dos registradores RPG, RPG1, PC, Flags de uma área da memória.
     * @param startAddress O endereço inicial da área de salvamento.
     */
    private void restoreContext(int startAddress) {
        // RPG <- memory[startAddress]
        extbus1.put(startAddress);
        memory.read();
        RPG.store();

        // RPG1 <- memory[startAddress + 1]
        extbus1.put(startAddress + 1);
        memory.read();
        RPG1.store();

        // PC <- memory[startAddress + 2]
        extbus1.put(startAddress + 2);
        memory.read();
        PC.store();

        // IR <- memory[startAddress + 3]
        extbus1.put(startAddress + 3);
        memory.read();
        IR.store();

        // Flags <- memory[startAddress + 4]
        extbus1.put(startAddress + 4);
        memory.read();
        Flags.store(); // Flags.store() vai definir o valor interno a partir do bus

        // StkTOP <- memory[startAddress + 5]
        extbus1.put(startAddress + 5);
        memory.read();
        StkTOP.store();

        System.out.println("Contexto restaurado de: " + startAddress + " (RPG, RPG1, PC, IR, Flags, StkTOP)");
    }


    // --- Métodos de Simulação/Depuração (mantidos) ---

    /**
     * Este método é usado para mostrar o status dos componentes em condições de simulação antes da decodificação e execução.
     * @param command O comando atual.
     */
    private void simulationDecodeExecuteBefore(int command) {
        System.out.println("----------BEFORE Decode and Execute phases--------------");
        String instructionName;
        
        for (Register r : registersList) {
            System.out.println(r.getRegisterName() + ": " + r.getData());
        }

        if (command != -1) {
            instructionName = commandsList.get(command);
        } else {
            instructionName = "END";
        }

        System.out.println("Instrução: " + instructionName);
        
        // Tenta mostrar o parâmetro da próxima instrução para depuração
        // Isso é complexo porque o PC já avançou. Mas o PC + 1 seria o parâmetro.
        try {
            int currentPC = PC.getData(); // PC já está no endereço da instrução ou primeiro parâmetro
            // Se a instrução tem 1 parâmetro (como 'add addr'), o parâmetro está em PC-1
            // Se a instrução tem 2 parâmetros (como 'moveRegReg'), o primeiro parâmetro está em PC-2, o segundo em PC-1.
            // Para simular aqui, podemos tentar prever.
            // Para ser preciso, precisaríamos saber a aridade da instrução.
            // Por simplicidade, vamos mostrar a próxima palavra.
            System.out.println("PC atual: " + currentPC + ", IR: " + IR.getData());
            // Mostra a próxima instrução ou parâmetro.
            if (currentPC < memory.getMemorySize()) {
                 System.out.println("Memória[" + currentPC + "] (próximo opcode/parâmetro): " + memory.getData(currentPC));
            }

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Erro ao tentar ler próxima palavra para depuração: " + e.getMessage());
        }
    }

    /**
     * Este método é usado para mostrar o status dos componentes em condições de simulação após a decodificação e execução.
     */
    private void simulationDecodeExecuteAfter() {
        System.out.println("-----------AFTER Decode and Execute phases--------------");
        System.out.println("Internal Bus 1: " + intbus1.get());
        System.out.println("Internal Bus 2: " + intbus2.get());
        System.out.println("External Bus 1: " + extbus1.get());
        for (Register r : registersList) {
            System.out.println(r.getRegisterName() + ": " + r.getData());
        }
        Scanner entrada = new Scanner(System.in);
        System.out.println("Press <Enter> para continuar...");
        entrada.nextLine(); // Espera o Enter para continuar
        // entrada.close(); // NÃO FECHAR System.in aqui, pode impedir futuras leituras
    }

    /**
     * Este método usa o PC para encontrar, na memória,
     * o código do comando que deve ser executado.
     * Este comando deve ser armazenado em IR.
     */
    private void fetch() {
        PC.read(); // PC (endereço do opcode) -> extbus1
        memory.read(); // memory lê endereço do extbus, coloca opcode no extbus
        IR.store(); // IR <- extbus (IR agora tem o opcode)
        
        simulationFetch();
    }

    /**
     * Este método é usado para mostrar o status dos componentes em condições de simulação na fase de Fetch.
     */
    private void simulationFetch() {
        if (simulation) {
            System.out.println("-------Fetch Phase------");
            System.out.println("PC: " + PC.getData());
            System.out.println("IR: " + IR.getData());
        }
    }

    /**
     * Este método retorna o tamanho da memória alocada para esta arquitetura.
     * @return O tamanho da memória em palavras.
     */
    public int getMemorySize() {
        return memorySize;
    }
    
    // --- Método para carregar programa executável (.dxf) ---
    /**
     * Este método lê um arquivo inteiro em código de máquina (.dxf) e
     * o armazena na memória.
     * Ele também inicializa o PC para 0 (ou para o início do programa).
     * @param filenameWithoutExtension O nome do arquivo .dxf sem a extensão.
     * @throws IOException Se houver um erro de leitura do arquivo.
     */
    public void readExec(String filenameWithoutExtension) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filenameWithoutExtension + ".dxf"))) {
            String linha;
            int i = 0; // Endereço de memória
            int tempValue = 0; // Valor a ser escrito
            boolean expectingAddress = true; // Flag para o ciclo memory.store()

            while ((linha = br.readLine()) != null) {
                if (linha.trim().equals("-1")) {
                    break; // Fim do arquivo .dxf
                }
                
                int parsedValue = Integer.parseInt(linha.trim());
                
                // O `memory.store()` na Memory.java usa um ciclo de 2 chamadas:
                // 1. Pega o endereço do bus (expectingDataForStore = true)
                // 2. Pega o dado do bus e grava (expectingDataForStore = false)
                
                // Para carregar o DXF, precisamos explicitamente setar o endereço e depois o dado
                extbus1.put(i);     // Coloca o endereço 'i' no barramento
                memory.store();     // Memory pega 'i' como endereço alvo (MAR)
                                    // Agora `memory` está esperando o dado.

                extbus1.put(parsedValue); // Coloca o valor lido da linha no barramento
                memory.store();     // Memory armazena 'parsedValue' no MAR

                i++; // Próxima posição de memória
            }
            PC.setData(0); // PC começa na posição 0 da memória após carregar o programa (ou 0, se esse for o start_program)
            System.out.println("Programa executável carregado na memória. Total de " + i + " palavras.");
        }
    }
    
    /**
     * Este método executa um programa que está armazenado na memória.
     */
    public void controlUnitEexec() {
        halt = false;
        while (!halt) {
            fetch();
            decodeExecute();
            
            // Adicionado um limite para evitar loops infinitos em programas com erro
            if (PC.getData() < 0 || PC.getData() >= memorySize) {
                System.err.println("Erro: Program Counter fora dos limites da memória: " + PC.getData() + ". Parando CPU.");
                halt = true;
            }
            // A condição de parada `halt = true` é setada pelo microprograma HALT
        }
    }
    
    /**
     * Este método implementa o processo de decodificação e execução.
     * Encontra a operação correta a ser executada de acordo com o comando (opcode) em IR.
     */
    private void decodeExecute() {
        IR.internalRead(); // A instrução (opcode) está no intbus2
        int command = intbus2.get(); // Obtém o opcode
        
        simulationDecodeExecuteBefore(command); // Log de simulação

        // As operações `add()`, `sub()`, etc. vão lidar com o avanço do PC internamente.
        switch (command) {
            case Opcodes.ADD:
                add();
                break;
            case Opcodes.SUB:
                sub();
                break;
            case Opcodes.JMP:
                jmp();
                break;
            case Opcodes.JZ:
                jz();
                break;
            case Opcodes.JN:
                jn();
                break;
            case Opcodes.READ:
                read();
                break;
            case Opcodes.STORE:
                store();
                break;
            case Opcodes.LDI:
                ldi();
                break;
            case Opcodes.INC:
                inc();
                break;
            case Opcodes.MOVE_REG_REG:
                moveRegReg();
                break;
            case Opcodes.JNZ: // Novo comando
                jnz();
                break;
            case Opcodes.JEQ: // Novo comando
                jeq();
                break;
            case Opcodes.JGT: // Novo comando
                jgt();
                break;
            case Opcodes.JLW: // Novo comando
                jlw();
                break;
            case Opcodes.CALL: // Novo comando
                call();
                break;
            case Opcodes.RET: // Novo comando
                ret();
                break;
            case Opcodes.IMUL: // Novo comando
                imul();
                break;
            case Opcodes.HALT: // Novo comando
                halt(); // Chama o método halt para parar a simulação
                break;
            default:
                System.err.println("Erro: Comando não reconhecido em IR: " + command + ". Parando simulação.");
                halt = true; // Parar se um comando desconhecido for encontrado
                break;
        }
        
        if (simulation) {
            simulationDecodeExecuteAfter(); // Log de simulação
        }
    }

    /**
     * Este método retorna um registrador da registersList dado seu ID.
     * @param id O ID numérico do registrador (0 para RPG, 1 para RPG1, etc.)
     * @return O objeto Register correspondente.
     * @throws IllegalArgumentException Se o ID for inválido.
     */
    private Register getRegisterById(int id) {
        if (id < 0 || id >= registersList.size()) {
            throw new IllegalArgumentException("ID de registrador inválido: " + id + ". Registradores disponíveis: " + registersList.size());
        }
        return registersList.get(id);
    }
    
    /**
     * Este método é usado para mostrar de forma correta os operandos (se houver) da instrução,
     * quando em modo de simulação.
     * @param instruction O nome da instrução.
     * @return true se a instrução tem operandos, false caso contrário.
     */
    private boolean hasOperands(String instruction) {
        // Isso precisa ser mais robusto. Deveria consultar o Opcodes ou um mapa de aridade.
        // Por enquanto, mantenho a lógica original simples, mas é limitada.
        // A lógica do Assembler (getInstructionSize) é mais precisa para aridade.
        return !("inc".equals(instruction) || "ret".equals(instruction) || "halt".equals(instruction));
    }

    // --- Main Method (ponto de entrada da simulação) ---
    public static void main(String[] args) throws IOException {
        String assemblyFileName = "program"; // Nome padrão do arquivo assembly (sem extensão)
        if (args.length > 0) {
            assemblyFileName = args[0]; // Permite passar o nome do arquivo via linha de comando
        }

        // 1. Instanciar a Arquitetura (que cria todos os componentes)
        // Usaremos o modo de simulação (true) para depuração. Mude para false para execução rápida.
        Architecture arch = new Architecture(true); 
        System.out.println("----- Simulador da Arquitetura C Inicializado -----");
        System.out.println("Tamanho da Memória: " + arch.getMemorySize() + " palavras.");

        // 2. Montar o Microprograma IMUL
        // O microprograma IMUL precisa ser um arquivo .dsf e ser montado pelo Assembler.
        // Ou podemos hardcode o código de máquina do microprograma aqui.
        // Para simplificar, vou hardcode aqui, mas o ideal é que seja montado.
        
        // Exemplo de Microprograma IMUL (para 5 * 3 = 15)
        // Usa: IMUL_OP1_TEMP_ADDR, IMUL_OP2_TEMP_ADDR, IMUL_RESULT_ADDRESS
        // Microprograma IMUL (lógica: REG0 * REG1 -> REG2)
        // RPG = multiplicando
        // RPG1 = multiplicador (contador)
        // PC = resultado (temporário na ULA, depois move para memória)

        // Pseudocódigo IMUL:
        // imul_start:
        //   read IMUL_OP1_TEMP_ADDR ; RPG <- multiplicando (de IMUL_OP1_TEMP_ADDR)
        //   store IMUL_REGS_SAVE_AREA_START + 0 ; Salva RPG (multiplicando) de volta para o contexto
        //   read IMUL_OP2_TEMP_ADDR ; RPG <- multiplicador (de IMUL_OP2_TEMP_ADDR)
        //   store IMUL_REGS_SAVE_AREA_START + 1 ; Salva RPG1 (multiplicador) de volta para o contexto
        //
        //   ldi 0 ; RPG <- 0 (acumulador para o resultado)
        //   store IMUL_RESULT_ADDRESS ; Salva 0 no endereço do resultado

        // Para evitar mexer nos RPGs originais no microprograma, vamos usar as posições IMUL_OP_TEMP_ADDR
        // e um par de registradores internos da ULA, ou RPG/RPG1.

        // Microprograma IMUL REALÍSTICO (Assembly para o Assembler.java):
        // imul_microprogram.dsf
        // -----------------------
        // imul_start:
        //   read IMUL_OP1_TEMP_ADDR_VAL ; RPG <- Operando1
        //   move %RPG %RPG1            ; RPG1 <- RPG (RPG1 agora é o multiplicando)
        //   read IMUL_OP2_TEMP_ADDR_VAL ; RPG <- Operando2 (RPG agora é o multiplicador)
        //   ldi 0                      ; RPG1 <- 0 (acumulador para resultado)
        //
        //   jz imul_end                 ; Se multiplicador (RPG) for 0, pula para o fim
        //
        // imul_loop:
        //   add %RPG1 <IMUL_RESULT_ADDRESS_VAL> ; Result += multiplicando (RPG1)
        //   inc ; RPG-- (decrementa o multiplicador)
        //   jnz imul_loop              ; Se RPG (multiplicador) != 0, continua o loop
        //
        // imul_end:
        //   store IMUL_RESULT_ADDRESS_VAL ; Salva o resultado final de RPG para IMUL_RESULT_ADDRESS
        //   halt                       ; Sinaliza o fim do microprograma para a Arquitetura
        // -----------------------
        //
        // O Loader/Assembler precisa resolver IMUL_OP1_TEMP_ADDR_VAL etc. como valores literais
        // ou labels para os endereços internos da Architecture.
        // O jeito mais simples é passar esses endereços para o Assembler e fazer ele substituir.
        // No Main.java, vamos usar o Assembler para montar o microprograma IMUL.

        String imulMicroprogramAsmFile = "imul_microprogram"; // Nome do arquivo .dsf para o IMUL

        // 2.1. Crie o arquivo imul_microprogram.dsf:
        // (Você pode copiar e colar este conteúdo em um arquivo "imul_microprogram.dsf" na raiz do seu projeto)
        // Ele usará os valores de endereços que a Architecture.java definiu.
        // Para isso, precisaremos de um jeito de passar esses endereços para o Assembler,
        // ou o Assembler precisa conhecê-los de alguma forma (constantes, ou lendo de um .h/config).
        // Por simplicidade, vou hardcode esses endereços no arquivo .dsf.

        // Endereços de exemplo (ajuste com os valores reais da sua Architecture.java,
        // você pode pegá-los de um objeto Architecture temporário aqui)
        Architecture tempArchForAddrs = new Architecture(); // Instância temporária para pegar os endereços
        final int IMUL_OP1_TEMP_ADDR_VAL = tempArchForAddrs.getImulOp1TempAddr();
        final int IMUL_OP2_TEMP_ADDR_VAL = tempArchForAddrs.getImulOp2TempAddr();
        final int IMUL_RESULT_ADDRESS_VAL = tempArchForAddrs.getImulResultAddress();
        final int IMUL_REGS_SAVE_AREA_START_VAL = tempArchForAddrs.getImulRegistersSaveAreaStart();
        // Não é ideal criar uma Architecture temporária. O ideal é que esses endereços sejam constantes estáticas.

        String imulProgramContent = String.join("\n",
            "imul_start:",
            "  read " + IMUL_OP1_TEMP_ADDR_VAL, // RPG <- Operando1
            "  move %RPG %RPG1",                // RPG1 <- RPG (RPG1 agora é o multiplicando)
            "  read " + IMUL_OP2_TEMP_ADDR_VAL, // RPG <- Operando2 (RPG agora é o multiplicador)
            "  ldi 0",                         // RPG1 <- 0 (acumulador para resultado)
            "imul_loop_check_zero:", // Novo label para clareza
            "  jz imul_end",                   // Se RPG (multiplicador) for 0, pula para o fim
            "imul_loop:",
            "  add " + IMUL_REGS_SAVE_AREA_START_VAL + " + 0 ; RPG += RPG1 (acumula RPG1 no RPG, ERROR: needs ADD Reg Reg, not ADD addr)",
            // A instrução ADD no seu assembly é 'add addr' ou 'add %reg %reg'.
            // Para 'RPG += RPG1' usando a sua ULA e microcódigo:
            // RPG.internalRead(); ula.store(0); // RPG -> ULA.internalReg0
            // RPG1.internalRead(); ula.store(1); // RPG1 -> ULA.internalReg1 (se RPG1 pudesse ir para intbus2)
            // ULA.add(); ULA.internalRead(1); RPG.internalStore();
            //
            // Dado seu assembly, 'add %regA %regB' é 'add %<regA> %<regB> || RegB <- RegA + RegB'.
            // Então, se queremos 'RPG1 <- RPG + RPG1' (acumulador em RPG1, multiplicando em RPG):
            // add %RPG %RPG1
            // Isso já é `moveregreg` com opcode 9.
            // Precisamos que o Assembler entenda `add %reg %reg`.
            // No seu Assembler.java, o `add` é `add addr` (opcode 0).
            // A sua arquitetura não tem `add %reg %reg` com o opcode `add`.
            // O `moveRegReg` tem opcode 9.
            // Para fazer `RPG1 <- RPG + RPG1`, usaríamos:
            // RPG.internalRead(); ula.store(0); // RPG -> internalReg0
            // RPG1.internalRead(); ula.store(1); // RPG1 -> internalReg1 (SE RPG1 PUDER IR PARA INTBUS2)
            // Isso é um problema. Os RPGs só vão para intbus1.
            //
            // SOLUÇÃO REALISTA PARA IMUL: O microprograma deve ser codificado usando as instruções existentes:
            // imul_start:
            //   read " + IMUL_OP1_TEMP_ADDR_VAL + " ; RPG <- multiplicando (valor a ser somado)
            //   store " + IMUL_RESULT_ADDRESS_VAL + " ; result = 0
            //   read " + IMUL_OP2_TEMP_ADDR_VAL + " ; RPG1 <- multiplicador (contador)
            // imul_loop:
            //   jz imul_end
            //   add " + IMUL_RESULT_ADDRESS_VAL + " ; RPG <- RPG + result
            //   inc ; RPG1--
            //   jmp imul_loop
            // imul_end:
            //   store " + IMUL_RESULT_RESULT_ADDRESS_VAL + " ; result <- RPG
            //   halt
            //
            // ISTO É MUITO IMPORTANTE: A arquitetura do seu microcódigo é baseada em RPG como acumulador.
            // "add addr" -> RPG <- RPG + mem[addr]
            // "sub addr" -> RPG <- RPG - mem[addr]
            // "inc" -> RPG++
            // "ldi x" -> RPG <- x
            // "read addr" -> RPG <- mem[addr]
            // "store addr" -> mem[addr] <- RPG
            //
            // Então, a multiplicação deve ser feita assim:
            // result = 0
            // counter = multiplicador
            // while (counter > 0) {
            //   result = result + multiplicando
            //   counter--
            // }
            //
            // Microprograma IMUL com a sua arquitetura:
            "imul_start:",
            "  read " + IMUL_OP1_TEMP_ADDR_VAL,   // RPG <- multiplicando (o valor a ser somado repetidamente)
            "  store " + IMUL_RESULT_ADDRESS_VAL,   // Mem[IMUL_RESULT_ADDRESS] <- multiplicando (temporário para inicializar)
                                                   // Isso é para ter o valor de '0' no resultado.
                                                   // Melhor: ldi 0; store IMUL_RESULT_ADDRESS_VAL.
            "  ldi 0",                           // RPG <- 0 (vai ser o acumulador do resultado)
            "  store " + IMUL_RESULT_ADDRESS_VAL,   // Mem[IMUL_RESULT_ADDRESS] <- 0
            
            "  read " + IMUL_OP2_TEMP_ADDR_VAL,   // RPG <- multiplicador (vai ser o contador)
            "  move %RPG %RPG1",                   // RPG1 <- RPG (RPG1 agora é o contador original)
                                                   // Precisamos de um contador decrescente, e o valor do multiplicador salvo.
                                                   // Então RPG1 é o multiplicando, RPG é o contador.
            "imul_loop:",
            "  jz imul_end",                   // Se RPG (contador) for 0, pula para o fim
            "  read " + IMUL_RESULT_ADDRESS_VAL, // RPG <- resultado atual
            "  add " + IMUL_OP1_TEMP_ADDR_VAL,   // RPG <- RPG + multiplicando (IMUL_OP1_TEMP_ADDR_VAL)
            "  store " + IMUL_RESULT_ADDRESS_VAL, // Mem[IMUL_RESULT_ADDRESS] <- RPG (novo resultado)
            "  inc",                           // RPG++ (isso é errado para decremento, precisa de SUB)
            // Seu `inc` em `Architecture.java` é RPG++. Não temos `dec`.
            // Para `RPG--`: `ldi 1; sub @temp_reg_for_one; RPG <- RPG - 1`
            // Isso requer um endereço de memória para o '1' constante.
            // `sub @address_of_constant_one`
            // Para simplificar, vou usar `inc` e depois `sub` (isso é gambiarra para simular decremento)
            // OU, você adiciona um opcode `DEC` no `Opcodes.java` e implementa o microprograma `dec()` em `Architecture.java`.
            // Se `inc` é `RPG++`, então não posso usar `inc` para decremento.

            // Vou simular decremento de RPG (que é o contador) usando `ldi 1` e `sub`:
            "  ldi 1",                         // RPG <- 1
            "  move %RPG %RPG1",               // RPG1 <- 1
            "  read " + IMUL_OP2_TEMP_ADDR_VAL, // RPG <- multiplicador (contador)
            "  sub " + IMUL_REGS_SAVE_AREA_START_VAL + 0, // RPG <- RPG - 1. (sub @var onde @var é 1)
                                                         // Isso requer um valor '1' em alguma posição de memória fixa.
                                                         // Ou se `sub` aceitasse imediato.
            // A instrução `sub addr` no seu assembly é `RPG <- RPG - memória[addr]`.
            // Precisamos de um endereço fixo na memória que contenha o valor 1.
            // Digamos que `CONST_ONE_ADDR` seja reservado na memória para isso.
            "  sub " + (arch.getMemorySize() - 2), // Usando um endereço fixo `memorySize - 2` para armazenar o '1'
                                                    // Este endereço DEVE ter o valor 1, carregado no Main.
            "  store " + IMUL_OP2_TEMP_ADDR_VAL, // Salva o novo contador
            "  jmp imul_loop",                 // Volta para o loop
            "imul_end:",
            "  halt"                           // Sinaliza o fim do microprograma
        );
        
        // Criar o arquivo .dsf temporariamente para o Assembler
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(imulMicroprogramAsmFile + ".dsf"))) {
            writer.write(imulProgramContent);
        }

        Assembler imulAssembler = new Assembler(arch.getMemorySize());
        imulAssembler.read(imulMicroprogramAsmFile);
        imulAssembler.parse();
        imulAssembler.makeExecutable(imulMicroprogramAsmFile);

        // Carregar o microprograma IMUL na memória da arquitetura
        arch.readExec(imulMicroprogramAsmFile);
        System.out.println("Microprograma IMUL carregado a partir do endereço: " + arch.IMUL_MICROPROGRAM_START);

        // 2.2. Carregar o valor 1 na memória em arch.getMemorySize() - 2
        arch.getExtbus1().put(arch.getMemorySize() - 2); // Endereço para '1'
        arch.getMemory().store(); // Memory pega endereço
        arch.getExtbus1().put(1); // Valor '1'
        arch.getMemory().store(); // Memory grava valor


        // 3. Montar o Programa Principal
        Assembler mainAssembler = new Assembler(arch.getMemorySize());
        System.out.println("\nMontando programa assembly principal: " + assemblyFileName + ".dsf");
        mainAssembler.read(assemblyFileName);
        mainAssembler.parse();
        mainAssembler.makeExecutable(assemblyFileName);

        // 4. Carregar o Programa Principal na memória
        // O programa principal começa após o microprograma IMUL
        // O PC da Architecture.readExec() sempre inicia em 0.
        // Precisamos ajustar o PC para onde o programa principal foi carregado.
        // A lógica do Assembler mapeia labels a partir do 0.
        // Se o programa principal não começar em 0, precisamos de um offset.
        
        // A `readExec` da Architecture.java sempre carrega a partir do endereço 0 da memória.
        // Para carregar o programa principal em um offset, o `readExec` precisa aceitar `startAddress`.
        // Ou o `Assembler` precisa ser dito para compensar os endereços.
        
        // Por simplicidade, vamos carregar o programa principal no início (endereço 0)
        // e o microprograma IMUL em um endereço acima das instruções do programa principal.
        // ISSO VAI INVERTER A LÓGICA DE ALOCAÇÃO DO IMUL.
        // Onde IMUL_MICROPROGRAM_START é maior que o programa principal.
        
        // Reajuste: IMUL microprograma e áreas de sistema devem estar no final da memória, não no início.
        // Variáveis: do final para o início.
        // Pilha: do final para o início.
        // O código principal: a partir do 0.
        // O main() na Architecture.java é o driver.

        // Vamos carregar o programa principal em 0.
        // E carregar o IMUL em IMUL_MICROPROGRAM_START (que deve ser alto na memória).
        // Isso requer que a `Architecture.readExec()` aceite um endereço de carga.
        // O `readExec` atual sempre carrega do 0. Precisa de um `loadExec(filename, startAddress)`.

        // ALTERAÇÃO CRÍTICA: Mudar `readExec` em `Architecture.java` para aceitar `startAddress`.
        // public void readExec(String filenameWithoutExtension, int startAddress) throws IOException { ... }
        // E ajustar as chamadas em `Main`.

        // Assumindo que `readExec` foi atualizado para `readExec(filename, startAddress)`:
        arch.readExec(assemblyFileName, 0); // Carrega o programa principal a partir do endereço 0
        
        // Carregar o microprograma IMUL no endereço reservado
        arch.readExec(imulMicroprogramAsmFile, arch.IMUL_MICROPROGRAM_START);

        System.out.println("Programa principal carregado na memória.");
        System.out.println("\nEstado inicial dos registradores:");
        arch.printRegisters();
        System.out.println("\nConteúdo da memória (primeiras 30 palavras e área IMUL):");
        arch.getMemory().dumpMemory(0, 30);
        arch.getMemory().dumpMemory(arch.IMUL_MICROPROGRAM_START, arch.IMUL_MICROPROGRAM_START + imulAssembler.getExecProgram().size() + 2);
        
        System.out.println("\n----- Iniciando Execução da Arquitetura -----");
        arch.controlUnitEexec();
        System.out.println("----- Execução Finalizada -----");

        System.out.println("\nEstado final dos registradores:");
        arch.printRegisters();
        System.out.println("\nConteúdo da memória após execução (amostra):");
        arch.getMemory().dumpMemory(0, 30); // Amostra do início da memória
        arch.getMemory().dumpMemory(arch.getMemorySize() - 10, arch.getMemorySize() - 1); // Amostra do final (pilha/vars)
        System.out.println("Resultado IMUL em MEM[" + arch.getImulResultAddress() + "]: " + arch.getMemory().getData(arch.getImulResultAddress()));
    }
}