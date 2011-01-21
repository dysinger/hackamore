(ns hackamore.test
  (:use [clojure.test]
        [hackamore context route processor]))

(deftest hackamore
  (testing "a context with a simple a->b route"
    (let [ctx (context)]
      (doto ctx
        (route (from "vm:a") (to "vm:b"))
        (start))
      (testing "should be able to send msgs to a and see them on b"
        (-> ctx
            (.createProducerTemplate)
            (.sendBody "vm:a" "ohai"))
        (is (= "ohai"
               (-> ctx
                   (.createConsumerTemplate)
                   (.receiveBody "vm:b")))))
      (stop ctx))))
