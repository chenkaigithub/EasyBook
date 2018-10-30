package engine;

import bean.Book;
import bean.Chapter;
import listener.EventListener;
import util.SiteUtil;
import util.TextUtil;
import util.conventer.FoxEpubWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By zia on 2018/10/30.
 */
public class Downloader {


    private Type type;
    private EventListener eventListener;
    private String path = ".";

    public Downloader(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void download(String bookName) {
        download(bookName, Type.EPUB);
    }

    public void download(String bookName, Type type) {
        download(bookName, SiteUtil.getAllSites(), type);
    }

    public void download(String bookName, List<BaseSite> sites, Type type) {
        if (path == null) {
            eventListener.onError("请输入文件路径", new FileNotFoundException());
            return;
        }
        eventListener.pushMessage("开始搜索书籍");

        int resultSize = 0;
        List<List<Book>> bookListList = new ArrayList<>();
        for (BaseSite site : sites) {
            List<Book> results = null;
            try {
                results = site.search(bookName);
                resultSize += results.size();
            } catch (IOException e) {
                eventListener.pushMessage(e.getMessage());
            }
            if (results == null) {
                eventListener.pushMessage(site.getSiteName() + "搜索结果加载错误");
                continue;
            }
            bookListList.add(results);
        }

        //混合插入，每一个站点的
        List<Book> bookList = new ArrayList<>();
        int index = 0;
        while (bookList.size() < resultSize) {
            for (List<Book> bl : bookListList) {
                if (index < bl.size()) {
                    bookList.add(bl.get(index));
                }
            }
            index++;
        }
        //按长度微调排序
        bookList.sort((o1, o2) -> {
            if (o1.getBookName().length() == bookName.length()
                    && o2.getBookName().length() != bookName.length()) {
                return -1;
            } else if (o1.getBookName().length() != bookName.length()
                    && o2.getBookName().length() == bookName.length()) {
                return 1;
            }
            return 0;
        });

        eventListener.pushMessage("搜索到" + bookList.size() + "本相关书籍");

        //选择要下载的书籍
        Book book = eventListener.onChooseBook(bookList);

        BookFucker bookFucker = new BookFucker(book, eventListener);
        List<Chapter> chapters;
        try {
            chapters = bookFucker.download();
        } catch (IOException e) {
            eventListener.onError("目录解析失败", e);
            return;
        } catch (InterruptedException e) {
            eventListener.onError("并发错误", e);
            return;
        }

        String bkName = book.getBookName() + "-" + book.getSite().getSiteName();
        try {
            save(chapters, bkName, type);
        } catch (Exception e) {
            eventListener.onError("文件保存失败", e);
        }

        eventListener.onEnd("保存成功，路径：" + path + File.separator + bkName);
    }

    private void save(List<Chapter> chapters, String bookName, Type type) throws IOException {
        if (type == Type.EPUB) {
            String name = bookName + ".epub";
            File file = new File(path + File.separator + name);
            FoxEpubWriter foxEpubWriter = new FoxEpubWriter(file, bookName);
            for (Chapter chapter : chapters) {
                StringBuilder content = new StringBuilder();
                for (String line : chapter.getContents()) {
                    line = TextUtil.cleanContent(line);
                    content.append("<p>");
                    content.append("    ");
                    content.append(line);
                    content.append("</p>");
                }
                foxEpubWriter.addChapter(chapter.getChapterName(), content.toString());
            }
            foxEpubWriter.saveAll();
        } else {
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(path + File.separator + bookName)));
            for (Chapter chapter : chapters) {
                bufferedWriter.write(chapter.getChapterName());
                bufferedWriter.write("\n\n");
                for (String line : chapter.getContents()) {
                    line = TextUtil.cleanContent(line);
                    //4个空格+正文+换行+空行
                    bufferedWriter.write("    ");
                    bufferedWriter.write(line);
                    bufferedWriter.write("\n\n");
                }
                //章节结束空三行，用来分割下一章节
                bufferedWriter.write("\n\n\n");
            }
        }
    }

    public void setSavePath(String path) {
        this.path = path;
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                eventListener.onError("文件路径创建失败", new Exception("文件路径创建失败"));
            }
        }
    }

    public void setSaveResult(Type type) {
        this.type = type;
    }

    public enum Type {
        EPUB, TXT
    }
}
