package pl.edu.mimuw.mr395415.searcher;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class SearchEngine implements AutoCloseable {
    private IndexSearcher indexSearcher;
    private IndexReader indexReader;

    private String lang;
    private boolean details;
    private int limit;
    private boolean color;
    private String mode;
    private Analyzer analyzer;

    private Analyzer polyglotAnalyzer() {
        Map<String,Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put("en", new EnglishAnalyzer());
        analyzerMap.put("pl", new PolishAnalyzer());
        PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(
                new StandardAnalyzer(), analyzerMap);
        return wrapper;
    }

    private void error(String s) {
        System.err.println(s);
    }

    void setLang(String lang) {
        if (lang.equals("en") || lang.equals("pl")) {
            this.lang = lang;
        } else {
            error("Language can be either 'en' or 'pl'");
        }
    }

    void setLimit(String limit) {
        int l = Integer.parseInt(limit);
        if (l == 0) {
            this.limit = Integer.MAX_VALUE;
        } else if (l > 0) {
            this.limit = l;
        } else {
            error("Limit needs to be nonnegative.");
        }
    }

    void setDetails(String details) {
        if (details.equals("on")) {
            this.details = true;
        } else if (details.equals("off")) {
            this.details = false;
        } else {
            error("Details can be either on of off.");
        }
    }

    void setColor(String color) {
        if (color.equals("on")) {
            this.color = true;
        } else if (color.equals("off")) {
            this.color = false;
        } else {
            error("Color can be either on of off.");
        }
    }

    void setMode(String mode) {
        if (mode.equals("term") || mode.equals("phrase") || mode.equals("fuzzy")) {
            this.mode = mode;
        } else {
            error("Wrong mode selected.");
        }
    }

    SearchEngine() throws IOException {
        String indexPath = Paths.get(System.getProperty("user.home"), ".index").toAbsolutePath().toString();

        indexReader = DirectoryReader.open(SimpleFSDirectory.open(Paths.get(indexPath)));
        indexSearcher = new IndexSearcher(indexReader);

        lang = "en";
        details = false;
        limit = Integer.MAX_VALUE;
        color = false;
        mode = "term";
        analyzer = polyglotAnalyzer();
    }

    private String getContext(Query query, ScoreDoc scoreDoc) throws IOException {
        Document doc = indexSearcher.doc(scoreDoc.doc);
        String text = doc.get(lang);
        SimpleHTMLFormatter htmlFormatter;
        if (color) {
            // red color with underline
            htmlFormatter = new SimpleHTMLFormatter("\033[31;1;4m", "\033[0m");
        } else {
            htmlFormatter = new SimpleHTMLFormatter("", "");
        }
        Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));

        TokenStream tokenStream = TokenSources.getAnyTokenStream(indexSearcher.getIndexReader(), scoreDoc.doc, lang, analyzer);

        try {
            TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, text, false, 20);
            List<String> l = new ArrayList<>();
            for (TextFragment f : frag) {
                l.add(f.toString());
            }
            return String.join(" ... ", l);
        } catch (InvalidTokenOffsetsException e) {
            return "";
        }

    }

    void search(String query) {
        Query q;
        if (mode.equals("term")) {
            q = new WildcardQuery(new Term(lang, query));
        } else if (mode.equals("phrase")) {
            q = new PhraseQuery(lang, query.split("\\s"));
        } else if (mode.equals("fuzzy")) {
            q = new FuzzyQuery(new Term(lang, query));
        } else {
            q = null; // shouldn't ever happen, as setMode checks for it
        }

        try {
            TopDocs search = indexSearcher.search(q, limit);
            System.out.println("File count: " + search.scoreDocs.length + "\n");

            for (ScoreDoc score : search.scoreDocs) {
                Document doc = indexSearcher.doc(score.doc);
                System.out.println(doc.get("path"));
                if (details) {
                    String context = getContext(q, score);
                    System.out.println(context);
                }
            }
        } catch (IOException e) {
            error("Cannot read index.");
        }

    }


    @Override
    public void close() throws Exception {
        indexReader.close();
    }
}