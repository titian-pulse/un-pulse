package org.unpulse.connector.command;


import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * User: gregswensen
 * Date: 11/11/14
 * Time: 11:25 AM
 */
@Component
public class Connector implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hi");
    }
}