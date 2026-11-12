package it.webshop;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Indexer {

     public static void main(String[] args) {

          // Directory contenente i file originali
          Path documentsPath = Paths.get("ordini");
          // Directory in cui Lucene salverà l'indice
          Path indexPath = Paths.get("indice");

          try {
               // 1. Definizione degli analyzer
               Analyzer filenameAnalyzer = new StandardAnalyzer();
               Analyzer contentAnalyzer = new ItalianAnalyzer();

               // 2. Associazione analyzer -> campo
               Map<String, Analyzer> analyzerMap = new HashMap<>();
               analyzerMap.put("filename", filenameAnalyzer);
               analyzerMap.put("content", contentAnalyzer);

               Analyzer analyzer = new PerFieldAnalyzerWrapper(
                         contentAnalyzer, // fallback
                         analyzerMap);

               // 3. Configurazione dell'IndexWriter
               Directory directory = FSDirectory.open(indexPath);
               IndexWriterConfig config = new IndexWriterConfig(analyzer);
               config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
               IndexWriter writer = new IndexWriter(directory, config);

               // 4. Scansione ricorsiva della directory
               try (Stream<Path> files = Files.walk(documentsPath)) {
                    files.filter(Files::isRegularFile).filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                              .forEach(path -> {
                                   try {
                                        String filename = path.getFileName().toString();
                                        String content = Files.readString(path);
                                        String filePath = path.toAbsolutePath().toString();

                                        // 5. Creazione del documento Lucene
                                        Document doc = new Document();
                                        // Campo filename: indicizzato e memorizzato
                                        doc.add(new TextField(
                                                  "filename",
                                                  filename, Field.Store.YES));
                                        // Campo content: indicizzato e memorizzato
                                        doc.add(new TextField(
                                                  "content",
                                                  content, Field.Store.YES));
                                        // Identificatore del file, non analizzato
                                        doc.add(new StringField(
                                                  "path",
                                                  filePath, Field.Store.YES));

                                        // 6. Inserimento nell'indice (aggiornamento in caso di duplicati)
                                        writer.updateDocument(new Term("path", filePath), doc);
                                        System.out.println("Indicizzato: " + filename);
                                   } catch (IOException e) {
                                        System.err.println("Errore nella lettura: " + path);
                                        e.printStackTrace();
                                   }
                              });
               }
               // 8. Chiusura e salvataggio dell'indice
               writer.close();
               directory.close();
               analyzer.close();
               filenameAnalyzer.close();
               contentAnalyzer.close();
               System.out.println("\nIndicizzazione completata!");
          } catch (IOException e) {
               e.printStackTrace();
          }
     }
}
