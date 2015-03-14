package bitbot.handler.channel.tasks;

import bitbot.server.Constants;
import org.simpleframework.http.Response;

/**
 *
 * @author zheng
 */
public class _ResponseHeader {

    public static final void addBasicResponseHeader(Response response) {
        long time = System.currentTimeMillis();

        response.setValue("Content-Type", "text/plain");
        response.setValue("Access-Control-Allow-Origin", "*");
        response.setValue("Server", Constants.Server_UserAgent);
        response.setValue("Info", "There is nothing to see here ;)");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
    }
}