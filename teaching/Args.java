package teaching;

import java.text.ParseException;
import java.util.*;

import teaching.ArgsException.ErrorCode;

//import teaching.ArgsException.ErrorCode;

public class Args {
	
	private String schema;
	private String[] args;
	private boolean valid = true;
	private Set<Character> unexpectedArguments = new TreeSet<Character>();
	private Map<Character, Boolean> booleanArgs = new HashMap<Character, Boolean>();
	private Map<Character, String> stringArgs = new HashMap<Character, String>();
	private Map<Character, Integer> intArgs = new HashMap<Character, Integer>();
	private Set<Character> argsFound = new HashSet<Character>();
	private int currentArgument;
	private char errorArgumentId = '\0';
	private String errorParameter = "TILT";
	private ErrorCode errorCode = ErrorCode.OK;
	
	public Args(String schema, String[] args) throws ParseException, ArgsException {
		this.schema = schema;
		this.args = args;
		valid = parse();
	}
	
	private boolean parse() throws ParseException, ArgsException {
		if (schema.length() == 0 && args.length == 0)
			return true;
		parseSchema();
		parseArguments();
		return valid;
	}

	private boolean parseSchema() throws ParseException {
		for(String element : schema.split(","))
		{
			if(element.length() > 0) {
				String trimmedElement = element.trim();
				try {
					parseSchemaElement(trimmedElement);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ArgsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	private void parseSchemaElement(String element) throws ParseException, ArgsException {
		char elementId = element.charAt(0);
		String elementTail = element.substring(1);
		validateSchemaElementId(elementId);
		
		try {
			ArgumentMarshaler marshaler = ArgumentMarshalerFactory.getMarshaler(elementTail);
			if (marshaler instanceof BooleanArgumentMarshaler) {
				parseBooleanSchemaElement(elementId);
			} else if (marshaler instanceof StringArgumentMarshaler) {
				parseStringSchemaElement(elementId);
			} else if (marshaler instanceof IntegerArgumentMarshaler) {
				parseIntegerSchemaElement(elementId);
			}
		} catch (ArgsException e) {
			errorCode = e.getErrorCode();
			errorArgumentId = elementId;
			throw e;
		}
	}

	private void validateSchemaElementId(char elementId) throws ParseException{
		if (!Character.isLetter(elementId)) {
			throw new ParseException 
			("Bad character:" + elementId + "in Args format: " + schema, 0);
		}
		
	}
	
	private void parseBooleanSchemaElement(char elementId) {
		booleanArgs.put(elementId, false);		
	}
	
	private void parseIntegerSchemaElement(char elementId) {
		intArgs.put(elementId, 0);		
	}
	
	private void parseStringSchemaElement(char elementId) {
		stringArgs.put(elementId,"");	
	}
	
	private boolean isStringSchemaElement(String elementTail) {
		return elementTail.equals("*");
	}
	
	private boolean isBooleanSchemaElement(String elementTail) {
		return elementTail.length() == 0;
	}
	
	private boolean isIntegerSchemaElement(String elementTail) {
		return elementTail.equals("#");
	}
	
	private boolean parseArguments() throws ArgsException {
		for (currentArgument = 0; currentArgument < args.length; currentArgument++) {
			String arg = args[currentArgument];
			parseArgument(arg);
		}
		return true;
	}

	private void parseArgument(String arg) throws ArgsException {
		if (arg.startsWith("-") && !arg.equals("-"))
			parseElements(arg);		
	}

	private void parseElements(String arg) throws ArgsException {
		for(int i = 1; i < arg.length(); i++)  
			parseElement(arg.charAt(i));		
	}

	private void parseElement(char argChar) throws ArgsException{
		if (setArgument(argChar))
			argsFound.add(argChar);
		else {
			unexpectedArguments.add(argChar);
			throw new ArgsException(ErrorCode.UNEXPECTED_ARGUMENT, argChar, null);
		}		
	}

	private boolean setArgument(char argChar) throws ArgsException{
		if (isBooleanArg(argChar))
			setBooleanArg(argChar, true);
		else if (isStringArg(argChar))
			setStringArg(argChar);
		else if (isIntArg(argChar))
			setIntArg(argChar);
		else
			return false;
		return true;
	}
	
	private boolean isIntArg(char argChar) {return intArgs.containsKey(argChar);}
	
	private void setIntArg(char argChar) throws ArgsException {
		currentArgument++;
		String parameter = null;
		try {
		   parameter = args[currentArgument];
		   intArgs.put(argChar, Integer.parseInt(parameter));
		} catch (ArrayIndexOutOfBoundsException e) {
		    valid = false;
		    errorArgumentId = argChar;
			errorCode = ErrorCode.MISSING_INTEGER;
			throw new ArgsException(ErrorCode.MISSING_INTEGER, argChar, parameter);
		} catch (NumberFormatException e) {
		    valid = false;
			errorArgumentId = argChar;
			errorParameter = parameter;
			errorCode = ErrorCode.INVALID_INTEGER;
			throw new ArgsException(ErrorCode.INVALID_INTEGER, argChar, parameter);
		}		
	}

	private void setStringArg(char argChar) throws ArgsException {
		currentArgument++;		
		try {		
		   stringArgs.put(argChar, args[currentArgument]);
		} catch (ArrayIndexOutOfBoundsException e) {
		    valid = false;
			errorArgumentId = argChar;			
			errorCode= ErrorCode.MISSING_STRING;
			throw new ArgsException();
		}		
	}

	private boolean isStringArg(char argChar) {
		return stringArgs.containsKey(argChar);
	}

	private void setBooleanArg(char argChar, boolean value) {
		booleanArgs.put(argChar, value);		
	}

	private boolean isBooleanArg(char argChar) {
		return booleanArgs.containsKey(argChar);
	}
	
	public int cardinality() {
		return argsFound.size();
	}

	public String usage() {
		if (schema.length() > 0)
			return "-[" + schema + "]";
		else
			return "";
	}
	
	public String errorMessage() throws Exception {
		switch (errorCode) {
		case OK:
			throw new Exception("TILT: Should not get here.");
		case UNEXPECTED_ARGUMENT:
			return unexpectedArgumentMessage();
		case MISSING_STRING:
			return String.format("Could not find string parameter for -%c.", errorArgumentId);
		case INVALID_INTEGER:
			return String.format("Argument -%c expects an integer but was '%s'.", errorArgumentId, errorParameter);
		case MISSING_INTEGER:
			return String.format("Could not find integer parameter for -%c.", errorArgumentId);
		case INVALID_DOUBLE:
			return String.format("Argument -%c expects a double but was '%s'.", errorArgumentId, errorParameter);
		case MISSING_DOUBLE:
			return String.format("Could not find double parameter for -%c.", errorArgumentId);
		case INVALID_ARGUMENT_NAME:
			return String.format("'%c' is not a valid argument name.", errorArgumentId);
		case INVALID_ARGUMENT_FORMAT:
			return String.format("'%s' is not a valid argument format.", errorArgumentId);
		}
		return "";
	}
	
	private String unexpectedArgumentMessage() {
		StringBuffer message = new StringBuffer("Argument(s) -");
		for(char c : unexpectedArguments) {
			message.append(c);
		}
		message.append("  unexpected.");
		return message.toString();
	}
	
	private boolean falseIfNull(Boolean b) {
		return b != null && b;
	}
	
	private int zeroIfNull(Integer i) {
		return i == null ? 0 : i;
	}
	
	private String blankIfNull(String s) {
		return s == null ? "" : s;
	}
	
	public String getString(char arg) {		
		return blankIfNull(stringArgs.get(arg));
	}
	
	public int getInt(char arg) {
		return zeroIfNull(intArgs.get(arg));
	}

	public boolean getBoolean(char arg) {
		return falseIfNull(booleanArgs.get(arg));
	}
	
	public boolean has(char arg) {
		return argsFound.contains(arg);
	}
	
	public boolean isValid() {
		return valid;
	}

}
