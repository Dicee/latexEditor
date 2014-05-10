package latex.formules;


public class Indice extends OperateurBinaire {

	public Indice(Formule op1, Formule op2) {
		super(op1, op2);
	}
	
	public Indice(int op1, Formule op2) {
		super(op1, op2);
	}
	
	public Indice(Formule op1, int op2) {
		super(op1, op2);
	}
	
	public Indice(int op1, int op2) {
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
			result += "_{(" + op2.getLateXCode() + ")}";
		else result += "_{" + op2.getLateXCode() + "}";
		
		return result;
	}
}
