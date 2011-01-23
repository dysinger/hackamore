(ns hackamore.test
  (:import [org.apache.camel.component.mock MockEndpoint])
  (:use [clojure.test]
        [hackamore.core]))

(declare #^:dynamic *ctx*)

(defmacro ctx [r & body]
  `(let [c# (doto (context) (.addRoutes ~r) (.start))]
     (binding [*ctx* c#] ~@body)
     (.stop c#)))

(defn snd [msg]
  (-> *ctx*
      (.createProducerTemplate)
      (.sendBody "direct:start" msg)))

(deftest simple
  (testing "a route (start->end)"
    (ctx (route (.from "direct:start") (.to "mock:end"))
         (testing "should result in hello->hello"
           (let [e (doto (.getEndpoint *ctx* "mock:end")
                     (.expectedBodiesReceived ["hello"]))]
             (snd "hello")
             (.assertIsSatisfied e))))))

(deftest content-based-router
  (testing "a route with a content-based-router (start->cbr->end1|end2|end3)"
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
                        (.expectedBodiesReceived ["bye"]))]
             (snd "bye")
             (snd "hello")
             (snd "lol")
             (testing "should result in hello->end1"
               (.assertIsSatisfied end1))
             (testing "should result in lol->end2"
               (.assertIsSatisfied end2))
             (testing "should result in bye->end3"
               (.assertIsSatisfied end3)))))))

(deftest transformer
  (testing "a route with a transformer (start->proc->end)"
    (ctx (route (.from "direct:start")
                (.process (proc #(let [msg (.getIn %)]
                                   (when (= (.getBody msg) "hello")
                                     (.setBody msg "ohai")))))
                (.to "mock:end"))
         (testing "should result in hello->ohai & kthxbai->kthxbai"
           (let [end (doto (.getEndpoint *ctx* "mock:end")
                       (.expectedBodiesReceived ["ohai" "kthxbai"]))]
             (snd "hello")
             (snd "kthxbai")
             (.assertIsSatisfied end))))))

(deftest filter
  (testing "a route with a filter (start->filter->end)"
    (ctx (route (.from "direct:start")
                (.filter (expr #(not (= "bad" (-> % (.getIn) (.getBody))))))
                (.to "mock:end"))
         (testing "should filter out 'bad' messages"
           (let [end (doto (.getEndpoint *ctx* "mock:end")
                       (.expectedBodiesReceivedInAnyOrder ["hello" "lol"]))]
             (snd "hello")
             (snd "bad")
             (snd "bad")
             (snd "lol")
             (.assertIsSatisfied end))))))
