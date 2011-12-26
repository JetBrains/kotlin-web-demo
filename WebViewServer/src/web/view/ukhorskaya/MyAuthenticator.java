package web.view.ukhorskaya;

import com.intellij.openapi.util.Pair;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import web.view.ukhorskaya.server.ServerSettings;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/21/11
 * Time: 12:51 PM
 */
public class MyAuthenticator extends BasicAuthenticator {

    @Override
    public Result authenticate(HttpExchange exchange) {
        String data;
        try {
            data = getPostDataFromRequest(exchange);
        } catch (IllegalArgumentException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    "LOGIN", e, exchange.getRequestURI().toString()));
            return super.authenticate(exchange);
        }
        Pair<String, String> pair = parseData(data);
        if (checkCredentials(pair.first, pair.second)) {
            return new Success(new HttpPrincipal(pair.first, this.getRealm()));
        } else {
            return new Failure(401);
        }
        //To change body of overridden methods use File | Settings | File Templates.
    }

    public MyAuthenticator(String s) {
        super(s);
    }

    @Override
    public boolean checkCredentials(String s, String s1) {
        Map<String, String> map = readUsersFromFile();
        if (map == null) {
            return false;
        }
        String password = map.get(s);
        return password != null && password.equals(s1);
    }

    private Pair<String, String> parseData(String data) {
        return new Pair<String, String>(ResponseUtils.substringBetween(data, "login=", "&"),
                ResponseUtils.substringAfter(data, "password="));
    }

    private String getPostDataFromRequest(HttpExchange exchange) {
        StringBuilder reqResponse = new StringBuilder();
        try {
            reqResponse.append(ResponseUtils.readData(exchange.getRequestBody()));
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("Read data from post request", e, "getPostDataFromRequest " + exchange.getRequestURI()));
            throw new IllegalArgumentException("Cannot read data from file");
        }

        String finalResponse = null;
        try {
            finalResponse = URLDecoder.decode(reqResponse.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("LOGIN", e, "null"));
            throw new IllegalArgumentException("Cannot read data from file");
        }
        return finalResponse;
    }

    public static boolean addUser(String login, String password) {
        File file = new File(ServerSettings.STATISTICS_ROOT + File.separator + "users.xml");
        if (!file.exists()) {
            return false;
        }

        Document document = ResponseUtils.getXmlDocument(file);
        if (document == null) {
            return false;
        }
        NodeList nodeList = document.getElementsByTagName("users");
        Node newNode = document.createElement("user");
        Node loginNode = document.createElement("login");
        loginNode.appendChild(document.createTextNode(login));
        Node passwordNode = document.createElement("password");
        loginNode.appendChild(document.createTextNode(password));
        newNode.appendChild(loginNode);
        newNode.appendChild(passwordNode);
        nodeList.item(0).appendChild(newNode);

        try {
            // Get the first <slide> element in the DOM
            NodeList list = document.getElementsByTagName("users");
            Node node = list.item(0);

            DOMSource source = new DOMSource(node);

            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            FileWriter writer = new FileWriter(file);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            writer.close();
        } catch (Throwable e) {
            //impossible
            e.printStackTrace();
        }
        return true;
    }

    @Nullable
    private Map<String, String> readUsersFromFile() {
        //TODO add exceptions
        Map<String, String> users = new HashMap<String, String>();

        File file = new File(ServerSettings.STATISTICS_ROOT + File.separator + "users.xml");
        if (!file.exists()) {
            return null;
        }

        Document document = ResponseUtils.getXmlDocument(file);
        if (document == null) {
            return null;
        }
        NodeList nodeList = document.getElementsByTagName("user");
        if (nodeList == null) {
            return null;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            NodeList children = nodeList.item(i).getChildNodes();
            //login and password tag
            users.put(children.item(1).getTextContent(), children.item(3).getTextContent());
        }
        return users;
    }
}
