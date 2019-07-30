(ns innovation-competition.method1
  (:require [clojure.edn :as edn]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def ideas-data
  (edn/read-string
   (slurp "./resources/ideas.edn")))


;;;;;;;;;;;;;;;;;


(def users-data
  (edn/read-string
   (slurp "./resources/users.edn")))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Exploration ;; TODO
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(def keys-in-user-data
  (set (flatten (map keys users-data))))

(def keys-in-ideas-data
  (set (flatten (map keys ideas-data))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Transformations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ideas-data


(defn average-score
  [scores]
  (double
   (if (empty? scores)
     0
     (/ (reduce + scores) (count scores)))))

(defn find-average-score-of-idea [an-idea]
  (average-score
   (filter
    (fn [x]
      (not (nil? x)))
    (:scores  an-idea))))

(def ideas-with-average-scores
  (map
   (fn [an-idea]
     (assoc an-idea :scores (find-average-score-of-idea an-idea)))
   ideas-data))


;;;;;;;;;;;;;;;;;

;; users-data


(def users-with-houses
  (map
   (fn [a-user]
     (assoc a-user :house (:house a-user "Free Folk")))
   users-data))

(defn make-unique-houses [a-user]
  (cond
    (string? (:house a-user)) a-user

    (set? (:house a-user)) (let [double-houses (into [] (:house a-user))]
                             (for [a-house double-houses]
                               (assoc a-user :house a-house)))))

(def users-final
  (flatten (map make-unique-houses users-with-houses)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Merging the datasets
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn user-info [a-user-id]
  (filter
   (fn [x]
     (= a-user-id (:id x)))
   users-final))

(defn join-ideas-and-users [an-idea]
  (let [author-id (:author-id an-idea)]
    {:idea an-idea
     :authorship (user-info author-id)}))

(def ideas-and-authorship-data
  (map join-ideas-and-users ideas-with-average-scores))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn belongs-to-house [house-name authorship-datapoint]
  (if
    (some #{house-name} (map :house (:authorship authorship-datapoint)))
    true
    false))

;; FIXME
(defn ideas-by-a-house [house-name]
  (filter #(belongs-to-house house-name %)
          ideas-and-authorship-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Final solutions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;[ ] an list of the houses, from most innovative to least innovative
;[ ] the innovation score of each house

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; [ ] the number of ideas submitted by each house
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(count (ideas-by-a-house "Stark"))

(count (ideas-by-a-house "Stark"))

;;;;;;
;; NOTE Scratch

