package com.segment;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorpusPreProcess {

    private String filePath;
    private String outFilePath;
    private File fin;
    private File fout;

    public CorpusPreProcess() {
        filePath = "./corpusraw";
        fin = new File(filePath);
        if (!fin.exists()) {
            fin.mkdirs();
        }
        outFilePath = "./corpus";
        fout = new File(outFilePath);
        if (!fout.exists()) {
            fout.mkdirs();
        }
    }

    public CorpusPreProcess(String filePath, String outFilePath) {
        this.filePath = filePath;
        this.outFilePath = outFilePath;
        fin = new File(filePath);
        if (!fin.exists()) {
            fin.mkdirs();
        }
        fout = new File(outFilePath);
        if (!fout.exists()) {
            fout.mkdirs();
        }
    }

    public void cleanData() throws IOException {
        if (fin.isDirectory()) {
            File[] files = fin.listFiles();
            // cleanOneFile(files[0], outFilePath, 0);

            for (int i = 0; i < files.length; ++i) {
                cleanOneFile(files[i], outFilePath, i);
            }
        }
    }

    @SuppressWarnings("Since15")
    private void cleanOneFile(File f, String targetPath, int index) throws IOException {
        if (f.getName().endsWith("txt")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetPath+"/corpus-"+index+".txt"), "UTF-8"));
            while(true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                String[] words = cleanSentence(line).split("\\s+");
                ArrayList<String> wordsList = new ArrayList<String>(Arrays.asList(words));
                wordsList.remove(0);
                String newLine = String.join("", wordsList);
                // System.out.println(newLine);
                // int strLength = newLine.length();
                // System.out.println(strLength);
                String strLabels = "";
                for(String w: wordsList) {
                    int wLength = w.length();
                    if (wLength <= 4) {
                        for (int j = 0; j < wLength; j++) {
                            strLabels += (char)('b' + j);
                        }
                    }
                    else {
                        int j = 0;
                        for (; j < 4; j++) {
                            strLabels += (char)('b' + j);
                        }
                        for (; j < wLength; j++) {
                            strLabels += 'e';
                        }
                    }
                }
                // System.out.println(strLabels.length());
                // System.out.println(strLabels);
                ArrayList<String> wordAndLabel = new ArrayList<String>();
                for (int i = 0 ; i < strLabels.length(); i++) {
                    wordAndLabel.add((newLine.charAt(i) + "/" + strLabels.charAt(i)));
                }
                String writeLine = String.join(" ", wordAndLabel);
                // System.out.println(newLine);
                bw.write(writeLine);
                bw.write('\n');
            }
            br.close();
            bw.close();
        }
        else if (f.getName().endsWith("utf8")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetPath+"/"+f.getName()), "UTF-8"));
            while(true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                String[] words = line.split("\\s+");
                ArrayList<String> wordsList = new ArrayList<String>(Arrays.asList(words));
                String newLine = String.join("", wordsList);
                String strLabels = "";
                for(String w: wordsList) {
                    int wLength = w.length();
                    if (wLength <= 4) {
                        for (int j = 0; j < wLength; j++) {
                            strLabels += (char)('b' + j);
                        }
                    }
                    else {
                        int j = 0;
                        for (; j < 4; j++) {
                            strLabels += (char)('b' + j);
                        }
                        for (; j < wLength; j++) {
                            strLabels += 'e';
                        }
                    }
                }
                // System.out.println(strLabels.length());
                // System.out.println(strLabels);
                ArrayList<String> wordAndLabel = new ArrayList<String>();
                for (int i = 0 ; i < strLabels.length(); i++) {
                    wordAndLabel.add((newLine.charAt(i) + "/" + strLabels.charAt(i)));
                }
                String writeLine = String.join(" ", wordAndLabel);
                // System.out.println(newLine);
                bw.write(writeLine);
                bw.write('\n');
            }
            br.close();
            bw.close();

        }
    }

    private String cleanSentence(String input) {
        String output = input.replaceAll(" \\[", "");
        output = output.replaceAll("\\] ", "");
        output = output.replaceAll("/[a-zA-Z]*", "");
        return output;
    }

}
