package by.peter.jprofby;

import by.peter.jprofby.service.ParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Ny2018ContestApplication implements CommandLineRunner {

    @Autowired
    private ParserService parserService;

    public static void main(String[] args) {
        SpringApplication.run(Ny2018ContestApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        int count = parserService.parseAndCount();
        System.out.println("Result: " + count);
    }
}
