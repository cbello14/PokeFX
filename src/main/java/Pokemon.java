package main.java;

import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

enum Nature {
    Hardy, Lonely, Adamant, Naughty, Brave, Bold, Docile, Impish, Lax,
    Relaxed, Modest, Mild, Bashful, Rash, Quiet, Calm, Gentle, Careful,
    Quirky, Sassy, Timid, Hasty, Jolly, Naive, Serious
}

public class Pokemon {
    String pokeName;
    String nickName;
    int level=100;//hardcoced "for now"
    int idNum;

    Nature nature;

    ArrayList<Move> learnableMoves;
    ArrayList<Move> knownMoves;

    // not EV, but base stats of the pokemon.
    int HP;
    int Attack;
    int SpAttack;
    int Defense;
    int SpDefense;
    int Speed;

    int[] EVs;

    int currentHP;
    boolean fainted = false;

    int[] statMods;// indeces 0-4 are for attack through speed, 5 is accuracy, 6 is evasion

    ArrayList<String> types;

    /**
     * Creates a pokemon in its base state with a specific nature
     * 
     * @param id
     * @param nat
     */
    public Pokemon(int id, Nature nat) {
        idNum = id;
        nature = nat;
        // XQuery stats, name, type, moves

        // if known moves unspecified, do we go no moves or last 4 learned?

    }

    /**
     * Creates a Pokemon in its base state with a Hardy nature
     * 
     * @param id
     */
    public Pokemon(int id) {
        idNum = id;
        nature = Nature.Hardy;
        // XQuery stats, name, type, moves
        knownMoves = defaultMoves();
    }

    /**
     * Creates a Pokemon based on given input values with a Hardy nature (used for
     * creating pokemon that might have already gone through battle)
     * 
     * @param id
     * @param hp
     * @param atk
     * @param satk
     * @param def
     * @param sdef
     * @param spd
     */
    public Pokemon(String name, ArrayList<String> types, int id, int hp, int atk, int def, int satk, int sdef,
            int spd) {

        EVs = new int[6];
        this.idNum = id;
        nature = Nature.Hardy;
        this.HP = hp;
        setCurrentHP(getHP());
        this.Attack = atk;
        this.SpAttack = satk;
        this.Defense = def;
        this.SpDefense = sdef;
        this.Speed = spd;
        this.types = types;
        this.pokeName = name;
        this.nickName = name;

        statMods = new int[7];
        knownMoves = new ArrayList<Move>(4);
        for (int i = 0; i < 4; i++) {
            knownMoves.add(null);
        }
    }

    /**
     * Requires that learnableMoves isnt null - it can be empty
     * returns an arraylist of the 'last 4' moves a pokemon can learn
     * 
     * @return arraylist of Move type
     */
    private ArrayList<Move> defaultMoves() {
        ArrayList<Move> known = new ArrayList<>();
        for (int i = learnableMoves.size() - 1; i >= 0 && i >= learnableMoves.size() - 4; i--) {
            known.add(learnableMoves.get(i));
        }
        return known;
    }

    public final ArrayList<Move> getMoves() {
        return knownMoves;
    }

    public void setNick(String s) {
        nickName = s;
    }

    public String getNick() {
        return nickName;
    }

    public String getName(){return pokeName;}

    public void setNature(Nature n) {
        nature = n;
    }

    public Nature getNature() {
        return nature;
    }

    public final ArrayList<String> getTypes() {
        return types;
    }

    public int getLevel() {
        return level;
    }

    public int getRawHP() {
        return HP;
    }

    public int getAttack() {
        return (int) (getRawAttack() * statmod(statMods[0]));
    }

    public int getSpAttack() {
        return (int) (getRawSpAttack() * statmod(statMods[1]));
    }

    public int getDefense() {
        return (int) (getRawDefense() * statmod(statMods[2]));
    }

    public int getSpDefense() {
        return (int) (getRawSpDefense() * statmod(statMods[3]));
    }

    public int getSpeed() {
        return (int) (getRawSpeed() * statmod(statMods[4]));
    }

    public int getHP(){return (int) (Math.floor(0.01 * (2 * getRawHP() + Math.floor(0.25 * EVs[0])) * level))+level+10; }

    public int getRawAttack() {
        return (int) (Math.floor(0.01 * (2 * Attack + Math.floor(0.25 * EVs[1])) * level) + 5);
    }

