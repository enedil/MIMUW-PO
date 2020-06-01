package pl.edu.mimuw.mr395415.searcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Searcher {
    static class REPL implements AutoCloseable {
        private SearchEngine searchEngine;

        REPL() throws IOException {
            searchEngine = new SearchEngine();
        }
        void executeLine(String line) {
            try {
                if (line.startsWith("%")) {
                    String[] parts = line.split("\\s");
                    if (parts[0].equals("%limit")) {
                        searchEngine.setLimit(parts[1]);
                    } else if (parts[0].equals("%lang")) {
                        searchEngine.setLang(parts[1]);
                    } else if (parts[0].equals("%details")) {
                        searchEngine.setDetails(parts[1]);
                    } else if (parts[0].equals("%color")) {
                        searchEngine.setColor(parts[1]);
                    } else {
                        searchEngine.setMode(parts[0].substring(1));
                    }
                } else {
                    searchEngine.search(line);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Malformed line.");
            }
        }

        @Override
        public void close() throws Exception {
            searchEngine.close();
        }
    }

    public static void main(String[] args) {
        try (REPL repl = new REPL()) {
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            String line;
            try {
                while (true) {
                    System.out.print("> ");
                    line = br.readLine();
                    if (line == null || line.equals("\n")) {
                        break;
                    }
                    repl.executeLine(line.trim());
                }
            } catch (IOException ignore) {} // end of input
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}