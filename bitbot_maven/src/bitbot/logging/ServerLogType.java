package bitbot.logging;
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
    RemoteError             ("server_error.remoteRMIError", ServerLogGroup.Warning),
    UnhandledScript	    ("server_info.UnhandledScript", ServerLogGroup.Info),
    PacketSpam  	    ("server_info.PacketSpam", ServerLogGroup.Info),
    SwapTask                ("server_info.SwapTask", ServerLogGroup.Warning),
    HistoryCacheTask	    ("server_info.HistoryCacheTask", ServerLogGroup.Warning),
    HistoryCacheTask_DB	    ("server_info.HistoryCacheTask_Database", ServerLogGroup.Warning),
    HistoryIndexCacheTask_DB("server_info.HistoryIndexCacheTask_Database", ServerLogGroup.Warning),
    HistoryTradesTask_DB    ("server_info.HistoryTradesTask_Database", ServerLogGroup.Warning),
    PushNotification        ("server_info.PushNotification", ServerLogGroup.Warning),
    ;
    private final String coutFile;
    private final ServerLogGroup group;

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
