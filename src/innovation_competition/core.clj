(ns innovation-competition.core
  ^{:author "Abhinav Sharma",
    :doc "Innovation Challenge - CLJ version"}
  (:require [clojure.edn :as edn]
            [clojure.data.json :as json]
            [clojure.set :as st]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; DONE


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

;; DONE
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
;; Exploring the structure of `ideas-data` and `users-data`
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(comment
  ;;  "All unique keys from `users-data`"
  (set (flatten (map keys users-data)))

  ;; "All unique keys from `ideas-data`"
  (set (flatten (map keys ideas-data))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Transformation and Normalization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; DONE


(defn average-score
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

;; DONE
(defn find-average-score-of-idea
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
;; DONE
(def ideas-final
  "The ideas with non-zero `average-score` values"
  (let [valid-ideas       (filter (fn [an-idea]
                                    (if (not= 0.0 (:average-score an-idea))
                                      an-idea))
                                  ideas-data-with-average-scores)]
    (map #(st/rename-keys %1 {:id :idea-id}) valid-ideas)))

(comment
  (take 5
        (group-by :author-id ideas-final))
  ;; frequency of ideas submitted by a specific user
  (frequencies (map :author-id ideas-final))
  (take 5
        (frequencies
         (group-by :author-id ideas-final)))
  (first ideas-final)
  (last ideas-final)
  (nth ideas-final 4)
  (count ideas-final))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DONE
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
  (count
   (filter (fn [a-user] (= "FreeFolk" (:house a-user))) users-data-with-houses))
  (first
   (filter (fn [a-user] (= "FreeFolk" (:house a-user))) users-data-with-houses))
  (last
   (filter (fn [a-user] (= "FreeFolk" (:house a-user))) users-data-with-houses))
  (nth users-data-with-houses 0)
  (take 10 users-data-with-houses)
  (count users-data-with-houses))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DONE
(defn split-double-houses
  "Function for handling the users belonging to two distinct houses.
  ```clojure
     (split-double-houses (nth users-data-with-houses 8))
  ```
  "
  [a-user]
  (cond
    (string? (:house a-user)) a-user
    (set? (:house a-user)) (let [double-houses (into [] (:house a-user))]
                             (for [a-house double-houses]
                               (assoc a-user :house a-house)))))

(comment
  (count
   (filter (fn [a-user] (set? (:house a-user))) users-data-with-houses))
  (first
   (filter (fn [a-user] (set? (:house a-user))) users-data-with-houses))
  (last
   (filter (fn [a-user] (set? (:house a-user))) users-data-with-houses))
  (split-double-houses (nth users-data-with-houses 11)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DONE
(def users-final
  "Represents the `users-data` with the normalized `:house` key."
  (flatten (map split-double-houses users-data-with-houses)))

(comment
  (take 10
        (flatten (map split-double-houses users-data-with-houses)))
  (nth users-final 13)
  (take 15 users-final)
  (first users-final)
  (last users-final)
  (count users-final)
  (take 10
        (sort-by :id users-final))
  (last
   (sort-by :id users-final))
  (spit "users_final.json"
        (json/write-str (sort-by :id users-final))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DONE
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
;; DONE
(defn user-and-house-info
  "A function to query the `users-final` with a given `:id`
  ```clojure
  (user-and-house-info \"user-53-0008852\"))
  ```
  "
  [a-user-id]
  (filter
   (fn [x]
     (= a-user-id (:id x)))
   users-final))

(comment
  (user-and-house-info "user-94-0002159")
  (user-and-house-info "user-32-0008735")
  (user-and-house-info (:id (nth users-final 5)))
  (user-and-house-info (:id (nth users-final 12))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn merge-idea-and-author
  "Function to join the normalized `ideas` and `users` data.
  ```clojure
  (merge-idea-and-author (first ideas-final))
  ```
  "
  [an-idea]
  (let [author-id (:author-id an-idea)
        authors (user-and-house-info author-id)]
    (map (fn [an-author]
           (dissoc (merge an-idea an-author) :id :scores))
         authors)))

(comment
  (merge-idea-and-author (nth ideas-final 3))
  (merge-idea-and-author (nth ideas-final 12)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ideas-and-authorship-data
  "Represents the normalized combination of the initial `users-data` with the `ideas-data` key."
  (flatten
   (map merge-idea-and-author ideas-final)))

(comment
  (count ideas-and-authorship-data)
  (count (flatten ideas-and-authorship-data))
  (nth ideas-and-authorship-data 5)
  (take 10 ideas-and-authorship-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn authored-by-user?
  "Checks whether the idea has been authored by a particular house"
  [a-user-id idea-and-author-pair]
  (if
   (= a-user-id
      (:author-id (:idea idea-and-author-pair)))
    true
    false))

(comment
  (authored-by-user? "user-81-0004613" (nth ideas-and-authorship-data 9))
  (authored-by-user? "user-78-0008498" (nth ideas-and-authorship-data 3))
  (authored-by-user? "user-11-0008134" (nth ideas-and-authorship-data 12)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; NOTE this is exactly where the mechanism between the pythonic solution an
;; clojure solution differs
;; Here the user-id is directly only associated with 3 ideas.
(defn ideas-by-a-user
  "Returns all the ideas which have been submitted by a particular house."
  [a-user-id]
  (filter #(authored-by-user? a-user-id %)
          ideas-and-authorship-data))

(comment
  (count
   (ideas-by-a-user "user-55-0008466"))
  (ideas-by-a-user "user-78-0008498")
  (count
   (ideas-by-a-user "user-11-0008134")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn authored-by-house?
  "Checks whether the idea has been authored by a particular house"
  [house-name idea-and-author-pair]
  (if
   (some #{house-name}
         (map :house (:authorship idea-and-author-pair)))
    true
    false))

(comment
  (authored-by-house? "Tully" (nth ideas-and-authorship-data 3))
  (authored-by-house? "Stark" (nth ideas-and-authorship-data 3))
  (authored-by-house? "Greyjoy" (nth ideas-and-authorship-data 2)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn ideas-by-a-house
  "Returns all the ideas which have been submitted by a particular house."
  [house-name]
  (filter #(authored-by-house? house-name %)
          ideas-and-authorship-data))

(comment
  (nth (ideas-by-a-house "Stark") 9)
  (count (ideas-by-a-house "Lannister"))
  (count (ideas-by-a-house "Stark"))
  (count (ideas-by-a-house "FreeFolk"))
  ;; FIXME
  (spit "ideas_by_house_stark.json"
        (json/write-str (map :idea (ideas-by-a-house "Stark")))))

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

