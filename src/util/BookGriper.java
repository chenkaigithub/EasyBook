package util;

import bean.Book;
import engine.BaseSite;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By zia on 2018/10/30.
 */
public class BookGriper {
    /**
     * 百度站内搜索
     *
     * @param bookName 小说名字
     * @param s        网站识别码
     * @return
     */
    public static List<Book> baidu(BaseSite site, String bookName, String s) throws IOException {
        String url = "http://zhannei.baidu.com/cse/search?q="
                + URLEncoder.encode(bookName, "gbk")
                + "&s=" + s;
        String html = NetUtil.getHtml(url, "utf-8");

        Element body = Jsoup.parse(html).body();
        Elements results = body.getElementsByClass("result-list");
        if (results.size() == 0) {
            throw new IOException("没有找到该小说");
        }

        Elements details = results.get(0).getElementsByClass("result-game-item-detail");
        List<Book> bookList = new ArrayList<>(details.size());
        for (Element detail : details) {
            Element titleElement = detail.getElementsByClass("result-game-item-title-link").get(0);
            String bkName = titleElement.getElementsByAttribute("title").get(0).text();
            String bkUrl = titleElement.attr("href");
            Elements spans = detail.getElementsByTag("span");
            String author = spans.get(1).text();
            String lastUpdateTime = spans.get(5).text();
            String lastChapterName = detail.getElementsByClass("result-game-item-info-tag-item")
                    .get(0).text();
            Book book = new Book(bkName, author, bkUrl, "未知", lastUpdateTime, lastChapterName, site);
            bookList.add(book);
        }

        return bookList;
    }
}
