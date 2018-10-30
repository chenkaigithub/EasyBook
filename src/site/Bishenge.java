package site;

import bean.Book;
import bean.Catalog;
import engine.BaseSite;
import util.BookGriper;
import util.RegexUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By zia on 2018/10/30.
 * 笔神阁  http://www.bishenge.com
 * 测试约1.5m/s
 */
public class Bishenge extends BaseSite {
    @Override
    public String setSiteName() {
        return "笔神阁";
    }

    @Override
    public List<Book> search(String bookName) throws IOException {
        return BookGriper.baidu(this, bookName, "7751645214184726687");
    }

    @Override
    public List<Catalog> parseCatalog(String catalogHtml, String url) {
        String sub = RegexUtil.regexExcept("<div id=\"list\">", "</div>", catalogHtml).get(0);
        String ssub = sub.split("正文</dt>")[1];
        List<String> as = RegexUtil.regexInclude("<a", "</a>", ssub);
        List<Catalog> list = new ArrayList<>();
        as.forEach(s -> {
            RegexUtil.Tag tag = new RegexUtil.Tag(s);
            String name = tag.getText();
            String href = url + tag.getValue("href");
            list.add(new Catalog(name, href));
        });
        return list;
    }

    @Override
    public List<String> parseContent(String chapterHtml) {
        String sub = RegexUtil.regexExcept("<div id=\"content\">", "</div>", chapterHtml).get(0);
        String lines[] = sub.split("<br>|<br/>|<br />");
        List<String> contents = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                contents.add(line);
            }
        }
        return contents;
    }
}
