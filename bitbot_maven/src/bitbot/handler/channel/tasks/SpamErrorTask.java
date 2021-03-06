package bitbot.handler.channel.tasks;

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
                _ResponseHeader.addBasicResponseHeader(response);
                
                response.setStatus(Status.SERVICE_UNAVAILABLE);
                
                
                String retString = "too fast, or you have been banned. Contact support. bitbotlive@outlook.com";
                
                response.setContentLength(retString.length());
                body.println(retString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
