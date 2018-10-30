package util;

import engine.BaseSite;
import site.Biquge;
import site.Bishenge;
import site.Kanshenzuo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created By zia on 2018/10/30.
 */
public class SiteUtil {
    public static List<BaseSite> getAllSites() {
        List<BaseSite> baseSites = new ArrayList<>();
        baseSites.add(new Biquge());
        baseSites.add(new Kanshenzuo());
        baseSites.add(new Bishenge());
        return baseSites;
    }
}
