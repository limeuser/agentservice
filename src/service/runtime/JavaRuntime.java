package service.runtime;

import mjoys.util.Logger;

public class JavaRuntime {
    private static final Logger logger = new Logger().addPrinter(System.out);
    javac -Djava.ext.dirs=./libs -d bin .\src\*.java
    public JavaRuntime() {
        
    }
}
