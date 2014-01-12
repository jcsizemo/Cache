// John Sizemore jcsizem2 ECE 521
// Cache Project: Part B


import java.io.*;
import java.math.*;

public class sim_cache
{	
	private boolean isRead	   = false;
	private boolean L1LRUenabled = false;
	private boolean L1WBWAenabled = false;
	private boolean L2LRUenabled = false;
	private boolean L2WBWAenabled = false;
//	private String data = "";
	private int numberBlockOffsetBitsL1 = 0;
	private int numberIndexBitsL1 = 0;
	private int numberTagBitsL1 = 0;
	private int numberBlockOffsetBitsL2 = 0;
	private int numberIndexBitsL2 = 0;
	private int numberTagBitsL2 = 0;
	private int numberBytesOfData = 8;
	private int numberOfCommands = 0;
	private int numberOfWriteMissesL1 = 0;
	private int numberOfWriteMissesL2 = 0;
	private int numberOfReadMissesL1 = 0;
	private int numberOfReadMissesL2 = 0;
	private int VCcommands = 0;
	private int VCmisses = 0;
	private int numberOfWritesL1 = 0;
	private int numberOfReadsL1 = 0;
	private int numberOfWritesL2 = 0;
	private int numberOfReadsL2 = 0;
	private int numberOfWriteBacksL1L2 = 0;
	private int numberOfWriteBacksL2Memory = 0;
	private int numberOfWriteBacksL1VC = 0;
	private int memoryTraffic = 0;
	private int numberOfCacheSetsL1 = 0;
	private int numberOfCacheSetsL2 = 0;
	private int VCswaps = 0;
	private int VCSwapRequests = 0;
	private double L1hitTime = 0;
	private double L2hitTime = 0;
	private double VChitTime = 0;
	//private double  = 0;
	private double L2missPenalty = 0;
	private double L1VCmissRate = 0;
	private double L2missRate = 0;
	private double SRR = 0;
	private boolean L1full = false;
	private CacheSet[] L1;
	private CacheSet[] L2;
	private CacheSet[] VC;
	private boolean VCenabled = false;
	private boolean pmIndexing = false;
	private boolean xorIndexing = false;
	//private int modulo = 40;
	private int L1dirty = 0;
	private int L2dirty = 0;
	private int VCdirty = 0;
	private int primeNumberL1 = 0;
	private int primeNumberL2 = 0;

