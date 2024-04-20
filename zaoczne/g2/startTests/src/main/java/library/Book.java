package library;

public class Book {

    private static int lastId=0;
    private int id;
    private String author;
    private String title;

    public Book(String author, String title) {
        this.author = author;
        this.title = title;
        id=lastId++;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
