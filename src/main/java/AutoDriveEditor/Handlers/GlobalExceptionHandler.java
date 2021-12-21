package AutoDriveEditor.Handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler{

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public void uncaughtException(Thread t, Throwable e) { LOGGER.info(e.getMessage(), e); }
}
