package redisfx.tanh.rdm.ui.entity;

import javafx.scene.control.TableCell;

public interface ITable {

    String[] getProperties();


   default  <S, T extends ITable> TableCell<T,S> getCellFactory(int i){
     return null;
   }
}
