package bitbot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author z
 */
public class FileoutputUtil {
    public static final String RSA_Key = "RSA_KEY.rtf",
	    RSA_gen_encrypted = "RSA_gen_encrypted.rtf",
	    RSA_gen_decrypted = "RSA_gen_decrypted.rtf";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    
    public static void log(final String filename, final String msg) {
	File f = new File("DataLog");
	if (!f.exists()) {
	    f.mkdirs();
	}
	try (FileOutputStream out = new FileOutputStream(f.getPath() + filename, true)) {
	    out.write(String.format("[%s] %s\r\n", CurrentReadable_Time(), msg).getBytes());
	} catch (IOException ess) {
	    ess.printStackTrace();
	}
    }

    public static void outputFileError(final String filename, final Throwable t) {
	File f = new File("DataLog");
	if (!f.exists()) {
	    f.mkdirs();
	}
	try (FileOutputStream out = new FileOutputStream(f.getPath() + filename, true)) {
	    out.write(String.format("[%s] %s\n--\n", CurrentReadable_Time(), getString(t)).getBytes());
	} catch (IOException ess) {
	    ess.printStackTrace();
	}
    }

    public static void logList(final String filename, final String group, List<String> msgs) {
	File f = new File(String.format("DataLog%s%s", System.getProperty("file.separator"), group));
	if (!f.exists()) {
	    f.mkdirs();
	}
	try (FileOutputStream out = new FileOutputStream(f.getPath() + filename, true)) {
	    for (String s : msgs) {
		out.write(String.format("[%s] %s\n--\n", CurrentReadable_Time(), s).getBytes());
	    }
	} catch (IOException ess) {
	    ess.printStackTrace();
	}
    }

    public static void outputFileErrorList(final String filename, final String group, List<Throwable> ts) {
	File f = new File(String.format("DataLog%s%s", System.getProperty("file.separator"), group));
	if (!f.exists()) {
	    f.mkdirs();
	}
	try (FileOutputStream out = new FileOutputStream(f.getPath() + filename, true)) {
	    for (Throwable t : ts) {
		out.write(String.format("[%s] %s\n--\n", CurrentReadable_Time(), getString(t)).getBytes());
	    }
	} catch (IOException ess) {
	    ess.printStackTrace();
	}
    }

    public static final String CurrentReadable_Time() {
	return sdf.format(Calendar.getInstance().getTime());
    }

    public static final String getString(final Throwable e) {
	String retValue = "";

	try (StringWriter sw = new StringWriter()) {
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    retValue = sw.toString();
	} catch (IOException iex) {
	    // fucked anyway.
	}
	return retValue;
    }

}
