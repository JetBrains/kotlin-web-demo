package web.view.ukhorskaya;

import java.io.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/28/11
 * Time: 12:38 PM
 */


public class JavaRunner {
    //public static final String OUTPUT_DIRECTORY = "C:\\Documents and Settings\\Natalia.Ukhorskaya\\Local Settings\\Temp\\newProject";
    public static final String OUTPUT_DIRECTORY = "C:\\Development\\testProject\\out\\";

    private List<String> files;
    private SecurityManager securityManager;

    public JavaRunner(List<String> files) {
        this.files = files;
        //this.securityManager = securityManager;
    }

    public String run() {
        StringBuilder resultString = new StringBuilder();
        String s = "";
        try {
            Process p = null;
            String commandString = generateCommandString();
            try {
                p = Runtime.getRuntime().exec(commandString);
            } catch (Throwable e) {
                System.out.println("SECURITY EXCEPTION");
                e.printStackTrace();
            }
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            //if (stdInput.ready()) {
                while ((s = stdInput.readLine()) != null) {
                    resultString.append(s);
                    resultString.append("<br>");
                }
            //}

            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                resultString.append("<font color=\"red\">");
                resultString.append(s);
                resultString.append("<br>");
                resultString.append("</font>");
                System.err.println(s);
            }
        } catch (Exception e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }

        return resultString.toString();
    }

    private String generateCommandString() {
        StringBuilder builder = new StringBuilder("C:\\Program Files\\Java\\jdk1.6.0_26\\bin\\java.exe ");
        builder.append("-classpath ");
        //builder.append(System.getProperty("java.class.path"));
        builder.append(JavaRunner.OUTPUT_DIRECTORY);
        builder.append(";");
        builder.append("C:\\jet\\jet\\dist\\kotlin-compiler.jar;");
        builder.append("C:\\jet\\jet\\dist\\kotlin-runtime.jar ");
        builder.append("-Djava.security.manager ");
        for (String file : files) {
            builder.append(modifyClassNameFromPath(file));
            builder.append(" ");
        }
        return builder.toString();
    }

    private String modifyClassNameFromPath(String path) {
        String name = "";
        int pos = path.indexOf("out\\");
        if (pos != -1) {
            name = path.substring(pos + 4);
        } else {
            name = path;
        }
        pos = name.indexOf(".class");
        if (pos != -1) {
            name = name.substring(0, pos);
        }
        name = name.replaceAll("/", ".");
        return name;
    }

    /*class Source extends SimpleJavaFileObject {
    
    
        public Source(File file) {
            super(file.toURI(), Kind.SOURCE);
        }
    
    
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    
            StringBuilder sb = new StringBuilder("");
            try {
                    File file = new File(uri);
                    FileReader fr = new FileReader(file);
                    BufferedReader br = new BufferedReader(fr);
    
                    sb = new StringBuilder((int) file.length());
                    String line = "";
                    while ((line = br.readLine()) != null) {
                            sb.append(line);
                            sb.append("\n");
                    }
            } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
    
            return sb.toString();
        }
    }*/


}
