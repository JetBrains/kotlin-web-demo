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
import org.jetbrains.webdemo.examplesLoader.Project;
import org.jetbrains.webdemo.examplesLoader.ProjectFile;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlConnector {
    private static final MySqlConnector connector = new MySqlConnector();
    private Connection connection;
    private String databaseUrl;
    private ObjectMapper objectMapper = new ObjectMapper();

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
        try {
            getUserId(userInfo);
            return true;
        } catch (DatabaseOperationException e) {
            return false;
        }

    }

    public void saveFile(UserInfo userInfo, String projectName, ProjectFile file) throws DatabaseOperationException {
        int projectId = getProjectId(userInfo, projectName);
        try (PreparedStatement st = connection.prepareStatement("UPDATE files SET files.content = ? WHERE project_id = ? AND files.name = ?  ")) {
            st.setString(1, file.getContent());
            st.setString(2, projectId + "");
            st.setString(3, file.getName());
            st.execute();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("File with this name already exist in this project", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectName + " " + file.getName());
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectName + " " + file.getName());
            throw new DatabaseOperationException("Unknown exception", e);
        }
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


    public void saveProject(UserInfo userInfo, Project project) throws DatabaseOperationException {
        int userId = getUserId(userInfo);
        try (PreparedStatement st = connection.prepareStatement(
                "UPDATE projects SET projects.args = ? , projects.run_configuration = ? " +
                        "WHERE projects.owner_id = ?  AND projects.name = ?")) {
            st.setString(1, project.args);
            st.setString(2, project.confType);
            st.setString(3, userId + "");
            st.setString(4, project.name);
            st.execute();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("Project with this name already exist", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + project.name);
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + project.name);
            throw new DatabaseOperationException("Unknown exception", e);
        }
    }


    public void addProject(UserInfo userInfo, String name) throws DatabaseOperationException {
        int userId = getUserId(userInfo);
        try (PreparedStatement st = connection.prepareStatement("INSERT INTO projects (owner_id, name) VALUES (?,?) ")) {
            st.setString(1, userId + "");
            st.setString(2, name);
            st.execute();
            int projectId = getProjectId(userInfo, name);
            if (projectId != -1) {
                try (PreparedStatement st2 = connection.prepareStatement("INSERT INTO files (project_id, name, content) VALUES (?, ?, ?)")) {
                    String fileName = name.endsWith(".kt") ? name : name + ".kt";
                    st2.setString(1, projectId + "");
                    st2.setString(2, fileName);
                    st2.setString(3, "");
                    st2.execute();
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("Project with this name already exist", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + name);
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + name);
            throw new DatabaseOperationException("Unknown exception", e);
        }
    }

    public void addProject(UserInfo userInfo, Project project) throws DatabaseOperationException {
        int userId = getUserId(userInfo);
        PreparedStatement st = null;
        try {
            st = connection.prepareStatement("INSERT INTO projects (owner_id, name, args, run_configuration, origin) VALUES (?,?,?,?, ?) ");
            st.setString(1, userId + "");
            st.setString(2, project.name);
            st.setString(3, project.args);
            st.setString(4, project.confType);
            st.setString(5, project.originUrl);
            st.execute();

            int projectId = getProjectId(userInfo, project.name);
            for (ProjectFile file : project.files) {
                st = connection.prepareStatement("INSERT INTO files (project_id, name, content) VALUES (?,?,?)");
                st.setString(1, projectId + "");
                st.setString(2, file.getName());
                st.setString(3, file.getContent());
                st.execute();
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("Project with this name already exist", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + project.name);
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + project.name);
            throw new DatabaseOperationException("Unknown exception", e);
        } finally {
            closeStatement(st);
        }
    }

    public void addFile(UserInfo userInfo, String projectName, String fileName) throws DatabaseOperationException {
        addFile(userInfo, projectName, fileName, "");
    }

    public void addFile(UserInfo userInfo, String projectName, String fileName, String content) throws DatabaseOperationException {
        int projectId = getProjectId(userInfo, projectName);
        try (PreparedStatement st = connection.prepareStatement("INSERT INTO files (project_id, name, content) VALUES (?,?,?) ")) {
            st.setString(1, projectId + "");
            st.setString(2, fileName);
            st.setString(3, content);
            st.execute();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("File with this name already exist in this project", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectName + " " + fileName);
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectName + " " + fileName);
            throw new DatabaseOperationException("Unknown exception", e);
        }
    }

    public String getProjectNames(UserInfo userInfo) {
        if (!checkConnection()) {
            return ResponseUtils.getErrorInJson("Cannot connect to database to load list of your programs.");
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement(
                    "SELECT projects.id, projects.name FROM projects JOIN " +
                            "users ON projects.owner_id = users.id WHERE " +
                            "(users.client_id = ? AND users.provider = ?)"
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


    public String getProjectContent(UserInfo userInfo, String projectName) {
        if (!checkConnection()) {
            return ResponseUtils.getErrorInJson("Cannot connect to database for load your program.");
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement(
                    "SELECT projects.id,projects.args, projects.run_configuration, projects.origin FROM projects JOIN " +
                            "users ON projects.owner_id = users.id WHERE " +
                            "(users.client_id = ? AND users.provider = ? AND projects.name = ?)");
            st.setString(1, userInfo.getId());
            st.setString(2, userInfo.getType());
            st.setString(3, projectName);
            rs = st.executeQuery();

            if (rs.next()) {
                Project project = new Project();
                project.parent = "My Programs";
                project.name = projectName;
                project.args = rs.getString("args");
                project.confType = rs.getString("run_configuration");
                project.files = new ArrayList<>();

                project.isLocalVersion = true;
                if (rs.getString("origin") != null) {
                    project.originUrl = rs.getString("origin");
                    Project storedExample = ExamplesList.getExampleObject(rs.getString("origin"));
                    for(ProjectFile file : storedExample.files){
                        if(!file.isModifiable()){
                            project.files.add(file);
                        }
                    }
                }

                st = connection.prepareStatement("SELECT * FROM files WHERE project_id = ?");
                st.setString(1, rs.getInt("id") + "");
                rs = st.executeQuery();
                while (rs.next()) {
                    ProjectFile file = new ProjectFile(rs.getString("name"), rs.getString("content"), true);
                    project.files.add(file);
                }
                return objectMapper.writeValueAsString(project);
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

    public void deleteFile(UserInfo userInfo, String projectName, String fileName) throws DatabaseOperationException {
        if (!checkConnection()) {
            throw new DatabaseOperationException("Cannot connect to database to delete your program.");
        }
        int projectId = getProjectId(userInfo, projectName);
        PreparedStatement st = null;
        ResultSet resultSet = null;
        try {
            st = connection.prepareStatement("DELETE FROM files WHERE files.project_id =? AND files.name = ?");
            st.setString(1, projectId + "");
            st.setString(2, fileName);
            st.executeUpdate();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + fileName);
            throw new DatabaseOperationException("Unknown exception ", e);
        } finally {
            closeStatementAndResultSet(st, resultSet);
        }

    }


    public void renameFile(UserInfo userInfo, String projectName, String fileName, String newName) throws DatabaseOperationException {
        int projectId = getProjectId(userInfo, projectName);
        try (PreparedStatement st = connection.prepareStatement("UPDATE files SET files.name = ? WHERE files.name = ? AND files.project_id = ?")) {
            st.setString(1, newName);
            st.setString(2, fileName);
            st.setString(3, projectId + "");
            st.execute();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("File with this name already exist in this project", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Rename file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectName + " " + fileName);
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Rename file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectName + " " + fileName);
            throw new DatabaseOperationException("Unknown exception", e);
        }
    }


    public void deleteProject(UserInfo userInfo, String projectName) throws DatabaseOperationException {
        projectName = projectName.replaceAll("_", " ");
        if (!checkConnection()) {
            throw new DatabaseOperationException("Cannot connect to database to delete your program.");
        }
        int userId = getUserId(userInfo);
        try (PreparedStatement st = connection.prepareStatement("DELETE FROM projects WHERE projects.owner_id = ? AND projects.name = ?")) {
            st.setString(1, userId + "");
            st.setString(2, projectName);
            st.executeUpdate();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Delete project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectName);
            throw new DatabaseOperationException("Unknown exception");
        }

    }

    public void renameProject(UserInfo userInfo, String projectName, String newName) throws DatabaseOperationException {
        int userId = getUserId(userInfo);
        try (PreparedStatement st = connection.prepareStatement("UPDATE projects SET projects.name = ? WHERE projects.name =? AND projects.owner_id = ?")) {
            st.setString(1, newName);
            st.setString(2, projectName);
            st.setString(3, userId + "");
            st.execute();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("Project with this name already exist", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Rename project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectName);
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Rename project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectName + " ");
            throw new DatabaseOperationException("Unknown exception", e);
        }
    }

    private int getUserId(UserInfo userInfo) throws DatabaseOperationException {
        try (PreparedStatement st = connection.prepareStatement("SELECT users.id FROM users WHERE (users.client_id = ? AND users.provider=?)")) {
            st.setString(1, userInfo.getId());
            st.setString(2, userInfo.getType());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new DatabaseOperationException("User with this id don't exist");
                }
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            throw new DatabaseOperationException("Unknown exception", e);
        }
    }

    private int getProjectId(UserInfo userInfo, String projectName) throws DatabaseOperationException {
        try (PreparedStatement st = connection.prepareStatement(
                "SELECT projects.id FROM projects JOIN " +
                        "users ON projects.owner_id =users.id WHERE " +
                        "( users.client_id = ? AND  users.provider = ? AND projects.name = ?)")) {
            st.setString(1, userInfo.getId());
            st.setString(2, userInfo.getType());
            st.setString(3, projectName);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new DatabaseOperationException("Project with this name don't exist");
                }
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            throw new DatabaseOperationException("UnknownException", e);
        }
    }


}
