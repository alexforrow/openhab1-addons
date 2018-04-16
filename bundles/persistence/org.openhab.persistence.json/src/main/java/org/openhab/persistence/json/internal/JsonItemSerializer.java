/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
 package org.openhab.persistence.json.internal;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.util.Date;

import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.State;

/**
 * A {@link JsonSerializer} and {@link JsonDeserializer} for the {@link JsonItem}
 *
 * @author Alex Forrow
 *
 */
public class JsonItemSerializer implements JsonSerializer<JsonItem>, JsonDeserializer<JsonItem> {

    /**
     * Overridden to simply write out the state to json
     *
     * @param jsonitem the {@link JsonItem} to write out
     * @param type the type
     * @param context the serialization context
     */
    @Override
    public JsonElement serialize(JsonItem item, Type type, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("name", item.getName());
        root.addProperty("type", item.getState().getClass().getSimpleName());
        root.addProperty("state", item.getState().toString());
        root.addProperty("timestamp", item.getTimestamp().getTime());

        return root;
    }

    /**
     * Overridden to simply read the json
     *
     * @param elm the {@link JsonElement} to read from
     * @param type the type
     * @param context the serialization context
     */
    @Override
    public JsonItem deserialize(JsonElement elm, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject jo = (JsonObject) elm;

        JsonItem newItem = new JsonItem();
        newItem.setName(jo.get("name").getAsString());
        newItem.setTimestamp(new Date(jo.get("timestamp").getAsLong()));

        String stateStr = jo.get("state").getAsString();
        State state = null;
        switch (jo.get("type").getAsString()) {
            case "DateTimeType":
                state = DateTimeType.valueOf(stateStr);
                break;
            case "DecimalType":
                state = DecimalType.valueOf(stateStr);
                break;
            case "HSBType":
                state = HSBType.valueOf(stateStr);
                break;
            case "OnOffType":
                state = OnOffType.valueOf(stateStr);
                break;
            case "OpenClosedType":
                state = OpenClosedType.valueOf(stateStr);
                break;
            case "PercentType":
                state = PercentType.valueOf(stateStr);
                break;
            case "UnDefType":
                state = UnDefType.UNDEF;
                break;
            default:
                state = StringType.valueOf(stateStr);
                break;
        }

        newItem.setState(state);

        return newItem;
    }
}
