package cn.bit.framework.utils.dataStructure.forkJoin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Administrator on 2017/11/14 0014.
 */
public class SubFileInfo implements Comparable<SubFileInfo> {

    private Integer currentNum;
    private BufferedReader br;
    private String fileName;

    public SubFileInfo() {
    }

    public SubFileInfo(String fileName) {
        this.fileName = fileName;
        try {
            this.br = new BufferedReader(new FileReader(fileName));
            this.readNext();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public Integer getCurrentNum() {
        return currentNum;
    }

    public void setCurrentNum(Integer currentNum) {
        this.currentNum = currentNum;
    }

    public BufferedReader getBr() {
        return br;
    }

    public void setBr(BufferedReader br) {
        this.br = br;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void readNext() throws IOException {
        String line = this.br.readLine();
        if (line != null) {
            this.currentNum = Integer.valueOf(line);
        } else {
            this.currentNum = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubFileInfo that = (SubFileInfo) o;

        return fileName.equals(that.fileName);

    }

    @Override
    public int hashCode() {
        return fileName.hashCode();
    }


    @Override
    public int compareTo(SubFileInfo o) {
        if (this.currentNum != o.currentNum)
            return this.currentNum - o.currentNum;
        return this.fileName.compareTo(o.fileName);
    }

}
