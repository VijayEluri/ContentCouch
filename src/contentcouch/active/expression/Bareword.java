package contentcouch.active.expression;

import togos.rra.Response;

public class Bareword implements Expression {
	
	public String text;
	
	public Bareword(String text) {
	    if( text == null ) throw new RuntimeException("Tried to create Bareword with null text");
		this.text = text;
	}
	
	public String toString() {
		return text;
	}
	
	public String toUri() {
		throw new RuntimeException("Bareword cannot be converted to a URI! (" + text + ")");
	}

	public Response eval() {
		throw new RuntimeException("Bareword cannot be evaluated!");
	}
	
	public boolean isConstant() {
		return true;
	}
}
