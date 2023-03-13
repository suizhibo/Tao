package utils;


import org.apache.commons.cli.*;


public class Command {
    private String classPath;
    private String libPath;

    public String getSinksPath() {
        return sinksPath;
    }

    public void setSinksPath(String sinksPath) {
        this.sinksPath = sinksPath;
    }

    private String sinksPath;

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getLibPath() {
        return libPath;
    }

    public void setLibPath(String libPath) {
        this.libPath = libPath;
    }

    public String getOutPut() {
        return outPut;
    }

    public void setOutPut(String outPut) {
        this.outPut = outPut;
    }

    private String outPut;

    public Command() {
    }

    public void parse(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, "打印命令行帮助信息");
        options.addOption("cp", "class-path", true, "类文件地址");
        options.addOption("lp", "lib-path", true, "库文件地址");
        options.addOption("sp", "sinks-path", true, "设置sink文件地址");
        options.addOption("o", "outPut", true, "结果保存目录");

        CommandLine commandLine = parser.parse(options, args);
        HelpFormatter helpFormatter = new HelpFormatter();
        if (commandLine.hasOption("h")) {
            helpFormatter.printHelp("java -jar Tao.jar", options, true);
            System.exit(0);
        }

        if (commandLine.hasOption("cp")) {
            this.setClassPath(commandLine.getOptionValue("cp"));
        }

        if (commandLine.hasOption("lp")) {
            this.setLibPath(commandLine.getOptionValue("lp"));
        }

        if (commandLine.hasOption("sp")) {
            this.setSinksPath(commandLine.getOptionValue("sp"));
        }

        if (commandLine.hasOption("o")) {
            this.setOutPut(commandLine.getOptionValue("o"));
        }
    }
}
