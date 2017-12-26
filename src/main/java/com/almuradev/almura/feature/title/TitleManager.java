package com.almuradev.almura.feature.title;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.title.network.ClientboundPlayerSelectedTitlePacket;
import com.almuradev.almura.feature.title.network.ClientboundPlayerSelectedTitlesPacket;
import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.typesafe.config.ConfigRenderOptions;
import net.kyori.membrane.facet.Activatable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class TitleManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private static final String CONFIG_NAME = "title.conf";
    private static final String CONFIG_HEADER = "Almura title configuration\n\nFor further assistance, join #almura on EsperNet.";
    private static final String TITLES = "titles";
    private final Game game;
    private final PluginContainer container;
    private final Logger logger;
    private final ChannelBinding.IndexedMessageChannel network;
    private final Path configRoot;
    private final Map<String, Text> titles = new LinkedHashMap<>();

    private final Map<UUID, Text> serverTitles = new HashMap<>();

    @SideOnly(Side.CLIENT)
    private final Map<UUID, String> clientTitles = new HashMap<>();

    @Inject
    public TitleManager(final Game game, final PluginContainer container, Logger logger, @ChannelId(NetworkConfig.CHANNEL) final
    ChannelBinding.IndexedMessageChannel network, @ConfigDir(sharedRoot = false) final Path configRoot) {
        this.game = game;
        this.container = container;
        this.logger = logger;
        this.network = network;
        this.configRoot = configRoot;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        this.game.getCommandManager().register(this.container, TitleCommands.generateRootCommand(), "almura");
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event) {
        this.titles.clear();

        try {
            this.loadTitles();
            this.logger.info("Loaded {} title(s)", this.titles.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientConnectedToServerEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.clientTitles.clear();
    }

    @Listener
    public void onClientConnectionEventJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        // Send joining player's title to everyone
        Text selectedTitle = this.getSelectedTitleFor(player).orElse(null);
        if (selectedTitle == null) {
            selectedTitle = this.getTitlesFor(player).stream().findFirst().orElse(null);
            if (selectedTitle != null) {
                this.putSelectedTitle(player.getUniqueId(), selectedTitle);
            }
        }

        Task.builder()
                .async()
                .delayTicks(40)
                .execute(() -> this.createPlayerSelectedTitlesPacket().ifPresent((packet) -> {
                    // Send joining player everyone's title (including itself)
                    this.network.sendTo(player, packet);
                })).submit(this.container);

        if (selectedTitle != null) {
            // Java, just why
            final Text found = selectedTitle;
            this.game.getServer().getOnlinePlayers().stream().filter((p) -> !p.getUniqueId().equals(player.getUniqueId())).forEach((p) ->
                    this.network.sendTo(p, this.createAddPlayerSelectedTitlePacket(player.getUniqueId(), found)));
        }
    }

    @Listener
    public void onClientConnectionEventDisconnect(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player player) {
        this.removeSelectedTitle(player.getUniqueId());
        this.network.sendToAll(this.createRemovePlayerSelectedTitlePacket(player.getUniqueId()));
    }

    public Map<String, Text> getAllTitles() {
        return Collections.unmodifiableMap(this.titles);
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    public String getTitleForRender(UUID uniqueId) {
        checkNotNull(uniqueId);

        final String title = this.clientTitles.get(uniqueId);
        if (title == null) {
            return null;
        }

        return title;
    }

    public Set<Text> getTitlesFor(Player player) {
        checkNotNull(player);

        final Set<Text> playerTitles = new LinkedHashSet<>();

        this.titles.forEach((permission, title) -> {
            if (player.hasPermission(permission)) {
                playerTitles.add(title);
            }
        });

        return playerTitles;
    }

    public Optional<Text> getSelectedTitleFor(Player player) {
        return Optional.ofNullable(this.serverTitles.get(player.getUniqueId()));
    }

    public void refreshSelectedTitles() {
        this.createPlayerSelectedTitlesPacket()
                .ifPresent(packet -> this.game.getServer().getOnlinePlayers().forEach((player) -> this.network.sendTo(player, packet)));
    }

    public void refreshSelectedTitleFor(Player player, boolean add) {
        final Message message;
        if (add) {
            message = this.createAddPlayerSelectedTitlePacket(player.getUniqueId(), this.serverTitles.get(player.getUniqueId()));
        } else {
            message = this.createRemovePlayerSelectedTitlePacket(player.getUniqueId());
        }
        this.network.sendToAll(message);
    }

    public boolean loadTitles() throws IOException {

        // Reload titles by permission

        this.titles.clear();

        final Path titlePath = this.configRoot.resolve(CONFIG_NAME);
        boolean exists = this.createConfigIfNeeded(titlePath);

        final ConfigurationLoader<CommentedConfigurationNode> loader = this.createLoader(titlePath);
        if (!exists) {
            loader.save(loader.createEmptyNode());
        }

        final ConfigurationNode root = loader.load();

        final ConfigurationNode titleNode = root.getNode(TITLES);
        if (!titleNode.isVirtual()) {
            titleNode.getChildrenMap().forEach((permission, node) -> {
                final String title = node.getString("");
                if (!title.isEmpty()) {
                    this.titles.put(permission.toString(), TextSerializers.LEGACY_FORMATTING_CODE.deserialize(title));
                }
            });
        }

        // Re-set selected titles (they may have a title they no longer have permission for)

        this.serverTitles.clear();

        if (!this.titles.isEmpty()) {
            this.game.getServer().getOnlinePlayers().forEach((player) -> {
                Text selectedTitle = this.getSelectedTitleFor(player).orElse(null);
                final Set<Text> availableTitles = this.getTitlesFor(player);

                if (selectedTitle == null) {
                    selectedTitle = availableTitles.stream().findFirst().orElse(null);
                } else {
                    if (!availableTitles.contains(selectedTitle)) {
                        selectedTitle = null;
                    }
                }

                if (selectedTitle != null) {
                    this.serverTitles.put(player.getUniqueId(), selectedTitle);
                }
            });
        }

        return !this.titles.isEmpty();
    }

    private boolean createConfigIfNeeded(Path path) throws IOException {
        checkNotNull(path);

        boolean exists = true;
        // Ensure our file exists
        if (Files.notExists(path)) {
            Files.createDirectories(path.getParent());
            exists = false;
        }

        return exists;
    }

    private ConfigurationLoader<CommentedConfigurationNode> createLoader(Path path) {
        checkNotNull(path);

        return HoconConfigurationLoader.builder()
                .setPath(path)
                .setDefaultOptions(ConfigurationOptions.defaults().setHeader(CONFIG_HEADER))
                .setRenderOptions(
                        ConfigRenderOptions.defaults()
                                .setFormatted(true)
                                .setComments(true)
                                .setOriginComments(false)
                )
                .build();
    }

    private Optional<ClientboundPlayerSelectedTitlesPacket> createPlayerSelectedTitlesPacket() {
        if (this.serverTitles.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ClientboundPlayerSelectedTitlesPacket(Collections.unmodifiableMap(this.serverTitles)));
    }

    private ClientboundPlayerSelectedTitlePacket createAddPlayerSelectedTitlePacket(UUID uniqueId, Text title) {
        checkNotNull(uniqueId);
        checkNotNull(title);

        return new ClientboundPlayerSelectedTitlePacket(uniqueId, title);
    }

    private ClientboundPlayerSelectedTitlePacket createRemovePlayerSelectedTitlePacket(UUID uniqueId) {
        checkNotNull(uniqueId);

        return new ClientboundPlayerSelectedTitlePacket(uniqueId);
    }

    public void putSelectedTitles(Map<UUID, Text> titles) {
        checkNotNull(titles);

        this.serverTitles.clear();

        this.serverTitles.putAll(titles);
    }

    public void putSelectedTitle(UUID uniqueId, Text title) {
        checkNotNull(uniqueId);
        checkNotNull(title);

        this.serverTitles.put(uniqueId, title);
    }

    public void removeSelectedTitle(UUID uniqueId) {
        checkNotNull(uniqueId);

        this.serverTitles.remove(uniqueId);
    }

    @SideOnly(Side.CLIENT)
    public void putClientSelectedTitles(Map<UUID, String> titles) {
        checkNotNull(titles);

        this.clientTitles.clear();

        this.clientTitles.putAll(titles);
    }

    @SideOnly(Side.CLIENT)
    public void putClientSelectedTitle(UUID uniqueId, String title) {
        checkNotNull(uniqueId);
        checkNotNull(title);

        this.clientTitles.put(uniqueId, title);
    }

    @SideOnly(Side.CLIENT)
    public void removeClientSelectedTitle(UUID uniqueId) {
        checkNotNull(uniqueId);

        this.clientTitles.remove(uniqueId);
    }
}