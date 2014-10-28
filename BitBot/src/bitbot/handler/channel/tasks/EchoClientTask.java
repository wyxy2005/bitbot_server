package bitbot.handler.channel.tasks;

import bitbot.server.Constants;
import java.io.IOException;
import java.io.PrintStream;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 *
 * @author z
 */
public class EchoClientTask implements Runnable {

    private final Response response;
    private final Request request;

    public EchoClientTask(Request request, Response response) {
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
                response.setValue("Info", "There is nothing to see here ;)");
                response.setDate("Date", time);
                response.setDate("Last-Modified", time);

                body.println("You are connected to BitBot server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
