package main.java;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * class that stores the internals of a battle, has players, references to pokemon, and ability to do a turn
 * all stuff should be done by the Server so that random parts arent different inbetween clients
 */
public class Battle {
    public int pokeNumber;//number of mons on the field
    public int playerNum;//number of people importing teams (currently saying everyone gets 6 lmao)
    public ArrayList<ArrayList<Pokemon>> pokemons;//array of Arraylist of the pokemon on a player's team, each arraylist being a different persons team
    public int[] activeMons;//ones on the field

    public int gameType;//1 is single, 2 is double
    public int gameState; // int representing where the game is. 0 is waiting for moves, 1 is doing a turn, 2 is waiting for substitutions, 3 is subbing in and out, -1 is game is over

    public int[] movesToBeUsed;
    public int[][] targetsToBeHit;
    Lock lock = new ReentrantLock();

    ConcurrentHashMap<Integer, Integer> playerToIndex;//map that goes from the Server index to the internal Battle index

    public Battle(int playerNum, int type) {
        this.playerNum = playerNum;
        this.gameType = type;
        pokeNumber=playerNum*type;
        activeMons=new int[pokeNumber];
        pokemons=new ArrayList<ArrayList<Pokemon>>(playerNum);
        for(int i=0;i<pokeNumber;i++){
            pokemons.add(new ArrayList<Pokemon>());
        }
        movesToBeUsed=new int[pokeNumber];
        targetsToBeHit=new int[pokeNumber][pokeNumber];
        for(int i=0;i<pokeNumber;i++){
            movesToBeUsed[i]=-2;
            targetsToBeHit[i]=null;
        }//no moves have been chosen
        playerToIndex=new ConcurrentHashMap<Integer, Integer>();
    }

	public String loadTeam(int player, ArrayList<Pokemon> team) {
        int personalIndex=-1;
        if(playerToIndex.containsKey(player)){
            personalIndex=playerToIndex.get(player);
        }
        else{
            personalIndex=playerToIndex.size();
            playerToIndex.put(player,personalIndex);
		}
        pokemons.set(personalIndex, new ArrayList<Pokemon>(team));
        activeMons[personalIndex]=0;

        return ""+personalIndex;
	}

    /**
     * Sets waht player wants their pokemon to do, and does a move
     * @param player
     * @param move
     * @param targets
     * @return
     */
    public String makeMove(int player, int move, int[] targets){
        System.out.println(player + " " + move + " " + targets[0]);
        int index=playerToIndex.get(player);
        System.out.println(index + " " + move + " " + targets[0]);
        String returning= "";
        lock.lock();
        movesToBeUsed[index]=move;
        targetsToBeHit[index]=targets;
        lock.unlock();

        if(timeToGo()){
            doTurn(movesToBeUsed,targetsToBeHit);
            //figure out how to send data to the stuff
            if(gameState!=-1){

                for(int i=0; i<activeMons.length; i++){
                    ArrayList<Pokemon> temp=pokemons.get(i);
                    Pokemon p=temp.get(  activeMons[i]);
                    returning+=(p.currentState());
                    returning+="#"+activeMons[i];
                    if(i<activeMons.length-1)
                        returning+=('&');

                }
            }//send - all pokemon on field, their HP, and maybe if a player has fainted, then maybe player loss
            else{
                returning+="[<GameOver>] ";
                for(int i=0; i<activeMons.length; i++){

                    returning+="#"+activeMons[i];
                    if(i<activeMons.length-1)
                        returning+=('&');

                }
            }//game has ended

        }//all people have chosen moves
        System.out.println("returning: "+returning);
        return returning.toString();
    }

