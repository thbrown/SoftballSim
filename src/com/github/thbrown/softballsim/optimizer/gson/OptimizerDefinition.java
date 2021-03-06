package com.github.thbrown.softballsim.optimizer.gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public class OptimizerDefinition {
  private String id;
  private String name;
  private String shortDescritpion;
  private String longDescriptionFile;
  private String img;
  private List<String> supportedLineupTypes;
  private String machine;
  private List<OptimizerDefinitionArgument> arguments;
  private boolean hideInUi;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getShortDescription() {
    return shortDescritpion;
  }

  public String getLongDescriptionFile() {
    return longDescriptionFile;
  }

  public String getImageUrl() {
    return img;
  }

  public List<String> getSupportedLineupTypes() {
    return supportedLineupTypes;
  }

  public String getMachine() {
    return machine;
  }

  public List<OptimizerDefinitionArgument> getArguments() {
    return arguments;
  }

  public boolean isHideInUi() {
    return hideInUi;
  }

  /**
   * @return a map that contains keys for all arguments for this optimization in the format longLabel
   *         -> value. The value is extracted from the CommandLine instance passed in as the argument
   *         to this method.
   */
  public Map<String, String> getArgumentsAndValuesAsMap(CommandLine cmd) {
    Map<String, String> data = new HashMap<>();
    for (OptimizerDefinitionArgument argument : arguments) {
      String key = argument.getShortLabel();
      String value = argument.getValue(cmd);
      data.put(key, value);
    }
    return data;
  }

  /**
   * @return command line options that should be available for this optimizer
   */
  public List<Option> getArgumentsAsCommandLineOptions() {
    List<Option> options = new ArrayList<>();
    for (OptimizerDefinitionArgument argument : arguments) {
      options.add(argument.getCommandLineOption());
    }
    return options;
  }
}
