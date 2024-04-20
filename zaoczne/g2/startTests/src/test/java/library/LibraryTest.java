package library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {

    private Library lib;

    @BeforeEach
    void setUp() {
        lib = new Library();
    }

    @AfterEach
    void tearDown() {
        lib = null;
    }

    @Test
    void init() {
        //*** GIVEN ***
        // class is empty
        //*** WHEN ***
        Book[] books = new Book[]{
          new Book("J.R.R. Tolkien", "Silmalirion"),
          new Book("J. Joice", "Ulisses"),
          new Book("M. Twain", "Tom Sawyer"),
        };
        lib.init(books);
        //*** THEN ***
        try {
            List<Book> listBooks;
            Field f = lib.getClass().getDeclaredField("books");
            f.setAccessible(true);
            listBooks = (List<Book>) f.get(lib);
            f.setAccessible(false);
            assertEquals(3, listBooks.size());
            assertEquals("Silmalirion", listBooks.get(0).getTitle());
        }
        catch(Exception e){

        }
    }

    @Test
    void succesfullBorrow() {
        //*** GIVEN ***
        Book[] books = new Book[]{
                new Book("J.R.R. Tolkien", "Silmalirion"),
                new Book("J. Joice", "Ulisses"),
                new Book("M. Twain", "Tom Sawyer"),
        };
        List<Book> listBooks = null;
        try {
            Field f = lib.getClass().getDeclaredField("books");
            f.setAccessible(true);
            listBooks = (List<Book>) f.get(lib);
            listBooks.addAll(Arrays.asList(books));
            f.setAccessible(false);
        }
        catch(Exception e){

        }
        //*** WHEN ***
        Book b = lib.borrow("J. Joice", "Ulisses");
        //*** THEN ***
        assertNotNull(b);
        assertEquals("J. Joice",b.getAuthor());
        assertEquals(2, listBooks.size());
    }

    @Test
    void unsuccesfullBorrow(){
        //*** GIVEN ***
        Book[] books = new Book[]{
                new Book("J.R.R. Tolkien", "Silmalirion"),
                new Book("J. Joice", "Ulisses"),
                new Book("M. Twain", "Tom Sawyer"),
        };
        List<Book> listBooks = null;
        try {
            Field f = lib.getClass().getDeclaredField("books");
            f.setAccessible(true);
            listBooks = (List<Book>) f.get(lib);
            listBooks.addAll(Arrays.asList(books));
            f.setAccessible(false);
        }
        catch(Exception e){

        }
        //*** WHEN ***
        Book b = lib.borrow("J.R.R. Tolkien", "Dwie Wieże");
        //*** THEN ***
        assertNull(b,"This book should not exist");
        assertEquals(3, listBooks.size());
    }

     /*Zadanie, modyfikacje kodu + odpowiednie testy:
        - Wypożyczanie książki nie kasuje jej z biblioteki
        - Dodanie użytkownika i jego karty wypożyczeń
            - wypożycza użytkownik
            - oddaje użytkownik
        - Serializacja danych (plik lub sqllite)
    */
}