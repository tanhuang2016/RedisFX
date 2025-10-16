/* SPDX-License-Identifier: MIT */

package redisfx.tanh.rdm.ui.sampler.page.components;

import atlantafx.base.controls.Spacer;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.layout.InputGroup;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.BBCodeParser;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import redisfx.tanh.rdm.common.util.Util;
import redisfx.tanh.rdm.ui.common.Applications;
import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;
import redisfx.tanh.rdm.ui.common.RedisDataTypeEnum;
import redisfx.tanh.rdm.ui.common.TabPaneStyleEnum;
import redisfx.tanh.rdm.ui.entity.config.KeyTabPaneSetting;
import redisfx.tanh.rdm.ui.entity.config.ServerTabPaneSetting;
import redisfx.tanh.rdm.ui.sampler.event.DefaultEventBus;
import redisfx.tanh.rdm.ui.sampler.event.EventBus;
import redisfx.tanh.rdm.ui.sampler.event.TabPaneEvent;
import redisfx.tanh.rdm.ui.sampler.page.OutlinePage;

import java.util.List;
import java.util.UUID;

import static javafx.scene.control.TabPane.TabClosingPolicy;
import static redisfx.tanh.rdm.ui.util.LanguageManager.language;

public final class TabPanePage extends OutlinePage {

    public static final String NAME = "main.setting.window.tab";
    public static Sub server;
    public static Sub key;
    private static final EventBus EVENT_BUS = DefaultEventBus.getInstance();

    @Override
    public String getName() {
        return NAME;
    }

    public TabPanePage() {
        super();

        addPageHeader();
        addFormattedText(String.format("""
            [i]TabPane[/i] %s"""
        , language("main.setting.window.tab.describe")));
        server = getSub(1);
        key = getSub(2);
        addSection(language("main.setting.window.tab.server"), server.pane);
        addSection(language("main.setting.window.tab.key"), key.pane);
    }