	static int L1size;
	static int L1assoc;
	static int L1blocksize;
	static int L1replacement;
	static int L1writePolicy;
	static int vcNumblocks;
	static int L2blocksize;
	static int L2size;
	static int L2assoc;
	static int L2replacement;
	static int L2writePolicy;
	static int indexFunction;
	static String filename = "";
	
	
	public sim_cache()
		{
		if (L1replacement == 0)
			L1LRUenabled = true;
		if (L1writePolicy == 0)
			L1WBWAenabled = true;
		if (L2replacement == 0)
			L2LRUenabled = true;
		if (L2writePolicy == 0)
			L2WBWAenabled = true;
		if (indexFunction == 1)
			xorIndexing = true;
		if (indexFunction == 2)
			pmIndexing = true;
		
		numberOfCacheSetsL1 = numberOfSetsPowerOfTwoCheck(L1size,L1assoc,L1blocksize);
			if (numberOfCacheSetsL1 == 0)
				{
				System.out.println("Number of sets in L1 is not a power of two.");
				return;
				}
		
		numberOfCacheSetsL2 = numberOfSetsPowerOfTwoCheck(L2size,L2assoc,L2blocksize);
			if (numberOfCacheSetsL2 == 0)
				{
				System.out.println("Number of sets in L2 is not a power of two");
				return;
				}
			
		if (pmIndexing)
			{
			int numberOfFactorsL1 = 0;
			int numberOfFactorsL2 = 0;
			int numL1 = numberOfCacheSetsL1 + 1;
			int numL2 = numberOfCacheSetsL2 + 1;
				while(numberOfFactorsL1 != 2)
					{
					numberOfFactorsL1 = 0;
					for (int i = 1; i <= numL1; i++)
						{
						if (numL1%i == 0)
							numberOfFactorsL1++;
						}
					if (numberOfFactorsL1 != 2)
						numL1++;
					}
				primeNumberL1 = numL1;
				while(numberOfFactorsL2 != 2)
					{
					numberOfFactorsL2 = 0;
					for (int i = 1; i <= numL2; i++)
						{
						if (numL2%i == 0)
							numberOfFactorsL2++;
						}
					if (numberOfFactorsL2 != 2)
						numL2++;
					}
				primeNumberL2 = numL2;
			}
		
		numberBlockOffsetBitsL1 = (int) (Math.log(L1blocksize)/Math.log(2));
		numberIndexBitsL1 = (int) (Math.log(numberOfCacheSetsL1)/Math.log(2));
		numberTagBitsL1 = 32 - numberBlockOffsetBitsL1 - numberIndexBitsL1;
		
		numberBlockOffsetBitsL2 = (int) (Math.log(L2blocksize)/Math.log(2));
		numberIndexBitsL2 = (int) (Math.log(numberOfCacheSetsL2)/Math.log(2));
		numberTagBitsL2 = 32 - numberBlockOffsetBitsL2 - numberIndexBitsL2;
		
		L1 = new CacheSet[numberOfCacheSetsL1];				// initializing L1
		for (int i = 0; i < numberOfCacheSetsL1; i++)
			{
			L1[i] = new CacheSet();
			L1[i].setCount = 0;
			L1[i].index = decimalToBinary(i);
			L1[i].tag = new String[L1assoc];
			L1[i].blockOffset = new String[L1assoc];
				for (int h = 0; h < L1assoc; h++)
					{
					L1[i].tag[h] = "";
					L1[i].blockOffset[h] = "";
					}
			while (L1[i].index.length() != (Math.log(numberOfCacheSetsL1)/Math.log(2)))
				{
				if (numberOfCacheSetsL1 == 1)
					break;
				L1[i].index = "0" + L1[i].index;
				}
			L1[i].CacheBlock = new CacheBlock[L1assoc];
			for (int j = 0; j < L1assoc; j++)
				{
				L1[i].CacheBlock[j] = new CacheBlock();
				L1[i].CacheBlock[j].dataBytes = new String[numberBytesOfData];
				for (int k = 0; k < numberBytesOfData; k++)
					{
					L1[i].CacheBlock[j].dataBytes[k] = "";
					}
				}
			}
		
		L2 = new CacheSet[numberOfCacheSetsL2];				// initializing L2
		for (int i = 0; i < numberOfCacheSetsL2; i++)
			{
			L2[i] = new CacheSet();
			L2[i].setCount = 0;
			L2[i].index = decimalToBinary(i);
			L2[i].tag = new String[L2assoc];
			L2[i].blockOffset = new String[L2assoc];
				for (int h = 0; h < L2assoc; h++)
					{
					L2[i].tag[h] = "";
					L2[i].blockOffset[h] = "";
					}
			while (L2[i].index.length() != (Math.log(numberOfCacheSetsL2)/Math.log(2)))
				{
				if (numberOfCacheSetsL2 == 1)
					break;
				L2[i].index = "0" + L2[i].index;
				}
			L2[i].CacheBlock = new CacheBlock[L2assoc];
			for (int j = 0; j < L2assoc; j++)
				{
				L2[i].CacheBlock[j] = new CacheBlock();
				L2[i].CacheBlock[j].dataBytes = new String[numberBytesOfData];
				for (int k = 0; k < numberBytesOfData; k++)
					{
					L2[i].CacheBlock[j].dataBytes[k] = "";
					}
				}
			}
		
		if (vcNumblocks > 0)
			{
			VCenabled = true;
			VC = new CacheSet[vcNumblocks];				// initializing VC, VC is just a list of tags, indexes, and offsets
			for (int i = 0; i < vcNumblocks; i++)
				{
				VC[i] = new CacheSet();
				VC[i].tag = new String[1];
				VC[i].blockOffset = new String[1];
				VC[i].tag[0] = "";
				VC[i].blockOffset[0] = "";

				VC[i].CacheBlock = new CacheBlock[1];
				VC[i].CacheBlock[0] = new CacheBlock();
				VC[i].CacheBlock[0].blockCount = 0;
				}
			}
		
		// a cache set is just an array of cache blocks
		
		try
			{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			try
				{
				String command = br.readLine();
				while (command != null)
					{
					
					if (command.startsWith("r") || command.startsWith("R"))
						{
						isRead = true;
						L1full = false;
						command = command.substring(1,command.length()).trim();
						BigInteger bi = new BigInteger(command, 16);
						String binaryCommand = bi.toString(2);
						if (binaryCommand.length() != 32)
							{
							for (int i = 32 - binaryCommand.length(); i > 0; i--)
								{
								binaryCommand = "0" + binaryCommand;
								}
							}
						String tagL1 = binaryCommand.substring(0,numberTagBitsL1);
						tagL1 = binaryToHex(tagL1);
						String indexL1 = binaryCommand.substring(numberTagBitsL1,numberTagBitsL1+numberIndexBitsL1);
						if (indexL1.length() == 0)
							indexL1 = "0";
						String blockOffsetL1 = binaryCommand.substring(numberTagBitsL1+numberIndexBitsL1);
						
						String tagL2 = binaryCommand.substring(0,numberTagBitsL2);
						tagL2 = binaryToHex(tagL2);
						String indexL2 = binaryCommand.substring(numberTagBitsL2,numberTagBitsL2+numberIndexBitsL2);
						if (indexL2.length() == 0)
							indexL2 = "0";
						String blockOffsetL2 = binaryCommand.substring(numberTagBitsL2+numberIndexBitsL2);
/*						String bite = "";
						System.out.println(binaryCommand);
						System.out.println("Tag is: " + tag);
						System.out.println("Index is: " + index);
						System.out.println("Block offset is: " + blockOffset);
						int tagDec = binaryToDecimal(tag);
						int indexDec = binaryToDecimal(index);
						int blockOffsetDec = binaryToDecimal(blockOffset);
						System.out.println("Tag (in decimal) is: " + tagDec);
						System.out.println("Index (in decimal) is: " + indexDec);
						System.out.println("Block offset (in decimal) is: " + blockOffsetDec);*/
						numberOfCommands++;
						
						if (!checkCacheL1ForTag(tagL1, indexL1, L1assoc))  			// The condition is true if there's a hit in L1
							{
							numberOfReadMissesL1++;
							
							if (checkVCforTag(tagL1,indexL1) && VCenabled && L1full)
								{
								swapWithVC(tagL1,indexL1);
								
								VCSwapRequests++;
								VCswaps++;
								}
							else
								{
								if (L1full && VCenabled)
									{
									VCmisses++;
									storeInVC(tagL1,indexL1,blockOffsetL1);
									
									VCSwapRequests++;
									}
								else
									{
									if (L1full)
										replaceInCacheL1(tagL1, indexL1, blockOffsetL1);
									else
										storeInCacheL1(tagL1,indexL1, blockOffsetL1);
									}
								
							
								isRead = true;
								if (!checkCacheL2ForTag(tagL2, indexL2, L2assoc))
									{
									numberOfReadMissesL2++;								// if L1 and L2 miss, go here
									storeInCacheL2(tagL2, indexL2, blockOffsetL2);
									}
								}
							}
						}
					
					
					else if (command.startsWith("W") || command.startsWith("w"))
						{
						isRead = false;
						L1full = false;
						command = command.substring(1,command.length()).trim();
						BigInteger bi = new BigInteger(command, 16);
						String binaryCommand = bi.toString(2);
						if (binaryCommand.length() != 32)
							{
							for (int i = 32 - binaryCommand.length(); i > 0; i--)
								{
								binaryCommand = "0" + binaryCommand;
								}
							}
//						System.out.println(binaryCommand);
						String tagL1 = binaryCommand.substring(0,numberTagBitsL1);
						tagL1 = binaryToHex(tagL1);
						String indexL1 = binaryCommand.substring(numberTagBitsL1,numberTagBitsL1+numberIndexBitsL1);
						if (indexL1.length() == 0)
							indexL1 = "0";
						String blockOffsetL1 = binaryCommand.substring(numberTagBitsL1+numberIndexBitsL1);
						
						String tagL2 = binaryCommand.substring(0,numberTagBitsL2);
						tagL2 = binaryToHex(tagL2);
						String indexL2 = binaryCommand.substring(numberTagBitsL2,numberTagBitsL2+numberIndexBitsL2);
						if (indexL2.length() == 0)
							indexL2 = "0";
						String blockOffsetL2 = binaryCommand.substring(numberTagBitsL2+numberIndexBitsL2);
//						String bite = "";
//						System.out.println(binaryCommand);
//						System.out.println("Tag is: " + tag);
//						System.out.println("Index is: " + index);
//						System.out.println("Block offset is: " + blockOffset);
//						int tagDec = binaryToDecimal(tag);
//						int indexDec = binaryToDecimal(index);
//						int blockOffsetDec = binaryToDecimal(blockOffset);
//						System.out.println("Tag (in decimal) is: " + tagDec);
//						System.out.println("Index (in decimal) is: " + indexDec);
//						System.out.println("Block offset (in decimal) is: " + blockOffsetDec);
						numberOfCommands++;
						
						if (!checkCacheL1ForTag(tagL1, indexL1, L1assoc))
							{
							numberOfWriteMissesL1++;
							
							
							if (checkVCforTag(tagL1,indexL1) && VCenabled && L1full)
								{
								
								swapWithVC(tagL1,indexL1);
								VCswaps++;
								VCSwapRequests++;
								}
							
							else
								{
								if (L1full && VCenabled && L1WBWAenabled)
									{
									
									storeInVC(tagL1,indexL1,blockOffsetL1);
									VCSwapRequests++;
									VCmisses++;
									}
								else
									{
									if (L1full && L1WBWAenabled)
										replaceInCacheL1(tagL1, indexL1, blockOffsetL1);
									else if (L1WBWAenabled)
										storeInCacheL1(tagL1, indexL1, blockOffsetL1);
									}
							
								if (L1WBWAenabled)
									isRead = true;
								if (!checkCacheL2ForTag(tagL2,indexL2, L2assoc))
									{
									if (!isRead)
										numberOfWriteMissesL2++;
									else
										numberOfReadMissesL2++;
									if (L2WBWAenabled)
										storeInCacheL2(tagL2,indexL2, blockOffsetL2);
									}
								}
							}
						
						else if (!L1WBWAenabled)
								{
								if (!checkCacheL2ForTag(tagL2,indexL2, L2assoc))
									{
									if (!isRead)
										numberOfWriteMissesL2++;
									else
										numberOfReadMissesL2++;
									storeInCacheL2(tagL2,indexL2,blockOffsetL2);
									}
								}
							
						}
					if (numberOfCommands == 10000) // error at 72907
						{						
						int y = 1;				
						printStuff();
						y = y+1;
						}
					command = br.readLine();
					}
				br.close();
				//System.out.println("Number of commands: " + numberOfCommands);
				printStuff();
				}
			
			catch (IOException ioe)
				{
				System.out.println(ioe.getMessage());
				}
			}
		
		catch (FileNotFoundException fnfe)
			{
			System.out.println("Trace file not found.");
			return;
			}
		}
	
	
	public static void main(String[] args) 
	{
	if (args.length != 13)
		{
		System.out.println("Not enough command line arguments for adequate simulation.");
		return;
		}
	L1size = 0;
	L1assoc = 0;
	L1blocksize = 0;
	L1replacement = 0;
	L1writePolicy = 0;
	filename = "";
	try 
		{
		L1blocksize = Integer.parseInt(args[0]);
			if (L1blocksize <= 0)
				{
				System.out.println("Zero or negative block size for L1.");
				return;
				}
			int powerChecker = 1;
			while (powerChecker < L1blocksize)
				{
				powerChecker = powerChecker*2;
				}
			if (powerChecker != L1blocksize)
				{
				System.out.println("Block size is not a power of two.");
				return;
				}
			
		L1size = Integer.parseInt(args[1]);
			if (L1size <= 0)
				{
				System.out.println("Size for L1 is zero or negative.");
				return;
				}
			if (L1blocksize > L1size)
				{
				System.out.println("The block size for L1 is larger than the L1 cache size.");
				return;
				}
			
		L1assoc = Integer.parseInt(args[2]);
			if (L1assoc <= 0)
				{
				System.out.println("L1 associativity less than or equal to zero");
				return;
				}
				
		L1replacement = Integer.parseInt(args[3]);
			if (L1replacement != 0 && L1replacement != 1)
				{
				System.out.println("L1 replacement must be either a 0 or 1.");
				return;
				}
			
		L1writePolicy = Integer.parseInt(args[4]);
			if (L1writePolicy != 0 && L1writePolicy != 1)
			{
			System.out.println("L1 replacement must be either a 0 or 1.");
			return;
			}
			
		vcNumblocks = Integer.parseInt(args[5]);
		
	L2blocksize = Integer.parseInt(args[6]);
		if (L2blocksize <= 0)
			{
			System.out.println("Zero or negative block size for L2.");
			return;
			}
		int powerChecker2 = 1;
		while (powerChecker2 < L2blocksize)
			{
			powerChecker2 = powerChecker2*2;
			}
		if (powerChecker2 != L2blocksize)
			{
			System.out.println("Size for L2 is not a power of two.");
			return;
			}
		
	L2size = Integer.parseInt(args[7]);
		if (L2size <= 0)
			{
			System.out.println("Block size for L2 is zero or negative.");
			return;
			}
		if (L2blocksize > L2size)
			{
			System.out.println("The block size for L2 is larger than the cache L2 size.");
			return;
			}
		
	L2assoc = Integer.parseInt(args[8]);
		if (L2assoc <= 0)
			{
			System.out.println("L2 associativity less than or equal to zero");
			return;
			}
			
	L2replacement = Integer.parseInt(args[9]);
		if (L2replacement != 0 && L2replacement != 1)
			{
			System.out.println("L2 replacement must be either a 0 or 1.");
			return;
			}
		
	L2writePolicy = Integer.parseInt(args[10]);
		if (L2writePolicy != 0 && L2writePolicy != 1)
		{
		System.out.println("L2 write policy must be either a 0 or 1.");
		return;
		}
		
		indexFunction = Integer.parseInt(args[11]);
		if (indexFunction < 0 || indexFunction > 2)
			{
			System.out.println("Please input an index function of 0, 1, or 2.");
			return;
			}
			
		filename = args[12];
		/*	if (filename.length() > 11)
				{
				System.out.println("Filename is more than 11 characters long.");
				return;
				}*/
		}
	catch (NumberFormatException nfe)
		{
		System.out.println("One or more input parameters illegal.");
		}
	/*for (L1size = 1; L1size < (512*1024); L1size += L1size)
		for (L1blocksize = 1; L1blocksize < L1size; L1blocksize += L1blocksize)
			for (L1assoc)*/
	new sim_cache();

	}
	
