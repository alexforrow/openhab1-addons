/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.persistence.json.internal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;

/**
 * This is a basic {@link PersistenceService} implementation to store state
 * to simple json files on disk
 *
 * @author Alex Forrow
 * @since 1.9.0
 */
public class JsonPersistenceService implements QueryablePersistenceService {

    private static final String SERVICE_NAME = "json";

    protected final static String DB_FOLDER_NAME = getUserDataFolder() + File.separator + "json";

    private static final Logger logger = LoggerFactory.getLogger(JsonPersistenceService.class);


    public void activate(final BundleContext bundleContext, final Map<String, Object> config) {
        logger.debug("json persistence service is being activated");

        File folder = new File(DB_FOLDER_NAME);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
              logger.error("Failed to create one or more directories in the path '{}'", DB_FOLDER_NAME);
              logger.error("Json persistence service activation has failed.");
              return;
            }
        }

        logger.debug("Json persistence service is now activated");
    }

    public void deactivate(final int reason) {
        logger.debug("Json persistence service deactivated");
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    @Override
    public void store(Item item) {
        store(item, null);
    }

    @Override
    public void store(Item item, String alias) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(JsonItem.class, new JsonItemSerializer());
        gsonBuilder.setPrettyPrinting();
        final Gson gson = gsonBuilder.create();

        if (alias == null) {
            alias = item.getName();
        }

        JsonItem saveItem = new JsonItem();
        saveItem.setName(item.getName());
        saveItem.setState(item.getState());
        saveItem.setTimestamp(new Date());

        Path path = Paths.get(getItemFile(alias));
        try {
          Files.write(path, (gson.toJson(saveItem) + System.lineSeparator()).getBytes());
          logger.debug("Stored {} with value {}", alias, item.getState().toString());
        } catch (IOException e) {
          logger.error("Failed persisting {}: {}", alias, e.getMessage());
        }
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(JsonItem.class, new JsonItemSerializer());
        gsonBuilder.setPrettyPrinting();
        final Gson gson = gsonBuilder.create();

        String filename = getItemFile(filter.getItemName());

        try {
          JsonReader reader = new JsonReader(new FileReader(filename));
          JsonItem item = gson.fromJson(reader, JsonItem.class);
          logger.debug("Loaded state {} for {}",item.getState().toString(), filter.getItemName());
          return Collections.singletonList(item);
        } catch (FileNotFoundException e) {
          logger.debug("No state available for {}", filter.getItemName());
          return Collections.emptyList();
        }
    }

    private static String getUserDataFolder() {
        String progArg = System.getProperty("smarthome.userdata");
        if (progArg != null) {
            return progArg + File.separator + "persistence";
        } else {
            return "etc";
        }
    }

    private static String getItemFile(String name) {
        return DB_FOLDER_NAME + File.separator + name;
    }

}
