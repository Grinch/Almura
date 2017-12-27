/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import com.almuradev.almura.shared.event.Witness;
import com.typesafe.config.ConfigRenderOptions;
import net.kyori.membrane.facet.Activatable;
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
import org.spongepowered.api.event.game.state.GameStartingServerEvent;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerPageManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;
    private final Logger logger;
    private final Path configRoot;
    private final Map<String, Page> pages = new HashMap<>();

    @Inject
    public ServerPageManager(final Game game, Logger logger, @ConfigDir(sharedRoot = false) final Path configRoot) {
        this.game = game;
        this.logger = logger;
        this.configRoot = configRoot.resolve(GuideConfig.DIR_GUIDE);
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) throws IOException {
        this.pages.clear();
        
        if (Files.notExists(this.configRoot)) {
            Files.createDirectories(this.configRoot);
        }

        final Path pagesRoot = this.configRoot.resolve(GuideConfig.DIR_GUIDE_PAGES);
        if (Files.notExists(pagesRoot)) {
            Files.createDirectories(pagesRoot);
        }

        final PageWalker pageWalker = new PageWalker(this.logger);
        Files.walkFileTree(pagesRoot, pageWalker);

        pageWalker.found.forEach((pageFile) -> {
            final ConfigurationLoader<CommentedConfigurationNode> loader = this.createLoader(pageFile);
            try {
                final String id = pageFile.getFileName().toString().split("\\.")[0];

                final ConfigurationNode rootNode = loader.load();
                final int index = rootNode.getNode(GuideConfig.INDEX).getInt(0);
                final String name = rootNode.getNode(GuideConfig.NAME).getString(id);
                final String title = rootNode.getNode(GuideConfig.TITLE).getString("");

                final ConfigurationNode lastModifiedNode = rootNode.getNode(GuideConfig.LastModified.LAST_MODIFIED);
                final String lastModifiedModifierRaw = lastModifiedNode.getNode(GuideConfig.LastModified.MODIFIER).getString("");

                UUID lastModifiedModifier;
                try {
                    lastModifiedModifier = UUID.fromString(lastModifiedModifierRaw);
                } catch (Exception ex) {
                    lastModifiedModifier = new UUID(0L, 0L);
                }

                final String lastModifiedTimeRaw = lastModifiedNode.getString(GuideConfig.LastModified.TIME);

                Instant lastModifiedTime;
                try {
                    lastModifiedTime = Instant.parse(lastModifiedTimeRaw);
                } catch (Exception ex) {
                    lastModifiedTime = Instant.now();
                }

                final ConfigurationNode createdNode = rootNode.getNode(GuideConfig.Created.CREATED);
                final String createdCreatorRaw = createdNode.getNode(GuideConfig.Created.CREATOR).getString("");

                UUID createdCreator;
                try {
                    createdCreator = UUID.fromString(createdCreatorRaw);
                } catch (Exception ex) {
                    createdCreator = new UUID(0L, 0L);
                }

                final String createdTimeRaw = lastModifiedNode.getString(GuideConfig.Created.TIME);

                Instant createdTime;
                try {
                    createdTime = Instant.parse(createdTimeRaw);
                } catch (Exception ex) {
                    createdTime = Instant.now();
                }

                final String content = rootNode.getNode(GuideConfig.CONTENT).getString("");

                final Page page = new Page(id, createdCreator, createdTime);
                page.setIndex(index);
                page.setName(name);
                page.setTitle(title);
                page.setLastModifier(lastModifiedModifier);
                page.setLastModified(lastModifiedTime);
                page.setContent(content);

                this.logger.info("Loaded '" + id + "' page.");

                this.pages.put(id, page);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.logger.info("Loaded " + this.pages.size() + " page(s).");
    }

    private ConfigurationLoader<CommentedConfigurationNode> createLoader(Path path) {
        return HoconConfigurationLoader.builder()
                .setPath(path)
                .setDefaultOptions(ConfigurationOptions.defaults())
                .setRenderOptions(
                        ConfigRenderOptions.defaults()
                                .setFormatted(true)
                                .setComments(true)
                                .setOriginComments(false)
                )
                .build();
    }

    public Map<String, Page> getAvailablePagesFor(Player player) {
        return this.pages.entrySet()
                .stream()
                .filter(entry -> player.hasPermission("almura.guide.page." + entry.getKey()))
                .sorted(Comparator.comparingInt(p -> p.getValue().getIndex()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) ->
                {
                    throw new IllegalStateException(String.format("Duplicate key %s", k));
                }, LinkedHashMap::new));
    }

    public Optional<Page> getPage(String id) {
        return Optional.ofNullable(this.pages.get(id));
    }

    private static final class PageWalker implements FileVisitor<Path> {
        private final Logger logger;
        private final Set<Path> found = new HashSet<>();

        PageWalker(Logger logger) {
            this.logger = logger;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final Path fileName = file.getFileName();
            if (fileName.toString().endsWith(".conf")) {
                this.found.add(file);
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exception) throws IOException {
            if (exception != null && !(exception instanceof NoSuchFileException)) {
                this.logger.error("Encountered an exception while visiting file '{}'", file, exception);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path directory, IOException exception) throws IOException {
            if (exception != null && !(exception instanceof NoSuchFileException)) {
                this.logger.error("Encountered an exception while visiting directory '{}'", directory, exception);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
