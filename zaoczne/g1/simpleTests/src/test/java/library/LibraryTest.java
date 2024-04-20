package library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {

    private Library library;

    @BeforeEach
    void setUp() {
        library = new Library();
    }

    @AfterEach
    void tearDown() {
        try {
            Field f = Book.class.getDeclaredField("lastId");
            f.setAccessible(true);
            Book b = new Book ("","");
            f.set(b,0);
            f.setAccessible(false);
        }
        catch (Exception e)
        {

        }
        library = null;

    }

    @Test
    void init() {
        //**** GIVEN ****
        Book[] b = new Book[] {
                new Book("Adam Mickiewicz", "Pan Tadeusz"),
                new Book("Juliusz Słowacki", "Antygona"),
                new Book("Andrzej Sapkowski", "Krew Elfów")
        };
        //**** WHEN ****
        library.init(b);
        //**** THEN ****
        //** check 1
        assertEquals(3, library.books.size(), "Bad size of the list");
        //** check 2
        assertEquals("Krew Elfów", library.books.get(2).getTitle());
    }

    @Test
    void succesfullBorrow() {
        //**** GIVEN ****
        Book[] books = new Book[] {
                new Book("Adam Mickiewicz", "Pan Tadeusz"),
                new Book("Juliusz Słowacki", "Antygona"),
                new Book("Andrzej Sapkowski", "Krew Elfów")
        };
        library.init(books);
        //**** WHEN ****
        Book b = library.borrow("Juliusz Słowacki", "Antygona");
        //**** THEN ****
        assertNotNull(b);
        assertEquals(2,library.books.size());

    }

    @Test
    void unsuccesfullBorrow() {
        //**** GIVEN ****
        Book[] books = new Book[] {
                new Book("Adam Mickiewicz", "Pan Tadeusz"),
                new Book("Juliusz Słowacki", "Antygona"),
                new Book("Andrzej Sapkowski", "Krew Elfów")
        };
        library.init(books);
        //**** WHEN ****
        Book b = library.borrow("Adam Mickiewicz", "Dziady");
        //**** THEN ****
        assertNull(b);
        assertEquals(3,library.books.size());

    }
}