	int numberOfSetsPowerOfTwoCheck(int L1size, int L1assoc, int L1blockL1size)
		{
		Integer L1sizeI = new Integer(L1size);
		double L1sizeD = L1sizeI.doubleValue();
		Integer L1assocI = new Integer(L1assoc);
		double L1assocD = L1assocI.doubleValue();
		Integer L1blockL1sizeI = new Integer(L1blockL1size);
		double L1blockL1sizeD = L1blockL1sizeI.doubleValue();
		
		double numberOfSets = (L1sizeD)/(L1assocD*L1blockL1sizeD);
		int powerChecker = 1;
		while (powerChecker < numberOfSets)
			{
			powerChecker = powerChecker*2;
			}
		if (powerChecker != numberOfSets)
			{
			return 0;
			}
		Double numberOfSetsD = new Double(numberOfSets);
		int numSets = numberOfSetsD.intValue();
		return numSets;
		}
	
	int binaryToDecimal(String binary)
		{
		BigInteger bi = new BigInteger(binary,2);
		String decString = bi.toString(10);
		int decimal = Integer.parseInt(decString);
		return decimal;
		}
	
	long binaryToLong(String binary)
		{
		BigInteger bi = new BigInteger(binary,2);
		String decString = bi.toString(10);
		long decimal = Long.parseLong(decString);
		return decimal;
		}
	
	String decimalToBinary(int decimal)
		{
		Integer decI = new Integer(decimal);
		String binary = decI.toString();
		BigInteger bi = new BigInteger(binary,10);
		binary = bi.toString(2);
		return binary;
		}
	
	String binaryToHex(String binary)
		{
		BigInteger bi = new BigInteger(binary,2);
		String hex = bi.toString(16);
		return hex;
		}
	
	String hexToBinary(String hex)
		{
		BigInteger bi = new BigInteger(hex,16);
		String binary = bi.toString(2);
		return binary;
		}
	
	boolean checkVCforTag(String L1tag, String L1index)
		{
		int VCcount = 0;
		boolean inVC = false;
		String L1tagBin = hexToBinary(L1tag);
		String searchTag = L1tagBin + L1index;
		searchTag = binaryToHex(searchTag);
			for (int i = 0; i < vcNumblocks; i++)
				{
				if (VC[i].tag[0].equals(searchTag))
					inVC = true;
				if (VC[i].tag[0].length() > 0)
					VCcount++;
				}

		return inVC;
		}
	
	void storeInVC(String tagL1, String indexL1, String blockOffsetL1)
		{
		int replaceIndexL1 = 0;
		int storeIndexVC = 0;
		int setIndexL1 = 0;
		int blockCountL1 = 0;
		int blockCountVC = VC[0].CacheBlock[0].blockCount;
		boolean VCvacant = false;
		boolean tempDirty = false;
		String tempTag = "";
		String tempBlockOffset = "";
		String VCtag = "";
		VCcommands++;
		
		if (xorIndexing)
			{
			String tagL1Bin = hexToBinary(tagL1);
			if (tagL1Bin.length() != numberTagBitsL1)
				{
				for (int i = numberTagBitsL1 - tagL1Bin.length(); i>0; i--)
					{
					tagL1Bin = "0" + tagL1Bin;
					}
				}
			String left6BitsL1Tag = tagL1Bin.substring(numberTagBitsL1-numberIndexBitsL1,numberTagBitsL1);
			int left6Bits = binaryToDecimal(left6BitsL1Tag);
			int realIndexInt = binaryToDecimal(indexL1);
			int xorIndexInt = left6Bits^realIndexInt;
			String xorIndex = decimalToBinary(xorIndexInt);
			if (xorIndex.length() != numberIndexBitsL1)
				{
				for (int i = numberIndexBitsL1 - xorIndex.length(); i>0; i--)
					{
					xorIndex = "0" + xorIndex;
					}
				}
			setIndexL1 = xorIndexInt;
			blockCountL1 = L1[setIndexL1].CacheBlock[0].blockCount;
			VCtag = hexToBinary(tagL1) + indexL1;
			}
		else if (pmIndexing)
			{
			String binaryL1Tag = hexToBinary(tagL1);
			String A = binaryL1Tag+indexL1;
			long Along = binaryToLong(A);
			int Aint = (int) Along;
			setIndexL1 = Aint % primeNumberL1 % numberOfCacheSetsL1;
			blockCountL1 = L1[setIndexL1].CacheBlock[0].blockCount;
			VCtag = hexToBinary(tagL1) + indexL1;
			}
		else
			{
			setIndexL1 = binaryToDecimal(indexL1);
			blockCountL1 = L1[setIndexL1].CacheBlock[0].blockCount;
			VCtag = hexToBinary(tagL1) + indexL1;
			}
		
		VCtag = binaryToHex(VCtag);
		
			for (int i = 1; i < L1assoc; i++)			// for loop finds the highest block count
				{
				if ((L1[setIndexL1].CacheBlock[i].blockCount > blockCountL1) && L1LRUenabled)
					{
					blockCountL1 = L1[setIndexL1].CacheBlock[i].blockCount;
					replaceIndexL1 = i;					// replace index updated every time a larger block count is found
					}
				if ((L1[setIndexL1].CacheBlock[i].blockCount < blockCountL1) && !L1LRUenabled)
					{
					blockCountL1 = L1[setIndexL1].CacheBlock[i].blockCount;	// replace index updated every time a smaller block count is found
					replaceIndexL1 = i;
					}
				}
			
			for (int i = 0; i < vcNumblocks; i++)
				{
				if (VC[i].tag[0].length() == 0)
					{
					storeIndexVC = i;
					VCvacant = true;
					break;
					}
				}
			
			if (!VCvacant)
				{
				for (int i = 0; i < vcNumblocks; i++)
					{
					if (VC[i].CacheBlock[0].blockCount > blockCountVC)
						{
						storeIndexVC = i;
						blockCountVC = VC[i].CacheBlock[0].blockCount;
						}
					}
				}
			
			if (L1full && !VCvacant && L2WBWAenabled && VC[storeIndexVC].CacheBlock[0].isDirty)
				{
				boolean oldIsRead = isRead;
				isRead = false;
				String oldL1TagPlusIndex = hexToBinary(VC[storeIndexVC].tag[0]);
				if (oldL1TagPlusIndex.length() != numberTagBitsL1 + numberIndexBitsL1)
					{
					for (int i = (numberTagBitsL1+numberIndexBitsL1) - oldL1TagPlusIndex.length(); i>0; i--)
						{
						oldL1TagPlusIndex = "0"+oldL1TagPlusIndex;
						}
					}
				String oldL1tag = oldL1TagPlusIndex.substring(0,numberTagBitsL1);
				String oldL1index = oldL1TagPlusIndex.substring(numberTagBitsL1,numberTagBitsL1+numberIndexBitsL1);
				String oldL1blockOffset = VC[storeIndexVC].blockOffset[0];
				String storeInL2Command = oldL1tag + oldL1index + oldL1blockOffset;
				
				String storeInL2tag = storeInL2Command.substring(0,numberTagBitsL2);
				storeInL2tag = binaryToHex(storeInL2tag);
				String storeInL2index = storeInL2Command.substring(numberTagBitsL2,numberTagBitsL2+numberIndexBitsL2);
				String storeInL2blockOffset = storeInL2Command.substring(numberTagBitsL2+numberIndexBitsL2);
				numberOfWriteBacksL1VC++;
				//System.out.println(numberOfCommands);			// DO NOT Implement indexing function here. DONT IMPLEMENT when passing
				//System.out.println(storeInL2tag);				into a function. May need to modify certain things like getting the old
				//System.out.println(storeInL2index);			// original L1 index out of the oldL1index to reconstruct the addr and send to L2
				if (!checkCacheL2ForTag(storeInL2tag,storeInL2index,L2assoc))
					{
					storeInCacheL2(storeInL2tag,storeInL2index,storeInL2blockOffset);
					}
				//VC[storeIndexVC].CacheBlock[0].isDirty = false;
				isRead = oldIsRead;
				}
			
				String oldL1Tag = hexToBinary(L1[setIndexL1].tag[replaceIndexL1]);    // error here?
				if (oldL1Tag.length() != numberTagBitsL1 && !pmIndexing)
				{
				for (int i = numberTagBitsL1 - oldL1Tag.length(); i > 0; i--)
					{	
					oldL1Tag = "0" + oldL1Tag;
					}
				}
			else
				if (oldL1Tag.length() != numberTagBitsL1 + numberIndexBitsL1)
				{
				for (int i = (numberTagBitsL1+numberIndexBitsL1) - oldL1Tag.length(); i > 0; i--)
					{	
					oldL1Tag = "0" + oldL1Tag;
					}
				oldL1Tag = oldL1Tag.substring(0,numberTagBitsL1);
				}
			tempDirty = L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty;
			if (tempDirty)
				VCdirty++;
			tempTag = oldL1Tag + indexL1;
			tempTag = binaryToHex(tempTag);
			tempBlockOffset = L1[setIndexL1].blockOffset[replaceIndexL1];
			if (L1WBWAenabled && !isRead)
				{
				L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty = true;
				L1dirty++;
				}
			else
				L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty = false;
			
			if (pmIndexing)
				{
				L1[setIndexL1].tag[replaceIndexL1] = VCtag;
				}
			else
				{
				L1[setIndexL1].tag[replaceIndexL1] = tagL1;					// error??
				}
			L1[setIndexL1].blockOffset[replaceIndexL1] = blockOffsetL1;
			VC[storeIndexVC].CacheBlock[0].isDirty = tempDirty;
			VC[storeIndexVC].tag[0] = tempTag;
			VC[storeIndexVC].blockOffset[0] = tempBlockOffset;
			VC[storeIndexVC].CacheBlock[0].blockCount = 0;
			L1[setIndexL1].CacheBlock[replaceIndexL1].blockCount = 0;
			
			if (L1LRUenabled)
				{
				for (int i = 0; i < L1assoc; i++)
					{
					if (i != replaceIndexL1)
						L1[setIndexL1].CacheBlock[i].blockCount++;
					}
				}
			
			else
				L1[setIndexL1].CacheBlock[replaceIndexL1].blockCount++;
		
			for (int i = 0; i < vcNumblocks; i++)
				{
				if (i != storeIndexVC)
					VC[i].CacheBlock[0].blockCount++;
				}
		}
	
