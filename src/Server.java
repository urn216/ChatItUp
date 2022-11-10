import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
  public static volatile HashMap<Integer, ClientHandler> sockets = new HashMap<Integer, ClientHandler>();

  public static void main(String[] args) {
    if (args.length < 1) return;
    ServerSocket sock;
    System.out.println("Creating new Server!");
    try {
      int id = 0;
      sock = new ServerSocket(Integer.parseInt(args[0]));
      while (true) {
        ClientHandler newClient = new ClientHandler(sock.accept(), id);
        sockets.put(id, newClient);
        newClient.start();
        id++;
        System.out.println("number of active users: " + sockets.size());
      }
    } catch(IOException e){System.out.println(e);}
    System.out.println("Server closed unexpectedly!");
    System.out.println("Press Enter to continue...");
    Scanner scanner = new Scanner(System.in);
    scanner.nextLine();
    scanner.close();
  }

  public static void broadcast(String msg) {
    System.out.println(msg);
    for (ClientHandler c : sockets.values()) {
      c.send(msg, "SERVER", -1);
    }
  }
    
}

class ClientHandler extends Thread {
  private final Socket clientSock;
  private final PrintWriter out;
  private final int id;
  private String uid;

  public ClientHandler(Socket clientSock, int id) {
    this.clientSock = clientSock;
    this.id = id;
    PrintWriter a = null;
    try {
      a = new PrintWriter(clientSock.getOutputStream(), true);
    } catch(IOException e) {System.out.println(e);}
    out = a;
  }

  public void send(String msg, String user, int id) {
    if (this.id == id) return;
    out.println(user + "> " + msg);
  }

  public void run() {
    try {
      System.out.println("Client Connected");
      out.println("Welcome, new user!");
      out.println(getQuote());
      out.print("Username: ");
      out.flush();
      BufferedReader in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));

      uid = in.readLine();

      Server.broadcast("Welcome, " + uid);
      while (true) {
        String msg = in.readLine();
        if (msg.equals("exit")) break;
        System.out.println(uid + "> " + msg);
        for (ClientHandler c : Server.sockets.values()) {
          c.send(msg, uid, id);
        }
      }
      clientSock.close();
      Server.broadcast("User " + uid + " disconnected");
      Server.sockets.remove(id);
    } catch(IOException e){Server.broadcast("User " + uid + " disconnected poorly: " + e); Server.sockets.remove(id);}
    System.out.println("number of active users: " + Server.sockets.size());
  }

  private static String getQuote() {
    String[] quotes = {
      "Wow, you're so cool!",
      "[Insert inspiration here]",
      "I bet you could achieve that task if you put your mind to it for once!",
      "Fold your laundry!",
      "[Something funny]",
      "Someone smart probably said something about missing basketball shots or something...",
      "Hang in there!"
    };
    return quotes[(int)(Math.random()*quotes.length)];
  }
}
