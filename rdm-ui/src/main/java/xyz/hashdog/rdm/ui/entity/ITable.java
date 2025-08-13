package xyz.hashdog.rdm.ui.entity;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import xyz.hashdog.rdm.ui.util.GuiUtil;

public interface ITable {

    String[] getProperties();


   default  <S, T extends ITable> TableCell<T,S> getCellFactory(int i){
     return null;
   }
}
