package latex.formules;

public class Quotient extends OperateurBinaire {

	public Quotient(Formule op1, Formule op2) {
		super(op1, op2);
	}
	
	public Quotient(int op1, Formule op2) {
		super(op1, op2);
	}
	
	public Quotient(Formule op1, int op2) {
		super(op1, op2);
	}
	
	public Quotient(int op1, int op2) {
		super(op1, op2);
	}

	@Override
	public String getLateXCode() {
		return "\\frac{" + op1.getLateXCode() + "}{" + op2.getLateXCode() + "}";
	}
}
