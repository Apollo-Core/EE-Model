package at.uibk.dps.ee.model.properties;

import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlow.DataFlowType;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.AbstractPropertyService;

/**
 * Static method container for methods used to control the properties of the
 * function nodes modeling the distribution and aggregation of collection data.
 * 
 * @author Fedor Smirnov
 */
public final class PropertyServiceFunctionDataFlowCollections extends AbstractPropertyService {

	/**
	 * No constructor.
	 */
	private PropertyServiceFunctionDataFlowCollections() {
	}

	/**
	 * Properties defining attribute names.
	 * 
	 * @author Fedor Smirnov
	 */
	protected enum Property {
		/**
		 * Type of the operation.
		 */
		OperationType,
		/**
		 * The compound defining the scope between the distribution and the aggregation.
		 */
		Scope
	}

	/**
	 * Type of the data flow operation
	 * 
	 * @author Fedor Smirnov
	 *
	 */
	public enum OperationType {
		Distribution, Aggregation
	}

	/**
	 * Creates a task with the given ID, modeling the requested collection data flow
	 * operation.
	 * 
	 * @param taskId        the given task id
	 * @param operationType the requested operation
	 * @return a task with the given ID, modeling the requested collection data flow
	 *         operation
	 */
	public static Task createCollectionDataFlowTask(String taskId, OperationType operationType, String scope) {
		Task result = PropertyServiceFunctionDataFlow.createDataFlowFunction(taskId, DataFlowType.Collections);
		setOperationType(result, operationType);
		setScope(result, scope);
		return result;
	}

	/**
	 * Sets the given operation type for the givent task.
	 * 
	 * @param task          the given task
	 * @param operationType the given operation type
	 */
	protected static void setOperationType(Task task, OperationType operationType) {
		checkTask(task);
		String attrName = Property.OperationType.name();
		task.setAttribute(attrName, operationType.name());
	}

	/**
	 * Returns the operation type for the given task.
	 * 
	 * @param task the given task
	 * @return the operation type of the given task
	 */
	public static OperationType getOperationType(Task task) {
		checkTask(task);
		String attrName = Property.OperationType.name();
		return OperationType.valueOf((String) getAttribute(task, attrName));
	}

	/**
	 * Returns the scope for the given node.
	 * 
	 * @param task the given task
	 * @return the scope string
	 */
	public static String getScope(Task task) {
		checkTask(task);
		String attrName = Property.Scope.name();
		return (String) getAttribute(task, attrName);
	}

	/**
	 * Sets the scope for the given task.
	 * 
	 * @param task  the given task
	 * @param scope the scope
	 */
	protected static void setScope(Task task, String scope) {
		checkTask(task);
		String attrName = Property.Scope.name();
		task.setAttribute(attrName, scope);
	}

	/**
	 * Checks that the given task is a collection operation.
	 * 
	 * @param task the given task
	 */
	protected static void checkTask(Task task) {
		PropertyServiceFunctionDataFlow.checkTask(task);
		if (!PropertyServiceFunctionDataFlow.getDataFlowType(task).equals(DataFlowType.Collections)) {
			throw new IllegalArgumentException(
					"Task " + task.getId() + " does not model a collection data flow operation.");
		}
	}
}