package latex.formules;

public class Variable extends Formule {

	private String name;
	
	public Variable(String name) {
		this.name = name;
	}
	@Override
	public String getLateXCode() {
		return name;
	}
}
