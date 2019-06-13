package com.github.thbrown.softballsim.gson;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.github.thbrown.softballsim.OptimizationResult;
import com.github.thbrown.softballsim.Result;
import com.github.thbrown.softballsim.SoftballSim;
import com.github.thbrown.softballsim.datasource.NetworkProgressTracker;
import com.github.thbrown.softballsim.datasource.ProgressTracker;
import com.github.thbrown.softballsim.lineup.DummyAlternatingBattingLineup;
import com.github.thbrown.softballsim.lineup.DummyOrdinaryBattingLineup;
import com.github.thbrown.softballsim.lineupgen.LineupGenerator;
import com.google.gson.Gson;

public class MonteCarloExaustiveData extends BaseOptimizationData {
  private int innings;
  private int iterations;
  private int lineupType;

  private Map<String, List<String>> initialLineup;
  private int startIndex;
  private Double initialScore; // Allow for nulls
  
  private Map<Long, Long> initialHistogram;
  
  public int getInnings() {
    return innings;
  }

  public int getIterations() {
    return iterations;
  }

  public int getLineupType() {
    return lineupType;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public Map<String, List<String>> getInitialLineup() {
    return initialLineup;
  }
  
  public Double getInitialScore() {
    return initialScore;
  }

  public Map<Long, Long> getInitialHistogram() {
    return initialHistogram;
  }

  @Override
  public void runSimulation(Gson gson, PrintWriter network) {
    int gamesToSimulate = this.getIterations() != 0 ? this.getIterations() : SoftballSim.DEFAULT_GAMES_TO_SIMULATE;
    int inningsToSimulate = this.getInnings() != 0 ? this.getInnings() : SoftballSim.DEFAULT_INNINGS_PER_GAME;
    int lineupType = this.getLineupType();
    
    // Transform the json lineup given by the network to the comma separated form this program expects
    StringBuilder transformedData = new StringBuilder();
    if(this.getLineupType() == 1) {
      for(ParsedPlayerEntry entry : this.getPlayers()) {
        String outs = buildCommaSeparatedList("0", entry.getOuts());
        String singles = buildCommaSeparatedList("1", entry.getSingles());
        String doubles = buildCommaSeparatedList("2", entry.getDoubles());
        String triples = buildCommaSeparatedList("3", entry.getTriples());
        String homeruns = buildCommaSeparatedList("4", entry.getHomeruns());
        String line = joinIgnoreEmpty(",", entry.getId(), outs, singles, doubles, triples, homeruns, "\n");
        transformedData.append(line);
      }
    } else if(this.getLineupType() == 2 || this.getLineupType() == 3) {
      // TODO: We should probably have both lineup types take the same stats data and the non-gendered types can just ignore gender
      for(ParsedPlayerEntry entry : this.getPlayers()) {
        String outs = buildCommaSeparatedList("0", entry.getOuts());
        String singles = buildCommaSeparatedList("1", entry.getSingles());
        String doubles = buildCommaSeparatedList("2", entry.getDoubles());
        String triples = buildCommaSeparatedList("3", entry.getTriples());
        String homeruns = buildCommaSeparatedList("4", entry.getHomeruns());
        
        // Using A/B instead of M/F so we can reuse this optimizer for other use case (like maybe old/young). <- Still trying to decide if this was a good idea
        String line = joinIgnoreEmpty(",", entry.getId(), entry.getGender().equals("F") ? "B" : "A", outs, singles, doubles, triples, homeruns, "\n");
        transformedData.append(line);
      }
    } else {
      throw new UnsupportedOperationException("Unrecognized lineup type " + this.getLineupType());
    }
    
    LineupGenerator generator = SoftballSim.getLineupGenerator(String.valueOf(this.getLineupType()));
    generator.readDataFromString(transformedData.toString());
    
    if(generator.size() <= 0) {
      throw new IllegalArgumentException("There are no possible lineups for this lineup type and player combination");
    }

    // Account for initial conditions if specified
    long startIndex = this.getStartIndex();
    Map<Long, Long> initialHisto = this.getInitialHistogram();
    Double initialScore = this.getInitialScore();
    Result initialResult = null;
    Map<String, List<String>> initialLineup = this.getInitialLineup();
    
    if(initialLineup != null && initialScore != null && initialHisto != null) {
      // Build a list of players
      if(lineupType == 1 || lineupType == 3) {
        initialResult = new Result(initialScore, new DummyOrdinaryBattingLineup(initialLineup.get("GroupA")));
      } else if(lineupType == 2) {
        initialResult = new Result(initialScore, new DummyAlternatingBattingLineup(initialLineup.get("GroupA"), initialLineup.get("GroupB")));
      } else {
        throw new RuntimeException("Unrecognized lineup type: " + lineupType);
      }

      System.out.println("Initial conditions were specified");
      System.out.println(initialResult);
      System.out.println(initialHisto);
    } else {
      initialHisto = null;
      initialResult = null;
    }
    
    long startTime = System.currentTimeMillis();   
    
    ProgressTracker tracker = new NetworkProgressTracker(generator.size(), SoftballSim.DEFAULT_UPDATE_FREQUENCY, startIndex, gson, network);
    
    OptimizationResult result = SoftballSim.simulateLineups(generator, gamesToSimulate, inningsToSimulate, startIndex, tracker, initialResult, initialHisto);
    
    System.out.println(result.toString());
    System.out.println("Simulation took " + (System.currentTimeMillis() - startTime) + " milliseconds.");
    
    // Send the results back over the network
    Map<String,Object> completeCommand = new HashMap<>();
    completeCommand.put("command", "COMPLETE");
    completeCommand.put("lineup", result.getLineup());
    completeCommand.put("score", result.getScore());
    completeCommand.put("histogram", result.getHistogram());
    completeCommand.put("total", generator.size());
    completeCommand.put("complete", generator.size());
    String jsonCompleteCommand = gson.toJson(completeCommand);
    network.println(jsonCompleteCommand);
    System.out.println("SENT: \t\t" + jsonCompleteCommand);
  }

  private String joinIgnoreEmpty(String delimiter, String...strings) {
    StringJoiner joiner = new StringJoiner(delimiter);
    for(String s : strings) {
      if(s.equals("") || s == null) {
        continue;
      }
      joiner.add(s);
    }
    return joiner.toString();
  }

  private String buildCommaSeparatedList(String value, int count) {
    if(count <= 0) {
      return "";
    }
    
    StringBuilder result = new StringBuilder();
    for(int i = 0; i < count; i ++) {
        result.append(value);
        result.append(",");
    }
    return result.length() > 0 ? result.substring(0, result.length() - 1): "";
  }

}
