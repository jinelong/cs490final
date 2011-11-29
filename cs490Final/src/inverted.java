import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//github: git@github.com:jinelong/cs490final.git

/*
 * cs490 final project
 * 
 */

public class inverted {
	final static boolean DEBUG = true; 
	final static boolean SHOW_RESULT = true;
	
	// the threshold for determine whether a message is a spam or not
	final static double threshhold = 0.5;
	/*
	 * training : ling-spam
	 * training2:  PU1TESTING_PATH
	 */
	final static String TRAINING_PATH = "/tmp/spamHam/training/";
	final static String TRAINING_PATH2 = "/tmp/spamHam/training2/";
	
	final static String TESTING_PATH = "/tmp/spamHam/testingLing/";
	final static String TESTING_PATH2 = "/tmp/spamHam/testingPU1/";
	
	final static String ROOT_PATH = "/tmp/spamHam/";
	
	static int totalNumOfFiles = 0;
	static int totalNumOfSpam = 0;
	static int totalNumOfHam = 0;
	static int totalWords = 0;
	
	
	FileOutputStream ostream ;
    DataOutputStream out;
    BufferedWriter bw ;
		
    FileOutputStream ostream2 ;
    DataOutputStream out2;
    BufferedWriter bw2 ;
		
    
	
	static double Ph = 0;	//the overall probability that any given message is not spam 
	static double Ps = 0; //the overall probability that any given message is spam
	
	static boolean ifHam = false;
	
	static class invertedSlot{
		
		public String word;
		public int spamCount;
		public int hamCount;
		public double spamProb;
		public double hamProb;
		public boolean flag;
		public invertedSlot(String w){
			
			word = w;
			spamCount =0;
			hamCount =0;
			spamProb = -1;
			hamProb = -1;
			
		}
		
    	
	}
	
	//list of word that with statistics
	static ArrayList<invertedSlot> wordList = new ArrayList<invertedSlot>();
	
	static ArrayList<String> rawList = new ArrayList<String>(); //raw word list 
	
	/**
	 * @throws IOException
	 * print out debug information
	 */
	public void showList() throws IOException{
		
		 ostream = new FileOutputStream("/run/shm/wordStat");
         out = new DataOutputStream(ostream);
         bw = new BufferedWriter(new OutputStreamWriter(out));
         
         
		
		for(invertedSlot s : wordList){
			bw.write(s.word + "\thamProb: " + s.hamProb + "\tspamProb: " + s.spamProb + "\n"); 
			
		}
		System.out.println("totalFile: " + totalNumOfFiles + "\ttotalNumOfHam: " + totalNumOfHam + "\ttotalNumofSpam: " + totalNumOfSpam + "\ttotalWordCount " + rawList.size());
		System.out.println("Ph: " + Ph +"\tPs"+ Ps);
		
		bw.write("totalFile: " + totalNumOfFiles + "\ttotalNumOfHam: " + totalNumOfHam + "\ttotalNumofSpam: " + totalNumOfSpam + "\ttotalWordCount " + rawList.size() + "\n");
		bw.write("Ph: " + Ph +"\tPs"+ Ps + "\n");
		
		bw.close();
		out.close();
		ostream.close();
	}
	
	
	/**
	 * @param word
	 * @return  true if the word is a symbol
	 */
	public boolean checkWord(String word){
		
		Pattern p = Pattern.compile("\\W");
		Matcher m = p.matcher(word);
		 
		return m.matches();
		
	}

/**
 * @param files : traning direcotry
 * @param trainingpath	: training directory in string
 * @throws IOException
 * 
 * this method will read traning files and record the stats for each word
 */
public void training(File[] files, String trainingpath) throws IOException {
	System.out.println("total file in " + trainingpath + " to train: " + files.length);
    for (File file : files) {
        if (!file.isDirectory() && file.getName().contains(".txt")) {
           
        	totalNumOfFiles ++;
            if(file.getName().contains("spm"))
            	ifHam = false;
            else
            	ifHam= true;
            
            if(ifHam)
            	totalNumOfHam++;
            else
            	totalNumOfSpam++;
            
            String fileName = file.getName().trim();
            
            
           // System.out.println("proccessing " + fileName);
            FileInputStream fstream = new FileInputStream(trainingpath+fileName);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine = null;
            Scanner s = null;
     
            while ((strLine = br.readLine()) != null)   {
            	  // Print the content on the console
            		s = new Scanner(strLine);
            		while(s.hasNext()){
            			String temp = s.next();
            			temp = temp.toLowerCase();
            			//increase the counter
            			if(checkWord(temp) || temp.equals("subject:"))
            					continue;
            			
            			//int index = rawList.indexOf(temp);
            			if(rawList.contains(temp)){
            				
            				for(int i=0;i<wordList.size();i++){
            					
            					if(wordList.get(i).word.equals(temp)){
            						if(ifHam){
            							wordList.get(i).hamCount++;
            							
            						}
            						else{
            							wordList.get(i).spamCount++;
            							
            						}
            						break;            						
            					}
            					
            				}//for
            				
            			}
            			//add a slot
            			else{
            				invertedSlot temp2 = new invertedSlot(temp);
            				if(ifHam){
            					temp2.hamCount++;
            					
            				}
            				else{
            					temp2.spamCount++;
            				
            				}
            				wordList.add(temp2);
            				rawList.add(temp);
            				totalWords++;
            				
            			}
            			
            		}//while
            	
            	//  System.out.println (strLine);
            		
            }//while
            	  //Close the input stream
            		s.close();
            	  in.close();
        	}
    }
   
    
    //calculate pws and pwh
    
    
	Ph = (double)totalNumOfHam/ totalNumOfFiles;	//the overall probability that any given message is not spam 
	Ps = (double)totalNumOfSpam/ totalNumOfFiles; //the overall probability that any given message is spam
	
	//for each distinct word in the training corpora, calculate its probability 
	//of appearing in spam messages and legitimate messages
	for(int i =0;i < wordList.size(); i++){
    	wordList.get(i).hamProb = (double)wordList.get(i).hamCount/(wordList.get(i).hamCount+wordList.get(i).spamCount);
		wordList.get(i).spamProb = (double)wordList.get(i).spamCount/(wordList.get(i).hamCount+wordList.get(i).spamCount);
		
    }
	
	 if(DEBUG)
	    	showList();
	 
	 
	 
	}//training
	
