package listener;

import bean.Book;

import java.util.List;

public interface EventListener {
    void onChooseBook(List<Book> books);

    void pushMessage(String msg);

    void onDownload(int progress, String msg);

    void onEnd(String msg);

    void onError(String msg, Exception e);
}
