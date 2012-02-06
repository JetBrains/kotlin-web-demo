package org.jetbrains.demo.ukhorskaya;

import com.intellij.errorreport.bean.ErrorBean;
import com.intellij.errorreport.itn.ITNProxy;
import org.apache.log4j.Logger;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/5/11
 * Time: 12:30 PM
 */

public class ErrorWriterOnServer extends ErrorWriter {
    public static final Logger LOG_FOR_EXCEPTIONS = Logger.getLogger("exceptionLogger");
    public static final Logger LOG_FOR_INFO = Logger.getLogger("infoLogger");

    private static final ErrorWriterOnServer writer = new ErrorWriterOnServer();

    private ErrorWriterOnServer() {

    }

    @Override
    public void writeException(String moreInfo) {

        LOG_FOR_EXCEPTIONS.error(moreInfo);
    }

    @Override
    public void writeInfo(String message) {
        LOG_FOR_INFO.info(message);
    }

    public void writeExceptionToExceptionAnalyzer(Throwable e, String type, String description) {
        ErrorBean bean = new ErrorBean(e, type);
        bean.addProgramText(description);
        bean.setPluginName("Kotlin Web Demo");

        sendViaITNProxy(bean);


    }

    private void sendViaITNProxy(ErrorBean error) {
        String login = "Natalia.Ukhorskaya";
        String password = "pqow02";
        try {
            String result = ITNProxy.postNewThread(login, password, error, String.valueOf(System.currentTimeMillis()), ServerSettings.KOTLIN_VERSION);
            if ("unauthorized".equals(result) || result.startsWith("update ") || result.startsWith("message ")) {
                LOG_FOR_EXCEPTIONS.error(getExceptionForLog("SEND_TO_EA", result, ""));
                LOG_FOR_EXCEPTIONS.error(getExceptionForLog(error.getLastAction(), error.getMessage(), error.getDescription()));
            } else {
                LOG_FOR_INFO.info("Submitted to Exception Analyzer: " + result);
            }
        } catch (IOException e1) {
            LOG_FOR_EXCEPTIONS.error(getExceptionForLog("SEND_TO_EXCEPTION_ANALYZER", e1, login));
        }
    }

    public void writeExceptionToExceptionAnalyzer(String message, String stackTrace, String type, String description) {
        ErrorBean bean = new ErrorBean(message, stackTrace, type);
        bean.addProgramText(description);
        bean.setPluginName("Kotlin Web Demo");

        sendViaITNProxy(bean);
    }


    public static ErrorWriterOnServer getInstance() {
        return writer;
    }
}