	//Psw = (Pws * Ps)/ (Pws*Ps+ Pwh*Ph)


	/**
	 * @param word
	 * @return: the word stat, if the word was not included in the training set, return 0 for both param
	 */
	private double[] getProbs(String word){
		double [] retval = new double[2];
		retval[0] = 0;
		retval[1] = 0;
		
		if(rawList.contains(word)){
			for(invertedSlot s: wordList){
				if(s.word.equals(word)){
					retval[0] = s.hamProb;
					retval[1] = s.spamProb;
				}
			}
			
		}
		
		return retval;
	}
	
	//return true if retval is greater than 0.5
	public double estimate(String fileName) throws IOException{
		
		
		double Pwh = 0; //the probability that the word "replica" appears in ham messages.calcualte this for each word
		double Pws = 0; // probability that the word "replica" appears in spam messages;
		double returnPair[] = new double[2]; // pwh , pws
		ArrayList <Double> aggregate = new ArrayList<Double>();
		ArrayList <Double> aggregate2 = new ArrayList<Double>();
		double spamScore = 0;
		int counter =0;
		double term1 , term2;
		
		double zero = 0;
		
		
		 FileInputStream fstream = new FileInputStream(TESTING_PATH+fileName);
         DataInputStream in = new DataInputStream(fstream);
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String strLine = null;
         Scanner s = null;

         
         while ((strLine = br.readLine()) != null)   {
        	 s = new Scanner(strLine);
     		while(s.hasNext()){ 
     			double Psw  = 0;//what we need
     			String temp = s.next();
     			temp = temp.toLowerCase();
     			//increase the counter
     			
     			if(checkWord(temp) || !rawList.contains(temp))
					continue;
     			
     			returnPair = getProbs(temp);
     			counter ++;
     			
     			Pws = returnPair[0];
     			Pwh = returnPair[1];
     			
     			if(Pwh==0 || Pws ==0)
     				continue;
     		
     			Psw = (double)(Pws * Ps)/ (Pws*Ps+ Pwh*Ph);
     			if(Psw!=zero){
     				aggregate.add(Math.log(Psw));
     				aggregate2.add(Math.log(1-Psw));
     			}else
     				System.out.println("got ya");
     		}//while
        	
         }
         
         s.close();
         br.close();
         in.close();
         fstream.close();
         
         if(counter==0)
        	 return 0;
       
         if(aggregate.size() != aggregate2.size()){
        	 System.err.println("size not match");
        	 System.exit(0);
         }
         	
         term1 = 0;
 		 term2 = 0;
         for(int i=0;i<aggregate.size();i++){
        	 term1 += aggregate.get(i);
        	 term2 += aggregate2.get(i);
         }
         
         spamScore = term1/(term1+term2);
         
         //Psw /= counter;
        //System.out.println("spamScore is " + spamScore + " counter is " + counter + " term1: " + term1 + " term2: "+ term2);
       
         return spamScore;
	}
    
