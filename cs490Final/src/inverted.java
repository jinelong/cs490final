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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// the word is already stemmed
// stopword removed

public class inverted {
	final static boolean DEBUG = true; 
	final static String TRAINING_PATH = "/tmp/spamHam/training2/";
	final static String TESTING_PATH = "/tmp/spamHam/testing/";
	final static String ROOT_PATH = "/tmp/spamHam/";
	
	static int totalNumOfFiles = 0;
	static int totalNumOfSpam = 0;
	static int totalNumOfHam = 0;
	static int totalWords = 0;
	
	FileOutputStream ostream ;
    DataOutputStream out;
    BufferedWriter bw ;
	
	
	
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
	
	static ArrayList<invertedSlot> wordList = new ArrayList<invertedSlot>();
	static ArrayList<String> rawList = new ArrayList<String>();
	
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
	
	
	public boolean checkWord(String word){
		
		Pattern p = Pattern.compile("\\W");
		Matcher m = p.matcher(word);
		 
		return m.matches();
		
	}

public void training(File[] files) throws IOException {
    for (File file : files) {
        if (!file.isDirectory() && file.getName().contains(".txt")) {
           
            //System.out.println("File: " + file.getName());
            
            // for calculating probablity of spam and ham
            // spam / total
            // ham / total
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
            FileInputStream fstream = new FileInputStream(TRAINING_PATH+fileName);
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
	
//	Ph = (double)totalNumOfHam/totalNumOfSpam;
//	Ps = (double)totalNumOfSpam/totalNumOfHam;
	
	for(int i =0;i < wordList.size(); i++){
    	wordList.get(i).hamProb = (double)wordList.get(i).hamCount/(wordList.get(i).hamCount+wordList.get(i).spamCount);
		wordList.get(i).spamProb = (double)wordList.get(i).spamCount/(wordList.get(i).hamCount+wordList.get(i).spamCount);
		
    }
	
	 if(DEBUG)
	    	showList();
	 
	 
	 
	}//training
	   
	//return true if it is spam
	
	//Psw = (Pws * Ps)/ (Pws*Ps+ Pwh*Ph)


	private double[] getProbs(String word){
		double [] retval = new double[2];
		retval[0] = 0;
		retval[1] = 0;
		
		if(rawList.contains(word)){
			for(invertedSlot s: wordList){
				if(s.word.equals(word)){
					/*
					 * double Pwh = 0; //the probability that the word "replica" appears in ham messages.calcualte this for each word
						double Pws = 0; // probability that the word "replica" appears in spam messages;
					 */
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
     			
     			Pwh = returnPair[0];
     			Pws = returnPair[1];
     			
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
         
     	// System.out.println("t1: " + t1.doubleValue());
     	// System.out.println("t2: " + t2.doubleValue());
      
         spamScore = term1/(term1+term2);
         
         //Psw /= counter;
         System.out.println("spamScore is " + spamScore + " counter is " + counter + " term1: " + term1 + " term2: "+ term2);
        
         return spamScore;
	}
    
	public void getSpamQuota(File[] files, String flag) throws IOException{
		
			
		 	 ostream = new FileOutputStream(ROOT_PATH+"outLog");
             out = new DataOutputStream(ostream);
             bw = new BufferedWriter(new OutputStreamWriter(out));
            double total = 0;
            int counter = 0;
            
            System.out.println("-----getSpamQuota----");
		   for (File file : files) {
		        if (!file.isDirectory() && file.getName().contains(".txt")) {
		         
		        	if(flag.equals("spm")){
			            if(!file.getName().contains("spm"))
			            	continue;
		        	}
			        else{
			        	if(file.getName().contains("spm"))
			            	continue;
			        }
			            	
		            
		            String fileName = file.getName().trim();
		         
		           double retval =  estimate(fileName);
		            total += retval;
		            bw.write(fileName + " " + retval+"\n");
		            counter ++;
		 
		        }//if
		   }//for
		bw.close();
		out.close();
		ostream.close();
		
		System.out.println("count: " + counter + " total " + total +" files\t" + "averyge score: " + total/counter);
	}//getSpamQuota
    public static void main(String []args) throws IOException {
    	inverted temp = new inverted();
    	
        File[] files = new File(TRAINING_PATH).listFiles();
        temp.training(files);
        
        System.out.println("training done");
        
        File[] files2 = new File(TESTING_PATH).listFiles();
       
        while(true){
        	
        	
        	String input;
        	System.out.print("spm or msg? ");
        	Scanner stdin = new Scanner(System.in);
        	input = stdin.next();
        	
        	
        	temp.getSpamQuota(files2, input);
        }
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
