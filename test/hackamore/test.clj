(ns hackamore.test
  (:use [clojure.test]
        [hackamore context route processor]))

(deftest hackamore

  (testing "a context with a simple a->b route"
    (doto (context)
      (route (from "vm:a") (to "vm:b"))
      (start))
    (testing "should see messages sent ta 'a' should show up at 'b'"
      (-> ctx
          (.createProducerTemplate)
          (.sendBody "vm:a" "ohai"))
      (is (= "ohai" (-> ctx
                        (.createConsumerTemplate)
                        (.receiveBody "vm:b")))))
    (stop ctx)))
