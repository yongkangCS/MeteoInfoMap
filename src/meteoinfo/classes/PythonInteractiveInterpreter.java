/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteoinfo.classes;

//import bsh.JavaCharStream;
//import bsh.util.JConsole;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
//import org.python.core.PyObject;
//import org.python.util.InteractiveConsole;

/**
 *
 * @author yaqiang
 */
//public class PythonInteractiveInterpreter extends InteractiveConsole implements Runnable {
public class PythonInteractiveInterpreter implements Runnable {

    transient Reader in;
    transient PrintStream out;
    transient PrintStream err;
    //JConsole console;

//    public PythonInteractiveInterpreter(JConsole paramJConsole) {
//        this.console = paramJConsole;
//        this.in = paramJConsole.getIn();
//        this.out = paramJConsole.getOut();
//        this.err = paramJConsole.getErr();
//        setOut(this.out);
//        setErr(this.err);
//    }
//
    public void run() {
//        int i = 0;
//        JavaCharStream localJavaCharStream = new JavaCharStream(this.in, 1, 1);
//        exec("_ps1 = sys.ps1");
//        PyObject localPyObject1 = get("_ps1");
//        String str1 = localPyObject1.toString();
//        exec("_ps2 = sys.ps2");
//        PyObject localPyObject2 = get("_ps2");
//        String str2 = localPyObject2.toString();
//        this.out.print(getDefaultBanner() + "\n");
//        this.out.print(str1);
//        String str3 = "";
//        while (i == 0) {
//            System.out.flush();
//            System.err.flush();
//            Thread.yield();
//            try {
//                int j = 0;
//                str3 = "";
//                while (j == 0) {
//                    char c = localJavaCharStream.readChar();
//                    j = c == '\n' ? 1 : 0;
//                    if (j == 0) {
//                        str3 = str3 + c;
//                    }
//                }
//                if (str3.equals(";")) {
//                    str3 = "";
//                }
//                boolean bool = push(str3);
//                if (bool) {
//                    this.out.print(str2);
//                } else {
//                    this.out.print(str1);
//                }
//            } catch (IOException localIOException) {
//            }
//        }
    }
}
