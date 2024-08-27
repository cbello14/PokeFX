@echo off
goto start
REM More general script for running Pokemon Battler
REM Requires path to FX passed as argument 1
REM Allows invocation from anywhere on system

:start
echo Comipling and running pokemon battler game

REM CD to correct directory
CD %~dp0
REM CD ../

REM Check for JavaFX install
SET fxpath=%1

if [%fxpath%] == [] (
    ECHO ERROR: Must provide path to JavaFX Installation to use script
    ECHO Please have usage match "universalWindows.bat <your/FX/Install/path>"
    EXIT /B
) else (
    ECHO Using JavaFX located at %fxpath%
)

REM Generate javadocs
ECHO This is a placeholder for javadocs

REM Compile Application
ECHO Compiling application...
javac -d ../src/bin --module-path %fxpath% --add-modules javafx.controls,javafx.fxml,javafx.media ../src/main/java/*.java

REM Start Application
ECHO Starting application...
java -cp ../src/bin --module-path %fxpath% --add-modules javafx.controls,javafx.fxml,javafx.media main.java.Client