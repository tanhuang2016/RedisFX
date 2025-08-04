package xyz.hashdog.rdm.ui.controller;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.Tile;
import atlantafx.base.theme.Styles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

import static xyz.hashdog.rdm.ui.sampler.page.Page.FAKER;

public class ReportController extends BaseKeyController<ServerTabController> implements Initializable {
    public PieChart memory;
    public PieChart keys;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final var rnd = FAKER.random();

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30))
        );
        ObservableList<PieChart.Data> data2 = FXCollections.observableArrayList(
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30)),
                new PieChart.Data(FAKER.food().fruit(), rnd.nextInt(10, 30))
        );
        keys.setData(data);
        memory.setData(data2);
    }
}
