package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.MultiMedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Royce_Lyu
 */
public class LobbyScene extends BaseScene{

    private final static Logger logger = LogManager.getLogger(LobbyScene.class);

    /**
     * The player's channel
     */
    private StringProperty myChannel = new SimpleStringProperty("");

    /**
     * The player's username
     */
    private StringProperty myName = new SimpleStringProperty();

    /**
     * If the player is the host of the channel
     */
    private BooleanProperty host = new SimpleBooleanProperty(false);

    /**
     * A repeating timer requesting current channels
     */
    private Timer timer;

    /**
     * Communicator
     */
    private Communicator communicator;

    /**
     * Hold the channel list
     */
    private ChannelList channelList;

    /**
     * Hold the user List
     */
    private UserList userList;

    /**
     * Hold the chat box
     */
    private ChatBox chatBox;

    /**
     * Scroller Pane
     */
    private ScrollPane scroller;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        this.communicator = gameWindow.getCommunicator();
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {
        this.scene.setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                communicator.send("PART");
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                    timer = null;
                }
                this.gameWindow.startMenu();
            }
        });
        communicator.addListener((message) -> {
            Platform.runLater(() -> {
                handleMessage(message.trim());
            });
        });

        communicator.send("LIST");
        TimerTask refresh = new TimerTask() {
            @Override
            public void run() {
                //Request for the user list
                if(!myChannel.get().equals("")) {
                    communicator.send("USERS");
                }
                //Request for the channel list
                LobbyScene.logger.info("Refreshing channel list");
                communicator.send("LIST");
            }
        };
        timer = new Timer();
        timer.schedule(refresh,0,3000);
    }

    /**
     * Build the layout of the scene
     */
    @Override
    public void build() {
        logger.info("Building {}", this.getClass().getName());
        root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());
        StackPane lobbyPane = new StackPane();
        lobbyPane.setMaxWidth(this.gameWindow.getWidth());
        lobbyPane.setMaxHeight(this.gameWindow.getHeight());
        lobbyPane.getStyleClass().add("lobby-background");
        root.getChildren().add(lobbyPane);
        var mainPane = new BorderPane();
        lobbyPane.getChildren().add(mainPane);

        Text title = new Text("Multiplayer");
        title.getStyleClass().add("title");
        BorderPane.setAlignment(title, Pos.CENTER);
        mainPane.setTop(title);

        //UI of the Channel list
        channelList = new ChannelList();
        mainPane.setLeft(channelList);
        channelList.myChannel.bind(myChannel);

        //UI of the User List
        userList = new UserList();
        userList.visibleProperty().bind(myChannel.isNotEmpty());
        mainPane.setRight(userList);

        //UI of the Chat Box
        chatBox = new ChatBox();
        chatBox.channelName.bind(myChannel);
        chatBox.visibleProperty().bind(myChannel.isNotEmpty());
        BorderPane.setMargin(chatBox, new Insets(20,10,50,10));
        mainPane.setCenter(chatBox);

    }

    /**
     * Join in the selected channel
     * @param channel The selected channel
     */
    public void joinChannel(String channel) {
        if (!channel.equals(myChannel.get())) {
            host.set(false);
            communicator.send("JOIN " + channel);
        } else {
            logger.info("You have already in the group!");
        }
    }

    /**
     * Handle the incoming message
     * @param message incoming message
     */
    public void handleMessage(String message) {
        logger.info("Receiving message: {}", message);
        String[] components = message.split(" ",2);

        //Receiving and processing CHANNELS message
        if (components[0].equals("CHANNELS")) {
            if (components.length > 1) {
                handleChannelList(components[1]);
            } else {
                handleChannelList("");
            }
        }

        //Receiving and processing JOIN message
        if (components[0].equals("JOIN")) {
            host.set(false);
            channelList.addChannel(components[1]);
            myChannel.set(components[1]);
            chatBox.getMsgField().getChildren().clear();
            logger.info("You joined the channel: {}", myChannel.get());
        }

        //Receiving and processing HOST message
        if (components[0].equals("HOST")) {
            logger.info("You are the host of the channel: {}", myChannel);
            host.set(true);
        }

        //Receiving and processing USERS message
        if (components[0].equals("USERS")) {
            logger.info("Processing user list");
            handleUserList(components[1]);
        }

        //Receiving and processing NICK message
        if (components[0].equals("NICK") && components.length > 1) {
            if (!components[1].contains(":")) {
                myName.set(components[1]);
            }
        }

        //Receiving and processing PARTED message
        if (components[0].equals("PARTED")) {
            logger.info("Leaving Channel!");
            myChannel.set("");
        }

        //Receiving and processing MSG message
        if (components[0].equals("MSG")) {
            receiveMSG(components[1]);
        }

        //Receiving and processing START message
        if (components[0].equals("START")) {
            startGame();
        }

        //Receiving and processing ERROR message
        if (components[0].equals("ERROR")) {
            logger.error(components[1]);
            Alert alert = new Alert(Alert.AlertType.ERROR, components[1], new ButtonType[0]);
            alert.showAndWait();
        }
    }

    /**
     * Decode message and add to the chat box
     * @param source messages in the chat box
     */
    private void receiveMSG(String source) {
        String[] components = source.split(":",2);
        TextFlow msg = new TextFlow();
        msg.getStyleClass().add("message");

        //Get current time
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("HH:mm:ss");
        Date date = new Date();
        Text time = new Text("[" + sdf.format(date) + "]");

        Text userName = new Text("<" + components[0] + ">" + ": ");
        Text message = new Text(components[1]);
        msg.getChildren().addAll(new Node[] {time, userName, message});
        chatBox.getMsgField().getChildren().add(msg);
        scroller.setVvalue(1);
    }

    /**
     * Decode User list message and add to user list
     * @param message List of User
     */
    private void handleUserList(String message) {
        String[] components = message.split("\n");
        List<String> users = Arrays.asList(components);
        userList.setUserList(users);
    }

    /**
     * Decode Channel list message and add to channel list
     * @param message incoming Channel List message
     */
    private void handleChannelList(String message) {
        logger.info("Handling channel list message: {}", message);
        String[] components = message.split("\n");
        List<String> channels = Arrays.asList(components);
        channelList.setChannels(channels);
    }

    /**
     * Send the message
     * @param message messages
     */
    public void sendMsg(String message) {
        if (message.startsWith("/")) {
            String[] components = message.split(" ", 2);
            String command = components[0].toLowerCase();
            if (command.equals("/nick") && components.length > 1) {
                this.communicator.send("NICK " + components[1]);
            } else if (command.equals("/start") && host.get()) {
                this.communicator.send("START");
            } else if (command.equals("/part")) {
                this.communicator.send("PART");
            }
        } else {
            this.communicator.send("MSG " + message);
        }
    }

    /**
     * Request to start the game
     */
    public void requestStart() {
        logger.info("Requesting game start");
        this.communicator.send("START");
    }

    /**
     * Start the game
     */
    public void startGame() {
        logger.info("Launch the game");
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }

        gameWindow.startMultiplayerGame();
    }

    public class ChannelList extends VBox {
        private HashMap<String, Text> channelList = new HashMap();
        private StringProperty myChannel = new SimpleStringProperty();

        /**
         * Layout of the channel list
         */
        public ChannelList() {
            setSpacing(10);
            setPrefWidth(180);
            setPadding(new Insets(10,5,10,20));
            getStyleClass().add("channelList");
            Text hostChannel = new Text("Host New Game");
            hostChannel.getStyleClass().add("channelItem");
            getChildren().add(hostChannel);
            TextField newChannelName = new TextField();
            newChannelName.setVisible(false);
            getChildren().add(newChannelName);
            hostChannel.setOnMouseClicked(mouseEvent -> {
                MultiMedia.playAudio("rotate.wav");
                newChannelName.setVisible(true);
                newChannelName.requestFocus();
            });
            newChannelName.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    MultiMedia.playAudio("rotate.wav");
                    communicator.send("CREATE " + newChannelName.getText().trim());
                    communicator.send("LIST");
                    newChannelName.setVisible(false);
                    newChannelName.clear();
                }
            });
            myChannel.addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                    for (Text i : channelList.values()) {
                        i.getStyleClass().remove("selected");
                    }
                    for (Text i : channelList.values()) {
                        if (i.equals(channelList.get(newValue))) {
                            i.getStyleClass().add("selected");
                            logger.info("CSS changes successfully");
                        }
                    }
                }
            });
        }

        /**
         * Add the channel to UI
         * @param channelName The added channel
         */
        public void addChannel(String channelName) {
            if (!channelList.containsKey(channelName)) {
                logger.info("Adding channel {}", channelName);
                Text channelText = new Text(channelName);
                channelText.getStyleClass().add("channelItem");
                getChildren().add(channelText);
                channelText.setOnMouseClicked(mouseEvent -> {
                    LobbyScene.this.joinChannel(channelName);
                });
                channelList.put(channelName, channelText);
            }
        }

        /**
         * Set the channel list
         * @param channels list of channels
         */
        public void setChannels(List<String> channels) {
            Set<String> exist = channelList.keySet();
            logger.info("Set channel list: {}", channels);
            if (exist.size() == channels.size() && exist.containsAll(channels)) {
                return;
            } else {
                List<String> toRemove = new ArrayList();
                for (String i : exist) {
                    if (!channels.contains(i)) {
                        getChildren().remove(channelList.get(i));
                        toRemove.add(i);
                    }
                }

                for (String i : toRemove) {
                    channelList.remove(i);
                }

                for (String channel : channels) {
                    addChannel(channel);
                }
            }
        }

    }

    public class ChatBox extends VBox {
        private StringProperty channelName = new SimpleStringProperty("");
        private VBox msgField;

        /**
         * Layout of chat box
         */
        public ChatBox() {
            getStyleClass().add("gameBox");
            setSpacing(10);
            setPadding(new Insets(5,5,5,5));
            Text channelText = new Text("Current Games");
            channelText.getStyleClass().add("heading");
            getChildren().add(channelText);
            //UI of messages box
            scroller = new ScrollPane();
            scroller.setPrefHeight(gameWindow.getHeight()/2);
            scroller.setMaxHeight(gameWindow.getHeight()/2);
            scroller.setFitToWidth(true);
            scroller.getStyleClass().add("scroller");
            msgField = new VBox();
            msgField.getStyleClass().add("messages");
            getChildren().add(scroller);
            scroller.setContent(msgField);

            //UI of send message Box
            var sendMsgBox = new TextField();
            sendMsgBox.getStyleClass().add("messageBox");
            sendMsgBox.setPromptText("Send a message");
            sendMsgBox.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                    sendMsg(sendMsgBox.getText());
                    sendMsgBox.clear();
                }
            });
            getChildren().add(sendMsgBox);

            //Buttons
            Button partButton = new Button("Leave Game");
            Button startButton = new Button("Start Game");
            HBox buttonsField = new HBox();
            buttonsField.setAlignment(Pos.CENTER);
            buttonsField.setSpacing(255);
            buttonsField.getChildren().add(partButton);
            buttonsField.getChildren().add(startButton);
            getChildren().add(buttonsField);

            //Button action
            partButton.setOnAction(event -> {
                communicator.send("PART");
                communicator.send("LIST");
            });
            startButton.visibleProperty().bind(host);
            startButton.setOnAction(event ->{
                requestStart();
            });

            //Message Instruction
            Text instruction = new Text("Welcome to the lobby\nType /nick NewName to change your name\n" +
                    "Type /part to quit this channel\n" +
                    "Type /start to start the game\n");
            instruction.getStyleClass().add("channelItem");
            getChildren().add(instruction);
        }

        /**
         * Call MsgField
         * @return message box
         */
        public VBox getMsgField() {
            return msgField;
        }
    }

    public class UserList extends VBox {
        private ArrayList<String> players = new ArrayList();

        /**
         * Layout of the user list
         */
        public UserList() {
            getStyleClass().add("playerBox");
            setSpacing(5);
            setPrefWidth(150);
            setPadding(new Insets(5,5,5,5));
        }

        /**
         * Set the user list
         * @param userList list of users
         */
        public void setUserList(List<String> userList) {
            players.clear();
            players.addAll(userList);
            refresh();
        }

        /**
         * Refresh UI of the user list
         */
        public void refresh() {
            getChildren().clear();
            Text title = new Text("USER LIST");
            title.getStyleClass().add("channelItem");
            getChildren().add(title);
            for(String i : players) {
                Text userName = new Text(i);

                //Set my name to Top
                if (i.equals(myName.get())) {
                    userName.getStyleClass().add("myname");
                    getChildren().add(1, userName);
                    continue;
                }
                getChildren().add(userName);
            }
        }
    }
}
