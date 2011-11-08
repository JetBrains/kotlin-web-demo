package web.view.ukhorskaya;

import com.intellij.openapi.util.Ref;

import java.io.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/28/11
 * Time: 12:38 PM
 */


public class JavaRunner {
    //public static final String OUTPUT_DIRECTORY = "C:\\Documents and Settings\\Natalia.Ukhorskaya\\Local Settings\\Temp\\newProject";
    public static String OUTPUT_DIRECTORY = "C:\\Development\\testProject\\out\\";

    private List<String> files;

    public JavaRunner(List<String> files) {
        this.files = files;
    }

    public String run() {
        final StringBuilder resultString = new StringBuilder();
        String s = "";
        try {
            Process p = null;
            String commandString = generateCommandString();

            try {
                final Ref<Process> refProcess = new Ref<Process>();
                p = Runtime.getRuntime().exec(commandString);
                refProcess.set(p);
                Timer timer = new Timer(true);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        refProcess.get().destroy();
                        resultString.append("<font color=\"red\">Timeout exception: impossible to execute your program because it take a lot of time for compilation and execution.");
                    }
                }, 5000);
            } catch (Throwable e) {
                System.out.println("SECURITY EXCEPTION");
                e.printStackTrace();
            }


            InputStream inputStream = p.getInputStream();

            BufferedReader stdInput = null;
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            if (inputStream.available() > 0) {
                System.out.println("bbb");
                stdInput = new BufferedReader(new InputStreamReader(inputStream));
            } else {
                //Timeout for read inputStream
                System.err.println("p.getInputStream().available() return 0");
                long time = System.currentTimeMillis();
                while (!isInputStreamReady(inputStream)) {
                }
                stdInput = new BufferedReader(new InputStreamReader(inputStream));
                System.out.println("wait for inputStream " + (System.currentTimeMillis() - time));
            }


            // read the output from the command
            //TODO check why there is a problem with input stream
            if ((stdInput != null) && (stdInput.ready())) {
                while ((s = stdInput.readLine()) != null) {
                    resultString.append(s);
                    resultString.append("<br>");
                }
            }

            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                resultString.append("<font color=\"red\">");
                resultString.append(s);
                resultString.append("<br>");
                resultString.append("</font>");
                System.err.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        } finally {
            for (String file : files) {
                deleteFile(file);
            }
        }

        return resultString.toString();
    }

    private int counter = 0;

    private boolean isInputStreamReady(InputStream is) {
        counter++;
        try {

            waitForInputStream(150);
            if (is.available() > 0) {
                return true;
            }
        } catch (IOException e) {
            System.out.println("Error while reading inputStream");
            e.printStackTrace();
        }
        if (counter > 20) {
            return true;
        }
        return false;
    }

    private void waitForInputStream(int timeout) {
        long endTimeMillis = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() <= endTimeMillis) {
        }
    }

    private void deleteFile(String path) {
        File f = new File(JavaRunner.OUTPUT_DIRECTORY + path);
        if (f.exists()) {
            f.delete();
        }
        /*if (f.exists()) {
            if (!f.getParent().equals(JavaRunner.OUTPUT_DIRECTORY)) {
                deleteFile(f.getParent());
            } else {
                f.delete();
            }
        } else if (f.isDirectory()) {
            f.delete();
        }*/
    }

    private String generateCommandString() {
        StringBuilder builder = new StringBuilder("java ");
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
