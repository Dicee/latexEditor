package latex.formules;

public abstract class Formule {
	public abstract String getLateXCode();
	public String toString() {
		return "$" + getLateXCode() + "$";
	}
}