    public boolean timeToGo(){
        for(int i=0;i<pokeNumber;i++){
            if(targetsToBeHit[i]==null){
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param Attack - used attack stat of attacker
     * @param Defense - used defense stat of defender
     * @param aLevel - attacker's level
     * @param movePow - base power of the move
     * @param target - if the move is multitargeting (1 if no, .75 if yes)
     * @param stab - if the move gets the Same Type Attack Boost (1.5 if yes, 1 if no)
     * @param moveEff - how effective the move is (.25 on double resist, .5 on resist, 1 on neutral, 2 on super effective, 4 on double super effective)
     * @return the amount of damage dealt given the parameters above
     */
    public int getDamage(int Attack, int Defense, int aLevel, int movePow, double target, double stab, double moveEff) {
        int t= (int) Math.round(2*aLevel/5.0);
        t=t*movePow*Attack;
        t= (int) (Math.round( Math.round((float) t / Defense) /50.0)+2);
        t= (int) Math.round(t*target*stab*moveEff);
        t= (int) Math.round(t*((Math.random()*15+85)/100));//rando damage mod
        double critChance=1.0/24;
        t= (int) Math.round(t*(Math.random()<(critChance)?1.5:1));//crit
        return t;
    }

    /**
     * Calculates the damage multiplier by looking at the types of pokemon and moves
     * @param moveType the type of the move
     * @param pokeType the types of the pokémon
     * @return the multiplier of damage due to the defending pokémon's type and offensive move
     */
    public double getEff(String moveType, ArrayList<String> pokeType){
        double effectiveness =1.0;
        for(String type:pokeType){
            switch (type){
                case "Normal":
                    switch (moveType){
                        case "Fighting":
                            effectiveness *=2;
                            break;
                        case "Ghost":
                            effectiveness *=0;
                            break;
                        default:
                            effectiveness *=1;
                            break;
                    }
                    break;
                case "Grass":
                    switch (moveType){
                        case "Grass", "Water", "Electric", "Ground":
                            effectiveness *=0.5;
                            break;
                        case "Fire", "Poison", "Ice", "Flying", "Bug":
                            effectiveness *=2;
                            break;
                        default:
                            effectiveness *=1;
                            break;
                    }
                    break;
                case "Fire":
                    switch (moveType){
                        case "Water", "Ground", "Rock":
                            effectiveness*=2;
                            break;
                        case "Fire","Grass","Ice", "Bug", "Steel", "Fairy":
                            effectiveness*=0.5;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Water":
                    switch (moveType){
                        case "Grass", "Electric":
                            effectiveness*=2;
                            break;
                        case "Fire","Water","Ice", "Steel":
                            effectiveness*=0.5;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Electric":
                    switch (moveType){
                        case "Ground":
                            effectiveness*=2;
                            break;
                        case "Electric", "Flying", "Steel":
                            effectiveness*=0.5;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Ground":
                    switch (moveType){
                        case "Water", "Grass", "Ice":
                            effectiveness*=2;
                            break;
                        case "Poison", "Rock":
                            effectiveness*=0.5;
                            break;
                        case "Electric":
                            effectiveness=0;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Rock":
                    switch (moveType){
                        case "Water", "Grass", "Fighting", "Ground", "Steel":
                            effectiveness*=2;
                            break;
                        case "Normal", "Fire", "Poison", "Flying":
                            effectiveness*=0.5;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Poison":
                    switch (moveType){
                        case "Ground", "Psychic":
                            effectiveness*=2;
                            break;
                        case "Grass", "Fighting", "Poison", "Bug", "Fairy":
                            effectiveness*=0.5;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Psychic":
                    switch (moveType){
                        case "Bug", "Ghost", "Dark":
                            effectiveness*=2;
                            break;
                        case "Fighting", "Psychic":
                            effectiveness*=0.5;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Ice":
                    switch (moveType){
                        case "Fire", "Fighting", "Rock", "Steel":
                            effectiveness*=2;
                            break;
                        case "Ice":
                            effectiveness*=0.5;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Fighting":
                    switch (moveType){
                        case "Flying", "Psychic", "Fairy":
                            effectiveness*=2;
                            break;
                        case "Bug", "Rock", "Dark":
                            effectiveness*=0.5;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Ghost":
                    switch (moveType){
                        case "Ghost", "Dark":
                            effectiveness*=2;
                            break;
                        case "Poison", "Bug":
                            effectiveness*=0.5;
                            break;
                        case "Normal", "Fighting":
                            effectiveness=0;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Dragon":
                    switch (moveType){
                        case "Ice", "Dragon", "Fairy":
                            effectiveness*=2;
                            break;
                        case "Fire", "Water", "Grass", "Electric":
                            effectiveness*=0.5;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Flying":
                    switch (moveType){
                        case "Electric", "Rock", "Ice":
                            effectiveness*=2;
                            break;
                        case "Grass", "Fighting", "Bug":
                            effectiveness*=0.5;
                            break;
                        case "Ground":
                            effectiveness=0;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Bug":
                    switch (moveType){
                        case "Fire", "Flying", "Rock":
                            effectiveness*=2;
                            break;
                        case "Grass", "Fighting", "Ground":
                            effectiveness*=0.5;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Dark":
                    switch (moveType){
                        case "Fighting", "Bug", "Fairy":
                            effectiveness*=2;
                            break;
                        case "Ghost", "Dark":
                            effectiveness*=0.5;
                            break;
                        case "Psychic":
                            effectiveness=0;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Steel":
                    switch (moveType){
                        case "Fire", "Fighting", "Ground":
                            effectiveness*=2;
                            break;
                        case "Normal", "Grass", "Ice", "Flying", "Psychic", "Bug", "Rock", "Dragon", "Steel", "Fairy":
                            effectiveness*=0.5;
                            break;
                        case "Poison":
                            effectiveness=0;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                case "Fairy":
                    switch (moveType){
                        case "Poison", "Steel":
                            effectiveness*=2;
                            break;
                        case "Fighting", "Bug", "Dark":
                            effectiveness*=0.5;
                            break;
                        case "Dragon":
                            effectiveness=0;
                            break;
                        default:
                            effectiveness*=1;
                            break;
                    }
                    break;
                default:
                    effectiveness*=1;
                    break;

            }
        }//type is the defensive, moveType is offensive
        return effectiveness;
    }

    /**
     * Determines if the used move has a similar type with the 'mon using it
     * @param moveType type of move
     * @param pokeType type of pokémon
     * @return the STAB multiplier
     */
    public double getStab(String moveType, ArrayList<String> pokeType){
        double stab=1.0;
        for(String type: pokeType){
            if(type.equals(moveType)) stab=1.5;
        }
        return stab;
    }

    /**
     * Makes the pokémon specified by activeP use the move determined by moveNum on targets targetP
     * @param activeP - which pokémon is using the move
     * @param moveNum - which move is being used
     * @param targetP - which pokemon are getting affected
     */
    public  void useMove(int activeP, int moveNum, int[] targetP){

        if(moveNum==-1){
            return;
        }//stunned?
        if(moveNum<0||moveNum>3){
            return;
        }//input sanitizing

        Pokemon user=pokemons.get(activeP).get(activeMons[activeP]);
        System.out.printf("pokemon currently using a move %s\n",user.getName());
        final ArrayList<Move> moves= user.getMoves();

        if(moves.size()<=moveNum){
            System.out.println("not enough moves boyo");
            return;
        }//the pokémon doesnt have 4 moves

        // make them use struggle maybe if out of bounds?? later

        Move using=moves.get(moveNum);//the move being used

		System.out.printf("move being used: %s\n", using.getName());

        int attack,aLevel,movePow;
        double stab,multiTarget;

        int acc=(using.accuracy==0?100:using.accuracy);

        if(Math.random()*100>acc){
            return;
        }//accuracy

        switch (using.getCategory()){
            case Category.Physical:
                System.out.println("physical attack happenin");
                attack=user.getAttack();
                aLevel=user.getLevel();
                stab= getStab(using.getType(),user.getTypes() ) ;
                movePow=using.getPower();
                multiTarget=(targetP.length==1?1.0:0.75);
                //^^^ all information not dependent on target
                for(int i=0; i<targetP.length; i++){
                    Pokemon target=pokemons.get(targetP[i]).get(activeMons[targetP[i]]);
                    System.out.printf("Will be targeting %s\n",target.getName());
                    double effectiveness=getEff(using.getType(), target.getTypes());
                    int defense=target.getDefense();
                    //^^info for damage calc dependant on target
                    int damageDealt=getDamage(attack,defense,aLevel, movePow, multiTarget,stab,effectiveness);
                    target.takeDamage(damageDealt);

                }
                break;
            case Category.Special:
                System.out.println("special attack happenin");
                attack=user.getSpAttack();
                aLevel=user.getLevel();
                stab= getStab(using.getType(),user.getTypes() ) ;
                movePow=using.getPower();
                multiTarget=(targetP.length==1?1.0:0.75);
                //^^^ all information not dependent on target
                for(int i=0; i<targetP.length; i++){
                    Pokemon target=pokemons.get(targetP[i]).get(activeMons[targetP[i]]);
                    System.out.printf("Will be targeting %s\n",target.getName());
                    double effectiveness=getEff(using.getType(), target.getTypes());
                    int defense=target.getSpDefense();
                    //Info for damage calc dependant on target(s)
                    int damageDealt=getDamage(attack,defense,aLevel, movePow, multiTarget,stab,effectiveness);
                    target.takeDamage(damageDealt);
                    //make em take damage
                   //remove from turn order if they havent gone yet, send flag that makes whatever happen on faint
                }
                break;
            case Category.Status:
                System.out.println("status attack happenin");
                //uhhhhh case by case bases lmao
                //*panicked wheezing*
                break;
        }

    }

    /**
     *
     * @param monSwap which trainer is swapping
     * @param swapTo which mon on their team they are swapping to - should be between 1 and 5, and ideally be a non fainted mon
     */
    public void swap(int monSwap, int swapTo){
        System.out.println("swap");
        if(swapTo<0||swapTo>pokemons.get(monSwap).size()-1){
            return;
        }//is a possible mon on team

        Pokemon temp = pokemons.get(monSwap).get(swapTo);
        if(pokemons.get(monSwap).get(swapTo)==null){
            System.out.println("Trying to swap to a null? cringe");
        }
        if(temp.isFainted()){
            return;
        }//they have fainted

        activeMons[monSwap]=swapTo;


    }

    /**
     * Figures out the turn order, then makes pokémon use moves, will update order if speeds change or mon faints
     * @param movesChosen the move chosen by the pokémon of index i, from its list of moves
     *                    -1 being passed means that player is out/lost
     *                    4 means that player is swapping
     * @param targets the targets of pokémon of index i's move
     */
    public void doTurn(int[] movesChosen, int[][] targets){
		System.out.println(movesChosen[0]);
        //only call once all moves from all mons have been chosen
        ArrayList<Move> moves= new ArrayList<>();
        ArrayList<Pokemon> yetToGo= new ArrayList<>();
        //Swapping
        for(int i=0; i<movesChosen.length; i++){
            if(movesChosen[i]==4){
                swap(i,targets[i][0]);
                movesToBeUsed[i]=-2;//take them out of turn order
                //swap to whatever - use target to get where they go to
            }//swap
            else if(movesChosen[i]==-1){

            }//do nothing as they have lost
            else{
				System.out.println(movesChosen[i]);
				if(pokemons.get(i).get(activeMons[i]).getMoves().get(movesChosen[i]) == null) {
					System.out.println("null f");
				}
				else {
					System.out.println(pokemons.get(i).get(activeMons[i]).getMoves().get(movesChosen[i]).stringify() + "w");
				}
                moves.add(pokemons.get(i).get(activeMons[i]).getMoves().get(movesChosen[i]));
                yetToGo.add(pokemons.get(i).get(activeMons[i]));
            }//they have an actual move
        }


        //Form changes
            //maybe later :)


        ArrayList<Integer> order= new ArrayList<>();

        for(int i=0; i<activeMons.length; i++){
            order.add(i);
        }

        order =turnReSorter(order);//order that the mons go in
        //System.out.printf("pokemon to go still :%s\n",order);

        Set<Integer> fainteds=new HashSet<>();
        int monNum=order.getFirst();
        //first get turn order
        while(movesToBeUsed[monNum]!=-2){

            Pokemon user=pokemons.get(order.get(0)).get(activeMons[order.get(0)]);
            System.out.printf("Pokemon going %s\n",user.getName());
            Move using=moves.get(monNum);
            useMove(monNum, movesChosen[monNum], targets[monNum]);
            movesToBeUsed[monNum]=-2;

            for(int i=0; i<activeMons.length; i++){
                if(pokemons.get(i).get(activeMons[i]).isFainted()){
                    System.out.printf("Mon %s fainted\n",pokemons.get(i).get(activeMons[i]).getName());
                    movesToBeUsed[i]=-2;//take them out of turn order
                    fainteds.add(i);
                }
                else{
                    System.out.printf("Mon %s alive\n",pokemons.get(i).get(activeMons[i]).getName());
                }
            }

            order =turnReSorter(order);
            /*
            System.out.printf("pokemon to go still :%s\n",order);
            for(int i=0; i<order.size(); i++){
                System.out.printf("Moves to be used is %d for team index %d\n",movesToBeUsed[order.get(i)], order.get(i));
            }
            */
            monNum=order.getFirst();

        }

        /*
        for(int i=0; i<order.size(); i++){
            System.out.printf("Moves to be used is %d for team index %d\n",movesToBeUsed[order.get(i)], order.get(i));
        }
        */


        /*
        while(!yetToGo.isEmpty()){
            System.out.printf("pokemon to go still :%s\n",yetToGo);
            int monNum=order.getFirst();
            if(movesChosen[monNum]==-2) break;//every has gone or is fainted
            Pokemon user=yetToGo.get(monNum);
            System.out.printf("Pokemon going %s\n",user.getName());
            Move using=moves.get(monNum);
            yetToGo.remove(user);
            moves.remove(using);
            movesToBeUsed[monNum]=-2;
            //remove from list
            useMove(monNum, movesChosen[monNum], targets[monNum]);
            ArrayList<Pokemon> temp=new ArrayList<>();
            ArrayList<Move> tempMove=new ArrayList<>();
            for(int i=0; i<yetToGo.size(); i++){
                if(!yetToGo.get(i).fainted){
                    System.out.printf("%s is hasn't fainted and will remain in turn order\n",yetToGo.get(i).getName());
                    temp.add(yetToGo.get(i));
                    tempMove.add(moves.get(i));
                    fainteds.add(i);
                }
                else{

                }
            }
            yetToGo=temp;
            moves=tempMove;
            //remove the dead
            order =turnReSorter(order);//re calc the order because speed changes
            //TODO:fix this messy code - more explained in the function
        }
        */
        //TODO : end turn effects

        //


        if(!fainteds.isEmpty()){
            //do if game is over

            ArrayList<Integer> standing=new ArrayList<>();
            for(int i=0; i<pokemons.size(); i++){
                if(inTheGame(i)) standing.add(i);
                int nextMon=nextAvailable(i);
                activeMons[i]=(nextMon);//Set Mode
            }// i is the team
            if(standing.size()>1){
                gameState=2;
            }
            else if(standing.size()==1){
                System.out.printf("Player %d wins\n", standing.getFirst());
                gameState=-1;
            }
            else{
                System.out.println("Draw");
            }//a draw....?
            System.out.printf("standing size is %d\n",standing.size());
        }
        else{
            gameState=0;
        }

        for(int i=0;i<pokeNumber;i++){
            movesToBeUsed[i]=-2;
            targetsToBeHit[i]=null;
        }//no moves have been chosen
        //A turn is over, if nobody has fainted, can go right back into asking for that, else gotta do fainteds

        //either set gameState to 0 or 2

        System.out.println("turn edned");
    }

    private int nextAvailable(int team){
        for(int i=0; i<pokemons.get(team).size(); i++){
            Pokemon p=pokemons.get(team).get(i);
            if (p==null) continue;
            if(!p.isFainted()) return i;
        }
        return -1;
    }

    public  boolean inTheGame(int i){
        for(Pokemon p: pokemons.get(i)){
			if(p == null){

            }
            else if(!p.isFainted()){
                return true;
            }
        }
        System.out.printf("PLayer %d lost\n",i);
        return false;
    }

    /**
     *
     * @param order
     * @return
     */
    public ArrayList<Integer> turnReSorter( ArrayList<Integer> order){

        /*
        ArrayList<Integer> modOrder=new ArrayList<>();
        for(int i=0; i<order.size(); i++){
            int in=order.get(i);
            if(movesToBeUsed[in]==-2){
                modOrder.add(in);
            }
            else{
                modOrder.add(0,in);
            }
        }
        */

        for(int i=0; i<order.size(); i++){

            for(int j=i+1; j<order.size(); j++){
                int in=order.get(i);
                int jn=order.get(j);
                Pokemon a= pokemons.get(in).get(activeMons[in]);//team 1's mon
                Pokemon b= pokemons.get(jn).get(activeMons[jn]);//team 2's mon
                if (movesToBeUsed[in]==-2){
                    int temp= order.get(i);
                    order.set(i, order.get(j));
                    order.set(j, temp);
                }//swap them with the other as they have already gone or are fainted?
                else if(movesToBeUsed[jn]==-2){

                }
                else {
                    //both pokemon are in play,
                    Move aMove=a.getMoves().get(movesToBeUsed[in]);
                    Move bMove=b.getMoves().get(movesToBeUsed[jn]);
                    if(aMove.getPriority()< bMove.getPriority() || (aMove.getPriority()== bMove.getPriority()&& a.getSpeed()< b.getSpeed() )) {
                        int temp= order.get(i);
                        order.set(i, order.get(j));
                        order.set(j, temp);
                    }

                }

            }
        }//is an n^2 search, but since n<5.... i dont care?

        return order;

    }

}

