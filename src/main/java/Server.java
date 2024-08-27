package main.java;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;

import java.io.*;
import java.net.*;

public class Server {
	private static ArrayList<String> clientNames = new ArrayList<String>();
	private static ArrayList<Socket> clients = new ArrayList<Socket>();
	private static ArrayList<BufferedReader> readers = new ArrayList<BufferedReader>();
	private static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	private static ArrayList<ReadClient> clientReaders = new ArrayList<ReadClient>();
	private static ArrayList<Battle> battles = new ArrayList<Battle>();
	private static ServerSocket server;
	private static xmlParser parser = null;
	private static boolean closing = false;
	private boolean valid = false;
	private static AtomicInteger peopleLeaving = new AtomicInteger(0);
	private static AtomicInteger connectI = new AtomicInteger(0);

	public Server(int port) throws IOException, ParserConfigurationException {
		System.out.println("tryna make server");
		server = new ServerSocket(port);
		parser = new xmlParser("../src/main/java/xml/pokemen.xml", "../src/main/java/xml/moves.xml");
		WaitForConnections wfc = new WaitForConnections();
		valid = true;
		wfc.start();
	}

	public boolean isValid() {
		return valid;
	}

	public static ArrayList<Pokemon> teamDeStringify(String teamString) {
		ArrayList<Pokemon> team = new ArrayList<Pokemon>(6);
		for (int i = 0; i < 6; i++) {
			team.add(null);
		}
		String[] splitTeam = teamString.split("&");
		for (int i = 0; i < 6; i++) {
			team.set(i, Pokemon.deStringify(splitTeam[i]));
		}
		return team;
	}

	public static ArrayList<Pokemon> teamDeStringifyWithMoves(String teamString) {
		ArrayList<Pokemon> team = new ArrayList<Pokemon>(6);
		for (int i = 0; i < 6; i++) {
			team.add(null);
		}
		String[] splitTeam = teamString.split("&");
		for (int i = 0; i < 6; i++) {
			team.set(i, Pokemon.deStringifyWithMoves(splitTeam[i]));
		}
		return team;
	}

