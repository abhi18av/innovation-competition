(ns innovation-competition.benchmarking
  (:require [criterium.core :as criterium]
            [innovation-competition.scratch :as scratch]))


(scratch/add9 9)


(criterium/quick-benchmark
 (map scratch/add9 (range 10)
   :max-gc-attempts 3,
   :samples 3,
   :target-execution-time 100000000,
   :warmup-jit-period 5000000000,
   :tail-quantile 0.025,
   :bootstrap-size 500))



(time
  (map scratch/add9 (range 10)))

