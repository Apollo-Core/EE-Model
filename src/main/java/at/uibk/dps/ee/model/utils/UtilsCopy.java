package at.uibk.dps.ee.model.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentSpecification;
import at.uibk.dps.ee.model.graph.MappingsConcurrent;
import at.uibk.dps.ee.model.graph.ResourceGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Element;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * Static container for convenience methods to generate deep copies (no relation
 * between original and copy!) of specifications and its components.
 * 
 * @author Fedor Smirnov
 */
public final class UtilsCopy {

  /**
   * No constructor
   */
  private UtilsCopy() {}

  /**
   * Restores the original state of an adjusted specification by copying the
   * attribute values from the original specification.
   * 
   * @param originalSpec the original specification
   * @param adjustedSpec the adjusted spec
   */
  public static void restoreSpecAttributes(final EnactmentSpecification originalSpec,
      final EnactmentSpecification adjustedSpec) {
    restoreEGraphAttributes(originalSpec.getEnactmentGraph(), adjustedSpec.getEnactmentGraph());
    restoreRGraphAttributes(originalSpec.getResourceGraph(), adjustedSpec.getResourceGraph());
    restoreMappingsAttributes(originalSpec.getMappings(), adjustedSpec.getMappings());
  }

  /**
   * Restores all attributes in the given adjusted enactment graph
   * 
   * @param original the original enactment graph
   * @param adjusted the adjusted enactment graph
   */
  static void restoreEGraphAttributes(final EnactmentGraph original,
      final EnactmentGraph adjusted) {
    original.getVertices().forEach(originalVertex -> {
      final Task adjustedVertex = adjusted.getVertex(originalVertex.getId());
      if (adjustedVertex == null) {
        throw new IllegalStateException(
            "Task " + originalVertex.getId() + " not present in the adjusted graph.");
      }
      restoreElementAttributes(originalVertex, adjustedVertex);
    });
    original.getEdges().forEach(originalEdge -> restoreElementAttributes(originalEdge,
        adjusted.getEdge(originalEdge.getId())));
  }

  /**
   * Restores all attributes in the given adjusted resource graph
   * 
   * @param original the original resource graph
   * @param adjusted the adjusted resource graph
   */
  static void restoreRGraphAttributes(final ResourceGraph original,
      final ResourceGraph adjusted) {
    original.getVertices().forEach(originalVertex -> restoreElementAttributes(originalVertex,
        adjusted.getVertex(originalVertex.getId())));
    original.getEdges().forEach(originalEdge -> restoreElementAttributes(originalEdge,
        adjusted.getEdge(originalEdge.getId())));
  }

  /**
   * Restores all attributes in the given adjusted mappings.
   * 
   * @param original the original mappings
   * @param adjusted the adjusted mappings
   */
  static void restoreMappingsAttributes(final MappingsConcurrent original,
      final MappingsConcurrent adjusted) {
    final Map<String, Mapping<Task, Resource>> originalMappingMap =
        original.mappingStream().collect(Collectors.toMap(oMap -> oMap.getId(), oMap -> oMap));
    final Map<String, Mapping<Task, Resource>> adjustedMappingMap =
        adjusted.mappingStream().collect(Collectors.toMap(aMap -> aMap.getId(), aMap -> aMap));
    originalMappingMap.keySet()
        .forEach(mappingKey -> restoreElementAttributes(originalMappingMap.get(mappingKey),
            adjustedMappingMap.get(mappingKey)));
  }

  /**
   * Restores the attributes of an adjusted element by setting it to the values
   * found in the original.
   * 
   * @param original the original element
   * @param adjusted the element with adjusted values
   */
  static void restoreElementAttributes(final Element original, final Element adjusted) {
    // all attributes which were not in the original are set to null
    adjusted.getAttributeNames().stream()
        .filter(attrName -> !original.getAttributeNames().contains(attrName))
        .forEach(notInOriginal -> adjusted.setAttribute(notInOriginal, null));
    // all other attributes are set to the same value as in the original
    original.getAttributeNames()
        .forEach(attrName -> adjusted.setAttribute(attrName, original.getAttribute(attrName)));
  }

