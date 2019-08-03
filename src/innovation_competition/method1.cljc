(ns innovation-competition.method1
  ^{:author "Abhinav Sharma",
    :doc "Innovation Challenge"}
  (:require [clojure.edn :as edn]))


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



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Exploration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def keys-in-user-data
  "Collects all unique keys from the `users-data`"
  (set (flatten (map keys users-data))))

(def keys-in-ideas-data
  "Collects all unique keys from the `ideas-data`"
  (set (flatten (map keys ideas-data))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Transformation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ideas-data


(defn- average-score
  "Util function to calculate averages."
  [scores]
  (double
   (if (empty? scores)
     0
     (/ (reduce + scores) (count scores)))))

(defn- find-average-score-of-idea
  "Calculates the average score, takes care of `nil` values. "
  [an-idea]
  (average-score
   (filter
    (fn [x]
      (not (nil? x)))
    (:scores  an-idea))))

(def ideas-data-with-average-scores
  "The `ideas-data` with an extra key of `:average-score` "
  (map
   (fn [an-idea]
     (assoc an-idea :average-score (find-average-score-of-idea an-idea)))
   ideas-data))


;;;;;;;;;;;;;;;;;

;; users-data


(def users-data-with-houses
  "The `users-data` normalized for `:house` being `FreeFolk` in place of `nil`."
  (map
   (fn [a-user]
     (assoc a-user :house (:house a-user "FreeFolk")))
   users-data))

(defn- make-unique-houses
  "Function to "
  [a-user]
  (cond
    (string? (:house a-user)) a-user
    (set? (:house a-user)) (let [double-houses (into [] (:house a-user))]
                             (for [a-house double-houses]
                               (assoc a-user :house a-house)))))

(def users-final
  (flatten (map make-unique-houses users-data-with-houses)))

(def set-of-house-names
  (set
   (map (fn [a-users-data]
          (name (:house a-users-data)))
        users-final)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Merging the datasets
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- user-info [a-user-id]
  (filter
   (fn [x]
     (= a-user-id (:id x)))
   users-final))

(defn- join-ideas-and-users [an-idea]
  (let [author-id (:author-id an-idea)]
    {:idea an-idea
     :authorship (user-info author-id)}))

(def ideas-and-authorship-data
  (map join-ideas-and-users ideas-data-with-average-scores))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- authored-by-house [house-name authorship-datapoint]
  (if
   (some #{house-name} (map :house (:authorship authorship-datapoint))
    true
    false)))

(defn- ideas-by-a-house [house-name]
  (filter #(authored-by-house house-name %)
          ideas-and-authorship-data))

(defn- house-info [house-name]
  (let [count-of-ideas  (count (ideas-by-a-house house-name))
        scores-of-all-ideas (map
                             (fn [an-idea]
                               (get-in an-idea [:idea :scores]))
                             (ideas-by-a-house house-name))]

    {:house-name house-name
     :innovation-score (/ (reduce + scores-of-all-ideas) count-of-ideas)
     :number-of-ideas count-of-ideas}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Final solution
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def solution-data
  (map house-info set-of-house-names))


;; DONE
;[ ] a list of the houses, from most innovative to least innovative

(println
 (map :house-name
      (reverse
       (sort-by :innovation-score solution-data))))

;; DONE
;[ ] the innovation score of each house

(map (fn [some-house-info]
       (select-keys some-house-info [:house-name :innovation-score]))
     solution-data)

;; DONE
;[ ] the number of ideas submitted by each house

(map (fn [some-house-info]
       (select-keys some-house-info [:house-name :number-of-ideas]))
     solution-data)

