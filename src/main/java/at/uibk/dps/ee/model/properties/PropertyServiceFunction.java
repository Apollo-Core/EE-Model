package at.uibk.dps.ee.model.properties;

import com.google.gson.JsonObject;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.AbstractPropertyService;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * Static method container offering convenient access to the attributes of the
 * nodes modeling functions.
 * 
 * @author Fedor Smirnov
 *
 */
public final class PropertyServiceFunction extends AbstractPropertyService {

  private static final String propNameUsageType = Property.UsageType.name();
  private static final String propNameInput = Property.Input.name();
  private static final String propNameOutput = Property.Output.name();
  private static final String propNameNegligibleWorkload = Property.NegligibleWorkload.name();

  /**
   * No constructor
   */
  private PropertyServiceFunction() {}

  /**
   * Properties of the function nodes.
   * 
   * @author Fedor Smirnov
   *
   */
  protected enum Property {
    /**
     * The type of the modeled function
     */
    UsageType,
    /**
     * The input of the function
     */
    Input,
    /**
     * The output of the function
     */
    Output,
    /**
     * True if the task introduces a negligible workload on the resource onto which
     * it is mapped
     */
    NegligibleWorkload
  }

  /**
   * The types of the modeled function.
   * 
   * @author Fedor Smirnov
   *
   */
  public enum UsageType {
    /**
     * Functions performing the actual calculation/processing defined by the user
     * (the practical purpose of the WF execution).
     */
    User,
    /**
     * Local functions which realize the data and control flow defined by the WF
     * designer with some degree of processing.
     */
    Utility,
    /**
     * Functions which do not result in computation, but only detail the data flow
     */
    DataFlow
  }

  /**
   * Returns true iff the given task introduces a negligible workload onto the
   * resource where it is mapped
   * 
   * @param task the given task
   * @return true iff the given task introduces a negligible workload onto the
   *         resource where it is mapped
   */
  public static boolean hasNegligibleWorkload(final Task task) {
    if (!isAttributeSet(task, propNameNegligibleWorkload)) {
      return true;
    }
    return (boolean) getAttribute(task, propNameNegligibleWorkload);
  }

  /**
   * Annotates that the workload of the given task is not negligible
   * 
   * @param task the given task
   */
  public static void annotateNonNegligibleWorkload(final Task task) {
    task.setAttribute(propNameNegligibleWorkload, false);
  }

  /**
   * Resets the output for the given task.
   * 
   * @param task the given task
   */
  public static void resetOutput(final Task task) {
    checkTask(task);
    task.setAttribute(propNameOutput, null);
  }

  /**
   * Returns the output stored for the given task.
   * 
   * @param task the given task
   * @return the output stored for the given task
   */
  public static JsonObject getOutput(final Task task) {
    checkTask(task);
    if (!isOutputSet(task)) {
      throw new IllegalStateException("Output of task " + task + " not set but requested.");
    }
    return (JsonObject) task.getAttribute(propNameOutput);
  }

  /**
   * Sets the given output for the given task.
   * 
   * @param task the given task
   * @param output the given output
   */
  public static void setOutput(final Task task, final JsonObject output) {
    checkTask(task);
    if (isOutputSet(task)) {
      throw new IllegalStateException("Output for task " + task.getId() + " is already set.");
    }
    task.setAttribute(propNameOutput, output);
  }

  /**
   * Returns true iff the output is set for the given function task
   * 
   * @param task the given function task
   * @return true iff the output is set for the given function task
   */
  public static boolean isOutputSet(final Task task) {
    checkTask(task);
    return task.getAttribute(propNameOutput) != null;
  }

  /**
   * Sets the key content for a given task node.
   * 
   * @param task the task node
   * @param content the content to set
   */
  public static void setInput(final Task task, final JsonObject content) {
    checkTask(task);
    if (isInputSet(task)) {
      throw new IllegalStateException("Input of task " + task + " already set.");
    }
    task.setAttribute(propNameInput, content);
  }

  /**
   * Returns the input annotated at the given function task.
   * 
   * @param task the given function task
   * @return the input annotated at the given function task
   */
  public static JsonObject getInput(final Task task) {
    checkTask(task);
    if (!isInputSet(task)) {
      throw new IllegalStateException("Input of task " + task + " not set but requested.");
    }
    return (JsonObject) task.getAttribute(propNameInput);
  }

  /**
   * Resets the function input.
   * 
   * @param task the function
   */
  public static void resetInput(final Task task) {
    checkTask(task);
    task.setAttribute(propNameInput, null);
  }

  /**
   * Returns true if at least one of the keys of the input is set.
   * 
   * @param task the task to check
   * @return true if at least one of the keys of the input is set.
   */
  public static boolean isInputSet(final Task task) {
    checkTask(task);
    return task.getAttribute(propNameInput) != null;
  }

  /**
   * Sets the function type for the provided function.
   * 
   * @param functionType the type to set
   * @param task the task to annotate
   */
  public static void setUsageType(final UsageType functionType, final Task task) {
    checkTask(task);
    task.setAttribute(propNameUsageType, functionType.name());
  }

  /**
   * Returns the function type of the given task.
   * 
   * @param task the given task
   * @return the function type of the given task
   */
  public static UsageType getUsageType(final Task task) {
    checkTask(task);
    return UsageType.valueOf((String) getAttribute(task, propNameUsageType));
  }

  /**
   * Checks that we are given a function task; Throws an exception if not.
   * 
   * @param task the task to check
   */
  static void checkTask(final Task task) {
    if (!TaskPropertyService.isProcess(task)) {
      throw new IllegalArgumentException("Node " + task.getId() + " is not a function node.");
    }
  }
}
