package service.runtime;

public abstract class LanguageRuntime {
    private String compiler;
    private String runner;
    private String bin;
    private String home;
    private String tar;
    
    public abstract String compile(String root);
    public abstract String deploy(String root);
    public abstract int run(String root);
    public abstract void kill(int pid);
}
