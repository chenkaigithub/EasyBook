package bean;

import engine.BaseSite;
import org.jetbrains.annotations.NotNull;

/**
 * Created By zia on 2018/10/30.
 */
public class Book {
    private String bookName;
    private String author = "未知";
    private String url;
    private String chapterSize = "未知";
    private String lastUpdateTime = "未知";
    private String lastChapterName = "未知";
    private BaseSite site;

    public Book(String bookName, String author, String url, String chapterSize, String lastUpdateTime, String lastChapterName, BaseSite site) {
        this.bookName = bookName;
        this.author = author;
        this.url = url;
        this.chapterSize = chapterSize;
        this.lastUpdateTime = lastUpdateTime;
        this.lastChapterName = lastChapterName;
        this.site = site;
    }

    public BaseSite getSite() {
        return site;
    }

    public void setSite(BaseSite site) {
        this.site = site;
    }

    @Override
    public String toString() {
        return "Book{" +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", url='" + url + '\'' +
                ", chapterSize='" + chapterSize + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", lastChapterName='" + lastChapterName + '\'' +
                ", site=" + site +
                '}';
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChapterSize() {
        return chapterSize;
    }

    public void setChapterSize(String chapterSize) {
        this.chapterSize = chapterSize;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLastChapterName() {
        return lastChapterName;
    }

    public void setLastChapterName(String lastChapterName) {
        this.lastChapterName = lastChapterName;
    }
}
