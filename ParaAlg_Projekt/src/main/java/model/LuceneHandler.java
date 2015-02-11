/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Tim Pontzen
 */
public class LuceneHandler {

    public static void main(String[] args) {
//        new LuceneHandler().formatDump("./dump.dat");
//        new LuceneHandler().createIndex();
//        new LuceneHandler().createTestSet(20000);
        new ConnectionHandler(5000);
    }

    /**
     * creates a random test set of uniProtIds.
     *
     * @param size number of ids in the set
     */
    public void createTestSet(int size) {
        try {
            Scanner scan = new Scanner(Paths.get("./formatedDump.txt"));

            ArrayList<String> list = new ArrayList<>();
            while (scan.hasNext()) {
                list.add(scan.nextLine().split(";")[0]);
            }
            PrintWriter pw = new PrintWriter("testSet.txt", "UTF-8");

            for (int x = 0; x < size; x++) {
                pw.print(list.get((int) (Math.random() * list.size())) + ";");
            }
            pw.close();
        } catch (IOException ex) {
        }

    }
    
    /**
     * method to process the Data sent from the other application
     * @param input HashMap containing the uniProtid,Value 
     * @return List of Pairs with clusterids and values
     */
    public static List<Pair<String, Double>> processInput(HashMap<String, Double>[] input) {
        List<Pair<String, Double>> results = new ArrayList<>();
        try {
            long timeB = System.nanoTime();

            List<Thread> threadA = new ArrayList<>();
            for (HashMap<String, Double> map : input) {
                threadA.add(new Thread(() -> {
                    threadAction(map, results);
                }));
                threadA.get(threadA.size()).start();
            }
            for (Thread t : threadA) {
                t.join();
            }
            long timeA = System.nanoTime();
            System.out.println((timeA - timeB) / (Math.pow(10, 6)));

        } catch (InterruptedException e) {
        }
        return results;
    }

    /**
     * subtasks for the threads.
     *
     * @param map Data to read and search for.
     * @param results
     */
    public static void threadAction(HashMap<String, Double> map, List<Pair<String, Double>> results) {
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("./UniProt_Index")));

            IndexSearcher indexSearcher = new IndexSearcher(reader);

            QueryParser parser = new QueryParser(Version.LUCENE_46, "uniprot", analyzer);

            for (Map.Entry<String, Double> s : map.entrySet()) {
                Query query = parser.parse(s.getKey());
                TopDocs docs = indexSearcher.search(query, 1);
                if (docs.totalHits == 0) {
                    continue;
                }
                results.add(new Pair<>(indexSearcher.doc(docs.scoreDocs[0].doc).get("culsterNodeId"),
                        s.getValue()));

            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(LuceneHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void formatDump(String pathToFile) {
        try {
            Scanner scan = new Scanner(Paths.get(pathToFile));
            TreeMap<String, List<String>> map = new TreeMap<>();
            String[] tmp;
            while (scan.hasNext()) {
                tmp = scan.nextLine().split(";");
                if (tmp.length < 2) {
                    continue;
                }
                if (map.containsKey(tmp[0])) {
                    List<String> list = map.get(tmp[0]);
                    //add clusterids to list
                    for (int x = 1; x < tmp.length; x++) {
                        list.add(tmp[x]);
                    }
                } else {
                    List<String> list = new ArrayList<>();
                    map.put(tmp[0], list);
                    //add clusterids to list
                    for (int x = 1; x < tmp.length; x++) {
                        list.add(tmp[x]);
                    }
                }
            }

            //test
            PrintWriter pw = new PrintWriter("formatedDump.txt", "UTF-8");
            TreeMap<String, String> tmpMap = new TreeMap<>();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                for (String s : entry.getValue()) {
                    if (tmpMap.containsKey(s)) {
                        tmpMap.put(s, tmpMap.get(s) + ":" + entry.getKey());
                    } else {
                        tmpMap.put(s, entry.getKey());
                    }
                }
            }
            for (Map.Entry<String, String> entry : tmpMap.entrySet()) {
                pw.print(entry.getKey() + ";");
                pw.println(entry.getValue());
            }
            pw.close();

        } catch (IOException ex) {
            Logger.getLogger(LuceneHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createIndex() {
        try {
            Scanner scan = new Scanner(Paths.get("./formatedDump.txt"));
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
            Directory dir = FSDirectory.open(new File("UniProt_Index"));
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, analyzer);
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(dir, conf);
            String[] line;
            Document doc;

            while (scan.hasNextLine()) {
                doc = new Document();
                line = scan.nextLine().split(";");
                doc.add(new Field("uniprot", line[0], TextField.TYPE_STORED));
                doc.add(new Field("clusterNodeId", line[1], TextField.TYPE_STORED));
                writer.addDocument(doc);

            }
            writer.prepareCommit();
            writer.commit();
            writer.close();
            scan.close();

        } catch (IOException ex) {
        }
    }

}
