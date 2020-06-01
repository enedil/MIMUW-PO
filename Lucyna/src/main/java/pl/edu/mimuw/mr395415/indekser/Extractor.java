package pl.edu.mimuw.mr395415.indekser;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.lucene.document.Document;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Extractor {
    private String text;
    private String path;
    private String lang;
    private IndexWriter indexWriter;
    private OptimaizeLangDetector detector;

    public Extractor(String path, IndexWriter indexWriter, OptimaizeLangDetector detector) throws IOException, TikaException {
        File file = new File(path);
        Tika tika = new Tika();
        this.text = tika.parseToString(file);
        this.path = path;

        this.detector = detector;
        detector.reset();
        detector.addText(text);
        LanguageResult result = detector.detect();
        this.lang = result.getLanguage();
        this.indexWriter = indexWriter;
    }

    public void getDocument() {
        if (!lang.equals("pl") && !lang.equals("en")) {
            return;
        }
        Document ret = new Document();
        Field pathField = new StringField("path", path, Field.Store.YES);
        ret.add(pathField);
        Field content = new TextField(lang, text, Field.Store.YES);
        ret.add(content);
        Field fileName = new StringField("file", Path.of(path).getFileName().toString(), Field.Store.YES);
        ret.add(fileName);

        try {
            indexWriter.addDocument(ret);
        } catch (IOException e) {
            return;
        }
    }
}
