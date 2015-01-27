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
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.lucene.index.IndexableField;
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

    String path = "./index_Martin_HPRD/lucene/node/uniprotidsearch";
    String path2 = "./index_Martin_HPRD/lucene/relationship/__rel_types__";

    public void readIndex() {
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(path)));

            IndexSearcher indexSearcher = new IndexSearcher(reader);
//            TreeMap<String,String> map= new TreeMap<>();
            PrintWriter pw = new PrintWriter("extractedIndex.txt", "UTF-8");
            Document doc;
            StringBuilder sB = null;
            for (int x = 0; x < reader.numDocs(); x++) {
                doc = reader.document(x);

                sB = new StringBuilder("");
                for (IndexableField f : doc.getFields()) {
                    sB.append(f.name());
                    sB.append(": ");
                    sB.append(f.stringValue());
                    sB.append(";");
                }
                pw.println(sB.toString());
            }
            pw.flush();
            int x = 0;

        } catch (IOException ex) {
            Logger.getLogger(LuceneHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
//        new LuceneHandler().formatDump("./dump.dat");
//        new LuceneHandler().createIndex();
//        new LuceneHandler().createTestSet();
        new LuceneHandler().testSpeed();
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

    public void testSpeed() {
        try {
            Scanner scan = new Scanner(Paths.get("./testSet.txt"));
            String[] values = scan.nextLine().split(";");
            List<String> test = new ArrayList<>((Arrays.asList(values)));
            long timeB = System.nanoTime();
            
            int threads=4;
            Thread[] threadA= new Thread[threads];
            for (int x = 0; x < threads; x++) {
                List<String> sublist=test.subList(x*test.size()/threads, (x+1)*test.size()/threads);
                threadA[x] = new Thread(() -> {
                    threadAction(sublist);
                });
                threadA[x].start();
            }
            for(Thread t:threadA){
                t.join();
            }
            long timeA = System.nanoTime();
            System.out.println((timeA - timeB) / (Math.pow(10, 6)));

        } catch (IOException | InterruptedException e) {
        }
    }

    public void threadAction(List<String> list) {
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("./UniProt_Index")));

            IndexSearcher indexSearcher = new IndexSearcher(reader);

            QueryParser parser = new QueryParser(Version.LUCENE_46, "uniprot", analyzer);
            List<String> result = new ArrayList<>();

            for (String s : list) {
                Query query = parser.parse(s);
                TopDocs docs = indexSearcher.search(query, 1);
                if (docs.totalHits == 0) {
                    continue;
                }
                result.add(indexSearcher.doc(docs.scoreDocs[0].doc).get("culsterNodeId"));

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
