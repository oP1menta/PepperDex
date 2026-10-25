package Server.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Puts this computer on the internet through a free Cloudflare "quick tunnel" (no account): the players open the
// https://<random>.trycloudflare.com link from any network. The tool (cloudflared) is looked for in ./tools, then in the
// PATH, and downloaded into ./tools the first time. The link is new on every run (that is how quick tunnels work).
public class TunnelServices {

    // "api.trycloudflare.com" also shows up in cloudflared's log, but it is not our link
    private static final Pattern LINK = Pattern.compile("https://(?!api\\.)[a-z0-9-]+\\.trycloudflare\\.com");
    private static final String DOWNLOAD = "https://github.com/cloudflare/cloudflared/releases/latest/download/";

    private int port;
    private volatile String status = "off";   // off | download | starting | ready | error
    private volatile String url = "";
    private volatile String message = "";
    private Process process;

    public TunnelServices(int port) {
        setPort(port);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    //============GETTERS=============

    public int getPort() {
        return port;
    }

    public String getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public String getMessage() {
        return message;
    }

    //============SETTERS=============

    public void setPort(int port) {
        this.port = port;
    }

    //============RUN=============

    public boolean isReady() {
        return "ready".equals(status);
    }

    // Starts in the background (does nothing if it is already starting or running)
    public synchronized void start() {
        if (status.equals("download") || status.equals("starting") || status.equals("ready")) return;
        status = "starting"; url = ""; message = "";
        Thread thread = new Thread(this::run, "tunnel");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stop() {
        if (process != null) process.destroy();
        process = null;
        status = "off"; url = "";
    }

    private void run() {
        Deque<String> lastLines = new ArrayDeque<>();
        try {
            String tool = findTool();
            status = "starting";
            Process p = new ProcessBuilder(tool, "tunnel", "--url", "http://localhost:" + port, "--no-autoupdate").redirectErrorStream(true).start();
            synchronized (this) { process = p; }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    lastLines.addLast(line);
                    if (lastLines.size() > 6) lastLines.removeFirst();
                    Matcher m = LINK.matcher(line);
                    if (url.isEmpty() && m.find()) { url = m.group(); status = "ready"; System.out.println("Public link: " + url); }
                }
            }
            p.waitFor();
            if (!status.equals("off")) fail("The tunnel stopped. " + String.join(" | ", lastLines));
        } catch (Exception e) {
            fail(e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    private void fail(String text) {
        status = "error"; url = ""; message = text;
        System.out.println("Tunnel: " + text);
    }

    //============THE TOOL (cloudflared)=============

    private String findTool() throws IOException, InterruptedException {
        String os = System.getProperty("os.name", "").toLowerCase();
        boolean windows = os.contains("win");
        String file = windows ? "cloudflared.exe" : "cloudflared";

        Path local = Path.of("tools", file);
        if (Files.isRegularFile(local)) return local.toAbsolutePath().toString();
        for (String dir : System.getenv().getOrDefault("PATH", "").split(java.io.File.pathSeparator)) {
            if (!dir.isEmpty() && Files.isRegularFile(Path.of(dir, file))) return Path.of(dir, file).toString();
        }

        String arch = System.getProperty("os.arch", "").toLowerCase();
        String asset;
        if (windows) asset = "cloudflared-windows-amd64.exe";
        else if (os.contains("linux")) asset = arch.contains("aarch64") || arch.contains("arm") ? "cloudflared-linux-arm64" : "cloudflared-linux-amd64";
        else throw new IOException("Install cloudflared by hand and put it in the tools folder (this system has no automatic download).");

        status = "download";
        Files.createDirectories(local.getParent());
        Path part = Path.of("tools", file + ".part");
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).connectTimeout(Duration.ofSeconds(20)).build();
        HttpResponse<Path> response = client.send(HttpRequest.newBuilder(URI.create(DOWNLOAD + asset)).timeout(Duration.ofMinutes(5)).build(),
            HttpResponse.BodyHandlers.ofFile(part));
        if (response.statusCode() != 200) { Files.deleteIfExists(part); throw new IOException("Could not download cloudflared (HTTP " + response.statusCode() + ")."); }
        Files.move(part, local, StandardCopyOption.REPLACE_EXISTING);
        local.toFile().setExecutable(true);
        return local.toAbsolutePath().toString();
    }

    //============JSON=============

    public String toJson(String lanUrl) {
        return "{\"status\":\"" + status + "\",\"url\":\"" + url + "\",\"lan\":\"" + lanUrl + "\",\"message\":\"" + message.replace("\\", "\\\\").replace("\"", "'").replaceAll("[\\r\\n]+", " ") + "\"}";
    }

}
