package at.uibk.dps.ee.model.properties;

import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.UsageType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUtility.UtilityType;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.AbstractPropertyService;

/**
 * Static method container managing the properties of the function nodes
 * modeling collection operations.
 * 
 * @author Fedor Smirnov
 */
public final class PropertyServiceFunctionUtilityCollections extends AbstractPropertyService {

  /**
   * No constructor.
   */
  private PropertyServiceFunctionUtilityCollections() {}

  /**
   * Different types of collection operations.
   * 
   * @author Fedor Smirnov
   */
  public enum CollectionOperation {
    ElementIndex, Block, Split, Replicate
  }

  /**
   * Properties of the element index function.
   * 
   * @author Fedor Smirnov
   */
  protected enum Property {
    /**
     * String describing the subcollection operation done by this node
     */
    SubCollectionString,
    /**
     * Type of collection operation
     */
    CollectionOperation
  }

  /**
   * Creates a function node modeling the element index operation (described by
   * the given subcollection string) applied to the data node with the given id.
   * 
   * @param dataId the given data id
   * @param subCollectionsString the string from the afcl file
   * @param collectionOperation the operation annotated at the corresponding task
   * @return a function node modeling the element index operation (described by
   *         the given subcollection) applied to the data node with the given id.
   */
  public static Task createCollectionOperation(final String dataId,
      final String subCollectionsString, final CollectionOperation collectionOperation) {
    final String taskId = dataId + ConstantsEEModel.KeywordSeparator1 + collectionOperation.name()
        + ConstantsEEModel.KeywordSeparator1 + subCollectionsString;
    final Task result = new Task(taskId);
    PropertyServiceFunction.setUsageType(UsageType.Utility, result);
    PropertyServiceFunctionUtility.setUtilityType(result, UtilityType.CollectionOperation);
    setSubCollectionsString(result, subCollectionsString);
    setCollectionOperation(result, collectionOperation);
    return result;
  }

  /**
   * Returns the collection operation of the provided task.
   * 
   * @param task the provided task
   * @return the collection operation of the provided task
   */
  public static CollectionOperation getCollectionOperation(final Task task) {
    checkTask(task);
    final String attrName = Property.CollectionOperation.name();
    return CollectionOperation.valueOf((String) getAttribute(task, attrName));
  }

  /**
   * Sets the collection operation for the given task.
   * 
   * @param task the given task
   * @param operation the operation to set
   */
  static void setCollectionOperation(final Task task, final CollectionOperation operation) {
    checkTask(task);
    final String attrName = Property.CollectionOperation.name();
    task.setAttribute(attrName, operation.name());
  }

  /**
   * Annotates the given subcollections onto the given node.
   * 
   * @param task the given task
   * @param subCollectionsString the subcollection string to annotate
   */
  static void setSubCollectionsString(final Task task, final String subCollectionsString) {
    checkTask(task);
    final String attrName = Property.SubCollectionString.name();
    task.setAttribute(attrName, subCollectionsString);
  }

  /**
   * Returns the subCollections annotated on the given node
   * 
   * @param task the given node
   * @return the subCollections annotated on the given node
   */
  public static String getSubCollectionsString(final Task task) {
    checkTask(task);
    final String attrName = Property.SubCollectionString.name();
    return (String) getAttribute(task, attrName);
  }

  /**
   * Checks whether the task is an element index task. Throws exception otherwise.
   * 
   * @param task the given task
   */
  static void checkTask(final Task task) {
    PropertyServiceFunctionUtility.checkTask(task);
    if (!PropertyServiceFunctionUtility.getUtilityType(task)
        .equals(UtilityType.CollectionOperation)) {
      throw new IllegalArgumentException(
          "The task " + task.getId() + " is not an element index task.");
    }
  }
}