	void swapWithVC(String L1tag, String indexL1) // need tag to look through VC to find correct entry, need index so L1 can find a block to swap
		{
		int replaceIndexL1 = 0;
		int replaceIndexVC = 0;
		int setIndexL1 = 0;
		int blockCountL1 = 0;
		String VCtag = "";
		
		if (xorIndexing)
		{
		String tagL1Bin = hexToBinary(L1tag);
		if (tagL1Bin.length() != numberTagBitsL1)
			{
			for (int i = numberTagBitsL1 - tagL1Bin.length(); i>0; i--)
				{
				tagL1Bin = "0" + tagL1Bin;
				}
			}
		String left6BitsL1Tag = tagL1Bin.substring(numberTagBitsL1-numberIndexBitsL1,numberTagBitsL1);
		int left6Bits = binaryToDecimal(left6BitsL1Tag);
		int realIndexInt = binaryToDecimal(indexL1);
		int xorIndexInt = left6Bits^realIndexInt;
		String xorIndex = decimalToBinary(xorIndexInt);
		if (xorIndex.length() != numberIndexBitsL1)
			{
			for (int i = numberIndexBitsL1 - xorIndex.length(); i>0; i--)
				{
				xorIndex = "0" + xorIndex;
				}
			}
		setIndexL1 = xorIndexInt;
		blockCountL1 = L1[setIndexL1].CacheBlock[0].blockCount;
		VCtag = hexToBinary(L1tag) + indexL1;
		}
	else if (pmIndexing)
		{
		String binaryL1Tag = hexToBinary(L1tag);
		String A = binaryL1Tag+indexL1;
		long Along = binaryToLong(A);
		int Aint = (int) Along;
		setIndexL1 = Aint % primeNumberL1 % numberOfCacheSetsL1;
		blockCountL1 = L1[setIndexL1].CacheBlock[0].blockCount;
		VCtag = hexToBinary(L1tag) + indexL1;	
		}
	else
		{
		setIndexL1 = binaryToDecimal(indexL1);
		blockCountL1 = L1[setIndexL1].CacheBlock[0].blockCount;
		VCtag = hexToBinary(L1tag) + indexL1;
		}
		
		
		
		VCcommands++;
		
		boolean tempDirty = false;
		String tempTag = "";
		String tempBlockOffset = "";
		
		VCtag = binaryToHex(VCtag);
		
			for (int i = 1; i < L1assoc; i++)			// for loop finds the highest block count
				{
				if ((L1[setIndexL1].CacheBlock[i].blockCount > blockCountL1) && L1LRUenabled)
					{
					blockCountL1 = L1[setIndexL1].CacheBlock[i].blockCount;
					replaceIndexL1 = i;					// replace index updated every time a larger block count is found
					}
				if ((L1[setIndexL1].CacheBlock[i].blockCount < blockCountL1) && !L1LRUenabled)
					{
					blockCountL1 = L1[setIndexL1].CacheBlock[i].blockCount;	// replace index updated every time a smaller block count is found
					replaceIndexL1 = i;
					}
				}
			
			for (int i = 0; i < vcNumblocks; i++)
				{
				if (VC[i].tag[0].equals(VCtag))
					{
					replaceIndexVC = i;
					break;
					}
				}
									
			
			String oldL1Tag = hexToBinary(L1[setIndexL1].tag[replaceIndexL1]);   // error here?
			if (oldL1Tag.length() != numberTagBitsL1 && !pmIndexing)
				{
				for (int i = numberTagBitsL1 - oldL1Tag.length(); i > 0; i--)
					{	
					oldL1Tag = "0" + oldL1Tag;
					}
				}
			else
				if (oldL1Tag.length() != numberTagBitsL1 + numberIndexBitsL1)
				{
				for (int i = (numberTagBitsL1+numberIndexBitsL1) - oldL1Tag.length(); i > 0; i--)
					{	
					oldL1Tag = "0" + oldL1Tag;
					}
				oldL1Tag = oldL1Tag.substring(0,numberTagBitsL1);
				}
			tempTag = oldL1Tag + indexL1;
			tempTag = binaryToHex(tempTag);
			String oldVCtag = hexToBinary(VC[replaceIndexVC].tag[0]);
			if (oldVCtag.length() != (numberTagBitsL1 + numberIndexBitsL1))
				{
				for (int i = (numberTagBitsL1+numberIndexBitsL1) - oldVCtag.length(); i > 0; i --)
					{
					oldVCtag = "0" + oldVCtag;
					}
				}
			tempDirty = L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty;
			if (tempDirty)
				VCdirty++;
			tempBlockOffset = L1[setIndexL1].blockOffset[replaceIndexL1];
			L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty = VC[replaceIndexVC].CacheBlock[0].isDirty;
			if (L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty)
				L1dirty++;
			L1[setIndexL1].tag[replaceIndexL1] = binaryToHex(oldVCtag.substring(0,numberTagBitsL1));
			L1[setIndexL1].blockOffset[replaceIndexL1] = VC[replaceIndexVC].blockOffset[0];
			VC[replaceIndexVC].CacheBlock[0].isDirty = tempDirty;
			VC[replaceIndexVC].tag[0] = tempTag;
			VC[replaceIndexVC].blockOffset[0] = tempBlockOffset;
			VC[replaceIndexVC].CacheBlock[0].blockCount = 0;
			L1[setIndexL1].CacheBlock[replaceIndexL1].blockCount = 0;
			
			if (!isRead)
				L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty = true;
			
			if (L1LRUenabled)
			{
			for (int i = 0; i < L1assoc; i++)
				{
				if (i != replaceIndexL1)
					L1[setIndexL1].CacheBlock[i].blockCount++;
				}
			}
		
			else
				L1[setIndexL1].CacheBlock[replaceIndexL1].blockCount++;
			
			for (int i = 0; i < vcNumblocks; i++)
				{
				if (i != replaceIndexVC)
					VC[i].CacheBlock[0].blockCount++;
				}
			
		}
	
	void storeInCacheL1(String tagL1, String indexL1, String blockOffsetL1)  // This function will store a block in Cache L1 if there is a vacancy
		{
		int setIndexL1 = 0;
		if (xorIndexing)		// Get index from these if statement depending on the indexing function
			{
			String binTag = hexToBinary(tagL1);
			String tagIndexL1 = binTag + indexL1;
			if (tagIndexL1.length() != numberTagBitsL1 + numberIndexBitsL1)
				{
				for (int i = (numberTagBitsL1 + numberIndexBitsL1)-tagIndexL1.length(); i>0; i--)
					{
					tagIndexL1 = "0" + tagIndexL1;
					}
				}
			String indexX = tagIndexL1.substring(numberTagBitsL1-numberIndexBitsL1,numberTagBitsL1);
			String indexY = tagIndexL1.substring(numberTagBitsL1,numberTagBitsL1+numberIndexBitsL1);
			int xInt = binaryToDecimal(indexX);
			int yInt = binaryToDecimal(indexY);
			setIndexL1 = xInt^yInt;
			}
		else if (pmIndexing)
			{
			String binaryL1Tag = hexToBinary(tagL1);
			String A = binaryL1Tag+indexL1;
			long Along = binaryToLong(A);
			int Aint = (int) Along;
			setIndexL1 = Aint % primeNumberL1 % numberOfCacheSetsL1;
			if (A.length() != numberTagBitsL1 + numberIndexBitsL1)
				{
				for (int i = (numberTagBitsL1 + numberIndexBitsL1) - A.length(); i>0; i--)
					{
					A = "0" + A;
					}
				}
			tagL1 = binaryToHex(A);
			}
		else
			setIndexL1 = binaryToDecimal(indexL1);

			for (int i = 0; i < L1assoc; i++)		// This for loop will find an empty space in the cache to store the block in
				{
					if (L1[setIndexL1].tag[i].length() == 0)	
					{
					L1[setIndexL1].tag[i] = tagL1;
					L1[setIndexL1].blockOffset[i] = blockOffsetL1;

					if (L1LRUenabled)		// if LRU enabled, the block count resets to 0
						{
						L1[setIndexL1].setCount = 0;
						L1[setIndexL1].CacheBlock[i].blockCount = 0;
						for (int j = 0; j < L1assoc; j++)		// for loop updates every other block cache block's block count by 1
							{
							if (i != j)
								L1[setIndexL1].CacheBlock[j].blockCount++;
							}
						}
					else
						L1[setIndexL1].CacheBlock[i].blockCount = L1[setIndexL1].setCount+1;	// this statement only activated if LFU
					
					if (!isRead)	// if writing, set the dirty bit. This function will not be entered if the problem is WTNA.
						{
						L1[setIndexL1].CacheBlock[i].isDirty = true;
						L1dirty++;
						}
					
					return;
					}
				}
		return;
		}
	
