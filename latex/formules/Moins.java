package latex.formules;


public class Moins extends OperateurBinaire {

	public Moins(Formule op1, Formule op2) {
		super(op1, op2);
	}

	public Moins(int op1, Formule op2) {
		super(op1, op2);
	}
	
	public Moins(Formule op1, int op2) {
		super(op1, op2);
	}
	
	public Moins(int op1, int op2) {
		super(op1, op2);
	}
	
	@Override
	public String getLateXCode() {
		return op1.getLateXCode() + " - " + op2.getLateXCode();
	}
}
