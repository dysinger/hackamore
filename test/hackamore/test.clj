(ns hackamore.test
  (:import [org.apache.camel.component.mock MockEndpoint])
  (:use [clojure.test]
        [hackamore.core]))

(declare ^:dynamic *ctx*)

(defmacro ctx [r & body]
  `(let [c# (doto (context) (.addRoutes ~r) (.start))]
     (binding [*ctx* c#] ~@body)
     (.stop c#)))

(defn snd [msg]
  (-> *ctx*
      (.createProducerTemplate)
      (.sendBody "direct:start" msg)))

(deftest basic-route-test
  (ctx (route (.from "direct:start") (.to "mock:end"))
       (let [end (doto (.getEndpoint *ctx* "mock:end")
                   (.expectedBodiesReceived ["hello"]))]
         (snd "hello")
         (.assertIsSatisfied end))))

(deftest content-based-router-test
  (letfn [(hello [ex] (= "hello" (-> ex (.getIn) (.getBody))))
          (lol   [ex] (= "lol"   (-> ex (.getIn) (.getBody))))]
    (ctx (route (.from "direct:start")
                (.choice)
                (.when (pred hello)) (.to "mock:end1")
                (.when (pred lol))   (.to "mock:end2")
                (.otherwise)         (.to "mock:end3"))
         (let [end1 (doto (.getEndpoint *ctx* "mock:end1")
                      (.expectedBodiesReceived ["hello"]))
               end2 (doto (.getEndpoint *ctx* "mock:end2")
                      (.expectedBodiesReceived ["lol"]))
               end3 (doto (.getEndpoint *ctx* "mock:end3")
                      (.expectedBodiesReceivedInAnyOrder
                       ["cheezburger" "inurserver" "lolol"]))]
           (doseq [msg ["cheezburger" "hello" "inurserver" "lol" "lolol"]]
             (snd msg))
           (.assertIsSatisfied end1)
           (.assertIsSatisfied end2)
           (.assertIsSatisfied end3)))))

(deftest transformer-test
  (ctx (route (.from "direct:start")
              (.transform (expr #(str (-> % (.getIn) (.getBody)) "!")))
              (.to "mock:end"))
       (let [end (doto (.getEndpoint *ctx* "mock:end")
                   (.expectedBodiesReceivedInAnyOrder ["hello!" "kthxbai!"]))]
         (doseq [msg ["hello" "kthxbai"]]
           (snd msg))
         (.assertIsSatisfied end))))

(deftest processor-test
  (let [counter (atom 0)]
    (ctx (route (.from "direct:start")
                (.process (proc #(when (= "bingo!" (-> (.getIn %) (.getBody)))
                                   (swap! counter inc))))
                (.to "mock:end"))
         (doseq [msg ["lol" "bingo!" "ohai" "bingo!" "cheezeburger"]]
           (snd msg))
         (is (= @counter 2)))))

(deftest filter-test
  (ctx (route (.from "direct:start")
              (.filter (pred #(not (= "bad" (-> % (.getIn) (.getBody))))))
              (.to "mock:end"))
       (let [end (doto (.getEndpoint *ctx* "mock:end")
                   (.expectedBodiesReceivedInAnyOrder ["hello" "ohai" "lol"]))]
         (doseq [msg ["hello" "bad" "ohai" "bad" "lol"]]
           (snd msg))
         (.assertIsSatisfied end))))
