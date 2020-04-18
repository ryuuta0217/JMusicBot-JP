package dev.cosgy.JMusicBot.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceUtil {
    public static String getStackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter pWriter = new PrintWriter(writer);
        throwable.printStackTrace(pWriter);
        pWriter.flush();
        return writer.toString().substring(0, (2000-3))+"...";
    }
}
