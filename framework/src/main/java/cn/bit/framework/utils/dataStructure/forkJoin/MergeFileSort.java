package cn.bit.framework.utils.dataStructure.forkJoin;

import java.io.*;
import java.util.*;

/**
 * Created by Administrator on 2017/11/14 0014.
 */
public class MergeFileSort {
    private static final int MAX_SIZE = 500000;
    private static final String SPLIT_PATH = "d:\\split_files";

    public void mSort(File file) throws IOException {
        split(file);
        merge();
    }

    private void split(File file) throws IOException {
        SortedSet<Integer> sortedSet = new TreeSet<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        int count = 0, fileNum = 0;
        do {
            line = br.readLine();
            if (line != null) {
                sortedSet.add(Integer.valueOf(line));
                count++;
                if (count >= MAX_SIZE) {
                    writeSubFile(fileNum, sortedSet);
                    count = 0;
                    sortedSet.clear();
                    fileNum++;
                }
            } else if (!sortedSet.isEmpty()) {
                writeSubFile(fileNum, sortedSet);
                count = 0;
                sortedSet.clear();
                fileNum++;
            }
        } while (line != null);

        if (br != null) {
            br.close();
            br = null;
        }
    }

    private void merge() throws IOException {
        File dir = new File(SPLIT_PATH);
        File[] files = dir.listFiles();
        List<SubFileInfo> fileInfoList = new LinkedList<>();
        BufferedWriter bw = new BufferedWriter(new FileWriter("d:\\mergeResult.txt"));
        for (File f : files) {
            SubFileInfo sf = new SubFileInfo(f.getAbsolutePath());
            fileInfoList.add(sf);
        }
        Collections.sort(fileInfoList);
        while (!fileInfoList.isEmpty()) {
            SubFileInfo sf = fileInfoList.get(0);
            Integer value = sf.getCurrentNum();

            bw.write(value + "\r\n");
            sf.readNext();

            if (sf.getCurrentNum() == null) {
                fileInfoList.remove(sf);
            }
            Collections.sort(fileInfoList);
        }
        bw.flush();
        if (bw!=null){
            bw.close();
            bw=null;
        }
    }


    private void writeSubFile(int fileNum, SortedSet<Integer> set) throws IOException {
        File dir = new File(SPLIT_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(SPLIT_PATH + File.separator + "s_" + fileNum + ".txt"));
        for (Integer v : set) {
            bw.write(v + "\r\n");
        }
        bw.flush();
        if (bw != null) {
            bw.close();
            bw = null;
        }
    }

    public static void main(String[] args) throws IOException {
        /*BufferedWriter bw = new BufferedWriter(new FileWriter("d:\\big_data.txt"));
        Random r = new Random();
        for (int i = 0; i < 1000000003; i++) {
            bw.write(r.nextInt(100000000) + "\r\n");
        }
        bw.flush();
        if (bw != null) {
            bw.close();
            bw = null;
        }*/

        MergeFileSort mfs = new MergeFileSort();
        mfs.mSort(new File("d:\\big_data.txt"));
    }
}
