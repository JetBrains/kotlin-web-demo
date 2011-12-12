package web.view.ukhorskaya;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import web.view.ukhorskaya.session.SessionInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
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
    private static int NUMBER_OF_USERS = 0;

    private final String numberOfUsers = "$NUMBER_OF_USERS$";
    private final String numberOfUsersPerDay = "$NUMBER_OF_USERS_PER_DAY$";
    private final String numberOfRequestPerUser = "$NUMBER_OF_REQUEST_PER_USER$";
    private final String numberOfRunRequestPerUser = "$NUMBER_OF_RUN_REQUEST_PER_USER$";
    private final String numberOfHighlightRequestPerUser = "$NUMBER_OF_HIGHLIGHT_REQUEST_PER_USER$";
    private final String numberOfCompleteRequestPerUser = "$NUMBER_OF_COMPLETE_REQUEST_PER_USER$";

    //id - userInfo
    private Map<String, UserInfo> userInfoMap = new HashMap<String, UserInfo>();
    //    private Set<ErrorElement> errorElementSet = new HashSet<ErrorElement>();
    //stacktrace - description
    private Map<String, ErrorElement> errorElementSet = new HashMap<String, ErrorElement>();
    private List<Integer> usersPerDayList = new ArrayList<Integer>();


    private Date dateFrom;
    private Date dateTo;

    boolean isInDateRange = true;

    private static Statistics instance = new Statistics();

    private Statistics() {
        File file = new File("counter.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);

                writer.write(URLDecoder.decode("0", "UTF-8"));
                writer.close();
            } catch (IOException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), e, file.getAbsolutePath())
                );
            }
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tmp = reader.readLine();
            NUMBER_OF_USERS = Integer.parseInt(tmp);
        } catch (FileNotFoundException e) {
            //Impossible
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), e, file.getAbsolutePath())
            );
        }

    }

    public static Statistics getInstance() {
        return instance;
    }

    public static void incNumberOfUsers() {
        NUMBER_OF_USERS++;
        writeToCounterFile();
    }

    private static void writeToCounterFile() {
        File file = new File("counter.txt");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(URLDecoder.decode(String.valueOf(NUMBER_OF_USERS), "UTF-8"));
            writer.close();
        } catch (IOException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), e, file.getAbsolutePath())
            );
        }
    }

    public static String getNumberOfUsers() {
        return String.valueOf(NUMBER_OF_USERS);
    }

    public String getNumberOfUsersPerDay() {
        int i = 0;
        for (Integer count : usersPerDayList) {
            i += count;
        }
        return String.valueOf(i / usersPerDayList.size());
    }


    public String writeStatistics(String response) {
        analyzeLog();
        StringBuilder buffer = new StringBuilder(response);

        double totalNumberOfRequestsPerUser = 0;
        double totalNumberOfRunRequestsPerUser = 0;
        double totalNumberOfHighlightRequestsPerUser = 0;
        double totalNumberOfCompleteRequestsPerUser = 0;
        for (UserInfo userInfo : userInfoMap.values()) {
            totalNumberOfRequestsPerUser += userInfo.numberOfRequest;
            totalNumberOfRunRequestsPerUser += userInfo.numberOfRunRequest;
            totalNumberOfHighlightRequestsPerUser += userInfo.numberOfHighlightRequest;
            totalNumberOfCompleteRequestsPerUser += userInfo.numberOfCompleteRequest;
        }
        totalNumberOfRequestsPerUser = userInfoMap.size();
        totalNumberOfRunRequestsPerUser /= userInfoMap.size();
        totalNumberOfHighlightRequestsPerUser /= userInfoMap.size();
        totalNumberOfCompleteRequestsPerUser /= userInfoMap.size();


        int pos = buffer.indexOf(numberOfUsers);
        buffer.replace(pos, pos + numberOfUsers.length(), NUMBER_OF_USERS + " " + userInfoMap.size());

        pos = buffer.indexOf(numberOfUsersPerDay);
        buffer.replace(pos, pos + numberOfUsersPerDay.length(), getNumberOfUsersPerDay());

        pos = buffer.indexOf(numberOfRequestPerUser);
        buffer.replace(pos, pos + numberOfRequestPerUser.length(), String.valueOf(totalNumberOfRequestsPerUser));

        pos = buffer.indexOf(numberOfRunRequestPerUser);
        buffer.replace(pos, pos + numberOfRunRequestPerUser.length(), String.valueOf(totalNumberOfRunRequestsPerUser));

        pos = buffer.indexOf(numberOfHighlightRequestPerUser);
        buffer.replace(pos, pos + numberOfHighlightRequestPerUser.length(), String.valueOf(totalNumberOfHighlightRequestsPerUser));

        pos = buffer.indexOf(numberOfCompleteRequestPerUser);
        buffer.replace(pos, pos + numberOfCompleteRequestPerUser.length(), String.valueOf(totalNumberOfCompleteRequestsPerUser));

        return buffer.toString();
    }

    private void analyzeLog() {
        userInfoMap = new HashMap<String, UserInfo>();
        errorElementSet = new HashMap<String, ErrorElement>();
        usersPerDayList = new ArrayList<Integer>();
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
                } else if (dateFrom != null && dateTo != null && file.getName().contains("exceptions.log")) {
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
        String tmp;
        try {
            @Nullable Date lastDate;
            @Nullable Date curDate = null;
            int numberOfUsersTmp = 0;
            while ((tmp = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(tmp, " ");
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();

                    if (token.equals("Number_of_users_since_start_server")) {
                        try {
                            if (curDate != null) {
                                lastDate = curDate;
                                curDate = DateFormat.getInstance().parse(tmp.substring(0, 8) + " 0:0 AM, PDT");
                                if (!lastDate.equals(curDate)) {
                                    usersPerDayList.add(numberOfUsersTmp);
                                    numberOfUsersTmp = 1;
                                } else {
                                    numberOfUsersTmp++;
                                }
                            } else {
                                curDate = DateFormat.getInstance().parse(tmp.substring(0, 8) + " 0:0 AM, PDT");
                                numberOfUsersTmp++;
                            }

                        } catch (ParseException e) {
                            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(),
                                    e, tmp));
                        }
                    }
                    if (token.equals("type=INC_NUMBER_OF_REQUESTS")) {
                        token = tokenizer.nextToken();
                        if (token.contains("userId=")) {
                            String id = ResponseUtils.substringAfter(token, "userId=");
                            UserInfo info = userInfoMap.get(id);
                            if (info == null) {
                                info = new UserInfo();
                            } else {
                            }
                            info.numberOfRequest++;
                            token = tokenizer.nextToken();
                            if (token.equals("message=" + SessionInfo.TypeOfRequest.RUN.name())) {
                                info.numberOfRunRequest++;
                            } else if (token.equals("message=" + SessionInfo.TypeOfRequest.HIGHLIGHT.name())) {
                                info.numberOfHighlightRequest++;
                            } else if (token.equals("message=" + SessionInfo.TypeOfRequest.COMPLETE.name())) {
                                info.numberOfCompleteRequest++;
                            }

                            userInfoMap.put(id, info);
                        }
                    }
                }
            }
            usersPerDayList.add(numberOfUsersTmp);
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
            Pattern logStr = Pattern.compile("[0-9]*/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]* [(][\\w]*.java [0-9]*[)] [\\[][\\w]*[\\]] ");

            Matcher matcher;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tmp;
            while ((tmp = reader.readLine()) != null) {
                matcher = logStr.matcher(tmp);
                if (matcher.find()) {
                    if (tmp.length() > 8) {
                        int inRange = compareDate(tmp.substring(0, 8) + " 1:0 AM, PDT");
                        switch (inRange) {
                            case -2: {
                                return;
                            }
                            case -1: {
                                isInDateRange = false;
                                continue;
                            }
                            case 0: {
                                isInDateRange = true;
                                break;
                            }
                            case 1: {
                                isInDateRange = false;
                                continue;
                            }
                        }
                        if (isInDateRange) {
                            writer.write("<error>\n");
                            writer.write("<date>" + tmp.substring(0, 8) + " 1:0 AM, PDT" + "</date>");
                        }
                    }

                    if (matcher.end() - matcher.start() != tmp.length()) {
                        writer.write(tmp.substring(0, matcher.start()) + tmp.substring(matcher.end()));
                    }
                } else {
                    if (isInDateRange && !tmp.equals("<error>")) {
                        writer.write(tmp + "\n");
                    }
                }
            }

            writer.write("</errors>");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
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
            NodeList childNodes = nodeList.item(i).getChildNodes();
            String more = "";
            String message = "";
            String stack = "";
            for (int j = 0; j < childNodes.getLength(); j++) {
                if (childNodes.item(j).getNodeName().equals("type")) {
                    more += childNodes.item(j).getTextContent();
                } else if (childNodes.item(j).getNodeName().equals("message")) {
                    message = childNodes.item(j).getTextContent();
                } else if (childNodes.item(j).getNodeName().equals("date")) {
                    more += childNodes.item(j).getTextContent() + "\n";
                } else if (childNodes.item(j).getNodeName().equals("moreinfo")) {
                    more += childNodes.item(j).getTextContent();
                }
                if (childNodes.item(j).getNodeName().equals("stack")) {
                    stack = childNodes.item(j).getTextContent();
                }
            }
            ErrorElement errorElement = errorElementSet.get(stack);
            if (errorElement == null) {
                errorElement = new ErrorElement(message, more);
            } else {
                if (!errorElement.moreinfo.contains(more)) {
                    errorElement.moreinfo += "\n" + more;
                }
            }
            errorElementSet.put(stack, errorElement);
        }
