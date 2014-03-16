/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bitbot.handler;

import java.io.IOException;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.Socket;

/**
 *
 * @author z
 */
public class SSLServer implements org.simpleframework.transport.Server {

   private final Server delegateServer;

   public SSLServer(Server delegateServer){
      this.delegateServer = delegateServer;
   }

   @Override
   public void process(Socket socket){
     //socket.getEngine().setNeedClientAuth(true);
       try {
           delegateServer.process(socket);
       } catch (IOException e) {
       }
   }
   
   @Override
   public void stop() {
       try {
           delegateServer.stop();
       } catch (IOException e) {
       }
   }
}