    private Sub getSub(int type) {
        Sub sub = new Sub(type);
        Pane playground = sub.playground();
        sub.pane = playground;
        return sub;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Playground                                                            //
    ///////////////////////////////////////////////////////////////////////////


    public class Sub{
        private final int type;
        private Side tabSide = Side.TOP;
        private boolean fullWidth = false;
        protected String style ;
        protected boolean animated;
        protected boolean dense;
        private Pane pane;
        private TabPaneEvent.EventType eventType;

        public Sub(int type) {
            this.type=type;
            if(type==2){
                eventType=TabPaneEvent.EventType.KEY_TAB_PANE_CHANGE;
                KeyTabPaneSetting settings = Applications.getConfigSettings(ConfigSettingsEnum.KEY_TAB_PANE.name);
                tabSide=Side.valueOf(settings.getSide());
                fullWidth=settings.isFullWidth();
                style=settings.getStyle();
                animated=settings.isAnimated();
                dense=settings.isDense();
            }else {
                eventType=TabPaneEvent.EventType.SERVER_TAB_PANE_CHANGE;
                ServerTabPaneSetting settings = Applications.getConfigSettings(ConfigSettingsEnum.SERVER_TAB_PANE.name);
                tabSide=Side.valueOf(settings.getSide());
                fullWidth=settings.isFullWidth();
                style=settings.getStyle();
                animated=settings.isAnimated();
                dense=settings.isDense();
            }
        }

        private Pane playground() {
            var tabs = createTabPane();
            tabs.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);

            var tabsLayer = new BorderPane();
            tabsLayer.setTop(tabs);
            tabs.getTabs().addListener((ListChangeListener<Tab>) c ->
                    updateTabsWidth(tabsLayer, tabs, fullWidth)
            );

            var controller = createController(tabsLayer, tabs);
            doRotateTabs(tabsLayer,tabs,this.tabSide);

            var controllerLayer = new BorderPane(controller);
            controllerLayer.setMinSize(500, 300);
            controllerLayer.setMaxSize(500, 300);

            String str=language("main.setting.window.tab.server.describe");
            if(type==2){
                str=language("main.setting.window.tab.key.describe");
            }
            var description = BBCodeParser.createFormattedText(str);

            var stack = new StackPane(tabsLayer, controllerLayer);
            stack.getStyleClass().add(Styles.BORDERED);
            stack.setMinSize(600, 500);

            return new VBox(VGAP_10, description, stack);
        }


        private TitledPane createController(BorderPane borderPane, TabPane tabs) {
            // == BUTTONS ==

            var toTopBtn = new Button(null, new FontIcon(Feather.ARROW_UP));
            toTopBtn.getStyleClass().addAll(Styles.BUTTON_ICON);
            toTopBtn.setOnAction(e -> rotateTabs(borderPane, tabs, Side.TOP));

            var toRightBtn = new Button(null, new FontIcon(Feather.ARROW_RIGHT));
            toRightBtn.getStyleClass().addAll(Styles.BUTTON_ICON);
            toRightBtn.setOnAction(e -> rotateTabs(borderPane, tabs, Side.RIGHT));

            var toBottomBtn = new Button(null, new FontIcon(Feather.ARROW_DOWN));
            toBottomBtn.getStyleClass().addAll(Styles.BUTTON_ICON);
            toBottomBtn.setOnAction(e -> rotateTabs(borderPane, tabs, Side.BOTTOM));

            var toLeftBtn = new Button(null, new FontIcon(Feather.ARROW_LEFT));
            toLeftBtn.getStyleClass().addAll(Styles.BUTTON_ICON);
            toLeftBtn.setOnAction(e -> rotateTabs(borderPane, tabs, Side.LEFT));

            var appendBtn = new Button(null, new FontIcon(Feather.PLUS));
            appendBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT);
            if(type==1){
                appendBtn.setOnAction(e -> tabs.getTabs().add(createRandomTab()));

            }else {
                appendBtn.setOnAction(e -> tabs.getTabs().add(createRandomTabKey()));
            }

            var buttonsPane = new BorderPane();
            buttonsPane.setMinSize(120, 120);
            buttonsPane.setMaxSize(120, 120);

            buttonsPane.setCenter(appendBtn);

            buttonsPane.setTop(toTopBtn);
            BorderPane.setAlignment(toTopBtn, Pos.CENTER);

            buttonsPane.setRight(toRightBtn);
            BorderPane.setAlignment(toRightBtn, Pos.CENTER);

            buttonsPane.setBottom(toBottomBtn);
            BorderPane.setAlignment(toBottomBtn, Pos.CENTER);

            buttonsPane.setLeft(toLeftBtn);
            BorderPane.setAlignment(toLeftBtn, Pos.CENTER);

            // == TOGGLES ==

            var closeableToggle = new ToggleSwitch();
            closeableToggle.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    tabs.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
                } else {
                    tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
                }
            });

            var tagToggle = new ToggleSwitch();
            tagToggle.setSelected(false);
            tagToggle.setDisable(true);
            tagToggle.selectedProperty().addListener((obs, old, val) -> {
                if (val != null && val) {
                    tabs.setStyle("");
                } else {
                    tabs.setStyle("""
                    -fx-open-tab-animation:none;\
                    -fx-close-tab-animation:none;"""
                    );
                }
            });

            var animatedToggle = new ToggleSwitch();
            animatedToggle.setSelected(this.animated);
            animatedToggle.selectedProperty().addListener((obs, old, val) -> {
                if (val != null && val) {
                    tabs.setStyle("");
                } else {
                    tabs.setStyle("""
                    -fx-open-tab-animation:none;\
                    -fx-close-tab-animation:none;"""
                    );
                }
                animated=val;
                EVENT_BUS.publish(new TabPaneEvent(eventType));
            });

            var fullWidthToggle = new ToggleSwitch();
            fullWidthToggle.setSelected(this.fullWidth);
            fullWidthToggle.selectedProperty().addListener((obs, old, val) -> {
                if (val != null) {
                    updateTabsWidth(borderPane, tabs, val);
                    fullWidth = val;
                    EVENT_BUS.publish(new TabPaneEvent(eventType));
                }
            });

            var denseToggle = new ToggleSwitch();
            denseToggle.setSelected(this.dense);
            denseToggle.selectedProperty().addListener(
                    (obs, old, val) ->{
                        Styles.toggleStyleClass(tabs, Styles.DENSE);
                        dense = val;
                        EVENT_BUS.publish(new TabPaneEvent(eventType));
                    }

            );

            var disableToggle = new ToggleSwitch();
            disableToggle.selectedProperty().addListener((obs, old, val) -> {
                if (val != null) {
                    tabs.setDisable(val);
                }
            });

            var togglesGrid = new GridPane();
            togglesGrid.setHgap(10);
            togglesGrid.setVgap(10);
