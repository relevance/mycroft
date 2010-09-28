(ns mycroft.reflect
  (:import [java.lang.reflect Modifier])
  (:require [clojure.string :as str])
  (:use [clojure.pprint :only (pprint)]))

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

(defn attribute-predicate
  [sym]
  `(defn ~(symbol (str sym "?"))
     ~(str "Does the :attributes of o include :" sym " ?")
     [~'o]
     ((:attributes ~'o) ~(keyword sym))))

(defmacro attribute-predicates
  [& attributes]
  `(do
     ~@(map
        attribute-predicate
        attributes)))

(attribute-predicates abstract final interface native private
                     protected public static strict synchronized
                     transient volatile)

(defprotocol ReplFormat
  (format-member [o] "Helper method used by describe when printing reflective descriptions."))

(extend-protocol ReplFormat
  Object
  (format-member [o] o)

  Class
  (format-member [c]
               (if (.isArray c)
                 (str (format-member (.getComponentType c)) "[]")
                 (.getName c))))

(defn param-str
  [m]
  (str "(" (str/join ", " (map format-member (:parameter-types m))) ")"))

(defrecord Constructor
  [name declaring-class parameter-types exceptions attributes]
  ReplFormat
  (format-member [c]
               (str "<init> " (param-str c))))

(defn constructor?
  "Is x an instance of mycroft.reflect/Constructor?"
  [o]
  (instance? Constructor o))

(defn constructor->map
  [^java.lang.reflect.Constructor constructor]
  (Constructor.
   (symbol (.getName constructor))
   (.getDeclaringClass constructor)
   (vec (.getParameterTypes constructor))
   (vec (.getExceptionTypes constructor))
   (modifiers->set (.getModifiers constructor))))

(defn declared-constructors
  "Return a set of the declared constructors of class as a Clojure map."
  [^Class cls]
  (set (map
        constructor->map
        (.getDeclaredConstructors cls))))

(defrecord Method
  [name return-type declaring-class parameter-types exception-types attributes]
  ReplFormat
  (format-member [c]
               (str (format-member (:return-type c)) " " (:name c) (param-str c))))

(defn method?
  "Is x an instance of mycroft.reflect/Method?"
  [x]
  (instance? Method x))

(defn method->map
  [^java.lang.reflect.Method method]
  (Method.
   (symbol (.getName method))
   (.getReturnType method)
   (.getDeclaringClass method)
   (vec (.getParameterTypes method))
   (vec (.getExceptionTypes method))
   (modifiers->set (.getModifiers method))))

(defn declared-methods
  "Return a set of the declared constructors of class as a Clojure map."
  [^Class cls]
  (set (map
        method->map
        (.getDeclaredMethods cls))))

(defrecord Field
  [name type declaring-class attributes]
  ReplFormat
  (format-member [c]
               (str (format-member (:type c)) " " (:name c))))

(defn field?
  "Is x an instance of mycroft.reflect/Field?"
  [x]
  (instance? Field x))

(defn field->map
  [^java.lang.reflect.Field field]
  (Field.
   (symbol (.getName field))
   (.getType field)
   (.getDeclaringClass field)
   (modifiers->set (.getModifiers field))))

(defn attributes
  [cls]
  (into #{}
        (remove nil?
                [(when (.isAnnotation cls) :annotation)
                 (when (.isAnonymousClass cls) :anonymous)
                 (when (.isArray cls) :array)
                 (when (.isEnum cls) :enum)
                 (when (.isInterface cls) :interface)
                 (when (.isLocalClass cls) :local)
                 (when (.isMemberClass cls) :member)
                 (when (.isPrimitive cls) :primitive)
                 (when (.isSynthetic cls) :synthetic)])))

(defn declared-fields
  "Return a set of the declared fields of class as a Clojure map."
  [^Class cls]
  (set (map
        field->map
        (.getDeclaredFields cls))))

(defn reflect
  "Low-level method called by members and describe.

   Reflect over o, returning a map containing information about its
   supers, fields, methods, and constructors. Includes all members
   all the way back up the inheritance hierarchy, so you can filter
   to get the parts you want."
  [o]
  (when o
    (if (class? o)
      (let [supers (supers o)
            classes (conj supers o)]
        {:supers supers
         :attributes (attributes o)
         :fields (into #{} (mapcat declared-fields classes))
         :methods (into #{} (mapcat declared-methods classes))
         :constructors (into #{} (mapcat declared-constructors classes))})
      (reflect (class o)))))

(defn exclude
  "Create a predicate that matches only items whose :declaring-class
   is *not* in the set specified by classes, or any superclasses of
   classes."
  [& classes]
  (let [excluded-class? (into (set classes) (mapcat supers classes))]
    #(not (excluded-class? (:declaring-class %)))))

(defn only
  "Create a predicate that matches only items whose :declaring-class
   is in the set specified by classes."
  [& classes]
  (let [included-class? (set classes)]
    #(included-class? (:declaring-class %))))

(defn returns
  "Create a predicate that matches items whose :return-type is type."
  [type]
  #(= type (:return-type %)))

(defn members
  "Returns all members (fields, methods, constructors) of o,
   sorted by name.

   Filters are predicates that are used to limit the results
   returned. Common filters include:

   (only Foo Bar)       include only results from classes
                        Foo and Bar
   (exclude Foo Bar)    exclude results from classes Foo and
                        Bar, *and* all their supers.
   (returns String)     include only methods that return
                        String
   private?             include only private methods
   public?              include only public methods?

   There are predicates for all the java Modifiers:
             abstract final interface native private
             protected public static strict synchronized
             transient volatile
   Examples:

   (members 123)
   (members String (returns String))
   (members foo private? (only Foo))"
  [o & filters]
  (let [reflection (reflect o)]
    (->> (mapcat reflection [:fields :methods :constructors])
         (clojure.core/filter (if filters
                                #(every? (fn [filter-fn] (filter-fn %)) filters)
                                identity))
         (sort-by :name))))

(defmacro describe
  "Print a description of o, via reflection. See members for
   a description of common filters."
  [o & filters]
  `(let [o# ~o
         c# (if (class? o#) o# (class o#))
         sep# "============================================="]
     (println sep#)
     (println (str c#))
     (println "Filters: " '~filters)
     (println sep#)
     (let [ms# (members o# ~@filters)]
       (if (seq ms#)
         (doseq [m# ms#]
           (println (str/join " "
                              [(str/join " " (map name (:attributes m#)))
                               (format-member m#)])))
         (println "No matches.")))
     (println sep#)))
