import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {

    private static ExecutorService service = Executors.newFixedThreadPool(5);

    /**
     *Input arguments:
        * 0.Listening port number
     **/

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Error : Not enough args");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket client = server.accept();
                    service.execute(new ClientHandlerThread(client));
                } catch (IOException e) {
                    System.err.println("Adding client error catched \n");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
