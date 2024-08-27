@echo off
CD %~dp0
javac -d ../src/bin --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml,javafx.media ../src/main/java/*.java
java -cp ../src/bin --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml,javafx.media ../src/main/java/Client.java