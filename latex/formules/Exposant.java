package latex.formules;


public class Exposant extends OperateurBinaire {

	public Exposant(Formule op1, Formule op2) {
		super(op1, op2);
	}
	
	public Exposant(int op1, Formule op2) {
		super(op1, op2);
	}
	
	public Exposant(Formule op1, int op2) {
		super(op1, op2);
	}
	
	public Exposant(int op1, int op2) {
		super(op1, op2);
	}

	@Override
	public String getLateXCode() {
		String result = "";
		if (op1 instanceof FormuleComposee)
			result += "(" + op1.getLateXCode() + ")";
		else
			result += op1.getLateXCode();
		if (op2 instanceof FormuleComposee)
			result += "^{(" + op2.getLateXCode() + ")}";
		else result += "^{" + op2.getLateXCode() + "}";
		
		return result;
	}
}
