/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bitbot.server;

/**
 *
 * @author z
 */

public enum ServerLogType {

    ServerMain		    ("server_common.Startup", ServerLogGroup.Info),
    ThreadError		    ("server_error.ThreadError", ServerLogGroup.Warning),
    MapTimer		    ("server_error.MapTimer", ServerLogGroup.Info),
    Timer		    ("server_error.Timer", ServerLogGroup.Info),
    ShutdownError	    ("server_error.shutdown", ServerLogGroup.Info),
    ShutdownHookError	    ("server_error.shutdownhook", ServerLogGroup.Info),
    ReconnectError	    ("server_error.reconnect", ServerLogGroup.Info),
    UnhandledScript	    ("server_info.UnhandledScript", ServerLogGroup.Info),
    PacketSpam  	    ("server_info.PacketSpam", ServerLogGroup.Info),
    HistoryCacheTask	    ("server_info.HistoryCacheTask", ServerLogGroup.Warning),
    HistoryCacheTask_DB	    ("server_info.HistoryCacheTask_Database", ServerLogGroup.Warning),
    ;
    private String coutFile;
    private ServerLogGroup group;

    private ServerLogType(String coutFile, ServerLogGroup group) {
	this.coutFile = String.format("%s%s", System.getProperty("file.separator"),  coutFile);
	this.group = group;
    }

    public String getcoutFile() {
	return coutFile;
    }
    
    public ServerLogGroup getLogGroup() {
	return group;
    }
}
