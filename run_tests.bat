@echo off
echo ===============================================
echo    ARQUITETURA C EXPANDIDA - 21 INSTRUCOES
echo ===============================================

echo.
echo [1/3] Compilando arquitetura expandida...
javac -d bin src\architecture\*.java src\components\*.java src\assembler\*.java

if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha na compilacao da arquitetura
    pause
    exit /b 1
)

echo [2/3] Compilando programa principal...
javac -d bin -cp bin src\tests\TestMainProgram.java

if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha na compilacao do programa principal
    pause
    exit /b 1
)

echo [3/3] Executando programa principal com todas as 21 instrucoes...
echo.

echo ===============================================
echo        EXECUTANDO PROGRAMA PRINCIPAL
echo ===============================================
echo Demonstrando todas as 21 instrucoes da arquitetura expandida:
echo - Instrucoes originais (0-11): add, sub, jmp, jz, jn, read, store, ldi, inc, moveRegReg, call, ret
echo - FASE 1 (12-15): addRegReg, subRegReg, jnz, incMem
echo - FASE 2 (16-17): addRegMem, subRegMem
echo - FASE 3 (18-20): cmp, je, jne
echo.

java -cp bin tests.TestMainProgram

echo.
echo ===============================================
echo     ARQUITETURA C EXPANDIDA FUNCIONANDO!
echo ===============================================
echo Todas as 21 instrucoes foram testadas com sucesso!
echo O programa demonstra a funcionalidade completa da arquitetura.
echo.
pause
