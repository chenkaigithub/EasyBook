package engine;

import bean.Book;
import bean.Catalog;

import java.io.IOException;
import java.util.List;

public abstract class BaseSite {

    private int threadCount = 300;
    private String encodeType = "gbk";
    private String siteName;
    private long repeatTime = 30;

    public BaseSite() {
        this.siteName = setSiteName();
    }

    public abstract String setSiteName();

    public void setRepeatTime(long repeatTime) {
        this.repeatTime = repeatTime;
    }

    public long getRepeatTime() {
        return repeatTime;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setEncodeType(String encodeType) {
        this.encodeType = encodeType;
    }

    public String getEncodeType() {
        return encodeType;
    }

    public abstract List<Book> search(String bookName) throws IOException;

    public abstract List<Catalog> parseCatalog(String catalogHtml, String url);

    public abstract List<String> parseContent(String chapterHtml);

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public int getThreadCount() {
        return threadCount;
    }
}
