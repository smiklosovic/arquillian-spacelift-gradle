package org.arquillian.spacelift.gradle

import org.gradle.util.Configurable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Object container that allows additional behaviors to be used for DSL definition.
 *
 * Mainly, this container supports inheritance of existing DSL elements
 *
 * @author kpiwko
 *
 * @param < T >
 */
class InheritanceAwareContainer<TYPE extends ContainerizableObject<TYPE>, DEFAULT_TYPE extends TYPE> implements Iterable<TYPE>, Configurable<TYPE>, Collection<TYPE>, Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceAwareContainer)

    Class<TYPE> type

    Class<DEFAULT_TYPE> defaultType

    Set<TYPE> objects

    Object parent

    InheritanceAwareContainer(Object parent, Class<TYPE> type, Class<DEFAULT_TYPE> defaultType) {
        this.type = type
        this.defaultType = defaultType
        this.parent = parent
        this.objects = new LinkedHashSet<TYPE>()
    }

    InheritanceAwareContainer(InheritanceAwareContainer<TYPE, DEFAULT_TYPE> other) {
        this.type = other.type
        this.parent = other.parent
        this.objects = new LinkedHashSet<TYPE>(other.objects)
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        new InheritanceAwareContainer<TYPE, DEFAULT_TYPE>(this)
    }

    /**
     * This method dynamically creates an object of type <TYPE>.
     * Object must define a constructor of type (name, parent)
     *
     * @param name name of object to be created
     * @param args
     * @return object of type <TYPE>
     */
    def methodMissing(String name, args) {
        Map behavior = DeferredValue.getBehaviors(args)
        DeferredValue<Void> configurator = DeferredValue.of(Void.class).ownedBy(this).from(args)
        create(name, behavior, configurator)
    }

    TYPE create(String name, Map behavior, DeferredValue configurator) {

        logger.debug("Creating ${type.simpleName} ${name} with behavior ${behavior}")

        ContainerizableObject<?> object

        // if from behavior it is not specified, defaults to default type for container
        // @Deprecated inherits - inherits to be deprecated and replaced with from
        // implementation note - not using map.get('key', defaultValue) as this adds
        // to the map
        Object from = behavior['from']
        if (from == null) {
            from = behavior['inherits']
        }
        if (from == null) {
            from = defaultType
        }

        if (from instanceof Class && type.isAssignableFrom(from)) {
            logger.debug("${name} will be instantiated from ${from.simpleName} class")
            object = from.newInstance(name, parent)
        } else {
            Object ancestor = resolveAncestor(from)
            logger.debug("${name} will be inherited from ${parent} reference")
            object = ancestor.clone(name)
        }

        // instrument and configure and store object
        DSLInstrumenter.instrument(object)
        configurator.apply(object)
        objects << object

        object
    }

    @Override
    TYPE configure(Closure configuration) {
        DeferredValue<Void> config = DeferredValue.of(Void.class).ownedBy(parent).from(configuration)
        config.resolveWith(this)
    }

    @Override
    Iterator<TYPE> iterator() {
        objects.iterator();
    }

    /**
     * Allows to use subscript operator on container
     *
     * @param name
     * @return
     */
    public TYPE getAt(String name) {
        TYPE element = null
        List<String> names = new ArrayList<String>()
        for (TYPE o : objects) {
            names.add(o.name)
            if (o.name == name) {
                return o
            }
        }
        throw new MissingPropertyException("Unable to get ${type.getSimpleName()} ${name}, have you meant one of ${names.join(', ')}?")
    }

    @Override
    boolean add(TYPE object) {
        objects.add(object)
    }

    @Override
    boolean addAll(Collection<? extends TYPE> others) {
        objects.addAll(others)
    }

    @Override
    void clear() {
        objects.clear()
    }

    @Override
    boolean contains(Object key) {
        objects.contains(key)
    }

    @Override
    boolean containsAll(Collection<?> others) {
        objects.containsAll(others)
    }

    @Override
    boolean isEmpty() {
        objects.isEmpty()
    }

    @Override
    boolean remove(Object arg0) {
        objects.remove(arg0)
    }

    @Override
    boolean removeAll(Collection<?> others) {
        objects.removeAll(others)
    }

    @Override
    boolean retainAll(Collection<?> others) {
        objects.retainAll(others);
    }

    @Override
    int size() {
        objects.size()
    }

    @Override
    Object[] toArray() {
        objects.toArray()
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        objects.toArray(a)
    }

    @Override
    String toString() {
        "Container(${type.simpleName}): ${objects.toArray()}"
    }

    /**
     * Resolves reference by either string. If it gets direct reference, checks whether type is compatible
     * @param reference reference to be resolved
     * @return resolved reference
     */
    private TYPE resolveAncestor(Object reference) {

        if (reference instanceof CharSequence) {
            getAt(reference.toString())
        } else if (reference && type.isAssignableFrom(reference.getClass())) {
            (TYPE) reference
        } else if (reference && !type.isAssignableFrom(reference.getClass())) {
            throw new MissingPropertyException("Reference ${reference} of type ${reference.getClass().simpleName} is not compatible with type ${type.getSimpleName()}")
        } else {
            throw new MissingPropertyException("Reference ${reference} of type${type.getSimpleName()} was not found.")
        }
    }
}