	void storeInCacheL2(String tagL2, String indexL2, String blockOffsetL2)		// This function is very similar to the previous one, and will store
		{																		// a block in L2 if there is a vacancy.
		int setIndexL2 = 0;	
		String pmTagL2 = "";
		if (xorIndexing)		// Get index from these if statements, dependent on the indexing function
			{
			String binTag = hexToBinary(tagL2);
			String tagIndexL2 = binTag + indexL2;
			if (tagIndexL2.length() != numberTagBitsL2 + numberIndexBitsL2)
				{
				for (int i = (numberTagBitsL2 + numberIndexBitsL2)-tagIndexL2.length(); i>0; i--)
					{
					tagIndexL2 = "0" + tagIndexL2;
					}
				}
			String indexX = tagIndexL2.substring(numberTagBitsL2-numberIndexBitsL2,numberTagBitsL2);
			String indexY = tagIndexL2.substring(numberTagBitsL2,numberTagBitsL2+numberIndexBitsL2);
			int xInt = binaryToDecimal(indexX);
			int yInt = binaryToDecimal(indexY);
			setIndexL2 = xInt^yInt;
			}
		else if (pmIndexing)
			{
			String binaryL2Tag = hexToBinary(tagL2);
			String A = binaryL2Tag+indexL2;
			long Along = binaryToLong(A);
			int Aint = (int) Along;
			setIndexL2 = Aint % primeNumberL2 % numberOfCacheSetsL2;
			if (numberOfCommands == 72102)
				{
				int y = 2;
				y = y*y;
				}
			if (A.length() != numberTagBitsL2 + numberIndexBitsL2)
				{
				for (int i = (numberTagBitsL2 + numberIndexBitsL2) - A.length(); i>0; i--)
					{
					A = "0" + A;
					}
				}
			pmTagL2 = binaryToHex(A);
			}
		else
			setIndexL2 = binaryToDecimal(indexL2);


			for (int i = 0; i < L2assoc; i++)
				{
					if (L2[setIndexL2].tag[i].length() == 0)
						{
						if (pmIndexing)
							L2[setIndexL2].tag[i] = pmTagL2;
						else
							L2[setIndexL2].tag[i] = tagL2;
						L2[setIndexL2].blockOffset[i] = blockOffsetL2;

						if (L2LRUenabled)
							{
							L2[setIndexL2].setCount = 0;
							L2[setIndexL2].CacheBlock[i].blockCount = 0;
							for (int j = 0; j < L2assoc; j++)		// for loop updates every other block cache block's block count by 1
								{
								if (i != j)
									L2[setIndexL2].CacheBlock[j].blockCount++;
								}
							}
						else
							L2[setIndexL2].CacheBlock[i].blockCount = L2[setIndexL2].setCount+1;
				
						if (!isRead)
							{
							L2[setIndexL2].CacheBlock[i].isDirty = true;
							L2dirty++;
							}
				
						return;
						}
			}
		replaceInCacheL2(tagL2, indexL2);
		}
	
	boolean checkCacheL1ForTag(String L1tag, String L1index, int L1assoc)	// This function simply checks L1 for the tag at the specified 
		{																	// index. This function will also record whether the check was a hit
		boolean inCache = false;											// or a miss.
		int cacheCount = 0;
		int setIndex = 0;
		
		if (xorIndexing)
			{
			String binTag = hexToBinary(L1tag);
			String tagIndexL1 = binTag + L1index;
			if (tagIndexL1.length() != numberTagBitsL1 + numberIndexBitsL1)
				{
				for (int i = (numberTagBitsL1 + numberIndexBitsL1)-tagIndexL1.length(); i>0; i--)
					{
					tagIndexL1 = "0" + tagIndexL1;
					}
				}
			String indexX = tagIndexL1.substring(numberTagBitsL1-numberIndexBitsL1,numberTagBitsL1);
			String indexY = tagIndexL1.substring(numberTagBitsL1,numberTagBitsL1+numberIndexBitsL1);
			int xInt = binaryToDecimal(indexX);
			int yInt = binaryToDecimal(indexY);
			setIndex = xInt^yInt;
			}
		else if (pmIndexing)
			{
			String binaryL1Tag = hexToBinary(L1tag);
			String A = binaryL1Tag+L1index;
			long Along = binaryToLong(A);
			int Aint = (int) Along;
			setIndex = Aint % primeNumberL1 % numberOfCacheSetsL1;
			if (A.length() != numberTagBitsL1 + numberIndexBitsL1)
				{
				for (int i = (numberTagBitsL1 + numberIndexBitsL1) - A.length(); i>0; i--)
					{
					A = "0" + A;
					}
				}
			L1tag = binaryToHex(A);
			}
		else
			setIndex = binaryToDecimal(L1index);
		
		if (!isRead)
			numberOfWritesL1++;
		else
			numberOfReadsL1++;
		
		
		for (int i = 0; i < L1assoc; i++)
			{
			if (L1[setIndex].tag[i].equals(L1tag))
				{
				inCache = true;
					if (!isRead)
						{
						L1[setIndex].CacheBlock[i].isDirty = true;
						L1dirty++;
						}
				if (L1LRUenabled)
					L1[setIndex].CacheBlock[i].blockCount = 0;
				else
					L1[setIndex].CacheBlock[i].blockCount++;
				if (L1assoc > 1 && L1LRUenabled)
					{
					for (int j = 0; j < L1assoc; j++)
						{
						if (i != j)
							L1[setIndex].CacheBlock[j].blockCount++;
						}
					}
				return inCache;
				}
			if (L1[setIndex].tag[i].length() != 0)
				cacheCount++;
			if (cacheCount == L1assoc)
				L1full = true;
			}
		return inCache;
		}
	
	boolean checkCacheL2ForTag(String L2tag, String L2index, int L2assoc)
	{
	boolean inCache = false;
	int setIndex = 0;
	
	if (xorIndexing)
		{
		String binTag = hexToBinary(L2tag);
		String tagIndexL2 = binTag + L2index;
		if (tagIndexL2.length() != numberTagBitsL2 + numberIndexBitsL2)
			{
			for (int i = (numberTagBitsL2 + numberIndexBitsL2)-tagIndexL2.length(); i>0; i--)
				{
				tagIndexL2 = "0" + tagIndexL2;
				}
			}
		String indexX = tagIndexL2.substring(numberTagBitsL2-numberIndexBitsL2,numberTagBitsL2);
		String indexY = tagIndexL2.substring(numberTagBitsL2,numberTagBitsL2+numberIndexBitsL2);
		int xInt = binaryToDecimal(indexX);
		int yInt = binaryToDecimal(indexY);
		setIndex = xInt^yInt;
		}
	else if (pmIndexing)
		{
		String binaryL1Tag = hexToBinary(L2tag);
		String A = binaryL1Tag+L2index;
		long Along = binaryToLong(A);
		int Aint = (int) Along;
		setIndex = Aint % primeNumberL2 % numberOfCacheSetsL2;
		if (A.length() != numberTagBitsL2 + numberIndexBitsL2)
			{
			for (int i = (numberTagBitsL2 + numberIndexBitsL2) - A.length(); i>0; i--)
				{
				A = "0" + A;
				}
			}
		L2tag = binaryToHex(A);
		}
	else
		setIndex = binaryToDecimal(L2index);
	
	if (!isRead)
		numberOfWritesL2++;
	else
		numberOfReadsL2++;
	
	//L2tag = binaryToHex(L2tag)
	
	for (int i = 0; i < L2assoc; i++)
		{
		if (L2[setIndex].tag[i].equals(L2tag))
			{
			inCache = true;
				if (!isRead)
					{
					L2[setIndex].CacheBlock[i].isDirty = true;
					L2dirty++;
					}
			if (L2LRUenabled)
				L2[setIndex].CacheBlock[i].blockCount = 0;
			else
				L2[setIndex].CacheBlock[i].blockCount++;
			if (L2assoc > 1 && L2LRUenabled)
				{
				for (int j = 0; j < L2assoc; j++)
					{
					if (i != j)
						L2[setIndex].CacheBlock[j].blockCount++;
					}
				}
			return inCache;
			}
		}
	return inCache;
	}
	