    public int getRawSpAttack() {
        return (int) (Math.floor(0.01 * (2 * SpAttack + Math.floor(0.25 * EVs[2])) * level) + 5);
    }

    public int getRawDefense() {
        return (int) (Math.floor(0.01 * (2 * Defense + Math.floor(0.25 * EVs[3])) * level) + 5);
    }

    public int getRawSpDefense() {
        return (int) (Math.floor(0.01 * (2 * SpDefense + Math.floor(0.25 * EVs[4])) * level) + 5);
    }

    public int getRawSpeed() {
        return (int) (Math.floor(0.01 * (2 * Speed + Math.floor(0.25 * EVs[5])) * level) + 5);
    }

    private double statmod(int change) {
        double mod = switch (change) {
            case -6 -> 1.0 / 4;
            case -5 -> 1.0 / 3.5;
            case -4 -> 1.0 / 3;
            case -3 -> 1.0 / 2.5;
            case -2 -> 1.0 / 2;
            case -1 -> 1.0 / 1.5;
            case 0 -> 1.0;
            case 1 -> 1.5;
            case 2 -> 2.0;
            case 3 -> 2.5;
            case 4 -> 3.0;
            case 5 -> 3.5;
            case 6 -> 4.0;
            default -> 1;
        };
        return mod;
    }

    private double accEvMod(int change) {
        double mod = switch (change) {
            case -6 -> 3.0 / 9;
            case -5 -> 3.0 / 8;
            case -4 -> 3.0 / 7;
            case -3 -> 3.0 / 6;
            case -2 -> 3.0 / 5;
            case -1 -> 3.0 / 4;
            case 1 -> 4 / 3.0;
            case 2 -> 5 / 3.0;
            case 3 -> 6 / 3.0;
            case 4 -> 7 / 3.0;
            case 5 -> 8 / 3.0;
            case 6 -> 9 / 3.0;
            default -> 1.0;
        };
        return mod;
    }

    public boolean takeDamage(int damage) {
        currentHP -= damage;
        if (currentHP <= 0) {
            currentHP = 0;
            fainted = true;
            return true;// dead
        }
        return false;// not dead
    }

    public void setCurrentHP(int cHP) {
        currentHP = cHP;
        fainted = currentHP == 0;
    }

    public boolean isFainted() {
        return fainted;
    }
	
	public void kill() {
		fainted = true;
	}
	
	public void reset() {
		fainted = false;
		setCurrentHP(getHP());
	}

    /**
     * A function that converts a pokémon into a compact string that can easily be
     * sent through sockets
     * 
     * @return
     */
    public String stringify() {
        return this.pokeName + "--" + this.stringifyTypes() + this.idNum + "|" + this.HP + "|" + this.Attack + "|"
                + this.Defense + "|"
                + this.SpAttack + "|"
                + this.SpDefense + "|" + this.Speed;
    }

    public String stringifyTypes() {
        String s = "";
        for (String type : types) {
            s += type + "+";
        }
        return s;
    }

    public String currentState() {
        return this.pokeName + "--" + this.stringifyTypes() + this.idNum + "|" + this.currentHP + "|" + this.HP + "|"
                + this.Attack + "|"
                + this.Defense + "|"
                + this.SpAttack + "|"
                + this.SpDefense + "|" + this.Speed;
    }

    public static Pokemon monFromState(String s) {
        if (s.equals("null")) {
            return null;
        }
        String[] firstSplit = s.split("[-][-]"); // splits name from rest of data
        String[] secondSplit = firstSplit[1].split("[+]"); // splits types from rest of data
        ArrayList<String> t = new ArrayList<String>(Arrays.asList(secondSplit));
        t.remove(t.size() - 1);
        int[] data = Arrays.stream(secondSplit[secondSplit.length - 1].split("[|]")).mapToInt(Integer::parseInt)
                .toArray(); // parses integer
        // data
        Pokemon p = new Pokemon(firstSplit[0], t, data[0], data[2], data[3], data[4], data[5], data[6], data[7]);
        p.setCurrentHP(data[1]);// at that health
        return p; // creates
        // pokemon
    }

