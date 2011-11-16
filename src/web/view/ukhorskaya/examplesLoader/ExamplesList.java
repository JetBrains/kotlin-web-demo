package web.view.ukhorskaya.examplesLoader;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import web.view.ukhorskaya.server.ServerSettings;

import java.io.File;
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
    private static final Logger LOG = Logger.getLogger(ExamplesList.class);
    private static final ExamplesList EXAMPLES_LIST = new ExamplesList();

    private ExamplesList() {
        list = new ArrayList<Map<String, String>>();
        generateList();
    }

    //
//    private static JSONArray list;
  private static List<Map<String, String>> list;

    public static ExamplesList getInstance() {
        return EXAMPLES_LIST;
    }

    //    
    public List<Map<String, String>> getList() {
        return list;
    }

    private void generateList() {
        File root = new File(ServerSettings.EXAMPLES_ROOT);
        File[] directories = root.listFiles();
        for (File directory : directories) {
            if (directory.isDirectory()) {
                File[] examples = directory.listFiles();
                Map<String, String> map = new HashMap<String, String>();
                map.put("type", "head");
                map.put("text", directory.getName());
                list.add(map);
                for (File example : examples) {
                    Map<String, String> exMap = new HashMap<String, String>();
                    exMap.put("type", "content");
                    exMap.put("text", example.getName());
                    list.add(exMap);
                }
            } else {
                LOG.error("There is file in root that isn't a directory: " + directory.getAbsolutePath());
            }
        }
    }

    public static void updateList() {
        ExamplesList.getInstance().generateList();
    }
}
