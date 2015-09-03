package bitbot.logging;

/**
 *
 * @author z
 */

public enum ServerLogGroup {
    Game, // Logs any field activity such as HT/pB/ field cheat
    Warning, // Logs critical stuff
    Info, // information as suggest
    ;
    
    private ServerLogGroup() {
    }
}
