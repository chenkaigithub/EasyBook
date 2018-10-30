package engine;

import bean.Book;
import bean.Catalog;
import bean.Chapter;
import listener.EventListener;
import util.NetUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created By zia on 2018/10/30.
 * 带重试队列和进度监听的并发下载工具
 * 进度监听会严重影响性能
 */
public class BookFucker {
    private ArrayList<Chapter> chapters;
    private LinkedList<Catalog> catalogQueue;
    private final Object bufferLock = new Object();
    private final Object queueLock = new Object();

    private Book book;
    private EventListener eventListener;
    private ExecutorService threadPool;
    volatile private boolean needFreshProcess = true;
    volatile private int tempProgress = 0;

    BookFucker(Book book, EventListener eventListener) {
        this.book = book;
        this.eventListener = eventListener;
        this.threadPool = Executors.newFixedThreadPool(book.getSite().getThreadCount());
    }

    ArrayList<Chapter> download() throws IOException, InterruptedException {
        eventListener.onDownload(0, "正在解析目录...");

        BaseSite site = book.getSite();

        //从目录页获取有序章节
        String catalogHtml = NetUtil.getHtml(book.getUrl(), site.getEncodeType());
        List<Catalog> catalogs = new ArrayList<>();
        try {
            catalogs = site.parseCatalog(catalogHtml, book.getUrl());
            //添加序号
            for (int i = 0; i < catalogs.size(); i++) {
                catalogs.get(i).setIndex(i + 1);
            }
        } catch (Exception e) {
            eventListener.onError("解析目录失败，请联系作者修复", e);
        }

        chapters = new ArrayList<>(catalogs.size() + 1);
        catalogQueue = new LinkedList<>(catalogs);

        if (catalogs.size() == 0) {
            System.err.println(catalogHtml);
            throw new IOException("无法解析目录");
        }

        eventListener.onDownload(0, "一共" + catalogs.size() + "张，开始下载...");

        final int catalogSize = catalogs.size();
        CountDownLatch leftBook = new CountDownLatch(catalogSize);
        CountDownLatch errorBook = new CountDownLatch(catalogSize);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                needFreshProcess = true;
            }
        }, 0, 1000);

        synchronized (queueLock) {
            //自动探测是否全部下载
            while (getBufferSize() < catalogs.size()) {
                Catalog catalog = catalogQueue.poll();
                if (catalog == null) {//如果队列为空，释放锁，等待唤醒或者超时后继续探测
                    queueLock.wait(1000);
                } else {//队列有章节，下载所有
                    while (catalog != null) {
                        Catalog finalCatalog = catalog;
                        threadPool.execute(() -> {
                            try {
                                String chapterHtml = NetUtil.getHtml(finalCatalog.getUrl(), site.getEncodeType());
                                List<String> contents = site.parseContent(chapterHtml);
                                if (needFreshProcess) {
                                    tempProgress = (int) (((errorBook.getCount() - leftBook.getCount()) / (float) (2 * catalogSize - errorBook.getCount())) * 100);
                                    needFreshProcess = false;
                                }
                                eventListener.onDownload(tempProgress, finalCatalog.getChapterName());
                                Chapter chapter = new Chapter(finalCatalog.getChapterName(), finalCatalog.getIndex(), contents);
                                addChapter(chapter);
                                leftBook.countDown();
                            } catch (Exception e) {
                                if (needFreshProcess) {
                                    tempProgress = (int) (((errorBook.getCount() - leftBook.getCount()) / (float) (2 * catalogSize - errorBook.getCount())) * 100);
                                    needFreshProcess = false;
                                }
                                eventListener.onDownload(tempProgress, "重试章节 ： " + finalCatalog.getChapterName());
                                errorBook.countDown();
                                addQueue(finalCatalog);//重新加入队列，等待下载
                            }
                        });
                        catalog = catalogQueue.poll();
                    }
                }
            }
        }
        threadPool.shutdown();
        timer.cancel();
        chapters.sort(Comparator.comparingInt(Chapter::getIndex));
        eventListener.onDownload(100, "下载完成(" + chapters.size() + "章)，等待保存");
        return chapters;
    }

    private void addChapter(Chapter chapter) {
        synchronized (bufferLock) {
            chapters.add(chapter);
        }
    }

    private int getBufferSize() {
        synchronized (bufferLock) {
            return chapters.size();
        }
    }

    private void addQueue(Catalog catalog) {
        synchronized (queueLock) {
            catalogQueue.offer(catalog);
            //唤醒线程，添加所有章节到下载队列
            queueLock.notify();
        }
    }
}
