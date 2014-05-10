package latex.formules;


public class Constante extends Formule {

	private String cst;
	
	public Constante(int i) {
		this.cst = new Integer(i).toString();
	}
	
	public Constante(double d) {
		this.cst = new Double(d).toString();
	}
	
	public Constante(float f) {
		this.cst = new Float(f).toString();
	}
	
	public String getLateXCode() {
		return cst;
	}

}
