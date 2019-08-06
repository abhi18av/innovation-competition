(ns innovation-competition.scratch
  ^{:author "Abhinav Sharma",
    :doc "Innovation Challenge - CLJ version"}
  (:require [clojure.edn :as edn]
            [clojure.data.json :as json]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def ideas-data
  "Represents the EDN data from `ideas.edn`
  ```clojure
   (first ideas-data)
  ```
  "
  (edn/read-string
   (slurp "./resources/ideas.edn")))

(comment
  (nth ideas-data 5)
  (count ideas-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def users-data
  "Represents the EDN data from `users.edn`
  ```clojure
   (first users-data)
  ```
  "
  (edn/read-string
   (slurp "./resources/users.edn")))

(comment
  (nth users-data 5)
  (count users-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Exploring the structure of `ideas-data` and `users-data`
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def keys-in-users-data
  "All unique keys from `users-data`"
  (set (flatten (map keys users-data))))

(comment
  (count keys-in-users-data))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def keys-in-ideas-data
  "All unique keys from `ideas-data`"
  (set (flatten (map keys ideas-data))))

(comment
  (count keys-in-ideas-data))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Transformation and Normalization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- average-score
  "Util function to calculate averages.
  ```clojure
  (average-score [1 2 3 4])
  ```
  "
  [scores]
  (let [numeric-values  (filter some? scores)]
    (if (= [nil] scores)
        0.0
        (double
         (/ (reduce + numeric-values)
            (count numeric-values))))))


(comment
 (average-score [nil])
 (average-score [0])
 (average-score [0 nil])
 (average-score [0 1 nil])
 (average-score [1 2 3 4]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- find-average-score-of-idea
  "Calculates the average score, takes care of `nil` values.
  ```clojure
  (find-average-score-of-idea  (first ideas-data))
  ```
  "
  [an-idea]
  (average-score (:scores  an-idea)))

(comment
  (average-score (:scores (nth ideas-data 0)))
  (average-score (:scores (nth ideas-data 8))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ideas-data-with-average-scores
  "The `ideas-data` with the key `:average-score`."
  (map
   (fn [an-idea]
     (assoc an-idea :average-score (find-average-score-of-idea an-idea)))
   ideas-data))

(comment
  (nth ideas-data-with-average-scores 4)
  (count ideas-data-with-average-scores))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ideas-data-cleaned
  "The ideas with non-zero `average-score` values"
  (filter (fn [an-idea]
            (if (not= 0.0 (:average-score an-idea))
              an-idea))
          ideas-data-with-average-scores))

(comment
  (nth ideas-data-cleaned 4)
  (count ideas-data-cleaned))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def users-data-with-houses
  "The `users-data` normalized for `:house` being `FreeFolk` in place of `nil`.

  ```clojure
  (first users-data-with-houses)
  ```
  "
  (map
   (fn [a-user]
     (assoc a-user :house (:house a-user "FreeFolk")))
   users-data))

(comment
  (nth users-data-with-houses 4)
  (count users-data-with-houses))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- split-double-houses
  "Function for handling the users belonging to two distinct houses.
  ```clojure
     (make-unique-houses (nth users-data-with-houses 8))
  ```
  "
  [a-user]
  (cond
    (string? (:house a-user)) a-user
    (set? (:house a-user)) (let [double-houses (into [] (:house a-user))]
                             (for [a-house double-houses]
                               (assoc a-user :house a-house)))))

(comment
  (split-double-houses (nth users-data-with-houses 11)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def users-final
  "Represents the `users-data` with the normalized `:house` key."
  (flatten (map split-double-houses users-data-with-houses)))

(comment
  (nth users-final 4)
  (count users-final))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def set-of-house-names
  "Represents the set of all the names of the houses in the `users-final`"
  (set
   (map (fn [a-users-data]
          (name (:house a-users-data)))
        users-final)))

(comment
  (count set-of-house-names))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Merging the user and ideas data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- user-info
  "A function to query the `users-final` with a given `:id`
  ```clojure
  (user-info \"user-53-0008852\"))
  ```
  "
  [a-user-id]
  (filter
   (fn [x]
     (= a-user-id (:id x)))
   users-final))

(comment
  (user-info (:id (nth users-final 5)))
  (user-info (:id (nth users-final 12))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- join-ideas-and-users
  "Function to join the normalized `ideas` and `users` data.
  ```clojure
  (join-ideas-and-users (first ideas-data-with-average-scores))
  ```
  "
  [an-idea]
  (let [author-id (:author-id an-idea)]
    {:idea an-idea
     :authorship (user-info author-id)}))

(comment
  (join-ideas-and-users (nth ideas-data-with-average-scores 5)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ideas-and-authorship-data
  "Represents the combination of normalized `users-data` with the `ideas-data` key."
  (map join-ideas-and-users ideas-data-with-average-scores))

(comment
  (nth ideas-and-authorship-data 5))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- authored-by-house?
  "Checks whether the idea has been authored by a particular house"
  [house-name authorship-datapoint]
  (if
      (some #{house-name}
            (map :house (:authorship authorship-datapoint)))
   true
   false))

(comment
  (authored-by-house? "Tully" (nth ideas-and-authorship-data 3))
  (authored-by-house? "Stark" (nth ideas-and-authorship-data 3))
  (authored-by-house? "Greyjoy" (nth ideas-and-authorship-data 2)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- ideas-by-a-house
  "Returns all the ideas which have been submitted by a particular house."
  [house-name]
  (filter #(authored-by-house? house-name %)
          ideas-and-authorship-data))

(comment
  (nth (ideas-by-a-house "Stark") 9)
  (count (ideas-by-a-house "FreeFolk")))
;; FIXME
(spit "ideas_by_house_lannister.json"
      (json/write-str (map :idea (ideas-by-a-house "Lannister"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn house-info
  "Returns the solution relevant information for a particular house"
  [house-name]
  (let [count-of-ideas  (count (ideas-by-a-house house-name))
        average-scores-of-all-ideas (map
                                     (fn [an-idea]
                                       (get-in an-idea [:idea :average-score]))
                                     (ideas-by-a-house house-name))]

    {:house-name house-name
     :innovation-score (/ (reduce + average-scores-of-all-ideas) count-of-ideas)
     :number-of-ideas count-of-ideas}))

(comment
  (house-info "Stark"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Final solution
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def solution-data
  (map house-info set-of-house-names))

(comment
  (nth solution-data 5))

;;(clojure.pprint/pprint ideas-with-average-scores (clojure.java.io/writer "ideas-final.edn"))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; DONE
;[ ] a list of the houses, from most innovative to least innovative


(map :house-name
     (reverse
      (sort-by :innovation-score solution-data)))

(comment
  (join-ideas-and-users (nth ideas-data-with-average-scores 5)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; DONE
;[ ] the innovation score of each house

(map (fn [some-house-info]
       (select-keys some-house-info [:house-name :innovation-score]))
     solution-data)

(comment
  (join-ideas-and-users (nth ideas-data-with-average-scores 5)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



;; DONE
;[ ] the number of ideas submitted by each house

(map (fn [some-house-info]
       (select-keys some-house-info [:house-name :number-of-ideas]))
     solution-data)

(comment
  (join-ideas-and-users (nth ideas-data-with-average-scores 5)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
