package web.view.ukhorskaya.authorization;

import com.intellij.openapi.util.Pair;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import web.view.ukhorskaya.ErrorWriter;
import web.view.ukhorskaya.ErrorWriterOnServer;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.server.ServerSettings;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/21/11
 * Time: 12:51 PM
 */
public class UserAuthenticator extends BasicAuthenticator {
    private final String type;

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

    public UserAuthenticator(String type) {
        super(type);
        this.type = type;
    }

    @Override
    public boolean checkCredentials(String s, String s1) {

        String query = "(objectclass=person)";
        String attribute = "cn";
        StringBuffer output = new StringBuffer();

        try {
//            String url = "ldap://msdc.labs.intellij.net";
//            String url = "ldap://msdc.labs.intellij.net:389/DC=labs,DC=intellij,DC=net";
            String url = "ldap://directory.cornell.edu/o=Cornell%20University,c=US";
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, url);
            DirContext context = new InitialDirContext(env);

            SearchControls ctrl = new SearchControls();
            ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration enumeration = context.search("", query, ctrl);
            for (int i = 0; i < 20; i++) {
                if (enumeration.hasMore()) {
// while (enumeration.hasMore()) {
                    SearchResult result = (SearchResult) enumeration.next();
                    Attributes attribs = result.getAttributes();
                    NamingEnumeration values = ((BasicAttribute) attribs.get(attribute)).getAll();
                    for (int j = 0; j < 20; j++) {
                        if (values.hasMore()) {
//                while (values.hasMore()) {
                            if (output.length() > 0) {
                                output.append("\n");
                            }
                            output.append(values.next().toString());
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print(output.toString());

        /*Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL,
                "ldap://msdc.labs.intellij.net:389/dc=labs,dc=intellij,dc=net ");

        DirContext ctx;
        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        LinkedList list = new LinkedList();
        NamingEnumeration results = null;
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            results = ctx.search("LABS\\Natalia.Ukhorskaya", "(objectclass=person)", controls);

            while (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                Attribute attr = attributes.get("cn");
                String cn = (String) attr.get();
                list.add(cn);
            }
        } catch (NameNotFoundException e) {
            // The base context was not found.
            // Just clean up and exit.
        } catch (NamingException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
//              return list;
        for (Object o : list) {
            System.out.println(o);
        }*/
        return false;
        /*Map<String, String> map = readUsersFromFile();
        if (map == null) {
            return false;
        }
        String password = map.get(s);
        try {
            s1 = generateMD5(s1);
        } catch (NoSuchAlgorithmException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("LOGIN", e, "login: " + s));
            return false;
        } catch (UnsupportedEncodingException e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("LOGIN", e, "login: " + s));
            return false;
        }
        return password != null && password.equals(s1);*/
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

    public String addUser(String login, String password) {
        try {
            File file = new File(ServerSettings.STATISTICS_ROOT + File.separator + type + ".xml");
            if (!file.exists()) {
                return "File doesn't exists: " + file.getAbsolutePath();
            }

            Map<String, String> map = readUsersFromFile();
            if (map == null) {
                return "Impossible to read xml file: " + file.getAbsolutePath();
            }
            if (map.containsKey(login)) {
                return "User already exists";
            }

            Document document = ResponseUtils.getXmlDocument(file);
            if (document == null) {
                return "Impossible to read xml file: " + file.getAbsolutePath();
            }
            NodeList nodeList = document.getElementsByTagName("users");
            Node newNode = document.createElement("user");
            newNode.appendChild(document.createElement("login"));
            newNode.appendChild(document.createTextNode(login));
            newNode.appendChild(document.createElement("password"));
            try {
                password = generateMD5(password);
            } catch (NoSuchAlgorithmException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("LOGIN", e, "login: " + login));
                return "Impossible to generate MD5";
            } catch (UnsupportedEncodingException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("LOGIN", e, "login: " + login));
                return "Impossible to read password in UTF-8";
            }
            newNode.appendChild(document.createTextNode(password));
            nodeList.item(0).appendChild(newNode);

            NodeList list = document.getElementsByTagName("users");
            Node node = list.item(0);

            DOMSource source = new DOMSource(node);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            FileWriter writer = new FileWriter(file);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            writer.close();
            return "User was added";
        } catch (Throwable e) {
            e.printStackTrace();
            return "Unknown error: User wasn't added";
        }
    }

    private String generateMD5(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] userPasswordMD5 = password.getBytes("UTF-8");
        userPasswordMD5 = messageDigest.digest(userPasswordMD5);
        BigInteger bigInt = new BigInteger(1, userPasswordMD5);
        String hashText = bigInt.toString(16);
        while (hashText.length() < 32) {
            hashText = "0" + hashText;
        }
        password = hashText;
        return password;
    }

    @Nullable
    private Map<String, String> readUsersFromFile() {
        try {
            //TODO add exceptions
            Map<String, String> users = new HashMap<String, String>();

            File file = new File(ServerSettings.STATISTICS_ROOT + File.separator + type + ".xml");
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
            System.out.println(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {
                NodeList children = nodeList.item(i).getChildNodes();
                users.put(children.item(1).getTextContent(), children.item(3).getTextContent());
            }
            return users;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
