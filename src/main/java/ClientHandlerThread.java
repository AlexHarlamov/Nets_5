
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandlerThread implements Runnable {
    private Socket clientDialog;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private static final int timeout = 3000;


    ClientHandlerThread(Socket client) throws IOException {
        clientDialog = client;
        inputStream = new DataInputStream(client.getInputStream());
        outputStream = new DataOutputStream(client.getOutputStream());
    }

    @Override
    public void run() {
        System.out.println("New client : " + clientDialog);
        try {

            String fileName = recvFileName();
            long file_size = recvFileSize();

            File f = makeFile(fileName);

            System.out.println("Start receiving file : " + fileName);
            recvFile(f, file_size);

            clientDialog.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String recvFileName() throws IOException {
        return inputStream.readUTF();
    }

    private long recvFileSize() throws IOException {
        return inputStream.readLong();
    }

    private String getSpeedMsg(double speed) {
        if (speed <= 1024) {
            return speed + " B/s";
        } else if (speed <= 1024 * 1024) {
            return speed / 1024 + " KB/s";
        } else if (speed <= 1024 * 1024 * 1024) {
            return speed / 1024 / 1024 + " MB/s";
        } else {
            return speed / 1024 / 1024 / 1024 + " GB/s";
        }

    }

    private void PrintSpeed(long start_time, long wait_to_read, long file_size, long last_msg_time, long last_msg_read) {
        long curr_time = System.currentTimeMillis();

        double avg_speed = 0;
        if (curr_time - start_time != 0)
            avg_speed = (file_size - wait_to_read) / (curr_time - start_time) * 1000;

        double instant_speed = 0;
        if (curr_time - last_msg_time != 0)
            instant_speed = last_msg_read / (curr_time - last_msg_time) * 1000;

        System.out.println("||||||||");
        System.out.println("| Ip : " + clientDialog.getInetAddress());
        System.out.println("| Avarage speed = " + getSpeedMsg(avg_speed));
        System.out.println("| Instant speed = " + getSpeedMsg(instant_speed));
        System.out.println("||||||||\n");
    }

    private void recvFile(File f, long file_size) {
        try (FileOutputStream fout = new FileOutputStream(f)) {

            long start_time = System.currentTimeMillis();
            long last_msg_time = System.currentTimeMillis();
            int buff_size = 1024;
            byte[] buffer = new byte[buff_size];

            long wait_to_read = file_size;
            long curr_read = 0;
            long last_msg_read = 0;

            while (wait_to_read > 0) {

                try {
                    long curr_start_time = System.currentTimeMillis();
                    long curr_end_time = System.currentTimeMillis();

                    PrintSpeed(start_time, wait_to_read, file_size, last_msg_time, last_msg_read);
                    last_msg_time = System.currentTimeMillis();
                    last_msg_read = 0;

                    do {
                        clientDialog.setSoTimeout((int) (timeout - (curr_end_time - curr_start_time)));

                        if (wait_to_read > buff_size) {
                            curr_read = inputStream.read(buffer);
                        } else {
                            curr_read = inputStream.read(buffer, 0, (int) wait_to_read);
                        }

                        fout.write(buffer, 0, (int) curr_read);
                        wait_to_read -= curr_read;
                        last_msg_read += curr_read;
                        curr_end_time = System.currentTimeMillis();

                    } while (curr_end_time - curr_start_time < timeout && wait_to_read > 0);
                } catch (SocketTimeoutException ignored) {}
            }

            PrintSpeed(start_time, wait_to_read, file_size, last_msg_time, curr_read);

            long difference = f.length() - file_size;

            if ((int) difference == 0) {
                outputStream.writeUTF("CorrectEnd");
                System.out.println("Finish receiving file : " + f.getName() + " Without errors");
            } else {
                outputStream.writeUTF("WrongSize");
                System.out.println("Finish receiving file : " + f.getName() + "Incorrect size");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private File makeFile(String fileName) {
        File res;
        String name;
        Path p = Paths.get(fileName);
        name = p.getFileName().toString();


        String fileType = "";
        int dot_pos = fileName.lastIndexOf(".");
        if (dot_pos != -1) {
            name = fileName.substring(0, dot_pos);
            fileType = fileName.substring(dot_pos);
        }

        String newFileName = name + fileType;

        for (int i = 1; ; i++) {
            res = new File("uploads" + "/" + newFileName);

            if (!res.exists())
                return res;

            newFileName = name + "(" + i + ")" + fileType;

        }

    }

}
