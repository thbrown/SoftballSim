package com.github.thbrown.softballsim;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.github.thbrown.softballsim.server.ServerCommandHooks;
import com.github.thbrown.softballsim.server.ServerComplete;
import com.github.thbrown.softballsim.server.ServerReady;
import com.github.thbrown.softballsim.util.Logger;
import com.github.thbrown.softballsim.data.gson.DataStats;
import com.github.thbrown.softballsim.datasource.network.DataSourceNetworkCommandData;
import com.github.thbrown.softballsim.datasource.network.NetworkHelper;
import com.github.thbrown.softballsim.helpers.TestGsonAccessor;
import com.github.thbrown.softballsim.server.Server;

public class MonteCarloExhaustiveTest {

  @Test
  public void testDataSourceFileSystem() throws Exception {
    final int INNINGS = 7;
    final int GAMES = 100;
    final int LINEUP_TYPE = 1;
    final int THREAD_COUNT = 1;

    String[] args = {"-O", "MONTE_CARLO_EXHAUSTIVE", "-F", "-L",
        "1OiRCCmrn16iyK,Oscar,Molly,Nelly,1CV6WRyspDjA7Z,1MPJ24EEyS0g6p",
        "-g", String.valueOf(GAMES), "-i", String.valueOf(INNINGS), "-T", String.valueOf(LINEUP_TYPE), "-t",
        String.valueOf(THREAD_COUNT)};

    SoftballSim.main(args);
  }

  @Test
  public void testDataSourceNetwork() throws Exception {
    final int INNINGS = 7;
    final int GAMES = 100;
    final int LINEUP_TYPE = 1;
    final int THREAD_COUNT = 4;

    // Setup test server
    ServerCommandHooks hooks = new ServerCommandHooks() {
      public boolean onReady(ServerReady data, NetworkHelper network) throws Exception {
        // Get the sample stats from the file system
        String statsJson = new String(Files.readAllBytes(Paths.get("./stats/exampleData.json")));
        DataStats statsObject = TestGsonAccessor.getInstance().getCustom().fromJson(statsJson,
            DataStats.class);

        // Define the network args
        Map<String, String> args = new HashMap<>();
        args.put("Lineup-type", String.valueOf(LINEUP_TYPE));
        args.put("innings", String.valueOf(INNINGS));
        args.put("games", String.valueOf(GAMES));
        args.put("threads", String.valueOf(THREAD_COUNT));

        // Create the corresponding command and write it to the network
        DataSourceNetworkCommandData dataCommand = new DataSourceNetworkCommandData(statsObject, args);
        network.writeCommand(dataCommand);
        return false;
      }

      @Override
      public boolean onComplete(ServerComplete data, NetworkHelper network) throws Exception {
        Logger.log("COMPLETE");
        return true;
      }
    };
    Server.start(hooks);

    String[] args = {"-D", "NETWORK", "-O", "MONTE_CARLO_EXHAUSTIVE", "-L",
        "1OiRCCmrn16iyK,Oscar,Molly,Nelly,1CV6WRyspDjA7Z,1MPJ24EEyS0g6p"};

    SoftballSim.main(args);
  }

}
