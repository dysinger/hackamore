(ns hackamore.core
  (:import [org.apache.camel Exchange Expression Message Predicate Processor]
           [org.apache.camel.builder RouteBuilder]
           [org.apache.camel.impl DefaultCamelContext ExpressionAdapter]))

(defn context
  []
  (DefaultCamelContext.))

(defmacro route
  [& body]
  `(proxy [RouteBuilder] []
     (configure [] (-> ~'this ~@body))))

(defn pred
  [f]
  (proxy [Predicate] []
    (matches [ex] (f ex))))

(defn expr
  [f]
  (proxy [ExpressionAdapter] []
    (evaluate [ex _] (f ex))))

(defn proc
  [f]
  (proxy [Processor] []
    (process [ex] (f ex))))
