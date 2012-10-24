/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.webdemo;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.*;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Statistics {
    private static int NUMBER_OF_USERS = 0;

    private final long MILLIS_PER_DAY = 86400000;
    private static final long MILLIS_PER_HOUR = 360000;
    private static final long MILLIS_PER_MINUTE = 60000;

    private final StatItem TOTAL_USERS = new StatItem("numberOfUsers", "$NUMBER_OF_USERS$");
    private final StatItem TOTAL_USERS_FROM_LOG = new StatItem("numberOfUsersFromLog", "$NUMBER_OF_USERS_FROM_LOG$");
    private final StatItem USERS_PER_DAY = new StatItem("numberOfUsersPerDay", "$NUMBER_OF_USERS_PER_DAY$");
    private final StatItem NEW_USERS_PER_DAY = new StatItem("numberOfNewUsersPerDay", "$NUMBER_OF_NEW_USERS_PER_DAY$");
    private final StatItem REQUEST_PER_USER = new StatItem("numberOfRequestPerUser", "$NUMBER_OF_REQUEST_PER_USER$");
    private final StatItem RUN_REQUEST_PER_USER = new StatItem("numberOfRunRequestPerUser", "$NUMBER_OF_RUN_REQUEST_PER_USER$");
    private final StatItem HIGHLIGHT_REQUEST_PER_USER = new StatItem("numberOfHighlightRequest", "$NUMBER_OF_HIGHLIGHT_REQUEST_PER_USER$");
    private final StatItem COMPLETE_REQUEST_PER_USER = new StatItem("numberOfCompleteRequestPerUser", "$NUMBER_OF_COMPLETE_REQUEST_PER_USER$");
    private final StatItem UPDATE_TIME = new StatItem("updateTime", "$UPDATETIME$");
    private final StatItem LOGS_PERIOD = new StatItem("logsPeriod", "$LOGSPERIOD$");

    //id - userInfo
    private Map<String, UserInfo> userInfoMapForId = new HashMap<String, UserInfo>();
    //stacktrace - description
    private Map<String, ErrorElement> errorElementSet = new HashMap<String, ErrorElement>();
    private List<Integer> usersPerDayList = new ArrayList<Integer>();
    //user id for day
    private List<Set<String>> uniqueUsersPerDay = new ArrayList<Set<String>>();

    private Date dateFrom;
    private Date dateTo;

    boolean isInDateRange = true;

    private static Statistics instance = new Statistics();

    private Statistics() {
        File file = new File(ApplicationSettings.STATISTICS_DIRECTORY + File.separator + "counter.txt");
        if (!file.exists()) {
            try {
                if (file.getAbsolutePath().contains("BuildAgent")) {
                    return;
                }
                file.createNewFile();
                FileWriter writer = new FileWriter(file);

                writer.write(URLDecoder.decode("0", "UTF-8"));
                writer.close();
            } catch (IOException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "STATISTICS", file.getAbsolutePath());
            }
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tmp = reader.readLine();
            NUMBER_OF_USERS = Integer.parseInt(tmp);
        } catch (FileNotFoundException e) {
            //Impossible
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(), file.getAbsolutePath());
        }

    }

    public static Statistics getInstance() {
        return instance;
    }

    public static void incNumberOfUsers() {
        NUMBER_OF_USERS++;
//        if (isNecessaryToUpdateCounter()) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                writeToCounterFile();
            }
        });
        t.start();
