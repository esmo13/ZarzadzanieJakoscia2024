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

    public Book borrow(String author, String title){
        int i = searchForBook(author, title);
        if (i<0) return null;
        Book b = books.get(i);
        this.books.remove(i);
        return b;
    }

    private int searchForBook(String author, String title) {
        for (int k=0; k<books.size(); k++){
            Book b = books.get(k);
            if (b.getAuthor().equals(author) && b.getTitle().equals(title)){
                return k;
            }
        }
        return -1;
    }


}
