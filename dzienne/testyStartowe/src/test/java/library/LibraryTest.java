package library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {

    Library lib;

    @BeforeEach
    void setUp() {
        lib = new Library();
    }

    @AfterEach
    void tearDown() {
        lib = null;
    }

    @Test
    void init() throws NoSuchFieldException, IllegalAccessException {

        //*** GIVEN ***
        //*** WHEN ***
        Book[] b = { new Book("Adam Mickiewicz", "Pan Tadeusz"),
                new Book("Adam Mickiewicz", "Dziady III"),
                new Book("Juliusz Słowacki", "Kordian"),};
        lib.init(b);
        //*** THEN ***
        // Czy lib ma trzy książki
        Field fbooks = lib.getClass().getDeclaredField("books");
        fbooks.setAccessible(true);
        List<Book> books = (List<Book>) fbooks.get(lib);
        fbooks.setAccessible(false);
        assertEquals(3, books.size(), "Zła ilość książek");
        // Czy drugą książką są Dziady III
        assertEquals("Dziady III", books.get(1).getTitle());
    }

    @Test
    void unsuccesfulBorrow() throws NoSuchFieldException, IllegalAccessException {
        //*** GIVEN ***
        Book[] b = { new Book("Adam Mickiewicz", "Pan Tadeusz"),
                new Book("Adam Mickiewicz", "Dziady III"),
                new Book("Juliusz Słowacki", "Kordian"),};
        Field fbooks = lib.getClass().getDeclaredField("books");
        fbooks.setAccessible(true);
        fbooks.set(lib, new ArrayList(Arrays.asList(b)));
        List<Book> books = (List<Book>) fbooks.get(lib);
        fbooks.setAccessible(false);
        //*** WHEN ***
        Book book = lib.borrow("Henryk Sienkiewicz", "Potop");
        //*** THEN ***
        assertNull(book);
        assertEquals(3,books.size());
    }

    @Test
    void succesfulBorrow() throws NoSuchFieldException, IllegalAccessException {
        //*** GIVEN ***
        Book[] b = { new Book("Adam Mickiewicz", "Pan Tadeusz"),
                new Book("Adam Mickiewicz", "Dziady III"),
                new Book("Juliusz Słowacki", "Kordian"),};
        Field fbooks = lib.getClass().getDeclaredField("books");
        fbooks.setAccessible(true);
        fbooks.set(lib, new ArrayList(Arrays.asList(b)));
        List<Book> books = (List<Book>) fbooks.get(lib);
        fbooks.setAccessible(false);
        //*** WHEN ***
        Book book = lib.borrow("Adam Mickiewicz", "Dziady III");
        //*** THEN ***
        assertNotNull(book, "Nie ma książki a powinna");
        assertEquals("Dziady III", book.getTitle(), "Zły tytuł");
        assertEquals(2,books.size());
    }

    /*Zadanie, modyfikacje kodu + odpowiednie testy:
        - Wypożyczanie książki nie kasuje jej z biblioteki
        - Dodanie użytkownika i jego karty wypożyczeń
            - wypożycza użytkownik
            - oddaje użytkownik
        - Serializacja danych (plik lub sqllite)

        TO READ: https://www.vogella.com/tutorials/java.html
        Kent Beck, Test Driven Development

    */
}