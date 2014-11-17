package guifx;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author David
 */
public class IconSelectionView extends ImageView {
    
    private final int  rows, cols;
    private final double  width, height;
    private String[] commands;
    private ActionListener listener;
    private String name;
    
    public IconSelectionView(Image img, int rows, int cols, String[] commandsArr, String name) {
        super(img);
        Rectangle rect = new Rectangle(width = img.getWidth(),height = img.getHeight());
        setClip(rect);
        this.rows     = rows;
        this.cols     = cols;
        this.commands = commandsArr;
        this.name     = name;
        setOnMouseClicked((MouseEvent ev) -> {
                 fireListener(commands[pixToVect(new Point((int) ev.getX(),(int) ev.getY()))]);
         });
    }
    
    public void setActionListener(ActionListener al) {
        listener = al;
    }

    private void fireListener(String command) {
        listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command));
    }

    private int pixToVect(Point p) {
        return matToVect(mouseToBlock(p));
    }

    private int matToVect(Point coord) {
        return cols * coord.x + coord.y;
    }

    /**
     * Conversion des coordonnees pixelliques de la souris en coordonnees de bloc.
     */
    private Point mouseToBlock(Point p) {
        int blockHeight = ((int) height) / rows;
        int blockWidth  = ((int) width)  / cols;
        return new Point((int) (p.getY()) / blockHeight, (int) p.getX() / blockWidth);
    }
    
    public String getName() {
        return name;
    }
}