import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

class ClientHandle implements Runnable {
    private final Socket clientSocket;

    public ClientHandle(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    private void handleClient() {
        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

        try(Scanner sc = new Scanner(clientSocket.getInputStream());
            OutputStream writer = clientSocket.getOutputStream()) {
            while(sc.hasNextLine()){
                String message=sc.nextLine();
                if(message.equalsIgnoreCase("PING")) {
                    writer.write("+PONG\r\n".getBytes());
                }
                if(message.equalsIgnoreCase("ECHO")){
                    String lenStr=sc.nextLine();
                    String bulkyStr= sc.nextLine();
                    System.out.println("Bulky String: "+bulkyStr);
                    bulkyStr = lenStr+
                            "\r\n" +
                            bulkyStr +
                            "\r\n";
                    writer.write(bulkyStr.getBytes());
                }
                writer.flush();
            }
        }catch (Exception e) {
            System.out.println("Client handler error: " + e.getMessage());
        }finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Close error: " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        handleClient();
    }
}
