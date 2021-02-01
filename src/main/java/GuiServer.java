
import java.awt.Checkbox;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiServer extends Application{

  /* sendToBoxes - maps an integer (equal to the client # that joins) to a checkbox
  for simple lookups and targeting when sending a message. */
	HashMap<Integer, CheckBox> sendToBoxes = new HashMap<Integer, CheckBox>();

  /* connectedClients - refreshed when an info object arrives and is updated whenever
  a client joins. */
	ArrayList<Integer> connectedClients = new ArrayList<Integer>();

  /* recipients - filled when a client hits send. */
	ArrayList<String> recipients = new ArrayList<String>();
	TextField typeHere;
	Button serverChoice,clientChoice,send, exit;
	HashMap<String, Scene> sceneMap;
	GridPane grid;
	HBox buttonBox;
	HBox recipientsBox;
	VBox clientBox;
	Scene startScene;
	BorderPane startPane;
	Server serverConnection;
	Client clientConnection;
	Integer clientID;
	Integer disconnectedID = -1;
	String serverMessage, clientMessage, sendMessage;
	ListView<String> listItems, listItems2;
	CheckBox toAll;
	int count = 0;


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("Chat Box Program");

		this.serverChoice = new Button("SERVER");
		this.serverChoice.setStyle("-fx-pref-width: 300px");
		this.serverChoice.setStyle("-fx-pref-height: 300px");
		this.serverChoice.setStyle("-fx-background-color: FLORALWHITE;");


    /* If the user chooses the server button,
    a new Server object is created and its list view is continously updated. */
		this.serverChoice.setOnAction(e->{
			primaryStage.setScene(sceneMap.get("server"));
			primaryStage.setTitle("This is the Server");

			serverConnection = new Server(data ->{ Platform.runLater(()->{

          /* Retrieve the Content object that is sent, retrieve the message
          associated with it, and add it to the list view. */
					serverMessage = ((Content) data).getServerMessage();
					listItems.getItems().add(serverMessage);
				});

			});
		});


		this.clientChoice = new Button("CLIENT");
		this.clientChoice.setStyle("-fx-background-color: FLORALWHITE;");

		synchronized(clientChoice) {
			this.clientChoice.setOnAction(e-> {
					primaryStage.setScene(sceneMap.get("client"));
					clientConnection = new Client(data->{ Platform.runLater(()->{
					/* Retrive the Content object that is sent and store its:
          message, ID, and the clients that are in the room (in order to tell which
          clients are connected). */

					clientMessage = ((Content) data).getClientMessage();
					clientID =((Content) data).getID();

					primaryStage.setTitle("Client");

					disconnectedID = ((Content) data).getDisconnected();
					connectedClients = ((Content) data).getClientsInRoom();


					recipientsBox.getChildren().removeAll(); // Clear the checkboxes.

          /* Create a new checkbox for each of the connected clients and
          add it to the recipientsBox (an HBox). This is how the checkboxes are
          kept up to date. */
					for ( int i = 0; i < connectedClients.size(); i++) {
						CheckBox newBox = new CheckBox("Client #" + connectedClients.get(i));
						sendToBoxes.put(connectedClients.get(i), newBox);
					}
					/* If it is the case that a client has disconnected, meaning the Content
          object's disconnectedID is not -1, then find the box associated with this value
          in sendToBoxes and remove it. */
					if (disconnectedID != -1) {
						sendToBoxes.remove(disconnectedID);
						disconnectedID = -1;
					}

          /* Clear the recipientsBox (an HBox)
          and add the button that allows the user to send a message to every client. */
					recipientsBox.getChildren().clear();
					recipientsBox.getChildren().add(toAll);

          /* Loop through each Integer, CheckBox pair and append its associated
          CheckBox to the recipientsBox (an HBox) */
					sendToBoxes.entrySet().forEach(entry->{
						recipientsBox.getChildren().add(entry.getValue());
					 });


          /* Unless the message sent is empty, add it to the listView. */
					if (clientMessage != null) {
						listItems2.getItems().add(clientMessage);
					}
					});
				});

				clientConnection.start();
			});
		}


    /* Set up the screen */
		Text welcome = new Text("WELCOME");
		welcome.setFont(Font.font("Helvetica",FontWeight.BOLD,40));
		welcome.setFill(Color.DARKGREY);
		HBox centerWelcome = new HBox(welcome);
		this.buttonBox = new HBox(50, serverChoice, clientChoice);
		startPane = new BorderPane();
		startPane.setPadding(new Insets(70));
		startPane.setCenter(buttonBox);
		startPane.setTop(centerWelcome);
		centerWelcome.setAlignment(Pos.CENTER);
		startPane.setStyle("-fx-background-color: mistyrose");
		buttonBox.setAlignment(Pos.CENTER);
		startScene = new Scene(startPane, 500, 500);

		listItems = new ListView<String>();
		listItems2 = new ListView<String>();

		typeHere = new TextField();
		send = new Button("SEND");


    /* send setOnAction
    Loop through each (Integer, CheckBox) pair and check each CheckBox's status. */
		send.setOnAction(e->{
			Content info = new Content();
			sendToBoxes.entrySet().forEach(entry->{


        /* Add each client to the recipients if the toAll checkbox is selected.
        Otherwise, only add the clients associated with a selected checkbox. */
				if (toAll.isSelected()) {
					recipients.add("Client #" + Integer.toString(entry.getKey()));
				}
				else if(entry.getValue().isSelected()) {
					recipients.add("Client #" + Integer.toString(entry.getKey()));
					count++;
				}

        /* Reset the checkboxes so that they appear unselected. */
				entry.getValue().setSelected(false);
			    System.out.println(entry.getKey() + " " + entry.getValue());
			});

			if (count == 0 && !(toAll.isSelected())) {
				return;
			}


			if (!recipients.contains("Client #" + Integer.toString(clientID))) {
				recipients.add("Client #" + Integer.toString(clientID));
			}
			System.out.println("recipients in GUI: " + recipients);

			sendMessage = typeHere.getText(); // Grab the message that is to be sent.
			info.setRecipients(recipients); // Set info's recipients.
			info.setClientMessage(sendMessage); // Set info's message.
			info.setID(clientID); // Set info's id to this client's ID.
			clientConnection.send(info); // Send info.

      /* Clear the fields */
			typeHere.clear();
			recipients.clear();
			toAll.setSelected(false);
		});


    /* Create and fill the HashMap mapping strings to scenes. */
		sceneMap = new HashMap<String, Scene>();
		sceneMap.put("server",  createServerGui());
		sceneMap.put("client",  createClientGui());

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(startScene);
		primaryStage.show();

	}


  /* createServerGui
  Creates a window with a list view which is updated everytime a message
  is sent or a client joins/leaves the session. */
	public Scene createServerGui() {

		BorderPane pane = new BorderPane();
		Text serverTitle = new Text("SERVER");
		serverTitle.setFont(Font.font("Helvetica",FontWeight.BOLD,20));
		serverTitle.setFill(Color.FLORALWHITE);
		HBox title = new HBox(serverTitle);
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: lightsalmon");

		title.setAlignment(Pos.CENTER);
		pane.setTop(title);
		pane.setCenter(listItems);

		return new Scene(pane, 500, 400);


	}
	  /* createClientGui
  Creates a window with a text field, a list of checkboxes (for choosing recipients), a send button, and a list view which is updated whenever a client joins/leaves and whenever a message is received. The sender sees its message on its screen. */

	public Scene createClientGui() {
		exit = new Button("DISCONNECT");
		toAll = new CheckBox("To All");
		recipientsBox = new HBox();
		recipientsBox.setSpacing(20);
		clientBox = new VBox(10, typeHere, recipientsBox, send,listItems2);
		clientBox.setStyle("-fx-background-color: lightblue");
		return new Scene(clientBox, 500, 400);

	}

}