//        }
    }

    public static String getNumberOfUsers() {
        return String.valueOf(NUMBER_OF_USERS);
    }

    public boolean updateStatistics(boolean force) {
        if ((!force && isNecessaryToUpdateStatistics()) || force) {
            calculateStatistics();
            writeStatisticsInFile();
            return true;
        }
        return false;
    }

    public boolean isNecessaryToUpdateStatistics() {
        File file = new File(ApplicationSettings.STATISTICS_DIRECTORY + File.separator + "statistics.xml");
        return (System.currentTimeMillis() - file.lastModified()) > MILLIS_PER_HOUR;
    }

    private void calculateStatistics() {
        analyzeLogs("statistics");

        double totalNumberOfRequestsPerUser = 0;
        double totalNumberOfRunRequestsPerUser = 0;
        double totalNumberOfHighlightRequestsPerUser = 0;
        double totalNumberOfCompleteRequestsPerUser = 0;
        for (UserInfo userInfo : userInfoMapForId.values()) {
            totalNumberOfRequestsPerUser += userInfo.numberOfRequest;
            totalNumberOfRunRequestsPerUser += userInfo.numberOfRunRequest;
            totalNumberOfHighlightRequestsPerUser += userInfo.numberOfHighlightRequest;
            totalNumberOfCompleteRequestsPerUser += userInfo.numberOfCompleteRequest;
        }

        TOTAL_USERS.value = String.valueOf(NUMBER_OF_USERS);
        TOTAL_USERS_FROM_LOG.value = "for id: " + String.valueOf(userInfoMapForId.size());
        NEW_USERS_PER_DAY.value = getNumberOfNewUsersPerDay();
        USERS_PER_DAY.value = getNumberOfUsersPerDay();

        if (userInfoMapForId.isEmpty()) {
            final String ZERO = "0";
            REQUEST_PER_USER.value = ZERO;
            RUN_REQUEST_PER_USER.value = ZERO;
            HIGHLIGHT_REQUEST_PER_USER.value = ZERO;
            COMPLETE_REQUEST_PER_USER.value = ZERO;
        }
        else {
            REQUEST_PER_USER.value = String.valueOf(totalNumberOfRequestsPerUser / userInfoMapForId.size())
                    + " (" + totalNumberOfRequestsPerUser + " / " + userInfoMapForId.size() + ")";
            RUN_REQUEST_PER_USER.value = String.valueOf(totalNumberOfRunRequestsPerUser / userInfoMapForId.size())
                    + " (" + totalNumberOfRunRequestsPerUser + " / " + userInfoMapForId.size() + ")";
            HIGHLIGHT_REQUEST_PER_USER.value = String.valueOf(totalNumberOfHighlightRequestsPerUser / userInfoMapForId.size())
                    + " (" + totalNumberOfHighlightRequestsPerUser + " / " + userInfoMapForId.size() + ")";
            COMPLETE_REQUEST_PER_USER.value = String.valueOf(totalNumberOfCompleteRequestsPerUser / userInfoMapForId.size())
                    + " (" + totalNumberOfCompleteRequestsPerUser + " / " + userInfoMapForId.size() + ")";
        }
        UPDATE_TIME.value = getUpdateTimeForStatistics();
        LOGS_PERIOD.value = getLogsPeriod();
    }

    public String showMap() {
        calculateStatistics();
        StringBuilder builder = new StringBuilder();
        builder.append(generateTableContent("id"));

        return builder.toString();
    }

    private String generateTableContent(String type) {
        StringBuilder builder = new StringBuilder();

        TreeMap<String, UserInfo> sortedMap = null;
        if (type.equals("id")) {
            ValueComparator comparator = new ValueComparator(userInfoMapForId);
            sortedMap = new TreeMap<String, UserInfo>(comparator);
            sortedMap.putAll(userInfoMapForId);
        }

        if (sortedMap == null) {
            return "";
        }

        builder.append("<table border='2' bordercolor='#eee' cellspacing='0' cellpadding='5' style='float: left; margin-right: 20px;'>");
        builder.append("<tr>");
        builder.append(ResponseUtils.generateTag("td", "&#8470;"));
        builder.append(ResponseUtils.generateTag("td", "user " + type));
        builder.append(ResponseUtils.generateTag("td", "Total"));
        builder.append(ResponseUtils.generateTag("td", "Run"));
        builder.append(ResponseUtils.generateTag("td", "Highlight"));
        builder.append(ResponseUtils.generateTag("td", "Complete"));
        builder.append("</tr>");
        Set<Map.Entry<String, UserInfo>> set = sortedMap.entrySet();
        int number = 1;
        for (Map.Entry<String, UserInfo> stringUserInfoEntry : set) {
            builder.append("<tr>");
            UserInfo info = stringUserInfoEntry.getValue();
            builder.append(ResponseUtils.generateTag("td", String.valueOf(number)));
            builder.append(ResponseUtils.generateTag("td", stringUserInfoEntry.getKey()));
            builder.append(ResponseUtils.generateTag("td", String.valueOf(info.numberOfRequest)));
            builder.append(ResponseUtils.generateTag("td", String.valueOf(info.numberOfRunRequest)));
            builder.append(ResponseUtils.generateTag("td", String.valueOf(info.numberOfHighlightRequest)));
            builder.append(ResponseUtils.generateTag("td", String.valueOf(info.numberOfCompleteRequest)));
            builder.append("</tr>");
            number++;
        }
        builder.append("</table>");
        return builder.toString();
    }

    private String getLogsPeriod() {
        Calendar c = Calendar.getInstance();
        long curMillis = 0;
        try {
            curMillis = DateFormat.getInstance().parse(ResponseUtils.getDate(c) + " 23:59 AM, PDT").getTime();
        } catch (ParseException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(), ResponseUtils.getDate(c) + " 23:59 AM, PDT");
            curMillis = System.currentTimeMillis();
        }
        c.setTimeInMillis(curMillis - (MILLIS_PER_DAY * uniqueUsersPerDay.size() - MILLIS_PER_MINUTE));
        return ResponseUtils.getDate(c) + " - " + ResponseUtils.getDate(Calendar.getInstance());
    }

    private String getUpdateTimeForStatistics() {
        File file = new File(ApplicationSettings.STATISTICS_DIRECTORY + File.separator + "statistics.xml");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(file.lastModified());
        return ResponseUtils.getDate(c) + " "
                + ResponseUtils.getTime(c);
    }

    private String getNumberOfNewUsersPerDay() {
        if (usersPerDayList.isEmpty())
            return "0";

        int i = 0;
        for (Integer count : usersPerDayList) {
            i += count;
        }
        return String.valueOf(i / usersPerDayList.size()) + " for " + usersPerDayList.size() + " day(s)";
    }

    private String getNumberOfUsersPerDay() {
        if (uniqueUsersPerDay.isEmpty())
            return "0";

        int i = 0;
        for (Set<String> count : uniqueUsersPerDay) {
            i += count.size();
        }
        return String.valueOf(i / uniqueUsersPerDay.size()) + " for " + uniqueUsersPerDay.size() + " day(s)";
    }

    private void writeStatisticsInFile() {
        File file = new File(ApplicationSettings.STATISTICS_DIRECTORY + File.separator + "statistics.xml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), file.getAbsolutePath());
            }
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write("<?xml version=\"1.0\"?>");
            writer.newLine();
            writer.write("<statistics>");
            writer.newLine();
            writer.write(ResponseUtils.generateTag(UPDATE_TIME.name, String.valueOf(System.currentTimeMillis())));
            writer.newLine();
            writer.write(ResponseUtils.generateTag(TOTAL_USERS.name, TOTAL_USERS.value));
            writer.newLine();
            writer.write(ResponseUtils.generateTag(TOTAL_USERS_FROM_LOG.name, TOTAL_USERS_FROM_LOG.value));
            writer.newLine();
            writer.write(ResponseUtils.generateTag(USERS_PER_DAY.name, USERS_PER_DAY.value));
            writer.newLine();
            writer.write(ResponseUtils.generateTag(NEW_USERS_PER_DAY.name, NEW_USERS_PER_DAY.value));
            writer.newLine();
            writer.write(ResponseUtils.generateTag(REQUEST_PER_USER.name, REQUEST_PER_USER.value));
            writer.newLine();
            writer.write(ResponseUtils.generateTag(RUN_REQUEST_PER_USER.name, RUN_REQUEST_PER_USER.value));
            writer.newLine();
            writer.write(ResponseUtils.generateTag(HIGHLIGHT_REQUEST_PER_USER.name, HIGHLIGHT_REQUEST_PER_USER.value));
            writer.newLine();
            writer.write(ResponseUtils.generateTag(COMPLETE_REQUEST_PER_USER.name, COMPLETE_REQUEST_PER_USER.value));
            writer.newLine();
            writer.write("</statistics>");
            writer.close();
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), file.getAbsolutePath());
        }
    }

    private void readStatisticsFromFile() {
        File file = new File(ApplicationSettings.STATISTICS_DIRECTORY + File.separator + "statistics.xml");
        if (!file.exists()) {
            writeStatisticsInFile();
        }

        Document document = ResponseUtils.getXmlDocument(file);
        if (document == null) {
            return;
        }
        NodeList nodeList = document.getElementsByTagName("statistics");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node item = nodeList.item(i);
                if (item.getNodeName().equals(TOTAL_USERS.name)) {
                    TOTAL_USERS.value = item.getTextContent();
                } else if (item.getNodeName().equals(TOTAL_USERS_FROM_LOG.name)) {
                    TOTAL_USERS_FROM_LOG.value = item.getTextContent();
                } else if (item.getNodeName().equals(USERS_PER_DAY.name)) {
                    USERS_PER_DAY.value = item.getTextContent();
                } else if (item.getNodeName().equals(NEW_USERS_PER_DAY.name)) {
                    NEW_USERS_PER_DAY.value = item.getTextContent();
                } else if (item.getNodeName().equals(REQUEST_PER_USER.name)) {
                    REQUEST_PER_USER.value = item.getTextContent();
                } else if (item.getNodeName().equals(RUN_REQUEST_PER_USER.name)) {
                    RUN_REQUEST_PER_USER.value = item.getTextContent();
                } else if (item.getNodeName().equals(HIGHLIGHT_REQUEST_PER_USER.name)) {
                    HIGHLIGHT_REQUEST_PER_USER.value = item.getTextContent();
                } else if (item.getNodeName().equals(COMPLETE_REQUEST_PER_USER.name)) {
                    COMPLETE_REQUEST_PER_USER.value = item.getTextContent();
                }
            }
            UPDATE_TIME.value = getUpdateTimeForStatistics();
            LOGS_PERIOD.value = getLogsPeriod();
        }
    }

    private static void writeToCounterFile() {
        File file = new File(ApplicationSettings.STATISTICS_DIRECTORY + File.separator + "counter.txt");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(URLDecoder.decode(String.valueOf(NUMBER_OF_USERS), "UTF-8"));
            writer.close();
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), file.getAbsolutePath());
        }
    }

    public String writeStatistics(String response) {
        if (!updateStatistics(false)) {
            readStatisticsFromFile();
        }

        analyzeLogs("statistics");
        StringBuilder buffer = new StringBuilder(response);

        int pos = buffer.indexOf(TOTAL_USERS.htmlPatern);
        buffer.replace(pos, pos + TOTAL_USERS.htmlPatern.length(), TOTAL_USERS.value);

        pos = buffer.indexOf(TOTAL_USERS_FROM_LOG.htmlPatern);
        buffer.replace(pos, pos + TOTAL_USERS_FROM_LOG.htmlPatern.length(), TOTAL_USERS_FROM_LOG.value);

        pos = buffer.indexOf(NEW_USERS_PER_DAY.htmlPatern);
        buffer.replace(pos, pos + NEW_USERS_PER_DAY.htmlPatern.length(), NEW_USERS_PER_DAY.value);

        pos = buffer.indexOf(USERS_PER_DAY.htmlPatern);
        buffer.replace(pos, pos + USERS_PER_DAY.htmlPatern.length(), USERS_PER_DAY.value);

        pos = buffer.indexOf(REQUEST_PER_USER.htmlPatern);
        buffer.replace(pos, pos + REQUEST_PER_USER.htmlPatern.length(), REQUEST_PER_USER.value);

        pos = buffer.indexOf(RUN_REQUEST_PER_USER.htmlPatern);
        buffer.replace(pos, pos + RUN_REQUEST_PER_USER.htmlPatern.length(), RUN_REQUEST_PER_USER.value);

        pos = buffer.indexOf(HIGHLIGHT_REQUEST_PER_USER.htmlPatern);
        buffer.replace(pos, pos + HIGHLIGHT_REQUEST_PER_USER.htmlPatern.length(), HIGHLIGHT_REQUEST_PER_USER.value);

        pos = buffer.indexOf(COMPLETE_REQUEST_PER_USER.htmlPatern);
        buffer.replace(pos, pos + COMPLETE_REQUEST_PER_USER.htmlPatern.length(), COMPLETE_REQUEST_PER_USER.value);

        pos = buffer.indexOf(UPDATE_TIME.htmlPatern);
        buffer.replace(pos, pos + UPDATE_TIME.htmlPatern.length(), UPDATE_TIME.value);

        pos = buffer.indexOf(LOGS_PERIOD.htmlPatern);
        buffer.replace(pos, pos + LOGS_PERIOD.htmlPatern.length(), LOGS_PERIOD.value);

        return buffer.toString();
    }

    private void analyzeLogs(String param) {
        userInfoMapForId = new HashMap<String, UserInfo>();
//        userInfoMapForIp = new HashMap<String, UserInfo>();
//        ipToIdMap = new HashMap<String, Set<Integer>>();
        errorElementSet = new HashMap<String, ErrorElement>();
        usersPerDayList = new ArrayList<Integer>();
        uniqueUsersPerDay = new ArrayList<Set<String>>();
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

                if ((param.equals("all") || param.equals("statistics")) && file.getName().contains("kotlincompiler.log")) {
                    analyzeInfoLog(file);
                } else if ((param.equals("all") || param.equals("exceptions")) && dateFrom != null && dateTo != null && file.getName().contains("exceptions.log")) {
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
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), file.getAbsolutePath());
            //impossible
            return;
        }
        String tmp;
        try {
            @Nullable Date lastDate;
            @Nullable Date curDate = null;
            int numberOfUsersTmp = 0;
            Set<String> set = new HashSet<String>();
            while ((tmp = reader.readLine()) != null) {
                if (tmp.contains("Number_of_users_since_start_server") || tmp.contains("type=INC_NUMBER_OF_REQUESTS")) {
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
                                        uniqueUsersPerDay.add(set);
                                        set = new HashSet<String>();
                                        numberOfUsersTmp = 1;
                                    } else {
                                        numberOfUsersTmp++;
                                    }
                                } else {
                                    curDate = DateFormat.getInstance().parse(tmp.substring(0, 8) + " 0:0 AM, PDT");
                                    numberOfUsersTmp++;
                                }

                            } catch (ParseException e) {
                                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(), tmp);
                            }
                        }
                        if (token.equals("type=INC_NUMBER_OF_REQUESTS")) {
                            token = tokenizer.nextToken();
                            if (token.contains("userId=")) {
                                String id = ResponseUtils.substringAfter(token, "userId=");
                                set.add(id);
                                UserInfo info = userInfoMapForId.get(id);
                                if (info == null) {
                                    info = new UserInfo();
                                }
                                info.numberOfRequest++;
                                token = tokenizer.nextToken();
                                if (!token.startsWith("message=")) {
                                    token = tokenizer.nextToken();
                                }
                                if (token.equals("message=" + SessionInfo.TypeOfRequest.RUN.name())) {
                                    info.numberOfRunRequest++;
                                } else if (token.equals("message=" + SessionInfo.TypeOfRequest.HIGHLIGHT.name())) {
                                    info.numberOfHighlightRequest++;
                                } else if (token.equals("message=" + SessionInfo.TypeOfRequest.COMPLETE.name())) {
                                    info.numberOfCompleteRequest++;
                                }

                                userInfoMapForId.put(id, info);
                            }
                        }
                    }
                }
            }
            usersPerDayList.add(numberOfUsersTmp);
            uniqueUsersPerDay.add(set);
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(), file.getAbsolutePath());
        }
    }

    public void sendErrorsToErrorsAnalyzer() {
        File dir = new File("C:\\Development\\contrib\\logsForSend");
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

            try {
                dateFrom = DateFormat.getInstance().parse("12/01/2011 0:0 AM, PDT");
            } catch (ParseException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), "12/01/2011 0:0 AM, PDT");
            }
            dateTo = new Date();

            for (File file : files) {
                if (file.getName().contains("exceptions.log")) {
                    try {
                        analyzeExceptionLog(file);
                    } catch (Throwable e) {
                        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), file.getAbsolutePath());
                    }
                }
            }

            int i = 0;
            for (String error : errorElementSet.keySet()) {
                ErrorElement el = errorElementSet.get(error);
                if (i < 10) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(el.message, error, el.type, el.moreinfo);
                }
            }
        }
    }

    private void analyzeExceptionLog(File file) {
        File ex = generateValidXmlFileForExceptions(file);
        if (ex == null) {
            return;
        }
        Document document = ResponseUtils.getXmlDocument(ex);
        if (document == null) {
            return;
        }
        NodeList nodeList = document.getElementsByTagName("error");
        for (int i = 0; i < nodeList.getLength(); ++i) {
            NodeList childNodes = nodeList.item(i).getChildNodes();
            String more = "";
            String type = "";
            String date = "";
            String message = "";
            String stack = "";
            for (int j = 0; j < childNodes.getLength(); j++) {
                if (childNodes.item(j).getNodeName().equals("type")) {
                    type = childNodes.item(j).getTextContent();
                } else if (childNodes.item(j).getNodeName().equals("message")) {
                    message = childNodes.item(j).getTextContent();
                } else if (childNodes.item(j).getNodeName().equals("date")) {
                    date = childNodes.item(j).getTextContent() + "\n";
                } else if (childNodes.item(j).getNodeName().equals("version")) {
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
                errorElement = new ErrorElement(message, more, date, type);
            } else {
                if (!errorElement.moreinfo.contains(more)) {
                    if (more.contains(errorElement.moreinfo)) {
                        errorElement.moreinfo = more;
                    } else {
                        errorElement.moreinfo += "\n" + more;
                    }
                }
            }
            errorElementSet.put(stack, errorElement);
        }
        ex.delete();
    }

    @Nullable
    private File generateValidXmlFileForExceptions(File file) {
        File ex;
        try {
            ex = new File(ApplicationSettings.OUTPUT_DIRECTORY + File.separator + "tmp.log" + new Random().nextInt());
            ex.createNewFile();
            if (!ex.exists()) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(new FileNotFoundException("Cannot create tmp file"),
                        "STATISTICS", file.getAbsolutePath()
                );
                return null;
            }
            FileWriter writer = new FileWriter(ex);
            writer.write("<?xml version=\"1.0\"?>");
            writer.write("<errors>");
            Pattern logStr = Pattern.compile("[0-9]*\\/[0-9]*\\/[0-9]* [0-9]*:[0-9]*:[0-9]* [(][\\w]*.java [0-9]*[)] [\\[][\\w]*[\\]]");

            Matcher matcher;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tmp;
            while ((tmp = reader.readLine()) != null) {
                matcher = logStr.matcher(tmp);
                if (matcher.find()) {
                    if (tmp.length() > 16) {
                        int inRange = compareDate(tmp.substring(0, 16) + " AM, PDT");
                        switch (inRange) {
                            case -2: {
                                return null;
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
                            writer.write("<date>" + tmp.substring(0, 16) + " AM, PDT" + "</date>");
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
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), file.getAbsolutePath()
            );
            return null;
        }
        return ex;
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
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), dateStr);
        }
        return -2;
    }

    public String getSortedExceptions(String from, String to) {
        try {
            if (from.equals("")) {
                dateFrom = DateFormat.getInstance().parse("12/01/2011 0:0 AM, PDT");
            }
            if (to.equals("")) {
                dateTo = new Date();
            }
            if (!from.equals("") && !to.equals("")) {
                if (from.equals("today")) {
                    dateFrom = DateFormat.getInstance().parse(
                            ResponseUtils.getDate(Calendar.getInstance()) + " 0:0 AM, PDT");
                    dateTo = new Date();
                } else if (from.equals("week")) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(c.getTimeInMillis() - (MILLIS_PER_DAY * 7));
                    dateFrom = DateFormat.getInstance().parse(ResponseUtils.getDate(c) + " 0:0 AM, PDT");
                    dateTo = new Date();
                } else {
                    dateFrom = DateFormat.getInstance().parse(from + " 0:0 AM, PDT");
                    dateTo = DateFormat.getInstance().parse(to + " 12:0 PM, PDT");
                }
            }
        } catch (ParseException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), "Incorrect date format: " + from + " " + to);
            return "Incorrect date format: " + from + " " + to;
        }
        analyzeLogs("exceptions");
        StringBuilder builder = new StringBuilder();
        for (String error : errorElementSet.keySet()) {
            ErrorElement el = errorElementSet.get(error);
            builder.append(ErrorWriter.getExceptionForLog("ERROR", el.message, error, el.date + el.moreinfo));
        }
        return builder.toString();
    }

    class UserInfo {
        public int numberOfRunRequest = 0;
        public int numberOfCompleteRequest = 0;
        public int numberOfHighlightRequest = 0;
        public int numberOfRequest = 0;
    }

    class ErrorElement {
        public final String message;
        public final String date;
        public final String type;
        public String moreinfo;

        ErrorElement(String message, String moreinfo, String date, String type) {
            this.message = message;
            this.moreinfo = moreinfo;
            this.date = date;
            this.type = type;
        }
    }

    class StatItem {
        public final String name;
        public final String htmlPatern;
        public String value;

        StatItem(String name, String htmlPatern) {
            this.htmlPatern = htmlPatern;
            this.name = name;
            this.value = "";
        }
    }

    class ValueComparator implements Comparator {
        Map<String, UserInfo> base;

        public ValueComparator(Map<String, UserInfo> base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {
            if (a instanceof String && b instanceof String) {
                UserInfo a1 = base.get((String) a);
                UserInfo b1 = base.get((String) b);
                if (a1.numberOfRequest < b1.numberOfRequest) {
                    return 1;
                } else if (a1.numberOfRequest == b1.numberOfRequest) {
                    if (a1.numberOfRunRequest < b1.numberOfRunRequest) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (a1.numberOfRequest > b1.numberOfRequest) {
                    return -1;
                }
            }
            return 0;
        }
    }

}
