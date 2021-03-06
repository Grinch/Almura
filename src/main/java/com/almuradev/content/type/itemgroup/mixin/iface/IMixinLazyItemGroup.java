/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.itemgroup.mixin.iface;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.itemgroup.ItemGroup;

public interface IMixinLazyItemGroup {

    void itemGroup(final Delegate<ItemGroup> group);
}
