import bean.Book;
import engine.Downloader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import listener.EventListener;

import java.io.File;
import java.util.List;

/**
 * Created By zia on 2018/10/30.
 */
public class GUI extends Application implements EventListener {

    private Button searchButton, downloadButton, selectPath;
    private TextField searchField, path;
    private ListView<Book> listView;
    private TextArea status;
    private Hyperlink zzzia;
    private ChoiceBox<String> choiceBox;

    private Book selectedBook = null;
    private Downloader downloader;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
        primaryStage.setTitle("小说下载器");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        findId(root);

        downloader = new Downloader(this);

        primaryStage.setOnCloseRequest(event -> {
            primaryStage.close();
            try {
                stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(0);
        });


        searchButton.setOnAction(event -> {
            if (searchField.getText() != null && searchField.getText().length() > 0) {
                pushMessage("正在搜索...");
                new Thread(() -> downloader.search(searchField.getText())).start();
            }
        });

        downloadButton.setOnAction(event -> {
            if (selectedBook == null) {
                pushMessage("先选择要下载的书籍");
            } else {
                new Thread(() -> downloader.download(selectedBook)).start();
            }
        });

        selectPath.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File file = directoryChooser.showDialog(primaryStage);
            if (file == null || file.getPath().isEmpty()) return;
            String savePath = file.getPath();
            path.setText(savePath);
            downloader.setSavePath(savePath);
        });

        choiceBox.setItems(FXCollections.observableArrayList("EPUB", "TXT"));
        choiceBox.setValue("EPUB");
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
            switch (newValue) {
                case "EPUB":
                    downloader.setSaveResult(Downloader.Type.EPUB);
                    break;
                case "TXT":
                    downloader.setSaveResult(Downloader.Type.TXT);
                    break;
            }
        });

        zzzia.setOnAction(event -> {
            try {
                // 创建一个URI实例
                java.net.URI uri = java.net.URI.create("https://github.com/Zzzia/BookDownloader");
                // 获取当前系统桌面扩展
                java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                // 判断系统桌面是否支持要执行的功能
                if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    // 获取系统默认浏览器打开链接
                    dp.browse(uri);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void findId(Parent root) {
        searchButton = (Button) root.lookup("#searchButton");
        searchField = (TextField) root.lookup("#searchField");
        listView = (ListView) root.lookup("#listView");
        zzzia = (Hyperlink) root.lookup("#zzzia");
        status = (TextArea) root.lookup("#status");
        downloadButton = (Button) root.lookup("#downloadButton");
        path = (TextField) root.lookup("#path");
        selectPath = (Button) root.lookup("#selectPath");
        choiceBox = (ChoiceBox<String>) root.lookup("#choiceBox");
    }

    @Override
    public void onChooseBook(List<Book> books) {
        Platform.runLater(() -> {
            ObservableList<Book> listData = FXCollections.observableArrayList();
            listData.addAll(books);
            listView.setItems(listData);
            listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                selectedBook = newValue;
                pushMessage("已选择 " + newValue.getBookName() + "-" + newValue.getSite().getSiteName());
            });
            listView.setCellFactory(new Callback<ListView<Book>, ListCell<Book>>() {
                @Override
                public ListCell<Book> call(ListView<Book> param) {
                    return new ListCell<Book>() {
                        @Override
                        protected void updateItem(Book item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null) return;
                            setText(item.getBookName() + "  " + item.getAuthor() + "  " + item.getSite().getSiteName() + "  " + item.getLastChapterName());
                        }
                    };
                }
            });
        });
    }

    @Override
    public void pushMessage(String msg) {
        Platform.runLater(() -> status.setText(msg));
    }

    @Override
    public void onDownload(int progress, String msg) {
        Platform.runLater(() -> status.setText(progress + "%   " + msg));
    }

    @Override
    public void onEnd(String msg) {
        Platform.runLater(() -> {
            status.setText(msg);
            selectedBook = null;
        });
    }

    @Override
    public void onError(String msg, Exception e) {
        e.printStackTrace();
        Platform.runLater(() -> {
            status.setText(msg);
            selectedBook = null;
        });
    }
}
