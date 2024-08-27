package main.java;

import java.net.*;
import java.io.*;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Optional;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.concurrent.*;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Client extends Application implements Initializable {

	private String currentStyle;

	private MediaPlayer bgmControl;

	private Media bgm;

	private Image appIcon;

	@FXML
	private ComboBox<String> themesSelector;

	@FXML
	private Canvas BattleCanvas;

	@FXML
	private TextField yourNameField;

	@FXML
	private ComboBox<String> otherPeopleDropdown;

	@FXML
	private Button confirmNameButton;

	@FXML
	private Button Pokemon1;

	@FXML
	private Button Pokemon2;

	@FXML
	private Button Pokemon3;

	@FXML
	private Button Pokemon4;

	@FXML
	private Button Pokemon5;

	@FXML
	private Button Pokemon6;

	@FXML
	private Label toOpponentLabel;

	@FXML
	private TextField toOpponentField;

	@FXML
	private Label toEveryoneLabel;

	@FXML
	private TextField toEveryoneField;

	@FXML
	private Label ipLabel;

	@FXML
	private Label hostStatusLabel;

	@FXML
	private Button hostServerButton;

	@FXML
	private Button closeServerButton;

	@FXML
	private TextField serverConnectField;

	@FXML
	private Button connectToServerButton;

	@FXML
	private Button disconnectFromServerButton;

	@FXML
	private Tab connectionTab;

	@FXML
	private Tab teambuilderTab;

	@FXML
	private Tab battleTab;

	@FXML
	private TabPane tabPane;

	@FXML
	private TextField nameFilter;

	@FXML
	private ComboBox<String> typeFilter;

	@FXML
	private VBox pokemonFilterDisplay;

	@FXML
	private Label opponentNameLabel;

	@FXML
	private Label toOpponentNameLabel;

	@FXML
	private ToggleGroup battleOptions;

	@FXML
	private HBox battleMovesBox;

	@FXML
	private HBox battlePokemonBox;

	@FXML
	private ToggleButton lockInButton;

	@FXML
	private ToggleButton battleMoveOne;

	@FXML
	private ToggleButton battleMoveTwo;

	@FXML
	private ToggleButton battleMoveThree;

	@FXML
	private ToggleButton battleMoveFour;

	@FXML
	private ToggleButton battlePokeOne;

	@FXML
	private ToggleButton battlePokeTwo;

	@FXML
	private ToggleButton battlePokeThree;

	@FXML
	private ToggleButton battlePokeFour;

	@FXML
	private ToggleButton battlePokeFive;

	@FXML
	private ToggleButton battlePokeSix;

	private Socket clientSocket;
	private Server server;
	private PrintWriter out;
	private Reader read;
	private boolean stopReading;
	private Stage s;
	private Scene baseScene;
	private String connectDest = "";
	private Pokemon[] team = { null, null, null, null, null, null };
	private Move[][] possibleMoves = new Move[6][];
	private boolean autofire = false;
	private int activePokemon = -1;

	private Color light;
	private Color dark;

	private int myIndex;

	private int indexOfPokemonToReplace = -1;
	private int indexOfPokemonToGetMovesOf = -1;
	private int possibleMovesIndex = -1;
	private int indexOfMoveToReplaceWith = -1;
	private boolean host = false;
	private Stage secretStage;

	/** Clears the battle canvas with a bright rectangle*/
	public void clearCanvas() {
		GraphicsContext gc = BattleCanvas.getGraphicsContext2D();
		gc.setFill(light);
		gc.fillRect(0, 0, BattleCanvas.getWidth(), BattleCanvas.getHeight());
	}

	/** Clears the battle canvas with a dark rectangle*/
	public void clearCanvasDark() {
		GraphicsContext gc = BattleCanvas.getGraphicsContext2D();
		gc.setFill(dark);
		gc.fillRect(0, 0, BattleCanvas.getWidth(), BattleCanvas.getHeight());
	}

	/** Draws the pokemon given, its name, and its healthbar onto the canvas in either the user's or opponent's slot
	*
	*	@param name The name of the pokemon to draw
	*	@param opponent Whether or not the pokemon should be drawn on your side or your opponent's
	*	@param percentHealth The percent health of the pokemon you're drawing
	*/
	public void drawPokemon(String name, boolean opponent, double percentHealth) {
		double ch = BattleCanvas.getHeight();
		double cw = BattleCanvas.getWidth();
		double min = ch;
		if (min > cw) {
			min = cw;
		}
		GraphicsContext gc = BattleCanvas.getGraphicsContext2D();
		gc.setFill(Color.rgb(248, 248, 248));
		gc.setStroke(Color.rgb(175, 175, 175));
		gc.setLineWidth(3);

		try {
			ImageView iv = getPokemonSprite(name);
			Image i = iv.getImage();
			double ih = i.getHeight();
			double iw = i.getWidth();
			double ratio = ih / iw;
			double size = min / 3;
			double x, y, px, py, ph, pw;
			if (opponent) {
				x = cw - size - 50;
				y = 50;
				iw = size;
				ih = size * ratio;
				px = 25;
				py = y + 25;
				pw = x - 25;

			} else {
				x = 50;
				y = ch - size * ratio - 50;
				iw = size;
				ih = size * ratio;
				px = x + iw;
				py = y + 25;
				pw = cw - px - 25;
			}
			ph = 50;
			gc.fillOval(x - 50, y + ih - 25, iw + 100, 35);
			gc.strokeOval(x - 50, y + ih - 25, iw + 100, 35);
			gc.drawImage(i, x, y, iw, ih);
			gc.fillRect(px, py, pw, ph);
			gc.strokeRect(px, py, pw, ph);
			gc.setFill(Color.GREEN);
			gc.fillRect(px + 5, py + 30, percentHealth * (pw - 10), 15);
			gc.strokeRect(px + 5, py + 30, pw - 10, 15);
			gc.setFont(new Font("Courier", 20));
			gc.setFill(Color.BLACK);
			gc.setLineWidth(1);
			gc.fillText(name, px + 5, py + 25, pw - 10);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** TODO Daniel
	*
	*/
	public void chooseAPokemonToReplace(Pokemon p) {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.getDialogPane().getStylesheets().add(currentStyle);
		dialog.setTitle("Team");
		String pn = p.pokeName;
		dialog.setHeaderText("Replace a Pokemon with " + p.pokeName.replace("_", " "));
		indexOfPokemonToReplace = -1;
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
		HBox vb = new HBox();

		dialog.getDialogPane().setContent(vb);
		ToggleButton[] tbs = new ToggleButton[6];
		HashMap<ToggleButton, Integer> indices = new HashMap<ToggleButton, Integer>();
		for (int i = 0; i < 6; i++) {
			ToggleButton b = new ToggleButton();

			indices.put(b, i);
			String name = ((team[i] == null) ? "N/A" : team[i].pokeName);
			b.setText(name);
			b.setContentDisplay(ContentDisplay.TOP);
			try {
				b.setGraphic(getPokemonSprite(name));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			vb.getChildren().add(b);
			tbs[i] = b;
			b.setOnAction(event -> {
				if (indexOfPokemonToReplace != -1) {
					tbs[indexOfPokemonToReplace].setSelected(false);
				}
				indexOfPokemonToReplace = indices.get(b);

				b.setSelected(true);

			});
		}
		// Make me pretty
		try {
			Stage diagWindow = (Stage) dialog.getDialogPane().getScene().getWindow();
			diagWindow.getIcons().add(appIcon);
		} catch (Exception e) {
			/* Don't worry bout it bruddah */}
		dialog.showAndWait().ifPresent(response -> {
			if (response == ButtonType.APPLY) {
				if (indexOfPokemonToReplace == -1) {
					return;
				}
				team[indexOfPokemonToReplace] = p;
				indexOfPokemonToGetMovesOf = indexOfPokemonToReplace;
				out.println("xpm " + p.pokeName + " " + indexOfPokemonToGetMovesOf);
				indexOfPokemonToReplace = -1;
			}
			if (response == ButtonType.CANCEL) {
				indexOfPokemonToReplace = -1;
			}
		});
		displayTeam();
	}

	/**	TODO Daniel
	*
	*/
	public void displayAddPokemonDialog(String s) {
		Pokemon p = Pokemon.deStringify(s);
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.getDialogPane().getStylesheets().add(currentStyle);
		dialog.setTitle("Pokemon");
		dialog.setHeaderText(p.pokeName.replace("_", " "));
		ButtonType done = new ButtonType("Add to Team", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(done, ButtonType.CANCEL);

		Node d = dialog.getDialogPane().lookupButton(done);
		d.setId("customBtn");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		dialog.getDialogPane().setContent(grid);

		try {
			grid.add(getPokemonSprite(p.pokeName), 0, 0);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		grid.add(new Label("Types: " + p.getTypes()), 0, 1);
		grid.add(new Label("Stats:"), 0, 2);
		grid.add(new Label("HP: " + p.HP), 1, 3);
		grid.add(new Label("Attack: " + p.Attack), 1, 4);
		grid.add(new Label("Special Attack: " + p.SpAttack), 1, 5);
		grid.add(new Label("Defense: " + p.Defense), 1, 6);
		grid.add(new Label("Special Defense: " + p.SpDefense), 1, 7);
		grid.add(new Label("Speed: " + p.Speed), 1, 8);

		// Make me pretty
		try {
			Stage diagWindow = (Stage) dialog.getDialogPane().getScene().getWindow();
			diagWindow.getIcons().add(appIcon);
		} catch (Exception e) {
			/* Don't worry bout it bruddah */e.printStackTrace();
		}

		dialog.showAndWait().ifPresent(response -> {
			if (response.getButtonData() == ButtonData.OK_DONE) {
				chooseAPokemonToReplace(p);
			}
			if (response == ButtonType.CANCEL) {

			}
		});

	}

	/** TODO Daniel
	*
	*/
	public void changePokemonMove(String move, int indexOfPokemon) {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Moves");
		dialog.setHeaderText("Replace " + move.replace("_", " "));
		ButtonType done = new ButtonType("Replace", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().setAll(done, ButtonType.CANCEL);
		dialog.getDialogPane().getStylesheets().add(currentStyle);

		Node d = dialog.getDialogPane().lookupButton(done);
		d.setId("customBtn");

		ScrollPane sp = new ScrollPane();
		sp.setPrefViewportHeight(400.0);
		Accordion ac = new Accordion();
		sp.setContent(ac);
		indexOfMoveToReplaceWith = -1;
		// ToggleButton[] tbs = new ToggleButton[possibleMoves[indexOfPokemon].length];
		dialog.getDialogPane().setContent(sp);
		HashMap<String, Integer> indices = new HashMap<String, Integer>();
		for (int i = 0; i < possibleMoves[indexOfPokemon].length; i++) {
			// ToggleButton tb = new ToggleButton();
			VBox vb = new VBox();
			TitledPane tp = new TitledPane(possibleMoves[indexOfPokemon][i].moveName.replace("_", " "), vb);
			ac.getPanes().add(tp);
			indices.put(possibleMoves[indexOfPokemon][i].moveName, i);
			// tb.setText(possibleMoves[indexOfPokemon][i].moveName);
			// tbs[i] = tb;
			vb.getChildren()
					.addAll(new Label("Type: "
							+ possibleMoves[indexOfPokemon][i].type), new Label(
									" Power: "
											+ possibleMoves[indexOfPokemon][i].power),
							new Label(
									" Accuracy: "
											+ possibleMoves[indexOfPokemon][i].accuracy),
							new Label(" PP: "
									+ possibleMoves[indexOfPokemon][i].maxPP),
							new Label(" Priority: "
									+ possibleMoves[indexOfPokemon][i].priority),
							new Label(" Category: "
									+ possibleMoves[indexOfPokemon][i].category.toString()));
			tp.setOnMouseClicked(event -> {
				indexOfMoveToReplaceWith = indices.get(tp.getText().replace(" ", "_"));
			});
		}

		// Make me pretty
		try {
			Stage diagWindow = (Stage) dialog.getDialogPane().getScene().getWindow();
			diagWindow.getIcons().add(appIcon);
		} catch (Exception e) {
			/* Don't worry bout it bruddah */e.printStackTrace();
		}

		dialog.showAndWait().ifPresent(response -> {
			if (response.getButtonData() == ButtonData.OK_DONE) {
			}
			if (response == ButtonType.CANCEL) {
				indexOfMoveToReplaceWith = -1;
			}
		});
	}

	/** TODO Daniel
	*
	*/
	public void displayPokemonDialog(int teamIndex) {
		Pokemon p = team[teamIndex];
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.getDialogPane().getStylesheets().add(currentStyle);
		dialog.setTitle("Pokemon");
		dialog.setHeaderText(p.pokeName.replace("_", " "));
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		dialog.getDialogPane().setContent(grid);
		try {
			grid.add(getPokemonSprite(p.pokeName), 0, 0);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		grid.add(new Label("Types: " + p.getTypes()), 0, 1);
		grid.add(new Label("Stats:"), 0, 2);
		grid.add(new Label("HP: " + p.HP), 1, 3);
		grid.add(new Label("Attack: " + p.Attack), 1, 4);
		grid.add(new Label("Special Attack: " + p.SpAttack), 1, 5);
		grid.add(new Label("Defense: " + p.Defense), 1, 6);
		grid.add(new Label("Special Defense: " + p.SpDefense), 1, 7);
		grid.add(new Label("Speed: " + p.Speed), 1, 8);

		grid.add(new Label("Moves: "), 0, 9);
		HashMap<Button, Integer> indices = new HashMap<Button, Integer>();
		for (int i = 0; i < 4; i++) {
			Button b = new Button();
			b.setText(((p.knownMoves.get(i) == null) ? "N/A" : p.knownMoves.get(i).moveName.replace("_", " ")));
			indices.put(b, i);
			grid.add(b, 1, 10 + i);
			b.setOnAction(event -> {
				int x = indices.get(b);
				changePokemonMove(((p.knownMoves.get(x) == null) ? "N/A" : p.knownMoves.get(x).moveName),
						teamIndex);
				if (indexOfMoveToReplaceWith != -1) {
					p.knownMoves.set(x, possibleMoves[teamIndex][indexOfMoveToReplaceWith]);
					b.setText(((p.knownMoves.get(x) == null) ? "N/A" : p.knownMoves.get(x).moveName.replace("_", " ")));
				}
			});
		}

		// Make me pretty
		try {
			Stage diagWindow = (Stage) dialog.getDialogPane().getScene().getWindow();
			diagWindow.getIcons().add(appIcon);
		} catch (Exception e) {
			/* Don't worry bout it bruddah */e.printStackTrace();
		}

		dialog.showAndWait().ifPresent(response -> {
			if (response == ButtonType.CLOSE) {

			}
		});

	}

	/** Displays the Dialog asking the user if they want to connect to a player*/
	public void displayConnectionDialog() {
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		dialog.getDialogPane().getStylesheets().add(currentStyle);
		dialog.setTitle("Connect?");
		dialog.setHeaderText(connectDest + " would like to connect");
		ButtonType accept = new ButtonType("Accept", ButtonBar.ButtonData.YES);
		ButtonType reject = new ButtonType("Reject", ButtonBar.ButtonData.NO);
		dialog.getDialogPane().getButtonTypes().addAll(accept, reject);

		Node d = dialog.getDialogPane().lookupButton(accept);
		d.setId("customBtn");
		d = dialog.getDialogPane().lookupButton(reject);
		d.setId("customBtn");

		// Make me pretty
		try {
			Stage diagWindow = (Stage) dialog.getDialogPane().getScene().getWindow();
			diagWindow.getIcons().add(appIcon);
		} catch (Exception e) {
			/* Don't worry bout it bruddah */e.printStackTrace();
		}

		dialog.showAndWait().ifPresent(response -> {
			if (response.getButtonData() == ButtonBar.ButtonData.YES) {
				if (!connectDest.equals("")) {
					send("connectAccept " + connectDest);
					opponentNameLabel.setText(connectDest);
					toOpponentNameLabel.setText("To " + connectDest);
					confirmNameButton.setDisable(true);
				}
				connectDest = "";
			} else if (response.getButtonData() == ButtonBar.ButtonData.NO) {
				System.out.println("nuh uh");
				send("connectReject " + connectDest);
				connectDest = "";
			}
		});
	}

	/** TODO Daniel*/
	public void sendFilter() {
		String nf = nameFilter.getText();
		if (nf.length() <= 0) {
			nf = "Any";
		} else {
			nf.toLowerCase();
			nf = nf.substring(0, 1).toUpperCase() + nf.substring(1);
		}
		out.println(
				"xf " + nf + " " + ((typeFilter.getValue() == null) ? "Any" : typeFilter.getValue()));
	}

	/** TODO Daniel*/
	public void displayTeam() {

		Pokemon1.setText(((team[0] == null) ? "N/A" : team[0].pokeName.replace("_", " ")));
		try {
			Pokemon1.setGraphic(getPokemonSprite(((team[0] == null) ? "N/A" : team[0].pokeName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Pokemon1.setOnAction(event -> {
			if (team[0] != null) {
				displayPokemonDialog(0);
			}
		});

		Pokemon2.setText(((team[1] == null) ? "N/A" : team[1].pokeName.replace("_", " ")));
		try {
			Pokemon2.setGraphic(getPokemonSprite(((team[1] == null) ? "N/A" : team[1].pokeName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Pokemon2.setOnAction(event -> {
			if (team[1] != null) {
				displayPokemonDialog(1);
			}
		});

		Pokemon3.setText(((team[2] == null) ? "N/A" : team[2].pokeName.replace("_", " ")));
		try {
			Pokemon3.setGraphic(getPokemonSprite(((team[2] == null) ? "N/A" : team[2].pokeName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Pokemon3.setOnAction(event -> {
			if (team[2] != null) {
				displayPokemonDialog(2);
			}
		});

		Pokemon4.setText(((team[3] == null) ? "N/A" : team[3].pokeName.replace("_", " ")));
		try {
			Pokemon4.setGraphic(getPokemonSprite(((team[3] == null) ? "N/A" : team[3].pokeName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Pokemon4.setOnAction(event -> {
			if (team[3] != null) {
				displayPokemonDialog(3);
			}
		});

		Pokemon5.setText(((team[4] == null) ? "N/A" : team[4].pokeName.replace("_", " ")));
		try {
			Pokemon5.setGraphic(getPokemonSprite(((team[4] == null) ? "N/A" : team[4].pokeName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Pokemon5.setOnAction(event -> {
			if (team[4] != null) {
				displayPokemonDialog(4);
			}
		});

		Pokemon6.setText(((team[5] == null) ? "N/A" : team[5].pokeName.replace("_", " ")));
		try {
			Pokemon6.setGraphic(getPokemonSprite(((team[5] == null) ? "N/A" : team[5].pokeName)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Pokemon6.setOnAction(event -> {
			if (team[5] != null) {
				displayPokemonDialog(5);
			}
		});
	}

	/** Retrieves the pokemon sprite corresponding to <name>
	*
	*	@param name The name of the pokemon to find the sprite for
	*	@return an ImageView containing the sprite of the pokemon given
	*/
	public ImageView getPokemonSprite(String name) throws FileNotFoundException {
		String n = name;
		if (name.equals("N/A")) {
			n = "missingno";
		}
		n = n.replace("_", "-");
		n = n.replace(":", "");
		File f;
		FileInputStream fis;
		try {
			f = new File("../src/main/resources/sprites/" + n + ".png");
			fis = new FileInputStream(f);
		} catch (Exception e) {
			n = "missingno";
			f = new File("../src/main/resources/sprites/" + n + ".png");
			fis = new FileInputStream(f);
		}
		Image img = new Image(fis);
		ImageView view = new ImageView(img);
		view.setFitHeight(80);
		view.setPreserveRatio(true);
		return view;

	}

	/** Gets the move/switch index and target from the Battle UI buttons pressed*/
	public String[] getOptionAndTarget() {
		String[] output = { "", "" };
		// TODO: don't always target 0 for moves
		if (battleMoveOne.isSelected()) {
			output[0] = "0";
			output[1] = "" + (1 - myIndex);
		} else if (battleMoveTwo.isSelected()) {
			output[0] = "1";
			output[1] = "" + (1 - myIndex);
		} else if (battleMoveThree.isSelected()) {
			output[0] = "2";
			output[1] = "" + (1 - myIndex);
		} else if (battleMoveFour.isSelected()) {
			output[0] = "3";
			output[1] = "" + (1 - myIndex);
			;
		} else if (battlePokeOne.isSelected()) {
			output[0] = "4";
			output[1] = "0";
		} else if (battlePokeTwo.isSelected()) {
			output[0] = "4";
			output[1] = "1";
		} else if (battlePokeThree.isSelected()) {
			output[0] = "4";
			output[1] = "2";
		} else if (battlePokeFour.isSelected()) {
			output[0] = "4";
			output[1] = "3";
		} else if (battlePokeFive.isSelected()) {
			output[0] = "4";
			output[1] = "4";
		} else if (battlePokeSix.isSelected()) {
			output[0] = "4";
			output[1] = "5";
		}
		return output;
	}

	/**	Locks in the move/switch chosen by the user with the Battle UI buttons*/
	public void battleLockedIn() {
		if (lockInButton.isSelected()) { // if locking in
			lockInButton.setText("Change Decision");
			battleMovesBox.setDisable(true);
			battlePokemonBox.setDisable(true);
			// get new active mon (if any)
			if (battlePokeOne.isSelected()) {
				activePokemon = 0;
			} else if (battlePokeTwo.isSelected()) {
				activePokemon = 1;
			} else if (battlePokeThree.isSelected()) {
				activePokemon = 2;
			} else if (battlePokeFour.isSelected()) {
				activePokemon = 3;
			} else if (battlePokeFive.isSelected()) {
				activePokemon = 4;
			} else if (battlePokeSix.isSelected()) {
				activePokemon = 5;
			}
			// TODO: make it so you aren't always targeting your one opponent
			String[] toSend = getOptionAndTarget();
			send("move " + toSend[0] + " " + toSend[1]);
		} else { // if changing decision
			lockInButton.setText("Lock In");
			battleMovesBox.setDisable(false);
			battlePokemonBox.setDisable(false);
		}
	}

	/** Sends the message in the everyone text field to everyone connected to the same server
	*
	*	@param e The key pressed to trigger this method
	*/
	public void sendEveryone(KeyEvent e) {
		if (e.getCode().equals(KeyCode.ENTER) && !toEveryoneField.getText().equals("")) {
			send("everyone " + yourNameField.getText() + ": " + toEveryoneField.getText());
			toEveryoneField.setText("");
		}
	}

	/** Sends the message in the opponent text field to everyone connected this client
	*
	*	@param e The key pressed to trigger this method
	*/
	public void sendText(KeyEvent e) {
		if (e.getCode().equals(KeyCode.ENTER) && !toOpponentField.getText().equals("")) {
			send("dm " + yourNameField.getText() + ": " + toOpponentField.getText());
			if (toOpponentLabel.getText().equals("")) {
				toOpponentLabel.setText(yourNameField.getText() + ": " + toOpponentField.getText());
			} else {
				toOpponentLabel.setText(
						toOpponentLabel.getText() + '\n' + yourNameField.getText() + ": " + toOpponentField.getText());
			}
			toOpponentField.setText("");
		}
	}

	/** Checks to ensure the client has a valid team, then sends a server request to connect to the player selected in the dropdown*/
	public void connectButtonClicked() {
		if (otherPeopleDropdown.getValue() != null) {
			// making sure there's at least one pokemon
			boolean found = false;
			for (int i = 0; i < 6; i++) {
				if (team[i] != null) {
					found = true;
					// making sure all pokemon have at least one move
					boolean moveFound = false;
					for (Move move : team[i].knownMoves) {
						if (move != null) {
							moveFound = true;
							break;
						}
					}
					if (!moveFound) {
						return;
					}
				}
			}
			if (!found) {
				return;
			}
			send("connect " + otherPeopleDropdown.getValue());
			confirmNameButton.setDisable(true);
		}
	}

	/** Sets up the type filter combobox*/
	public void setupTypeFilter() {
		typeFilter.getItems().addAll("Any", "Bug", "Dragon", "Fire", "Flying", "Psychic", "Steel", "Water",
				"Dark", "Grass", "Ground", "Ice", "Fairy", "Ghost", "Poison", "Electric", "Rock", "Fighting", "Normal");
		typeFilter.setValue("Any");
	}

	/** Sets up the theme filter*/
	public void setupThemesFilter() {
		themesSelector.getItems().addAll("Default", "Blue", "Yellow", "Windows XP");
		themesSelector.setValue("Default");
	}

	/** Initializes the GUI
	*
	*	@param url The URL needed to override
	*	@param rb The ResourceBundle needed to override
	*/
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// this is where the main "driver code goes" pretend that
		// anything that happens in main and start didnt actually
		// happen A.K.A. any variables/methods set up or called
		// there DO NOT EXIST
		// Idk y it works that way but it does as far as i have seen
		// that's fucking stupid what the fuck
		setupTypeFilter();
		setupThemesFilter();
		// sends your new name to the server to be distributed to the other clients
		currentStyle = "/font.css";
		confirmNameButton.setOnAction(event -> {
			if (!yourNameField.getText().toString().contains(" ")) {
				send("name " + yourNameField.getText());
			} else {
				Alert no = new Alert(Alert.AlertType.WARNING);
				no.setHeaderText("nuh uh");
				no.show();
				// Make me pretty
				try {
					Stage diagWindow = (Stage) no.getDialogPane().getScene().getWindow();
					diagWindow.getIcons().add(appIcon);
					no.getDialogPane().getStylesheets().add(currentStyle);
					Node d = no.getDialogPane().lookupButton(no.getButtonTypes().get(0));
					d.setId("customBtn");
				} catch (Exception e) {
					/* Don't worry bout it bruddah */}
			}
		});
		nameFilter.setOnKeyTyped(event -> {
			sendFilter();
		});
		typeFilter.setOnAction(event -> {
			sendFilter();
		});
		displayTeam();
		initializeHostTab();
		// battle options listener
		battleOptions.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
				// don't let client deselect option
				if (!autofire) {
					if (newToggle == null) {
						((ToggleButton) oldToggle).fire();
					} else {
						lockInButton.setDisable(false);
					}
				}
			}
		});
		try {
			File imageFile = new File("../src/main/resources/icons/pokeIcon.png");
			appIcon = new Image(new FileInputStream(imageFile));
		} catch (Exception e) {/*If it don't work it really ain't that deep dawgie*/}
		// setting colors
		light = Color.rgb(119, 15, 15);
		dark = Color.rgb(68, 10, 10);
	}

	/** Initializes the text in the Server Info tab*/
	public void initializeHostTab() {
		try {
			ipLabel.setText("Your IP: " + Inet4Address.getLocalHost().getHostAddress() + ' ');
		} catch (Exception e) {
		}
	}

	/** Tries to create a new Server, and connects to it if successful*/
	public void hostServer() {
		secretStage = (Stage) tabPane.getScene().getWindow();
		secretStage.setOnCloseRequest(event -> {
			event.consume();
			if (!clientSocket.isClosed()) {
				send("quit exit");
			} else {
				secretStage.close();
			}
		});
		if (host) {
			System.out.println("already hosting dumbass");
			return;
		}
		try { // try creating a server
			Server server = new Server(7653);
			host = true;
		} catch (Exception e) {
			hostStatusLabel.setText("Could not host server");
			return;
		}
		hostStatusLabel.setText("Hosting");
		try {// try connecting to the server
			clientSocket = new Socket("localhost", 7653);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			host = false;
			e.printStackTrace();
			System.out.println("Client failed");
		}
		try {// try to get the read/write info for the server
			connect();
		} catch (Exception e) {
			host = false;
			e.printStackTrace();
			System.out.println("connect method failed");
		}
		connectionTab.setDisable(false);
		teambuilderTab.setDisable(false);
		battleTab.setDisable(false);
		closeServerButton.setDisable(false);
		System.out.println(host);
		hostServerButton.setDisable(true);
		connectToServerButton.setDisable(true);
	}

	/** Tries to connect to a Server at the IP given in the text field*/
	public void connectToServer() {
		secretStage = (Stage) tabPane.getScene().getWindow();
		secretStage.setOnCloseRequest(event -> {
			event.consume();
			if (!clientSocket.isClosed()) {
				send("quit exit");
			} else {
				secretStage.close();
			}
		});
		try {// try connecting to the server
			clientSocket = new Socket(serverConnectField.getText(), 7653);
		} catch (IOException e) {
			hostStatusLabel.setText("Failed to connect to server");
			connectionTab.setDisable(true);
			teambuilderTab.setDisable(true);
			battleTab.setDisable(true);
			return;
		}
		try {// try to get the read/write info for the server
			connect();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("connect method failed");
			return;
		}
		hostStatusLabel.setText("Connected!");
		connectionTab.setDisable(false);
		teambuilderTab.setDisable(false);
		battleTab.setDisable(false);
		hostServerButton.setDisable(true);
		connectToServerButton.setDisable(true);
		disconnectFromServerButton.setDisable(false);
	}

	/** Closes and resets all connections and variables, and returns the client to the Server Info tab*/
	public void exitServer() {
		read.cancel();
		stopReading = true;
		try {
			clientSocket.close();
		} catch (Exception e) {
		}
		out.close();
		read = null;
		connectionTab.setDisable(true);
		teambuilderTab.setDisable(true);
		battleTab.setDisable(true);
		tabPane.getSelectionModel().select(0);
		hostStatusLabel.setText("Server closed");
		disconnectFromServerButton.setDisable(true);
		closeServerButton.setDisable(true);
		hostServerButton.setDisable(false);
		connectToServerButton.setDisable(false);
		confirmNameButton.setDisable(false);
		try {
			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		host = false;
	}

	/** Sends the signal to close the server*/
	public void closeServer() {
		send("quit disconnect");
		host = false;
	}

	/** Sends the signal to disconnect the client*/
	public void closeClient() {
		send("quit disconnect");
	}

	/** Creates and shows the Dialog to save the current team to a new file*/
	public void saveTeam() {
		// make dialog
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		dialog.getDialogPane().getStylesheets().add(currentStyle);
		ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
		Node d = dialog.getDialogPane().lookupButton(save);
		d.setId("customBtn");
		dialog.setTitle("Save Team");
		dialog.setHeaderText("Team Name");
		TextField fileField = new TextField();
		dialog.getDialogPane().setContent(fileField);

		// Make me pretty
		try {
			Stage diagWindow = (Stage) dialog.getDialogPane().getScene().getWindow();
			diagWindow.getIcons().add(appIcon);
		} catch (Exception e) {
			/* Don't worry bout it bruddah */}

		dialog.showAndWait().ifPresent(response -> {
			if (response.getButtonData() == ButtonData.OK_DONE) {
				// do file
				String filename = "../src/main/resources/teams/";
				if (fileField.getText().isEmpty()) {
					filename += "NewTeam.txt";
				} else {
					filename += fileField.getText().replace(" ", "_").replace("[\\\"/*<>?+:]", "").replace("|", "")
							+ ".txt";
				}
				PrintWriter pw = null;
				try {
					pw = new PrintWriter(filename);
					for (int i = 0; i < 6; i++) {
						if (team[i] == null) {
							pw.println("null");
						} else {
							pw.println(team[i].stringifyWithMoves());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						pw.close();
					} catch (Exception e) {
					}
				}
			}
		});
	}

	/** Creates and shows the Dialog to load a team from a file*/
	public void loadTeam() {
		// make dialog
		Dialog<ButtonType> dialog = new Dialog<ButtonType>();
		dialog.getDialogPane().getStylesheets().add(currentStyle);
		ButtonType load = new ButtonType("Load", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(load, ButtonType.CANCEL);
		Node d = dialog.getDialogPane().lookupButton(load);
		d.setId("customBtn");
		dialog.setTitle("Load Team");
		dialog.setHeaderText("Choose Team");
		ComboBox<String> fileBox = new ComboBox<String>();
		dialog.getDialogPane().setContent(fileBox);
		// finding files
		File dir = new File("../src/main/resources/teams");
		File[] teams = dir.listFiles();
		for (File team : teams) {
			if (team.isFile()) {
				fileBox.getItems().add(team.getName().replace(".txt", ""));
			}
		}

		// Make me pretty
		try {
			Stage diagWindow = (Stage) dialog.getDialogPane().getScene().getWindow();
			diagWindow.getIcons().add(appIcon);
		} catch (Exception e) {
			/* Don't worry bout it bruddah */}

		dialog.showAndWait().ifPresent(response -> {
			if (response.getButtonData() == ButtonData.OK_DONE) {
				// do file
				BufferedReader br = null;
				try {
					br = new BufferedReader(
							new FileReader("../src/main/resources/teams/" + fileBox.getValue() + ".txt"));
					for (int i = 0; i < 6; i++) {
						String line = br.readLine();
						if (line == null) {
							team[i] = null;
						} else {
							try {
								team[i] = Pokemon.deStringifyWithMoves(line);
								indexOfPokemonToGetMovesOf = i;
								send("xpm " + team[i].pokeName + " " + indexOfPokemonToGetMovesOf);
							} catch (Exception e) {
								team[i] = null;
							}
						}
					}
					indexOfPokemonToGetMovesOf = -1;
					// resetting teambuilder buttons
					displayTeam();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						br.close();
					} catch (Exception e) {
					}
				}
			}
		});
	}

	/** Parses the input message read from the server
	*
	*	@param line The message received from the Server split on space
	*/
	public void parseRead(String[] line) {
		if (line[0].equals("names")) {
			// reset names in connection dropdown as one has changed
			otherPeopleDropdown.getItems().clear();
			for (int i = 1; i < line.length; i++) {
				otherPeopleDropdown.getItems().add(line[i]);
			}
		} else if (line[0].equals("GameEnded")) {
			if (bgmControl != null)
				bgmControl.dispose();
			activePokemon = -1;
			// reset buttons
			autofire = true;
			if (lockInButton.isSelected()) {
				lockInButton.fire();
			}
			lockInButton.setDisable(true);
			battleMoveOne.fire();
			if (battleMoveOne.isSelected()) {
				battleMoveOne.fire();
			}
			autofire = false;
			// clear battle scene
			clearCanvasDark();
			if (line[1].equals("disconnect")) { // ended on a dc instead of a KO
				// resetting pokemon
				for (int i = 0; i < 6; i++) {
					if (team[i] != null) {
						team[i].reset();
					}
				}
				Alert a = new Alert(Alert.AlertType.INFORMATION);
				try {
					Stage alertWindow = (Stage) a.getDialogPane().getScene().getWindow();
					alertWindow.getIcons().add(appIcon);
					a.getDialogPane().getStylesheets().add(currentStyle);
					Node d = a.getDialogPane().lookupButton(a.getButtonTypes().get(0));
					d.setId("customBtn");
				} catch (Exception e) {
					/* Not really that important */}
				a.setTitle("Victory");
				a.setContentText("You Won!");
				// a.show();
			} else {
				String[] monData = line[1].split("&");
				for (int i = 0; i < monData.length; i++) {
					if (i == myIndex) {
						String[] parts = monData[i].split("#");
						try {
							int myMon = Integer.parseInt(parts[1]);
							String context = (myMon == -1 ? "You Lost" : "You Won!");

							Dialog<Integer> endDialog = new Dialog<>();
							try {
								Stage diagWindow = (Stage) endDialog.getDialogPane().getScene().getWindow();
								diagWindow.getIcons().add(appIcon);
								endDialog.getDialogPane().getStylesheets().add(currentStyle);
							} catch (Exception e) {
								/* bruddah */ e.printStackTrace();
							}
							endDialog.setTitle("Game Over");
							endDialog.setContentText(context);
							ButtonType disconnectButton = new ButtonType("Disconnect", ButtonData.OK_DONE);
							endDialog.getDialogPane().getButtonTypes().addAll(disconnectButton, ButtonType.CANCEL);
							Node d = endDialog.getDialogPane().lookupButton(disconnectButton);
							d.setId("customBtn");

							endDialog.setResultConverter(dialogButton -> {
								if (dialogButton == disconnectButton) {
									return 1;
								}
								return null;
							});

							Optional<Integer> result = endDialog.showAndWait();
							result.ifPresent(button -> {
								if (button == 1) {
									System.out.println("Bro wants to leave");
									closeClient();
								}
							});
						} catch (Exception e) {
							System.out.println("uh oh");
						}
					}
				}
			}
		} else if (line[0].equals("recvState")) {
			clearCanvas();
			// set up next turn
			String[] monData = line[1].split("&");
			for (int i = 0; i < monData.length; i++) {
				String[] parts = monData[i].split("#");
				if (i == myIndex) {
					try {
						int internalMon = Integer.parseInt(parts[1]);
						if (internalMon == -1) {
							System.out.println("-1 index");
						} else {
							Pokemon active = team[internalMon];
							if (internalMon != activePokemon) {
								team[activePokemon].kill();
							}
							activePokemon = internalMon;
							if (active == null) {
								System.out.println("null mon");
							} else {
								resetBattleButtons();
							}

						}

					} catch (Exception e) {

					}
				}
				// display chat
				// TODO: make compatible with 2v2 info
				Pokemon tempP = Pokemon.monFromState(parts[0]);
				String mes = "";
				if (i == myIndex) {
					mes += "Your ";
					drawPokemon(tempP.pokeName, false, ((float) tempP.currentHP) / tempP.getHP());

				} else {
					mes += "Opponent's ";
					drawPokemon(tempP.pokeName, true, ((float) tempP.currentHP) / tempP.getHP());
				}
				System.out.println(tempP.currentHP + " " + tempP.getHP());
				mes += tempP.pokeName.replace("_", " ") + " is at "
						+ (int) (100 * (tempP.currentHP * 1.0 / tempP.getHP())) + "% health";
				if (toOpponentLabel.getText().equals("")) {
					toOpponentLabel.setText(mes);
				} else {
					toOpponentLabel.setText(toOpponentLabel.getText() + '\n' + mes);
				}
			}
			// reset buttons
			autofire = true;
			if (lockInButton.isSelected()) {
				lockInButton.fire();
			}
			lockInButton.setDisable(true);
			battleMoveOne.fire();
			if (battleMoveOne.isSelected()) {
				battleMoveOne.fire();
			}
			autofire = false;
		} else if (line[0].equals("nameInit")) {
			// initialize your name and opponent names
			yourNameField.setText(line[1]);
			// send new name to everyone else
			send("name " + line[1]);
			// initialize opponent names in dropdown
			if (line.length > 2) { // checking if there are other players
				for (int i = 2; i < line.length; i++) {
					otherPeopleDropdown.getItems().add(line[i]);
				}
			}
		} else if (line[0].equals("connect")) {
			// someone wants to connect to you
			// making sure there's at least one pokemon
			boolean found = false;
			for (int i = 0; !found && i < 6; i++) {
				if (team[i] != null) {
					found = true;
					// making sure all pokemon have at least one move
					boolean moveFound = false;
					for (Move move : team[i].knownMoves) {
						if (move != null) {
							moveFound = true;
							break;
						}
					}
					if (!moveFound) {
						found = false;
					}
				}
			}
			if (found) {
				connectDest = line[1];
				displayConnectionDialog();
				confirmNameButton.setDisable(true);
			} else {
				send("connectReject " + line[1]);
			}
		} else if (line[0].equals("connectAccept")) {
			// connection accepted
			opponentNameLabel.setText(line[1]);
			toOpponentNameLabel.setText("To " + line[1]);
			confirmNameButton.setDisable(false);
		} else if (line[0].equals("connectReject")) { // connection rejected
			confirmNameButton.setDisable(false);
		} else if (line[0].equals("myIndex")) {
			try {
				myIndex = Integer.parseInt(line[1]);
			} catch (Exception e) {

			}
		} else if (line[0].equals("dm")) {
			// dm recieved from connected client
			if (toOpponentLabel.getText().equals("")) {
				toOpponentLabel.setText(line[1]);
			} else {
				toOpponentLabel.setText(toOpponentLabel.getText() + '\n' + line[1]);
			}
			for (int x = 2; x < line.length; x++) {
				toOpponentLabel.setText(toOpponentLabel.getText() + ' ' + line[x]);
			}
		} else if (line[0].equals("everyone")) { // @everyone message recieved
			if (toEveryoneLabel.getText().equals("")) {
				toEveryoneLabel.setText(line[1]);
			} else {
				toEveryoneLabel.setText(toEveryoneLabel.getText() + '\n' + line[1]);
			}
			for (int x = 2; x < line.length; x++) {
				toEveryoneLabel.setText(toEveryoneLabel.getText() + ' ' + line[x]);
			}
		} else if (line[0].equals("f")) {
			String[] names = line[1].split("[,]");
			pokemonFilterDisplay.getChildren().clear();
			for (String name : names) {
				if (!name.equals("NONE")) {
					Button b = new Button(name.replace("_", " "));
					b.setMinWidth(280);
					b.setOnAction(event -> {
						out.println("xp " + name);
					});
					if (names.length < 178) {
						try {
							ImageView v = getPokemonSprite(name);
							v.setFitHeight(40);
							b.setGraphic(v);
							// b.setContentDisplay(ContentDisplay.TOP);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					pokemonFilterDisplay.getChildren().add(b);
				}
			}
		} else if (line[0].equals("p")) {
			displayAddPokemonDialog(line[1]);
		} else if (line[0].equals("m")) {
			int newIndex = Integer.valueOf(line[line.length - 1]);
			possibleMoves[newIndex] = new Move[line.length - 2];
			for (int i = 0; i < line.length - 2; i++) {
				possibleMoves[newIndex][i] = Move.deStringify(line[i + 1]);
			}
			indexOfPokemonToGetMovesOf = -1;
		} else if (line[0].equals("close")) { // close client socket and exit to host tab
			if (bgmControl != null)
				bgmControl.dispose();
			exitServer();
		} else if (line[0].equals("battleStart")) { // start the battle
			startBattle();
			tabPane.getSelectionModel().select(3);
		} else if (line[0].equals("exit")) {
			if (bgmControl != null)
				bgmControl.dispose();
			exitServer();
			secretStage.close();
			System.exit(1);
		}
	}

	/** Resets the Battle UI buttons based on which pokemon is currently active*/
	public void resetBattleButtons() {
		// checking moves
		battleMoveOne.setDisable(true);
		battleMoveTwo.setDisable(true);
		battleMoveThree.setDisable(true);
		battleMoveFour.setDisable(true);
		if (activePokemon >= 0 && team[activePokemon] != null) {
			if (team[activePokemon].knownMoves.get(0) != null) {
				battleMoveOne.setDisable(false);
				battleMoveOne.setText(team[activePokemon].knownMoves.get(0).getName().replace("_", " "));
			} else {
				battleMoveOne.setText("N/A");
			}
			if (team[activePokemon].knownMoves.get(1) != null) {
				battleMoveTwo.setDisable(false);
				battleMoveTwo.setText(team[activePokemon].knownMoves.get(1).getName().replace("_", " "));
			} else {
				battleMoveTwo.setText("N/A");
			}
			if (team[activePokemon].knownMoves.get(2) != null) {
				battleMoveThree.setDisable(false);
				battleMoveThree.setText(team[activePokemon].knownMoves.get(2).getName().replace("_", " "));
			} else {
				battleMoveThree.setText("N/A");
			}
			if (team[activePokemon].knownMoves.get(3) != null) {
				battleMoveFour.setDisable(false);
				battleMoveFour.setText(team[activePokemon].knownMoves.get(3).getName().replace("_", " "));
			} else {
				battleMoveFour.setText("N/A");
			}
		}
		// checking pokemon
		battlePokeOne.setDisable(true);
		battlePokeTwo.setDisable(true);
		battlePokeThree.setDisable(true);
		battlePokeFour.setDisable(true);
		battlePokeFive.setDisable(true);
		battlePokeSix.setDisable(true);
		if (activePokemon != 0 && team[0] != null && !team[0].isFainted()) {
			battlePokeOne.setDisable(false);
		}
		if (activePokemon != 1 && team[1] != null && !team[1].isFainted()) {
			battlePokeTwo.setDisable(false);
		}
		if (activePokemon != 2 && team[2] != null && !team[2].isFainted()) {
			battlePokeThree.setDisable(false);
		}
		if (activePokemon != 3 && team[3] != null && !team[3].isFainted()) {
			battlePokeFour.setDisable(false);
		}
		if (activePokemon != 4 && team[4] != null && !team[4].isFainted()) {
			battlePokeFive.setDisable(false);
		}
		if (activePokemon != 5 && team[5] != null && !team[5].isFainted()) {
			battlePokeSix.setDisable(false);
		}
	}

	/** Starts up the Battle UI (renaming buttons and labels to match the new connection)*/
	public void startBattle() {
		// updating the gui
		try {
			if (team[0] != null) {
				battlePokeOne.setTooltip(new Tooltip(team[0].getNick().replace("_", " ")));
				battlePokeOne.setGraphic(getPokemonSprite(team[0].getNick()));
			} else {
				battlePokeOne.setGraphic(getPokemonSprite("N/A"));
			}
			if (team[1] != null) {
				battlePokeTwo.setTooltip(new Tooltip(team[1].getNick().replace("_", " ")));
				battlePokeTwo.setGraphic(getPokemonSprite(team[1].getNick()));
			} else {
				battlePokeTwo.setGraphic(getPokemonSprite("N/A"));
			}
			if (team[2] != null) {
				battlePokeThree.setTooltip(new Tooltip(team[2].getNick().replace("_", " ")));
				battlePokeThree.setGraphic(getPokemonSprite(team[2].getNick()));
			} else {
				battlePokeThree.setGraphic(getPokemonSprite("N/A"));
			}
			if (team[3] != null) {
				battlePokeFour.setTooltip(new Tooltip(team[3].getNick().replace("_", " ")));
				battlePokeFour.setGraphic(getPokemonSprite(team[3].getNick()));
			} else {
				battlePokeFour.setGraphic(getPokemonSprite("N/A"));
			}
			if (team[4] != null) {
				battlePokeFive.setTooltip(new Tooltip(team[4].getNick().replace("_", " ")));
				battlePokeFive.setGraphic(getPokemonSprite(team[4].getNick()));
			} else {
				battlePokeFive.setGraphic(getPokemonSprite("N/A"));
			}
			if (team[5] != null) {
				battlePokeSix.setTooltip(new Tooltip(team[5].getNick().replace("_", " ")));
				battlePokeSix.setGraphic(getPokemonSprite(team[5].getNick()));
			} else {
				battlePokeSix.setGraphic(getPokemonSprite("N/A"));
			}
		} catch (Exception e) {
		}
		resetBattleButtons();
		// sending the team data to the server
		String teamString = "";
		for (int i = 0; i < 6; i++) {
			if (team[i] == null) {
				teamString += "null";
			} else {
				teamString += team[i].stringifyWithMoves();
			}
			if (i != 5) {
				teamString += '&';
			}
		}
		send("loadTeam " + teamString);

		// I am going to create an environment that's so baller
		try {
			File battleTheme = new File("../src/main/resources/music/possibly_sans.mp3");
			bgm = new Media(battleTheme.toURI().toString());
			bgmControl = new MediaPlayer(bgm);
			bgmControl.setOnEndOfMedia(new Runnable() {
				public void run() {
					bgmControl.seek(Duration.ZERO);
				}
			});
			/*
			 * bgmControl.setOnReady(new Runnable() {
			 * public void run() {
			 * bgmControl.play();
			 * }
			 * });
			 */
			bgmControl.play();
		} catch (Exception error) {
			error.printStackTrace();
		}
	}

	/** Sends a message to the server
	*
	*	@param message The message to send
	*/
	public void send(String message) {
		System.out.println("sending " + message);
		out.println(message);
	}

	/** Initializes the reader and writer for the client-server connection*/
	public void connect() {
		System.out.println("CLIENT: connected");
		BufferedReader in = null;
		try {
			// assigning I/O objects
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			out.println("test");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		// creating reader service
		read = new Reader(in);
		read.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			public void handle(WorkerStateEvent event) {
				// getting line read
				String fullLine = ((Reader) event.getSource()).getValue();
				System.out.println("CLIENT: recieved " + fullLine);
				// sending split line to parser
				parseRead(fullLine.split(" "));
				// restarting reader
				if (!stopReading) {
					((Reader) event.getSource()).restart();
				} else {
					System.out.println("cancel");
					stopReading = false;
				}
			}
		});
		read.start();
		// s.setScene(baseScene);
		// s.show();
	}

	/** Updates the theme chosen by the dropdown*/
	public void updateTheme() {
		String selection = themesSelector.getValue();
		System.out.println(selection);
		switch (selection) {
			case "Blue":
				currentStyle = "/blue.css";
				dark = Color.rgb(10, 37, 68);
				break;
			case "Yellow":
				currentStyle = "/yellow.css";
				dark = Color.rgb(67, 68, 10);
				break;
			case "Windows XP":
				currentStyle = "/linus.css";
				dark = Color.rgb(0, 0, 0, 0);
				break;
			default:
				currentStyle = "/font.css";
				break;
		}
		light = dark.brighter();
		System.out.println(currentStyle);
		themesSelector.getScene().getStylesheets().clear();
		themesSelector.getScene().getStylesheets().add(currentStyle);
	}

	/** Loads the FXML and css files onto the Stage
	*
	*	@param stage The stage to load the files into
	*/
	@Override
	public void start(Stage stage) { // loads in fxml and shows it initializable happens next
		try {

			File file = new File("../src/main/java/game.fxml");
			FXMLLoader loader = new FXMLLoader();
			// System.out.println(file.getCanonicalPath());
			Parent root = loader.load(new FileInputStream(file.getCanonicalPath()));

			baseScene = new Scene(root);

			baseScene.getStylesheets().add("/font.css");
			stage.setScene(baseScene);
			stage.setResizable(false);
			try {
				File imageFile = new File("../src/main/resources/icons/pokeIcon.png");
				appIcon = new Image(new FileInputStream(imageFile));
				stage.getIcons().add(appIcon);
			} catch (Exception e) {
				/* If it don't work it really ain't that deep dawgie */}
			stage.setTitle("PokéFX");
			stage.show();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** Launches the application
	*
	*	@param args The command-line arguments given at runtime (these are never checked)
	*/
	public static void main(String[] args) { // main method activates start
		launch(args);
	}

}

/** Helper reader class to wait for and read in messages from the server without freezing the GUI*/
public class Reader extends Service<String> {

	private BufferedReader in;

	/**
	 * Basic constructor
	 *
	 * @param reader The BufferedReader connected to the peer system
	 */
	public Reader(BufferedReader reader) {
		in = reader;
	}

	/**
	 * Creates a newly threaded task to wait for and return input from the connected
	 * peer system
	 *
	 */
	@Override
	protected Task<String> createTask() {
		return new Task<String>() {
			@Override
			protected String call() {
				try {
					String line = in.readLine();
					return line;
				} catch (Exception e) {
				}
				return "";
			}
		};
	}
}