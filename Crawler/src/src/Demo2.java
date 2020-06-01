import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LocalFilesCounter extends Crawler {
    private Map<String, Integer> visitedDepth;
    private int maxDepth;

    LocalFilesCounter(String file, int maxDepth) throws IOException, InvalidParameterException {
        super();
        if (maxDepth < 0) {
            throw new InvalidParameterException();
        }
        Path path = Path.of(file);
        file = path.toAbsolutePath().normalize().toString();
        if (!Files.isReadable(Paths.get(file))) {
            throw new IOException();
        }
        this.maxDepth = maxDepth;
        enqueue("file:" + file);
        visitedDepth = new HashMap<>();
        visitedDepth.put(file, 0);
    }

    private static String removeScheme(String address) {
        try {
            URI uri = new URI(address).normalize();
            return uri.getSchemeSpecificPart();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    void process(String url, String htmlSource, List<String> outgoingLinks) {
        int depth = visitedDepth.getOrDefault(removeScheme(url), maxDepth+1);
        if (depth >= maxDepth) {
            // We don't want to enqueue further websites from now on.
            return;
        }
        for (String link : outgoingLinks) {
            try {
                URL base = new URL(url);
                URL full = new URL(base, link);
                String schemeSpecificPart = removeScheme(full.toString());
                if (full.getProtocol().equals("file") && !visitedDepth.containsKey(schemeSpecificPart)) {
                    enqueue(full.toString());
                    visitedDepth.put(schemeSpecificPart, depth + 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void display() {
        System.out.println("Total number of files: " + visitedDepth.size());
        if (visitedDepth.size() < 20) {
            System.out.println("Since the number of files is considerably small, here are they:");
            System.out.println(visitedDepth);
        }
    }
}


public class Demo2 {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage:    java Demo2 htmlFile maxDepth");
            System.exit(1);
        }
        String url = args[0];
        Integer maxDepth = Integer.parseInt(args[1]);
        try {
            LocalFilesCounter lfc = new LocalFilesCounter(url, maxDepth);
            lfc.run();
            lfc.display();
        } catch (IOException e) {
            System.err.println("Error: File is not readable.");
        } catch (InvalidParameterException e) {
            System.err.println("Error: maxDepth must be nonnegative");
        }
    }
}
