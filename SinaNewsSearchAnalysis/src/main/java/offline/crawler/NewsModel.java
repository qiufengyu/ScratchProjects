package offline.crawler;

/**
 * 新闻实体对象，包括新闻的标题、链接、类别和正文四个部分
 * Title: 标题，String
 * Link: 新闻的具体链接，String
 * Category: 新闻的类别，int
 * Content: 新闻正文
 * Time: 新闻的时间戳
 */
public class NewsModel {

    private String title;
    private String link;
    private int category;
    private String content;
    private int time;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        System.out.println("Title: "+ title);
        System.out.println("Link: " + link);
        System.out.println("Category: " + category);
        System.out.println("Timestamp: " + time);

        return "";

    }
}
