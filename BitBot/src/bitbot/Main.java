package bitbot;

import bitbot.handler.channel.ChannelServer;
import bitbot.server.threads.MultiThreadExecutor;
import bitbot.server.threads.TimerManager;
import bitbot.server.threads.TimerManager_Ping;
import java.util.Scanner;

/**
 *
 * @author z
 */
public class Main {
    
    
    public static void main(String args[]) throws Exception
    {
        // Start services
        MultiThreadExecutor.start();
        TimerManager.start();
        TimerManager_Ping.start();
        
        ChannelServer.getInstance().initializeChannelServer();
        
        /*Scanner sc = new Scanner(System.in);
        System.out.println(":::::::: Enter commands ::::::::");
        while (sc.hasNextLine()) {
            String NextLine = sc.nextLine();
            String[] input = NextLine.split(" ");
            
            switch (input[0]) {
                case "-benchmarkSelect": {
                    ProcessCommand(input);
                    break;
                }
            }
        }*/
    }
    
    private static void ProcessCommand(String[] input) {
        
    }
}
