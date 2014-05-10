package latex.formules;

public class Produit extends OperateurBinaire {
	
	public Produit(Formule op1, Formule op2) {
		super(op1, op2);
	}

	public Produit(int op1, Formule op2) {
		super(op1, op2);
	}
	
	public Produit(Formule op1, int op2) {
		super(op1, op2);
	}
	
	public Produit(int op1, int op2) {
		super(op1, op2);
	}
	
	public String getLateXCode() {
		String result;
		if (op1 instanceof Constante && op2 instanceof Constante)
			result = op1.getLateXCode() + " \\times " + op2.getLateXCode();
		else if (!(op1 instanceof FormuleComposee) && op2 instanceof Variable)
			result = op1.getLateXCode() + " " + op2.getLateXCode();
		else 
			result = parenthesage(op1) + " \\cdot " + parenthesage(op2);
		return result;
	}

	private String parenthesage(Formule f) {
		if (f instanceof FormuleComposee) {
			Operateur op =  ((FormuleComposee) f).getOperateur();
			if (op instanceof Plus || op instanceof Moins)
			return "(" + op.getLateXCode() + ")";
		}
		return	f.getLateXCode();	
	}
}
