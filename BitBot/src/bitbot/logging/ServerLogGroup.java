/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
