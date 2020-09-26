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
        if (res.startsWith("220 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }

        //Send user name
        try {
            System.out.print("Name: ");
            userIn = userScan.nextLine();
            bufferedWriter.write("USER " + userIn + "\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res.startsWith("331 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }

        //Send password and check for confirmation
        try {
            System.out.print("Password: ");
            userIn = userScan.nextLine();
            bufferedWriter.write("PASS " + userIn + "\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res.startsWith("230 ")) {
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
                    ls(tokens);
                    break;
                case "cd":
                    cd(tokens);
                    break;
                case "get":
                    System.out.println("Found get");
                    break;
                case "put":
                    System.out.println("Found put");
                    break;
                case "delete":
                    delete(tokens);
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

    //Performs cd operations and returns of it is missing a path
    private void cd(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("cd requires path argument");
            return;
        }
        String res = "";
        try {
            bufferedWriter.write("CWD " + tokens[1] + "\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res.startsWith("250 ")) {
            System.out.println(res);
        } else if (res.startsWith("550 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
        }
    }

    //performs delete command and returns if missing path
    private void delete(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("delete requires path argument");
            return;
        }
        String res = "";
        try {
            bufferedWriter.write("DELE " + tokens[1] + "\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res.startsWith("250 ")) {
            System.out.println(res);
        } else if (res.startsWith("550 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
        }
    }

    private void ls(String[] tokens) {
        //Enter passive mode
        String res = "";
        try {
            bufferedWriter.write("PASV\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res.startsWith("227 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }

        String host = "";
        int port = -1;
        int subBegin = res.indexOf('(') + 1;
        int subEnd = res.indexOf(')');
        String[] resTokens = res.substring(subBegin, subEnd).split(",");

        host = resTokens[0] + "." + resTokens[1] + "." + resTokens[2] + "." + resTokens[3];
        port = Integer.parseInt(resTokens[4]) * 256 + Integer.parseInt(resTokens[5]);

        Socket dataSocket = null;
        try {
            dataSocket = new Socket(host, port);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

        try {
            bufferedWriter.write("LIST\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res.startsWith("150 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }

        BufferedReader bufferData = null;
        try {
            bufferData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        do {
            try {
                res = bufferData.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (res != null) {
                System.out.println(res);
            }
        } while (res != null);

        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (res.startsWith("226 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
        }


        try {
            bufferData.close();
            dataSocket.close();
        } catch (IOException e) {
        }

    }
}
