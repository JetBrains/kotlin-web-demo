/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package org.jetbrains.webdemo.mail;

import org.apache.naming.NamingContext;
import org.jetbrains.webdemo.ErrorWriter;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class MailAgent {
    private static MailAgent instance = new MailAgent();
    private Session session;
    private String to;

    private MailAgent () {
        InitialContext initCtx = null;
        try {
            initCtx = new InitialContext();
            NamingContext envCtx = (NamingContext) initCtx.lookup("java:comp/env");
            String smtpHost = (String) envCtx.lookup("mail.smtp.host");
            Properties prop = new Properties();
            prop.setProperty("mail.smtp.host", smtpHost);
            session = Session.getInstance(prop);
            to = (String) envCtx.lookup("ask_question_mail");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public static MailAgent getInstance(){
        return instance;
    }

    public void send(String from, String name, String subject, String text){
        try{
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, name, "UTF-8"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(text, "UTF-8");
            Transport.send(message);
        }catch (Exception mex) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(mex, "Can't send message", "");
            mex.printStackTrace();
        }
    }
}
