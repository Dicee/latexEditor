package guifx.utils;

public class WrongFormatException extends Exception { 
	private static final long	serialVersionUID	= 1L;
	public WrongFormatException(String    msg) { super(msg); }
	public WrongFormatException(Exception e  ) { super(e  ); }
}
