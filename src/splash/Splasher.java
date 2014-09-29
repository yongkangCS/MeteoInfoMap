/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splash;

/**
 *
 * @author yaqiang
 */
public class Splasher {
     public static void main(String[] args) {
        SplashWindow.splash(Splasher.class.getResource("splash.gif"));
        SplashWindow.invokeMain("meteoinfo.forms.Program", args);
        SplashWindow.disposeSplash();
    }
}
