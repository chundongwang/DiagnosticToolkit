
package com.microsoft.projecta.tools.common;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface Loggable {
    /**
     * message to log for specific logger and level with a throwable
     * 
     * @param logger
     * @param level
     * @param message
     * @param e
     */
    void onLogOutput(Logger logger, Level level, String message, Throwable e);
}
