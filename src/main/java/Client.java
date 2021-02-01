import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;



public class Client extends Thread{
	Socket socketClient;

	ObjectOutputStream out;
	ObjectInputStream in;

	private Consumer<Serializable> callback;

	Client(Consumer<Serializable> call){

		callback = call;
	}
	/* run
  Modified to read in a Content object and run callback.accept() on it;
  updating the windows */
	public void run() {

		try {
		socketClient= new Socket("127.0.0.1",5555);
	    out = new ObjectOutputStream(socketClient.getOutputStream());
	    in = new ObjectInputStream(socketClient.getInputStream());
	    socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {}

		while(true) {

			try {
				Content info = (Content) in.readObject();
				callback.accept(info);
			}
			catch(Exception e) {}
		}

    }


  /* send
  Modified to take a Content object and send it out. */
	public void send(Content info) {

		try {
			out.writeObject(info);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
