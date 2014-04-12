package bitbot;

import bitbot.handler.channel.ChannelServer;
import bitbot.server.threads.MultiThreadExecutor;
import bitbot.server.threads.TimerManager;
import bitbot.server.threads.TimerManager_Ping;
import bitbot.util.MT4CVSReader;
import java.util.Scanner;

/**
 *
 * @author z
 */
public class Main {

    public static void main(String args[]) throws Exception {
        String command = args[0];
        switch (command) {
            case "start": {
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
                break;
            }
            case "readMT4Data": {
                if (args.length <= 1) {
                    System.out.println("Please specify the .CVS path.");
                    return;
                }
                String CVSFile = args[1];
                
                MT4CVSReader.ReadCVSFile(CVSFile);
            }
        }
    }

    private static void ProcessCommand(String[] input) {

    }
}
