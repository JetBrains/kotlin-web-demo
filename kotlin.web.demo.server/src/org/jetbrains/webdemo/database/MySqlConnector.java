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

package org.jetbrains.webdemo.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.naming.NamingContext;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ErrorWriterOnServer;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.examplesLoader.ExampleFile;
import org.jetbrains.webdemo.examplesLoader.ExampleObject;
import org.jetbrains.webdemo.examplesLoader.ExamplesFolder;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MySqlConnector {
    private static final MySqlConnector connector = new MySqlConnector();
    private Connection connection;
    private String databaseUrl;

    private MySqlConnector() {
        if (!connect() || !createTablesIfNecessary()) {
            System.exit(1);
        }
    }

    public static MySqlConnector getInstance() {
        return connector;
    }

    private boolean connect() {
        try {
            InitialContext initCtx = new InitialContext();
            NamingContext envCtx = (NamingContext) initCtx.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/kotlin");
            connection = ds.getConnection();
            databaseUrl = connection.toString();
            ErrorWriter.writeInfoToConsole("Connected to database: " + databaseUrl);
            ErrorWriter.getInfoForLog("CONNECT_TO_DATABASE", "-1", "Connected to database: " + databaseUrl);
            checkDatabaseVersion();
            return true;
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", databaseUrl);
            return false;
        }
    }

    private boolean checkConnection() {
        try {
            return connection.isValid(1000) || connect();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    databaseUrl);
            return false;
        }
    }

    private boolean createTablesIfNecessary() {
        try {
            if (!checkConnection()) {
                return false;
            }
            PreparedStatement st = connection.prepareStatement("SHOW TABLES");
            ResultSet rs = st.executeQuery();
            if (!rs.next()) {
//                st = connection.prepareStatement("CREATE TABLE databaseinfo (" +
//                        "  VERSION VARCHAR(45)" +
//                        ")" +
//                        "ENGINE = InnoDB;");
//                st.execute();
//                st = connection.prepareStatement("CREATE TABLE users (" +
//                        "  USER_ID VARCHAR(45) NOT NULL DEFAULT ''," +
//                        "  USER_TYPE VARCHAR(45) NOT NULL DEFAULT ''," +
//                        "  USER_NAME VARCHAR(45) NOT NULL DEFAULT ''" +
//                        ")" +
//                        "ENGINE = InnoDB;");
//                st.execute();
//                st = connection.prepareStatement("CREATE TABLE programs (" +
//                        "  PROGRAM_ID VARCHAR(45) NOT NULL DEFAULT ''," +
//                        "  PROGRAM_NAME VARCHAR(45) NOT NULL DEFAULT ''," +
//                        "  PROGRAM_TEXT LONGTEXT," +
//                        "  PROGRAM_ARGS VARCHAR(45)," +
//                        "  PROGRAM_LINK VARCHAR(150)," +
//                        "  RUN_CONF VARCHAR(45) NOT NULL DEFAULT ''," +
//                        "  PRIMARY KEY(PROGRAM_ID)" +
//                        ")" +
//                        "ENGINE = InnoDB;");
//                st.execute();
//
//                st = connection.prepareStatement("CREATE TABLE userprogramid (" +
//                        "  USER_ID VARCHAR(45) NOT NULL DEFAULT ''," +
//                        "  USER_TYPE VARCHAR(45) NOT NULL DEFAULT ''," +
//                        "  PROGRAM_ID VARCHAR(45) NOT NULL DEFAULT ''" +
//                        ")" +
//                        "ENGINE = InnoDB;");
//                st.execute();
//                st = connection.prepareStatement("INSERT INTO databaseinfo (VERSION) VALUES (?)");
//                st.setString(1, ApplicationSettings.DATABASE_VERSION);
//                st.executeUpdate();
            }
            closeStatementAndResultSet(st, rs);
            return true;
        } catch (Throwable e) {
            ErrorWriter.writeErrorToConsole("Cannot create tables in database: " + databaseUrl);
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    databaseUrl);
            e.printStackTrace();
            return false;
        }
    }

    private boolean compareVersion() {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement("SHOW TABLES");
            rs = st.executeQuery();
            if (!checkIfDatabaseInfoExists(rs)) {
                st = connection.prepareStatement("CREATE TABLE dbinfo (" +
                        "  version VARCHAR(45)" +
                        ") " +
                        "ENGINE = InnoDB;");
                st.execute();
                System.out.println("Create table databaseInfo");
                st = connection.prepareStatement("INSERT dbinfo (VERSION) SET VERSION=?");
                st.setString(1, ApplicationSettings.DATABASE_VERSION);
                st.executeUpdate();
                System.out.println("add database version");
                return false;
            }
            if (rs.next()) {
                st = connection.prepareStatement("SELECT * FROM dbinfo");
                rs = st.executeQuery();
                if (rs.next()) {
                    String version = rs.getString("VERSION");
                    return version.equals(ApplicationSettings.DATABASE_VERSION);
                }
            }
        } catch (SQLException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    "Cannot read database version");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
        return false;
    }

    private boolean checkIfDatabaseInfoExists(ResultSet rs) {
        try {
            while (rs.next()) {
                if (rs.getString(1).equals("dbinfo")) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    "Cannot read database version");
            return false;
        }
    }

    private void checkDatabaseVersion() {
        if (!compareVersion()) {
            try (PreparedStatement st = connection.prepareStatement("UPDATE dbinfo SET VERSION=?")) {
                st.setString(1, ApplicationSettings.DATABASE_VERSION);
                st.executeUpdate();

                //st = connection.prepareStatement("ALTER TABLE programs ADD COLUMN RUN_CONF VARCHAR(45) NOT NULL DEFAULT '' AFTER PROGRAM_LINK");
                //st.execute();
            } catch (SQLException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                        "Cannot update database");
            }
        }
    }

    public boolean addNewUser(UserInfo userInfo) {
        if (!checkConnection()) {
            return false;
        }
        if (!findUser(userInfo)) {
            try (PreparedStatement st = connection.prepareStatement("INSERT INTO users (client_id, provider, username) VALUES (?, ?, ?)")) {
                st.setString(1, userInfo.getId());
                st.setString(2, userInfo.getType());
                st.setString(3, userInfo.getName());
                st.executeUpdate();
                return true;
            } catch (Throwable e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                        userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            }
        }
        return false;
    }

    private void closeStatement(PreparedStatement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (SQLException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "null");
        }
    }

    public boolean findUser(UserInfo userInfo) {
        return (getUserId(userInfo) != -1);
    }

    public boolean saveFile(UserInfo userInfo, String folderName, String projectName, ExampleFile file){
        int projectId = getProjectId(userInfo, folderName, projectName);
        if(projectId != -1) {
            try (PreparedStatement st = connection.prepareStatement("UPDATE files SET files.content = ? WHERE project_id = ? AND files.name = ?  ")) {
                st.setString(1, file.content);
                st.setString(2, projectId + "");
                st.setString(3, file.name);
                st.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private String escape(String str) {
        return str.replaceAll("'", "\'");
    }

    public boolean checkCountOfPrograms(UserInfo userInfo) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement("SELECT count(*) FROM users WHERE USER_ID=?");
            st.setString(1, userInfo.getId());
            rs = st.executeQuery();
            if (!rs.next()) {
                return false;
            }
            int count = Integer.parseInt(rs.getString("count(*)"));
            return count < 100;
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            return false;
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    public String generatePublicLink(String programId) {
        if (!checkConnection()) {
            return ResponseUtils.getErrorInJson("Cannot connect to database for generate public link.");
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement("SELECT * FROM programs WHERE PROGRAM_ID=?");
            st.setString(1, programId);
            rs = st.executeQuery();
            if (!rs.next()) {
                return ResponseUtils.getErrorInJson("Cannot find the program.");
            }

            String publicLink = rs.getString("PROGRAM_LINK");
            if (publicLink == null || publicLink.isEmpty()) {
                publicLink = "http://" + ApplicationSettings.AUTH_REDIRECT + "/?publicLink=" + programId;
                st = connection.prepareStatement("UPDATE programs  SET PROGRAM_LINK=? WHERE PROGRAM_ID=?");
                st.setString(1, publicLink);
                st.setString(2, programId);
                st.executeUpdate();
            }

            return ResponseUtils.getJsonString("text", publicLink);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", programId);
            return ResponseUtils.getErrorInJson("Cannot generate public link");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    public String getProgramTextByPublicLink(String programId) {
        if (!checkConnection()) {
            return ResponseUtils.getErrorInJson("Cannot connect to database for load program by link.");
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement("SELECT * FROM programs WHERE PROGRAM_ID=?");
            st.setString(1, programId);
            rs = st.executeQuery();
            if (!rs.next()) {
//                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), url);
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find program by id in programIdProgramInfo", "unknown",
                        programId));
                return ResponseUtils.getErrorInJson("Cannot find program by this link");
            }
            String link = rs.getString("PROGRAM_LINK");
            if (link == null || link.isEmpty()) {
                return ResponseUtils.getErrorInJson("Link for this program is not public");
            }

            ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
            ObjectNode jsonObject = array.addObject();
            jsonObject.put("type", "text");
            jsonObject.put("text", rs.getString("PROGRAM_TEXT"));
            String args = rs.getString("PROGRAM_ARGS");
            if (args == null) {
                args = "";
            }
            jsonObject.put("args", args);
            jsonObject.put("confType", rs.getString("RUN_CONF"));
            return array.toString();

        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", programId);
            return ResponseUtils.getErrorInJson("Unknown error while loading program by link");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    public String updateProgram(String programId, String programText, String args, String runConfiguration) {
        if (!checkConnection()) {
            return ResponseUtils.getErrorInJson("Cannot connect to database for save your program.");
        }
        PreparedStatement st = null;
        try {
            st = connection.prepareStatement("UPDATE programs SET PROGRAM_TEXT=? WHERE PROGRAM_ID=?");
            st.setString(1, programText);
            st.setString(2, programId);
            st.executeUpdate();

            st = connection.prepareStatement("UPDATE programs SET PROGRAM_ARGS=? WHERE PROGRAM_ID=?");
            st.setString(1, args);
            st.setString(2, programId);
            st.executeUpdate();

            st = connection.prepareStatement("UPDATE programs SET RUN_CONF=? WHERE PROGRAM_ID=?");
            st.setString(1, runConfiguration);
            st.setString(2, programId);
            st.executeUpdate();

            return ResponseUtils.getJsonString("programId", programId);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", programId);
            return ResponseUtils.getErrorInJson("Unknown error while saving your program");
        } finally {
            closeStatement(st);
        }
    }

    public boolean findProgramByName(UserInfo userInfo, String programName) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement("SELECT * FROM programs WHERE PROGRAM_NAME=?");
            st.setString(1, programName);
            rs = st.executeQuery();

            ArrayList<String> programIds = new ArrayList<String>();
            while (rs.next()) {
                programIds.add(rs.getString("PROGRAM_ID"));
            }


            for (String programId : programIds) {
                st = connection.prepareStatement("SELECT * FROM userprogramid WHERE PROGRAM_ID=?");
                st.setString(1, programId);
                rs = st.executeQuery();
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
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            return false;
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    public boolean addProject(UserInfo userInfo, String name) {
        int userId = getUserId(userInfo);
        if (userId != -1) {
            try (PreparedStatement st = connection.prepareStatement("insert into projects (owner_id, name) values (?,?) ")) {
                st.setString(1, userId + "");
                st.setString(2, name);
                st.execute();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean addProject(UserInfo userInfo, ExampleObject project) {
        int userId = getUserId(userInfo);
        if (userId != -1) {
            PreparedStatement st = null;
            try {
                st = connection.prepareStatement("insert into projects (owner_id, name, parent, args, run_configuration) values (?,?,?,?,?) ");
                st.setString(1, userId + "");
                st.setString(2, project.name);
                st.setString(3, project.parent);
                st.setString(4, project.args);
                st.setString(5, project.confType);
                st.execute();

                int projectId = getProjectId(userInfo, project.parent, project.name);
                for (ExampleFile file : project.files) {
                    st = connection.prepareStatement("insert into files (project_id, name, content) values (?,?,?)");
                    st.setString(1, projectId + "");
                    st.setString(2, file.name);
                    st.setString(3, file.content);
                    st.execute();
                    st.execute();
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeStatement(st);
            }
        }
        return false;
    }

    public boolean addFile(UserInfo userInfo, String folderName, String projectName, String fileName) {
        return addFile(userInfo, folderName, projectName, fileName, "");
    }

    public boolean addFile(UserInfo userInfo, String folderName, String projectName, String fileName, String content) {
        int projectId = getProjectId(userInfo, folderName, projectName);
        if (projectId != -1) {
            try (PreparedStatement st = connection.prepareStatement("insert into files (project_id, name, content) values (?,?,?) ")) {
                st.setString(1, projectId + "");
                st.setString(2, fileName);
                st.setString(3, content);
                st.execute();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String getProjectNames(UserInfo userInfo) {
        if (!checkConnection()) {
            return ResponseUtils.getErrorInJson("Cannot connect to database to load list of your programs.");
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement(
                    "select projects.id, projects.name from projects join " +
                            "users on projects.owner_id = users.id where " +
                            "(users.client_id = ? and users.provider = ?)"
            );
            st.setString(1, userInfo.getId());
            st.setString(2, userInfo.getType());

            rs = st.executeQuery();

            List<String> projects = new ArrayList<>();
            while (rs.next()) {
                projects.add(rs.getString("name"));
            }

            return new ObjectMapper().writeValueAsString(projects);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            return ResponseUtils.getErrorInJson("Unknown error while loading list of your programs");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }


    public String getProjectContent(UserInfo userInfo, String parent, String projectName) {
        if (!checkConnection()) {
            return ResponseUtils.getErrorInJson("Cannot connect to database for load your program.");
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement(
                    "select projects.id,projects.args, projects.run_configuration from projects join " +
                            "users on projects.owner_id = users.id where " +
                            "(users.client_id = ? and users.provider = ? and projects.parent = ? and projects.name = ?)");
            st.setString(1, userInfo.getId());
            st.setString(2, userInfo.getType());
            st.setString(3, parent);
            st.setString(4, projectName);
            rs = st.executeQuery();

            if (rs.next()) {
                ExampleObject project = new ExampleObject();
                project.parent = "My Programs";
                project.name = projectName;
                project.args = rs.getString("args");
                project.confType = rs.getString("run_configuration");
                project.files = new ArrayList<>();
                project.isLocalVersion = true;

                st = connection.prepareStatement("select * from files where project_id = ?");
                st.setString(1, rs.getInt("id") + "");
                rs = st.executeQuery();
                while (rs.next()) {
                    ExampleFile file = new ExampleFile(rs.getString("name"), rs.getString("content"));
                    project.files.add(file);
                }
                return new ObjectMapper().writeValueAsString(project);
            } else if (!parent.equals("My Programs")) {
                ExampleObject project = ExamplesList.getExampleObject(projectName, parent);
                return new ObjectMapper().writeValueAsString(project);
            } else {
                return ResponseUtils.getErrorInJson("Can't load your project");
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", projectName);
            return ResponseUtils.getErrorInJson("Unknown error while loading your project");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    private void closeStatementAndResultSet(PreparedStatement st, ResultSet rs) {
        try {
            if (st != null) {
                st.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "null");
        }
    }

    public String deleteFile(UserInfo userInfo, String parentName, String projectName, String fileName) {
        if (!checkConnection()) {
            return ResponseUtils.getErrorInJson("Cannot connect to database to delete your program.");
        }
        int projectId = getProjectId(userInfo, parentName, projectName);
        if (projectId != -1) {
            try (PreparedStatement st = connection.prepareStatement("delete from files where files.project_id =? and files.name = ?")) {
                st.setString(1, projectId + "");
                st.setString(2, fileName);
                st.executeUpdate();
                return ResponseUtils.getJsonString("text", "Program was successfully deleted.", fileName);
            } catch (Throwable e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + fileName);
                return ResponseUtils.getErrorInJson("Unknown error while deleting your program");
            }
        } else {
            return ResponseUtils.getErrorInJson("Can't found project");
        }
    }

    public String deleteProject(UserInfo userInfo, String projectName) {
        if (!checkConnection()) {
            return ResponseUtils.getErrorInJson("Cannot connect to database to delete your program.");
        }
        int userId = getUserId(userInfo);
        if (userId != -1) {
            try (PreparedStatement st = connection.prepareStatement("delete from projects where projects.owner_id = ? and projects.name = ?")) {
                st.setString(1, userId + "");
                st.setString(2, projectName);
                st.executeUpdate();
                return ResponseUtils.getJsonString("text", "Project was successfully deleted.", projectName);
            } catch (Throwable e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectName);
                return ResponseUtils.getErrorInJson("Unknown error while deleting your program");
            }
        } else {
            return ResponseUtils.getErrorInJson("Can't found project");
        }
    }


    private int getUserId(UserInfo userInfo) {
        try (PreparedStatement st = connection.prepareStatement("select users.id from users where (users.client_id = ? and users.provider=?)")) {
            st.setString(1, userInfo.getId());
            st.setString(2, userInfo.getType());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    return -1;
                }
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            return -1;
        }
    }

    private int getProjectId(UserInfo userInfo, String parentName, String projectName) {
        try (PreparedStatement st = connection.prepareStatement(
                "select projects.id from projects join " +
                        "users on projects.owner_id =users.id where " +
                        "( users.client_id = ? and  users.provider = ? and projects.parent = ? and projects.name = ?)")) {
            st.setString(1, userInfo.getId());
            st.setString(2, userInfo.getType());
            st.setString(3, parentName);
            st.setString(4, projectName);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            return -1;
        }
    }


}
