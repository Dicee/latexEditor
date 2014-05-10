package latex.formules;

public class FormuleComposee extends Formule {

	private Operateur op;
	
	public FormuleComposee(Operateur op) {
		this.op = op;
	}
	
	public String getLateXCode() {
		return op.getLateXCode();
	}

	public Operateur getOperateur() {
		return op;
	}
}
