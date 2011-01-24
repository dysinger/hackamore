(ns hackamore.main
  (:use [hackamore core example])
  (:gen-class))

(defn -main [& args]
  (doto (context)
    (.addRoutes (move-text-files))
    (.start))
  (.join (Thread/currentThread)))
