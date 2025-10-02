/* SPDX-License-Identifier: MIT */

package redisfx.tanh.rdm.ui.sampler.page.general;

import atlantafx.base.theme.Styles;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import redisfx.tanh.rdm.ui.sampler.event.DefaultEventBus;
import redisfx.tanh.rdm.ui.sampler.event.ThemeEvent;
import redisfx.tanh.rdm.ui.sampler.page.OutlinePage;
import redisfx.tanh.rdm.ui.sampler.theme.SamplerTheme;
import redisfx.tanh.rdm.ui.sampler.theme.ThemeManager;
import redisfx.tanh.rdm.ui.sampler.util.Lazy;
import redisfx.tanh.rdm.ui.util.GuiUtil;

import java.net.URI;
import java.util.Objects;

import static redisfx.tanh.rdm.ui.sampler.theme.ThemeManager.DEFAULT_FONT_SIZE;
import static redisfx.tanh.rdm.ui.util.LanguageManager.language;


@SuppressWarnings("UnnecessaryLambda")
public final class ThemePage extends OutlinePage {

    public static final String NAME = "main.setting.general.theme";

    private static final ThemeManager TM = ThemeManager.getInstance();
    private static final String DEFAULT_FONT_ID = "Default";
    private static final Image SCENE_BUILDER_ICON = GuiUtil.ICON_REDIS;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean canDisplaySourceCode() {
        return false;
    }

