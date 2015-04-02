package guifx;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 *
 * @author David
 */
public class IconSelectionBox extends VBox {
    public IconSelectionBox() {
        super();      
        setPadding(new Insets(5));
        setSpacing(10);
    }
    
    public void addSelectionView(IconSelectionView view) { 
    	Label label = new Label();
    	label.textProperty().bind(view.nameProperty());
    	getChildren().addAll(label,view);
    }
}