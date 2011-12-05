package web.view.ukhorskaya;

import org.apache.log4j.Logger;
import web.view.ukhorskaya.session.SessionInfo;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/5/11
 * Time: 12:30 PM
 */

public class ErrorsWriterOnServer extends ErrorsWriter {
    public static final Logger LOG_FOR_EXCEPTIONS = Logger.getLogger("exceptionLogger");
    public static final Logger LOG_FOR_INFO = Logger.getLogger("infoLogger");

    private static final ErrorsWriterOnServer writer = new ErrorsWriterOnServer();

    private ErrorsWriterOnServer() {
        
    }

    @Override
    public void writeException(String moreInfo) {
        LOG_FOR_EXCEPTIONS.error(moreInfo);
    }

    @Override
    public void writeInfo(String message) {
        LOG_FOR_INFO.error(message);
    }


    public static ErrorsWriterOnServer getInstance() {
        return writer;
    }
}
