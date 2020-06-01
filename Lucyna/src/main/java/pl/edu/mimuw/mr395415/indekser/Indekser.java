package pl.edu.mimuw.mr395415.indekser;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.document.Document;

import java.io.File;
import java.io.IOException;
import java.lang.*;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Indekser {
    static class IndexCLI {
        private IndexWriter indexWriter;
        private IndexBuilder indexBuilder;

        private String indexPath;

        private Analyzer polyglotAnalyzer() {
            Map<String,Analyzer> analyzerMap = new HashMap<>();
            analyzerMap.put("en", new EnglishAnalyzer());
            analyzerMap.put("pl", new PolishAnalyzer());
            PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(
                    new StandardAnalyzer(), analyzerMap);
            return wrapper;
        }

        void purgeIndex() {
            try {
                indexWriter.deleteAll();
                FileUtils.deleteDirectory(new File(indexPath));
            } catch (NoSuchFileException ignore) {}
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        void add(String dir) {
            indexBuilder.addDirectory(dir);
        }

        void rm(String dir) {
            indexBuilder.removeDirectory(dir);
        }

        private List<String> trackedDirs() {
            List<String> directories = new ArrayList<>();
            try (IndexReader reader = DirectoryReader.open(SimpleFSDirectory.open(Paths.get(indexPath)))) {
                IndexSearcher searcher = new IndexSearcher(reader);
                Query query = new WildcardQuery(new Term("directory", "*"));
                TopDocs search = searcher.search(query, Integer.MAX_VALUE);
                for (ScoreDoc score : search.scoreDocs) {
                    Document doc = searcher.doc(score.doc);
                    String dir = doc.get("directory");
                    directories.add(dir);
                }
            } catch (IndexNotFoundException ignore) {
            } catch (IOException e) {
                e.printStackTrace();
            }
            return directories;
        }

        void reindex() {
            List<String> directories = trackedDirs();
            for (String dir : directories) {
                rm(dir);
                add(dir);
            }
        }

        void list() {
            System.out.println("Tracked directories:");
            List<String> directories = trackedDirs();
            for (String dir : directories) {
                System.out.println(dir);
            }
        }

        void indexingLoop() throws IOException {

            // convert strings to Paths
            List<String> dirs = trackedDirs();
            List<Path> d = new ArrayList<>();
            for (String dir : dirs) {
                d.add(Paths.get(dir));
            }

            WatchDir watchDir = new WatchDir(d, true, this);
            watchDir.processEvents();
        }

        void removeFIle(String path) {
            indexBuilder.removeFile(path);
        }

        IndexCLI() throws IOException {
            indexPath = Paths.get(System.getProperty("user.home"), ".index").toAbsolutePath().toString();
            IndexWriterConfig config = new IndexWriterConfig(polyglotAnalyzer());
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(new SimpleFSDirectory(Paths.get(indexPath)), config);
            indexBuilder = new IndexBuilder(indexWriter);
        }

        void reindexFile(String path) {
            indexBuilder.reindexFile(path);
        }

        void close() {
            try {
                indexWriter.close();
            } catch (NoSuchFileException ignore) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            IndexCLI i = new IndexCLI();
            if (args.length == 0) {
                i.indexingLoop();
            } else {
                if (args[0].equals("--purge")) {
                    i.purgeIndex();
                } else if (args[0].equals("--add")) {
                    i.add(args[1]);
                } else if (args[0].equals("--rm")) {
                    i.rm(args[1]);
                } else if (args[0].equals("--reindex")) {
                    i.reindex();
                } else if (args[0].equals("--list")) {
                    i.list();
                }
            }
            i.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
