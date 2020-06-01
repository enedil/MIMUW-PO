import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

class StatisticCrawler extends Crawler {
    StatisticCrawler(String url) {
        super();
        counter = new HashMap<>();
        enqueue(url);
    }

    private static String getHostname(String address) {
        try {
            URI uri = new URI(address);
            if (uri.getHost() == null) {
                return "";
            }
            return uri.getHost();
        } catch (URISyntaxException e) {
            return "";
        }
    }

    private Map<String, Integer> counter;

    @Override
    void process(String url, String htmlSource, List<String> outgoingLinks) {
        String inputHostname = getHostname(url);
        if (inputHostname.isEmpty()) {
            return;
        }
        for (String link : outgoingLinks) {
            String hostname = getHostname(link);
            if (!hostname.isEmpty()) {
                if (hostname.equals(inputHostname)) {
                    enqueue(link);
                } else {
                    Integer count = counter.getOrDefault(hostname, 0);
                    counter.put(hostname, count + 1);
                }
            }
        }
    }

    private List<Map.Entry<String, Integer>> sortedStatistics() {
        List<Map.Entry<String, Integer>> ret = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counter.entrySet()) {
            ret.add(e);
        }

        Collections.sort(ret, Comparator.comparing(p -> -p.getValue()));
        return ret;
    }

    @Override
    public void display() {
        System.out.println("Statystyki:");
        List<Map.Entry<String, Integer>> sorted = sortedStatistics();
        for (Map.Entry<String, Integer> e : sorted) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }
}

public class Demo1 {
    public static void main(String[] args) {
        if (args.length != 1) {
            help();
        }
        StatisticCrawler sc = new StatisticCrawler(args[0]);
        sc.run();
        sc.display();
    }

    public static void help() {
        System.err.println("Usage:  java Demo1 url");
        System.exit(1);
    }
}
