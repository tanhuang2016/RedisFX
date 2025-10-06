/* SPDX-License-Identifier: MIT */

package redisfx.tanh.rdm.ui.sampler.layout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import redisfx.tanh.rdm.ui.sampler.theme.SamplerTheme;
import redisfx.tanh.rdm.ui.sampler.theme.ThemeManager;

import java.util.Objects;

final class ThemeDialog extends ModalDialog {

    private final TilePane thumbnailsPane = new TilePane(20, 20);
    private final ToggleGroup thumbnailsGroup = new ToggleGroup();

    public ThemeDialog() {
        super();

        setId("theme-dialog");
        header.setTitle("Select a theme");
        content.setBody(createContent());
        content.setFooter(null);

        updateThumbnails();

        thumbnailsGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            if (val != null && val.getUserData() instanceof SamplerTheme theme) {
                ThemeManager.getInstance().setTheme(theme,true);
            }
        });
    }

    private VBox createContent() {
        thumbnailsPane.setAlignment(Pos.TOP_CENTER);
        thumbnailsPane.setPrefColumns(3);
        thumbnailsPane.setStyle("-color-thumbnail-border:-color-border-subtle;");

        var root = new VBox(thumbnailsPane);
        root.setPadding(new Insets(20));

        return root;
    }

    private void updateThumbnails() {
        var tm = ThemeManager.getInstance();

        thumbnailsPane.getChildren().clear();
        tm.getRepository().getAll().forEach(theme -> {
            var thumbnail = new ThemeThumbnail(theme);
            thumbnail.setToggleGroup(thumbnailsGroup);
            thumbnail.setUserData(theme);
            thumbnail.setSelected(Objects.equals(
                tm.getTheme().getName(),
                theme.getName()
            ));
            thumbnailsPane.getChildren().add(thumbnail);
        });
    }
}
