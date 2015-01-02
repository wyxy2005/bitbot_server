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
                _ResponseHeader.addBasicResponseHeader(response);

                body.println("You are connected to BitBot server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
