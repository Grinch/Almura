/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.type.material;

import com.almuradev.almura.content.type.material.registry.MapColorRegistryModule;
import com.almuradev.almura.content.type.material.registry.MaterialRegistryModule;
import com.almuradev.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public class MaterialModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.registry()
                .module(MapColor.class, MapColorRegistryModule.class)
                .module(Material.class, MaterialRegistryModule.class);
    }
}
