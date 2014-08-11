package bitbot.handler.channel.tasks;

import bitbot.server.scripting.AbstractScriptInteraction;
import bitbot.server.scripting_support.MarketBacktestScriptManager;
import java.io.IOException;
import java.io.PrintStream;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 *
 * @author z
 */
public class BacktestTask implements Runnable {

    private final Response response;
    private final Request request;

    private String content;

    public BacktestTask(Request request, Response response, Query query) {
        this.response = response;
        this.request = request;

        try {
            content = request.getContent();
        } catch (IOException exp) {
            content = null;
        }
    }

    @Override
    public void run() {
        try {
            try (PrintStream body = response.getPrintStream()) {
                if (content != null) {
                    String returnResult = MarketBacktestScriptManager.startBacktestScriptfromString(content);
                    if (returnResult == null) {
                        body.println("yolo! it works");
                        
                        response.setStatus(Status.OK);
                    } else {
                        body.println(returnResult);
                        
                        response.setStatus(Status.BAD_REQUEST);
                    }
                } else {
                    body.println("NO CONTENT AVAILABLE!");
                    
                    response.setStatus(Status.BAD_REQUEST);
                }
                body.close();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
