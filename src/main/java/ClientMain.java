import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ClientMain {

    private static DataOutputStream outputStream;

    /**
     * Input arguments :
         *  0.Server's port number
         *  1.Ip/Dns of server
         *  2.File path
     **/

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Error : Not enough args");
            return;
        }

        int port = Integer.parseInt(args[0]);

        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName(args[1]);
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
            return;
        }

        String filepath = args[2];

        File f = new File(filepath);

        try (Socket server = new Socket(serverAddress, port);
             FileInputStream stream = new FileInputStream(f)) {

            outputStream = new DataOutputStream(server.getOutputStream());
            DataInputStream inputStream = new DataInputStream(server.getInputStream());

            SendFileName(f);
            SendFileSize(f);
            SendFile(stream);

            String answ;
            answ = inputStream.readUTF();
            switch (answ) {
                case "CorrectEnd":
                    System.out.println("File transfer complited without errors");
                    break;
                case "WrongSize":
                    System.out.println("Received file has wrong size");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void SendFileName(File f) throws IOException {
        outputStream.writeUTF(f.getName());
    }

    private static void SendFileSize(File f) throws IOException {
        outputStream.writeLong(f.length());
    }

    private static void SendFile(FileInputStream stream) throws IOException {

        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer, 0, buffer.length)) > 0) {
            outputStream.write(buffer, 0, length);
        }
    }

}
