package com.github.thbrown.softballsim;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.github.thbrown.softballsim.data.gson.DataStats;
import com.github.thbrown.softballsim.datasource.network.DataSourceNetworkCommandData;
import com.github.thbrown.softballsim.datasource.network.NetworkHelper;
import com.github.thbrown.softballsim.helpers.TestGsonAccessor;
import com.github.thbrown.softballsim.server.Server;
import com.github.thbrown.softballsim.server.ServerCommandHooks;
import com.github.thbrown.softballsim.server.ServerComplete;
import com.github.thbrown.softballsim.server.ServerReady;
import com.github.thbrown.softballsim.util.Logger;

public class MonteCarloAdaptiveTest {

  @Test
  public void testMonteCarloAdaptive() throws Exception {
    final int INNINGS = 7;
    final double ALPHA = .001;
    final int LINEUP_TYPE = 2;
    final int THREAD_COUNT = 8;
    final String LINEUP = "1OiRCCmrn16iyK,Nelly,1CV6WRyspDjA7Z,1MPJ24EEyS0g6p,Devon,Jordyn";

    String[] args = {"-O", "MONTE_CARLO_ADAPTIVE", "-L", LINEUP, "-a", String.valueOf(ALPHA), "-i",
        String.valueOf(INNINGS), "-T", String.valueOf(LINEUP_TYPE), "-t", String.valueOf(THREAD_COUNT), "-F", "-P",
        "./stats/exampleData.json"};

    // Run the same simulation with the exhaustive optimizer, useful for doing a comparison
    String[] args2 = {"-O", "MONTE_CARLO_EXHAUSTIVE", "-L", LINEUP, "-g", String.valueOf(500000), "-i",
        String.valueOf(INNINGS), "-T", String.valueOf(LINEUP_TYPE), "-t", String.valueOf(THREAD_COUNT), "-P",
        "./stats/exampleData.json"};

    SoftballSim.main(args);
  }

  @Test
  public void testMonteCarloAdaptiveNetwork() throws Exception {
    final int INNINGS = 7;
    final double ALPHA = .001;
    final int LINEUP_TYPE = 2;
    final int THREAD_COUNT = 8;
    final String PLAYERS = "1OiRCCmrn16iyK,Nelly,1CV6WRyspDjA7Z,1MPJ24EEyS0g6p,Devon,Jordyn";

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
        args.put("Optimizer", String.valueOf("MONTE_CARLO_ADAPTIVE"));
        args.put("Players", String.valueOf(PLAYERS));

        args.put("innings", String.valueOf(INNINGS));
        args.put("alpha", String.valueOf(ALPHA));
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

    String[] args = {"-D", "NETWORK"};

    SoftballSim.main(args);
  }

}