	BigDecimal getMissRateL1VC(int readMiss, int writeMiss, int reads, int writes, int VCswaps)
		{
//		double L1missRateDouble = 0;
		Integer readMissI = new Integer(readMiss);
		Integer writeMissI = new Integer(writeMiss);
		Integer readI = new Integer(reads);
		Integer writesI = new Integer(writes);
		Integer VCswapsI = new Integer(VCswaps);
		double VCswapsD = VCswapsI.doubleValue();
		double readMissD = readMissI.doubleValue();
		double writeMissD = writeMissI.doubleValue();
		double readD = readI.doubleValue();
		double writesD = writesI.doubleValue();
		double numerator = readMissD + writeMissD - VCswapsD;
		double denominator = readD + writesD;
		L1VCmissRate = numerator/denominator;						// need to put in a case to get L2's miss rate too
		BigDecimal missRateBD = new BigDecimal(L1VCmissRate,MathContext.DECIMAL32);
		missRateBD = missRateBD.setScale(4, BigDecimal.ROUND_HALF_DOWN);
		return missRateBD;
		}
	
	BigDecimal getMissRateL2(int readMiss, int reads, int writeMiss, int writes)
		{
		Integer readMissI = new Integer(readMiss);
		Integer readsI = new Integer(reads);
		Integer writeMissI = new Integer(writeMiss);
		Integer writesI = new Integer(writes);
		double readMissD = readMissI.doubleValue();
		double readsD = readsI.doubleValue();
		double writeMissD = writeMissI.doubleValue();
		double writesD = writesI.doubleValue();
		L2missRate = (writeMissD+readMissD)/(readsD+writesD);
		BigDecimal missRateBD = new BigDecimal(L2missRate,MathContext.DECIMAL32);
		missRateBD = missRateBD.setScale(4, BigDecimal.ROUND_HALF_DOWN);
		return missRateBD;
		}
	
	BigDecimal getSRR(int requests, int reads, int writes)
		{
		Integer requestsI = new Integer(requests);
		Integer readsI = new Integer(reads);
		Integer writesI = new Integer(writes);
		double requestsD = requestsI.doubleValue();
		double readsD = readsI.doubleValue();
		double writesD = writesI.doubleValue();
		SRR = requestsD/(readsD+writesD);
		BigDecimal SRRBD = new BigDecimal(SRR,MathContext.DECIMAL32);
		SRRBD = SRRBD.setScale(4, BigDecimal.ROUND_HALF_DOWN);
		return SRRBD;
		}
	
	void replaceInCacheL1(String tagL1, String indexL1, String blockOffsetL1)
		{
		int setIndexL1 = 0;
		
		if (xorIndexing)
			{
			String binTag = hexToBinary(tagL1);
			String tagIndexL1 = binTag + indexL1;
			if (tagIndexL1.length() != numberTagBitsL1 + numberIndexBitsL1)
				{
				for (int i = (numberTagBitsL1 + numberIndexBitsL1)-tagIndexL1.length(); i>0; i--)
					{
					tagIndexL1 = "0" + tagIndexL1;
					}
				}
			String indexX = tagIndexL1.substring(numberTagBitsL1-numberIndexBitsL1,numberTagBitsL1);
			String indexY = tagIndexL1.substring(numberTagBitsL1,numberTagBitsL1+numberIndexBitsL1);
			int xInt = binaryToDecimal(indexX);
			int yInt = binaryToDecimal(indexY);
			setIndexL1 = xInt^yInt;
			}
		else if (pmIndexing)
			{
			String binaryL1Tag = hexToBinary(tagL1);
			String A = binaryL1Tag+indexL1;
			long Along = binaryToLong(A);
			int Aint = (int) Along;
			setIndexL1 = Aint % primeNumberL1 % numberOfCacheSetsL1;
			if (A.length() != numberTagBitsL1 + numberIndexBitsL1)
				{
				for (int i = (numberTagBitsL1 + numberIndexBitsL1) - A.length(); i>0; i--)
					{
					A = "0" + A;
					}
				}
			tagL1 = binaryToHex(A);
			}
		else
			setIndexL1 = binaryToDecimal(indexL1);
		
		int replaceIndexL1 = 0;
		int blockCountL1 = L1[setIndexL1].CacheBlock[0].blockCount;

			for (int i = 1; i < L1assoc; i++)			// for loop finds the highest block count
			{
				if ((L1[setIndexL1].CacheBlock[i].blockCount > blockCountL1) && L1LRUenabled)
					{
					blockCountL1 = L1[setIndexL1].CacheBlock[i].blockCount;
					replaceIndexL1 = i;					// replace index updated every time a larger block count is found
					}
				if ((L1[setIndexL1].CacheBlock[i].blockCount < blockCountL1) && !L1LRUenabled)
					{
					blockCountL1 = L1[setIndexL1].CacheBlock[i].blockCount;	// replace index updated every time a smaller block count is found
					replaceIndexL1 = i;
					}
			}
			if (L1LRUenabled)
				{
			for (int i = 0; i < L1assoc; i++)		// for loop updates every other block cache block's block count by 1
					{
					if (i != replaceIndexL1)
						{
						L1[setIndexL1].CacheBlock[i].blockCount++;	// update all other block counts
						}
					}
				}
			// replace cache block with corresponding replace index
			if (L1LRUenabled)
				{
				L1[setIndexL1].CacheBlock[replaceIndexL1].blockCount = 0;
				}
			else
				{
				L1[setIndexL1].setCount = L1[setIndexL1].CacheBlock[replaceIndexL1].blockCount;
				L1[setIndexL1].CacheBlock[replaceIndexL1].blockCount = L1[setIndexL1].setCount+1;
				}
			
			if (isRead == true && L1WBWAenabled == true && L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty == true)
				{
				L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty = false;
				if (L2WBWAenabled)
					{
					isRead = false;
					
					String storeInL2tag = L1[setIndexL1].tag[replaceIndexL1];
					storeInL2tag = hexToBinary(storeInL2tag);
					
					if (pmIndexing)
						{
						if (storeInL2tag.length() != numberTagBitsL1 + numberIndexBitsL1)
							{
							for (int i = (numberTagBitsL1 + numberIndexBitsL1) - storeInL2tag.length(); i>0; i--)
								{
								storeInL2tag = "0"+ storeInL2tag;
								}
							}
						}
					else	
						{
						if (storeInL2tag.length() != numberTagBitsL1)
							{
							for (int i = numberTagBitsL1 - storeInL2tag.length(); i>0; i--)
								{
								storeInL2tag = "0"+ storeInL2tag;
								}
							}
						}
						
					String storeInL2index = "";
					
					if (xorIndexing)						// reconstruct address
						{
						if (numberOfCommands == 22768)
							{
							int a = 1;
							a = a + 1;
							}
						String tagXOR = storeInL2tag.substring(numberTagBitsL1-numberIndexBitsL1);
						int tagXORint = binaryToDecimal(tagXOR);
						int storeInL2indexI = tagXORint^setIndexL1;
						storeInL2index = decimalToBinary(storeInL2indexI);
						if (storeInL2index.length() != numberIndexBitsL1)
							{
							for (int i = numberIndexBitsL1 - storeInL2index.length();i>0;i--)
								{
								storeInL2index = "0" + storeInL2index;
								}
							}
						}
					else if (pmIndexing)		// got the L1 tag with proper bit length here, parse out the send tag as well as real index
						{
						storeInL2index = storeInL2tag.substring(numberTagBitsL1,numberTagBitsL1+numberIndexBitsL1);
						storeInL2tag = storeInL2tag.substring(0,numberTagBitsL1);   // SetIndexL1 is the remainder from the modulo function, can find
						}															// true address by reversing modulo with the # cache sets and
					else															// the prime # for L1.
						storeInL2index = indexL1;  
					
					if (storeInL2index.length() != numberIndexBitsL1)
						{
						for (int i = numberIndexBitsL1 - storeInL2index.length(); i>0; i--)
							{
							storeInL2index = "0"+ storeInL2index;
							}
						}
					
					String storeInL2blockOffset = L1[setIndexL1].blockOffset[replaceIndexL1];
					
					if (storeInL2blockOffset.length() != numberBlockOffsetBitsL1)
						{
						for (int i = numberBlockOffsetBitsL1 - storeInL2blockOffset.length(); i>0; i--)
							{
							storeInL2blockOffset = "0"+ storeInL2blockOffset;
							}
						}
					String storeInL2Command = storeInL2tag + storeInL2index + storeInL2blockOffset;
					
					
					storeInL2tag = storeInL2Command.substring(0,numberTagBitsL2);
					storeInL2tag = binaryToHex(storeInL2tag);
					storeInL2index = storeInL2Command.substring(numberTagBitsL2,numberTagBitsL2+numberIndexBitsL2);  // DO NOT implement index 
					storeInL2blockOffset = storeInL2Command.substring(numberTagBitsL2+numberIndexBitsL2);			 // function here
					if (!checkCacheL2ForTag(storeInL2tag,storeInL2index, L2assoc))
						{
						storeInCacheL2(storeInL2tag,storeInL2index,storeInL2blockOffset);
						}
					numberOfWriteBacksL1L2++;
					}
				}
			else if (isRead == false && L1WBWAenabled == true)
				{
				if (L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty == true)
					{
					if (L2WBWAenabled)
						{
						String storeInL2tag = L1[setIndexL1].tag[replaceIndexL1];
						storeInL2tag = hexToBinary(storeInL2tag);
						
						if (pmIndexing)
						{
							if (storeInL2tag.length() != numberTagBitsL1 + numberIndexBitsL1)
								{
								for (int i = (numberTagBitsL1 + numberIndexBitsL1) - storeInL2tag.length(); i>0; i--)
									{
									storeInL2tag = "0"+ storeInL2tag;
									}
								}
							}
						else	
							{
							if (storeInL2tag.length() != numberTagBitsL1)
								{
								for (int i = numberTagBitsL1 - storeInL2tag.length(); i>0; i--)
									{
									storeInL2tag = "0"+ storeInL2tag;
									}
								}
							}
						
						String storeInL2index = "";
						
						if (xorIndexing)						// reconstruct address
							{
							if (numberOfCommands == 22768)
								{
								int a = 1;
								a = a + 1;
								}
							String tagXOR = storeInL2tag.substring(numberTagBitsL1-numberIndexBitsL1);
							int tagXORint = binaryToDecimal(tagXOR);
							int storeInL2indexI = tagXORint^setIndexL1;
							storeInL2index = decimalToBinary(storeInL2indexI);
							if (storeInL2index.length() != numberIndexBitsL1)
								{
								for (int i = numberIndexBitsL1 - storeInL2index.length();i>0;i--)
									{
									storeInL2index = "0" + storeInL2index;
									}
								}
							}
						else if (pmIndexing)
							{
							storeInL2index = storeInL2tag.substring(numberTagBitsL1,numberTagBitsL1+numberIndexBitsL1);
							storeInL2tag = storeInL2tag.substring(0,numberTagBitsL1);
							}
						else
							storeInL2index = indexL1;  
						
						if (storeInL2index.length() != numberIndexBitsL1)
							{
							for (int i = numberIndexBitsL1 - storeInL2index.length(); i>0; i--)
								{
								storeInL2index = "0"+ storeInL2index;
								}
							}
						
						String storeInL2blockOffset = L1[setIndexL1].blockOffset[replaceIndexL1];
						
						if (storeInL2blockOffset.length() != numberBlockOffsetBitsL1)
							{
							for (int i = numberBlockOffsetBitsL1 - storeInL2blockOffset.length(); i>0; i--)
								{
								storeInL2blockOffset = "0"+ storeInL2blockOffset;
								}
							}
						
						String storeInL2Command = storeInL2tag + storeInL2index + storeInL2blockOffset;
						storeInL2tag = storeInL2Command.substring(0,numberTagBitsL2);
						storeInL2tag = binaryToHex(storeInL2tag);
						storeInL2index = storeInL2Command.substring(numberTagBitsL2,numberTagBitsL2+numberIndexBitsL2); // DONT IMPLEMENT INDEX
						storeInL2blockOffset = storeInL2Command.substring(numberTagBitsL2+numberIndexBitsL2);			// FUNCTION
						if (!checkCacheL2ForTag(storeInL2tag,storeInL2index, L2assoc))
							{
							storeInCacheL2(storeInL2tag,storeInL2index,storeInL2blockOffset);
							}
						numberOfWriteBacksL1L2++;
						}
					}
				L1[setIndexL1].CacheBlock[replaceIndexL1].isDirty = true;
				L1dirty++;
				}
			
			L1[setIndexL1].tag[replaceIndexL1] = tagL1;	
	}
	
