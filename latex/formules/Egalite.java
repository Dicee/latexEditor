package latex.formules;


public class Egalite extends OperateurBinaire {

	public Egalite(Formule op1, Formule op2) {
		super(op1, op2);
	}
	
	public Egalite(int op1, Formule op2) {
		super(op1, op2);
	}
	
	public Egalite(Formule op1, int op2) {
		super(op1, op2);
	}
	
	public Egalite(int op1, int op2) {
		super(op1, op2);
	}

	@Override
	public String getLateXCode() {
		return op1.getLateXCode() + " = " + op2.getLateXCode();
	}

}
