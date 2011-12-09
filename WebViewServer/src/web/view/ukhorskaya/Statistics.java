package web.view.ukhorskaya;

import org.apache.commons.lang.math.RandomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import web.view.ukhorskaya.server.ServerSettings;
import web.view.ukhorskaya.session.SessionInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/8/11
 * Time: 2:52 PM
 */

public class Statistics {
    public static int NUMBER_OF_USERS = 0;

    private final String numberOfUsers = "$NUMBER_OF_USERS$";
    private final String numberOfUsersPerDay = "$NUMBER_OF_USERS_PER_DAY$";
    private final String numberOfRequestPerUser = "$NUMBER_OF_REQUEST_PER_USER$";
    private final String numberOfRunRequestPerUser = "$NUMBER_OF_RUN_REQUEST_PER_USER$";

    //id - userInfo
    Map<String, UserInfo> userInfoMap = new HashMap<String, UserInfo>();
    Set<ErrorElement> errorElementSet = new HashSet<ErrorElement>();

    public String getNumberOfUsers() {
        return String.valueOf(NUMBER_OF_USERS);
    }

    public String getNumberOfUsersPerDay() {
        analyzeLog();
        return String.valueOf(NUMBER_OF_USERS);
    }

    public String getNumberOfRequestPerUser() {
        return String.valueOf(10);
    }

    public String getNumberOfRunRequestPerUser() {
        return String.valueOf(10);
    }

    public String writeStatistics(String response) {
        StringBuilder buffer = new StringBuilder(response);
        int pos = buffer.indexOf(numberOfUsers);
        buffer.replace(pos, pos + numberOfUsers.length(), getNumberOfUsers());

        pos = buffer.indexOf(numberOfUsersPerDay);
        buffer.replace(pos, pos + numberOfUsersPerDay.length(), getNumberOfUsersPerDay());

        pos = buffer.indexOf(numberOfRequestPerUser);
        buffer.replace(pos, pos + numberOfRequestPerUser.length(), getNumberOfRequestPerUser());

        pos = buffer.indexOf(numberOfRunRequestPerUser);
        buffer.replace(pos, pos + numberOfRunRequestPerUser.length(), getNumberOfRunRequestPerUser());

        return buffer.toString();
    }

    private void analyzeLog() {
        File dir = new File("logs");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    int result = (Long.valueOf(f1.lastModified()).compareTo(f2.lastModified()));
                    if (result == 0) {
                        return result;
                    } else {
                        return -result;
                    }
                }
            });

            for (File file : files) {
                if (file.getName().contains("kotlincompiler.log")) {
                    analyzeInfoLog(file);
                } else if (file.getName().equals("exceptions.log")) {
                    analyzeExceptionLog(file);
                }
            }
        }
    }

    private void analyzeInfoLog(File file) {

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            //impossible
            return;
        }
        String tmp = "";
        try {
            while ((tmp = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(tmp, " ");
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    if (token.contains("userId")) {
                        String id = ResponseUtils.substringAfter(token, "userId=");
                        UserInfo info = userInfoMap.get(id);
                        if (info == null) {
                            info = new UserInfo();
                        } else {
                        }
                        info.numberOfRequest++;
                        token = tokenizer.nextToken();
                        if (token.equals("RunUserProgram")) {
                            info.numberOfRunRequest++;
                        }
                        userInfoMap.put(id, info);
                    }
                }
            }
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(),
                    e, file.getAbsolutePath()));
        }
        for (String id : userInfoMap.keySet()) {
            System.out.println(id + " " + userInfoMap.get(id).numberOfRequest + " " + userInfoMap.get(id).numberOfRunRequest);
        }
    }

    private void analyzeExceptionLog(File file) {
        File ex;
        try {
            ex = File.createTempFile("exception", "log");
            FileWriter writer = new FileWriter(ex);
            writer.write("<?xml version=\"1.0\"?>");
            writer.write("<errors>");
            Pattern pattern = Pattern.compile("[0-9]*-[0-9]*-[0-9]* [0-9]*:[0-9]*:[0-9]* [(][\\w]*.java [0-9]*[)] [\\[][\\w]*[\\]] ");

            Matcher matcher;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tmp;
            while ((tmp = reader.readLine()) != null) {
                matcher = pattern.matcher(tmp);
                if (matcher.find()) {
                    if (matcher.end() - matcher.start() != tmp.length()) {
                        writer.write(tmp.substring(0, matcher.start()) + tmp.substring(matcher.end()));
                    }
//                      System.out.println(matcher.group());
                } else {
                    writer.write(tmp);
                }
            }

            writer.write("</errors>");
            writer.close();
//            System.out.println(ResponseUtils.readData(new FileReader(ex), true));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(ex);
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), e, file.getAbsolutePath()
            ));
            return;
        } catch (ParserConfigurationException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), e, file.getAbsolutePath()
            ));
            return;
        } catch (SAXException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), e, file.getAbsolutePath()
            ));
            return;
        }
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getElementsByTagName("error");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            ErrorElement el = new ErrorElement(nodeList.item(i));
            boolean isAdd = errorElementSet.add(el);
        }
        filterSet();
        ex.delete();
        for (ErrorElement el : errorElementSet) {
            System.out.println(ErrorWriter.getExceptionForLog(el.type, el.message, el.stack, el.moreInfo));
        }

    }

    private void filterSet() {
        int index1 = 0;
        HashSet<ErrorElement> newErrorElements = new HashSet<ErrorElement>();
        for (ErrorElement e1 : errorElementSet) {
            ++index1;
            int index2 = 0;
            for (ErrorElement e2 : errorElementSet) {
                ++index2;
                if (index1 != index2) {
                    if (!e1.equals(e2)){
                        newErrorElements.add(e2);
                    }
                }
            }
        }
        errorElementSet = newErrorElements;
    }

    class UserInfo {
        public int numberOfRunRequest = 0;
        public int numberOfRequest = 0;
        public long startTime;
    }

    class ErrorElement {
        public String type;
        public String message;
        public String stack;
        public String moreInfo;

        ErrorElement(Node node) {
            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                if (childNodes.item(j).getNodeName().equals("type")) {
                    type = childNodes.item(j).getTextContent();
                } else if (childNodes.item(j).getNodeName().equals("message")) {
                    message = childNodes.item(j).getTextContent();
                } else if (childNodes.item(j).getNodeName().equals("stack")) {
                    stack = childNodes.item(j).getTextContent();
                } else if (childNodes.item(j).getNodeName().equals("moreinfo")) {
                    moreInfo = childNodes.item(j).getTextContent();
                }
            }
        }

        @Override
        public int hashCode() {
            return type.hashCode() + message.hashCode() + stack.hashCode() + moreInfo.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ErrorElement) {
                ErrorElement newElement = (ErrorElement) obj;
                if (stack.equals(newElement.stack)) {
                    if (message.equals(newElement.message)) {
                        if (moreInfo.equals(newElement.moreInfo)) {
                            return true;
                        } else if (!moreInfo.contains(newElement.moreInfo)) {
                            return true;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }

}
