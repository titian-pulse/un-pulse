package org.un.pulse.connector.command;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.un.pulse.connector.sqs.SQSWorker;

/**
 * Created with IntelliJ IDEA.
 * User: gregswensen
 * Date: 11/11/14
 * Time: 11:25 AM
 */
@Component
public class Connector implements CommandLineRunner {

    @Autowired
    SQSWorker sqsWorker;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hi");
    }
}