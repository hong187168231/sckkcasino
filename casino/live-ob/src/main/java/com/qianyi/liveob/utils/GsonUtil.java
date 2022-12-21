package com.qianyi.liveob.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * <p>
 * Gson单例工具
 * </p>
 *
 * @author gawin
 * @since 2019-07-31
 */
public class GsonUtil {

    private volatile static Gson instance = null;
    private volatile static Gson instance2 = null;
    private volatile static Gson instance3 = null;

    private GsonUtil() {
    }

    public static Gson getInstanceScaleTwo() {
        if (instance == null) {
            synchronized (GsonUtil.class) {
                if (instance == null) {
                    instance = new GsonBuilder()
                            .disableHtmlEscaping()
                            .registerTypeAdapter(Double.class,
                                    (JsonSerializer<Double>) (value, theType, context) -> {
                                        if (value.isNaN()) {
                                            return new JsonPrimitive(0); // Convert NaN to zero
                                        } else if (value.isInfinite() || value.doubleValue() < 0.01) {
                                            return new JsonPrimitive(value); // Leave small numbers and infinite alone
                                        } else {
                                            // Keep 2 decimal digits only
                                            JsonPrimitive jsonPrimitive = new JsonPrimitive((new BigDecimal(value)).setScale(2, RoundingMode.DOWN));

                                            Number num = jsonPrimitive.getAsNumber();
                                            // here you can handle double int/long values
                                            // and return any type you want
                                            // this solution will transform 3.0 float to long values
                                            if (Math.ceil(num.doubleValue()) == num.longValue()) {
                                                return new JsonPrimitive(new BigDecimal(num.longValue()));
                                            } else {
                                                return jsonPrimitive;
                                            }

                                        }
                                    })
                            .create();
                }
            }
        }
        return instance;
    }

    public static Gson getInstanceWithNull() {
        if (instance2 == null) {
            synchronized (GsonUtil.class) {
                if (instance2 == null) {
                    instance2 = new GsonBuilder().serializeNulls().create();
                }
            }
        }
        return instance2;
    }

    public static Gson getInstanceForMap() {
        Gson gson = new GsonBuilder().registerTypeAdapter(HashMap.class, new JsonDeserializer<HashMap>() {
            @Override
            public HashMap<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                HashMap<String, Object> resultMap = new HashMap<>();
                JsonObject jsonObject = json.getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                for (Map.Entry<String, JsonElement> entry : entrySet) {
                    resultMap.put(entry.getKey(), entry.getValue());
                }
                return resultMap;
            }

        }).create();
        return gson;
    }

    public static String prettyPrinting(Object bean) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(bean);
    }

    public static LinkedHashMap getLinkedHashMap(String jsonData) {
        return GsonUtil.getInstance().fromJson(jsonData, new TypeToken<LinkedHashMap<Object, Object>>() {
        }.getType());
    }

    public static Gson getInstance() {
        if (instance3 == null) {
            synchronized (GsonUtil.class) {
                if (instance3 == null) {
                    instance3 = new GsonBuilder()
                            .disableHtmlEscaping()
                            .registerTypeAdapter(new TypeToken<LinkedHashMap<Object, Object>>() {
                            }.getType(), new MapDeserializerDoubleAsIntFix())
                            .create();
                }
            }
        }
        return instance3;
    }

    private static class MapDeserializerDoubleAsIntFix implements JsonDeserializer<Map<Object, Object>> {
        public static void main(String[] args) {
            String json = "{\"playerId\":472611,\"abc\":\"abc\",\"balance\":99999.9999}";
            System.out.println("json == " + json);

            long start = SystemClock.millisClock().now();
            Object o = null;
            String s = null;
            for (int i = 0; i < 1000000; i++) {
                o = GsonUtil.getInstanceScaleTwo().fromJson(json, Object.class);
                s = GsonUtil.getInstanceScaleTwo().toJson(o);
            }
            System.out.println(o);
            System.out.println(s);
            System.out.println(SystemClock.millisClock().now() - start);
        }


        @Override
        public Map<Object, Object> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return (Map<Object, Object>) read(jsonElement);
        }

        public Object read(JsonElement in) {
            if (in.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                JsonArray arr = in.getAsJsonArray();
                for (JsonElement anArr : arr) {
                    list.add(read(anArr));
                }
                return list;
            } else if (in.isJsonObject()) {
                Map<Object, Object> map = new LinkedHashMap<>();
                JsonObject obj = in.getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entitySet = obj.entrySet();
                for (Map.Entry<String, JsonElement> entry : entitySet) {
                    map.put(entry.getKey(), read(entry.getValue()));
                }
                return map;
            } else if (in.isJsonPrimitive()) {
                JsonPrimitive prim = in.getAsJsonPrimitive();
                if (prim.isBoolean()) {
                    return prim.getAsBoolean();
                } else if (prim.isString()) {
                    return prim.getAsString();
                } else if (prim.isNumber()) {
                    Number num = prim.getAsNumber();
                    // here you can handle double int/long values
                    // and return any type you want
                    // this solution will transform 3.0 float to long values
                    if (Math.ceil(num.doubleValue()) == num.longValue()) {
                        return num.longValue();
                    } else {
                        return num.doubleValue();
                    }
                }
            }
            return null;
        }
    }
}