	void replaceInCacheL2(String tagL2, String indexL2)
	{
		int setIndexL2 = 0;
		if (xorIndexing)
			{
			String binTag = hexToBinary(tagL2);
			String tagIndexL2 = binTag + indexL2;
			if (tagIndexL2.length() != numberTagBitsL2 + numberIndexBitsL2)
				{
				for (int i = (numberTagBitsL2 + numberIndexBitsL2)-tagIndexL2.length(); i>0; i--)
					{
					tagIndexL2 = "0" + tagIndexL2;
					}
				}
			String indexX = tagIndexL2.substring(numberTagBitsL2-numberIndexBitsL2,numberTagBitsL2);
			String indexY = tagIndexL2.substring(numberTagBitsL2,numberTagBitsL2+numberIndexBitsL2);
			int xInt = binaryToDecimal(indexX);
			int yInt = binaryToDecimal(indexY);
			setIndexL2 = xInt^yInt;
			}
		else if (pmIndexing)
			{
			String binaryL1Tag = hexToBinary(tagL2);
			String A = binaryL1Tag+indexL2;
			if (numberOfCommands == 72102)
				{
				int y = 1;
				y = y + 1;
				}
			long Along = binaryToLong(A);
			int Aint = (int) Along;
			setIndexL2 = Aint % primeNumberL2 % numberOfCacheSetsL2;
			if (A.length() != numberTagBitsL2 + numberIndexBitsL2)
				{
				for (int i = (numberTagBitsL2 + numberIndexBitsL2) - A.length(); i>0; i--)
					{
					A = "0" + A;
					}
				}
			tagL2 = binaryToHex(A);
			}
		else
			setIndexL2 = binaryToDecimal(indexL2);
		
	int replaceIndexL2 = 0;
	int blockCountL2 = L2[setIndexL2].CacheBlock[0].blockCount;

		for (int i = 1; i < L2assoc; i++)			// for loop finds the highest block count
		{
			if ((L2[setIndexL2].CacheBlock[i].blockCount > blockCountL2) && L2LRUenabled)
				{
				blockCountL2 = L2[setIndexL2].CacheBlock[i].blockCount;
				replaceIndexL2 = i;					// replace index updated every time a larger block count is found
				}
			if ((L2[setIndexL2].CacheBlock[i].blockCount < blockCountL2) && !L2LRUenabled)
				{
				blockCountL2 = L2[setIndexL2].CacheBlock[i].blockCount;	// replace index updated every time a smaller block count is found
				replaceIndexL2 = i;
				}
		}
		if (L2LRUenabled)
			{
		for (int i = 0; i < L2assoc; i++)		// for loop updates every other block cache block's block count by 1
				{
				if (i != replaceIndexL2)
					{
					L2[setIndexL2].CacheBlock[i].blockCount++;	// update all other block counts
					}
				}
			}
		// replace cache block with corresponding replace index
		if (L2LRUenabled)
			{
			L2[setIndexL2].CacheBlock[replaceIndexL2].blockCount = 0;
			}
		else
			{
			L2[setIndexL2].setCount = L2[setIndexL2].CacheBlock[replaceIndexL2].blockCount;
			L2[setIndexL2].CacheBlock[replaceIndexL2].blockCount = L2[setIndexL2].setCount+1;
			}
		L2[setIndexL2].tag[replaceIndexL2] = tagL2;
		if (isRead == true && L2WBWAenabled == true && L2[setIndexL2].CacheBlock[replaceIndexL2].isDirty == true)
			{
			L2[setIndexL2].CacheBlock[replaceIndexL2].isDirty = false;
			numberOfWriteBacksL2Memory++;
			}
		if (isRead == false && L2WBWAenabled == true)
			{
			if (L2[setIndexL2].CacheBlock[replaceIndexL2].isDirty == true)
				{
				numberOfWriteBacksL2Memory++;
				}
			L2[setIndexL2].CacheBlock[replaceIndexL2].isDirty = true;
			L2dirty++;
			}	
}
	
	double getmissPenalty(int blocksize)
		{
		Integer blocksizeI = new Integer(blocksize);
		double blocksizeD = blocksizeI.doubleValue();
		double missPenalty = 20 + 0.5*(blocksizeD/16);
		return missPenalty;
		}
	
	double getHitTimeL1(int L1size, int L1blocksize, int L1assoc)
		{
		Integer L1sizeI = new Integer(L1size);
		Integer L1blocksizeI = new Integer(L1blocksize);
		Integer L1assocI = new Integer(L1assoc);
		double L1sizeD = L1sizeI.doubleValue();
		double L1blocksizeD = L1blocksizeI.doubleValue();
		double L1assocD = L1assocI.doubleValue();
		double L1sizeVal = 2.5*(L1sizeD/(512 * 1024));
		double blockVal = 0.025*(L1blocksizeD/16);
		double L1assocVal = 0.025*L1assocD;
		double hitTime = 0.25 + L1sizeVal + blockVal + L1assocVal;
		return hitTime;
		}
	
	double getHitTimeL2(int L2size, int L2blocksize, int L2assoc)
		{
		Integer L2sizeI = new Integer(L2size);
		Integer L2blocksizeI = new Integer(L2blocksize);
		Integer L2assocI = new Integer(L2assoc);
		double L2sizeD = L2sizeI.doubleValue();
		double L2blocksizeD = L2blocksizeI.doubleValue();
		double L2assocD = L2assocI.doubleValue();
		double L2sizeVal = 2.5*(L2sizeD/(512*1024));
		double blockVal = 0.025*(L2blocksizeD/16);
		double L2assocVal = 0.025*L2assocD;
		double hitTime = 2.5 + L2sizeVal + blockVal + L2assocVal;
		return hitTime;
		}
	
