(ns hackamore.test
  (:use [clojure.test]
        [hackamore context route processor]))

(deftest hackamore
  (testing "with a context"
    (let [ctx (context)]
      (testing "we should be able to define a simple route"
        (route ctx (from "vm:in") (to "vm:out"))
        (testing "and the messages should flow correctly"
          (start ctx)
          ;; send message
          (-> ctx
              (.createProducerTemplate)
              (.sendBody "vm:in" "ohai"))
          (is (= "ohai" (-> ctx
                            (.createConsumerTemplate)
                            (.receiveBody "vm:out"))))
          (stop ctx))))))
