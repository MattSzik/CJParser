/*CS 146 Project 1
Matthew Sziklay

Parses C, C++, and Java code, ensuring that all parentheses, brackets, curly braces, quotes,
single quotes, and (#ifdef/#ifndef/#if defined/#if !defined/#endif/#else all match up.
Due to time constraints I did not implement multiple error checking, but error checking still 
works as defined in the assignment.
Currently this code does not work in some scenarios involving ifdef, resulting in
a 
*/
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.Stack;


public class CppJavaParser {
	private static int errorCount = 0; //Part of an attempt at detecting multiple errors.

	public static void main(String[] args){
		boolean inQuotes =false, inComment =false, inSingleQuote=false, inIf = false,
				inSingleComment =false; //Should have used an enum rather than so many booleans.
		String line = null; //The current line the program is parsing.
		char c; //The current char the program is comparing.
		Stack<Character> s = new Stack<Character>();  //Stack of chars such as (, [, {
		Stack<String> strStack = new Stack<String>(); //Stack of strings such as /*, #ifdef
		int lineNum = 0; 
		Scanner in = new Scanner(System.in);
		String fName = in.next(); //File name is retrieved from user argument.
		System.out.println("Retrieving file " + fName);
		try{
		Scanner code = new Scanner(new FileReader(fName)); 
		while(code.hasNextLine()){
			lineNum++;
			line = code.nextLine();
			for(int i = 0;i<line.length();i++){ //While line has characters left to go through...
				c = line.charAt(i);				//Get the ith char and compare it to all symbols.
				if(c == '#' && !inComment && !inSingleComment && !inQuotes && !inSingleQuote){
					if(line.contains("#ifdef") || line.contains("#ifndef") || line.contains("#if defined")
							|| line.contains("#if !defined"))
						if(!inIf){
						strStack.push("if");
						inIf = true;
						}
					
				if(line.contains("#endif") && !inComment && !inQuotes
						&& !inSingleQuote){
					if(inIf){
						strStack.pop();				//Otherwise, pop and change status to false.
						inIf = false;
					}
					else{
						reportError(lineNum, i, strStack.peek().toString(), line);
					}
				}
			}
			//If / is detected, see if the next char is either * or / and act accordingly.	
			if(c == '/' && !inComment && !inSingleComment && !inQuotes && !inSingleQuote){ 
				if(line.length() >= i+2) //To avoid out of bounds errors.
				if(line.charAt(i+1) == '*'){
				strStack.push("/*");
				inComment = true;
				} if(line.length() >= i+2) //This line was changed for the makeup.
				if(line.charAt(i+1) == '/'){
					strStack.push("//");
					inSingleComment = true;
				}
			}
			//If * is detected, see if next char is /. If in comment, pop strStack. 
			//Otherwise, report an error.
				if(c == '*' && !inQuotes){
					if(line.length() >= i+2)
					if(line.charAt(i+1) == '/'){
						if(!strStack.isEmpty())
						if(strStack.peek().equals("/*")){
							inComment = false;
						}
						else
							reportError(lineNum, i, s.peek().toString(), line);
							strStack.pop();
					}
				}
			//These 3 follow the same general rule. Just push if it's not currently being ignored.
			if((c == '{' || c == '(' || c == '[') && !inComment && !inSingleComment 
				&& !inQuotes && !inSingleQuote)
					s.push(c);
			
			//If current char is a closing character, check the top of the stack
			//to make sure the corresponding opening character is present.
			//If not, report an error. 
			if(c == '}' && !inComment && !inSingleComment && !inQuotes && !inSingleQuote){
				if(!s.isEmpty())
				if(s.peek() != '{')
					reportError(lineNum, i, s.peek().toString(), line);
				s.pop();
			}	
			if(c == ']' && !inComment && !inSingleComment && !inQuotes && !inSingleQuote){
				if(!s.isEmpty())
				if(s.peek() != '[')
					reportError(lineNum, i, s.peek().toString(), line);
				s.pop();
			}
			if(c == ')' && !inComment && !inSingleComment && !inQuotes && !inSingleQuote){
				if(!s.isEmpty())
				if(s.peek() != '(')
					reportError(lineNum, i, s.peek().toString(), line);
				s.pop();
			}
			//Single quote check. Ended up using a String as I was having difficulties with
			// '/''
			if(c == "'".charAt(0) && !inComment && !inSingleComment && !inQuotes){
				if(!inSingleQuote){
				s.push("'".charAt(0));
				inSingleQuote=true;
				}
				else
					if(s.peek() == "'".charAt(0)){
						inSingleQuote=false;
						s.pop();
					}
					else{
						if(line.charAt(i-1) != '\\') //Ignore the single quote if specifically marked.
						reportError(lineNum, i, "'", line);
					}
			}
			//Basically the same as ' but with a different boolean.
			if(c == '"' && !inComment && !inSingleComment && !inSingleQuote){
				if(!inQuotes){
				s.push('"');
				inQuotes = true;
				}
				else if(line.charAt(i-1) != '\\'){
					s.pop(); //Error checking for quotes is later.
					inQuotes = false;
					}
				}
		}
		
	
	if(inQuotes)
		reportError(lineNum, -1, "\"", line); //Report error if line ends without closing quote.
	if(inSingleQuote)
		reportError(lineNum, -1, "'", line);
	if(inSingleComment){ //Return to normal after a // comment.
		strStack.pop(); 
		inSingleComment = false;
	}
	}
		//After file has been parsed...
		while(!s.isEmpty()) //Report an error for every unclosed item in the stack.
			reportError(lineNum, -1, s.pop().toString(), line);
		while(!strStack.isEmpty()){ //Report an error for an unclosed /*
			if(strStack.peek().equals("/*"))
				reportError(lineNum, -1, strStack.pop(), line);
			if(strStack.peek().equals("if")) //Report an #if without corresponding #endif
				reportError(lineNum, -1, "# definition", line);
		}
		code.close(); //Finished scanning.
		}
		catch(FileNotFoundException i){
			System.err.println("File not found.");
		}
		if(errorCount !=0) //Irrelevant code. This was used while attempting to parse multiple errors.
			System.exit(-1); 
		else{
			System.out.println("No errors to report.");
			System.exit(0);
		}
	}
	
	public static void reportError(int lineNum, int charNum, String s, String line){
		errorCount++;
		if(charNum!=-1)
		System.err.println(lineNum + ":" + (charNum+1) + ": \"" + line + "\": ERROR: " + s + " does not match "
				+ line.charAt(charNum) + ".");
		else
			System.err.println(lineNum + ":" + line.length()+ ": \"" + line 
					+ "\": ERROR: " + s + " unclosed.");
		System.exit(-1);
	}
	
}
