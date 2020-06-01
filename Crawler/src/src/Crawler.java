import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

abstract public class Crawler {
    private Queue<String> queue;
    private Set<String> seenUrls;

    public Crawler() {
        queue = new ArrayDeque<>();
        seenUrls = new HashSet<>();
    }

    protected void enqueue(String url) {
        try {
            URI uri = new URI(url);
            if (!uri.isAbsolute()) {
                url = uri.normalize().toString();
            }
            if (!seenUrls.contains(url)) {
                queue.add(url);
                seenUrls.add(url);
            }
        } catch (URISyntaxException e) {
            // then I guess we do nothing
        }
    }

    private List<String> extractLinks(Document doc, String prefix) {
        Elements anchor = doc.select("a");
        List<Element> tags = new ArrayList<>();
        anchor.iterator().forEachRemaining(tags::add);
        return tags
                .stream()
                .map(tag -> tag.absUrl("href"))
                .filter(href -> !href.isEmpty())
                .collect(Collectors.toList());
    }

    private static Document parseAddress(String address) {
        Document ret = null;
        try {
            URI uri = new URI(address);
            if (uri.getScheme().equals("file")) {
                try (FileInputStream f = new FileInputStream(uri.getSchemeSpecificPart())) {
                    ret = Jsoup.parse(f, "utf-8", address);
                } catch (IOException e) {}
            } else {
                ret = Jsoup.connect(address).get();
            }
        } catch (Exception e) {}
        return ret;
    }

    public void run() {
        while (!queue.isEmpty()) {
            String url = queue.remove();
            Document doc;
            doc = parseAddress(url);
            if (doc == null) {
                continue;
            }
            List<String> links = extractLinks(doc, url);
            process(url, doc.outerHtml(), links);
        }
    }
    abstract void process(String url, String htmlSource, List<String> outgoingLinks);
    abstract public void display();
}
