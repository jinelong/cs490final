import java.util.Comparator;
import java.util.Scanner;

class seqComparator implements Comparator<String>{
   
    public int compare(String emp1, String emp2){
   
     
        if(emp1.compareTo(emp2)<0)
        	return -1;
        else
        	return 1;
    }
}
   