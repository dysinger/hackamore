(ns hackamore.core
  (:import [org.apache.camel Expression Predicate Processor]
           [org.apache.camel.builder RouteBuilder]
           [org.apache.camel.impl DefaultCamelContext]))

(defn context
  []
  (DefaultCamelContext.))

(defmacro route
  [& body]
  `(proxy [RouteBuilder] []
     (configure [] (-> ~'this ~@body))))

(defn pred
  [f]
  (reify Predicate
    (matches [this exchange] (f exchange))))

(defn expr
  [f]
  (reify Expression
    (evaluate [this exchange _] (f exchange))))

(defn proc
  [f]
  (reify Processor
    (process [this exchange] (f exchange))))
