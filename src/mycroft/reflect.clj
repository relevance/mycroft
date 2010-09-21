(ns mycroft.reflect
  (:import [java.lang.reflect Field Constructor Method Modifier]))

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

(defn constructor->map
  [^Constructor constructor]
  {:name (.getName constructor)
   :declaring-class (.getDeclaringClass constructor)
   :parameter-types (vec (.getParameterTypes constructor))
   :exception-types (vec (.getExceptionTypes constructor))
   :modifiers (modifiers->set (.getModifiers constructor))})

(defn constructors-set
  [^Class cls]
  (set (map
        constructor->map
        (.getDeclaredConstructors cls))))

(defn method->map
  [^Method method]
  {:name (.getName method)
   :return-type (.getReturnType method)
   :declaring-class (.getDeclaringClass method)
   :parameter-types (vec (.getParameterTypes method))
   :exception-types (vec (.getExceptionTypes method))
   :modifiers (modifiers->set (.getModifiers method))})

(defn methods-set
  [^Class cls]
  (set (map
        method->map
        (.getDeclaredMethods cls))))

(defn field->map
  [^Field field]
  {:name (.getName field)
   :type (.getType field)
   :declaring-class (.getDeclaringClass field)
   :modifiers (modifiers->set (.getModifiers field))})

(defn fields-set
  [^Class cls]
  (set (map
        field->map
        (.getDeclaredFields cls))))


(defn reflect
  [cls]
  (when cls
    (if (class? cls)
      {:fields (fields-set cls)
       :methods (methods-set cls)
       :constructors (constructors-set cls)}
      (reflect (class cls)))))

