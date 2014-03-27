package bitbot.handler.channel.tasks;

import bitbot.server.Constants;
import java.io.IOException;
import java.io.PrintStream;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 *
 * @author z
 */
public class SpamErrorTask implements Runnable {
    
    private final Response response;
    private final Request request;

    public SpamErrorTask(Request request, Response response) {
        this.response = response;
        this.request = request;
    }

    @Override
    public void run() {
        try {
            try (PrintStream body = response.getPrintStream()) {
                long time = System.currentTimeMillis();

                response.setValue("Content-Type", "text/plain");
                response.setValue("Server", Constants.Server_UserAgent);
                response.setDate("Date", time);
                response.setDate("Last-Modified", time);
                response.setStatus(Status.SERVICE_UNAVAILABLE);
                
                body.println("too fast, or you have been banned. Contact support. bitbotlive@outlook.com");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
