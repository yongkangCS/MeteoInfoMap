/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteoinfo.forms;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.codehaus.groovy.control.CompilationFailedException;
import org.meteoinfo.global.util.FontUtil;
import org.meteoinfo.global.util.GlobalUtil;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

/**
 *
 * @author yaqiang
 */
public class Program {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //registerFonts();
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("-e")) {
                runTextEditor(args);
            } else if (args[0].equalsIgnoreCase("-b")) {
                if (args.length == 1) {
                    System.exit(0);
                } else {
                    String fn = args[1];
                    if (new File(fn).isFile()) {
                        System.setProperty("java.awt.headless", "true");
                        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                        System.out.println("Headless mode: " + ge.isHeadless());
                        runScript(args, fn, 1);
                    } else {
                        System.exit(0);
                    }
                }
            } else {
                String fn = args[0];
                if (new File(fn).isFile()) {
                    runScript(args, fn, 0);
                } else {
                    runApplication();
                }
            }
        } else {
            runApplication();
        }
    }

    private static void runScript(String args[], String fn, int idx) {
        try {
            String ext = GlobalUtil.getFileExtension(fn);
            if (ext.equals("groovy")) {
                System.out.println("Running Groovy script...");
                GroovyShell shell = new GroovyShell();
                //shell.setVariable("miapp", new FrmMainOld());
                Script script = shell.parse(new File(fn));
                script.run();
                System.exit(0);
            } else if (ext.equals("py")) {
                System.out.println("Running Jython script...");
                PySystemState state = new PySystemState();
                if (args.length > idx + 1) {
                    //state.argv.clear ();
                    //state.argv.append (new PyString (fn));  
                    for (int i = idx + 1; i < args.length; i++) {
                        state.argv.append(new PyString(args[i]));
                    }
                }

                PythonInterpreter interp = new PythonInterpreter(null, state);
                //interp.set("miapp", new FrmMainOld());
                interp.execfile(fn);
                System.exit(0);
            }
        } catch (CompilationFailedException ex) {
            Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void runTextEditor(final String args[]) {
        /* Set look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            //UIManager.setLookAndFeel("javax.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                FrmTextEditor frmTE = new FrmTextEditor();
                frmTE.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frmTE.setLocationRelativeTo(null);
                frmTE.setVisible(true);
                if (args.length > 1) {
                    String fn = args[1];
                    if (new File(fn).isFile()) {
                        frmTE.openFiles(new File[]{new File(fn)});
                    }
                }
            }
        });
    }

    private static void runApplication() {
        /* Set look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            //UIManager.setLookAndFeel("javax.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmTextEditor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            final SplashScreen splash = SplashScreen.getSplashScreen();
//                            if (splash == null){
//                                System.out.println("SplashScreen.getSplashScreen() returned null");
//                                return;
//                            }
//                            Graphics2D g = splash.createGraphics();
//                            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                            g.setFont(new Font("Arial", Font.BOLD, 60));
//                            g.setColor(Color.red);
//                            g.drawString("MeteoInfo", 100, 200);
//                            splash.update();
//                            Thread.sleep(1000);
//                            //splash.setImageURL(Program.class.getResource("/meteoinfo/resources/logo.png"));
//                            //splash.update();
//                        } catch (Exception e) {
//                        }
//                    }
//                }.start();

                //Locale.setDefault(Locale.ENGLISH);
                //registerFonts();
                FrmMain frame = new FrmMain();
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    private static void registerFonts() {
        FontUtil.registerWeatherFont();
        String fn = GlobalUtil.getAppPath(FrmMain.class);
        //fn = fn.substring(0, fn.lastIndexOf("/"));
        String path = fn + File.separator + "font";
        File pathDir = new File(path);
        if (pathDir.isDirectory()) {
            File[] files = pathDir.listFiles();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (File file : files) {
                try {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, file);
                    ge.registerFont(font);
                } catch (FontFormatException ex) {
                    Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Program.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
