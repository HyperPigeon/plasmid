package xyz.nucleoid.plasmid.game.common;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.widget.BossBarWidget;
import xyz.nucleoid.plasmid.game.common.widget.GameWidget;
import xyz.nucleoid.plasmid.game.common.widget.SidebarWidget;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for applying various {@link GameWidget} implementations for all players within a {@link GameSpace}.
 *
 * @see GlobalWidgets#addTo(GameActivity)
 * @see SidebarWidget
 * @see BossBarWidget
 */
public final class GlobalWidgets implements AutoCloseable {
    private final GameSpace gameSpace;
    private final List<GameWidget> widgets = new ArrayList<>();

    private GlobalWidgets(GameSpace gameSpace) {
        this.gameSpace = gameSpace;
    }

    /**
     * Creates a {@link GlobalWidgets} instance and registers it to the given {@link GameActivity}.
     * All players within this activity will have the added widgets displayed to them.
     *
     * @param activity the activity to add this instance to
     * @return a {@link GlobalWidgets} instance which can be used to add various widgets for all players
     */
    public static GlobalWidgets addTo(GameActivity activity) {
        GlobalWidgets widgets = new GlobalWidgets(activity.getGameSpace());

        activity.listen(GamePlayerEvents.ADD, widgets::onAddPlayer);
        activity.listen(GamePlayerEvents.REMOVE, widgets::onRemovePlayer);
        activity.listen(GameActivityEvents.DISABLE, widgets::close);

        return widgets;
    }

    /**
     * Adds a sidebar for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param title the title for the sidebar
     * @return the created {@link SidebarWidget}
     */
    public SidebarWidget addSidebar(Text title) {
        return this.addWidget(new SidebarWidget(this.gameSpace, title));
    }

    /**
     * Adds a boss bar for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param title the title for the boss bar
     * @return the created {@link BossBarWidget}
     */
    public BossBarWidget addBossBar(Text title) {
        return this.addWidget(new BossBarWidget(title));
    }

    /**
     * Adds a boss bar for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param title the title for the boss bar
     * @param color the color for the boss bar
     * @param style the style for the bossbar
     * @return the created {@link BossBarWidget}
     */
    public BossBarWidget addBossBar(Text title, BossBar.Color color, BossBar.Style style) {
        return this.addWidget(new BossBarWidget(title, color, style));
    }

    /**
     * Adds a {@link GameWidget} for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param widget the widget to add
     * @param <T> the type of widget being added
     * @return the added widget
     */
    public <T extends GameWidget> T addWidget(T widget) {
        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            widget.addPlayer(player);
        }
        this.widgets.add(widget);
        return widget;
    }

    /**
     * Removes a {@link GameWidget} for all players associated with this {@link GlobalWidgets} instance.
     *
     * @param widget the widget to remove
     */
    public void removeWidget(GameWidget widget) {
        if (this.widgets.remove(widget)) {
            widget.close();
        }
    }

    private void onAddPlayer(ServerPlayerEntity player) {
        for (GameWidget widget : this.widgets) {
            widget.addPlayer(player);
        }
    }

    private void onRemovePlayer(ServerPlayerEntity player) {
        for (GameWidget widget : this.widgets) {
            widget.removePlayer(player);
        }
    }

    @Override
    public void close() {
        for (GameWidget widget : this.widgets) {
            widget.close();
        }
        this.widgets.clear();
    }
}
