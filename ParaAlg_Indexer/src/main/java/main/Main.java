/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Tenuriel
 */
public class Main {
    public JButton format;
    public JButton create;
    public JTextField input;
    
    public Main() {
        
        JFrame frame= new JFrame("Index Creator");
        frame.setLayout(new BorderLayout());
        
        input= new JTextField();
        frame.add(input,BorderLayout.NORTH);
        
        ButtonLis lis= new ButtonLis();
        format= new JButton("Format Dump");
        format.addActionListener(lis);
        frame.add(format,BorderLayout.WEST);
        
        create= new JButton("Create Index");
        create.addActionListener(lis);
        frame.add(create,BorderLayout.EAST);
        
        frame.setResizable(false);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
        
    }

    public static void main(String[] args) {
        new Main();
    }

    /**
     * formats a file of type 
     * clusterid;uniprot1;...;uniprotN
     * so it can be processed by lucene.
     * @param pathToFile relative path of file from jar location
     */
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
                List<String> list;
                if (map.containsKey(tmp[0])) {
                    list = map.get(tmp[0]);
                } else {
                    list = new ArrayList<>();
                    map.put(tmp[0], list);
                }
                //add clusterids to list
                for (int x = 1; x < tmp.length; x++) {
                    list.add(tmp[x]);
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

        }
    }
    
    /**
     * uses the prior created formatedDump.txt to create an index.
     */
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
    class ButtonLis implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            if(ae.getSource()==create){
                
            }else if(ae.getSource()==format){
                
            }
        }
        
    }

}
