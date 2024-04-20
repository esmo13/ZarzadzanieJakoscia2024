package library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
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
        //*** WHEN ***
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
    void borrow() {
    }
}