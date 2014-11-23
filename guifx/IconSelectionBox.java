/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
