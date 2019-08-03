(ns innovation-competition.method1
  ^{:author "Abhinav Sharma",
    :doc "Innovation Challenge - CLJS version"}
  (:require [cljs.reader :as reader]
            [lumo.io :as io]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def ideas-data
  "Represents the EDN data from `ideas.edn`

  ```clojure
   (first ideas-data)

  =>  {:id \"idea-23-0003909\",
       :title \"Eius velit maiores molestiae error dolor odio est.\",
       :scores [nil 5 2],
       :body \"Omnis eum eos distinctio minima. Odio et natus et id tempora vitae sit eum. Dolores quibusdam enim omnis optio voluptatem sunt suscipit. Ut eos officia id atque unde porro.\",
        :author-id \"user-26-0005211\"}
  ```
  "
  (reader/read-string
   (io/slurp "../../resources/ideas.edn")))


;;;;;;;;;;;;;;;;;


(def users-data
  "Represents the EDN data from `users.edn`

  ```clojure
   (first users-data)

   => {:fname \"Olivia\",
       :lname \"Raymond\",
       :email \"annabelle75@me.com\",
       :id \"user-32-0008735\"}
  ```
  "
  (reader/read-string
   (io/slurp "../../resources/users.edn")))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Exploration
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def keys-in-user-data
  "Collects all unique keys from the `users-data`

  ```clojure
  keys-in-user-data
  => #{:email :fname :lname :id :house}
  ```
  "
  (set (flatten (map keys users-data))))

(def keys-in-ideas-data
  "Collects all unique keys from the `ideas-data`

  ```clojure
   keys-in-ideas-data
  => #{:title :id :author-id :scores :body}
  ```
  "
  (set (flatten (map keys ideas-data))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Transformation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ideas-data


(defn- average-score
  "Util function to calculate averages.
  ```clojure
  (average-score [1 2 3 4])
  => 2.5
  ```
  "
  [scores]
  (double
   (if (empty? scores)
     0
     (/ (reduce + scores) (count scores)))))

(defn- find-average-score-of-idea
  "Calculates the average score, takes care of `nil` values.

  ```clojure
  (find-average-score-of-idea  (first ideas-data))
  3.5
  ```
  "
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
  "The `users-data` normalized for `:house` being `FreeFolk` in place of `nil`.

  ```clojure
  (first users-data-with-houses)

  =>  {:fname \"Olivia\",
       :lname \"Raymond\",
       :email \"annabelle75@me.com\",
       :id \"user-32-0008735\",
       :house \"FreeFolk\"}
  ```
  "
  (map
   (fn [a-user]
     (assoc a-user :house (:house a-user "FreeFolk")))
   users-data))

(defn- split-double-houses
  "Function for handling the users belonging to two distinct houses.
  ```clojure

     (make-unique-houses (nth users-data-with-houses 8))

  => ({:fname \"Aaliyah\",
       :lname \"Ware\",
       :email \"aaron638@me.com\",
       :id \"user-20-0004583\",
       :house \"Lannister\"}
      {:fname \"Aaliyah\",
       :lname \"Ware\",
       :email \"aaron638@me.com\",
       :id \"user-20-0004583\",
       :house \"Greyjoy\"})

  ```
  "
  [a-user]
  (cond
    (string? (:house a-user)) a-user
    (set? (:house a-user)) (let [double-houses (into [] (:house a-user))]
                             (for [a-house double-houses]
                               (assoc a-user :house a-house)))))

(def users-final
  "Represents the `users-data` with the normalized `:house` key."
  (flatten (map split-double-houses users-data-with-houses)))

(def set-of-house-names
  "Represents the set of all the names of the houses in the `users-final`"
  (set
   (map (fn [a-users-data]
          (name (:house a-users-data)))
        users-final)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Merging the datasets
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- user-info
  "
  A function to query the `users-final` with a given `:id`
  ```clojure
  (user-info \"user-53-0008852\"))

  => ({:fname \"Jayden\",
       :lname \"Finch\",
       :email \"zbarker@me.com\",
       :id \"user-53-0008852\",
       :house \"Baratheon\"})

  ```
  "
  [a-user-id]
  (filter
   (fn [x]
     (= a-user-id (:id x)))
   users-final))

(defn- join-ideas-and-users
  "Function to join the normalized `ideas` and `users` data.

  ```clojure

  (join-ideas-and-users (first ideas-data-with-average-scores))

  => {:idea {:id \"idea-23-0003909\",
        :title \"Eius velit maiores molestiae error dolor odio est.\",
        :scores [nil 5 2],
        :body \"Omnis eum eos distinctio minima. Odio et natus et id tempora vitae sit eum. Dolores quibusdam enim omnis optio
  voluptatem sunt suscipit. Ut eos officia id atque unde porro.\",
        :author-id \"user-26-0005211\",
        :average-score 3.5},
  :authorship ({:fname \"Carson\",
               :lname \"Hoffman\",
               :email \"ealston@yahoo.com\",
               :id \"user-26-0005211\",
               :house \"Baratheon\"})}
  ```
  "
  [an-idea]
  (let [author-id (:author-id an-idea)]
    {:idea an-idea
     :authorship (user-info author-id)}))

(def ideas-and-authorship-data
  "Represents the combination of normalized `users-data` with the `ideas-data` key."
  (map join-ideas-and-users ideas-data-with-average-scores))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- authored-by-house?
  "Checks whether the idea has been authored by a particular house"
  [house-name authorship-datapoint]
  (if
   (some #{house-name} (map :house (:authorship authorship-datapoint)))
   true
   false))

(defn- ideas-by-a-house
  "Returns all the ideas which have been submitted by a particular house."
  [house-name]
  (filter #(authored-by-house? house-name %)
          ideas-and-authorship-data))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Final solution
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def solution-data
  (map house-info set-of-house-names))


;; DONE
;[ ] a list of the houses, from most innovative to least innovative

(map :house-name
     (reverse
      (sort-by :innovation-score solution-data)))

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




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Colored pretty printing for console via Lumo-cljs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



;; NOTE: Native colored output in Lumo-cljs via NodeJS
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

(defn colorized-log [color text]
  (str ((keyword color) ascii-colors) text (:reset ascii-colors)))

(colorized-log "red" "This is colored!")


;; DONE
;[ ] a list of the houses, from most innovative to least innovative

(with-out-str
 (js/console.log
  (colorized-log "red"
   (map :house-name
       (reverse
        (sort-by :innovation-score solution-data))))))

;; DONE
;[ ] the innovation score of each house

(with-out-str
  (js/console.log
   (colorized-log "green"
     (map (fn [some-house-info]
            (select-keys some-house-info [:house-name :innovation-score]))
          solution-data))))



;; DONE
;[ ] the number of ideas submitted by each house


(with-out-str
  (js/console.log
   (colorized-log "yellow"
                  (map (fn [some-house-info]
                         (select-keys some-house-info [:house-name :number-of-ideas]))
                       solution-data))))
