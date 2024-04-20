package library;

public class Book {
    static int lastId=0;
    int id;
    String author;
    String Title;

    public Book(String author, String title) {
        this.author = author;
        Title = title;
        id=lastId++;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getId() {
        return id;
    }
}
