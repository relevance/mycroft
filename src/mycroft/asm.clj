(ns mycroft.asm
  (:require [clojure.string :as str])
  (:import [clojure.asm ClassReader ClassVisitor Type]
           [mycroft.reflect Field Method Constructor]))

(defprotocol AsmHelper
  (classname [o]))

(extend-protocol AsmHelper
  Type
  (classname
   [t]
   (-> (.getClassName t)
       (symbol))))

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

(defn reflect
  "Uses context class loader to find class, but does not load it."
  [classname]
  (let [is (.. (Thread/currentThread)
              getContextClassLoader
              (getResourceAsStream (classname->filename classname)))
        r (ClassReader. is)
        result (atom {})]
    (.accept
     r
     (reify
      ClassVisitor
      (visit [_ version access name signature superName interfaces]
             (swap! result assoc :supers (set (map symbol interfaces))))
      (visitSource [_ name debug])
      (visitInnerClass [_ name outerName innerName access])
      (visitField [_ access name desc signature value]
                  (swap! result update-in [:fields] add-to-set
                         (Field. (symbol name)
                                 (descriptor->classname desc)
                                 classname
                                 nil))
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
                                            nil)
                              (Method. (symbol name)
                                       return-type
                                       classname
                                       parameter-types
                                       nil
                                       nil))))
                   nil)
      (visitEnd [_])
      ) 0)
    @result))
