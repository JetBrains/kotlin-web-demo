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
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.examplesLoader.Project;
import org.jetbrains.webdemo.examplesLoader.ProjectFile;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class MySqlConnector {
    private static final MySqlConnector connector = new MySqlConnector();
    private Connection connection;
    private String databaseUrl;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Random idGenerator = new Random();

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

    public void saveFile(UserInfo userInfo, ProjectFile file) throws DatabaseOperationException {
        try (PreparedStatement st = connection.prepareStatement("UPDATE files JOIN " +
                "projects ON files.project_id = projects.id JOIN " +
                "users ON projects.owner_id = users.id SET" +
                " files.content = ? WHERE" +
                " users.client_id = ? AND users.provider = ? AND files.public_id = ?  ")) {
            st.setString(1, file.getText());
            st.setString(2, userInfo.getId());
            st.setString(3, userInfo.getType());
            st.setString(4, file.getPublicId());
            st.execute();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("File with this name already exist in this project", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + file.getPublicId() + " " + file.getName());
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + file.getPublicId() + " " + file.getName());
            throw new DatabaseOperationException("Unknown exception", e);
        }
    }

    private String escape(String str) {
        return str.replaceAll(" ", "%20");
    }

    private String unEscape(String str) {
        return str.replaceAll("%20", " ");
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


    public void saveProject(UserInfo userInfo, String publicId, Project project) throws DatabaseOperationException {
        int userId = getUserId(userInfo);
        try (PreparedStatement st = connection.prepareStatement(
                "UPDATE projects SET projects.args = ? , projects.run_configuration = ? " +
                        "WHERE projects.owner_id = ?  AND projects.name = ? AND projects.public_id = ?")) {
            st.setString(1, project.args);
            st.setString(2, project.confType);
            st.setString(3, userId + "");
            st.setString(4, escape(project.name));
            st.setString(5, publicId);
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


    public String addProject(UserInfo userInfo, String name) throws DatabaseOperationException {
        try {
            String projectId = addProject(userInfo, new Project(name, "My Programs", "", "java", null));
            String fileId = addFileToProject(userInfo, projectId, name);

            ObjectNode response = new ObjectNode(JsonNodeFactory.instance);
            response.put("projectId", projectId);
            response.put("fileId", fileId);
            return objectMapper.writeValueAsString(response);
        } catch (IOException e) {
            throw new DatabaseOperationException("IO exception");
        }
    }

    public String addProject(UserInfo userInfo, Project project) throws DatabaseOperationException {
        int userId = getUserId(userInfo);
        PreparedStatement st = null;
        try {
            String publicId = userInfo.getId() + idGenerator.nextInt();

            st = connection.prepareStatement("INSERT INTO projects (owner_id, name, args, run_configuration, origin, public_id) VALUES (?,?,?,?,?,?) ");
            st.setString(1, userId + "");
            st.setString(2, escape(project.name));
            st.setString(3, project.args);
            st.setString(4, project.confType);
            st.setString(5, project.originUrl);
            st.setString(6, publicId);
            st.execute();

            int projectId = getProjectId(userInfo, publicId);
            for (ProjectFile file : project.files) {
                addFileToProject(userInfo, projectId, file.getName(), file.getText());
            }

            return publicId;
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

    public String addFileToProject(UserInfo userInfo, String projectPublicId, String fileName) throws DatabaseOperationException {
        return addFileToProject(userInfo, getProjectId(userInfo, projectPublicId), fileName, "");
    }

    public String addFileToProject(UserInfo userInfo, String projectPublicId, String fileName, String content) throws DatabaseOperationException {
        return addFileToProject(userInfo, getProjectId(userInfo, projectPublicId), fileName, content);
    }

    private String addFileToProject(UserInfo userInfo, int projectId, String fileName, String content) throws DatabaseOperationException {
        fileName = escape(fileName.endsWith(".kt") ? fileName : fileName + ".kt");
        try (PreparedStatement st = connection.prepareStatement("INSERT INTO files (project_id, public_id, name, content) VALUES (?,?,?,?) ")) {
            String publicId = userInfo.getId() + idGenerator.nextInt();

            st.setString(1, projectId + "");
            st.setString(2, publicId);
            st.setString(3, fileName);
            st.setString(4, content);
            st.execute();

            return publicId;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("File with this name already exist in this project", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectId + " " + fileName);
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Add file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + projectId + " " + fileName);
            throw new DatabaseOperationException("Unknown exception", e);
        }
    }

    public ArrayNode getProjectHeaders(UserInfo userInfo) throws DatabaseOperationException {
        if (!checkConnection()) {
            throw new DatabaseOperationException("Cannot connect to database to load list of your programs.");
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement(
                    "SELECT projects.public_id, projects.name FROM projects JOIN " +
                            "users ON projects.owner_id = users.id WHERE " +
                            "(users.client_id = ? AND users.provider = ?)"
            );
            st.setString(1, userInfo.getId());
            st.setString(2, userInfo.getType());

            rs = st.executeQuery();

            ArrayNode projects = new ArrayNode(JsonNodeFactory.instance);
            while (rs.next()) {
                ObjectNode object = new ObjectNode(JsonNodeFactory.instance);
                object.put("name", unEscape(rs.getString("name")));
                object.put("publicId", rs.getString("public_id"));
                projects.add(object);
            }

            return projects;
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown",
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName());
            throw new DatabaseOperationException("Unknown error while loading list of your programs");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }


    public String getProjectContent(UserInfo userInfo, String publicId) {
        if (!checkConnection()) {
            return ResponseUtils.getErrorInJson("Cannot connect to database for load your program.");
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement(
                    "SELECT projects.id, projects.name, projects.args, projects.run_configuration, projects.origin FROM projects WHERE " +
                            "projects.public_id = ?");
            st.setString(1, publicId);
            rs = st.executeQuery();

            if (rs.next()) {
                Project project = new Project(
                        unEscape(rs.getString("name")),
                        "My Programs",
                        rs.getString("args"),
                        rs.getString("run_configuration"),
                        rs.getString("origin")
                );

                st = connection.prepareStatement("SELECT * FROM files WHERE project_id = ?");
                st.setString(1, rs.getInt("id") + "");
                rs = st.executeQuery();
                while (rs.next()) {
                    ProjectFile file = new ProjectFile(unEscape(rs.getString("name")), rs.getString("content"), true, rs.getString("public_id"));
                    project.files.add(file);
                }
                return objectMapper.writeValueAsString(project);
            } else {
                return ResponseUtils.getErrorInJson("Can't load your project");
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", publicId);
            return ResponseUtils.getErrorInJson("Unknown error while loading your project");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }

    public boolean isProjectExists(String publicId) throws DatabaseOperationException {
        if (!checkConnection()) {
            throw new DatabaseOperationException("Cannot connect to database to check.");
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement(
                    "SELECT projects.id FROM projects WHERE projects.public_id = ?");
            st.setString(1, publicId);
            rs = st.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", publicId);
            throw new DatabaseOperationException("Unknown exception");
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

    public void deleteFile(UserInfo userInfo, String publicId) throws DatabaseOperationException {
        if (!checkConnection()) {
            throw new DatabaseOperationException("Cannot connect to database to delete your program.");
        }
        try (PreparedStatement st = connection.prepareStatement("DELETE files.* FROM files JOIN" +
                " projects ON files.project_id = projects.id JOIN " +
                " users ON projects.owner_id = users.id WHERE " +
                " users.client_id = ? AND users.provider  = ? AND files.public_id = ?")) {
            st.setString(1, userInfo.getId());
            st.setString(2, userInfo.getType());
            st.setString(3, publicId);
            st.executeUpdate();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + publicId);
            throw new DatabaseOperationException("Unknown exception ", e);
        }

    }


    public void renameFile(UserInfo userInfo, String publcId, String newName) throws DatabaseOperationException {
        try (PreparedStatement st = connection.prepareStatement("UPDATE files JOIN " +
                "projects ON files.project_id = projects.id JOIN " +
                "users ON projects.owner_id = users.id SET " +
                "files.name = ? WHERE " +
                "users.client_id = ? AND  users.provider = ? AND files.public_id = ?")) {
            st.setString(1, escape(newName));
            st.setString(2, userInfo.getId());
            st.setString(3, userInfo.getType());
            st.setString(4, publcId);
            st.execute();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("File with this name already exist in this project", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Rename file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + publcId);
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Rename file " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + publcId);
            throw new DatabaseOperationException("Unknown exception", e);
        }
    }


    public void deleteProject(UserInfo userInfo, String publicId) throws DatabaseOperationException {
        if (!checkConnection()) {
            throw new DatabaseOperationException("Cannot connect to database to delete your program.");
        }
        int userId = getUserId(userInfo);
        try (PreparedStatement st = connection.prepareStatement("DELETE FROM projects WHERE projects.owner_id = ? AND projects.public_id = ?")) {
            st.setString(1, userId + "");
            st.setString(2, publicId);
            st.executeUpdate();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Delete project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + publicId);
            throw new DatabaseOperationException("Unknown exception");
        }

    }

    public void renameProject(UserInfo userInfo, String publicId, String newName) throws DatabaseOperationException {
        int userId = getUserId(userInfo);
        try (PreparedStatement st = connection.prepareStatement("UPDATE projects SET projects.name = ? WHERE projects.public_id =? AND projects.owner_id = ?")) {
            st.setString(1, escape(newName));
            st.setString(2, publicId);
            st.setString(3, userId + "");
            st.execute();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseOperationException("Project with this name already exist", e);
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Rename project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + publicId);
                throw new DatabaseOperationException("Unknown exception", e);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.WORK_WITH_DATABASE.name(), "unknown", "Rename project " + userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + publicId + " ");
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

    private int getProjectId(UserInfo userInfo, String publicId) throws DatabaseOperationException {
        try (PreparedStatement st = connection.prepareStatement(
                "SELECT projects.id FROM projects JOIN " +
                        "users ON projects.owner_id =users.id WHERE " +
                        "( users.client_id = ? AND  users.provider = ? AND projects.public_id = ?)")) {
            st.setString(1, userInfo.getId());
            st.setString(2, userInfo.getType());
            st.setString(3, publicId);
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

    public String getProjectHeaderInfoByPublicId(UserInfo userInfo, String publicId) throws DatabaseOperationException {
        if (!checkConnection()) {
            throw new DatabaseOperationException("Cannot connect to database for load your program.");
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connection.prepareStatement("SELECT users.client_id, users.provider, projects.public_id, projects.name FROM files JOIN " +
                    "projects ON files.project_id = projects.id JOIN " +
                    "users ON projects.owner_id = users.id WHERE" +
                    " files.public_id = ?");
            st.setString(1, publicId);
            rs = st.executeQuery();
            if (rs.next()) {
                boolean isUserProject = rs.getString("client_id").equals(userInfo.getId()) && rs.getString("provider").equals(userInfo.getType());

                ObjectNode response = new ObjectNode(JsonNodeFactory.instance);
                response.put("name", unEscape(rs.getString("name")));
                response.put("publicId", rs.getString("public_id"));
                if (isUserProject) {
                    response.put("type", "USER_PROJECT");
                } else {
                    response.put("type", "PUBLIC_LINK");
                }


                return objectMapper.writeValueAsString(response);
            } else {
                throw new DatabaseOperationException("File with this ID do not exist");
            }
        } catch (Exception e) {
            throw new DatabaseOperationException("Unknown exception");
        } finally {
            closeStatementAndResultSet(st, rs);
        }
    }


}
