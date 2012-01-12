package web.view.ukhorskaya.examplesLoader;

import org.json.JSONArray;
import web.view.ukhorskaya.ErrorWriter;
import web.view.ukhorskaya.ErrorWriterOnServer;
import web.view.ukhorskaya.ResponseUtils;
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
//    private static final Logger LOG = Logger.getLogger(ExamplesLoader.class);

    public ExamplesLoader() {
    }

    public String getResult(int id, String headName) {
        Map<String, String> fileObj = ExamplesList.getInstance().getMapFromList(id);
        if (!fileObj.get("type").equals("content")) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error("Returned head while loading an example: " + id);
            return "[{\"text\":\"Cannot find this example. Please choose another example.\"}]";
        }
        String fileName = fileObj.get("text");
        headName = headName.replaceAll("%20", " ");
        File example = new File(ServerSettings.EXAMPLES_ROOT + File.separator + headName + File.separator + fileName + ".kt");
        if (!example.exists()) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error("Cannot find example with file name: " + example.getAbsolutePath());
            return "[{\"text\":\"Cannot find this example. Please choose another example.\"}]";
        }

        String fileContent;
        FileReader reader = null;
        try {
            reader = new FileReader(example);
            fileContent = ResponseUtils.readData(reader, true);
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error("Cannot read content for example with file name: " + example.getAbsolutePath());
            return "[{\"text\":\"Cannot load this example. Please choose another example.\"}]";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("Load examples", e, id + " " + headName));
            }
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
