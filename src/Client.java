import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Client {
  public static volatile Socket sock;

  public static void main(String[] args) {
    if (args.length < 2) return;
    try {
      sock = new Socket(args[0], Integer.parseInt(args[1]));
    } catch(IOException e){System.out.println("client connection error: "+e);}
    
    new Thread(){
      public void run() {
        try {
          while(true) {
            System.in.transferTo(sock.getOutputStream());
          }
        } catch(IOException e){System.out.println("client output error: "+e);}
      }
    }.start();
    
    new Thread() {
      public void run() {
        try {
          while (true) {
            sock.getInputStream().transferTo(System.out);
          }
        } catch(SocketException e) {System.out.println("Connection lost. Press enter to continue...");}
          catch(IOException e){System.out.println("client input error: "+e);}
      }
    }.start();
  }
}