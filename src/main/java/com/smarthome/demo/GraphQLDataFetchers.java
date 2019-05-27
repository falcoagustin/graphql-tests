package com.smarthome.demo;

import com.smarthome.demo.domain.Author;
import com.smarthome.demo.domain.Book;
import com.smarthome.demo.domain.User;
import com.smarthome.demo.repositories.AuthorRepository;
import com.smarthome.demo.repositories.BookRepository;
import com.smarthome.demo.repositories.UserRepository;
import com.smarthome.demo.services.LoginService;
import graphql.GraphQLException;
import graphql.schema.DataFetcher;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class GraphQLDataFetchers {
    private AuthorRepository authorRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;
    private LoginService loginService;

    // TODO: REALLY REALLY Dummy impl
    private final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public DataFetcher getBookByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String bookId = dataFetchingEnvironment.getArgument("id");
            return bookRepository.findById(Long.valueOf(bookId));
        };
    }

    public DataFetcher getAuthorDataFetcher() {
        return dataFetchingEnvironment -> {
            Book book = dataFetchingEnvironment.getSource();
            return book.getAuthor();
        };
    }

    public DataFetcher saveAuthor() {
        return dataFetchingEnvironment -> {
            Author author = new Author();
            author.setFirstName(dataFetchingEnvironment.getArgument("firstName"));
            author.setLastName(dataFetchingEnvironment.getArgument("lastName"));
            return authorRepository.save(author);
        };
    }

    public DataFetcher saveBook() {
        return dataFetchingEnvironment -> {
            Map map = dataFetchingEnvironment.getArgument("author");

            Book book = new Book();
            Author author = new Author();
            author.setId(Long.valueOf((String)map.get("id")));

            book.setAuthor(author);
            book.setName(dataFetchingEnvironment.getArgument("name"));
            // TODO: Fix as author is not loaded.
            return bookRepository.save(book);
        };
    }

    public DataFetcher updateBook() {
        return dataFetchingEnvironment -> {
            Long id = Long.valueOf(dataFetchingEnvironment.getArgument("id"));
            Optional<Book> optBook = bookRepository.findById(id);
            if (!optBook.isPresent()) {
              return null;
            }
            Book book = optBook.get();
            String name = dataFetchingEnvironment.getArgument("name");
            book.setName(name);
            Optional<Author> optAuthor = authorRepository.findById(Long.valueOf(dataFetchingEnvironment.getArgument("authorId")));
            optAuthor.ifPresent(i -> book.setAuthor(i));
            bookRepository.save(book);
            return book;
        };
    }

    public DataFetcher deleteBook() {
        return dataFetchingEnvironment -> {
            Long id = Long.valueOf(dataFetchingEnvironment.getArgument("id"));
            bookRepository.deleteById(id);
            return id;
        };
    }

    public DataFetcher bulkCreateAuthors() {
        return dataFetchingEnvironment -> {
            List<Map<String, Object>> toBePersisted = dataFetchingEnvironment.getArgument("objects");
            return authorRepository.saveAll(processRawAuthorData(toBePersisted));
        };
    }

    public DataFetcher saveUser() {
        return dataFetchingEnvironment -> {
            String email = dataFetchingEnvironment.getArgument("email");
            String password = hashPassword(dataFetchingEnvironment.getArgument("password"));
            User newUser = new User(email, password);
            return userRepository.save(newUser);
        };
    }

    public DataFetcher login() {
        return dataFetchingEnvironment -> {
            String email = dataFetchingEnvironment.getArgument("email");
            String password = hashPassword(dataFetchingEnvironment.getArgument("password"));
            List<User> users = userRepository.findByEmail(email);
            if (users.size() == 0) {
                throw new GraphQLException("No user found");
            }
            User user = users.get(0);
            if (!user.getPassword().equals(password)) {
                throw new GraphQLException("Invalid credentials");
            }
            return loginService.login(user);

        };
    }

    private static List<Author> processRawAuthorData(List<Map<String, Object>> rawDataList) {
        return rawDataList.stream().map(i -> {
            Author author = new Author();
            author.setFirstName((String) i.get("firstName"));
            author.setLastName((String) i.get("lastName"));
            return author;
        }).collect(Collectors.toList());
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hashedPassword = md.digest(password.getBytes());
            return new String(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            // Will not happen.
            return null;
        }
    }
}