	/**
	 * @param files
	 * @param flag
	 * @throws IOException
	 * 
	 * this method is for debugging purpose
	 */
	public void evaluate(File[] files, String flag) throws IOException{
		
		/*	
		 	 ostream = new FileOutputStream(ROOT_PATH+"outLog");
             out = new DataOutputStream(ostream);
             bw = new BufferedWriter(new OutputStreamWriter(out));
         */    
             ostream2 = new FileOutputStream(ROOT_PATH+"stat");
             out2 = new DataOutputStream(ostream2);
             bw2 = new BufferedWriter(new OutputStreamWriter(out2));
             
            double total = 0;
            int counter = 0;
            
            System.out.println("-----evaluate----");
		   for (File file : files) {
		        if (!file.isDirectory() && file.getName().contains(".txt")) {
		    
		        	/*
		        	if(flag.equals("spm")){
			            if(!file.getName().contains("spm"))
			            	continue;
		        	}
			        else{
			        	if(file.getName().contains("spm"))
			            	continue;
			        }*///commented out, evaluate all messages
			            	
		            
		            String fileName = file.getName().trim();
		            System.out.println("evaluating " + fileName);
		            
		            //output evaluation result here
		           double retval =  estimate(fileName);
		            total += retval;
		         //   bw.write(fileName + " " + retval+"\n");
		            counter ++;
		 
		            
		            String result = "";
		            if(retval > 0.5)
		           	 result = "spm";
		            else
		           	 result = "msg";
		             bw2.write(fileName+"\t"+result+"\n");
		          
		            
		        }//if
		   }//for
		bw.close();
		out.close();
		ostream.close();
		
		  
        bw2.close();
        out2.close();
        ostream2.close();
        
		System.out.println("count: " + counter + " total " + total +" files\t" + "averyge score: " + total/counter);
		printStat();
	}
	
	public void printStat() throws IOException{
		

		int total = 0;
		int correctSpam = 0;
		int errorSpam = 0;
		
		int spamCount = 0;
		int hamCount = 0;
		
		System.out.println("---printStat---");
		
		FileInputStream fstream = new FileInputStream(ROOT_PATH+"stat");
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine = null;
        Scanner s = null;
        
        while ((strLine = br.readLine()) != null)   {
       	 s = new Scanner(strLine);
    		while(s.hasNext()){ 
    			String name = s.next();
    			String result = s.next();
    			
    			if(name.contains("spm"))
    				spamCount ++;
    			else
    				hamCount ++;
    				
    			
    			if(name.contains("spm") && result.equals("spm"))
    				correctSpam ++;
    			else if(name.contains("msg") && result.equals("spm"))
    				errorSpam ++;
    			
    			
    		}
    		total ++;
    	}
        System.out.println("total: " + total + " spamCount: " + spamCount + " hamCount: " + hamCount + " corretSpam: " + correctSpam + " errorSpam: " + errorSpam );
	}
	
	
	
    public static void main(String []args) throws IOException {
    	inverted temp = new inverted();
    	
        //File[] files = new File(TRAINING_PATH).listFiles();
        //temp.training(files,TRAINING_PATH );
        
        File[] files2 = new File(TRAINING_PATH).listFiles();
        temp.training(files2,TRAINING_PATH);
        
        System.out.println("training done");
        
        File[] files3 = new File(TESTING_PATH).listFiles();
       
       	 
        	String input;
        	System.out.print("spm or msg? ");
        	Scanner stdin = new Scanner(System.in);
        	input = stdin.next();
        	
        	
        	temp.evaluate(files3, input);
       
        	
      /* 
        while(true){
        	
        	
        	String input;
        	System.out.print("input fileName: ");
        	Scanner stdin = new Scanner(System.in);
        	input = stdin.next();
        	
        if(temp.estimate(input)>0.5)
        		System.out.println("spam");
        else
        	System.out.println("ham");
        
        }*/
    }

}
