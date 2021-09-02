package pers.wu.gobang.msg;

import java.io.Serializable;

public class News implements Serializable {
    private String news;

    public News() {
    }

    public News(String news) {
        this.news = news;
    }

    public String getNews() {
        return news;
    }

    public void setNews(String news) {
        this.news = news;
    }
}
