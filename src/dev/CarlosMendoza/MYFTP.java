package dev.CarlosMendoza;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MYFTP {

    private Socket socket = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;
    private Scanner userScan = null;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: myftp server-name");
            return;
        }

        MYFTP myftp = new MYFTP(args[0]);

    }

    public MYFTP(String host) {

        userScan = new Scanner(System.in);

        try {
            socket = new Socket(host, 21);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Error: " + e);
            return;
        }

        String res = "";
        String userIn = "";

        //Connect to Server
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res.startsWith("220 ")){
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }

        //Send user name
        try {
            System.out.print("Name: ");
            userIn = userScan.nextLine();
            bufferedWriter.write("USER "+userIn+"\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res.startsWith("331 ")){
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }

        //Send password and check for confirmation
        try {
            System.out.print("Password: ");
            userIn = userScan.nextLine();
            bufferedWriter.write("PASS "+userIn+"\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res.startsWith("230 ")){
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }

        //Once successfully logged in then perform command loops until quit or application close
        Boolean exit = false;
        while (!exit) {
            System.out.print("ftp> ");
            userIn = userScan.nextLine();
            if (userIn.isEmpty()) continue;

            String[] tokens = userIn.split(" ");

            switch (tokens[0]) {
                case "ls":
                    System.out.println("Found ls");
                    break;
                case "cd":
                    System.out.println("Found cd");
                    break;
                case "get":
                    System.out.println("Found get");
                    break;
                case "put":
                    System.out.println("Found put");
                    break;
                case "delete":
                    System.out.println("Found delete");
                    break;
                case "quit":
                    try {
                        bufferedWriter.write("QUIT\r\n");
                        bufferedWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        res = bufferedReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (res.startsWith("221 ")) {
                        System.out.println(res);
                    } else {
                        System.out.println("Error: " + res);
                    }
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid command");
            }
        }

    }

}