	static class WaitForConnections extends Thread {
		public void run() {
			try {
				clientNames.clear();
				clients.clear();
				readers.clear();
				writers.clear();
				clientReaders.clear();
				battles.clear();
				closing = false;
				for (; !closing; connectI.incrementAndGet()) { // waiting for clients
					// wait for client
					System.out.println("waiting");
					Socket client = server.accept();
					int i = connectI.get();
					System.out.println("SERVER: found a connection " + i);
					// assign variables
					battles.add(null);
					clients.add(client);
					readers.add(new BufferedReader(new InputStreamReader(client.getInputStream())));
					writers.add(new PrintWriter(client.getOutputStream(), true));
					clientNames.add("Guest" + i);
					ReadClient r = new ReadClient(i);
					clientReaders.add(r);
					r.start();
					// updating everyone's name info
					String namesListNew = "nameInit Guest" + i + " ";
					for (int w = 0; w < i; w++) {
						String namesListOthers = "names ";
						// getting all the client names
						for (int j = 0; j < clientNames.size(); j++) {
							if (j != w) { // don't add the client's own name
								namesListOthers += clientNames.get(j) + " ";
							}
							if (w == 0 && j != i) { // don't add the new client's name to itself
								namesListNew += clientNames.get(j) + " ";
							}
						}
						writers.get(w).println(namesListOthers);
					}
					writers.get(i).println(namesListNew);
				}
				System.out.println("h " + closing);
			}
			catch(SocketException e) {}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static class ReadClient extends Thread {
		public int yourIndex;
		public ArrayList<Integer> connectedIndices;

		public ReadClient(int i) {
			yourIndex = i;
			connectedIndices = new ArrayList<Integer>();
		}

		@Override
		public void run() {
			System.out.println("starting reader");
			while (!closing) {
				try {
					String line = "uninitialized";
					try {
						line = readers.get(yourIndex).readLine();
					}
					catch(IndexOutOfBoundsException e) {
						System.out.println("oob");
						while(peopleLeaving.get() > 0) {}
						continue;
					}
					catch(Exception e) {
						e.printStackTrace();
						continue;
					}
					
					if (line == null) {
						//System.out.println("was null");
						writers.get(yourIndex).println("server read null");
						break;
					}
					
					// different commands will output to different places
					String[] split = line.split(" "); // splitting on space (can be changed easily to a different regex)
					System.out.println("SERVER: received '" + line + "'");
					if (split[0].equals("name")) {
						// client wants to change name internally
						// resetting the client's name internally
						clientNames.set(yourIndex, split[1]);
						// sending the new names list to all clients (except this one)
						for (int i = 0; i < writers.size(); i++) {
							String namesList = "names ";
							if (i != yourIndex) {
								for (int j = 0; j < clientNames.size(); j++) {
									if (j != i) {
										namesList += clientNames.get(j) + " ";
									}
								}
								writers.get(i).println(namesList);
							}
						}
					} else if (split[0].equals("connect")) {
						// client wants to connect to another client
						// finding index of client to send info to
						int i;
						for (i = 0; i < clientNames.size(); i++) {
							if (clientNames.get(i).equals(split[1])) {
								break;
							}
						}
						if(i != clientNames.size()) {
							// making sure both clients aren't in a battle
							if(battles.get(i) == null && battles.get(yourIndex) == null) {
								// sending info
								writers.get(i).println("connect " + clientNames.get(yourIndex));
							}
						}
					} else if (split[0].equals("connectAccept")) {
						// connection accepted, link the two clients
						// finding index of client to link to
						int i;
						for (i = 0; i < clientNames.size(); i++) {
							if (clientNames.get(i).equals(split[1])) {
								break;
							}
						}
						if (i != clientNames.size()) {
							connectedIndices.add(i);
							clientReaders.get(i).connectedIndices.add(yourIndex);
							writers.get(i).println("connectAccept " + clientNames.get(yourIndex));
						}
						// create the battle between both players
						Battle bat = new Battle(2, 1); // new single battle (double battles don't exist yet LMAO
						battles.set(yourIndex, bat);
						battles.set(i, bat);
						writers.get(i).println("battleStart");
						writers.get(yourIndex).println("battleStart");
					}
					else if(split[0].equals("connectReject")) { // connection request rejected
						// finding index of client to send rejection message
						int i;
						for (i = 0; i < clientNames.size(); i++) {
							if (clientNames.get(i).equals(split[1])) {
								break;
							}
						}
						if (i != clientNames.size()) {
							writers.get(i).println("connectReject " + clientNames.get(yourIndex));
						}
					}
					else if (split[0].equals("dm")) {
						// send dm to connected clients
						String message = "";
						for (int i = 1; i < split.length; i++) {
							message += split[i] + " ";
						}
						for (int i = 0; i < connectedIndices.size(); i++) {
							writers.get(connectedIndices.get(i)).println("dm " + message);
						}
					} else if (split[0].equals("everyone")) {
						// send message to everyone
						String message = "";
						for (int i = 1; i < split.length; i++) {
							message += split[i] + " ";
						}
						for (int i = 0; i < writers.size(); i++) {
							writers.get(i).println("everyone " + message);
						}
					} else if (split[0].equals("xf")) {	
						// xml filter for pokemon
						if (split[1].equals("Any")) {
							split[1] = "";
						}
						if (split[2].equals("Any")) {
							split[2] = "";
						}
						ArrayList<String> pokemonNames = parser.pokemonFilter(split[1], split[2]);
						if (pokemonNames.size() <= 0) {
							System.out.println("");
							writers.get(yourIndex).println("f NONE");
						} else {
							String s = "";
							for (String names : pokemonNames) {
								s += names + ",";
							}
							s.replace(pokemonNames.get(pokemonNames.size() - 1) + ",",
									pokemonNames.get(pokemonNames.size() - 1));
							System.out.println(s);
							writers.get(yourIndex).println("f " + s);
						}
					} else if (split[0].equals("move")) {
						// A Client is locking in a move, and also gives targets
						// format is move <move Number> <Targets>
						int usingMove = -1;

						try {
							usingMove = Integer.parseInt(split[1]);
						} catch (Exception e) {

						}
						int[] targets = new int[split.length - 2];
						for (int i = 2; i < split.length; i++) {
							try {
								targets[i - 2] = Integer.parseInt(split[i]);
							} catch (Exception e) {

							}

						}

						// update some global shit that does the battle
						int whichPlayer = battles.get(yourIndex).playerToIndex.get(yourIndex);
						String message = battles.get(yourIndex).makeMove(yourIndex, usingMove, targets);
						if (message.contains("[<GameOver>]")){
							message=message.substring(message.indexOf(' ')+1);
							System.out.println("Game ended for server");
							for (int i = 0; i < connectedIndices.size(); i++) {
								battles.set(connectedIndices.get(i), null);
								writers.get(connectedIndices.get(i)).println("GameEnded " + message);
								clientReaders.get(connectedIndices.get(i)).connectedIndices.remove(Integer.valueOf(yourIndex));
							}
							writers.get(yourIndex).println("GameEnded " + message);
							
						}
						else if (!message.equals("")) {
							// send all that to the clients
							for (int i = 0; i < connectedIndices.size(); i++) {
								writers.get(connectedIndices.get(i)).println("recvState " + message);
							}
							writers.get(yourIndex).println("recvState " + message);
						}
					} else if (split[0].equals("xp")) { // xml get stringified pokemon
						String pokemon = parser.getPokemon(split[1]);
						writers.get(yourIndex).println("p " + pokemon);
					} else if (split[0].equals("xm")) { // xml get stringified move
						String move = parser.getMove(split[1]);
						writers.get(yourIndex).println("m " + move);
					} else if (split[0].equals("xpm")) { // xml get moves of pokemon
						String moves = parser.getPokemonMoves(split[1]);
						String[] moveList = moves.split("[,]");
						Set<String> movesSet = new LinkedHashSet<String>();
						for (String move : moveList) {
							movesSet.add(move);
						}
						String s = "";
						for (String move : movesSet) {
							s += parser.getMove(move) + " ";
						}
						writers.get(yourIndex).println("m " + s + split[2]);
						System.out.println("m " + s);
						// writers.get(yourIndex).println("pm " + moves);
					} else if (split[0].equals("close")) {
						// close server
						// telling clients the server's closing
						if (yourIndex != 0) {
							for (int i = 0; i < clientReaders.size(); i++) {
								if (i != yourIndex) {
									writers.get(i).println("close");
								}
							}
							// closing the server
							closing = true;
							server.close();
						}
					} else if (split[0].equals("loadTeam")) { // loads team into memory
						// TODO: make this compatible for more than 1 player vs 1 player
						String ind = battles.get(yourIndex).loadTeam(yourIndex, teamDeStringifyWithMoves(split[1]));
						// also need to get the moves
						if (!ind.equals("-1")) {
							writers.get(yourIndex).println("myIndex " + ind);
						}

						/*
						 * if(yourIndex > connectedIndices.get(0)) {
						 * battles.get(yourIndex).loadTeam(0, teamDeStringify(split[1]));
						 * }
						 * else {
						 * battles.get(yourIndex).loadTeam(1, teamDeStringify(split[1]));
						 * }
						 * 
						 */
					} else if (split[0].equals("quit")) {
						if (yourIndex == 0) {
							// Host left
							// tell clients to stop listening to server
							for (int i = 1; i < writers.size(); i++) {
								writers.get(i).println("close");
							}
							closing = true;
							server.close();
						} else {
							// client left
							peopleLeaving.incrementAndGet();
							// disconnecting everyone client was connected to
							for(int i = 0; i < connectedIndices.size(); i++) {
								writers.get(i).println("GameEnded disconnect");
								clientReaders.get(i).connectedIndices.remove(Integer.valueOf(yourIndex));
							}
							// removing client data
							readers.remove(yourIndex);
							clientReaders.remove(yourIndex);
							clientNames.remove(yourIndex);
							clients.remove(yourIndex);
							battles.remove(yourIndex);
							// updating everyone else's connected indices
							for(ReadClient r: clientReaders) {
								for(int i = 0; i < r.connectedIndices.size(); i++) {
									if(i > yourIndex) {
										r.yourIndex--;
									}
									if(r.connectedIndices.get(i) > yourIndex) {
										r.connectedIndices.set(i, r.connectedIndices.get(i) - 1);
									}
								}
							}
							// sending the updated names list
							for (int i = 0; i < writers.size(); i++) {
								String namesList = "names ";
								for (int j = 0; j < clientNames.size(); j++) {
									if (j != i) {
										namesList += clientNames.get(j) + " ";
									}
								}
								writers.get(i).println(namesList);
							}
						}
						if(split[1].equals("disconnect")) {
							writers.remove(yourIndex).println("close");
						}
						else {
							writers.remove(yourIndex).println("exit");
						}
						peopleLeaving.decrementAndGet();
						connectI.decrementAndGet();
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
			System.out.println("clo " + closing);
		}
	}

}