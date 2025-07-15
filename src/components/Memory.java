package components;

public class Memory {
    private int[] data;
    private final int MAX_SIZE; // Tamanho total da memória em "palavras" (inteiros)
    private Bus extbus; // Referência ao barramento externo

    // Novo campo para simular o "endereço ativo" para operações de leitura/escrita.
    // Em um hardware real, o endereço viria de um MUX/DEC, aqui ele vem do extbus.
    private int currentAddressInBus; 

    // Construtor: agora recebe o tamanho da memória e o barramento externo
    public Memory(int maxSize, Bus extbus) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Memory size must be positive.");
        }
        this.MAX_SIZE = maxSize;
        this.data = new int[MAX_SIZE];
        this.extbus = extbus; // Armazena a referência ao barramento

        // Inicializa a memória com zeros
        for (int i = 0; i < MAX_SIZE; i++) {
            this.data[i] = 0;
        }

        // Os endereços de áreas reservadas (IMUL, Stack, etc.) não são mais definidos aqui.
        // Essa lógica de alocação específica pertence à Architecture ou a uma camada superior.
        // A Memory é apenas uma coleção de células de armazenamento.
    }

    /**
     * Lê um valor da memória. O endereço a ser lido é obtido do extbus,
     * e o valor lido é colocado no extbus.
     * Corresponde a `memory.read();` em Architecture.java.
     */
    public void read() {
        // Assume que o endereço já está no extbus (vindo do PC ou outro registrador)
        currentAddressInBus = extbus.get(); // Lê o endereço do barramento
        if (currentAddressInBus < 0 || currentAddressInBus >= MAX_SIZE) {
            throw new IndexOutOfBoundsException("Memory read error: Address " + currentAddressInBus + " is out of bounds [0, " + (MAX_SIZE - 1) + "]");
        }
        extbus.put(data[currentAddressInBus]); // Coloca o valor da memória no barramento
        // System.out.println("DEBUG: Memory leu MEM[" + currentAddressInBus + "] = " + data[currentAddressInBus] + " para extbus.");
    }

    /**
     * Escreve um valor na memória. O endereço é lido do extbus,
     * e o valor a ser escrito é lido do extbus (após o endereço ser processado).
     * Corresponde a `memory.store();` em Architecture.java.
     */
    public void store() {
        // Assume que o endereço já está no extbus (vindo do PC ou outro registrador)
        // Isso é uma simplificação. Em um hardware real, o endereço seria travado antes do dado.
        // No seu microcódigo, `memory.store()` é chamado duas vezes.
        // A primeira vez, `extbus` tem o endereço.
        // A segunda vez, `extbus` tem o valor.
        // Vamos adaptar para a lógica explícita que você usa.

        // O primeiro 'store()' captura o endereço.
        // O segundo 'store()' captura o valor e o grava no endereço capturado.
        // Para isso, a Memory precisa ter um estado interno que indica o "modo" ou o "endereço recente".
        // Vamos fazer a primeira chamada para store() armazenar o endereço
        // e a segunda (consecutiva) armazenar o valor.

        // A forma mais direta de seguir seu microcódigo `memory.store()` duas vezes
        // é se a `memory.store()` for inteligente sobre o que ela está recebendo.
        // Uma flag `expectingValue` ajuda a controlar isso.
        
        // No seu microcódigo para `store()`:
        // 7. memory reads // memory reads the data in the parameter address. (this is memory.read())
        //                // BUT THE COMMENT says "memory reads the data in the parameter address."
        //                // AND memory.store() is called. This is a bit confusing.
        //
        // Let's assume your "memory reads" in microcode means "memory.read()" operation
        // and "memory stores" means "memory.store()" operation.
        // In the `store()` microprogram:
        // 6. pc -> extbus
        // 7. memory.read() (This will put the *content* of PC's address (which is the address to store TO) into extbus)
        // 8. memory.store() (This must take the *address* from extbus) -- this implies extbus has the address
        // 9. RPG -> Externalbus
        // 10. memory.store() (This must take the *value* from extbus and write to the address taken at step 8)

        // Isso significa que a `memory.store()` precisa ser capaz de receber o endereço e o valor.
        // A forma mais simples de seguir seu microcódigo é:
        // Crio um registrador interno para o endereço que a memória vai usar.
        // E ela sempre pega o DADO para gravar do extbus.

        // Vamos revisar a lógica do seu `store()` no `Architecture.java`:
        // 6. PC.read(); // coloca o *endereço do parâmetro* no extbus
        // 7. memory.read(); // LÊ o *valor* no endereço do parâmetro (que é o endereço de destino) para o extbus
        //                    // Ou seja, `extbus` agora tem o ENDEREÇO DE DESTINO FINAL.
        // 8. memory.store(); // ESTE `store()` DEVE PEGAR O ENDEREÇO DO EXTBUS E SALVAR INTERNAMENTE
        //                    // (ou seja, `this.currentAddressInBus = extbus.get();`)
        // 9. RPG.read(); // coloca o *valor do RPG* no extbus
        // 10. memory.store(); // ESTE `store()` DEVE PEGAR O VALOR DO EXTBUS E ESCREVER NO currentAddressInBus.

        // Dada essa complexidade do seu microcódigo `store`,
        // a classe Memory precisará de dois métodos de `store`: um para o endereço e um para o dado.
        // Ou um método `store(boolean isAddress)` para o seu microcódigo específico.
        // VOU IMPLEMENTAR memory.store() para pegar o ENDEREÇO do extbus e esperar o DADO na próxima chamada.
        // Mas a sua memory.read() no step 7 já coloca o DADO NO EXTbus.

        // Uma interpretação mais alinhada ao hardware e menos confusa:
        // Para store addr:
        // 1. PC.internalRead() ... PC.internalStore() // PC agora aponta para o "addr" (endereço onde deve guardar)
        // 2. PC.read() // Coloca "addr" (o endereço de destino) no extbus
        // 3. memory.setAddressFromBus() // Memory lê o extbus e internaliza o "addr"
        // 4. RPG.read() // Coloca o valor a ser guardado no extbus
        // 5. memory.writeValueFromBus() // Memory lê o extbus e guarda no "addr" internalizado.
        // ISSO REQUERIA MUDANÇAS NO SEU MICROCODIGO DE STORE.

        // Vamos tentar seguir o que você escreveu no microcódigo `store()` mais de perto:
        // A primeira `memory.store()` em `Architecture.store()` está estranha.
        // 7. memory.read(); // "the parameter address (pointing to the address where data must be stored is now in externalbus1"
        //                   // ISSO é `extbus.put(data[address_from_extbus])`
        // 8. memory.store(); // "the address is in the memory. Now we must to send the data"
        //                   // Se a step 7 já colocou o valor no extbus, o step 8 vai ler esse valor como endereço.
        //                   // Isso não faz sentido.

        // Eu **preciso** que você esclareça a lógica exata de `memory.read()` e `memory.store()` nos seus microprogramas.
        // Pelo seu código, parece que `memory.read()` (sem parâmetro) espera que o endereço esteja no extbus
        // e, em seguida, coloca o *conteúdo* desse endereço no extbus.
        //
        // E `memory.store()` (sem parâmetro) espera que o *endereço* esteja no extbus, e depois o *valor* a ser armazenado.
        // Isso requer uma lógica de estado na Memory ou dois métodos separados para store: `storeAddress()` e `storeValue()`.

        // Dada a sua descrição `memory.read();` e `memory.store();` em Architecture.java,
        // vou implementar a Memory com um registrador interno para o endereço (MAR - Memory Address Register)
        // e um registrador para o dado (MDR - Memory Data Register), e ela interage com o extbus.

        // Lógica de `Memory` baseada no uso em `Architecture.java`:
        // `read()`: Lê o endereço do `extbus`, lê o dado de `data[address]`, e coloca o dado no `extbus`.
        // `store()`: Lê o endereço do `extbus` (MAR), e na próxima vez que for chamada para `store()`
        //            (ou um método `storeData()`), lê o dado do `extbus` (MDR) e o grava.
        //            Isso significa `memory.store()` em seu microcódigo *primeiro* pega o endereço,
        //            e a *próxima* `memory.store()` pega o dado.
        
        // Ok, vamos tentar implementar a `store()` dessa forma.
        // Se `store()` é chamada, e o barramento contém um endereço, ela salva.
        // Se `store()` é chamada, e o barramento contém um dado, ela usa o último endereço salvo.
        // Isso é arriscado e pode causar bugs se o fluxo não for sempre "endereço, depois dado".

        // Outra possibilidade, e mais provável, é que:
        // `memory.read()`: Lê o valor em `data[extbus.get()]` e coloca NO extbus.
        // `memory.store()`: Lê o *endereço* de `extbus.get()`, e **então** lê o *valor* de `extbus.get()`
        // (o que significa que um `extbus.put(value)` deve ter sido feito por outro componente logo antes).

        // A clareza do seu `Architecture.java` para `memory.read()` e `memory.store()` é o seguinte:
        // `memory.read()`: `PC.read()` (PC no extbus) -> `memory.read()` (pega do extbus, coloca dado no extbus)
        // `memory.store()`: `PC.read()` (endereço no extbus) -> `memory.read()` (valor no extbus) -> `memory.store()` (pega endereço, espera valor) -> `RPG.read()` (valor no extbus) -> `memory.store()` (pega valor, armazena)
        // **Este padrão indica que `memory.store()` é chamada duas vezes:**
        // 1. A primeira vez, `memory.store()` deve **consumir o endereço do bus**.
        // 2. A segunda vez, `memory.store()` deve **consumir o dado do bus e armazená-lo no endereço previamente consumido**.
        // Isso implica um estado interno na `Memory` (um "endereço alvo").

        private int mar; // Memory Address Register (endereço alvo para store)
        private boolean expectingDataForStore; // Flag para controlar o ciclo de store

        /**
         * Construtor da memória.
         * @param maxSize Tamanho da memória.
         * @param extbus O barramento externo ao qual a memória está conectada.
         */
        public Memory(int maxSize, Bus extbus) {
            if (maxSize <= 0) {
                throw new IllegalArgumentException("Memory size must be positive.");
            }
            this.MAX_SIZE = maxSize;
            this.data = new int[MAX_SIZE];
            this.extbus = extbus;

            for (int i = 0; i < MAX_SIZE; i++) {
                this.data[i] = 0;
            }
            this.mar = -1; // Endereço de memória de destino, -1 indica não setado
            this.expectingDataForStore = false; // Não está esperando dado para store por padrão
        }

        /**
         * Lê um valor da memória.
         * Assume que o endereço está no `extbus`.
         * Coloca o valor lido da memória no `extbus`.
         */
        public void read() {
            // No seu microcódigo `read()` e `add()`, `PC.read()` ou `RPG.read()` é chamado antes de `memory.read()`.
            // Isso coloca o ENDEREÇO (ou dado, no caso de RPG) no `extbus`.
            // Se `memory.read()` é chamado duas vezes seguidas, isso é um sinal.
            // Exemplo `add()`:
            // PC.read(); (endereço do parâmetro no extbus)
            // memory.read(); // (lê o *conteúdo* do endereço do parâmetro, que é o endereço real, e coloca no extbus)
            // memory.read(); // (lê o *conteúdo* do endereço real, que é o dado, e coloca no extbus)
            // Isso é "read address, then read data at address".

            // Para simular isso, a primeira `read()` pega o endereço e a segunda pega o dado.
            // Ou, a memória sempre assume que o `extbus` tem o endereço a ser lido, e ela lê o dado e o coloca de volta no bus.

            // Vou usar a interpretação mais simples: `memory.read()` sempre lê o endereço do bus, pega o dado e o coloca no bus.
            // Isso significa que seu microcódigo `memory.read(); memory.read();`
            // precisaria ser `memory.read_address(); memory.read_data();` ou similar.
            // Dada a repetição de `memory.read()` no seu microcódigo para obter o VALOR,
            // (ex: em ADD, você faz `PC.read(); memory.read(); memory.read(); RPG.store();`)
            // isso é um problema. A primeira `memory.read()` deveria retornar o endereço, e a segunda o dado.
            //
            // ASSUNÇÃO CRÍTICA: No seu microcódigo:
            // - `memory.read()`: Pega o endereço do `extbus`, e **COLOCA O CONTEÚDO DESSE ENDEREÇO NO `extbus`**.
            // A segunda `memory.read()` significa que o `extbus` agora tem o endereço *real* do dado (vindo do primeiro `memory.read()` via endereço).

            int addressToAccess = extbus.get(); // Pega o endereço do barramento

            if (addressToAccess < 0 || addressToAccess >= MAX_SIZE) {
                throw new IndexOutOfBoundsException("Memory read error: Address " + addressToAccess + " is out of bounds [0, " + (MAX_SIZE - 1) + "]");
            }
            extbus.put(data[addressToAccess]); // Coloca o CONTEÚDO desse endereço no barramento
            // System.out.println("DEBUG: Memory leu MEM[" + addressToAccess + "] = " + data[addressToAccess] + " e colocou em extbus.");
        }

        /**
         * Escreve um valor na memória.
         * Esta operação funciona em dois estágios, ou com um endereço já definido.
         * Dependendo do estado interno `expectingDataForStore`:
         * - Se `false`: O valor no `extbus` é interpretado como o ENDEREÇO de destino (`mar`).
         * - Se `true`: O valor no `extbus` é interpretado como o DADO a ser escrito em `mar`.
         * Corresponde a `memory.store();` em Architecture.java.
         */
        public void store() {
            if (!expectingDataForStore) {
                // Primeiro estágio: extbus tem o ENDEREÇO
                this.mar = extbus.get(); // Armazena o endereço de destino
                if (this.mar < 0 || this.mar >= MAX_SIZE) {
                    throw new IndexOutOfBoundsException("Memory store error (address phase): Address " + this.mar + " is out of bounds [0, " + (MAX_SIZE - 1) + "]");
                }
                expectingDataForStore = true; // Agora estamos esperando o dado
                // System.out.println("DEBUG: Memory armazenou endereço alvo: " + mar + ". Esperando dado.");
            } else {
                // Segundo estágio: extbus tem o DADO
                int valueToStore = extbus.get(); // Pega o dado do barramento
                data[this.mar] = valueToStore; // Armazena o dado no endereço alvo
                expectingDataForStore = false; // Reseta para o próximo ciclo de store
                this.mar = -1; // Limpa o endereço alvo
                // System.out.println("DEBUG: Memory armazenou DADO " + valueToStore + " em MEM[" + this.mar + "].");
            }
        }
        
        // --- Métodos Especiais para statusMemory ---
        // Estes métodos são para a memória de status de 2 posições em Architecture.java (statusMemory)
        // Eles não se encaixam na `read()` e `store()` genéricas acima.
        // Assumo que eles serão usados apenas pela `statusMemory` e que ela não usa os barramentos `extbus`
        // da mesma forma que a memória principal.

        // `statusMemory.storeIn1()`: Armazena o valor do extbus na posição 1 da statusMemory.
        public void storeIn1() {
            // Este método é chamado em um objeto Memory que foi criado como new Memory(2, extbus1)
            // indicando que ele tem apenas duas posições (0 e 1).
            this.data[1] = extbus.get();
            // System.out.println("DEBUG: Status Memory armazenou " + data[1] + " na posição 1.");
        }

        // `statusMemory.storeIn0()`: Armazena o valor do extbus na posição 0 da statusMemory.
        public void storeIn0() {
            this.data[0] = extbus.get();
            // System.out.println("DEBUG: Status Memory armazenou " + data[0] + " na posição 0.");
        }

        // `statusMemory.read()`: Retorna o endereço de desvio (posição 1) se o bit de flag (vindo do extbus) for 1,
        // caso contrário, retorna o endereço da próxima instrução (posição 0).
        // `extbus1.put(Flags.getBit(0));` antes de `statusMemory.read();`
        public void read() {
            // A `read()` genérica de memória foi modificada acima para `read()`.
            // Se `statusMemory.read()` está sendo chamada, é um caso especial.
            // Para distinguí-la da `read()` da memória principal,
            // `statusMemory` precisa de um método `readConditional()` ou similar.
            // OU, se `statusMemory` é instanciada com seu próprio `Bus`,
            // e o `extbus` para ela é o `intbus2` (como Flags.getBit(0) vai para extbus1 no seu jz/jn,
            // mas o construtor da Memory é `extbus1`).
            //
            // AQUI É UM PONTO DE DIFICULDADE.
            // SE statusMemory é `new Memory(2, extbus1)`, ela vai interagir com `extbus1`.
            // O seu microcódigo `Flags.getBit(0)` (que seria a Zero Flag)
            // É COLOCADO DIRETAMENTE NO `extbus1`.
            // E entao `statusMemory.read()` é chamado.
            //
            // Então, a `read()` precisa verificar o valor do bit no `extbus` para decidir o retorno.

            // A `read()` acima para a memória principal não serve para a `statusMemory.read()`.
            // Vou criar um método específico para isso.
            // Mas, dado que você só tem `public void read()`, o seu microcódigo `jz` e `jn`
            // está chamando essa mesma `read()`.
            // Isso é um conflito de design. A `Memory` genérica não pode se comportar
            // como uma `Memory` de status específica.

            // OPÇÃO 1 (Recomendada): Crie uma classe `StatusMemory` separada que herda de `Memory`
            // ou tem uma `Memory` e implementa essa lógica especial.
            // OPÇÃO 2: Modificar a `Memory.read()` para ter um `if` (this.MAX_SIZE == 2)
            // para o caso especial, o que não é um bom design.
            // OPÇÃO 3: Assumir que o `read()` genérico sempre coloca o dado no bus.
            //          E a `Architecture` é que pega o dado do bus e faz a lógica condicional.
            //          MAS SEU MICROCODIGO `statusMemory.read()` IMPLICA QUE A MEMORY FAZ A DECISÃO.

            // DADA A ESTRUTURA DO SEU `jz()` e `jn()`:
            // extbus1.put(Flags.getBit(0)); // OU getBit(1)
            // statusMemory.read(); // <<< ESTE MÉTODO PRECISA LER O VALOR DO EXTBus para decidir

            int flagBit = extbus.get(); // Pega o bit de flag do barramento
            int addressToReturn;
            if (flagBit == 1) {
                // Se a flag for 1, retorna o endereço de salto (posição 1)
                addressToReturn = this.data[1];
            } else {
                // Se a flag for 0, retorna o endereço da próxima instrução (posição 0)
                addressToReturn = this.data[0];
            }
            extbus.put(addressToReturn); // Coloca o endereço escolhido no barramento
            // System.out.println("DEBUG: Status Memory decidiu " + addressToReturn + " com flag " + flagBit + " e colocou em extbus.");
        }


        // Métodos da versão anterior, mantidos para completude se ainda forem úteis
        // ou se a Architecture ou Assembler os utilizarem para carga inicial.
        public int getData(int address) { // Renomeado de read(int address)
            if (address < 0 || address >= MAX_SIZE) {
                throw new IndexOutOfBoundsException("Memory read error: Address " + address + " is out of bounds [0, " + (MAX_SIZE - 1) + "]");
            }
            return data[address];
        }

        public void setData(int address, int value) { // Renomeado de write(int address, int value)
            if (address < 0 || address >= MAX_SIZE) {
                throw new IndexOutOfBoundsException("Memory write error: Address " + address + " is out of bounds [0, " + (MAX_SIZE - 1) + "]");
            }
            data[address] = value;
        }

        public int getMemorySize() { // Renomeado de getMaxSize()
            return MAX_SIZE;
        }

        // Método para carregar um programa/dados na memória (usado pelo Loader/Assembler)
        // Isso assume que o Loader/Assembler ainda usará este método.
        // O `readExec` em Architecture.java é um loader de baixo nível.
        public void load(int startAddress, int[] programData) {
            if (startAddress < 0 || startAddress + programData.length > MAX_SIZE) {
                throw new IllegalArgumentException("Program data does not fit in memory at address " + startAddress);
            }
            System.arraycopy(programData, 0, data, startAddress, programData.length);
        }
        
        // Método para depuração
        public int[] getDataList() { // Adicionado para simulaçãoDecodeExecuteBefore
            return data;
        }
    
        public void dumpMemory(int start, int end) {
            System.out.println("--- Memory Dump from " + start + " to " + end + " ---");
            for (int i = start; i <= end; i++) {
                if (i >= 0 && i < MAX_SIZE) {
                    System.out.printf("MEM[%04d]: %d%n", i, data[i]);
                } else {
                    System.out.println("MEM[----]: Out of bounds");
                }
            }
            System.out.println("---------------------------");
        }
    }