package web.view.ukhorskaya.examplesLoader;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.examplesLoader.ExamplesList;
import web.view.ukhorskaya.server.ServerSettings;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/16/11
 * Time: 5:29 PM
 */

public class ExamplesLoader {
    private static final Logger LOG = Logger.getLogger(ExamplesLoader.class);

    public ExamplesLoader() {
    }

    public String getResult(String fileName) {
        File example = new File(ServerSettings.EXAMPLES_ROOT + File.separator + fileName);
        if (!example.exists()) {
            LOG.error("Cannot find example with file name: " + example.getAbsolutePath());
            return "[{\"text\":\"Cannot find this example. Please choose an other example.\"}]";
        }

        String fileContent;
        try {
            fileContent = ResponseUtils.readData(new FileReader(example), true);
        } catch (IOException e) {
            LOG.error("Cannot read content for example with file name: " + example.getAbsolutePath());
            return "[{\"text\":\"Cannot load this example. Please choose an other example.\"}]";
        }
        JSONArray response = new JSONArray();
        Map<String, String> map = new HashMap<String, String>();
        map.put("text", fileContent);
        response.put(map);
        return response.toString();
    }

    public String getExamplesList() {
        JSONArray response = new JSONArray();
        List<Map<String, String>> list = ExamplesList.getInstance().getList();
        for (Map<String, String> map : list) {
            response.put(map);
        }
        return response.toString();
    }
}
