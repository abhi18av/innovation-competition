(ns innovation-competition.lumo
  ^{:author "Abhinav Sharma",
    :doc "Innovation Challenge CLJS version"}
  (:require [cljs.reader :as reader]
            [clojure.set :as st]
            [lumo.io :as io]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; General outline of the solution
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; - Read in the data
;; - Address the issue of `nil` values in `idea scores`
;; - Address `double-houses` for users
;; - Associate the final forms of both datasets
;; - Print out the solution

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ideas-data
  "Represents the EDN data from `ideas.edn`
  ```clojure
   (first ideas-data)
  ```
  "
  (reader/read-string
   (io/slurp "../../resources/ideas.edn")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def users-data
  "Represents the EDN data from `users.edn`
  ```clojure
   (first users-data)
  ```
  "
  (reader/read-string
   (io/slurp "../../resources/users.edn")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Transformations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn find-average-score-of-idea
  "Calculates the average score, takes care of `nil` values.
  ```clojure
  (find-average-score-of-idea  (first ideas-data))
  ```
  "
  [an-idea]
  (average-score (:scores  an-idea)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ideas-data-with-average-scores
  "The `ideas-data` with the key `:average-score`."
  (map
   (fn [an-idea]
     (assoc an-idea :average-score (find-average-score-of-idea an-idea)))
   ideas-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ideas-final
  "The ideas with non-zero `average-score` values"
  (let [valid-ideas       (filter (fn [an-idea]
                                    (if (not= 0.0 (:average-score an-idea))
                                      an-idea))
                                  ideas-data-with-average-scores)]
    (map #(st/rename-keys %1 {:id :idea-id}) valid-ideas)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def users-final
  "Represents the `users-data` with the normalized `:house` key."
  (flatten (map split-double-houses users-data-with-houses)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def set-of-house-names
  "Represents the set of all the names of the houses in the `users-final`"
  (set
   (map (fn [a-users-data]
          (name (:house a-users-data)))
        users-final)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Merging the user and ideas data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ideas-and-authorship-data
  "Represents the normalized combination of the initial `users-data` with the `ideas-data`."
  (flatten
   (map merge-idea-and-author ideas-final)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn authored-by-user?
  "Checks whether the idea has been authored by a particular house"
  [a-user-id idea-and-author-pair]
  (if
      (= a-user-id
         (:author-id idea-and-author-pair))
    true
    false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ideas-by-a-user
  "Returns all the ideas which have been submitted by a particular house."
  [a-user-id]
  (filter #(authored-by-user? a-user-id %)
          ideas-and-authorship-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn authored-by-house?
  "Checks whether the idea has been authored by a particular house"
  [house-name idea-and-author-pair]
  (if
      (= house-name
         (:house idea-and-author-pair))
    true
    false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ideas-by-a-house
  "Returns all the ideas which have been submitted by a particular house."
  [house-name]
  (filter #(authored-by-house? house-name %)
          ideas-and-authorship-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn house-info
  "Returns the solution relevant information for a particular house"
  [house-name]
  (let [count-of-ideas  (count (ideas-by-a-house house-name))
        average-scores-of-all-ideas (map
                                     (fn [an-idea]
                                       (get an-idea :average-score))
                                     (ideas-by-a-house house-name))]

    {:house-name house-name
     :innovation-score (/ (reduce + average-scores-of-all-ideas) count-of-ideas)
     :number-of-ideas count-of-ideas}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Final solution
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def solution-data
  (map house-info set-of-house-names))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;[X] a list of the houses, from most innovative to least innovative
(map :house-name
     (reverse
      (sort-by :innovation-score solution-data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;[X] the innovation score of each house
(reverse (sort-by :innovation-score
                  (map (fn [some-house-info]
                         (select-keys some-house-info [:house-name :innovation-score]))
                       solution-data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;[X] the number of ideas submitted by each house
(map (fn [some-house-info]
       (select-keys some-house-info [:house-name :number-of-ideas]))
     solution-data)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Colored pretty printing for console via Lumo-cljs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; NOTE: Native colored output in Lumo-cljs via NodeJS printer
;; Reference: http://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html

(def ascii-colors
  {:red "\u001b[31m"
   :green "\u001b[32m"
   :yellow "\u001b[33m"
   :blue "\u001b[34m"
   :magenta "\u001b[35m"
   :cyan "\u001b[36m"
   :white "\u001b[37m"
   :reset "\u001b[0m"})


(defn colorized-output [color text]
  (with-out-str
    (js/console.log "\n########################################\n"
                    (str ((keyword color) ascii-colors)
                         text
                         (:reset ascii-colors))
                    "\n########################################\n")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DONE
;;[X] a list of the houses, from most innovative to least innovative

(def sorted-house-scores
  (map :house-name
       (reverse
        (sort-by :innovation-score solution-data))))

(colorized-output "cyan" "A list of the houses, from most innovative to least innovative")
(js/console.log (clj->js sorted-house-scores))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DONE
;; [X] the innovation score of each house

(def innovation-score-of-each-house
  (reverse (sort-by :innovation-score
                  (map (fn [some-house-info]
                         (select-keys some-house-info [:house-name :innovation-score]))
                       solution-data))))

(colorized-output "cyan" "The innovation score of each house")
(js/console.log (clj->js innovation-score-of-each-house))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DONE
;; [X] the number of ideas submitted by each house

(def number-of-ideas-per-house
 (reverse (sort-by :number-of-ideas
                   (map (fn [some-house-info]
                          (select-keys some-house-info [:house-name :number-of-ideas]))
                        solution-data))))


(colorized-output "cyan" "The number of ideas submitted by each house")
(js/console.log (clj->js number-of-ideas-per-house))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Thank you ;)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(colorized-output "cyan" "Thank you for participating :) ")