//        filterSetByDate("12/12/11 0:0 AM, PDT", "12/13/11 0:0 AM, PDT");
        //filterSet();
        ex.delete();
        for (String error : errorElementSet.keySet()) {
            ErrorElement el = errorElementSet.get(error);
            System.out.println(ErrorWriter.getExceptionForLog(el.message, el.moreinfo, error));
        }
        /*for (ErrorElement el : errorElementSet) {
        }*/

    }

    private int compareDate(String dateStr) {
        try {
            Date date = DateFormat.getInstance().parse(dateStr);
            if (date.after(dateFrom) && date.before(dateTo)) {
                return 0;
            } else if (!date.after(dateFrom)) {
                return -1;
            } else if (!date.before(dateTo)) {
                return 1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -2;
    }

    /*private void filterSetByDate(String start, String end) {
        try {
            Date startDate = DateFormat.getInstance().parse(start);
            Date endDate = DateFormat.getInstance().parse(end);

            for (ErrorElement errorElement : errorElementSet) {
                Date date = DateFormat.getInstance().parse(errorElement.moreInfo);
                if (date.after(startDate) && date.before(endDate)) {

                } else {
                    errorElementSet.remove(errorElement);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
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
                    if (!e1.equals(e2)) {
                        newErrorElements.add(e2);
                    }
                }
            }
        }
        errorElementSet = newErrorElements;
    }*/

    public String getSortedExceptions(String from, String to) {
        if (from.equals("") && to.equals("")) {
            try {
                dateFrom = DateFormat.getInstance().parse("12/01/11 0:0 AM, PDT");
            } catch (ParseException e) {
                return "Incorrect date format";
            }
            dateTo = new Date();
        } else {
            try {
                dateFrom = DateFormat.getInstance().parse(from + " 0:0 AM, PDT");
                dateTo = DateFormat.getInstance().parse(to + " 12:0 PM, PDT");
            } catch (ParseException e) {
                return "Incorrect date format";
            }
        }
        analyzeLog();
        StringBuilder builder = new StringBuilder();
        for (String error : errorElementSet.keySet()) {
            ErrorElement el = errorElementSet.get(error);
            builder.append(ErrorWriter.getExceptionForLog("null", el.message, error, el.moreinfo));
        }
        /*for (ErrorElement el : errorElementSet) {
            builder.append(ErrorWriter.getExceptionForLog(el.type, el.message, el.stack, el.date + "\n" + el.moreInfo));
        }*/
        return builder.toString();
    }

    class UserInfo {
        public int numberOfRunRequest = 0;
        public int numberOfCompleteRequest = 0;
        public int numberOfHighlightRequest = 0;
        public int numberOfRequest = 0;
        public long startTime;
    }

    /*class ErrorElement {
        public String date;
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
                } else if (childNodes.item(j).getNodeName().equals("date")) {
                    date = childNodes.item(j).getTextContent();
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
    }*/

    class ErrorElement {
        //        public Set<String> moreInfo = new HashSet<String>();
        public String message = "";
        public String moreinfo = "";

        /*public Set<String> getMoreInfo() {
            return moreInfo;
        }*/

        public String getMessage() {
            return message;
        }

        public String getMoreinfo() {
            return moreinfo;
        }

        ErrorElement(String message, String moreinfo) {
            this.message = message;
            this.moreinfo = moreinfo;
        }
    }

}