  /**
   * Generates a deep copy of the provided spec (objects on all levels have
   * different references, but identical attributes and relations).
   * 
   * @param original the specification which is being copied
   * @param copySuffix the suffix added to the original id to create the copy id
   * @return the deep copy of the specification
   */
  public static EnactmentSpecification deepCopySpec(final EnactmentSpecification original,
      final String copySuffix) {
    final EnactmentGraph deepCopyEGraph = deepCopyEGraph(original.getEnactmentGraph());
    final ResourceGraph deepCopyRGraph = deepCopyRGraph(original.getResourceGraph());
    final MappingsConcurrent deepCopyMappings =
        deepCopyMappings(original.getMappings(), deepCopyEGraph, deepCopyRGraph);
    return new EnactmentSpecification(deepCopyEGraph, deepCopyRGraph, deepCopyMappings,
        original.getId() + copySuffix);
  }

  /**
   * Generates a deep copy of the provided enactment graph (objects on all levels
   * have different references, but identical attributes and relations).
   * 
   * @param original the enactment graph which is being copied
   * @return the deep copy of the enactment graph
   */
  public static EnactmentGraph deepCopyEGraph(final EnactmentGraph original) {
    final EnactmentGraph result = new EnactmentGraph();
    original.getVertices()
        .forEach(originalNode -> result.addVertex(deepCopyEGraphNode(originalNode)));
    original.getEdges()
        .forEach(originalEdge -> addDeepCopyDependency(originalEdge, original, result));
    return result;
  }

  /**
   * Generates a deep copy of the provided resource graph (objects on all levels
   * have different references, but identical attributes and relations).
   * 
   * @param original the resource graph which is being copied
   * @return the deep copy of the resource graph
   */
  public static ResourceGraph deepCopyRGraph(final ResourceGraph original) {
    final ResourceGraph result = new ResourceGraph();
    original.getVertices().forEach(originalRes -> result.addVertex(deepCopyResource(originalRes)));
    original.getEdges().forEach(originalLink -> addDeepCopyLink(originalLink, original, result));
    return result;
  }

  /**
   * Generates a deep copy of the provided mappings (objects on all levels have
   * different references, but identical attributes and relations).
   * 
   * @param original the original mappings
   * @param deepCopyEGraph a deep copy of the enactment graph
   * @param deepCopyRGraph a deep copy of the resource graph
   * @return a deep copy of the mappings
   */
  public static MappingsConcurrent deepCopyMappings(final MappingsConcurrent original,
      final EnactmentGraph deepCopyEGraph, final ResourceGraph deepCopyRGraph) {
    final MappingsConcurrent result = new MappingsConcurrent();
    original.forEach(originalMapping -> result
        .addMapping(deepCopyMapping(originalMapping, deepCopyEGraph, deepCopyRGraph)));
    return result;
  }

  /**
   * Method to create deep copies of a task or a communication
   * 
   * @param original the original egraph node
   * @return the deep copy
   */
  public static Task deepCopyEGraphNode(final Task original) {
    return TaskPropertyService.isProcess(original) ? deepCopyTask(original)
        : deepCopyCommunication(original);
  }

  /**
   * Creates a deep copy of a task.
   * 
   * @param original the original task
   * @return the deep copy of the task
   */
  public static Task deepCopyTask(final Task original) {
    return deepCopyElement(Task.class, original);
  }

  /**
   * Creates a deep copy of a communication
   * 
   * @param original the original communication
   * @return the deep copy of the communication
   */
  public static Communication deepCopyCommunication(final Task original) {
    return deepCopyElement(Communication.class, original);
  }

  /**
   * Creates a deep copy of the given resource.
   * 
   * @param original the original resource
   * @return a deep copy of the original resource
   */
  public static Resource deepCopyResource(final Resource original) {
    return deepCopyElement(Resource.class, original);
  }

  /**
   * Make a deep copy the provided element of the provided type.
   * 
   * @param <E> the type of the processed element
   * @param clazz the provided type
   * @param original the original element (of type E)
   * @return a deep copy the provided element of the provided type
   */
  static <E extends Element> E deepCopyElement(final Class<E> clazz,
      final Element original) {
    final String elementId = original.getId();
    try {
      final E result = clazz.getDeclaredConstructor(String.class).newInstance(elementId);
      copyAttributes(original, result);
      return result;
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new IllegalArgumentException(
          "Error when trying to construct an element of type " + clazz.getCanonicalName(), e);
    }
  }

