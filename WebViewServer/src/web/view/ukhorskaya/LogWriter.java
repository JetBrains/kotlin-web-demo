package web.view.ukhorskaya;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/1/11
 * Time: 10:59 AM
 */

public class LogWriter extends ErrorsWriter {
    public static final Logger LOG_FOR_EXCEPTIONS = Logger.getLogger("exceptionLogger");
    public static final Logger LOG_FOR_INFO = Logger.getLogger("infoLogger");
}
