package site;

import bean.Book;
import bean.Catalog;
import engine.BaseSite;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import util.BookGriper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By zia on 2018/10/31.
 * 顶点小说 https://www.booktxt.net/
 */
public class Dingdian extends BaseSite {
    @Override
    public String setSiteName() {
        return "顶点小说";
    }

    @Override
    public List<Book> search(String bookName) throws Exception {
        return BookGriper.baidu(this, bookName, "5334330359795686106");
    }

    @Override
    public List<Catalog> parseCatalog(String catalogHtml, String url) {
        Element listElement = Jsoup.parse(catalogHtml).getElementById("list");
        Elements as = listElement.after("</dt>").after("</dt>").getElementsByTag("a");
        List<Catalog> catalogs = new ArrayList<>();
        for (Element a : as) {
            String href = a.attr("href");
            String name = a.text();
            catalogs.add(new Catalog(name, url + href));
        }
        return catalogs;
    }

    @Override
    public List<String> parseContent(String chapterHtml) {
        List<TextNode> textNodes = Jsoup.parse(chapterHtml).getElementById("content").textNodes();
        return BookGriper.getContentsByTextNodes(textNodes);
    }
}
