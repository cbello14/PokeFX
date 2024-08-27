package main.java;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class xmlParser {
    private DocumentBuilderFactory factory;
    private File pokeFile, moveFile;

    /**
     * The constructor for the parser
     * 
     * @param pokemonPath The path to the pokemon xml file
     * @param movesPath   the path to the moves xml file
     * @throws ParserConfigurationException
     */
    public xmlParser(String pokemonPath, String movesPath) throws ParserConfigurationException {
        this.factory = DocumentBuilderFactory.newInstance();
        this.pokeFile = new File(pokemonPath);
        this.moveFile = new File(movesPath);
    }

    /**
     * A function that returns an Array List of all the pokemon names that match the
     * filters
     * 
     * @param name A filter that searches for all pokemon that start with this
     *             String
     * @param type A filter that searches for all pokemon that are this type
     * @return An arraylist of strings where each string is the name of a pokemon
     *         that matches the filters
     */
    public ArrayList<String> pokemonFilter(String name, String type) {
        ArrayList<String> names = new ArrayList<String>();

        DocumentBuilder builder;
        try {
            builder = this.factory.newDocumentBuilder();
            Document doc;
            try {
                doc = builder.parse(pokeFile);
                NodeList namesNodes = doc.getElementsByTagName("name");
                for (int i = 0; i < namesNodes.getLength(); i++) {
                    Node nameNode = namesNodes.item(i);
                    Node typeNode = nameNode.getNextSibling().getNextSibling().getNextSibling().getNextSibling();
                    if (nameNode.getTextContent().startsWith(name) && typeNode.getTextContent().contains(type)) {
                        names.add(nameNode.getTextContent());
                    }
                }
            } catch (SAXException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return names;
    }

    /**
     * A function that gets a String of comma seperated moves that a pokemon has, it
     * can be split on the commas to get an array of move names
     * 
     * @param name The name of the pokemon to get the moves of
     * @return The string of comma seperated move names, or null if the pokemon
     *         doesnt exist or the function failed
     */
    public String getPokemonMoves(String name) {
        String s = null;
        DocumentBuilder builder;
        try {
            builder = this.factory.newDocumentBuilder();
            Document doc;
            try {
                doc = builder.parse(pokeFile);
                NodeList names = doc.getElementsByTagName("name");
                for (int i = 0; i < names.getLength(); i++) {
                    Node nameNode = names.item(i);
                    if (nameNode.getTextContent().equals(name)) {
                        Node movesNode = nameNode.getNextSibling().getNextSibling().getNextSibling().getNextSibling()
                                .getNextSibling().getNextSibling().getNextSibling().getNextSibling();
                        String movesText = movesNode.getTextContent();
                        movesText = movesText.replaceAll("[\\[]", "");
                        movesText = movesText.replaceAll("[\\]]", "");
                        movesText = movesText.replaceAll("[\\']", "");
                        movesText = movesText.replaceAll("[,][ ]", ",");
                        movesText = movesText.replaceAll("[ ]", "_");
                        return movesText;
                    }
                }
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return s;
    }

    /**
     * A function that returns the Stringified form of a pokemon given its name.
     * This can then be destringified to get the base form of a pokemon
     * 
     * @param name The name of a pokemon
     * @return The stringified form of the pokemon, or null if it fails to get the
     *         pokemon/ the pokemon name is invalid
     */
    public String getPokemon(String name) {
        String s = null;
        DocumentBuilder builder;
        try {
            builder = this.factory.newDocumentBuilder();
            Document doc;
            try {
                doc = builder.parse(pokeFile);
                NodeList names = doc.getElementsByTagName("name");
                for (int i = 0; i < names.getLength(); i++) {
                    Node nameNode = names.item(i);
                    if (nameNode.getTextContent().equals(name)) {
                        Node indexNode = nameNode.getNextSibling().getNextSibling();
                        int index;
                        try {
                            index = Integer.parseInt(indexNode.getTextContent());
                        } catch (Exception e) {
                            break;
                        }
                        Node typeNode = indexNode.getNextSibling().getNextSibling();

                        String typeText = typeNode.getTextContent();
                        String[] types = typeText.split(" ");
                        ArrayList<String> t = new ArrayList<String>(Arrays.asList(types));
                        Node statsNode = typeNode.getNextSibling().getNextSibling().getNextSibling().getNextSibling()
                                .getNextSibling().getNextSibling();
                        String statsText = statsNode.getTextContent();
                        String[] stats = new String[6];
                        int j = 0;
                        for (String stat : statsText.split(",")) {
                            String numText = stat.split(":")[1];
                            numText = numText.replaceAll("[^\\d.]", "");
                            stats[j] = numText;
                            j++;
                        }
                        int[] data = Arrays.stream(stats)
                                .mapToInt(Integer::parseInt)
                                .toArray();
                        Pokemon p = new Pokemon(name, t, index, data[0], data[1], data[2], data[3],
                                data[4], data[5]);
                        return p.stringify();

                    }
                }
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return s;
    }

    /**
     * A function that get the Stringified form of a move given a move name
     * 
     * @param name The name of the move that is being searched for
     * @return The stringified move or null if the move couldnt be found or an error
     *         occurred
     */
    public String getMove(String name) {
        String s = null;
        DocumentBuilder builder;
        try {
            builder = this.factory.newDocumentBuilder();
            Document doc;
            try {
                doc = builder.parse(moveFile);
                NodeList names = doc.getElementsByTagName("name");
                for (int i = 0; i < names.getLength(); i++) {
                    Node nameNode = names.item(i);
                    if (nameNode.getTextContent().equals(name)) {

                        Node typeNode = nameNode.getNextSibling().getNextSibling();
                        String type = typeNode.getTextContent();
                        Node powerNode = typeNode.getNextSibling().getNextSibling();
                        int power;
                        try {
                            power = Integer.parseInt(powerNode.getTextContent());

                        } catch (Exception e) {
                            power = 0; // TODO: handle exception
                        }
                        Node accuracyNode = powerNode.getNextSibling().getNextSibling();

                        int accuracy;
                        try {
                            accuracy = Integer.parseInt(accuracyNode.getTextContent());

                        } catch (Exception e) {
                            accuracy = 0; // TODO: handle exception
                        }
                        Node ppNode = accuracyNode.getNextSibling().getNextSibling();
                        int pp;
                        try {
                            pp = Integer.parseInt(ppNode.getTextContent());
                        } catch (Exception e) {
                            pp = 0;
                        }
                        Node priorityNode = ppNode.getNextSibling().getNextSibling();

                        int priority;
                        try {
                            priority = Integer.parseInt(priorityNode.getTextContent());

                        } catch (Exception e) {
                            priority = 0; // TODO: handle exception
                        }
                        Node categoryNode = priorityNode.getNextSibling().getNextSibling();

                        String category = categoryNode.getTextContent();

                        Move m = new Move(name, type, power, accuracy, pp, pp, priority, category);
                        s = m.stringify();
                    }
                }
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return s;
    }

    public static void main(String[] args) {
        try {
            double start = System.currentTimeMillis();
            xmlParser x = new xmlParser("src/main/java/xml/pokemen.xml", "src/main/java/xml/moves.xml");
            // x.pokemonFilter("T", "Electric");
            String s = x.getPokemonMoves("Charmander");
            System.out.println(s);
            System.out.println(System.currentTimeMillis() - start);
        } catch (Exception e) {
            System.out.println("Failed"); // TODO: handle exception
        }
    }
}
