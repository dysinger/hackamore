(ns hackamore.example
  (:use [hackamore core]))

(defn print-headers
  [ex]
  (println (-> ex .getIn .getHeaders)))

(defn txt-file?
  [filename]
  (not (nil? (re-find #"\.txt" filename))))

(defn is-txt-file
  [ex]
  (txt-file? (-> ex .getIn (.getHeader "CamelFileName"))))

(defn move-text-files
  []
  (route (.from "file:///tmp/inbox")
         (.process (proc print-headers))
         (.filter (pred is-txt-file))
         (.to "file:///tmp/outbox")))
