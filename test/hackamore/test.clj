(ns hackamore.test
  (:use [clojure.test]
        [hackamore context route processor]))

(deftest hackamore
  (testing "with a context"
    (let [ctx (context)]
      (testing "we should be able to define a simple route"
        (doto ctx
          (route (from "vm:a") (to "vm:b"))
          (start))
        (testing "and the messages should flow correctly"
          (-> ctx
              (.createProducerTemplate)
              (.sendBody "vm:a" "ohai"))
          (is (= "ohai" (-> ctx
                            (.createConsumerTemplate)
                            (.receiveBody "vm:b")))))
        (stop ctx)))))
