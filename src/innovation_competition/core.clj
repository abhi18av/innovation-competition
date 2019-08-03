(ns innovation-competition.core
  ^{:author "Abhinav Sharma",
    :doc "Innovation Challenge"}
  (:require [clojure.edn :as edn]
            [innovation-competition.method1 :as method1]
            [innovation-competition.method2 :as method2]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def ideas-data
  "Represents the EDN data from `ideas.edn`"
  (edn/read-string
   (slurp "./resources/ideas.edn")))


;;;;;;;;;;;;;;;;;


(def users-data
  "Represents the EDN data from `users.edn`"
  (edn/read-string
   (slurp "./resources/users.edn")))


