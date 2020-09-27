package dev.CarlosMendoza;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Scanner;

public class myftp {


    private Socket socket = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;
    private Scanner userScan = null; //Keyboard input

    public static void main(String[] args) {
        //In the event that it is entered incorrectly
        if (args.length == 0) {
            System.out.println("Usage: myftp server-name");
            return;
        }

        myftp myftp = new myftp(args[0]);

    }

    //Main driving function
    public myftp(String host) {

        //Initialize base value, ftp has a port of 21 so hardcoded
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
            System.out.println("Error: " + e);
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
            System.out.println("Error: " + e);
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
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
            System.out.println("Error: " + e);
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
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
                    get(tokens);
                    break;
                case "put":
                    put(tokens);
                    break;
                case "delete":
                    delete(tokens);
                    break;
                case "quit":
                    try {
                        bufferedWriter.write("QUIT\r\n");
                        bufferedWriter.flush();
                    } catch (IOException e) {
                        System.out.println("Error: " + e);
                    }
                    try {
                        res = bufferedReader.readLine();
                    } catch (IOException e) {
                        System.out.println("Error: " + e);
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
        //Needs an argument
        if (tokens.length < 2) {
            System.out.println("cd requires path argument");
            return;
        }
        String res = "";
        try {
            bufferedWriter.write("CWD " + tokens[1] + "\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
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
            System.out.println("Error: " + e);
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
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
            System.out.println("Error: " + e);
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        if (res.startsWith("227 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }

        //initiate datasocket from passive command
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
        //once data socket setup tell server to send data through data socket
        try {
            bufferedWriter.write("LIST\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        if (res.startsWith("150 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }
        //Get stream from server and print until end of stream
        BufferedReader bufferData = null;
        try {
            bufferData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

        do {
            try {
                res = bufferData.readLine();
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
            if (res != null) {
                System.out.println(res);
            }
        } while (res != null);

        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
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

    //responsible for putting data on the server, also initiates a passive connection for data socket
    private void put(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("delete requires path argument");
            return;
        }
        //Enter passive mode
        String res = "";
        try {
            bufferedWriter.write("PASV\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
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
            bufferedWriter.write("STOR " + tokens[1] + "\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        if (res.startsWith("150 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }

        File file = new File(tokens[1]);
        if (!file.exists()) {
            System.out.println("ftp: file does not exist");
            return;
        } else if (file.isDirectory()) {
            System.out.println("ftp: cannot put directory");
        }

        InputStream fileStream = null;
        try {
            fileStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println("Error: " + e);
        }

        BufferedOutputStream bufferData = null;
        BufferedInputStream inputStream = null;
        try {
            bufferData = new BufferedOutputStream(dataSocket.getOutputStream());
            inputStream = new BufferedInputStream(fileStream);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

        byte[] data = new byte[4096];
        int bytesRead = 0;
        int totalBytes = 0;
        boolean stop = false;
        Timestamp startTime = new Timestamp(System.currentTimeMillis());
        while (!stop) {
            try {
                bytesRead = inputStream.read(data);
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
            if (bytesRead == -1) {
                stop = true;
                continue;
            }

            totalBytes += bytesRead;
            try {
                bufferData.write(data, 0, bytesRead);
                bufferData.flush();
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }

        }

        Timestamp endTime = new Timestamp(System.currentTimeMillis());

        //important to close bufferData otherwise the server doesnt know when the stream will stop
        try {
            bufferData.close();
            inputStream.close();
            fileStream.close();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        if (res.startsWith("226 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
        }

        System.out.println(totalBytes + " bytes sent in " + (endTime.getTime() - startTime.getTime()) + " ms");

    }

    //similar to the put method but instead the streams are reversed in direction
    private void get(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("delete requires path argument");
            return;
        }
        //Enter passive mode
        String res = "";
        try {
            bufferedWriter.write("PASV\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
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
            bufferedWriter.write("RETR " + tokens[1] + "\r\n");
            bufferedWriter.flush();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        if (res.startsWith("150 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
            return;
        }

        File file = new File(tokens[1]);

        OutputStream fileStream = null;

        try {
            fileStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println("Error: " + e);
        }

        InputStream bufferData = null;
        BufferedOutputStream outputStream = null;
        try {
            bufferData = dataSocket.getInputStream();
            outputStream = new BufferedOutputStream(fileStream);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

        byte[] data = new byte[4096];
        int bytesRead = 0;
        int totalBytes = 0;
        boolean stop = false;
        Timestamp startTime = new Timestamp(System.currentTimeMillis());

        while (!stop) {
            try {
                bytesRead = bufferData.read(data);
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
            if (bytesRead == -1) {
                stop = true;
                continue;
            } else {
                totalBytes += bytesRead;
            }

            try {
                outputStream.write(data, 0, bytesRead);
                outputStream.flush();
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }

        }

        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        try {
            bufferData.close();
            outputStream.close();
            fileStream.close();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

        try {
            res = bufferedReader.readLine();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        if (res.startsWith("226 ")) {
            System.out.println(res);
        } else {
            System.out.println("Error: " + res);
        }

        System.out.println(totalBytes + " bytes received in " + (endTime.getTime() - startTime.getTime()) + " ms");
    }
}
