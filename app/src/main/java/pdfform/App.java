/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package pdfform;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        // 命令行1: 将 with.xml 填充到 src.pdf 中，生成 output.pdf 文件。 
        // command -f src.pdf -w with.xml -o output.pdf
        
        // 命令行2: 提取 src.pdf 中的 XML，并放在同一目录下新建的 src_extracted 文件夹中。
        // command -x src.pdf
        // System.out.println(new App().getGreeting());
        
        // create the Options
        Options options = new Options();
        
        Option optionX = new Option("x", "extract", true, "提取 PDF 文件中的表格数据，保存在同目录的 filename_extracted/ 新建文件夹中");
        optionX.setArgName("srcFile");
        
        Option optionF = new Option("f", "fillout", true, "fillout a src PDF with one XML, and generate an output PDF.");
        optionF.setArgName("srcFile");
        
        options.addOption(Option.builder("w")
                            .longOpt("with")
                            .hasArg()
                            .argName("XML")
                            .desc("与 f 参数一起使用，表示要填充的 XML")
                            .build());
        options.addOption(Option.builder("o")
                            .longOpt("output")
                            .hasArg()
                            .argName("outFile")
                            .desc("与 f 参数一起使用，表示输出的文件路径")
                            .build());
        options.addOption(Option.builder("h").longOpt("help").desc("Display usage.").build());

        OptionGroup optionGroup = new OptionGroup();
        optionGroup.addOption(optionX);
        optionGroup.addOption(optionF);
        options.addOptionGroup(optionGroup);

        HelpFormatter helpFormatter = new HelpFormatter();
        String helpHeader = "==============================\n" +
                            "命令行1: 将 with.xml 填充到 src.pdf 中，生成 output.pdf 文件。\n" +
                            "  command -f src.pdf -w with.xml -o output.pdf \n" +
                            "命令行2: 提取 src.pdf 中的 XML，并放在同一目录下新建的 src_extracted 文件夹中。\n" +
                            "  command -x src.pdf \n" +
                            "==============================\n";
        
         // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("x")) {
                String srcFile = cmd.getOptionValue("x");
                System.out.println("Args: src=" + srcFile);

                App.extractPDF(srcFile);

            } else if (cmd.hasOption("f")) {
                String srcFile = cmd.getOptionValue("f");
                String xmlFile = cmd.getOptionValue("w");
                String outputFile = cmd.getOptionValue("o");
                System.out.println(String.format("Args: src=%s, with=%s, output=%s", srcFile, xmlFile, outputFile));
                
                App.filloutPDF(srcFile, xmlFile, outputFile);

            } else {
                helpFormatter.printHelp("pdfform", helpHeader, options, "", true);
            }
        }
        catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("命令行参数错误.  Reason: " + exp.getMessage());
            
            // automatically generate the help statement
            helpFormatter.printHelp("pdfform", helpHeader, options, "", true);
        }
    }

    public static void extractPDF(String src) {
        Path srcPath = Paths.get(src).toAbsolutePath();
        if (!Files.exists(srcPath)) {
            System.err.println("文件不存在: " + srcPath.toString());
            return ;
        }

        Path destPath = srcPath.getParent().resolve(com.google.common.io.Files.getNameWithoutExtension(src) + "_extracted");
        
        try {
            Files.createDirectories(destPath);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        ExtractorUsingOpenpdf.extractAcroformFields(src, destPath.resolve("acrofields.txt").toString());

        ExtractorUsingOpenpdf.extractXfa(src, destPath.toString());
    }

    public static void filloutPDF(String src, String xml, String output) {
        try {
            FillerUsingOpenpdf.fillXfaData(src, output, new FileInputStream(xml));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
