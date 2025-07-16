# Projeto Arquitetura C Expandida

## Visão Geral

Este projeto implementa uma extensão da Arquitetura C original, expandindo de 12 para 21 instruções funcionais. O desenvolvimento foi realizado em fases sistemáticas, demonstrando conceitos avançados de arquitetura de computadores e organização de sistemas.

## Evolução do Projeto

### Estado Inicial
O projeto iniciou com a Arquitetura C base contendo 12 instruções fundamentais:
- Operações aritméticas: add, sub, inc
- Controle de fluxo: jmp, jz, jn
- Manipulação de memória: read, store, ldi
- Transferência de dados: moveRegReg
- Subrotinas: call, ret

### Fase 1: Extensões de Registradores (Instruções 12-15)
A primeira expansão introduziu quatro novas instruções focadas em operações diretas entre registradores e incremento de memória:

**addRegReg (Instrução 12)**
- Implementação de soma direta entre dois registradores
- Eliminação da necessidade de usar o acumulador como intermediário
- Melhoria na eficiência de operações aritméticas

**subRegReg (Instrução 13)**
- Subtração direta entre registradores
- Complemento natural da instrução addRegReg
- Expansão das capacidades aritméticas diretas

**jnz (Instrução 14)**
- Salto condicional baseado em valor não-zero
- Complemento das instruções de salto existentes (jz, jn)
- Ampliação do controle de fluxo condicional

**incMem (Instrução 15)**
- Incremento direto de valores na memória
- Otimização de operações comuns em loops e contadores
- Redução do número de instruções necessárias para incrementos

### Fase 2: Operações Registrador-Memória (Instruções 16-17)
A segunda fase introduziu operações híbridas que combinam registradores com acesso direto à memória:

**addRegMem (Instrução 16)**
- Soma o conteúdo de um registrador com um valor da memória
- Resultado armazenado no registrador de origem
- Integração eficiente entre registradores e memória

**subRegMem (Instrução 17)**
- Subtração do conteúdo da memória de um registrador
- Operação complementar à addRegMem
- Flexibilidade em operações aritméticas com dados armazenados

### Fase 3: Sistema de Comparação Avançado (Instruções 18-20)
A fase final implementou um sistema completo de comparação e saltos condicionais:

**cmp (Instrução 18)**
- Comparação entre dois registradores
- Estabelecimento de flags de estado para igualdade
- Base para operações condicionais avançadas

**je (Instrução 19)**
- Salto condicional se os valores comparados forem iguais
- Dependente da instrução cmp para estabelecer o estado
- Implementação de lógica condicional sofisticada

**jne (Instrução 20)**
- Salto condicional se os valores comparados forem diferentes
- Complemento da instrução je
- Completude do sistema de comparação condicional

## Componentes Atualizados

### Assembler
O assembler foi completamente atualizado para reconhecer e processar todas as 21 instruções:
- Reconhecimento de novos códigos de operação (12-20)
- Processamento correto de parâmetros de registradores
- Geração de código de máquina compatível com a arquitetura expandida
- Tratamento adequado de flags de parâmetros para diferentes tipos de instrução

### Architecture.java
A implementação da arquitetura foi expandida sistematicamente:
- Adição de 9 novos microprogramas (instruções 12-20)
- Implementação de flags de comparação para suporte às instruções je/jne
- Manutenção da compatibilidade com as instruções originais
- Validação e teste de todas as funcionalidades

### Sistema de Testes
Desenvolvimento de um programa principal abrangente que demonstra todas as 21 instruções:
- Teste sistemático de cada fase de desenvolvimento
- Validação da integração entre todas as instruções
- Demonstração prática das capacidades expandidas da arquitetura

## Estrutura Final do Projeto

```
Projeto_OAC/
├── src/
│   ├── architecture/
│   │   └── Architecture.java     # Implementação das 21 instruções
│   ├── assembler/
│   │   └── Assembler.java        # Assembler atualizado
│   ├── components/               # Componentes básicos (ULA, Memory, etc.)
│   └── tests/                    # Programas de teste
├── program.dsf                   # Código assembly principal
├── program.dxf                   # Código de máquina principal
├── run_tests.bat                 # Script de execução
└── README.md                     # Documentação
```

## Programa Principal

O programa principal (program.dsf/program.dxf) demonstra todas as 21 instruções de forma sistemática:

1. **Teste das instruções originais (0-11):** Carregamento de valores, operações aritméticas básicas, controle de fluxo e manipulação de memória

2. **Demonstração da Fase 1 (12-15):** Operações diretas entre registradores e incremento de memória

3. **Demonstração da Fase 2 (16-17):** Operações híbridas registrador-memória

4. **Demonstração da Fase 3 (18-20):** Sistema completo de comparação e saltos condicionais

## Execução

Para executar o projeto:

```bash
# Windows
run_tests.bat

# Execução manual
javac -d bin src\architecture\*.java src\components\*.java src\assembler\*.java
javac -d bin -cp bin src\tests\TestMainProgram.java
java -cp bin tests.TestMainProgram
```

## Resultados Alcançados

O projeto atingiu todos os objetivos propostos:
- Implementação completa de 21 instruções funcionais
- Assembler totalmente atualizado e compatível
- Sistema de testes abrangente e automatizado
- Documentação completa do processo de desenvolvimento
- Estrutura organizada e intuitiva para uso acadêmico

A arquitetura expandida demonstra conceitos fundamentais de design de processadores, incluindo extensibilidade, compatibilidade e otimização de operações comuns, fornecendo uma base sólida para o estudo de arquitetura de computadores.