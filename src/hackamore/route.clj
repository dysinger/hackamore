(ns hackamore.route
  (:import [org.apache.camel.builder RouteBuilder]))

(defmacro route
  [c & r]
  `(.addRoutes ~c (proxy [RouteBuilder] [] (configure [] (.. ~'this ~@r)))))
