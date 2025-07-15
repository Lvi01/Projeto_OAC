package components;

public class Stack {
    private Memory memory;
    private Register stackTop;
    private Register stackBottom;
    private ULA ula; // Referência à ULA para manipular stackTop
    private Bus extbus; // Referência ao barramento externo para interagir com a memória

    // Construtor atualizado para receber a ULA e o Bus externo
    public Stack(Memory memory, Register stackTop, Register stackBottom, ULA ula, Bus extbus) {
        this.memory = memory;
        this.stackTop = stackTop;
        this.stackBottom = stackBottom;
        this.ula = ula;
        this.extbus = extbus; // Armazena a instância do barramento externo
    }

    public void push(int value) {
        // Antes de empilhar, decrementa StackTop usando a ULA
        // Lógica de micro-operação similar à que PC.inc() usa na Architecture.java:
        // stackTop -> intbus2 -> ULA.internalReg1 -> ULA.internalReg1-- -> intbus2 -> stackTop
        stackTop.internalRead(); // StackTop (valor atual) -> intbus2
        ula.internalStore(1);    // intbus2 -> ULA.internalReg1
        ula.inc();               // ULA.internalReg1++ (Aqui o `inc` da ULA é incrementa, não decrementa)

        // SOLUÇÃO: A ULA precisa de um método `dec()`. Se não tiver, precisamos simular dec.
        // Se a ULA não tem dec(), o "microcódigo" para `stackTop.get() - 1` tem que ser refeito.
        // A sua ULA tem inc(), add(), sub(). Não tem dec().
        //
        // Opção 1: Adicionar `public void dec()` na ULA que decrementa `internalReg1`.
        // Opção 2: Simular decremento com SUB: `stackTop.get()` para `internalReg0`, `1` para `internalReg1`, `ula.sub()`.
        // Mas a ULA.sub() opera `internalReg1 = internalReg0 - internalReg1;`.
        // Então para `X - 1`: internalReg0=X, internalReg1=1. Resulta em `X-1`.
        //
        // VOU ASSUMIR QUE A ULA TEM UM MÉTODO `dec()` OU QUE O COMPORTAMENTO DE `ula.inc()`
        // PODE SER INTERPRETADO COMO UM DECREMENTO NO CONTEXTO DA PILHA SE VOCÊ MUDAR O SENTIDO DA ULA.
        // Mas para ser correto com "decresce na memória", `stackTop.get() - 1`.
        //
        // A ULA refatorada por mim antes tinha `dec()`:
        // public void dec() { this.internalReg1--; }
        // Se ela não foi removida, podemos usá-la. Seu ULA.java que você enviou recentemente
        // NÃO TEM public void dec().

        // Vou simular o decremento usando ULA.sub() de forma explícita com os barramentos:
        int currentTopValue = stackTop.getData(); // Pega o valor atual do StackTop

        extbus.put(currentTopValue); // Coloca o valor de StackTop no barramento externo
        ula.store(0); // extbus -> ULA.internalReg0 (primeiro operando)
        
        extbus.put(1); // Coloca o valor '1' no barramento externo
        ula.store(1); // extbus -> ULA.internalReg1 (segundo operando)
        
        ula.sub(); // ULA.internalReg1 = ULA.internalReg0 - ULA.internalReg1 (StackTop - 1)
        ula.internalRead(1); // Resultado (newTopValue) de ULA.internalReg1 -> intbus2

        stackTop.internalStore(); // intbus2 -> StackTop (StackTop agora aponta para a nova posição)
        
        // Agora, escreve o valor na memória na nova posição do topo
        extbus.put(stackTop.getData()); // Coloca o endereço (novo StackTop) no extbus
        memory.store(); // Memory pega o endereço alvo (MAR)
        extbus.put(value); // Coloca o valor a ser empilhado no extbus
        memory.store(); // Memory grava o valor no MAR
    }

    public int pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack underflow: pilha vazia.");
        }
        
        // Lê o valor da posição atual de StackTop na memória
        extbus.put(stackTop.getData()); // Coloca o endereço (StackTop) no extbus
        memory.read(); // Memory lê o endereço do extbus, coloca o DADO no extbus
        int poppedValue = extbus.get(); // Pega o valor do barramento

        // Incrementa StackTop usando a ULA
        // Lógica de micro-operação para `stackTop.get() + 1`:
        stackTop.internalRead(); // StackTop (valor atual) -> intbus2
        ula.internalStore(1);    // intbus2 -> ULA.internalReg1
        ula.inc();               // ULA.internalReg1++ (StackTop + 1)
        ula.internalRead(1);     // ULA.internalReg1 -> intbus2
        stackTop.internalStore(); // intbus2 -> StackTop (StackTop agora aponta para a nova posição)
        
        return poppedValue;
    }

    public boolean isEmpty() {
        // Pilha vazia quando StackTop e StackBottom apontam para o mesmo endereço
        return stackTop.getData() == stackBottom.getData();
    }

    public void reset() {
        stackTop.setData(stackBottom.getData());
    }
}