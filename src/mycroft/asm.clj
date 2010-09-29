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

(defn access-flag
  [[name flag & contexts]]
  {:name name :flag flag :contexts (set (map keyword contexts))})

(def flag-descriptors
  (vec
   (map access-flag
        [[:ACC_PUBLIC 0x0001 :class :field ::method]
         [:ACC_PRIVATE 0x002 :class :field ::method]
         [:ACC_PRIVATE 0x0002  :class :field :method]
         [:ACC_PROTECTED 0x0004  :class :field :method]
         [:ACC_STATIC 0x0008  :field :method]
         [:ACC_FINAL 0x0010  :class :field :method]
         [:ACC_SUPER 0x0020  :class]
         [:ACC_SYNCHRONIZED 0x0020  :method]
         [:ACC_VOLATILE 0x0040  :field]
         [:ACC_BRIDGE 0x0040  :method]
         [:ACC_VARARGS 0x0080  :method]
         [:ACC_TRANSIENT 0x0080  :field]
         [:ACC_NATIVE 0x0100  :method]
         [:ACC_INTERFACE 0x0200  :class]
         [:ACC_ABSTRACT 0x0400  :class :method]
         [:ACC_STRICT 0x0800  :method]
         [:ACC_SYNTHETIC 0x1000  :class :field :method]
         [:ACC_ANNOTATION 0x2000  :class]
         [:ACC_ENUM 0x4000  :class :field :inner]])))

(defn parse-flags
  [flags context]
  (reduce
   (fn [result fd]
     (if (and (get (:contexts fd) context)
              (not (zero? (bit-and flags (:flag fd)))))
       (conj result (:name fd))
       result))
   #{}
   flag-descriptors))

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
   (parse-flags (.getModifiers constructor) :method)))

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
   (parse-flags (.getModifiers method) :method)))

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
   (classname (.getType field))
   (classname (.getDeclaringClass field))
   (parse-flags (.getModifiers field) :field)))

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
     :attributes (parse-flags (.getModifiers cls) :class)
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
  {:pre [(string? d)]}
  (classname (Type/getType d)))

(defn internal-name->classname
  [d]
  {:pre [(string? d)]}
  (classname (Type/getObjectType d)))

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
             (swap! result merge {:bases (set (map symbol interfaces))
                                  :attributes (parse-flags access :class)}))
      (visitSource [_ name debug])
      (visitInnerClass [_ name outerName innerName access])
      (visitField [_ access name desc signature value]
                  (swap! result update-in [:fields] add-to-set
                         (Field. (symbol name)
                                 (descriptor->classname desc)
                                 classname
                                 (parse-flags access :field)))
                  nil)
      (visitMethod [_ access name desc signature exceptions]
                   (let [constructor? (= name "<init>")]
                     (swap! result update-in [(if constructor? :constructors :methods)] add-to-set
                            (let [{:keys [parameter-types return-type]} (parse-method-descriptor desc)
                                  attributes (parse-flags access :method)]
                              (if constructor?
                                (Constructor. classname
                                              classname
                                              parameter-types
                                              (vec (map internal-name->classname exceptions))
                                              attributes)
                                (Method. (symbol name)
                                         return-type
                                         classname
                                         parameter-types
                                         (vec (map internal-name->classname exceptions))
                                         attributes)))))
                   nil)
      (visitEnd [_])
      ) 0)
    @result))
