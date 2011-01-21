(ns hackamore.processor
  (:import [org.apache.camel Processor]))

(defn processor
  [p]
  (proxy [Processor] [] (process [e] (p e))))