  /**
   * Copies all attributes of a given original to a copy.
   * 
   * @param original the given original
   * @param copy the given copy
   */
  public static void copyAttributes(final Element original, final Element copy) {
    if (!original.getClass().equals(copy.getClass())) {
      throw new IllegalArgumentException("Element " + original.getId() + " and element "
          + copy.getId() + " are not of the same class.");
    }
    original.getAttributeNames()
        .forEach(attrName -> copy.setAttribute(attrName, original.getAttribute(attrName)));
  }

  /**
   * Creates a deep copy of the given original mapping.
   * 
   * @param original the original mapping
   * @param copyEGraph copy of the enactment graph
   * @param copyRGraph copy of the resource graph
   * @return deep copy of the given mapping
   */
  public static Mapping<Task, Resource> deepCopyMapping(final Mapping<Task, Resource> original,
      final EnactmentGraph copyEGraph, final ResourceGraph copyRGraph) {
    final Task taskCopy = Optional.ofNullable(copyEGraph.getVertex(original.getSource().getId()))
        .orElseThrow(() -> new IllegalStateException(
            "Src of mapping " + original.getId() + " not in the copied e graph."));
    final Resource resCopy = Optional.ofNullable(copyRGraph.getVertex(original.getTarget().getId()))
        .orElseThrow(() -> new IllegalStateException(
            "Target of mapping " + original.getId() + " not in the copied r graph."));
    final Mapping<Task, Resource> result = new Mapping<>(original.getId(), taskCopy, resCopy);
    original.getAttributeNames()
        .forEach(attrName -> result.setAttribute(attrName, original.getAttribute(attrName)));
    return result;
  }

  /**
   * Returns a deep copy of the given dependency.
   * 
   * @param original the given dependency
   * @return a deep copy of the given dependency
   */
  public static Dependency deepCopyDependency(final Dependency original) {
    return deepCopyElement(Dependency.class, original);
  }

  /**
   * Creates a deep copy of the given dependency and adds it at the appropriate
   * position of the copied enactment graph.
   * 
   * @param original the original dependency
   * @param originalGraph the original egraph
   * @param copyEGraph the copied egraph
   * @return the deep copy of the dependency (added to the copy graph by this
   *         method)
   */
  public static Dependency addDeepCopyDependency(final Dependency original,
      final EnactmentGraph originalGraph, final EnactmentGraph copyEGraph) {
    final Dependency result = deepCopyDependency(original);
    final Task srcTask =
        Optional.ofNullable(copyEGraph.getVertex(originalGraph.getSource(original).getId()))
            .orElseThrow(() -> new IllegalStateException(
                "Src of edge " + original + " not in the copied e graph"));
    final Task dstTask =
        Optional.ofNullable(copyEGraph.getVertex(originalGraph.getDest(original).getId()))
            .orElseThrow(() -> new IllegalStateException(
                "Dst of edge " + original + " not in the copied e graph"));
    copyEGraph.addEdge(result, srcTask, dstTask, EdgeType.DIRECTED);
    return result;
  }

  /**
   * Creates a deep copy of the given dependency and adds it at the appropriate
   * position of the copied resource graph.
   * 
   * @param original the original link
   * @param originalGraph the original resource graph
   * @param copyGraph the copied resource graph
   * @return the deep copy of the link (added to the copy graph by this method)
   */
  public static Link addDeepCopyLink(final Link original, final ResourceGraph originalGraph,
      final ResourceGraph copyGraph) {
    final Link result = deepCopyElement(Link.class, original);
    final Resource oSrc = originalGraph.getSource(result);
    final Resource oDst = originalGraph.getDest(result);
    if (!copyGraph.containsVertex(oSrc.getId()) || !copyGraph.containsVertex(oDst.getId())) {
      throw new IllegalStateException("One of the endpoints of copied link not in the copy graph");
    }
    final Resource cSrc = copyGraph.getVertex(oSrc.getId());
    final Resource cDst = copyGraph.getVertex(oDst.getId());
    copyGraph.addEdge(result, cSrc, cDst, EdgeType.UNDIRECTED);
    return result;
  }
}