    @Override
    public @Nullable URI getJavadocUri() {
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////

    private final ReadOnlyObjectWrapper<Color> bgBaseColor = new ReadOnlyObjectWrapper<>(Color.WHITE);
    private final Lazy<ThemeRepoManagerDialog> themeRepoManagerDialog;
    private final Lazy<ContrastCheckerDialog> contrastCheckerDialog;
//    private final Lazy<SceneBuilderDialog> sceneBuilderDialog;
    private final ColorPalette colorPalette;
    private final ColorScale colorScale = new ColorScale(bgBaseColor);
    private final ChoiceBox<SamplerTheme> themeSelector = createThemeSelector();
    private final ComboBox<String> fontFamilyChooser = createFontFamilyChooser();
    private final Spinner<Integer> fontSizeSpinner = createFontSizeSpinner();

    public ThemePage() {
        super();

        themeRepoManagerDialog = new Lazy<>(() -> {
            var dialog = new ThemeRepoManagerDialog();
            dialog.setClearOnClose(true);
            return dialog;
        });

        contrastCheckerDialog = new Lazy<>(() -> {
            var dialog = new ContrastCheckerDialog(bgBaseColor);
            dialog.setClearOnClose(true);
            return dialog;
        });

//        sceneBuilderDialog = new Lazy<>(() -> {
//            var dialog = new SceneBuilderDialog();
//            dialog.setClearOnClose(true);
//            dialog.setOnClose(e -> dialog.reset());
//            return dialog;
//        });

        colorPalette = new ColorPalette(colorBlock -> {
            ContrastCheckerDialog dialog = contrastCheckerDialog.get();
            dialog.getContent().setValues(
                colorBlock.getFgColorName(),
                colorBlock.getFgColor(),
                colorBlock.getBgColorName(),
                colorBlock.getBgColor()
            );
            dialog.show(getScene());
        }, bgBaseColor);

        DefaultEventBus.getInstance().subscribe(ThemeEvent.class, e -> {
            var eventType = e.getEventType();
            if (eventType == ThemeEvent.EventType.THEME_ADD || eventType == ThemeEvent.EventType.THEME_REMOVE) {
                themeSelector.getItems().setAll(TM.getRepository().getAll());
                selectCurrentTheme();
            }
            if (eventType == ThemeEvent.EventType.THEME_CHANGE || eventType == ThemeEvent.EventType.COLOR_CHANGE) {
                colorPalette.updateColorInfo(Duration.seconds(1));
                colorScale.updateColorInfo(Duration.seconds(1));
                fontFamilyChooser.getSelectionModel().select(DEFAULT_FONT_ID);
                fontSizeSpinner.getValueFactory().setValue(DEFAULT_FONT_SIZE);
            }
        });

        // mandatory base bg for flatten color calc
        Styles.appendStyle(this, "-fx-background-color", "-color-bg-default");
        backgroundProperty().addListener(
            (obs, old, val) -> bgBaseColor.set(val != null && !val.getFills().isEmpty()
                ? (Color) val.getFills().get(0).getFill()
                : Color.WHITE
            ));

        addPageHeader();
        addNode(createThemeManagementSection());
//        addSection("Scene Builder", createSceneBuilderSection());
        addSection(language("main.setting.general.theme.palette"), createColorPaletteSection());
        addSection(language("main.setting.general.theme.scale"), createColorScaleSection());

        //Platform.runLater(this::selectCurrentTheme);
    }

    @Override
    protected void onRendered() {
        super.onRendered();
        colorPalette.updateColorInfo(Duration.ZERO);
        colorScale.updateColorInfo(Duration.ZERO);
    }

    private Node createThemeManagementSection() {
        var themeRepoBtn = new Button(null, new FontIcon(Material2OutlinedMZ.SETTINGS));
        themeRepoBtn.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        themeRepoBtn.setTooltip(new Tooltip("Settings"));
        themeRepoBtn.setOnAction(e -> {
            ThemeRepoManagerDialog dialog = themeRepoManagerDialog.get();
            dialog.getContent().update();
            dialog.show(getScene());
        });

        var accentSelector = new AccentColorSelector();

        // ~

        var grid = new GridPane();
        grid.setHgap(HGAP_20);
        grid.setVgap(VGAP_10);
        grid.addRow(0, new Label(language("main.setting.general.theme.color")), themeSelector, themeRepoBtn);
        grid.addRow(1, new Label(language("main.setting.general.theme.accent")), accentSelector);
        grid.addRow(2, new Label(language("main.setting.general.theme.font")), new HBox(10, fontFamilyChooser, fontSizeSpinner));

        return grid;
    }

//    private Node createSceneBuilderSection() {
//        var sceneBuilderBtn = new Button("SceneBuilder Integration");
//        sceneBuilderBtn.setGraphic(new ImageView(SCENE_BUILDER_ICON));
//        sceneBuilderBtn.setOnAction(e -> {
//            SceneBuilderDialog dialog = sceneBuilderDialog.get();
//            dialog.show(getScene());
//        });
//
//        var description = BBCodeParser.createFormattedText("""
//            While SceneBuilder does not support adding custom themes, it is \
//            possible to overwrite looked-up CSS paths to make the existing \
//            SceneBuilder menu options load custom CSS files."""
//        );
//
//        return new VBox(VGAP_20, description, sceneBuilderBtn);
//    }

    private Node createColorPaletteSection() {
        var description = createFormattedText(String.format("""
            %s
                        
           %s
                      
            [ul]
            [li]4.5:1 %s[/li]
            [li]3:1 %s (>24px)[/li]
            [li]3:1 %s[/li]
            [/ul]
            """,language("main.setting.general.theme.palette.describe1")
                ,language("main.setting.general.theme.palette.describe2")
                ,language("main.setting.general.theme.palette.describe3")
                ,language("main.setting.general.theme.palette.describe4")
                ,language("main.setting.general.theme.palette.describe5")), true
        );

        return new VBox(VGAP_10, description, colorPalette);
    }

    private Node createColorScaleSection() {
        var description = createFormattedText(String.format("""
            %s"""
        ,language("main.setting.general.theme.scale.describe")), false);

        return new VBox(VGAP_10, description, colorScale);
    }

    private ChoiceBox<SamplerTheme> createThemeSelector() {
        var choiceBox = new ChoiceBox<SamplerTheme>();

        var themes = TM.getRepository().getAll();
        choiceBox.getItems().setAll(themes);

        // set initial value
        var currentTheme = Objects.requireNonNullElse(TM.getTheme(), TM.getDefaultTheme());
        themes.stream()
            .filter(t -> Objects.equals(currentTheme.getName(), t.getName()))
            .findFirst()
            .ifPresent(t -> choiceBox.getSelectionModel().select(t));

        // must be after setting the initial value
        choiceBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null && getScene() != null) {
                TM.setTheme(val,true);
            }
        });
        choiceBox.setPrefWidth(310);

        choiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SamplerTheme theme) {
                return theme != null ? theme.getName() : "";
            }

            @Override
            public SamplerTheme fromString(String themeName) {
                return TM.getRepository().getAll().stream()
                    .filter(t -> Objects.equals(themeName, t.getName()))
                    .findFirst()
                    .orElse(null);
            }
        });

        return choiceBox;
    }

    private ComboBox<String> createFontFamilyChooser() {
        var comboBox = new ComboBox<String>();
        comboBox.setPrefWidth(200);

        // keyword to reset font family to its default value
        comboBox.getItems().add(DEFAULT_FONT_ID);
        comboBox.getItems().addAll(FXCollections.observableArrayList(Font.getFamilies()));

        // select active font family value on page load
        comboBox.getSelectionModel().select(TM.getFontFamily());
        comboBox.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                TM.setFontFamily(DEFAULT_FONT_ID.equals(val) ? ThemeManager.DEFAULT_FONT_FAMILY_NAME : val,true);
            }
        });

        return comboBox;
    }

    private Spinner<Integer> createFontSizeSpinner() {
        var spinner = new Spinner<Integer>(
            ThemeManager.SUPPORTED_FONT_SIZE.get(0),
            ThemeManager.SUPPORTED_FONT_SIZE.get(ThemeManager.SUPPORTED_FONT_SIZE.size() - 1),
            TM.getFontSize()
        );
        spinner.setPrefWidth(100);

        // Instead of this we should obtain font size from a rendered node.
        // But since it's not trivial (thanks to JavaFX doesn't expose relevant API)
        // we just keep current font size inside ThemeManager singleton.
        // It works fine if ThemeManager default font size value matches
        // default theme font size value.
        spinner.getValueFactory().setValue(TM.getFontSize());

        spinner.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                TM.setFontSize(val,true);
            }
        });

        return spinner;
    }

    private void selectCurrentTheme() {
        if (TM.getTheme() != null) {
            themeSelector.getItems().stream()
                .filter(t -> Objects.equals(TM.getTheme().getName(), t.getName()))
                .findFirst()
                .ifPresent(t -> themeSelector.getSelectionModel().select(t));
        }
    }
}
