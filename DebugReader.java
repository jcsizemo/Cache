import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class DebugReader {

	private static String filename;
	private int numberL1Reads = 0;
	private int numberL1Writes = 0;
	private int numberL2Reads = 0;
	private int numberL2Writes = 0;
	private int numberL1Misses = 0;
	private int numberL2Misses = 0;
	private int dirtyBitsVC = 0;
	private int dirtyBitsL1 = 0;
	private int dirtyBitsL2 = 0;
	private int numberOfCommands = -1;
	private int numberDirty = 0;
	private int numberReset = 0;
	private int memoryTrafficL1L2 = 0;
	private boolean gotIt = false;
	private int modulo = 72907;  
	private int VCSwaps = 0;
	private boolean L1SetAlready = false;
	private boolean L1Set = false;
	private int swaps = 0;
	private int inserts = 0;
	private int VCmisses = 0;
	
	public DebugReader(String filename) 
		{
		try
		{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		//BufferedWriter bw = new BufferedWriter(new FileWriter("DebugRun10Modified.txt",true));
			try {
				
				String command = br.readLine();
				
				
				while (command != null)
					{
					/*if (command.equals("L1 Set Dirty Bit") && L1Set)
						{
						L1SetAlready = true;
						}
					if (command.equals("L1 Set Dirty Bit"))
						L1Set = true;
					else
						{
						L1SetAlready=false;
						L1Set = false;
						}
					if (L1SetAlready)
						{
						bw.write("");
						bw.newLine();
						}
					
					else if (command.equals(" miss"))
					{
					bw.write("L1 miss");
					bw.newLine();
					}
					else
					{
					bw.write(command);
					bw.newLine();
					L1SetAlready = false;
					}
					*/
					
					if ((command.contains("L1 miss") && !command.contains("L1 miss rate")))
						{
						numberL1Misses++;
						gotIt = false;
						}
					if ((command.contains("L1 Read :")))
						{
						numberL1Reads++;
						gotIt = false;
						}
					if ((command.contains("L2 Read :")))
						{
						numberL2Reads++;
						gotIt = false;
						}
					if ((command.contains("L1 Set Dirty Bit")))
						dirtyBitsL1++;
					if ((command.contains("L2 Set Dirty Bit")))
						dirtyBitsL2++;
					if ((command.contains("VC Set Dirty Bit")))
						dirtyBitsVC++;
					if ((command.contains("VC swap start")))
						VCSwaps++;
					if ((command.contains("L1 Write :")))
						{
						numberL1Writes++;
						gotIt = false;
						}
					if (command.contains("VC miss"))
						VCmisses++;
						if ((command.contains("L2 Write :")))
						{
						numberL2Writes++;
						gotIt = false;
						}
					if ((command.contains("L2 miss") && !command.contains("L2 miss rate")))
						{
						numberL2Misses++;
						gotIt = false;
						}
					if ((command.contains("VC swap start")))
						swaps++;
					if ((command.contains("VC Inserting Block start")))
						inserts++;
					if ((command.contains("Set Dirty Bit")))
						numberDirty++;
					if ((command.contains("L1 Reset")))
						memoryTrafficL1L2++;
					if (command.contains("-------"))
						numberOfCommands++;
					
					command = br.readLine();
					if ((numberOfCommands%modulo == 0 && numberOfCommands != 0))
						{
						int y = 1;
						y = y%2;
						printStuff();
						gotIt = true;
						}
					}
				}
			catch (IOException ioe){}
				//bw.close();
				printStuff();
		}
		catch (IOException ioe){}
			
			
		//System.out.println("Number dirty: " + numberDirty);
		//System.out.println("Number reset: " + numberReset);
		
		}
	
	
	
	public static void main(String[] args)
	{
	filename = args[0];
	new DebugReader(filename);
	}
	
	void printStuff()
		{
		//System.out.println("Number of commands: " + numberOfCommands);
		//System.out.println("Memory traffic L1 L2: " + memoryTrafficL1L2);
		
		
		System.out.println("L1 reads: " + numberL1Reads);
		System.out.println("Misses L1: " + numberL1Misses);
		System.out.println("L1 writes: " + numberL1Writes);
		System.out.println("L2 reads: " + numberL2Reads);
		System.out.println("Misses L2: " + numberL2Misses);
		System.out.println("L2 writes: " + numberL2Writes);
		System.out.println("VC Swaps: "+ VCSwaps);
		System.out.println("L1 dirty bits: " + dirtyBitsL1);
		System.out.println("L2 dirty bits: " + dirtyBitsL2);
		System.out.println("VC dirty bits: " + dirtyBitsVC);
		System.out.println(swaps);
		System.out.println(inserts);
		System.out.println(VCmisses);
		}
		

}
