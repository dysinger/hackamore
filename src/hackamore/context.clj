(ns hackamore.context
  (:import [org.apache.camel.impl DefaultCamelContext]))

(defn context
  []
  (DefaultCamelContext.))

(defn start
  [c]
  (.start c))

(defn block
  []
  (.join (Thread/currentThread)))

(defn stop
  [c]
  (.stop c))
