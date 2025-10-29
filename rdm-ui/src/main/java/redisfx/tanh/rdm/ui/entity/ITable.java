package redisfx.tanh.rdm.ui.entity;

import javafx.scene.control.TableCell;

public interface ITable {

    String[] getProperties();


    /**
     * 获取行号
     * @return -1为无效值
     */
    default int getRow() {
        return -1;
    }


   default  <S, T extends ITable> TableCell<T,S> getCellFactory(int i){
     return null;
   }
}