	double getHitTimeVC(int L1blockSize, int numVCblocks)
		{
		Integer VCblockSizeI = new Integer(L1blockSize);
		Integer numVCblocksI = new Integer(numVCblocks);
		int VCsize = numVCblocks*L1blockSize;
		Integer VCsizeI = new Integer(VCsize);
		double VCblockSizeD = VCblockSizeI.doubleValue();
		double numVCblocksD = numVCblocksI.doubleValue();
		double VCsizeD = VCsizeI.doubleValue();
		double VCsizeVal = 2.5*(VCsizeD/(512*1024));
		double blockVal = 0.025*(VCblockSizeD/16);
		double numVCblocksVal = 0.025*numVCblocksD;
		double hitTime = 0.25 + VCsizeVal + blockVal + numVCblocksVal;
		return hitTime;
		}
	
	String getTag(String binary,int tagBits)
		{
		String tag = binary.substring(0,tagBits);
		tag = binaryToHex(tag);
		return tag;
		}
	
	String getIndex(String binary, int tagBits, int indexBits)
		{
		String index = binary.substring(tagBits,tagBits+indexBits);
		if (index.length() == 0)
			index = "0";
		return index;
		}
	
	String getBlockOffset(String binary,int tagBits, int indexBits)
		{
		String blockOffset = binary.substring(tagBits+indexBits);
		return blockOffset;
		}
	
	String getBinaryCommand(String tag, String index, String blockOffset)
		{
		BigInteger bi = new BigInteger(tag,16);
		String tagBinary = bi.toString(2);
		String command = tagBinary+index+blockOffset;
		if (command.length() != 32)
		{
		for (int i = 32 - command.length(); i > 0; i--)
			{
			command = "0" + command;
			}
		}
		return command;
		}
	
	void printStuff()
	{
		//System.out.println("");
		//System.out.println("");
		//System.out.println("");
		//System.out.println("Number of commands: " + numberOfCommands);
		System.out.println("  ===== Simulator configuration =====");
		System.out.println("  L1_BLOCKSIZE:			   " + L1blocksize);
		System.out.println("  L1_SIZE:		         " + L1size);
		System.out.println("  L1_ASSOC:			    " + L1assoc);
		System.out.println("  L1_REPLACEMENT_POLICY:            " + L1replacement);
		System.out.println("  L1_WRITE_POLICY:		    " + L1writePolicy);
		System.out.println("  VC_NUMBLOCKS:			" + vcNumblocks);	
		System.out.println("  L2_BLOCKSIZE:			   " + L2blocksize);
		System.out.println("  L2_SIZE:		         " + L2size);
		System.out.println("  L2_ASSOC:			    " + L2assoc);
		System.out.println("  L2_REPLACEMENT_POLICY:            " + L2replacement);
		System.out.println("  L2_WRITE_POLICY:		    " + L2writePolicy);
		System.out.println("  INDEXING_FUNCTION:		" + indexFunction);
		System.out.println("  trace_file:	        " + filename);
		System.out.println("  ===================================");
		System.out.println("");
		System.out.println("===== L1 contents =====");
		String line = "";
		for (int i = 0; i < numberOfCacheSetsL1; i++)
			{
			line = "set	" + i + ":	";
				for (int j = 0; j < L1assoc; j++)
					{
					if (L1[i].tag[j].length() > 0)
						line = line + L1[i].tag[j];
					else
						line = line + " - ";
					if (L1[i].CacheBlock[j].isDirty && L1WBWAenabled)
						{
						line = line + " D   ";
						}
					else 
						line = line + "     ";
					}
			System.out.println(line);
			}
		if (VCenabled)
			{
		System.out.println("===== VC contents =====");
			line = "set 0 : ";
			for (int i = 0; i < vcNumblocks; i++)
				{
				if (VC[i].tag[0].length() > 0)
					line = line + VC[i].tag[0];
				else
					line = line + " - ";
				if (VC[i].CacheBlock[0].isDirty)
					{
					line = line + " D   ";
					}
				else
					line = line + "     ";
				}
			System.out.println(line);
			}
		//System.out.println("L1 dirty: " + L1dirty);
		//System.out.println("L2 dirty: " + L2dirty);
		//System.out.println("VC dirty: " + VCdirty);
		System.out.println("===== L2 contents =====");
		for (int i = 0; i < numberOfCacheSetsL2; i++)
		{
		line = "set	" + i + ":	";
			for (int j = 0; j < L2assoc; j++)
				{
				if (L2[i].tag[j].length() > 0)
					line = line + L2[i].tag[j];
				else
					line = line + " - ";
				if (L2[i].CacheBlock[j].isDirty && L2WBWAenabled)
					{
					line = line + " D   ";
					}
				else 
					line = line + "     ";
				}
		System.out.println(line);
		}
		System.out.println("");
		System.out.println("  ====== Simulation results (raw) ======");
		System.out.println("  a. number of L1 reads:          " + numberOfReadsL1);
		System.out.println("  b. number of L1 read misses:	   " + numberOfReadMissesL1);
		//System.out.println(" number of L1 misses: " + (numberOfReadMissesL1 + numberOfWriteMissesL1));
		System.out.println("  c. number of L1 writes:         " + numberOfWritesL1);
		System.out.println("  d. number of L1 write misses:	   " + numberOfWriteMissesL1);
		BigDecimal L1VCmissRateBD = getMissRateL1VC(numberOfReadMissesL1, numberOfWriteMissesL1, numberOfReadsL1, numberOfWritesL1, VCswaps);
		double L1VCmissRateRounded = L1VCmissRateBD.doubleValue();
		System.out.println("  e. number of swap requests: 		 " + VCSwapRequests);
		BigDecimal SRRBD = getSRR(VCSwapRequests,numberOfReadsL1,numberOfWritesL1);
		System.out.println("  f. swap request rate (SRR): " + SRRBD);
		System.out.println("  g. number of swaps:          " + VCswaps);
		System.out.println("  h. combined L1+VC miss rate: 		" + L1VCmissRateRounded);
		if (VCenabled)
			System.out.println("  i. number writebacks from L1/VC: " + numberOfWriteBacksL1VC);
		else
			System.out.println("  i. number writebacks from L1/VC: " + numberOfWriteBacksL1L2);
		System.out.println("  j. number of L2 reads: " + numberOfReadsL2);
		//System.out.println(" number of L2 misses: " + (numberOfReadMissesL2 + numberOfWriteMissesL2));
		System.out.println("  k. number of L2 read misses: " + numberOfReadMissesL2);
		System.out.println("  l. number of L2 writes: " + numberOfWritesL2);
		System.out.println("  m. number of L2 write misses: " + numberOfWriteMissesL2);
		BigDecimal L2missRateBD = getMissRateL2(numberOfReadMissesL2, numberOfReadsL2, numberOfWriteMissesL2, numberOfWritesL2);
		double L2missRateRounded = L2missRateBD.doubleValue();
		System.out.println("  n. L2 miss rate:		 " + L2missRateRounded);
		System.out.println("  o. number of writebacks from L2:    " + numberOfWriteBacksL2Memory);
		if (L2WBWAenabled)
			{
			memoryTraffic = numberOfReadMissesL2 + numberOfWriteMissesL2 + numberOfWriteBacksL2Memory;
			}
		else
			{
			memoryTraffic = numberOfWritesL2 + numberOfReadMissesL2;
			}
		System.out.println("  p. total memory traffic:         " + memoryTraffic);
		System.out.println("");
		System.out.println("  ==== Simulation results (performance) ====");
		//L1missPenalty = getmissPenalty(L1blocksize);
		L2missPenalty = getmissPenalty(L2blocksize);
		L1hitTime = getHitTimeL1(L1size,L1blocksize,L1assoc);
		L2hitTime = getHitTimeL2(L2size,L2blocksize,L2assoc);
		VChitTime = getHitTimeVC(L1blocksize, vcNumblocks);
		double aatD = L1hitTime + (SRR*VChitTime) + (L1VCmissRate*(L2hitTime + (L2missRate * L2missPenalty)));
		BigDecimal aatBD = new BigDecimal(aatD,MathContext.DECIMAL32);
		aatBD = aatBD.setScale(4,BigDecimal.ROUND_HALF_DOWN);
		double aat = aatBD.doubleValue();
		Double aatDD = new Double(aat);
		String AAT = aatDD.toString();
		while (AAT.length() < 6)
			AAT = AAT + "0";
		System.out.println("  1. average access time:         " + AAT + " ns");
		Integer sizeOfL1 = new Integer(L1size);
		Integer sizeOfL2 = new Integer(L2size);
		Integer numberOfVCBlocks = new Integer(vcNumblocks);
		Integer blockSizeOfL1 = new Integer(L1blocksize);
		double sizeOfL1D = sizeOfL1.doubleValue();
		double sizeOfL2D = sizeOfL2.doubleValue();
		double numberOfVCBlocksD = numberOfVCBlocks.doubleValue();
		double blockSizeOfL1D = blockSizeOfL1.doubleValue();
		double cacheArea = sizeOfL1D+sizeOfL2D+(numberOfVCBlocksD*blockSizeOfL1D);
		System.out.println(" Total cache area (in KB): " + cacheArea/1024);
		//System.out.println(" L1 dirty bits: " + L1dirty);
		//System.out.println(" L2 dirty bits: " + L2dirty);
		//System.out.println(" VC dirty bits: " + VCdirty);
	}
	
}
	



