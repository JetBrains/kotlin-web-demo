package web.view.ukhorskaya.examplesLoader;

import org.jetbrains.annotations.Nullable;
import web.view.ukhorskaya.ErrorsWriter;
import web.view.ukhorskaya.ErrorsWriterOnServer;
import web.view.ukhorskaya.server.ServerSettings;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/16/11
 * Time: 6:33 PM
 */
public class ExamplesList {
//    private static final Logger LOG = Logger.getLogger(ExamplesList.class);
    private static final ExamplesList EXAMPLES_LIST = new ExamplesList();

    private ExamplesList() {
        list = new ArrayList<Map<String, String>>();
        generateList();
    }

    private static List<Map<String, String>> list;

    public static ExamplesList getInstance() {
        return EXAMPLES_LIST;
    }

    public List<Map<String, String>> getList() {
        return list;
    }

    public Map<String, String> getMapFromList(int id) {
        if (id < list.size()) {
            return list.get(id);
        }

        ErrorsWriterOnServer.LOG_FOR_EXCEPTIONS.error("There is a request for example with number " + id + " - absent in map");
        return list.get(1);
    }

    private void generateList() {
        File root = new File(ServerSettings.EXAMPLES_ROOT);
        if (root.exists()) {
            File order = checkIsOrderTxtExists(root);
            if (order != null) {
                addInOrder(order, root, true);
            } else {
                addWoOrder(root, true);
            }
        } else {
            ErrorsWriterOnServer.LOG_FOR_EXCEPTIONS.error("Examples root doesn't exists");
            ErrorsWriter.writeErrorToConsole("Examples root doesn't exists");
        }
        ErrorsWriter.writeInfoToConsole("Examples were loaded.");
    }

    private void addWoOrder(File dir, boolean isDirectory) {
        File[] directories = dir.listFiles();
        for (File directory : directories) {
            if ((dir.isDirectory() && isDirectory)
                    || (dir.exists() && !isDirectory)) {
                Map<String, String> map = new HashMap<String, String>();
                if (isDirectory) {
                    map.put("type", "head");
                } else {
                    map.put("type", "content");
                }
                map.put("text", directory.getName());
                list.add(map);

                if (isDirectory) {
                    File order = new File(directory.getAbsolutePath() + File.separator + "order.txt");
                    if (order.exists()) {
                        addInOrder(order, directory, false);
                    } else {
                        addWoOrder(directory, false);
                    }
                }
            } else {
                ErrorsWriterOnServer.LOG_FOR_EXCEPTIONS.error("Incorrect structure for examples (folder - files): " + directory.getAbsolutePath());
            }
        }
    }

    private void addInOrder(File order, File parent, boolean isDirectory) {
        try {
            String[] children = parent.list();
            FileReader fReader = new FileReader(order);
            BufferedReader reader = new BufferedReader(fReader);
            String tmp = "";
            List<String> orderedChildren = new ArrayList<String>();
            while ((tmp = reader.readLine()) != null) {
                File child = new File(parent.getAbsolutePath() + File.separator + tmp);
                if ((child.isDirectory() && isDirectory)
                        || (child.exists() && !isDirectory)) {
                    Map<String, String> map = new HashMap<String, String>();
                    if (isDirectory) {
                        map.put("type", "head");
                    } else {
                        map.put("type", "content");
                    }
                    map.put("text", child.getName());
                    list.add(map);
                    orderedChildren.add(child.getName());

                    if (isDirectory) {
                        File orderChildren = checkIsOrderTxtExists(child);
                        if (orderChildren != null) {
                            addInOrder(orderChildren, child, false);
                        } else {
                            addWoOrder(child, false);
                        }
                    }
                }
            }
            //+1 for order.txt
            if (orderedChildren.size() + 1 < children.length) {
                for (String childName : children) {
                    if (!childName.equals("order.txt") && !childName.equals("helpExamples.xml")) {
                        boolean isAdded = false;
                        for (String orderedChild : orderedChildren) {
                            if (childName.equals(orderedChild)) {
                                isAdded = true;
                            }
                        }
                        if (!isAdded) {
                            File child = new File(parent.getAbsolutePath() + File.separator + childName);
                            if ((child.isDirectory() && isDirectory)
                                    || (child.exists() && !isDirectory)) {
                                Map<String, String> map = new HashMap<String, String>();
                                if (isDirectory) {
                                    map.put("type", "head");
                                } else {
                                    map.put("type", "content");
                                }
                                map.put("text", child.getName());
                                list.add(map);
                                ErrorsWriter.writeErrorToConsole("File/Directory " + childName + " is absent in order.txt and was added at end.");
                                ErrorsWriterOnServer.LOG_FOR_EXCEPTIONS.error("File/Directory " + childName + " is absent in order.txt and was added at end.");

                                if (isDirectory) {
                                    File orderChildren = checkIsOrderTxtExists(child);
                                    if (orderChildren != null) {
                                        addInOrder(orderChildren, child, false);
                                    } else {
                                        addWoOrder(child, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            ErrorsWriterOnServer.LOG_FOR_EXCEPTIONS.error("Cannot find order.txt file: " + order.getAbsolutePath(), e);
        } catch (IOException e) {
            ErrorsWriterOnServer.LOG_FOR_EXCEPTIONS.error("Cannot read order.txt file: " + order.getAbsolutePath(), e);
        }
    }

    @Nullable
    private File checkIsOrderTxtExists(File root) {
        File order = new File(root.getAbsolutePath() + File.separator + "order.txt");
        if (order.exists()) {
            return order;
        }
        return null;
    }

    public static void updateList() {
        list = new ArrayList<Map<String, String>>();
        ExamplesList.getInstance().generateList();

    }
}
