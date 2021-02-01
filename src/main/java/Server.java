import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;

public class Server{

	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	ArrayList<Integer> clientsInRoom = new ArrayList<Integer>(); // Container for the clients based on a client's ID.
	HashMap<String, ClientThread> numberToThread = new HashMap<String, ClientThread>(); // Mapping a client name to a ClientThread (for updating clients).
	TheServer server;
	private Consumer<Serializable> callback;
	Content info = new Content(); // Content object to be passed to and from the Client and Server.


	Server(Consumer<Serializable> call){

		callback = call;
		server = new TheServer();
		server.start();
	}


	public class TheServer extends Thread{

		public void run() {

			try(ServerSocket mysocket = new ServerSocket(5555);){
		    System.out.println("Server is waiting for a client!");


		    while(true) {
				ClientThread c = new ClientThread(mysocket.accept(), count);
        /* Set the message of info (Content object) to indicate that a client has joined.
        Add this client (using count) to the list of clients and map the client to the newly created thread */
				info.setServerMessage("A client has connected to server: " + "client #" + count);
				clientsInRoom.add(count);

				callback.accept(info); // Send this message to the server GUI.
				numberToThread.put("Client #" + Integer.toString(count), c); // Put this client and thread in the map.
				clients.add(c);
				c.start();

				count++;

			    }
			}//end of try
				catch(Exception e) {
					callback.accept("Server socket did not launch");
				}
			}//end of while
		}


		class ClientThread extends Thread{
			Socket connection;
			int count;
			int clientNum = 1;
			Content clientInfo = new Content();
			Content specificClientInfo = new Content();
			ObjectInputStream in;
			ObjectOutputStream out;

			ClientThread(Socket s, int count){
				this.connection = s;
				this.count = count;
			}

      /* getCount
      returns the number associated with a ClientThread */
			public Integer getCount() {
				return count;
			}
			/* getContent
      returns the Content object which contains information such as messages and recipients. */
			public Content getContent() {
				return clientInfo;
			}


      /* updateSpecificClients
      Taking only a Content object named info, info's recipients are found.
      These recipients are searched for in the HashMap that maps
      clients to ClientThreads, gets the ClientThreads, and sends info to each of them.
      Specific ID's are set for each ClientThread. */
			public synchronized void updateSpecificClients(Content info) {
				ArrayList<String> recipients = info.getRecipients();
				System.out.println("recipients in update specific clients" + info.getRecipients());
				for(String s : recipients) {
					ClientThread t = numberToThread.get(s);
					System.out.println("t: " + t.getCount());
					try {
						info.setID(t.getCount());
						t.out.writeObject(info);

						out.flush();
					}
					catch(Exception e) {
						System.out.println("oops");
						e.printStackTrace();
					}
				}
			}
			/* updateClients
      Selects each client thread in the room and sends a Content object.
      Primarily used for general purposes such as when a client enters or leaves.
      */
			public synchronized void updateClients(Content information) {

				for(int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
						info.setID(t.getCount());
						t.out.writeObject(information);
						out.flush();
					}
					catch(Exception e) {}
				}
			}

			public void run(){

				try {
					in = new ObjectInputStream(connection.getInputStream());
					out = new ObjectOutputStream(connection.getOutputStream());
					connection.setTcpNoDelay(true);
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}

				// New client joined. Update this client's message to notify the clients.
				clientInfo.setClientMessage("new client on server: client #"+ count);

				// Tell this client which clients are currently in the room.
				clientInfo.setClientsInRoom(clientsInRoom);
				// Update the info object to include a new recipient of the message (this client).
				info.addRecipients("Client #" + Integer.toString(count));
				// Set the ID of the client.
				info.setID(count);

				// Send info to update the clients
				updateClients(clientInfo);
				updateSpecificClients(info);

				 while(true) {
					    try {
                /* Read in the object as a Content object and create
                an ArrayList to store the recipients of the contents of this info object. */
					    	info = (Content)in.readObject();
					    	ArrayList<String> sentTo = new ArrayList<String>();

					    	System.out.println("id: " + info.getID());

					    	for ( int i = 0; i < info.getRecipients().size(); i++) {
					    		if (!((info.getRecipients()).get(i)).equals("Client #" + Integer.toString(info.getID()))) {
					    			sentTo.add(info.getRecipients().get(i));
					    		}
					    	}
					    	System.out.println("sent to: " + sentTo);

                /* Determine how this message is meant to be dislayed on the server and
                client windows */

               /* If the recipients of the info object is the same as the clients in the room,
               make the server say that a message was sent to everybody. Also tell this to the
               clients.

               Otherwise, the message is meant for a specific group of people or an individual. Set the messages in a more general sense. */
					    	if(info.getRecipients().size() == clientsInRoom.size()) {
					    		info.setServerMessage("client #" + count + " sent to everybody: " + info.getClientMessage());
						    	info.setClientMessage("client #" + count + " sent to everybody: " + info.getClientMessage());
					    	}
					    	else {
					    		info.setServerMessage("client #" + count + " sent to " + sentTo + ": " + info.getClientMessage());
						    	info.setClientMessage("client #" + count + " sent to " +  sentTo + ": " + info.getClientMessage());
					    	}

					    	System.out.println("Client message!: " + info.getClientMessage());
					    	System.out.println("recipients in server: " + info.getRecipients());

					    	callback.accept(info); // Send this information back to the server.
					    	updateSpecificClients(info); // Send this information to the clients.
                // Reset info.
					    	info.clearRecipients();
					    	info.setClientMessage(null);
				    	}
					    catch(Exception e) {
                /* If an exception is thrown, then there is a disconnect between a
                client and the server. */

					    	info.setServerMessage("Client #" + count + " disconnected"); // Prepare the message that will be shown on the server GUI.

                /* Create a new Content object which provides information about the disconnect. */
					    	Content disconnectClient = new Content();
					    	disconnectClient.setClientMessage("Client #" + count + " has left the server!");

                /* Update info so as to reflect the disonnected state. */
					    	info.setConnStatus(false); // Set the 'disconnected flag' to true.
					    	info.setID(count); // This client (count) has disconnected.
					    	callback.accept(info); // Update the server.
					    	// updateClients(disconnectClient);

					    	ArrayList<String> recipients = new ArrayList<String>();

					    	int disconnectedIndex = 0;
                /* Iterate through the clients in the room and
                locate the client that has disconnected.
                Save the index. Otherwise, add the other clients to the
                recipients list. */
					    	for ( int i = 0; i < clientsInRoom.size(); i++) {
					    		if (clientsInRoom.get(i) == count) {
					    			disconnectedIndex = i;
					    		}
					    		else {
					    			recipients.add("Client #" + Integer.toString(clientsInRoom.get(i)));
					    		}
					    	}


					    	clientsInRoom.remove(disconnectedIndex); // Remove the disconnected client from the list.
					    	numberToThread.remove("Client #" + Integer.toString(count)); // Remove the disconnected client from the map.

                /* Fill disconnectedClient and update the remaining clients */
					    	disconnectClient.setClientsInRoom(clientsInRoom);
					    	disconnectClient.setRecipients(recipients);
					    	disconnectClient.setDisconnected(count);
					    	updateSpecificClients(disconnectClient);


                /* If there are no more clients in the room.
                clear disconnnectedClient and remove 'this' client. */
					    	if (clientsInRoom.size() == 0) {
					    		disconnectClient.clearRecipients();
					    		info.clearRecipients();
					    	}
					    	clients.remove(this);

					    	break;
					    }
					}
				}//end of run


		}//end of client thread
}
