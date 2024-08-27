package main.java;

import java.util.Arrays;

enum Category {
    Physical, Special, Status
}

class Move {
    int moveId;
    Category category;
    int power;
    // Secondary effect
    double effectProbability;
    int accuracy;
    String description;
    String moveName;

    int priority;
    int maxPP;
    int currentPP;

    String type;

    boolean selectable = true;// hopefully never used, but for messed up cases // might actually be useful for
                              // stuff like gigaton hammer

    public Move(String name) {
        moveName = name;

        // maybe XQuery the rest
    }

    public Category getCategory() {
        return category;
    }

    public String getType(){return type;}

    public int getPriority(){return priority;}

    public int getPower() {
        return power;
    }

    public String getName(){return moveName;}

    /**
     * Creates a move based off the given input values
     * 
     * @param id
     * @param pwr
     * @param acc
     * @param cPP
     * @param mPP
     */
    public Move(String name, String type, int pwr, int acc, int cPP, int mPP, int prio, String cat) {
        this.type = type;
        this.moveName = name;
        this.power = pwr;
        this.accuracy = acc;
        this.currentPP = cPP;
        this.maxPP = mPP;
		this.priority = prio;
		switch(cat) {
			case "Physical":
				category = Category.Physical;
				break;
			case "Special":
				category = Category.Special;
				break;
			case "Status":
				category = Category.Status;
				break;
			default:
				System.out.println("what the heck happened here");
				break;
		}
    }

    /**
     * A function that stores a move's data into a compact string
     * 
     * @return
     */
    public String stringify() {
        return this.moveName + "|" + this.type + "|"  + this.category + "|" + this.power + "~" + this.accuracy + "~" + this.currentPP + "~"
                + this.maxPP + "~" + this.priority;
    }

    /**
     * A function for converting a string in the "Stringify" format back into a move
     * 
     * @param s
     * @return
     */
    public static Move deStringify(String s) {
		if(s.equals("null")) {
			return null;
		}
        String[] firstSplit = s.split("[|]");
        int[] data = Arrays.stream(firstSplit[3].split("[~]")).mapToInt(Integer::parseInt).toArray();
        return new Move(firstSplit[0], firstSplit[1], data[0], data[1], data[2], data[3], data[4], firstSplit[2]);
		
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Move){
            Move m = (Move)o;
            return (m.type.equals(this.type))&&(m.power==this.power) && (m.accuracy==this.accuracy) && m.moveName.equals(this.moveName) && m.maxPP==this.maxPP;
        }
        else return false;
    }

}