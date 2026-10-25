package Server.Util;

// Opens a page in the default browser (no java.desktop needed, so it works in the small packaged runtime)
public class Browser {

    public static void open(String url) {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("win")) new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            else if (os.contains("mac")) new ProcessBuilder("open", url).start();
            else new ProcessBuilder("xdg-open", url).start();
        } catch (Exception e) {
            System.out.println("Open this page in your browser: " + url);
        }
    }

}
