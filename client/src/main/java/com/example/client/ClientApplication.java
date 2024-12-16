package com.example.client;

import com.example.client.service.ClientService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class ClientApplication {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean flag = true;
        while (flag) {
            System.out.println("Enter command : 1. Read 2. Write 3. Exit");
            int command = scanner.nextInt();
            switch (command) {
                case 1:
                    readOperation();
                    break;
                case 2:
                    writeOperation();
                    break;
                case 3:
                    flag = false;
            }
        }
    }

    public static void readOperation() {
        Scanner scanner = new Scanner(System.in);
        ClientService clientService = new ClientService();
        String fileName;
        System.out.println("##Read Operation## Enter File Name : ");
        fileName = scanner.nextLine();
//        clientService.readFile(fileName);
    }

    public static void writeOperation() {
        ClientService clientService = new ClientService();
        Scanner scanner = new Scanner(System.in);
        String fileName;
        System.out.println("##Write Operation## Enter File Path : ");
        String filePath = scanner.nextLine();
        byte[] file;
        try {
            file = Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Enter File Name : ");
        fileName = scanner.nextLine();
        System.out.println("Enter Number Of Copies : ");
        int numCopies = scanner.nextInt();
        System.out.println(clientService.uploadFile(fileName, file, numCopies));
    }
}