//        togglesGrid.addRow(0, createGridLabel("Closeable"), closeableToggle);
            if(type==2){
                togglesGrid.addRow(0, createGridLabel("Key Tag Color"), tagToggle);
            }
            togglesGrid.addRow(1, createGridLabel("Animated"), animatedToggle);
            togglesGrid.addRow(2, createGridLabel("Full width"), fullWidthToggle);
            togglesGrid.addRow(3, createGridLabel("Dense"), denseToggle);
//        togglesGrid.addRow(4, createGridLabel("Disable"), disableToggle);

            // == TAB STYLE ==

            var styleToggleGroup = new ToggleGroup();

            var defaultStyleToggle = new ToggleButton("Default");
            defaultStyleToggle.setToggleGroup(styleToggleGroup);
            defaultStyleToggle.setUserData(
                    TabPaneStyleEnum.DEFAULT
            );
            defaultStyleToggle.setSelected(this.style.equals(TabPaneStyleEnum.DEFAULT.name));

            var floatingStyleToggle = new ToggleButton("Floating");
            floatingStyleToggle.setToggleGroup(styleToggleGroup);
            floatingStyleToggle.setUserData(
                    TabPaneStyleEnum.FLOATING
            );
            floatingStyleToggle.setSelected(this.style.equals(TabPaneStyleEnum.FLOATING.name));
            var classicStyleToggle = new ToggleButton("Classic");
            classicStyleToggle.setToggleGroup(styleToggleGroup);
            classicStyleToggle.setUserData(
                    TabPaneStyleEnum.CLASSIC
            );
            classicStyleToggle.setSelected(this.style.equals(TabPaneStyleEnum.CLASSIC.name));
            List<String> styles = TabPaneStyleEnum.getByName(this.style).classes;
            Styles.addStyleClass(tabs, styles.get(0), styles.get(1), styles.get(2));

            styleToggleGroup.selectedToggleProperty().addListener((obs, old, val) -> {
                if (val != null) {
                    TabPaneStyleEnum styleEnum = (TabPaneStyleEnum) val.getUserData();
                    style = styleEnum.name;
                    List<String> classes =styleEnum.classes;
                    Styles.addStyleClass(tabs, classes.get(0), classes.get(1), classes.get(2));
                    EVENT_BUS.publish(new TabPaneEvent(eventType));
                }
            });

            var styleBox = new InputGroup(
                    defaultStyleToggle, floatingStyleToggle, classicStyleToggle
            );
            styleBox.setAlignment(Pos.CENTER);

            // == LAYOUT ==

            var controls = new HBox(
                    40, new Spacer(), buttonsPane, togglesGrid, new Spacer()
            );
            controls.setAlignment(Pos.CENTER);

            var root = new TitledPane("Controller", new VBox(30, controls, styleBox));
            root.setCollapsible(false);

            return root;
        }


        private void updateTabsWidth(BorderPane borderPane, TabPane tabs, boolean val) {
            tabs.tabMinWidthProperty().unbind();

            // reset tab width
            if (!val) {
                tabs.setTabMinWidth(Region.USE_COMPUTED_SIZE);
                return;
            }

            // There are two issues with full-width tabs.
            // - minWidth is applied to the tab itself but to internal .tab-container,
            //   thus we have to subtract tab paddings that are normally set via CSS.
            // - .control-buttons-tab appears automatically and can't be disabled via
            //   TabPane property.
            // Overall this feature should be supported by the TabPane internally, otherwise
            // it's hard to make it work properly.

            if (tabs.getSide() == Side.TOP || tabs.getSide() == Side.BOTTOM) {
                tabs.tabMinWidthProperty().bind(borderPane.widthProperty()
                        .subtract(18) // .control-buttons-tab width
                        .divide(tabs.getTabs().size())
                        .subtract(28) // .tab paddings
                );
            }
            if (tabs.getSide() == Side.LEFT || tabs.getSide() == Side.RIGHT) {
                tabs.tabMinWidthProperty().bind(borderPane.heightProperty()
                        .subtract(18) // same as above
                        .divide(tabs.getTabs().size())
                        .subtract(28)
                );
            }
        }

        private TabPane createTabPane() {
            var tabs = new TabPane();
            tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
            tabs.setMinHeight(60);

            // NOTE: Individually disabled tab is still closeable even while it looks
            //       like disabled. To prevent it from closing one can use "black hole"
            //       event handler. #javafx-bug
            if(type==1){
                tabs.getTabs().addAll(
                        createRandomTab(),
                        createRandomTab(),
                        createRandomTab()
                );
            }else {
                tabs.getTabs().addAll(
                        createRandomTabKey(),
                        createRandomTabKey(),
                        createRandomTabKey()
                );
            }


            return tabs;
        }


        private void rotateTabs(BorderPane borderPane, TabPane tabs, Side side) {
            if (tabSide == side) {
                return;
            }
            doRotateTabs(borderPane,tabs, side);
            EVENT_BUS.publish(new TabPaneEvent(eventType));
        }

        private void doRotateTabs(BorderPane borderPane, TabPane tabs, Side side) {


            borderPane.getChildren().removeAll(tabs);
            tabSide = side;

            Platform.runLater(() -> {
                tabs.setSide(side);
                switch (side) {
                    case TOP -> borderPane.setTop(tabs);
                    case RIGHT -> borderPane.setRight(tabs);
                    case BOTTOM -> borderPane.setBottom(tabs);
                    case LEFT -> borderPane.setLeft(tabs);
                }
                updateTabsWidth(borderPane, tabs, fullWidth);
            });
        }

        private Tab createRandomTab() {
//            var tab = new Tab(FAKER.cat().name());
            var tab = new Tab(Util.generateRandomString(5));
            tab.setGraphic(new FontIcon(randomIcon()));
            return tab;
        }
        private Label createGridLabel(String text) {
            var label = new Label(text);
            label.setAlignment(Pos.CENTER_RIGHT);
            label.setMaxWidth(Double.MAX_VALUE);
            return label;
        }

        private Tab createRandomTabKey() {
            int i = RANDOM.nextInt(RedisDataTypeEnum.values().length);
            RedisDataTypeEnum value = RedisDataTypeEnum.values()[i];
            var tab = new Tab(String.format("%s|%s|%s", 0,value.type, UUID.randomUUID().toString().split("-")[0]));
            tab.setGraphic(new FontIcon(Feather.KEY));
            return tab;
        }


        public int getType() {
            return type;
        }

        public Side getTabSide() {
            return tabSide;
        }

        public boolean isFullWidth() {
            return fullWidth;
        }

        public String getStyle() {
            return style;
        }

        public boolean isAnimated() {
            return animated;
        }

        public boolean isDense() {
            return dense;
        }
    }









}
