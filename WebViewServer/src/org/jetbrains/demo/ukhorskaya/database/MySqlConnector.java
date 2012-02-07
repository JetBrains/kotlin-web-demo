package org.jetbrains.demo.ukhorskaya.database;

import com.mysql.jdbc.jdbc2.optional.JDBC4ConnectionWrapper;
import org.apache.commons.lang.math.RandomUtils;
import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.ErrorWriterOnServer;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.jetbrains.demo.ukhorskaya.handlers.ServerHandler;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.demo.ukhorskaya.session.UserInfo;
import org.json.JSONArray;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 1/25/12
 * Time: 10:10 AM
 */

public class MySqlConnector {
    private Connection connection;

    private static final MySqlConnector connector = new MySqlConnector();

    private MySqlConnector() {
        if (!connect() || !createTablesIfNecessary()) {
            System.exit(1);
        }
    }

    private boolean connect() {
        String url = "";
        try {
            url = "jdbc:mysql://" + ServerSettings.MYSQL_HOST + ":" + ServerSettings.MYSQL_PORT + "/" + ServerSettings.MYSQL_DATABASE_NAME + "";
            connection = DriverManager.getConnection(url, ServerSettings.MYSQL_USERNAME, ServerSettings.MYSQL_PASSWORD);
            ErrorWriter.writeInfoToConsole("Connected to database: " + url);
            ErrorWriter.getInfoForLog("CONNECT_TO_DATABASE", "-1", "Connected to database: " + url);
            return true;
        } catch (Throwable e) {
            ErrorWriter.writeErrorToConsole("Cannot connect to database: " + url);
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), url);
            return false;
        }
    }

    private boolean checkConnection() {
        try {
            return connection.isValid(1000) || connect();
        } catch (SQLException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(),
                    "jdbc:mysql://" + ServerSettings.MYSQL_HOST + "/" + ServerSettings.MYSQL_DATABASE_NAME + "");
            return false;
        }
    }

    private boolean createTablesIfNecessary() {
        try {
            if (!checkConnection()) {
                return false;
            }
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SHOW TABLES");
            if (!rs.next()) {
                st = connection.createStatement();
                st.executeUpdate("CREATE TABLE " + ServerSettings.MYSQL_DATABASE_NAME + ".users (" +
                        "  USER_ID VARCHAR(45) NOT NULL DEFAULT ''," +
                        "  USER_TYPE VARCHAR(45) NOT NULL DEFAULT ''," +
                        "  USER_NAME VARCHAR(45) NOT NULL DEFAULT ''" +
                        ")" +
                        "ENGINE = InnoDB;");
                st = connection.createStatement();
                st.executeUpdate("CREATE TABLE " + ServerSettings.MYSQL_DATABASE_NAME + ".programs (" +
                        "  PROGRAM_ID VARCHAR(45) NOT NULL DEFAULT ''," +
                        "  PROGRAM_NAME VARCHAR(45) NOT NULL DEFAULT ''," +
                        "  PROGRAM_TEXT VARCHAR(1000) NOT NULL DEFAULT ''," +
                        "  PROGRAM_ARGS VARCHAR(45)," +
                        "  PROGRAM_LINK VARCHAR(150)," +
                        "  PRIMARY KEY(PROGRAM_ID)" +
                        ")" +
                        "ENGINE = InnoDB;");

                st = connection.createStatement();
                st.executeUpdate("CREATE TABLE " + ServerSettings.MYSQL_DATABASE_NAME + ".userprogramid (" +
                        "  USER_ID VARCHAR(45) NOT NULL DEFAULT ''," +
                        "  USER_TYPE VARCHAR(45) NOT NULL DEFAULT ''," +
                        "  PROGRAM_ID VARCHAR(45) NOT NULL DEFAULT ''" +
                        ")" +
                        "ENGINE = InnoDB;");
            }
            return true;
        } catch (Throwable e) {
            ErrorWriter.writeErrorToConsole("Cannot create tables in database: " + "jdbc:mysql://" + ServerSettings.MYSQL_HOST + "/" + ServerSettings.MYSQL_DATABASE_NAME + "");
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(),
                    "jdbc:mysql://" + ServerSettings.MYSQL_HOST + "/" + ServerSettings.MYSQL_DATABASE_NAME + "");
            return false;
        }
    }

    public static MySqlConnector getInstance() {
        return connector;
    }


    public boolean addNewUser(UserInfo userInfo) {
        if (!checkConnection()) {
            return false;
        }
        if (!findUser(userInfo)) {
            Statement st = null;
            try {
                st = connection.createStatement();
                st.executeUpdate("INSERT INTO users VALUES ('" + userInfo.getId() + "', '" + userInfo.getType() + "', '" + userInfo.getName() + "')");
                return true;
            } catch (Throwable e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(),
                        userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            } finally {
                closeStatement(st);
            }
        }
        return false;
    }

    private void closeStatement(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (SQLException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "null");
        }
    }

    public boolean findUser(UserInfo userInfo) {
        Statement st = null;
        ResultSet rs = null;
        try {
            String query = "SELECT * FROM users WHERE USER_ID='" + userInfo.getId() + "'";
            st = connection.createStatement();
            rs = st.executeQuery(query);
            while (rs.next()) {
                if (rs.getString("USER_TYPE").equals(userInfo.getType())) {
                    return true;
                }
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(),
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
        } finally {
            closeStatementAndResultSet(st, rs);
        }
        return false;

    }

    public String saveProgram(UserInfo userInfo, String programName, String programText, String args) {
        if (!checkConnection()) {
            return ResponseUtils.getJsonString("exception", "Cannot connect to database for save your program.");
        }
        Statement st = null;
        try {
            if (findUser(userInfo)) {

                if (!checkCountOfPrograms(userInfo)) {
                    return ResponseUtils.getJsonString("exception", "You can save only 100 programs");
                }
                if (findProgramByName(userInfo, programName)) {
                    return ResponseUtils.getJsonString("exception", "Program with same name already exists. Please choose the another one.");
                }


                st = connection.createStatement();
                String programId = userInfo.getId() + RandomUtils.nextInt();
                st.executeUpdate("INSERT INTO programs VALUES ('" + programId + "', '" + programName + "', '" + programText + "', '" + args + "', '')");
                st.executeUpdate("INSERT INTO userProgramId VALUES ('" + userInfo.getId() + "', '" + userInfo.getType() + "', '" + programId + "')");
                return ResponseUtils.getJsonString("programName", programName + "&id=" + programId);
            } else {
//                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), url);
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find user at userIdUserInfo table",
                        userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName()));
                return ResponseUtils.getJsonString("exception", "Please, login.");
            }
        } catch (Throwable e) {
            if (e.getMessage().contains("Data too long")) {
                return ResponseUtils.getJsonString("exception", "Data is too long. You can save only 1000 characters.");
            }
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(),
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            return ResponseUtils.getJsonString("exception", "Unknown error until saving your program");
        } finally {
            closeStatement(st);
        }
    }

    public boolean checkCountOfPrograms(UserInfo userInfo) {
        Statement st = null;
        ResultSet rs = null;
        try {
            String query = "SELECT count(*) FROM users WHERE USER_ID='" + userInfo.getId() + "'";
            st = connection.createStatement();
            rs = st.executeQuery(query);
            if (!rs.next()) {
                return false;
            }
            int count = Integer.parseInt(rs.getString("count(*)"));
            return count < 100;
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(),
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            return false;
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    public String generatePublicLink(String programId) {
        if (!checkConnection()) {
            return ResponseUtils.getJsonString("exception", "Cannot connect to database for generate public link.");
        }
        Statement st = null;
        ResultSet rs = null;
        try {
            String query = "SELECT * FROM programs WHERE PROGRAM_ID='" + programId + "'";
            st = connection.createStatement();
            rs = st.executeQuery(query);
            if (!rs.next()) {
                return ResponseUtils.getJsonString("exception", "Cannot find the program.");
            }

            String publicLink = rs.getString("PROGRAM_LINK");
            if (publicLink == null || publicLink.isEmpty()) {
                publicLink = "http://" + ServerSettings.AUTH_REDIRECT + "/?publicLink=" + programId;
                st.executeUpdate("UPDATE programs SET PROGRAM_LINK='" + publicLink + "' WHERE PROGRAM_ID='" + programId + "'");
            }

            return ResponseUtils.getJsonString("text", publicLink);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), programId);
            return ResponseUtils.getJsonString("exception", "Cannot generate public link");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    public String getProgramTextByPublicLink(String programId) {
        if (!checkConnection()) {
            return ResponseUtils.getJsonString("exception", "Cannot connect to database for load program by link.");
        }
        Statement st = null;
        ResultSet rs = null;
        try {
            String query = "SELECT * FROM programs WHERE PROGRAM_ID='" + programId + "'";
            st = connection.createStatement();
            rs = st.executeQuery(query);
            if (!rs.next()) {
//                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), url);
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find program by id in programIdProgramInfo",
                        programId));
                return ResponseUtils.getJsonString("exception", "Cannot find program by this link");
            }
            String link = rs.getString("PROGRAM_LINK");
            if (link == null || link.isEmpty()) {
                return ResponseUtils.getJsonString("exception", "Link for this program is not public");
            }

            JSONArray array = new JSONArray();
            Map<String, String> map = new HashMap<String, String>();
            map.put("type", "text");
            map.put("text", rs.getString("PROGRAM_TEXT"));
            String args = rs.getString("PROGRAM_ARGS");
            if (args == null) {
                args = "";
            }
            map.put("args", args);
            array.put(map);
            return array.toString();

        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), programId);
            return ResponseUtils.getJsonString("exception", "Unknown error until loading program by link");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    public String updateProgram(String programId, String programText, String args) {
        if (!checkConnection()) {
            return ResponseUtils.getJsonString("exception", "Cannot connect to database for save your program.");
        }
        Statement st = null;
        try {
            st = connection.createStatement();
            st.executeUpdate("UPDATE programs SET PROGRAM_TEXT='" + programText + "' WHERE PROGRAM_ID='" + programId + "'");
            st.executeUpdate("UPDATE programs SET PROGRAM_ARGS='" + args + "' WHERE PROGRAM_ID='" + programId + "'");
            return ResponseUtils.getJsonString("programId", programId);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), programId);
            return ResponseUtils.getJsonString("exception", "Unknown error until saving your program");
        } finally {
            closeStatement(st);
        }
    }

    public boolean findProgramByName(UserInfo userInfo, String programName) {
        Statement st = null;
        ResultSet rs = null;
        try {
            String query = "SELECT * FROM programs WHERE PROGRAM_NAME='" + programName + "'";
            st = connection.createStatement();
            rs = st.executeQuery(query);

            ArrayList<String> programIds = new ArrayList<String>();
            while (rs.next()) {
                programIds.add(rs.getString("PROGRAM_ID"));
            }


            for (String programId : programIds) {
                query = "SELECT * FROM userProgramId WHERE PROGRAM_ID='" + programId + "'";
                rs = st.executeQuery(query);
                if (!rs.next()) {
                    return false;
                }

                if (rs.getString("USER_ID").equals(userInfo.getId()) && rs.getString("USER_TYPE").equals(userInfo.getType())) {
                    return true;
                }
            }
            return false;
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(),
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            return false;
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    public String getProgramText(String programId) {
        if (!checkConnection()) {
            return ResponseUtils.getJsonString("exception", "Cannot connect to database for load your program.");
        }
        Statement st = null;
        ResultSet rs = null;
        try {
            String query = "SELECT * FROM programs WHERE PROGRAM_ID='" + programId + "'";
            st = connection.createStatement();
            rs = st.executeQuery(query);
            if (!rs.next()) {
                return ResponseUtils.getJsonString("exception", "Cannot find the program.");
            }
            JSONArray array = new JSONArray();
            Map<String, String> map = new HashMap<String, String>();
            map.put("type", "text");
            map.put("text", rs.getString("PROGRAM_TEXT"));
            String args = rs.getString("PROGRAM_ARGS");
            if (args == null) {
                args = "";
            }
            map.put("args", args);
            array.put(map);
            return array.toString();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), programId);
            return ResponseUtils.getJsonString("exception", "Unknown error until loading your program");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    public String getListOfProgramsForUser(UserInfo userInfo) {
        if (!checkConnection()) {
            return ResponseUtils.getJsonString("exception", "Cannot connect to database to load list of your programs.");
        }
        Statement st = null;
        ResultSet rs = null;
        try {
            String query = "SELECT * FROM userProgramId WHERE USER_ID='" + userInfo.getId() + "'";
            st = connection.createStatement();
            rs = st.executeQuery(query);
            JSONArray result = new JSONArray();
            ArrayList<String> programIds = new ArrayList<String>();
            while (rs.next()) {
                if (rs.getString("USER_TYPE").equals(userInfo.getType())) {
                    programIds.add(rs.getString("PROGRAM_ID"));
                }
            }

            String query2 = "SELECT * FROM programs WHERE PROGRAM_ID=";
            for (String id : programIds) {
                rs = st.executeQuery(query2 + "'" + id + "'");
                if (!rs.next()) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                            SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find program with in programs table",
                            userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + id));
                    continue;
                }
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", rs.getString("PROGRAM_ID"));
                map.put("name", rs.getString("PROGRAM_NAME"));
                result.put(map);
            }

            return result.toString();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(),
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            return ResponseUtils.getJsonString("exception", "Unknown error until loading list of your programs");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    private void closeStatementAndResultSet(Statement st, ResultSet rs) {
        try {
            if (st != null) {
                st.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "null");
        }
    }

    public String deleteProgram(UserInfo userInfo, String programId) {
        if (!checkConnection()) {
            return ResponseUtils.getJsonString("exception", "Cannot connect to database to delete your program.");
        }
        Statement st = null;
        try {
            if (findUser(userInfo)) {
                st = connection.createStatement();
                st.executeUpdate("DELETE FROM programs WHERE PROGRAM_ID='" + programId + "'");
                st.executeUpdate("DELETE FROM userProgramId WHERE USER_ID='" + userInfo.getId() + "' AND USER_TYPE='" + userInfo.getType() + "' AND PROGRAM_ID='" + programId + "'");
                return ResponseUtils.getJsonString("text", "Program was successfully deleted.", programId);
            } else {
//                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), url);
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find user at userIdUserInfo table",
                        userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName()));
                return ResponseUtils.getJsonString("exception", "Unknown error until deleting your program");
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + programId);
            return ResponseUtils.getJsonString("exception", "Unknown error until saving your program");
        } finally {
            closeStatement(st);
        }
    }


}
