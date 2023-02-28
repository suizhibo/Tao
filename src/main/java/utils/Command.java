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

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    private String projectPath;

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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private String projectName;

    public Command() {
    }

    public void parse(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, "打印命令行帮助信息");
        options.addOption("pn", "project-name", true, "项目名称");
        options.addOption("pp", "project-path", true, "项目路径");
        options.addOption("cp", "class-path", true, "类文件地址");
        options.addOption("lp", "lib-path", true, "库文件地址");
        options.addOption("sp", "sinks-path", true, "设置sink文件地址");
        options.addOption("o", "outPut", true, "结果保存目录");

        CommandLine commandLine = parser.parse(options, args);
        HelpFormatter helpFormatter = new HelpFormatter();
        if (commandLine.hasOption("h")) {
            helpFormatter.printHelp("java -jar RouteCheck.jar", options, true);
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

        if (commandLine.hasOption("pn")) {
            this.setProjectName(commandLine.getOptionValue("pn"));
        }

        if (commandLine.hasOption("pp")) {
            this.setProjectPath(commandLine.getOptionValue("pp"));
        }

        if (commandLine.hasOption("o")) {
            this.setOutPut(commandLine.getOptionValue("o"));
        }
    }
}
