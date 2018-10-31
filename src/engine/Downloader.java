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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created By zia on 2018/10/30.
 */
public class Downloader {


    private Type type = Type.EPUB;
    private EventListener eventListener;
    private String path = ".";

    public Downloader(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void search(String bookName) {
        search(bookName, SiteUtil.getAllSites());
    }

    public void search(String bookName, List<BaseSite> sites) {
        if (path == null) {
            eventListener.onError("请配置文件路径", new FileNotFoundException());
            return;
        }
        eventListener.pushMessage("开始搜索书籍");

        ConcurrentLinkedQueue<List<Book>> bookListList = new ConcurrentLinkedQueue<>();
        CountDownLatch countDownLatch = new CountDownLatch(sites.size());
        ExecutorService service = Executors.newFixedThreadPool(20);

        for (BaseSite site : sites) {
            service.execute(() -> {
                List<Book> results = null;
                try {
                    results = site.search(bookName);
                } catch (Exception e) {
                    eventListener.pushMessage(e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
                if (results == null) {
                    eventListener.pushMessage(site.getSiteName() + "搜索结果错误，正在尝试其它网站");
                    return;
                }
                bookListList.add(results);
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            eventListener.onError("搜索时发生并发错误", e);
        } finally {
            service.shutdown();
        }


        int resultSize = 0;
        for (List<Book> bookList : bookListList) {
            resultSize += bookList.size();
        }

        if (resultSize == 0) {
            eventListener.onError("没有搜索到书籍", new IOException());
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
        //微调排序
        bookList.sort((o1, o2) -> {
            if (o1.getBookName().equals(bookName) && !o2.getBookName().equals(bookName)) {
                return -1;
            } else if (!o1.getBookName().equals(bookName) && o2.getBookName().equals(bookName)) {
                return 1;
            } else if (o1.getBookName().contains(bookName) && !o2.getBookName().contains(bookName)) {
                return -1;
            } else if (!o1.getBookName().contains(bookName) && o2.getBookName().contains(bookName)) {
                return 1;
            } else if (o1.getBookName().length() == bookName.length()
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
        eventListener.onChooseBook(bookList);


    }

    public void download(Book book) {
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
            String savePath = save(chapters, bkName, type);
            eventListener.onEnd("保存成功，路径：" + savePath);
        } catch (Exception e) {
            eventListener.onError("文件保存失败", e);
        }
    }

    private String save(List<Chapter> chapters, String bookName, Type type) throws IOException {
        if (type == Type.EPUB) {
            String name = bookName + ".epub";
            String savePath = path + File.separator + name;
            File file = new File(savePath);
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
            return savePath;
        } else {
            String savePath = path + File.separator + bookName + ".txt";
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(savePath)));
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
            return savePath;
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
