(ns mycroft.reflect
  (:import java.lang.reflect.Modifier))

(defn modifiers->set
  [mod]
  (set (remove nil?
               [(when (Modifier/isAbstract mod) :abstract)
                (when (Modifier/isFinal mod) :final)
                (when (Modifier/isInterface mod) :interface)
                (when (Modifier/isNative mod) :native)
                (when (Modifier/isPrivate mod) :private)
                (when (Modifier/isProtected mod) :protected)
                (when (Modifier/isPublic mod) :public)
                (when (Modifier/isStatic mod) :static)
                (when (Modifier/isStrict mod) :strict)
                (when (Modifier/isSynchronized mod) :synchronized)
                (when (Modifier/isTransient mod) :transient)
                (when (Modifier/isVolatile mod) :volatile)])))

(defn method->map
  [method]
  {:name (.getName method)
   :return-type (.getReturnType method)
   :declaring-class (.getDeclaringClass method)
   :parameter-types (vec (.getParameterTypes method))
   :exception-types (vec (.getExceptionTypes method))
   :modifiers (modifiers->set (.getModifiers method))})

(defn methods-set
  [cls]
  (set (map
        method->map
        (.getMethods cls))))

(defn field->map
  [field]
  {:name (.getName field)
   :type (.getType field)
   :declaring-class (.getDeclaringClass field)
   :modifiers (modifiers->set (.getModifiers field))})

(defn fields-set
  [cls]
  (set (map
        field->map
        (.getFields cls))))


(defn reflect
  [cls]
  (if (class? cls)
    {:fields (fields-set cls)
     :methods (methods-set cls)}
    (reflect (class cls))))

