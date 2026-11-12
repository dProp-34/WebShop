package it.webshop;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.Scanner;

public class Searcher {

     public static void main(String[] args) {

          try {
               // Directory dell'indice
               Directory directory = FSDirectory.open(
                         Paths.get("indice_lucene"));
               // Apertura dell'indice
               DirectoryReader reader = DirectoryReader.open(directory);
               // Creazione del motore di ricerca
               IndexSearcher searcher = new IndexSearcher(reader);

               // Analyzer
               Analyzer filenameAnalyzer = new StandardAnalyzer();
               Analyzer contentAnalyzer = new ItalianAnalyzer();

               Scanner scanner = new Scanner(System.in);
               boolean continua = true;

               while (continua) {
                    System.out.println("\n=== MOTORE DI RICERCA ===");
                    System.out.println("1. Cerca nel nome dei file");
                    System.out.println("2. Cerca nel contenuto");
                    System.out.println("0. Esci");
                    System.out.print("\nScelta: ");

                    int scelta = scanner.nextInt();
                    scanner.nextLine();
                    if (scelta == 0) {
                         continua = false;
                         continue;
                    }
                    String field;
                    Analyzer analyzer;

                    if (scelta == 1) {
                         field = "filename";
                         analyzer = filenameAnalyzer;
                    } else if (scelta == 2) {
                         field = "content";
                         analyzer = contentAnalyzer;
                    } else {
                         System.out.println("Scelta non valida!");
                         continue;
                    }

                    System.out.print("Inserisci il termine da cercare: ");
                    String text = scanner.nextLine();

                    // Creazione del parser
                    QueryParser parser = new QueryParser(field, analyzer);
                    Query query = parser.parse(text);

                    // Ricerca dei primi 10 risultati
                    TopDocs results = searcher.search(query, 10);
                    System.out.println("\nRisultati trovati: " + results.totalHits.value());

                    for (ScoreDoc scoreDoc : results.scoreDocs) {
                         Document doc = searcher.storedFields().document(scoreDoc.doc);

                         System.out.println("\n--------------------");
                         System.out.println("Nome: " + doc.get("filename"));
                         System.out.println("Percorso: " + doc.get("path"));
                         System.out.println("Punteggio: " + scoreDoc.score);
                    }
               }
               scanner.close();
               filenameAnalyzer.close();
               contentAnalyzer.close();
               reader.close();
               directory.close();
          } catch (Exception e) {
               e.printStackTrace();
          }
     }
}
