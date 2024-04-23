package library;

import java.util.ArrayList;
import java.util.List;

public class Library {

    private List<Book> books = new ArrayList<>();

    public void init(Book[] books){
        for(Book b : books){
            this.books.add(b);
        }
    }

    public  Book borrow(String author, String title){
        int i = searchBook(author, title);
        if (i<0) return null;
        Book b = books.get(i);
        books.remove(i);
        return b;
    }

    private int searchBook(String author, String title) {
        for (int i =0; i< books.size(); i++){
            Book b = books.get(i);
            if (b.getAuthor().equals(author) && b.getTitle().equals(title)){
                return i;
            }
        }
        return -1;
    }
}
