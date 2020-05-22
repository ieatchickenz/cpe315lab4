import java.util.Hashtable;
import java.util.stream.Stream;
import java.util.List;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Scanner;
import java.lang.String;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class lab4 {
	static int[] regList;
	static int[] dataMem;
	static int pc;
    static ArrayList<String> mylist;
    ArrayList<String> queue;
    ArrayList<Integer> pclist;

    public static void main(String[] args) 
    {
        //and, or, add, addi, sll, sub, slt, beq, bne, lw, sw, j, jr, and jal
	
	    //Need to make MIPS register (int array), data memory(int array 8192), PC
        int ic = 0;
        int cc = 0;
        regList = new int[32];
        Arrays.fill(regList, 0);

        dataMem = new int[8192];
        Arrays.fill(dataMem, 0);
        
        ArrayList<String> queue = new ArrayList<String>(2000);
        ArrayList<Integer> pclist = new ArrayList<Integer>(2000);

        mylist = new ArrayList<String>(4);
        pc = 0;
        
        //This is an array of instructions, index is pc
        List<instructionObject> program = new ArrayList<instructionObject>();

        List<List<String>> lineList = new ArrayList<List<String>>();

        Hashtable<String, instructionObject> lineObjectTable = new Hashtable<String, instructionObject>();
        lineObjectTable.put("and", new instructionObject("and", "R", "100100", "000000"));
        lineObjectTable.put("or", new instructionObject("or", "R", "100101", "000000"));
        lineObjectTable.put("add", new instructionObject("add", "R", "100000", "000000"));
        lineObjectTable.put("sub", new instructionObject("sub", "R", "100010", "000000"));
        lineObjectTable.put("addi", new instructionObject("addi", "I", "", "001000"));
        lineObjectTable.put("sll", new instructionObject("sll", "RS", "000000", "000000"));
        lineObjectTable.put("slt", new instructionObject("slt", "R", "101010", "000000"));
        lineObjectTable.put("beq", new instructionObject("beq", "I", "", "000100"));
        lineObjectTable.put("bne", new instructionObject("bne", "I", "", "000101"));
        lineObjectTable.put("lw", new instructionObject("lw", "IS", "", "100011"));
        lineObjectTable.put("sw", new instructionObject("sw", "IS", "", "101011"));
        lineObjectTable.put("j", new instructionObject("j", "J", "", "000010"));
        lineObjectTable.put("jr", new instructionObject("jr", "RJ", "001000", "000000"));
        lineObjectTable.put("jal", new instructionObject("jal", "J", "", "000011"));

        Hashtable<String, String> registerTable = new Hashtable<String, String>();
        registerTable.put("zero","00000");
        registerTable.put("0","00000");
        registerTable.put("v0","00010");
        registerTable.put("v1","00011");
        registerTable.put("a0","00100");
        registerTable.put("a1","00101");
        registerTable.put("a2","00110");
        registerTable.put("a3","00111");
        registerTable.put("t0","01000");
        registerTable.put("t1","01001");
        registerTable.put("t2","01010");
        registerTable.put("t3","01011");
        registerTable.put("t4","01100");
        registerTable.put("t5","01101");
        registerTable.put("t6","01110");
        registerTable.put("t7","01111");
        registerTable.put("s0","10000");
        registerTable.put("s1","10001");
        registerTable.put("s2","10010");
        registerTable.put("s3","10011");
        registerTable.put("s4","10100");
        registerTable.put("s5","10101");
        registerTable.put("s6","10110");
        registerTable.put("s7","10111");
        registerTable.put("t8","11000");
        registerTable.put("t9","11001");
        registerTable.put("sp","11101");
        registerTable.put("ra","11111");

        Hashtable<String, Integer> labels = new Hashtable<String, Integer>();

        //String x = instructionTable.get("jal");
        //System.out.println(x);
        //Read in .asm file
        
        //instructionObject i = new instructionObject("1", "1", "1", "1");
        //System.out.println(i.name);
        //System.out.println(i.register1);
        if(args.length == 0){
            System.out.println("Argument mismatch");
            System.out.println("Usage: lab3 input.asm [script]");
            return;
        }

	    File asmFile = new File(args[0]);
        int address = 0;

	    try(Stream<String> instructions = Files.lines(asmFile.toPath())){
            List<String> instList = instructions.map(String::trim)
                                    .filter(line -> line.length() > 0)
                                    .collect(Collectors.toList());

             for(int j=0;j<instList.size();j++){
                String line = instList.get(j);
                if(line.charAt(0) == '#'){
                    continue;
                }
                else{
                    //System.out.print("line: ");
                    //System.out.println(line);
                    String[] noComment = line.split("#");
                    String justInst = noComment[0];
                    String[] seperate = justInst.split("\\$|\\(|\\)|\\s|\\,");
                    ArrayList<String> list = Arrays.stream(seperate)
                             .filter(t->!t.isEmpty())
                             .map(String::trim)
                             .collect(Collectors.toCollection(ArrayList::new));
                    
                    //System.out.print("As list: ");
                    //System.out.println(list);
                    if(list.get(0).contains(":")){
                        //System.out.println(list.get(0));
                        //make sure to check after colon for instruction
                        int colon = list.get(0).indexOf(':',0);
                        int length = list.get(0).length();
                        String noColon = list.get(0).substring(0, colon);
                        if(list.get(0).charAt(length-1) != ':'){
                            list.set(0,list.get(0).substring(colon+1)); 
                        }
                        labels.put(noColon, address);
                    }
                    if(list.size() > 1){
                        if(list.get(0).contains(":")) {
                            list.remove(0);
                            //System.out.println(list);
                        }
                        lineList.add(list);
                    }
                    //conditional to check label: contains ':'
                    //System.out.println(address);
                    //System.out.println();
                    address += (list.size()<2) ? 0:1;
                }
            }
        //System.out.println(labels);
        //System.out.println(lineList);

        //hash the command name
        //load in list[1] and list[2]
        //depending on format load list[3] as imm/reg/addr
        //for jump, hash label for true destination address
        instructionObject temp = new instructionObject("1", "1", "1", "1");
        instructionObject invalid = new instructionObject("1", "1", "1", "1");
        
        
        for(int i = 0; i < lineList.size(); i++) {
            address = i;
            temp = lineObjectTable.getOrDefault(lineList.get(i).get(0), invalid);
            temp = new instructionObject(	temp.name, 
									            	temp.format, 
									            	temp.functioncode, 
									            	temp.opcode);
            int tempInt;
            switch (temp.format) {
                case "R":
                    temp.registerS = Integer.parseInt(registerTable.get(lineList.get(i).get(2)), 2);
                    temp.registerT = Integer.parseInt(registerTable.get(lineList.get(i).get(3)), 2);
                    temp.registerD = Integer.parseInt(registerTable.get(lineList.get(i).get(1)), 2);
                    break;

                case "RS":
                    
                    temp.registerT = Integer.parseInt(registerTable.get(lineList.get(i).get(2)), 2);
                    temp.registerD = Integer.parseInt(registerTable.get(lineList.get(i).get(1)), 2);
                    temp.shamt = Integer.parseInt(lineList.get(i).get(3));
                    break;

                case "I":
                    
                    temp.registerS = Integer.parseInt(registerTable.get(lineList.get(i).get(2)), 2);
                    temp.registerT = Integer.parseInt(registerTable.get(lineList.get(i).get(1)), 2);
                    
                    if((lineList.get(i).get(3).matches("-?([0-9]+)?[0-9]+")) || (lineList.get(i).get(3).matches("-"))){
                        tempInt = Integer.parseInt(lineList.get(i).get(3));
                        //System.out.println(lineList.get(i).get(3));
                    }
                    else{
                        tempInt = labels.get(lineList.get(i).get(3))-address-1; 
                    }
                    temp.immediate = tempInt;
                    break;

                case "IS":
                    
                    temp.registerS = Integer.parseInt(registerTable.get(lineList.get(i).get(3)), 2);
                    temp.registerT = Integer.parseInt(registerTable.get(lineList.get(i).get(1)), 2);
                    //System.out.print(temp.shamt.equals("") ? "00000" + " ":temp.shamt + " ");
                    //need to check if label
                    tempInt = Integer.parseInt(lineList.get(i).get(2));
                    temp.immediate = tempInt;
                    
                    break;

                case "J":
                    //has labels
                    //System.out.println("Jump Format");
                    //System.out.print(temp.opcode + " ");
                    if(lineList.get(i).get(1).matches("-?([0-9]+)?[0-9]+")){
                        tempInt = Integer.parseInt(lineList.get(i).get(1));
                    }
                    else{
                        tempInt = labels.get(lineList.get(i).get(1));
                    }
                    temp.immediate = tempInt;
                    //immed = String.format("%26s", Integer.toBinaryString(tempInt)).replace(" ", "0" );
                    //System.out.println(immed.substring(immed.length() -26) + " ");
                    break;

                case "RJ":
                    
                    temp.registerS = Integer.parseInt(registerTable.get(lineList.get(i).get(1)), 2);
                    
                    break;

                default:
                    System.out.print("invalid instruction: ");
                    System.out.println(lineList.get(i).get(0) + "\n");
                    return;
               }
               program.add(temp);
            }

        String takenString = "taken";
        String squashString = "squash";
        String stallString = "stall";
        pclist.add(pc);
        while(pc < program.size()) {
            queue.add(program.get(pc).name);
            temp = program.get(pc);
            //if we encounter a branch or a jump
            //emulator pc with switch immediately -> need to display
            //after the proper stage
            //in-between: "branch-<address>"
            //branch and jump issues
            switch (temp.name) {
                case "beq":
                    if(regList[temp.registerS] == regList[temp.registerT])
                    {
                        //taken
                        queue.add(program.get(pc).name);
                        queue.add(program.get(pc + 1).name);
                        queue.add(takenString);
                        pclist.add(pc);
                        pclist.add(pc + 1);
                        pclist.add(pc + 2);
                        pclist.add(pc + 3);
                        pclist.add(pc + 4);
                        cc += 3;
                    }
                    cc += 1;
                    break;

                case "bne":
                    if(regList[temp.registerS] != regList[temp.registerT])
                    {
                        //taken
                        queue.add(program.get(pc).name);
                        queue.add(program.get(pc + 1).name);
                        queue.add(takenString);
                        pclist.add(pc + 1);
                        pclist.add(pc + 2);
                        pclist.add(pc + 3);
                        pclist.add(pc + 4);
                        cc += 3;
                    }
                    cc += 1;
                    break;


                case "lw":
                    if((temp.registerT == program.get(pc+1).registerT) ||
                        (temp.registerT == program.get(pc+1).registerS))
                    {
                        queue.add(stallString);
                        pclist.add(pc + 1);
                        pclist.add(pc + 2);
                        cc += 2;
                    }
                    else
                    {
                        cc += 1;
                    }
                    break;


                case "j":
                    queue.add(squashString);
                    pclist.add(pc + 1);
                    pclist.add(pc + 2);
                    cc += 2;

                    break;


                case "jal":
                    queue.add(squashString);
                    pclist.add(pc + 1);
                    pclist.add(pc + 2);
                    cc += 2;

                    break;


                case "jr":
                    queue.add(squashString);
                    pclist.add(pc + 1);
                    pclist.add(pc + 2);
                    cc += 2;
                    break;

                    
                default:
                    pclist.add(pc + 1);
                    cc += 1;
                    break;
            }
            step(program.get(pc));
            pc += 1;
            //instruction count: +1 per instruction emulated
            //cycle count: +c per instruction emulated, c depending on stall/squash
            //CPI = cc/ic
            ic += 1;
        }
        for(int i = 0; i < 4; i++)
        {
            mylist.add(0, "empty");
        }
        /*for(int i = 0; i < queue.size(); i++)
        {
            System.out.println(queue.get(i));
        }
        pclist.add(0, 0);*/
        for(int i = 0; i < pclist.size(); i++)
        {
            System.out.println(pclist.get(i));
        }
        System.out.println("\n\n\n" + ic);
        System.out.println(cc);
        System.out.println(queue.size());

        //clear everything

        Arrays.fill(regList, 0);
        Arrays.fill(dataMem, 0);
        pc = 0;

        //first loop is for labels:
        //auto-discard whitespace (string.trim())
        //wrap -> array or direct -> array
        //first instruction is at address 0, increment by 1
        //hash table from label -> address

        //second pass:
        //have to subtract ht.get(label) from (current + 1) for jumps
        //funcTable only for register format
        //?make private class for lines read in? -> line object array
        //?make an array of lines?

        //from objects -> hash various object variables based on format
        //and concatenate to a string


        }
        catch(IOException ex){

        }


        //This is after the formatting has been done for the .asm
        Scanner in = new Scanner(System.in);
        String command;
        String[] commandArg;

        if(args.length<2 && args.length!=0){
            //Do prompt
            while(true){ 
                System.out.print("mips> ");
                command = in.nextLine();
                commandArg = command.split(" ");
                //System.out.println(Arrays.toString(commandArg));
                switch(commandArg[0]){
                    case "q":
                        in.close();
                        return;
                    case "quit":
                        in.close();
                        return;

                    case "h":
                        help();
                        System.out.println();
                        break;
                    case "help":
                        help();
                        System.out.println();
                        break;

                    case "d":
                        regDump(regList, pc);
                        System.out.println();
                        break;

                    case "p":
                        pipelinePrint(mylist, pclist.get(0));
                        break;

                    case "s":
                    		//determine fate of invalid PC address
                    		int tempint = 1;
                    		if(commandArg.length < 2)
                    		{
                    			tempint = 1;
                    		}
                    		else
                    		{
                    			tempint = Integer.parseInt(commandArg[1]);
                    		}
                    		for(int z = 0; z < tempint && z < program.size(); z++)
                    		{
                    			step(program.get(pc));
                        	pc += 1;
                    		}
                        System.out.println();

                        pipelineHandle(queue, pclist);
                        pipelinePrint(mylist, pclist.get(0));
                        break;

                    case "r":
                        while(pc < program.size()) {
                            step(program.get(pc));
                            pc += 1;
                        }
                        System.out.println();
                        System.out.println("Program complete");
                        System.out.println("CPI = " + ((float) cc)/((float) ic) + "Cycles = " + cc + "Instructions = " + ic);
                        break;

                    case "m":
                        if(commandArg.length > 3 || commandArg.length < 3){
                            System.out.println("Incorrect Formatting: type 'h' for help");
                        }
                        else{
                            memDisp(dataMem, Integer.parseInt(commandArg[1]), Integer.parseInt(commandArg[2]));
                        }
                        System.out.println();
                        break;

                    case "c":
                        Arrays.fill(regList, 0);
                        Arrays.fill(dataMem, 0);
                        pc = 0;
                        System.out.println("\tSimulator reset");
                        System.out.println();
                        break;

                    default:
                        System.out.println("Invalid command.");
                        System.out.println();
                        break;
                }

            }

        }
        else if(args.length == 2){
            //script read start here
            File scriptFile = new File(args[1]);

            try(Stream<String> commands = Files.lines(scriptFile.toPath())){
                List<String> commandList = commands.map(String::trim)
                                        .filter(line -> line.length() > 0)
                                        .collect(Collectors.toList());
                String[] oneCommand;
                for(int y = 0; y<commandList.size(); y++){
                    System.out.println("mips> " + commandList.get(y));
                    oneCommand = commandList.get(y).split(" ");
                    switch(oneCommand[0]){
                        case "q":
                            in.close();
                            return;
                        case "quit":
                            in.close();
                            return;
    
                        case "h":
                            help();
                            System.out.println();
                            break;
                        case "help":
                            help();
                            System.out.println();
                            break;
    
                        case "d":
                            regDump(regList, pc);
                            System.out.println();
                            break;

                        case "p":
                            pipelinePrint(mylist, pclist.get(0));
                            break;
    
                        case "s":
                                //determine fate of invalid PC address
                                int tempint = 1;
                                if(oneCommand.length < 2)
                                {
                                    tempint = 1;
                                }
                                else
                                {
                                    tempint = Integer.parseInt(oneCommand[1]);
                                }
                                for(int z = 0; z < tempint && z < program.size(); z++)
                                {
                                    step(program.get(pc));
                                    pc += 1;
                                }
                            System.out.println();

                            //need to remove from queue here after pipelinePrint
                            pipelineHandle(queue, pclist);    
                            pipelinePrint(mylist, pclist.get(0));
                            break;
    
                        case "r":
                            while(pc < program.size()) {
                                step(program.get(pc));
                                pc += 1;
                            }
                            System.out.println();
                            System.out.println("Program complete");
                            System.out.println("CPI = " + ((float) cc)/((float) ic) + "Cycles = " + cc + "Instructions = " + ic);

                            break;
    
                        case "m":
                            if(oneCommand.length > 3 || oneCommand.length < 3){
                                System.out.println("Incorrect Formatting: type 'h' for help");
                            }
                            else{
                                memDisp(dataMem, Integer.parseInt(oneCommand[1]), Integer.parseInt(oneCommand[2]));
                            }
                            System.out.println();
                            break;
    
                        case "c":
                            Arrays.fill(regList, 0);
                            Arrays.fill(dataMem, 0);
                            pc = 0;
                            System.out.println("\tSimulator reset");
                            System.out.println();
                            break;
    
                        default:
                            System.out.println("Invalid command.");
                            System.out.println();
                            break;
                    }
                }
                
            }
            catch(IOException exs){

            }

        }
        else{
            System.out.println("Argument mismatch");
            System.out.println("Usage: lab3 input.asm [script]");
        }
        
    }

    public static void pipelinePrint(ArrayList<String> mylist, int pc){
        System.out.println();
        System.out.print(pc);
        mylist.forEach(System.out::println);
    }

    public static void pipelineHandle(ArrayList<String> queue, ArrayList<Integer> pclist){
        //queue will delete first element after printing
        //same with pclist
        //String squashString = "squash";

        //mylist needs to persist between prints
        //mylist is modified here

        //step modifies the queue
        switch(queue.get(0)){
            case "taken":
                //taken
                break;

            case "stall":
                //keep "print array" index 0 the same, 
                //insert stall at 1 and advance others
                mylist.add(0, queue.get(1));
                mylist.remove(4);
                queue.remove(0);
                queue.add(0,"stallp2");
                pclist.remove(0);
                break;

            case "stallp2":
                //keep "print array" index 0 the same, 
                //insert stall at 1 and advance others
                mylist.add(1, "stall");
                mylist.remove(4);
                queue.remove(0);
                queue.remove(0);
                pclist.remove(0);
                break;

            default:
                mylist.add(0, queue.get(0));
                mylist.remove(4);
                queue.remove(0);
                pclist.remove(0);
                break;
        }

    }

    public static void help(){
        System.out.println();
        System.out.println("\th = show help");
        System.out.println("\td = dump register state");
        System.out.println("\tp = show pipeline registers");
        System.out.println("\ts = single step through the program (i.e. execute 1 instruction and stop)");
        System.out.println("\ts num = step through num instructions of the program");
        System.out.println("\tr = run until the program ends");
        System.out.println("\tm num1 num2 = display data memory from location num1 to num2");
        System.out.println("\tc = clear all registers, memory, and the program counter to 0");
        System.out.println("\tq = exit the program");
    }

    public static void regDump(int[] regList, int progCount){
        System.out.println();
        System.out.println("pc = " + progCount);
        System.out.println("$0 = " + regList[0] + "          $v0 = " + regList[2] + "          $v1 = " + regList[3] + "          $a0 = " + regList[4]);
        System.out.println("$a1 = " + regList[5] + "         $a2 = " + regList[6] + "          $a3 = " + regList[7] + "          $t0 = " + regList[8]);
        System.out.println("$t1 = " + regList[9] + "         $t2 = " + regList[10] + "          $t3 = " + regList[11] + "          $t4 = " + regList[12]);
        System.out.println("$t5 = " + regList[13] + "         $t6 = " + regList[14] + "          $t7 = " + regList[15] + "          $s0 = " + regList[16]);
        System.out.println("$s1 = " + regList[17] + "         $s2 = " + regList[18] + "          $s3 = " + regList[19] + "          $s4 = " + regList[20]);
        System.out.println("$s5 = " + regList[21] + "         $s6 = " + regList[22] + "          $s7 = " + regList[23] + "          $t8 = " + regList[24]);
        System.out.println("$t9 = " + regList[25] + "         $sp = " + regList[29] + "          $ra = " + regList[31]);
    }

    public static void step(instructionObject instruction){
      //a case statement
    	//return a string to be printed in 's' case
    	//ignore if we run
    	//and, or, add, addi, sll, sub, slt, beq, bne, lw, sw, j, jr, and jal
    	//return;
    	switch(instruction.name){
                    case "add":
                        regList[instruction.registerD] = regList[instruction.registerS] + regList[instruction.registerT];
                        return;

                    case "addi":	
                        regList[instruction.registerT] = regList[instruction.registerS] + instruction.immediate;
                        //System.out.println("ADDI:" + "T: " + instruction.registerT + " S: " + instruction.registerS + " immed: " + instruction.immediate);
                    		return;

                    	case "and":
                        regList[instruction.registerD] = regList[instruction.registerS] & regList[instruction.registerT];
                    		return;

                    	case "or":
                        regList[instruction.registerD] = regList[instruction.registerS] | regList[instruction.registerT];
                    		return;

                    	case "sll":
                        regList[instruction.registerD] = regList[instruction.registerT] << instruction.shamt;
                    		return;

                    	case "sub":
                        regList[instruction.registerD] = regList[instruction.registerS] - regList[instruction.registerT];
                    		return;

                    	case "slt":
                        regList[instruction.registerD] = (regList[instruction.registerS] < regList[instruction.registerT]) ? 1:0;
                    		return;

                    	case "beq":
                        pc = (regList[instruction.registerS] == regList[instruction.registerT]) ? pc + instruction.immediate : pc;
                    		return;

                    	case "bne":
                        pc = (regList[instruction.registerS] != regList[instruction.registerT]) ? pc + instruction.immediate : pc;
                    		return;

                    	case "lw":
                        regList[instruction.registerT] = dataMem[regList[instruction.registerS] + instruction.immediate];
                    		return;

                    	case "sw":
                        dataMem[regList[instruction.registerS] + instruction.immediate] = regList[instruction.registerT];
                    		return;

                    	case "j":
                            pc = instruction.immediate - 1;
                    		return;

                    	case "jr":
                    		pc = regList[instruction.registerS] - 1;
                    		return;

                    	case "jal":
                    		regList[31] = pc + 1;
                    		pc = instruction.immediate - 1;
                    		return;

                    	default:
                    		return;
         }
    }

    public static void run(){
    		//step a bunch
    		//end at end of instrs
    }

    public static void memDisp(int[] dataMem, int start, int end){
        int newEnd = end +1;
        for(int x = start; x < newEnd; x++){
            System.out.println("[" + x + "] = " + dataMem[x]);
        }
    }

}


class instructionObject {
    public String name;
    public String format;
    public String functioncode;
    public String opcode;
    public Integer registerS;
    public Integer registerT;
    public Integer registerD;
    public Integer immediate;
    public Integer shamt;

    instructionObject() 
    {
        this.name = "";
        this.format = "";
        this.functioncode = "";
        this.opcode = "";
    }

    instructionObject(String name, String format, String functioncode, String opcode) 
    {
        this.name = name;
        this.format = format;
        this.functioncode = functioncode;
        this.opcode = opcode;
        this.registerS = null;
        this.registerT = null;
        this.registerD = null;
        this.immediate = null;
        this.shamt = null;
    }

    @Override
    public String toString()
    {
        return "-----" + name + " Reg:" + immediate + "-----";
    }
    
}
//lab2 l = new lab2();
//lab2.instructionObject i = l.new instructionObject("1", "1", "1", "1");
