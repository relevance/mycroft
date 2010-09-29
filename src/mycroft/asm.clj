(ns mycroft.asm
  (:require [clojure.string :as str])
  (:import [clojure.asm ClassReader ClassVisitor Type]
           [java.lang.reflect Modifier]))

(defprotocol AsmHelper
  (classname [o]))

(extend-protocol AsmHelper
  Class
  (classname
   [c]
   (-> (.getName c)
       (symbol)))
  
  Type
  (classname
   [t]
   (-> (.getClassName t)
       (symbol))))

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

(defrecord Constructor
  [name declaring-class parameter-types exceptions attributes])

(defn constructor?
  "Is x an instance of mycroft.reflect/Constructor?"
  [o]
  (instance? Constructor o))

(defn constructor->map
  [^java.lang.reflect.Constructor constructor]
  (Constructor.
   (symbol (.getName constructor))
   (classname (.getDeclaringClass constructor))
   (vec (map classname (.getParameterTypes constructor)))
   (vec (map classname (.getExceptionTypes constructor)))
   (modifiers->set (.getModifiers constructor))))

(defn declared-constructors
  "Return a set of the declared constructors of class as a Clojure map."
  [^Class cls]
  (set (map
        constructor->map
        (.getDeclaredConstructors cls))))

(defrecord Method
  [name return-type declaring-class parameter-types exception-types attributes])

(defn method?
  "Is x an instance of mycroft.reflect/Method?"
  [x]
  (instance? Method x))

(defn method->map
  [^java.lang.reflect.Method method]
  (Method.
   (symbol (.getName method))
   (classname (.getReturnType method))
   (classname (.getDeclaringClass method))
   (vec (map classname (.getParameterTypes method)))
   (vec (map classname (.getExceptionTypes method)))
   (modifiers->set (.getModifiers method))))

(defn declared-methods
  "Return a set of the declared constructors of class as a Clojure map."
  [^Class cls]
  (set (map
        method->map
        (.getDeclaredMethods cls))))

(defrecord Field
  [name type declaring-class attributes])

(defn field?
  "Is x an instance of mycroft.reflect/Field?"
  [x]
  (instance? Field x))

(defn field->map
  [^java.lang.reflect.Field field]
  (Field.
   (symbol (.getName field))
   (map classname (.getType field))
   (map classname (.getDeclaringClass field))
   (modifiers->set (.getModifiers field))))

(defn declared-fields
  "Return a set of the declared fields of class as a Clojure map."
  [^Class cls]
  (set (map
        field->map
        (.getDeclaredFields cls))))

(def template {:bases #{} :attributes #{} :fields #{} :methods #{} :constructors #{}})

(defn java-reflect
  [classname]
  (let [cls (Class/forName (str classname))] ;; TODO use context version
    {:bases (set (bases cls))
     :attributes (attributes cls)
     :fields (declared-fields cls)
     :methods (declared-methods cls)
     :constructors (declared-constructors cls)}))

(defn classname->filename
  [classname]
  (-> (str classname)
      (str/replace "." "/")
      (str ".class")))

(defn descriptor->classname
  [d]
  (classname (Type/getType d)))

(def add-to-set (fnil conj #{}))

(defn parse-method-descriptor
  [md]
  {:parameter-types (vec (map classname (Type/getArgumentTypes md)))
   :return-type (classname (Type/getReturnType md))})

(defn asm-reflect
  "Uses context class loader to find class, but does not load it."
  [classname]
  (let [is (.. (Thread/currentThread)
              getContextClassLoader
              (getResourceAsStream (classname->filename classname)))
        r (ClassReader. is)
        result (atom template)]
    (.accept
     r
     (reify
      ClassVisitor
      (visit [_ version access name signature superName interfaces]
             (swap! result assoc :bases (set (map symbol interfaces))))
      (visitSource [_ name debug])
      (visitInnerClass [_ name outerName innerName access])
      (visitField [_ access name desc signature value]
                  (swap! result update-in [:fields] add-to-set
                         (Field. (symbol name)
                                 (descriptor->classname desc)
                                 classname
                                 (modifiers->set access)))
                  nil)
      (visitMethod [_ access name desc signature exceptions]
                   (swap! result update-in [:methods] add-to-set
                          (let [{:keys [parameter-types return-type]}
                                (parse-method-descriptor desc)]
                            (if (= name "<init>")
                              (Constructor. (symbol name)
                                            classname
                                            parameter-types
                                            nil
                                            (modifiers->set access))
                              (Method. (symbol name)
                                       return-type
                                       classname
                                       parameter-types
                                       nil
                                       (modifiers->set access)))))
                   nil)
      (visitEnd [_])
      ) 0)
    @result))
