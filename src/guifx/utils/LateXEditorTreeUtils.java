package guifx.utils;

import static com.dici.collection.richIterator.PairRichIterator.pairIterator;
import static guifx.utils.Settings.properties;
import static guifx.utils.Settings.strings;
import static properties.ConfigProperties.LEAF_ICON;
import guifx.components.latexEditor.LateXEditorTreeView;

import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;
import latex.elements.LateXElement;

public class LateXEditorTreeUtils {
    public static List<Pair<Integer,NamedObject<LateXElement>>> namedLateXElements(List<Pair<Integer, LateXElement>> elts) {
        return pairIterator(elts.iterator(), Pair::getKey, pair -> namedLateXElement(pair.getValue())).toList();
    }

    public static LateXElement getValue(TreeItem<NamedObject<LateXElement>> treeItem) { return treeItem.getValue().bean; }
    
    public static NamedObject<LateXElement> namedLateXElement(LateXElement elt) { return new NamedObject<>(strings.getObservableProperty(elt.getType()), elt); }
    
    public static TreeItem<NamedObject<LateXElement>> newTreeItem(LateXElement elt) { return newTreeItem(namedLateXElement(elt)); }

    public static TreeItem<NamedObject<LateXElement>> newTreeItem(NamedObject<LateXElement> elt) {
        String url  = properties.getProperty(elt.bean.getType() + "Icon");
        Node   icon = new ImageView(new Image(LateXEditorTreeView.class.getResourceAsStream(url != null ? url : properties.getProperty(LEAF_ICON))));
        return icon == null ? new TreeItem<>(elt) : new TreeItem<>(elt,icon);
    }
}