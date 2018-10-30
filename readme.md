## 小说一键下载框架

本框架易于移植不同平台，扩展性强。

聚合了多个小说网站资源，选择性高。

已支持网站：

- [笔趣阁](http://www.biquge.com.tw)
- [笔神阁](http://www.bishenge.com)
- [看神作(推荐)](http://www.kanshenzuo.com)
- ~~[南山书院](https://www.szyangxiao.com)~~
- ~~[E8中文网](http://www.e8zw.com)~~

#### 一键下载并生成格式规范的txt或epub格式的小说

##### 并发下载，出错自动重试，理论能达到满速，实测下载速度2m/s以上

###### 网络请求采用okHttp，需要添加kotlin支持，最新版idea自带该插件，在KotlinPlugin文件里指定即可

------

使用方法：

在Main.java中配置正确的文件保存路径

```java
public class Main implements EventListener {
    public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            Downloader downloader = new Downloader(new Main());
            //设置保存格式，可选epub和txt
            downloader.setSaveResult(Downloader.Type.EPUB);
            //设置保存路径，默认在项目文件夹下
            downloader.setSavePath("/Users/jiangzilai/Documents/book");
            System.out.println("输入书籍名字:");
            String bookName = scanner.nextLine();
            downloader.download(bookName);
    }
    //......
}
```



在java环境下运行，然后根据提示操作即可