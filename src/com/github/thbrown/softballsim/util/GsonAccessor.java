package com.github.thbrown.softballsim.util;

import java.util.Map;
import com.github.thbrown.softballsim.Result;
import com.github.thbrown.softballsim.cloud.Arguments;
import com.github.thbrown.softballsim.cloud.ArgumentsDeserializer;
import com.github.thbrown.softballsim.data.gson.DataStats;
import com.github.thbrown.softballsim.data.gson.DataStatsDeserializer;
import com.github.thbrown.softballsim.datasource.network.DataSourceNetworkCommand;
import com.github.thbrown.softballsim.datasource.network.DataSourceNetworkCommandDeserializer;
import com.github.thbrown.softballsim.lineup.BattingLineup;
import com.github.thbrown.softballsim.lineup.BattingLineupDeserializer;
import com.github.thbrown.softballsim.lineup.BattingLineupSerializer;
import com.github.thbrown.softballsim.optimizer.gson.OptimizerDefinitionArgument;
import com.github.thbrown.softballsim.optimizer.gson.OptimizerDefinitionArgumentDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class provides a consistent way to retrieve and re-use GSON objects
 */
public class GsonAccessor {

  private static GsonAccessor instance = new GsonAccessor();

  private Gson customGson;

  private Gson defaultGson;

  public static GsonAccessor getInstance() {
    return instance;
  }

  /**
   * @return a GSON instance with all the custom serializers/deserailizers registered for this
   *         application
   */
  public Gson getCustom() {
    if (this.customGson == null) {
      GsonBuilder gsonBuilder = new GsonBuilder();
      register(gsonBuilder);
      this.customGson = gsonBuilder.create();
    }
    return this.customGson;
  }

  protected void register(GsonBuilder gsonBuilder) {
    // Deserializers
    gsonBuilder.registerTypeAdapter(DataStats.class, new DataStatsDeserializer());
    gsonBuilder.registerTypeAdapter(DataSourceNetworkCommand.class, new DataSourceNetworkCommandDeserializer());
    gsonBuilder.registerTypeAdapter(OptimizerDefinitionArgument.class, new OptimizerDefinitionArgumentDeserializer());
    gsonBuilder.registerTypeAdapter(BattingLineup.class, new BattingLineupDeserializer());
    gsonBuilder.registerTypeAdapter(Result.class, new ResultDeserializer());
    gsonBuilder.registerTypeAdapter(Arguments.class, new ArgumentsDeserializer());

    // Serializers
    gsonBuilder.registerTypeAdapter(BattingLineup.class, new BattingLineupSerializer());

    // Alow NaN
    gsonBuilder.serializeSpecialFloatingPointValues();
  }

  /**
   * @return the default GSON instance, with no registered custom serializers/deserailizers
   */
  public Gson getDefault() {
    if (this.defaultGson == null) {
      GsonBuilder gsonBuilder = new GsonBuilder();
      gsonBuilder.serializeSpecialFloatingPointValues();
      this.defaultGson = gsonBuilder.create();
    }
    return this.defaultGson;
  }


}
