package pl.edu.mimuw.mr395415.indekser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.OptimaizeLangDetector;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

class IndexBuilder {
    private IndexWriter indexWriter;
    private OptimaizeLangDetector detector;

    IndexBuilder(IndexWriter writer) throws IOException {
        indexWriter = writer;
        detector = new OptimaizeLangDetector();
        Set<String> langs = new HashSet<>();
        langs.add("pl");
        langs.add("en");
        detector.loadModels(langs);
    }

    private void addFile(Path path) {
        Path p = path.toAbsolutePath();
        try {
            Extractor e = new Extractor(p.toString(), indexWriter, detector);
            e.getDocument();
        }
        // we couldn't process file, so it won't be added
        catch (IOException | TikaException e) {
            e.printStackTrace();
        }
    }

    void addDirectory(String dir) {
        Path path = Path.of(dir);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile()) {
                        addFile(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            Document d = new Document();
            d.add(new TextField("directory", dir, Field.Store.YES));
            indexWriter.addDocument(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void removeDirectory(String dir) {
        try {
            Term term = new Term("path", dir);
            Query query = new PrefixQuery(term);
            indexWriter.deleteDocuments(query);

            term = new Term("directory", dir);
            query = new TermQuery(term);
            indexWriter.deleteDocuments(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void removeFile(String path) {
        try {
            Term term = new Term("path", path);
            Query query = new PrefixQuery(term);
            indexWriter.deleteDocuments(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void reindexFile(String path) {
        removeFile(path);
        addFile(Paths.get(path));
    }
}