    /**
     * A function that converts a string in the "stringify" format into a pokemon
     * 
     * @param s
     * @return
     */
    public static Pokemon deStringify(String s) {
        if (s.equals("null")) {
            return null;
        }
        String[] firstSplit = s.split("[-][-]"); // splits name from rest of data
        String[] secondSplit = firstSplit[1].split("[+]"); // splits types from rest of data
        ArrayList<String> t = new ArrayList<String>(Arrays.asList(secondSplit));
        t.remove(t.size() - 1);
        int[] data = Arrays.stream(secondSplit[secondSplit.length - 1].split("[|]")).mapToInt(Integer::parseInt)
                .toArray(); // parses integer
        // data
        return new Pokemon(firstSplit[0], t, data[0], data[1], data[2], data[3], data[4], data[5], data[6]); // creates
                                                                                                             // pokemon
    }

    public String stringifyWithMoves() {
        String output = "";
        output += this.pokeName + "--" + this.stringifyTypes() + this.idNum + "|" + this.HP + "|" + this.Attack + "|"
                + this.Defense + "|"
                + this.SpAttack + "|"
                + this.SpDefense + "|" + this.Speed;
        output += "@";
        for (int i = 0; i < knownMoves.size(); i++) {
            if (knownMoves.get(i) == null) {
                output += "null";
            } else {
                output += knownMoves.get(i).stringify();
            }
            if (i != knownMoves.size() - 1) {
                output += "$";
            }
        }
        return output;
    }

    public static Pokemon deStringifyWithMoves(String s) {
        if (s.equals("null")) {
            return null;
        }
        String[] nameSplit = s.split("[-][-]");
        String[] typeSplit = nameSplit[1].split("[+]");
        String[] movesSplit = typeSplit[typeSplit.length - 1].split("[@]");
        String[] pokeData = movesSplit[0].split("[|]");
        String[] movesData = movesSplit[1].split("[$]");
        ArrayList<String> types = new ArrayList<String>(Arrays.asList(typeSplit));
        types.remove(types.size() - 1);
        Pokemon ret = new Pokemon(nameSplit[0], types, Integer.valueOf(pokeData[0]), Integer.valueOf(pokeData[1]),
                Integer.valueOf(pokeData[2]),
                Integer.valueOf(pokeData[3]), Integer.valueOf(pokeData[4]), Integer.valueOf(pokeData[5]),
                Integer.valueOf(pokeData[6]));
        for (int i = 0; i < movesData.length; i++) {
            ret.knownMoves.set(i, Move.deStringify(movesData[i]));
        }
        return ret;
    }

    /**
     * A function that takes in an array of pokemon with their known moves and
     * converts it into a string
     * 
     * @param team
     * @return
     */
    public static String teamStringify(Pokemon[] team) {
        String s = "";
        for (Pokemon p : team) {
            s += p.stringify() + "(";
            // System.out.println(p.knownMoves.size());
            for (Move m : p.knownMoves) {
                s += m.stringify() + ")";
            }
            s += "(";

        }
        return s;
    }

    /**
     * A function that takes in a previously stringified team and destringifies it,
     * it assumes each pokemon has only 4 moves and works for teams of any amount of
     * pokemon
     * 
     * @param s
     * @return
     */
    public static ArrayList<Pokemon> teamDestringify(String s) {
        ArrayList<Pokemon> team = new ArrayList<Pokemon>();
        String[] ss = s.split("[(]");
        for (int a = 0; a < ss.length; a += 2) {
            Pokemon p = Pokemon.deStringify(ss[a]);
            String[] aa = ss[a + 1].split("[)]");
            for (int i = 0; i < 4; i++) {
                Move m = Move.deStringify(aa[i]);
                p.knownMoves.set(i, m);
            }
            team.add(p);
        }
        return team;
    }

    public String toString(){
        return getName();
    }

    public static void main(String[] args) {
        try {
            Pokemon[] team = new Pokemon[6];
            xmlParser x = new xmlParser("src\\main\\java\\xml\\pokemen.xml", "src/main/java/xml/moves.xml");
            for (int j = 0; j < 6; j++) {
                Pokemon p = Pokemon.deStringify(x.getPokemon("Pikachu"));
                for (int i = 0; i < 4; i++) {
                    String m = x.getMove("Comet_Punch");
                    p.knownMoves.set(i, Move.deStringify(m));

                }
                team[j] = p;
            }
            String s = Pokemon.teamStringify(team);
            ArrayList<Pokemon> t = Pokemon.teamDestringify(s);
            for (Pokemon p : t) {
                System.out.println(p.pokeName);
                for (Move m : p.knownMoves) {
                    System.out.println(m.moveName);
                }
            }
            // System.out.println(s);

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
