package web.view.ukhorskaya;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;

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


    public static ErrorWriterOnServer getInstance() {
        return writer;
    }
}
