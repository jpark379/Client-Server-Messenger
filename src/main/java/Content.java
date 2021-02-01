import java.io.Serializable;
import java.util.ArrayList;

// CONTENT: class that holds the contents needed for
// sending messages
public class Content implements Serializable {

	// Recipients holds clients that we're sending to
	ArrayList<String> recipients;

	// List of clients in the room
	ArrayList<Integer> clientsInRoom;

	// id holds the client's id number
	int id = 0;

	// id holds the disconnected id number
	int disconnected = -1;

	// boolean that holds the connection status
	boolean connStatus = true;

	// String that holds message being sent to clients
	String messageToClients;

	// String that holds message being sent to server
	String messageToServer;


	// CONTENT CONSTRUCTOR: initialize the ArrayList
	Content() {
		recipients = new ArrayList<String>();
		clientsInRoom = new ArrayList<Integer>();
	}

	// GETRECIPIENTS: returns the recipient ArrayList
	public ArrayList<String> getRecipients() {
		return recipients;
	}

	// SETRECIPIENTS: sets the new recipients ArrayList
	public void setRecipients(ArrayList<String> newRecipients) {
		recipients.clear();
		for ( int i = 0; i < newRecipients.size(); i++) {
			recipients.add(newRecipients.get(i));
		}
	}

	// CLEARRECIPIENTS: sets the new recipients ArrayList
	public void clearRecipients() {
		recipients.clear();
	}

	// ADDRECIPIENTS: adds a value to recipients
	public void addRecipients(String clientNum) {
		recipients.add(clientNum);
	}

	// GETID: returns the id
	public int getID() {
		return id;
	}

	// GETID: sets the new id
	public void setID(int clientNum) {
		id = clientNum;
	}

	// GETDISCONNECTED: returns the id
	public int getDisconnected() {
		return disconnected;
	}

	// SETDISCONNECTED: sets the new id
	public void setDisconnected(int clientNum) {
		disconnected = clientNum;
	}

	// GETCONNSTATUS: returns the connection status
	public Boolean getConnStatus() {
		return connStatus;
	}

	// SETCONNSTATUS: sets the connection status
	public void setConnStatus(boolean status) {
		connStatus = status;
	}

	// GETCLIENTMESSAGE: returns the message to clients
	public String getClientMessage() {
		return messageToClients;
	}

	// SETCLIENTMESSAGE: sets the message to clients
	public void setClientMessage(String newMessage) {
		messageToClients = newMessage;
	}

	// GETSERVERMESSAGE: returns the message to server
	public String getServerMessage() {
		return messageToServer;
	}

	// SETSERVERMESSAGE: sets the message to server
	public void setServerMessage(String newMessage) {
		messageToServer = newMessage;
	}

	// GETCLIENTSINROOM: returns clientsInRoom
	public ArrayList<Integer> getClientsInRoom() {
		return clientsInRoom;
	}

	// SETCLIENTSINROOM: sets clientsInRoom
	public void setClientsInRoom(ArrayList<Integer> list) {
		clientsInRoom.clear();
		for ( int i = 0; i < list.size(); i++) {
			clientsInRoom.add(list.get(i));
		}
	}

	// SETCLIENTSINROOM: sets clientsInRoom
	public void addClientsInRoom(int num) {
		clientsInRoom.add(num);
	}

}
