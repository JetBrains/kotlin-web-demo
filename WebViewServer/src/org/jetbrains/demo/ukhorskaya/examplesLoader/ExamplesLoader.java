package org.jetbrains.demo.ukhorskaya.examplesLoader;

import com.intellij.openapi.util.Pair;
import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.ErrorWriterOnServer;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.json.JSONArray;

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
    
    public String getResultByExampleName(String name) {
        Pair<Integer, String> pair = ExamplesList.getInstance().findExampleByName(name);
        if (pair != null) {
            return getResult(pair.first, pair.second);
        }
        return "[{\"text\":\"Cannot find this example. Please choose another example.\"}]";
    }

    public String getResultByNameAndHead(String param) {
        String name = ResponseUtils.getExampleOrProgramNameByUrl(param);
        String folder = ResponseUtils.getExampleOrProgramFolderByUrl(param);
        Pair<Integer, String> pair = ExamplesList.getInstance().findExampleByNameAndHead(name, folder);
        if (pair != null) {
            return getResult(pair.first, pair.second);
        }
        return "[{\"text\":\"Cannot find this example. Please choose another example.\"}]";
    }

    public String getResult(int id, String folderName) {
        Map<String, String> fileObj = ExamplesList.getInstance().getMapFromList(id);
        if (!fileObj.get("type").equals("content")) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                    new UnsupportedOperationException("Incorrect path to example"),
                    SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), String.valueOf(id) + " " + folderName);
            return "[{\"text\":\"Cannot find this example. Please choose another example.\"}]";
        }
        String fileName = fileObj.get("text");
        folderName = folderName.replaceAll("_", " ");
        File example = new File(ServerSettings.EXAMPLES_ROOT + File.separator + folderName + File.separator + fileName + ".kt");
        if (!example.exists()) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                    new UnsupportedOperationException("Cannot find an example"),
                    SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), example.getAbsolutePath());
            return "[{\"text\":\"Cannot find this example. Please choose another example.\"}]";
        }

        String fileContent;
        FileReader reader = null;
        try {
            reader = new FileReader(example);
            fileContent = ResponseUtils.readData(reader, true);
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), example.getAbsolutePath());
            return "[{\"text\":\"Cannot load this example. Please choose another example.\"}]";
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        SessionInfo.TypeOfRequest.LOAD_EXAMPLE.name(), id + " " + folderName);
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
