@echo off

javac -d ../src/bin --module-path "C:\Program Files\Java\openjfx-21.0.3_windows-x64_bin-sdk\javafx-sdk-21.0.3\lib" --add-modules javafx.controls,javafx.fxml,javafx.media ../src/main/java/*.java
java -cp ../src/bin --module-path "C:\Program Files\Java\openjfx-21.0.3_windows-x64_bin-sdk\javafx-sdk-21.0.3\lib" --add-modules javafx.controls,javafx.fxml,javafx.media ../src/main/java/Client.java