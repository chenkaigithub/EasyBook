import bean.Book;
import engine.Downloader;
import listener.EventListener;

import java.util.List;
import java.util.Scanner;

/**
 * Created By zia on 2018/10/30.
 */
public class Main implements EventListener {
    private static Downloader downloader;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        downloader = new Downloader(new Main());
        //设置保存格式
        downloader.setSaveResult(Downloader.Type.EPUB);
        //设置保存路径，默认在项目文件夹下
        downloader.setSavePath("/Users/jiangzilai/Documents/book");
        System.out.println("输入书籍名字:");
        String bookName = scanner.nextLine();
        downloader.search(bookName);
    }

    @Override
    public void onChooseBook(List<Book> books) {
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            System.out.print("[" + i + "]");
            System.out.print("  " + book.getBookName());
            System.out.print("  作者:" + book.getAuthor());
            System.out.print("  来源:" + book.getSite().getSiteName());
            System.out.print("  最新章节:" + book.getLastChapterName());
            System.out.print("  最近更新时间:" + book.getLastUpdateTime());
            System.out.print("  链接：" + book.getUrl());
            System.out.println();
        }
        int index;
        System.out.println("请选择，输入序号");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            index = scanner.nextInt();
            if (index < 0 || index >= books.size()) {
                System.out.println("输入错误，请输入正确序号");
            } else {
                break;
            }
        }
        downloader.download(books.get(index));
    }

    @Override
    public void pushMessage(String msg) {
        System.out.println(msg);
    }

    @Override
    public void onDownload(int progress, String msg) {
        System.out.println(progress + "%," + msg);
    }

    @Override
    public void onEnd(String msg) {
        System.out.println(msg);
    }

    @Override
    public void onError(String msg, Exception e) {
        e.printStackTrace();
        System.err.println(msg);
        System.exit(0);
    }
}
